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

import org.jacorb.notification.AnyMessage;
import org.jacorb.notification.StructuredEventMessage;
import org.jacorb.notification.TypedEventMessage;
import org.jacorb.notification.servant.TypedProxyPushSupplierImpl;
import org.jacorb.test.notification.NotificationTestCase;
import org.jacorb.test.notification.NotificationTestCaseSetup;

import org.omg.CORBA.Any;
import org.omg.CORBA.Object;
import org.omg.CosEventChannelAdmin.TypeError;
import org.omg.CosEventComm.Disconnected;
import org.omg.CosNotification.EventType;
import org.omg.CosNotification.Property;
import org.omg.CosNotification.PropertySeqHelper;
import org.omg.CosNotification.StructuredEvent;
import org.omg.CosNotifyComm.InvalidEventType;
import org.omg.CosTypedNotifyChannelAdmin.TypedProxyPushSupplier;
import org.omg.CosTypedNotifyChannelAdmin.TypedProxyPushSupplierHelper;
import org.omg.CosTypedNotifyComm.TypedPushConsumer;
import org.omg.CosTypedNotifyComm.TypedPushConsumerPOA;
import org.omg.DynamicAny.DynAnyFactoryHelper;

import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;
import junit.framework.Test;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */
public class TypedProxyPushSupplierImplTest extends NotificationTestCase {

    TypedProxyPushSupplierImpl objectUnderTest_;

    TypedProxyPushSupplier supplier_;

    private static String DRINKING_COFFEE_ID =
        "::org::jacorb::test::notification::typed::Coffee::drinking_coffee";

    public TypedProxyPushSupplierImplTest(String name, NotificationTestCaseSetup setup) {
        super(name, setup);
    }


    public void setUp() throws Exception {
        objectUnderTest_ = new TypedProxyPushSupplierImpl(CoffeeHelper.id());

        getChannelContext().resolveDependencies(objectUnderTest_);

        objectUnderTest_.preActivate();

        supplier_ = TypedProxyPushSupplierHelper.narrow(objectUnderTest_.activate());
    }


    public void testConnect() throws Exception {
        MockCoffee _mockCoffee = new MockCoffee();

        final Coffee _coffee = _mockCoffee._this(getORB());

        MockTypedPushConsumer _mockConsumer = new MockTypedPushConsumer() {
                public org.omg.CORBA.Object get_typed_consumer() {
                    return _coffee;
                }
            };

        TypedPushConsumer _consumer = _mockConsumer._this(getORB());

        supplier_.connect_typed_push_consumer(_consumer);
    }


    public void testConnectWrongTypeThrowsException() throws Exception {
        final Map _map = new HashMap();

        MockTypedPushConsumer _mockConsumer = new MockTypedPushConsumer() {
                public org.omg.CORBA.Object get_typed_consumer() {
                    return (org.omg.CORBA.Object)_map.get("object");
                }
            };

        final TypedPushConsumer _consumer = _mockConsumer._this(getORB());

        _map.put("object", _consumer);

        try {
            supplier_.connect_typed_push_consumer(_consumer);
            fail();
        } catch (TypeError e) {}
    }


    public void testPushPulledEvent() throws Exception {
        // setup test data
        TypedEventMessage _event = new TypedEventMessage();

        _event.setTypedEvent(PullCoffeeHelper.id(),
                             DRINKING_COFFEE_ID,
                             new Property[] {
                                 new Property("name", toAny("alphonse")),
                                 new Property("minutes", toAny(10))
                             });


        // setup mock
        MockCoffee _mockCoffee = new MockCoffee() {
                public void drinking_coffee(String name, int minutes) {
                    super.drinking_coffee(name, minutes);

                    assertEquals("alphonse", name);
                    assertEquals(10, minutes);
                }
            };

        _mockCoffee.drinking_coffee_expect = 1;

        // setup and connect consumer
        final Coffee _coffee = _mockCoffee._this(getORB());

        MockTypedPushConsumer _mockConsumer = new MockTypedPushConsumer() {
                public org.omg.CORBA.Object get_typed_consumer() {
                    return _coffee;
                }
            };

        TypedPushConsumer _consumer = _mockConsumer._this(getORB());

        supplier_.connect_typed_push_consumer(_consumer);

        // run test
        objectUnderTest_.getMessageConsumer().deliverMessage(_event.getHandle());

        // verify results
        _mockCoffee.verify();
    }


    public void testPushTyped() throws Exception {
        // setup test data
        TypedEventMessage _event = new TypedEventMessage();

        _event.setTypedEvent(CoffeeHelper.id(),
                             DRINKING_COFFEE_ID,
                             new Property[] {
                                 new Property("name", toAny("alphonse")),
                                 new Property("minutes", toAny(10))
                             });


        // setup mock
        MockCoffee _mockCoffee = new MockCoffee() {
                public void drinking_coffee(String name, int minutes) {
                    super.drinking_coffee(name, minutes);

                    assertEquals("alphonse", name);
                    assertEquals(10, minutes);
                }
            };

        _mockCoffee.drinking_coffee_expect = 1;

        // setup and connect consumer
        final Coffee _coffee = _mockCoffee._this(getORB());

        MockTypedPushConsumer _mockConsumer = new MockTypedPushConsumer() {
                public org.omg.CORBA.Object get_typed_consumer() {
                    return _coffee;
                }
            };

        TypedPushConsumer _consumer = _mockConsumer._this(getORB());

        supplier_.connect_typed_push_consumer(_consumer);

        // run test
        objectUnderTest_.getMessageConsumer().deliverMessage(_event.getHandle());

        // verify results
        _mockCoffee.verify();
    }


    public void testPushStructured() throws Exception {

        // setup test data
        StructuredEventMessage _event = new StructuredEventMessage();

        StructuredEvent _data = getTestUtils().getEmptyStructuredEvent();

        _data.filterable_data = new Property[] {
            new Property("operation", toAny(DRINKING_COFFEE_ID)),
            new Property("name", toAny("alphonse")),
            new Property("minutes", toAny(10))
        };

        _event.setStructuredEvent(_data, false, false);

        // setup mock
        MockCoffee _mockCoffee = new MockCoffee() {
                public void drinking_coffee(String name, int minutes) {
                    super.drinking_coffee(name, minutes);

                    assertEquals("alphonse", name);
                    assertEquals(10, minutes);
                }
            };

        _mockCoffee.drinking_coffee_expect = 1;

        // setup and connect consumer
        final Coffee _coffee = _mockCoffee._this(getORB());

        MockTypedPushConsumer _mockConsumer = new MockTypedPushConsumer() {
                public org.omg.CORBA.Object get_typed_consumer() {
                    return _coffee;
                }
            };

        TypedPushConsumer _consumer = _mockConsumer._this(getORB());

        supplier_.connect_typed_push_consumer(_consumer);

        // run test
        objectUnderTest_.getMessageConsumer().deliverMessage(_event.getHandle());

        // verify results
        _mockCoffee.verify();
    }


    public void testPushAny() throws Exception {

        // setup test data
        AnyMessage _event = new AnyMessage();

        Any _any = getORB().create_any();

        PropertySeqHelper.insert(_any,
                                 new Property[] {
                                     new Property("operation", toAny(DRINKING_COFFEE_ID)),
                                     new Property("name", toAny("alphonse")),
                                     new Property("minutes", toAny(10))
                                 });

        _event.setAny(_any);

        // setup mock
        MockCoffee _mockCoffee = new MockCoffee() {
                public void drinking_coffee(String name, int minutes) {
                    super.drinking_coffee(name, minutes);

                    assertEquals("alphonse", name);
                    assertEquals(10, minutes);
                }
            };

        _mockCoffee.drinking_coffee_expect = 1;


        // setup and connect consumer
        final Coffee _coffee = _mockCoffee._this(getORB());

        MockTypedPushConsumer _mockConsumer = new MockTypedPushConsumer() {
                public org.omg.CORBA.Object get_typed_consumer() {
                    return _coffee;
                }
            };

        TypedPushConsumer _consumer = _mockConsumer._this(getORB());

        supplier_.connect_typed_push_consumer(_consumer);

        // run test
        objectUnderTest_.getMessageConsumer().deliverMessage(_event.getHandle());

        // verify results
        _mockCoffee.verify();
    }


    public static Test suite() throws Exception {
        return NotificationTestCase.suite(TypedProxyPushSupplierImplTest.class);
    }
}

class MockTypedPushConsumer extends TypedPushConsumerPOA {

    public org.omg.CORBA.Object get_typed_consumer() {
        return null;
    }

    public void push(Any any) throws Disconnected {
    }

    public void disconnect_push_consumer() {
    }

    public void offer_change(EventType[] eventTypeArray,
                             EventType[] eventTypeArray1) throws InvalidEventType {
    }
}


class MockCoffee extends CoffeePOA {

    int drinking_coffee_expect;
    int drinking_coffee_called;
    int cancel_coffee_expect;
    int cancel_coffee_called;

    public void drinking_coffee(String string, int n) {
        drinking_coffee_called++;
    }

    public void cancel_coffee(String string) {
        cancel_coffee_called++;
    }

    public void verify() {
        Assert.assertEquals(drinking_coffee_expect, drinking_coffee_called);
        Assert.assertEquals(cancel_coffee_expect, cancel_coffee_called);
    }
}
