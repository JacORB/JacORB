package org.jacorb.notification.impl;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2012 Gerald Brose / The JacORB Team.
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
 *
 */

import org.jacorb.config.*;
import org.jacorb.notification.AnyMessage;
import org.jacorb.notification.MessageFactory;
import org.jacorb.notification.StructuredEventMessage;
import org.jacorb.notification.TypedEventMessage;
import org.jacorb.notification.interfaces.Disposable;
import org.jacorb.notification.interfaces.Message;
import org.jacorb.notification.servant.IProxyConsumer;
import org.jacorb.notification.util.AbstractObjectPool;
import org.jacorb.notification.util.AbstractPoolablePool;
import org.omg.CORBA.Any;
import org.omg.CORBA.Bounds;
import org.omg.CORBA.NVList;
import org.omg.CORBA.NamedValue;
import org.omg.CORBA.ORB;
import org.omg.CosNotification.Property;
import org.omg.CosNotification.StructuredEvent;
import org.omg.CosNotification.StructuredEventHelper;

/**
 * @author Alphonse Bendt
 */

public class DefaultMessageFactory implements Disposable, MessageFactory
{
    private final ORB orb;

    private final AbstractObjectPool typedEventMessagePool_ =
        new AbstractPoolablePool("TypedEventMessagePool")
    {
        public Object newInstance()
        {
            return new TypedEventMessage();
        }
    };

    private final AbstractObjectPool anyMessagePool_ =
        new AbstractPoolablePool("AnyMessagePool")
    {
        public Object newInstance()
        {
            return new AnyMessage();
        }
    };

    private final AbstractObjectPool structuredEventMessagePool_ =
        new AbstractPoolablePool("StructuredEventMessagePool")
    {
        public Object newInstance()
        {
            return new StructuredEventMessage(orb);
        }
    };

    public DefaultMessageFactory(ORB orb, Configuration conf)
    {
        this.orb = orb;

        anyMessagePool_.configure(conf);

        structuredEventMessagePool_.configure(conf);

        typedEventMessagePool_.configure(conf);
    }

    public void dispose()
    {
        structuredEventMessagePool_.dispose();

        anyMessagePool_.dispose();

        typedEventMessagePool_.dispose();
    }

    ////////////////////////////////////////

    // Used by the Proxies

    /**
     * create a Message wrapping an unstructured event.
     */
    public Message newMessage(Any any, IProxyConsumer consumer)
    {
        if (StructuredEventHelper.type().equals(any.type()))
        {
            // received a StructuredEvent wrapped inside an Any
            // see Spec. 2-11
            return newMessage(StructuredEventHelper.extract(any), consumer);
        }

        AnyMessage _mesg = newAnyMessage(any);

        _mesg.initReceiveTimestamp();

        _mesg.setFilterStage(consumer.getFirstStage());

        return _mesg.getHandle();
    }

    /**
     * create a Message wrapping a structured event.
     */
    public Message newMessage(StructuredEvent structuredEvent, IProxyConsumer consumer)
    {
        final String _typeName = structuredEvent.header.fixed_header.event_type.type_name;

        if (AnyMessage.TYPE_NAME.equals(_typeName))
        {
            // received an Any wrapped inside a StructuredEvent
            // see Spec. 2-11
            return newMessage(structuredEvent.remainder_of_body, consumer);
        }

        StructuredEventMessage _mesg =
            (StructuredEventMessage) structuredEventMessagePool_.lendObject();

        _mesg.initReceiveTimestamp();

        _mesg.setFilterStage(consumer.getFirstStage());

        _mesg.setStructuredEvent(structuredEvent, consumer.getStartTimeSupported(), consumer
                .getStopTimeSupported());

        return _mesg.getHandle();
    }

    /**
     * create a Message wrapping a typed event.
     */
    public Message newMessage(String interfaceName, String operationName, NVList args,
            IProxyConsumer consumer)
    {
        try
        {
            TypedEventMessage _mesg = (TypedEventMessage) typedEventMessagePool_.lendObject();

            _mesg.initReceiveTimestamp();

            Property[] _props = new Property[args.count()];

            for (int x = 0; x < _props.length; ++x)
            {
                NamedValue _nv = args.item(x);

                _props[x] = new Property(_nv.name(), _nv.value());
            }

            _mesg.setTypedEvent(interfaceName, operationName, _props);

            _mesg.setFilterStage(consumer.getFirstStage());

           return _mesg.getHandle();
        } catch (Bounds e)
        {
            // this should never happen as NamedValue bounds are checked always.
            throw new RuntimeException(e.toString());
        }
    }

    ////////////////////////////////////////

    // used by the Filters

    /**
     * create a message wrapping a typed event.
     */
    public Message newMessage(Property[] props)
    {
        TypedEventMessage _mesg = (TypedEventMessage) typedEventMessagePool_.lendObject();

        _mesg.setTypedEvent(props);

        return _mesg.getHandle();
    }

    /**
     * create a Message wrapping a unstructured event.
     */
    public Message newMessage(Any any)
    {
        if (StructuredEventHelper.type().equals(any.type()))
        {
            return newMessage(StructuredEventHelper.extract(any));
        }

        AnyMessage _mesg = newAnyMessage(any);

        return _mesg.getHandle();
    }

    private AnyMessage newAnyMessage(Any any)
    {
        AnyMessage _mesg = (AnyMessage) anyMessagePool_.lendObject();

        _mesg.setAny(any);

        return _mesg;
    }

    /**
     * create a message wrapping a structured event.
     */
    public Message newMessage(StructuredEvent structuredEvent)
    {
        String _typeName = structuredEvent.header.fixed_header.event_type.type_name;

        if (AnyMessage.TYPE_NAME.equals(_typeName))
        {
            return newMessage(structuredEvent.remainder_of_body);
        }

        StructuredEventMessage _mesg =
            (StructuredEventMessage) structuredEventMessagePool_.lendObject();

        _mesg.setStructuredEvent(structuredEvent, false, false);

        return _mesg.getHandle();
    }
}