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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jacorb.notification.NoTranslationException;
import org.jacorb.notification.TypedEventMessage;
import org.jacorb.notification.interfaces.Message;
import org.jacorb.notification.interfaces.MessageConsumer;
import org.jacorb.notification.queue.EventQueue;
import org.jacorb.notification.util.PropertySet;
import org.jacorb.notification.util.PropertySetListener;
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
import org.omg.CORBA.RepositoryHelper;
import org.omg.CORBA.ServerRequest;
import org.omg.CORBA.InterfaceDefPackage.FullInterfaceDescription;
import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.CosEventChannelAdmin.AlreadyConnected;
import org.omg.CosEventComm.Disconnected;
import org.omg.CosEventComm.PullConsumer;
import org.omg.CosNotification.DiscardPolicy;
import org.omg.CosNotification.EventTypeHelper;
import org.omg.CosNotification.OrderPolicy;
import org.omg.CosNotification.Property;
import org.omg.CosNotification.UnsupportedQoS;
import org.omg.CosNotifyChannelAdmin.ProxyType;
import org.omg.CosTypedNotifyChannelAdmin.TypedProxyPullSupplierHelper;
import org.omg.CosTypedNotifyChannelAdmin.TypedProxyPullSupplierOperations;
import org.omg.CosTypedNotifyChannelAdmin.TypedProxyPullSupplierPOATie;
import org.omg.DynamicAny.DynAny;
import org.omg.DynamicAny.DynAnyFactory;
import org.omg.DynamicAny.DynAnyFactoryHelper;
import org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCode;
import org.omg.PortableServer.DynamicImplementation;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.Servant;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */

public class TypedProxyPullSupplierImpl
    extends AbstractProxySupplier
    implements TypedProxyPullSupplierOperations
{
    private final Any trueAny_;

    private final Any falseAny_;

    private DynAnyFactory dynAnyFactory_;

    private String supportedInterface_;

    private FullInterfaceDescription fullInterfaceDescription_;

    private PullConsumer pullConsumer_;

    private TypedProxyPullSupplier typedProxyPullSupplierServant_;

    private org.omg.CORBA.Object typedProxyPullSupplier_;

    private final Map eventQueueMap_ = new HashMap();

    private final Map invalidResponses_ = new HashMap();

    private class TypedProxyPullSupplier extends DynamicImplementation
    {
        private String[] supportedInterfaces_ =
            new String[] {supportedInterface_};

        public void invoke(ServerRequest request)
        {
            String _operation = request.operation();

            boolean _isTryOp = false;
            if (_operation.startsWith("try_"))
                {
                    _isTryOp = true;
                    // cut 'try_' prefix
                    _operation = _operation.substring(4);
                }

            EventQueue _queue = (EventQueue)eventQueueMap_.get(_operation);

            Message _mesg = null;

            try {
                _mesg = _queue.getEvent(!_isTryOp);

                NVList _args = null;

                if (_mesg == null)
                    {
                        _args = (NVList)invalidResponses_.get(_operation);

                        if (_isTryOp)
                            {
                                request.set_result(falseAny_);
                            }
                    }
                else
                    {
                        _args = prepareResponse(_operation, _mesg);

                        if (_isTryOp)
                            {
                                request.set_result(trueAny_);
                            }
                    }
                request.arguments(_args);
            } catch (InterruptedException e) {
            } finally {
                if (_mesg != null) {
                    _mesg.dispose();
                }
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


    private NVList prepareResponse(String operation, Message mesg)
    {
        NVList _args = null;

        try
        {
            Property[] _props = mesg.toTypedEvent();

            _args = getORB().create_list(_props.length - 1);

            // start at index 1 here. index 0 contains operation name
            for (int x = 1; x < _props.length; ++x)
            {
                _args.add_value(_props[x].name, _props[x].value, ARG_OUT.value);
            }
        }
        catch (NoTranslationException e)
        {
            // cannot happen here
            // as there are no nontranslatable Messages queued.
        }

        return _args;
    }


    public TypedProxyPullSupplierImpl(String supportedInterface)
    {
        super();

        supportedInterface_ = supportedInterface;

        trueAny_ = ORB.init().create_any();
        falseAny_ = ORB.init().create_any();

        trueAny_.insert_boolean(true);
        falseAny_.insert_boolean(false);
    }


    private void ensureMethodOnlyUsesOutParams(OperationDescription operation)
        throws IllegalArgumentException
    {
        int _noOfParameters = operation.parameters.length;

        for (int x = 0; x < _noOfParameters; ++x)
        {
            switch (operation.parameters[x].mode.value())
            {
                case ParameterMode._PARAM_IN:
                    // fallthrough
                case ParameterMode._PARAM_INOUT:
                    throw new IllegalArgumentException("only OUT params allowed");
                case ParameterMode._PARAM_OUT:
                    break;
            }
        }
    }

    private void prepareInvalidResponse(OperationDescription operation) throws InconsistentTypeCode
    {
        NVList _expectedParams =
            getORB().create_list(operation.parameters.length);

        for (int x = 0; x < operation.parameters.length; ++x)
        {

            DynAny _dynAny = dynAnyFactory_.create_dyn_any_from_type_code(operation.parameters[x].type);

            _expectedParams.add_value(operation.parameters[x].name,
                                      _dynAny.to_any(),
                                      ARG_OUT.value );
        }

        invalidResponses_.put(operation.name, _expectedParams);
    }


    public void preActivate() throws UnsupportedQoS, InconsistentTypeCode, InvalidName
    {
        // do not call super.preActivate() here !

        dynAnyFactory_ =
            DynAnyFactoryHelper.narrow(getORB().resolve_initial_references("DynAnyFactory"));

        Repository _repository =
            RepositoryHelper.narrow(getORB().resolve_initial_references("InterfaceRepository"));

        InterfaceDef _interfaceDef =
            InterfaceDefHelper.narrow(_repository.lookup_id(supportedInterface_));

        fullInterfaceDescription_ = _interfaceDef.describe_interface();

        List _allQueues = new ArrayList(fullInterfaceDescription_.operations.length);

        for (int x = 0; x < fullInterfaceDescription_.operations.length; ++x)
        {
            ensureMethodOnlyUsesOutParams(fullInterfaceDescription_.operations[x]);

            if (!fullInterfaceDescription_.operations[x].name.startsWith("try_"))
            {
                logger_.debug("Create Queue for Operation: " + fullInterfaceDescription_.operations[x].name);

                EventQueue _eventQueue = getEventQueueFactory().newEventQueue(qosSettings_);

                _allQueues.add(_eventQueue);

                eventQueueMap_.put(fullInterfaceDescription_.operations[x].name, _eventQueue);

                prepareInvalidResponse(fullInterfaceDescription_.operations[x]);
            }
        }

        qosSettings_.addPropertySetListener(new String[] {OrderPolicy.value,
                                                          DiscardPolicy.value},
                                            reconfigureEventQueues_);

    }

    private void configureEventQueue() throws UnsupportedQoS
    {
        try
        {
            synchronized (eventQueueMap_)
            {
                Iterator i = eventQueueMap_.keySet().iterator();

                while (i.hasNext())
                {
                    String _key = (String)i.next();

                    EventQueue _queue = (EventQueue)eventQueueMap_.get(_key);

                    EventQueue _newQueue =
                        getEventQueueFactory().newEventQueue( qosSettings_ );

                    if (!_queue.isEmpty())
                    {
                        Message[] _allEvents = _queue.getAllEvents(true);

                        for (int x = 0; x < _allEvents.length; ++x)
                        {
                            _newQueue.put(_allEvents[x]);
                        }
                    }

                    eventQueueMap_.put(_key, _newQueue);
                }
            }
        }
        catch (InterruptedException e)
        {
            throw new RuntimeException(e.getMessage());
        }
    }

    private PropertySetListener reconfigureEventQueues_ =
        new PropertySetListener()
        {
            public void validateProperty(Property[] props, List errors)
            {}

            public void actionPropertySetChanged(PropertySet source)
            throws UnsupportedQoS
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
        dispose();
    }


    public void connect_typed_pull_consumer(PullConsumer pullConsumer) throws AlreadyConnected
    {
        assertNotConnected();

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


    public List getSubsequentFilterStages()
    {
        return null;
    }


    public boolean hasMessageConsumer()
    {
        return true;
    }


    public MessageConsumer getMessageConsumer()
    {
        return this;
    }


    public Servant getServant()
    {
        if (thisServant_ == null)
        {
            thisServant_ = new TypedProxyPullSupplierPOATie(this);
        }
        return thisServant_;
    }


    public org.omg.CORBA.Object activate()
    {
        return TypedProxyPullSupplierHelper.narrow(getServant()._this_object(getORB()));
    }


    public void deliverMessage(Message message)
    {
        try
        {
            Property[] _props = message.toTypedEvent();

            String _fullQualifiedOperation = null;

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

            ((EventQueue)eventQueueMap_.get(_operation)).put((Message)message.clone());
        }
        catch (NoTranslationException e)
        {
            // ignore
            // Message is not delivered to the connected Consumer
        }
    }


    public void deliverPendingData()
    {}


    public void disconnectClient()
    {
        if (pullConsumer_ != null)
        {
            pullConsumer_.disconnect_pull_consumer();
            pullConsumer_ = null;
        }
    }
}
