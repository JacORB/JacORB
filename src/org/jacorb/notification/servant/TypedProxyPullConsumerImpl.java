package org.jacorb.notification.servant;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2004  Gerald Brose.
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

import java.util.HashMap;
import java.util.Map;

import org.apache.avalon.framework.configuration.Configuration;
import org.jacorb.notification.MessageFactory;
import org.jacorb.notification.OfferManager;
import org.jacorb.notification.SubscriptionManager;
import org.jacorb.notification.engine.TaskProcessor;
import org.jacorb.notification.interfaces.Message;
import org.jacorb.notification.interfaces.MessageSupplier;
import org.omg.CORBA.ARG_OUT;
import org.omg.CORBA.Any;
import org.omg.CORBA.InterfaceDef;
import org.omg.CORBA.InterfaceDefHelper;
import org.omg.CORBA.NVList;
import org.omg.CORBA.ORB;
import org.omg.CORBA.OperationDescription;
import org.omg.CORBA.Request;
import org.omg.CORBA.InterfaceDefPackage.FullInterfaceDescription;
import org.omg.CosEventChannelAdmin.AlreadyConnected;
import org.omg.CosEventChannelAdmin.TypeError;
import org.omg.CosNotifyChannelAdmin.ProxyType;
import org.omg.CosNotifyChannelAdmin.SupplierAdmin;
import org.omg.CosTypedEventComm.TypedPullSupplier;
import org.omg.CosTypedNotifyChannelAdmin.TypedProxyPullConsumerHelper;
import org.omg.CosTypedNotifyChannelAdmin.TypedProxyPullConsumerOperations;
import org.omg.CosTypedNotifyChannelAdmin.TypedProxyPullConsumerPOATie;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.Servant;

/**
 * @Author Alphonse Bendt
 * @version $Id$
 */

public class TypedProxyPullConsumerImpl extends AbstractProxyConsumer implements
        TypedProxyPullConsumerOperations, MessageSupplier, ITypedProxy
{
    private String[] tryPullOperations_;

    private TypedPullSupplier pullSupplier_;

    private org.omg.CORBA.Object typedPullSupplier_;

    private InterfaceDef interfaceDef_;

    private final static Object[] STRING_ARRAY_TEMPLATE = new String[0];

    private final String expectedInterface_;

    private final Map operationDescriptions_ = new HashMap();

    private final Map fullQualifiedOperationNames_ = new HashMap();

    // ////////////////////////////

    public TypedProxyPullConsumerImpl(ITypedAdmin admin, SupplierAdmin supplierAdmin, ORB orb,
            POA poa, Configuration conf, TaskProcessor taskProcessor, MessageFactory messageFactory,
            OfferManager offerManager, SubscriptionManager subscriptionManager)
    {
        super(admin, orb, poa, conf, taskProcessor, messageFactory, supplierAdmin, offerManager,
                subscriptionManager);

        expectedInterface_ = admin.getSupportedInterface();
    }

    // ////////////////////////////

    public void connect_typed_pull_supplier(TypedPullSupplier typedPullSupplier)
            throws AlreadyConnected, TypeError
    {
        logger_.info("connect typed_pull_supplier");

        checkIsNotConnected();

        connectClient(typedPullSupplier);

        pullSupplier_ = typedPullSupplier;

        typedPullSupplier_ = pullSupplier_.get_typed_supplier();

        interfaceDef_ = InterfaceDefHelper.narrow(typedPullSupplier_._get_interface_def());

        if (interfaceDef_ == null)
        {
            throw new NullPointerException();
        }

        if (!typedPullSupplier_._is_a(expectedInterface_))
        {
            throw new TypeError();
        }
    }

    private String[] getTryPullOperations()
    {
        if (tryPullOperations_ == null)
        {
            FullInterfaceDescription _fullIfDescription = interfaceDef_.describe_interface();

            for (int x = 0; x < _fullIfDescription.operations.length; ++x)
            {
                if (_fullIfDescription.operations[x].name.startsWith("try_"))
                {
                    operationDescriptions_.put(_fullIfDescription.operations[x].name,
                            _fullIfDescription.operations[x]);
                }
            }
            
            tryPullOperations_ = (String[]) operationDescriptions_.keySet().toArray(
                    STRING_ARRAY_TEMPLATE);
        }
        
        return tryPullOperations_;
    }

    public void runPullMessage()
    {
        runPullMessageInternal();
    }

    private OperationDescription getOperationDescription(String operation)
    {
        return (OperationDescription) operationDescriptions_.get(operation);
    }

    private String getFullQualifiedName(String operation)
    {
        String _fullQualifiedName = (String) fullQualifiedOperationNames_.get(operation);
        if (_fullQualifiedName == null)
        {
            _fullQualifiedName = interfaceDef_.lookup(operation).absolute_name();
            fullQualifiedOperationNames_.put(operation, _fullQualifiedName);
        }
        return _fullQualifiedName;
    }

    private Request prepareRequest(String operation)
    {
        Request _request = typedPullSupplier_._request(operation);

        NVList _args = _request.arguments();

        OperationDescription _operationDescription = getOperationDescription(operation);

        for (int x = 0; x < _operationDescription.parameters.length; ++x)
        {
            Any _any = getORB().create_any();

            _any.type(_operationDescription.parameters[x].type);

            _args.add_value(_operationDescription.parameters[x].name, _any, ARG_OUT.value);
        }

        _request.set_return_type(_operationDescription.result);

        return _request;
    }

    private void runPullMessageInternal()
    {
        String[] _tryPullOperations = getTryPullOperations();

        for (int x = 0; x < _tryPullOperations.length; ++x)
        {
            Request _request = prepareRequest(_tryPullOperations[x]);

            if (logger_.isDebugEnabled())
            {
                logger_.debug("invoke " + _tryPullOperations[x]);
            }

            try
            {
                _request.invoke();

                Any _result = _request.result().value();

                boolean _success = _result.extract_boolean();

                if (_success)
                {
                    String _operationNameWithoutTry = _tryPullOperations[x].substring(4);
                    String _operationName = getFullQualifiedName(_operationNameWithoutTry);

                    Message _mesg = getMessageFactory().newMessage(expectedInterface_,
                            _operationName, _request.arguments(), this);

                    checkMessageProperties(_mesg);

                    processMessage(_mesg);
                }
            } catch (Exception e)
            {
                logger_.error("An error occured while invoking " + _tryPullOperations[x], e);
            }
        }
    }

    public void disconnect_pull_consumer()
    {
        destroy();
    }

    public ProxyType MyType()
    {
        return ProxyType.PULL_TYPED;
    }

    public org.omg.CORBA.Object activate()
    {
        return TypedProxyPullConsumerHelper.narrow(getServant()._this_object(getORB()));
    }

    public void disconnectClient()
    {
        if (pullSupplier_ != null)
        {
            pullSupplier_.disconnect_pull_supplier();
            pullSupplier_ = null;
        }
    }

    public Servant getServant()
    {
        if (thisServant_ == null)
        {
            thisServant_ = new TypedProxyPullConsumerPOATie(this);
        }
        return thisServant_;
    }
}