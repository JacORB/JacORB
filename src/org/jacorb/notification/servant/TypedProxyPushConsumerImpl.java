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

import org.omg.CORBA.Any;
import org.omg.CORBA.InterfaceDef;
import org.omg.CORBA.InterfaceDefHelper;
import org.omg.CORBA.InterfaceDefPackage.FullInterfaceDescription;
import org.omg.CORBA.NVList;
import org.omg.CORBA.ORB;
import org.omg.CORBA.OperationDescription;
import org.omg.CORBA.ParameterMode;
import org.omg.CORBA.Repository;
import org.omg.CORBA.ServerRequest;
import org.omg.CosEventChannelAdmin.AlreadyConnected;
import org.omg.CosEventComm.Disconnected;
import org.omg.CosEventComm.PushSupplier;
import org.omg.CosNotifyChannelAdmin.ProxyType;
import org.omg.CosTypedNotifyChannelAdmin.TypedProxyPushConsumerHelper;
import org.omg.CosTypedNotifyChannelAdmin.TypedProxyPushConsumerOperations;
import org.omg.CosTypedNotifyChannelAdmin.TypedProxyPushConsumerPOATie;
import org.omg.PortableServer.DynamicImplementation;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.Servant;

import org.jacorb.notification.interfaces.Message;
import org.omg.CORBA.NO_IMPLEMENT;
import org.omg.CORBA.RepositoryHelper;
import org.omg.CORBA.ORBPackage.InvalidName;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */
public class TypedProxyPushConsumerImpl
    extends AbstractProxyConsumer
    implements TypedProxyPushConsumerOperations {

    private String supportedInterface_;

    private TypedProxyPushConsumer typedProxyPushConsumerServant_;

    private PushSupplier pushSupplier_;

    private FullInterfaceDescription interfaceDescription_;

    private class TypedProxyPushConsumer extends DynamicImplementation {
        String[] supportedInterfaces_;

        TypedProxyPushConsumer() {
            supportedInterfaces_ = new String[] {supportedInterface_};
        }

        public void invoke(ServerRequest request) {
            NVList _params = getExpectedParamList(request.operation());

            request.arguments(_params);

            Message _mesg = getMessageFactory().newMessage( supportedInterface_,
                                                            request.operation(),
                                                            _params,
                                                            TypedProxyPushConsumerImpl.this );

            getTaskProcessor().processMessage(_mesg);
        }

        public String[] _all_interfaces(POA poa, byte[] oid) {
            return supportedInterfaces_;
        }


        public POA _default_POA() {
            return getPOA();
        }
    }


    private OperationDescription getOperationDescription(String operation) {

        for (int x=0; x<interfaceDescription_.operations.length; ++x) {

            if (operation.equals(interfaceDescription_.operations[x].name)) {
                return interfaceDescription_.operations[x];
            }
        }

        return null;
    }


    private NVList getExpectedParamList(String operation) {
        OperationDescription _operation = getOperationDescription(operation);

        NVList _expectedParams = getORB().create_list(_operation.parameters.length);

        for (int x=0; x<_operation.parameters.length; ++x) {

            Any _value = getORB().create_any();

            _value.type(_operation.parameters[x].type);

            _expectedParams.add_value(_operation.parameters[x].name,
                                      _value,
                                      ParameterMode._PARAM_IN);
        }

        return _expectedParams;
    }


    public TypedProxyPushConsumerImpl( String supportedInterface) {
        super();

        supportedInterface_ = supportedInterface;
    }


    private void ensureOperationOnlyUsesInParams() throws IllegalArgumentException {
        for (int x=0; x<interfaceDescription_.operations.length; ++x) {
            int _noOfParameters = interfaceDescription_.operations[x].parameters.length;

            for (int y=0; y < _noOfParameters; ++y) {
                switch(interfaceDescription_.operations[x].parameters[y].mode.value()) {
                case ParameterMode._PARAM_IN:
                    break;
                case ParameterMode._PARAM_INOUT:
                    // fallthrough
                case ParameterMode._PARAM_OUT:
                    throw new IllegalArgumentException("only IN params allowed");
                }
            }
        }
    }


    public void preActivate() throws Exception, InvalidName {
        super.preActivate();

        Repository _repository =
            RepositoryHelper.narrow(getORB().resolve_initial_references("InterfaceRepository"));

        InterfaceDef _interfaceDef =
            InterfaceDefHelper.narrow(_repository.lookup_id(supportedInterface_));

        interfaceDescription_ = _interfaceDef.describe_interface();

        ensureOperationOnlyUsesInParams();
    }


    public ProxyType MyType()
    {
        return ProxyType.PUSH_TYPED;
    }


    public void connect_typed_push_supplier(PushSupplier pushSupplier) throws AlreadyConnected {
        logger_.info( "connect typed_push_supplier" );

        assertNotConnected();

        connectClient(pushSupplier);

        pushSupplier_ = pushSupplier;
    }


    public void push(Any any) throws Disconnected {
        throw new NO_IMPLEMENT();
    }


    public org.omg.CORBA.Object get_typed_consumer() {
        if (typedProxyPushConsumerServant_ == null) {
            typedProxyPushConsumerServant_ = new TypedProxyPushConsumer();
        }

        return typedProxyPushConsumerServant_._this_object(getORB());
    }


    public void disconnect_push_consumer() {
        dispose();
    }


    public void disconnectClient() {
        if (pushSupplier_ != null) {
            pushSupplier_.disconnect_push_supplier();
            pushSupplier_ = null;
        }
    }


    public Servant getServant() {
        if (thisServant_ == null)
            {
                thisServant_ = new TypedProxyPushConsumerPOATie( this );
            }
        return thisServant_;
    }


    public org.omg.CORBA.Object activate()
    {
        return TypedProxyPushConsumerHelper.narrow( getServant()._this_object(getORB()) );
    }
}
