package org.jacorb.test.notification.typed;

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

import org.jacorb.notification.interfaces.Message;
import org.jacorb.notification.servant.TypedProxyPushConsumerImpl;
import org.jacorb.test.notification.NotificationTestCase;
import org.jacorb.test.notification.NotificationTestCaseSetup;
import org.jacorb.test.notification.mocks.*;

import org.omg.CORBA.Any;
import org.omg.CORBA.ORB;
import org.omg.CORBA.Repository;
import org.omg.CORBA.RepositoryHelper;
import org.omg.CosNotifyChannelAdmin.ProxyType;
import org.omg.CosTypedNotifyChannelAdmin.TypedProxyPushConsumer;
import org.omg.CosTypedNotifyChannelAdmin.TypedProxyPushConsumerHelper;

import junit.framework.Test;
import org.omg.CosNotification.Property;
import org.omg.CosNotification.EventTypeHelper;
import org.omg.CosNotification.EventType;
import org.omg.CORBA.NO_IMPLEMENT;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */
public class TypedProxyPushConsumerImplTest extends NotificationTestCase {

    TypedProxyPushConsumerImpl objectUnderTest_;

    TypedProxyPushConsumer consumer_;

    Repository repository_;

    public void setUp() throws Exception {
        objectUnderTest_ =
            new TypedProxyPushConsumerImpl(CoffeeHelper.id());

        getChannelContext().resolveDependencies(objectUnderTest_);

        objectUnderTest_.preActivate();

        consumer_ = TypedProxyPushConsumerHelper.narrow(objectUnderTest_.activate());
    }


    public TypedProxyPushConsumerImplTest(String name, NotificationTestCaseSetup setup) {
        super(name, setup);
    }


    public void testGetTypedConsumer() throws Exception {
        Coffee coffee = CoffeeHelper.narrow( consumer_.get_typed_consumer() );
    }


    public void testInvokeDrinkingCoffee() throws Exception {

        MockTaskProcessor taskProcessor = new MockTaskProcessor() {
                public void processMessage(Message mesg) {
                    super.processMessage(mesg);

                    assertEquals(Message.TYPE_TYPED, mesg.getType());

                    try {
                        Property[] _props = mesg.toTypedEvent();

                        assertEquals("event_type", _props[0].name);
                        EventType et = EventTypeHelper.extract(_props[0].value);

                        assertEquals(CoffeeHelper.id(), et.domain_name);
                        assertEquals("drinking_coffee", et.type_name);

                        assertEquals("name", _props[1].name);
                        assertEquals("jacorb", _props[1].value.extract_string());

                        assertEquals("minutes", _props[2].name);
                        assertEquals(10, _props[2].value.extract_long());
                    } catch (Exception e) {
                        fail();
                    }
                }
            };

        taskProcessor.expectProcessMessage(1);

        objectUnderTest_.setTaskProcessor(taskProcessor);

        org.omg.CORBA.Object coff = CoffeeHelper.narrow( consumer_.get_typed_consumer() );

        String coffString = coff.toString();

        Coffee coffee = CoffeeHelper.narrow( getClientORB().string_to_object( coffString ) );

        coffee.drinking_coffee("jacorb", 10);

        taskProcessor.verify();
    }


    public void testMyType() throws Exception {
        assertEquals(ProxyType.PUSH_TYPED, consumer_.MyType());
    }


    public void testPushAny() throws Exception {
        MockPushSupplier _supplier = new MockPushSupplier();

        consumer_.connect_typed_push_supplier(_supplier._this(getORB()));

        Any any = getORB().create_any();

        any.insert_string("push");

        try {
            consumer_.push(any);
        } catch (NO_IMPLEMENT e) {}
    }


    public static Test suite() throws Exception {
        return NotificationTestCase.suite(TypedProxyPushConsumerImplTest.class);
    }
}
