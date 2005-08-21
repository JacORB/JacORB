package org.jacorb.notification.servant;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2003  Gerald Brose.
 *
 *   This library is free software; you can redistribute it and/or
 *   modify it under the terms of the GNU Library General Public
 *   License as published by the Free Software Foundation; either
 *   version 2 of the License, or (at your option) any later version.
 *
 *   This library is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *   Library General Public License for more details.
 *
 *   You should have received a copy of the GNU Library General Public
 *   License along with this library; if not, write to the Free
 *   Software Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

import java.util.List;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.jacorb.notification.NoTranslationException;
import org.jacorb.notification.OfferManager;
import org.jacorb.notification.SubscriptionManager;
import org.jacorb.notification.TypedEventMessage;
import org.jacorb.notification.engine.PushOperation;
import org.jacorb.notification.engine.PushTaskExecutorFactory;
import org.jacorb.notification.engine.TaskProcessor;
import org.jacorb.notification.interfaces.Message;
import org.jacorb.notification.interfaces.MessageConsumer;
import org.omg.CORBA.ARG_IN;
import org.omg.CORBA.NVList;
import org.omg.CORBA.ORB;
import org.omg.CORBA.Request;
import org.omg.CORBA.TCKind;
import org.omg.CORBA.TypeCode;
import org.omg.CosEventChannelAdmin.AlreadyConnected;
import org.omg.CosEventChannelAdmin.TypeError;
import org.omg.CosEventComm.Disconnected;
import org.omg.CosNotification.EventTypeHelper;
import org.omg.CosNotification.Property;
import org.omg.CosNotifyChannelAdmin.ConsumerAdmin;
import org.omg.CosNotifyChannelAdmin.ProxyType;
import org.omg.CosTypedEventComm.TypedPushConsumer;
import org.omg.CosTypedNotifyChannelAdmin.TypedProxyPushSupplierHelper;
import org.omg.CosTypedNotifyChannelAdmin.TypedProxyPushSupplierOperations;
import org.omg.CosTypedNotifyChannelAdmin.TypedProxyPushSupplierPOATie;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.Servant;

/**
 * @jmx.mbean extends = "AbstractProxyPushSupplierMBean"
 * @jboss.xmbean
 * 
 * @author Alphonse Bendt
 * @version $Id$
 */

public class TypedProxyPushSupplierImpl extends AbstractProxyPushSupplier implements
        TypedProxyPushSupplierOperations, ITypedProxy, TypedProxyPushSupplierImplMBean
{
    private class PushTypedOperation implements PushOperation 
    {
        private final Request request_;

        public PushTypedOperation(Request request) {
            request_ = request;
        }

        public void invokePush() throws Disconnected {
            deliverMessageInternal(request_);
        }

        public void dispose() {
            // No Op
        }
    }
    
    private TypedPushConsumer pushConsumer_;

    private org.omg.CORBA.Object typedConsumer_;

    private static final TypeCode TYPE_CODE_VOID = ORB.init().get_primitive_tc(TCKind.tk_void);

    private final String supportedInterface_;

    private long timeSpent_ = 0;

    public TypedProxyPushSupplierImpl(ITypedAdmin admin, ConsumerAdmin consumerAdmin, ORB orb,
            POA poa, Configuration conf, TaskProcessor taskProcessor, PushTaskExecutorFactory pushTaskExecutorFactory,
            OfferManager offerManager, SubscriptionManager subscriptionManager)
            throws ConfigurationException
    {
        super(admin, orb, poa, conf, taskProcessor, pushTaskExecutorFactory, offerManager,
                subscriptionManager, consumerAdmin);

        supportedInterface_ = admin.getSupportedInterface();
    }

    public void disconnect_push_supplier()
    {
        destroy();
    }

    public void connect_typed_push_consumer(TypedPushConsumer typedPushConsumer)
            throws AlreadyConnected, TypeError
    {
        logger_.info("connect typed_push_supplier");

        checkIsNotConnected();

        connectClient(typedPushConsumer);

        pushConsumer_ = typedPushConsumer;

        typedConsumer_ = pushConsumer_.get_typed_consumer();

        if (!typedConsumer_._is_a(supportedInterface_))
        {
            throw new TypeError();
        }
    }

    public ProxyType MyType()
    {
        return ProxyType.PUSH_TYPED;
    }


    public org.omg.CORBA.Object activate()
    {
        return TypedProxyPushSupplierHelper.narrow(getServant()._this_object(getORB()));
    }

    public void isIDLAssignable(final String ifName) throws IllegalArgumentException
    {
        if (typedConsumer_._is_a(ifName))
        {
            return;
        }

        if (ifName.indexOf("Pull") > 0)
        {
            int idx = ifName.indexOf("Pull");

            StringBuffer _nonPullIF = new StringBuffer();
            _nonPullIF.append(ifName.substring(0, idx));
            _nonPullIF.append(ifName.substring(idx + 4));

            if (typedConsumer_._is_a(_nonPullIF.toString()))
            {
                return;
            }
        }

        throw new IllegalArgumentException();
    }


    public void pushPendingData()
    {
        final Message[] messages = getAllMessages();

        for (int i = 0; i < messages.length; ++i)
        {
            try
            {
                deliverMessageWithRetry(messages[i]);
            } finally
            {
                messages[i].dispose();
            }
        }
    }

    private void deliverMessageWithRetry(Message message)
    {
        try
        {
            final Property[] _props = message.toTypedEvent();

            final String _fullQualifiedOperation;

            if (TypedEventMessage.OPERATION_NAME.equals(_props[0].name))
            {
                _fullQualifiedOperation = _props[0].value.extract_string();
            }
            else if (TypedEventMessage.EVENT_TYPE.equals(_props[0].name))
            {
                _fullQualifiedOperation = EventTypeHelper.extract(_props[0].value).type_name;

                String _idlType = EventTypeHelper.extract(_props[0].value).domain_name;

                isIDLAssignable(_idlType);
            }
            else
            {
                throw new IllegalArgumentException();
            }

            int _idx = _fullQualifiedOperation.lastIndexOf("::");
            final String _operation = _fullQualifiedOperation.substring(_idx + 2);

            final Request _request = typedConsumer_._request(_operation);

            final NVList _arguments = _request.arguments();

            for (int x = 1; x < _props.length; ++x)
            {
                _arguments.add_value(_props[x].name, _props[x].value, ARG_IN.value);
            }

            _request.set_return_type(TYPE_CODE_VOID);

            try
            {
                deliverMessageInternal(_request);
            } catch (Exception t)
            {
                PushTypedOperation _failedOperation = new PushTypedOperation(_request);

                handleFailedPushOperation(_failedOperation, t);
            }
        } catch (NoTranslationException e)
        {
            // ignore
            // nothing will be delivered to the consumer

            logger_.info("No Translation possible", e);
        }
    }

    void deliverMessageInternal(final Request request)
    {
        long now = System.currentTimeMillis();
        request.invoke();
        timeSpent_ += (System.currentTimeMillis() - now);
        resetErrorCounter();
    }

    protected void disconnectClient()
    {
        if (pushConsumer_ != null)
        {
            pushConsumer_.disconnect_push_consumer();
            pushConsumer_ = null;
        }
    }

    public synchronized Servant getServant()
    {
        if (thisServant_ == null)
        {
            thisServant_ = new TypedProxyPushSupplierPOATie(this);
        }

        return thisServant_;
    }

    protected long getCost()
    {
        return timeSpent_;
    }
    
    /**
     * @jmx.managed-attribute
     *                        access = "read-only"
     */
    public String getSupportedInterface()
    {
        return supportedInterface_;
    }
}