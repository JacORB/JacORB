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

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.jacorb.notification.NoTranslationException;
import org.jacorb.notification.OfferManager;
import org.jacorb.notification.SubscriptionManager;
import org.jacorb.notification.TypedEventMessage;
import org.jacorb.notification.engine.TaskProcessor;
import org.jacorb.notification.interfaces.Message;
import org.jacorb.notification.queue.MessageQueueAdapter;
import org.jacorb.notification.queue.RWLockEventQueueDecorator;
import org.jacorb.notification.util.PropertySet;
import org.jacorb.notification.util.PropertySetAdapter;
import org.omg.CORBA.ARG_OUT;
import org.omg.CORBA.Any;
import org.omg.CORBA.BooleanHolder;
import org.omg.CORBA.InterfaceDef;
import org.omg.CORBA.InterfaceDefHelper;
import org.omg.CORBA.NO_IMPLEMENT;
import org.omg.CORBA.NVList;
import org.omg.CORBA.ORB;
import org.omg.CORBA.OperationDescription;
import org.omg.CORBA.ParameterMode;
import org.omg.CORBA.Repository;
import org.omg.CORBA.ServerRequest;
import org.omg.CORBA.InterfaceDefPackage.FullInterfaceDescription;
import org.omg.CosEventChannelAdmin.AlreadyConnected;
import org.omg.CosEventComm.Disconnected;
import org.omg.CosEventComm.PullConsumer;
import org.omg.CosNotification.DiscardPolicy;
import org.omg.CosNotification.EventTypeHelper;
import org.omg.CosNotification.OrderPolicy;
import org.omg.CosNotification.Property;
import org.omg.CosNotifyChannelAdmin.ConsumerAdmin;
import org.omg.CosNotifyChannelAdmin.ProxyType;
import org.omg.CosTypedNotifyChannelAdmin.TypedProxyPullSupplierHelper;
import org.omg.CosTypedNotifyChannelAdmin.TypedProxyPullSupplierOperations;
import org.omg.CosTypedNotifyChannelAdmin.TypedProxyPullSupplierPOATie;
import org.omg.DynamicAny.DynAny;
import org.omg.DynamicAny.DynAnyFactory;
import org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCode;
import org.omg.PortableServer.DynamicImplementation;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.Servant;

/**
 * @jmx.mbean extends = "AbstractProxySupplierMBean"
 * @jboss.xmbean
 * 
 * @author Alphonse Bendt
 * @version $Id$
 */

public class TypedProxyPullSupplierImpl extends AbstractProxySupplier implements
        TypedProxyPullSupplierOperations, ITypedProxy, TypedProxyPullSupplierImplMBean
{
    private final Any trueAny_;

    private final Any falseAny_;

    private final DynAnyFactory dynAnyFactory_;

    private final String supportedInterface_;

    private PullConsumer pullConsumer_;

    private TypedProxyPullSupplier typedProxyPullSupplierServant_;

    private org.omg.CORBA.Object typedProxyPullSupplier_;

    private final Map messageQueueMap_;

    private final Map invalidResponses_;

    private final Repository repository_;

    private class TypedProxyPullSupplier extends DynamicImplementation
    {
        private final String[] supportedInterfaces_ = new String[] { supportedInterface_ };

        public void invoke(final ServerRequest request)
        {
            String _operation = request.operation();

            boolean _isTryOp = false;
            if (_operation.startsWith("try_"))
            {
                _isTryOp = true;
                // cut 'try_' prefix
                _operation = _operation.substring(4);
            }

            try
            {
                final Message _mesg;

                final MessageQueueAdapter _queue = (MessageQueueAdapter) messageQueueMap_
                        .get(_operation);

                if (_isTryOp)
                {
                    _mesg = _queue.getMessageNoBlock();
                }
                else
                {
                    _mesg = _queue.getMessageBlocking();
                }

                try
                {
                    final NVList _args;

                    if (_mesg == null)
                    {
                        _args = (NVList) invalidResponses_.get(_operation);

                        if (_isTryOp)
                        {
                            request.set_result(falseAny_);
                        }
                    }
                    else
                    {
                        _args = prepareResponse(_mesg);

                        if (_isTryOp)
                        {
                            request.set_result(trueAny_);
                        }
                    }

                    request.arguments(_args);
                } finally
                {
                    if (_mesg != null)
                    {
                        _mesg.dispose();
                    }
                }
            } catch (InterruptedException e)
            {
                // ignore
            }
        }

        public String[] _all_interfaces(POA poa, byte[] oid)
        {
            return supportedInterfaces_;
        }

        public POA _default_POA()
        {
            return getPOA();
        }
    }

    private final NVList prepareResponse(Message mesg)
    {
        try
        {
            final Property[] _props = mesg.toTypedEvent();
            
            final NVList _args = getORB().create_list(_props.length - 1);

            // start at index 1 here. index 0 contains the operation name
            for (int x = 1; x < _props.length; ++x)
            {
                _args.add_value(_props[x].name, _props[x].value, ARG_OUT.value);
            }

            return _args;
        } catch (NoTranslationException e)
        {
            // cannot happen here
            // as there are no nontranslatable Messages queued.
            throw new RuntimeException();
        }
    }

    public TypedProxyPullSupplierImpl(ITypedAdmin admin, ConsumerAdmin consumerAdmin, ORB orb,
            POA poa, Configuration conf, TaskProcessor taskProcessor, OfferManager offerManager,
            SubscriptionManager subscriptionManager, DynAnyFactory dynAnyFactory,
            Repository repository) throws ConfigurationException
    {
        super(admin, orb, poa, conf, taskProcessor, offerManager,
                subscriptionManager, consumerAdmin);

        trueAny_ = orb.create_any();
        falseAny_ = orb.create_any();

        trueAny_.insert_boolean(true);
        falseAny_.insert_boolean(false);

        supportedInterface_ = admin.getSupportedInterface();

        dynAnyFactory_ = dynAnyFactory;
        repository_ = repository;

        qosSettings_.addPropertySetListener(
                new String[] { OrderPolicy.value, DiscardPolicy.value }, reconfigureEventQueues_);

        try
        {
            FullInterfaceDescription interfaceDescription = getInterfaceDescription();

            validateInterface(interfaceDescription);

            messageQueueMap_ = Collections
                    .unmodifiableMap(newMessageQueueMap(interfaceDescription));

            invalidResponses_ = Collections
                    .unmodifiableMap(newInvalidResponseMap(interfaceDescription));
        } catch (InconsistentTypeCode e)
        {
            throw new RuntimeException();
        } 
    }

    private void ensureMethodOnlyUsesOutParams(OperationDescription operation)
            throws IllegalArgumentException
    {
        int _noOfParameters = operation.parameters.length;

        for (int x = 0; x < _noOfParameters; ++x)
        {
            switch (operation.parameters[x].mode.value()) {
            case ParameterMode._PARAM_IN:
            // fallthrough
            case ParameterMode._PARAM_INOUT:
                throw new IllegalArgumentException("only OUT params allowed");
            case ParameterMode._PARAM_OUT:
                break;
            }
        }
    }

    private void prepareInvalidResponse(Map map, OperationDescription operation)
            throws InconsistentTypeCode
    {
        final NVList _expectedParams = getORB().create_list(operation.parameters.length);

        for (int x = 0; x < operation.parameters.length; ++x)
        {
            DynAny _dynAny = dynAnyFactory_
                    .create_dyn_any_from_type_code(operation.parameters[x].type);

            _expectedParams
                    .add_value(operation.parameters[x].name, _dynAny.to_any(), ARG_OUT.value);
        }

        map.put(operation.name, _expectedParams);
    }

    private final Map newMessageQueueMap(FullInterfaceDescription interfaceDescription)
    {
        Map map = new HashMap();

        for (int x = 0; x < interfaceDescription.operations.length; ++x)
        {
            if (!interfaceDescription.operations[x].name.startsWith("try_"))
            {
                logger_.debug("Create Queue for Operation: "
                        + interfaceDescription.operations[x].name);

                MessageQueueAdapter _messageQueue = 
                    getMessageQueueFactory().newMessageQueue(qosSettings_);

                map.put(interfaceDescription.operations[x].name, 
                        new RWLockEventQueueDecorator(_messageQueue));
            }
        }

        return map;
    }

    private final Map newInvalidResponseMap(FullInterfaceDescription interfaceDescription)
            throws InconsistentTypeCode
    {
        Map map = new HashMap();

        for (int x = 0; x < interfaceDescription.operations.length; ++x)
        {
            if (!interfaceDescription.operations[x].name.startsWith("try_"))
            {
                prepareInvalidResponse(map, interfaceDescription.operations[x]);
            }
        }

        return map;
    }

    private final void validateInterface(FullInterfaceDescription interfaceDescription)
    {
        for (int x = 0; x < interfaceDescription.operations.length; ++x)
        {
            ensureMethodOnlyUsesOutParams(interfaceDescription.operations[x]);
        }
    }

    private FullInterfaceDescription getInterfaceDescription()
    {
        InterfaceDef _interfaceDef = InterfaceDefHelper.narrow(repository_
                .lookup_id(supportedInterface_));

        return _interfaceDef.describe_interface();
    }

    private final void configureEventQueue()
    {
        try
        {
            Iterator i = messageQueueMap_.keySet().iterator();

            while (i.hasNext())
            {
                String _key = (String) i.next();

                RWLockEventQueueDecorator _queueAdapter = 
                    (RWLockEventQueueDecorator) messageQueueMap_.get(_key);

                MessageQueueAdapter _newQueue = getMessageQueueFactory().newMessageQueue(qosSettings_);

                _queueAdapter.replaceDelegate(_newQueue);
            }

        } catch (InterruptedException e)
        {
            throw new RuntimeException(e.getMessage());
        }
    }
    
    public int getPendingMessagesCount()
    {
        try
        {
            Iterator i = messageQueueMap_.keySet().iterator();

            int _count = 0;
            
            while (i.hasNext())
            {
                String _key = (String) i.next();

                RWLockEventQueueDecorator _queueAdapter = 
                    (RWLockEventQueueDecorator) messageQueueMap_.get(_key);

                _count += _queueAdapter.getPendingMessagesCount();
            }

            return _count;
        } catch (InterruptedException e)
        {
            return -1;
        }
    }

    private PropertySetAdapter reconfigureEventQueues_ = new PropertySetAdapter()
    { 
        public void actionPropertySetChanged(PropertySet source)
        {
            configureEventQueue();
        }
    };

    public Any pull() throws Disconnected
    {
        throw new NO_IMPLEMENT();
    }

    public Any try_pull(BooleanHolder booleanHolder) throws Disconnected
    {
        throw new NO_IMPLEMENT();
    }

    public void disconnect_pull_supplier()
    {
        destroy();
    }

    public void connect_typed_pull_consumer(PullConsumer pullConsumer) throws AlreadyConnected
    {
        checkIsNotConnected();

        connectClient(pullConsumer);

        pullConsumer_ = pullConsumer;
    }

    public org.omg.CORBA.Object get_typed_supplier()
    {
        if (typedProxyPullSupplierServant_ == null)
        {
            typedProxyPullSupplierServant_ = new TypedProxyPullSupplier();

            typedProxyPullSupplier_ = typedProxyPullSupplierServant_._this_object(getORB());
        }
        return typedProxyPullSupplier_;
    }

    public ProxyType MyType()
    {
        return ProxyType.PULL_TYPED;
    }


    public Servant getServant()
    {
        if (thisServant_ == null)
        {
            thisServant_ = new TypedProxyPullSupplierPOATie(this);
        }
        
        return thisServant_;
    }

    public void queueMessage(Message message)
    {
        try
        {
            Property[] _props = message.toTypedEvent();

            final String _fullQualifiedOperation;

            if (TypedEventMessage.OPERATION_NAME.equals(_props[0].name))
            {
                _fullQualifiedOperation = _props[0].value.extract_string();
            }
            else if (TypedEventMessage.EVENT_TYPE.equals(_props[0].name))
            {
                _fullQualifiedOperation = EventTypeHelper.extract(_props[0].value).type_name;
            }
            else
            {
                throw new IllegalArgumentException();
            }

            int idx = _fullQualifiedOperation.lastIndexOf("::");
            String _operation = _fullQualifiedOperation.substring(idx + 2);

            final Message _clonedMessage = (Message) message.clone();
           
            try
            {
                ((MessageQueueAdapter) messageQueueMap_.get(_operation)).enqeue(_clonedMessage);
            } catch (InterruptedException e)
            {
                _clonedMessage.dispose();
            }
        } catch (NoTranslationException e)
        {
            // ignore
            // Message is not delivered to the connected Consumer
        }
    }

    public void deliverPendingData()
    {
        // No Op as this Proxy is a PullSupplier
    }

    public void disconnectClient()
    {
        if (pullConsumer_ != null)
        {
            pullConsumer_.disconnect_pull_consumer();
            pullConsumer_ = null;
        }
    }
    
    protected long getCost()
    {
        return 0;
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