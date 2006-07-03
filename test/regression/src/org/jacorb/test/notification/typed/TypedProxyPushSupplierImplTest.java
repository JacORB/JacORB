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

import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;
import junit.framework.Test;

import org.easymock.MockControl;
import org.jacorb.notification.AnyMessage;
import org.jacorb.notification.OfferManager;
import org.jacorb.notification.StructuredEventMessage;
import org.jacorb.notification.SubscriptionManager;
import org.jacorb.notification.TypedEventMessage;
import org.jacorb.notification.engine.DirectExecutorPushTaskExecutorFactory;
import org.jacorb.notification.servant.ITypedAdmin;
import org.jacorb.notification.servant.TypedProxyPushSupplierImpl;
import org.jacorb.test.notification.common.NotificationTestCase;
import org.jacorb.test.notification.common.NotificationTestCaseSetup;
import org.omg.CORBA.Any;
import org.omg.CosEventChannelAdmin.TypeError;
import org.omg.CosEventComm.Disconnected;
import org.omg.CosNotification.EventType;
import org.omg.CosNotification.Property;
import org.omg.CosNotification.PropertySeqHelper;
import org.omg.CosNotification.StructuredEvent;
import org.omg.CosNotifyChannelAdmin.ConsumerAdmin;
import org.omg.CosNotifyComm.InvalidEventType;
import org.omg.CosTypedNotifyChannelAdmin.TypedProxyPushSupplier;
import org.omg.CosTypedNotifyChannelAdmin.TypedProxyPushSupplierHelper;
import org.omg.CosTypedNotifyComm.TypedPushConsumer;
import org.omg.CosTypedNotifyComm.TypedPushConsumerPOA;

import edu.emory.mathcs.backport.java.util.concurrent.CountDownLatch;
import edu.emory.mathcs.backport.java.util.concurrent.TimeUnit;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */
public class TypedProxyPushSupplierImplTest extends NotificationTestCase
{
    private TypedProxyPushSupplierImpl objectUnderTest_;

    private TypedProxyPushSupplier proxyPushSupplier_;

    private static final String DRINKING_COFFEE_ID = "::org::jacorb::test::notification::typed::Coffee::drinking_coffee";

    private MockControl controlAdmin_;

    private ITypedAdmin mockAdmin_;

    private MockControl controlConsumerAdmin_;

    private ConsumerAdmin mockConsumerAdmin_;

    public TypedProxyPushSupplierImplTest(String name, NotificationTestCaseSetup setup)
    {
        super(name, setup);
    }

    public void setUpTest() throws Exception
    {
        controlAdmin_ = MockControl.createNiceControl(ITypedAdmin.class);
        mockAdmin_ = (ITypedAdmin) controlAdmin_.getMock();
        mockAdmin_.getProxyID();
        controlAdmin_.setReturnValue(10);

        mockAdmin_.isIDPublic();
        controlAdmin_.setReturnValue(true);

        mockAdmin_.getContainer();
        controlAdmin_.setReturnValue(getPicoContainer());

        mockAdmin_.getSupportedInterface();
        controlAdmin_.setReturnValue(CoffeeHelper.id());

        controlAdmin_.replay();

        controlConsumerAdmin_ = MockControl.createControl(ConsumerAdmin.class);
        mockConsumerAdmin_ = (ConsumerAdmin) controlConsumerAdmin_.getMock();

        objectUnderTest_ = new TypedProxyPushSupplierImpl(mockAdmin_, mockConsumerAdmin_, getORB(),
                getPOA(), getConfiguration(), getTaskProcessor(), new DirectExecutorPushTaskExecutorFactory(), new OfferManager(), new SubscriptionManager());

        proxyPushSupplier_ = TypedProxyPushSupplierHelper.narrow(objectUnderTest_.activate());
    }

    public void testID()
    {
        assertEquals(new Integer(10), objectUnderTest_.getID());
        assertTrue(objectUnderTest_.isIDPublic());
    }


    public void testConnect() throws Exception
    {
        MockCoffee _mockCoffee = new MockCoffee();

        final Coffee _coffee = _mockCoffee._this(getClientORB());

        MockTypedPushConsumer _mockConsumer = new MockTypedPushConsumer()
        {
            public org.omg.CORBA.Object get_typed_consumer()
            {
                return _coffee;
            }
        };

        TypedPushConsumer _consumer = _mockConsumer._this(getClientORB());

        proxyPushSupplier_.connect_typed_push_consumer(_consumer);
    }

    public void testConnectWrongTypeThrowsException() throws Exception
    {
        final Map _map = new HashMap();

        MockTypedPushConsumer _mockConsumer = new MockTypedPushConsumer()
        {
            public org.omg.CORBA.Object get_typed_consumer()
            {
                return (org.omg.CORBA.Object) _map.get("object");
            }
        };

        final TypedPushConsumer _consumer = _mockConsumer._this(getClientORB());

        _map.put("object", _consumer);

        try
        {
            proxyPushSupplier_.connect_typed_push_consumer(_consumer);
            fail();
        } catch (TypeError e)
        {
            // expected
        }
    }

    public void testPushPulledEvent() throws Exception
    {
        // setup test data
        TypedEventMessage _event = new TypedEventMessage();

        _event.setTypedEvent(PullCoffeeHelper.id(), DRINKING_COFFEE_ID, new Property[] {
                new Property("name", toAny("alphonse")), new Property("minutes", toAny(10)) });

        // setup mock
        MockCoffee _mockCoffee = new MockCoffee()
        {
            public void drinking_coffee(String name, int minutes)
            {
                super.drinking_coffee(name, minutes);

                assertEquals("alphonse", name);
                assertEquals(10, minutes);
            }
        };

        _mockCoffee.drinking_coffee_expect = 1;

        // setup and connect consumer
        final Coffee _coffee = _mockCoffee._this(getClientORB());

        MockTypedPushConsumer _mockConsumer = new MockTypedPushConsumer()
        {
            public org.omg.CORBA.Object get_typed_consumer()
            {
                return _coffee;
            }
        };

        TypedPushConsumer _consumer = _mockConsumer._this(getClientORB());

        proxyPushSupplier_.connect_typed_push_consumer(_consumer);

        // run test
        objectUnderTest_.getMessageConsumer().queueMessage(_event.getHandle());

        // verify results
        _mockCoffee.verify();
    }

    public void testPushTyped() throws Exception
    {
        // setup test data
        TypedEventMessage _event = new TypedEventMessage();

        _event.setTypedEvent(CoffeeHelper.id(), DRINKING_COFFEE_ID, new Property[] {
                new Property("name", toAny("alphonse")), new Property("minutes", toAny(10)) });

        // setup mock
        MockCoffee _mockCoffee = new MockCoffee()
        {
            public void drinking_coffee(String name, int minutes)
            {
                super.drinking_coffee(name, minutes);

                assertEquals("alphonse", name);
                assertEquals(10, minutes);
            }
        };

        _mockCoffee.drinking_coffee_expect = 1;

        // setup and connect consumer
        final Coffee _coffee = _mockCoffee._this(getClientORB());

        MockTypedPushConsumer _mockConsumer = new MockTypedPushConsumer()
        {
            public org.omg.CORBA.Object get_typed_consumer()
            {
                return _coffee;
            }
        };

        TypedPushConsumer _consumer = _mockConsumer._this(getClientORB());

        proxyPushSupplier_.connect_typed_push_consumer(_consumer);

        // run test
        objectUnderTest_.getMessageConsumer().queueMessage(_event.getHandle());

        // verify results
        _mockCoffee.verify();
    }

    public void testPushStructured() throws Exception
    {
        // setup test data
        StructuredEventMessage _event = new StructuredEventMessage(getORB());

        StructuredEvent _data = getTestUtils().getEmptyStructuredEvent();

        _data.filterable_data = new Property[] {
                new Property("operation", toAny(DRINKING_COFFEE_ID)),
                new Property("name", toAny("alphonse")), new Property("minutes", toAny(10)) };

        _event.setStructuredEvent(_data, false, false);

        // setup mock
        MockCoffee _mockCoffee = new MockCoffee()
        {
            public void drinking_coffee(String name, int minutes)
            {
                super.drinking_coffee(name, minutes);

                assertEquals("alphonse", name);
                assertEquals(10, minutes);
            }
        };

        _mockCoffee.drinking_coffee_expect = 1;

        // setup and connect consumer
        final Coffee _coffee = _mockCoffee._this(getClientORB());

        MockTypedPushConsumer _mockConsumer = new MockTypedPushConsumer()
        {
            public org.omg.CORBA.Object get_typed_consumer()
            {
                return _coffee;
            }
        };

        TypedPushConsumer _consumer = _mockConsumer._this(getClientORB());

        proxyPushSupplier_.connect_typed_push_consumer(_consumer);

        // run test
        objectUnderTest_.getMessageConsumer().queueMessage(_event.getHandle());

        // verify results
        _mockCoffee.verify();
    }

    public void testPushAny() throws Exception
    {
        // setup test data
        AnyMessage _event = new AnyMessage();

        Any _any = getORB().create_any();

        PropertySeqHelper.insert(_any, new Property[] {
                new Property("operation", toAny(DRINKING_COFFEE_ID)),
                new Property("name", toAny("alphonse")), new Property("minutes", toAny(10)) });

        _event.setAny(_any);

        final CountDownLatch _hasReceived = new CountDownLatch(1);

        // setup mock
        MockCoffee _mockCoffee = new MockCoffee()
        {
            public void drinking_coffee(String name, int minutes)
            {
                super.drinking_coffee(name, minutes);

                assertEquals("alphonse", name);
                assertEquals(10, minutes);

                _hasReceived.countDown();
            }
        };

        _mockCoffee.drinking_coffee_expect = 1;

        // setup and connect consumer
        final Coffee _coffee = _mockCoffee._this(getClientORB());

        MockTypedPushConsumer _mockConsumer = new MockTypedPushConsumer()
        {
            public org.omg.CORBA.Object get_typed_consumer()
            {
                return _coffee;
            }
        };

        TypedPushConsumer _consumer = _mockConsumer._this(getClientORB());

        proxyPushSupplier_.connect_typed_push_consumer(_consumer);

        // run test
        objectUnderTest_.getMessageConsumer().queueMessage(_event.getHandle());

        assertTrue(_hasReceived.await(5000, TimeUnit.MILLISECONDS));

        // verify results
        _mockCoffee.verify();
    }

    public static Test suite() throws Exception
    {
        return NotificationTestCase.suite(TypedProxyPushSupplierImplTest.class);
    }
}

class MockTypedPushConsumer extends TypedPushConsumerPOA
{
    public org.omg.CORBA.Object get_typed_consumer()
    {
        return null;
    }

    public void push(Any any) throws Disconnected
    {
        // ignored
    }

    public void disconnect_push_consumer()
    {
        // ingored
    }

    public void offer_change(EventType[] eventTypeArray, EventType[] eventTypeArray1)
            throws InvalidEventType
    {

        // ignored
    }
}

class MockCoffee extends CoffeePOA
{
    int drinking_coffee_expect;

    int drinking_coffee_called;

    int cancel_coffee_expect;

    int cancel_coffee_called;

    public void drinking_coffee(String string, int n)
    {
        drinking_coffee_called++;
    }

    public void cancel_coffee(String string)
    {
        cancel_coffee_called++;
    }

    public void verify()
    {
        Assert.assertEquals(drinking_coffee_expect, drinking_coffee_called);
        Assert.assertEquals(cancel_coffee_expect, cancel_coffee_called);
    }
}