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

import junit.framework.Assert;
import junit.framework.Test;

import org.easymock.AbstractMatcher;
import org.easymock.MockControl;
import org.jacorb.notification.OfferManager;
import org.jacorb.notification.SubscriptionManager;
import org.jacorb.notification.engine.TaskProcessor;
import org.jacorb.notification.interfaces.Message;
import org.jacorb.notification.servant.ITypedAdmin;
import org.jacorb.notification.servant.TypedProxyPullConsumerImpl;
import org.jacorb.test.notification.NotificationTestCase;
import org.jacorb.test.notification.NotificationTestCaseSetup;
import org.omg.CORBA.IntHolder;
import org.omg.CORBA.StringHolder;
import org.omg.CosNotification.EventType;
import org.omg.CosNotification.EventTypeHelper;
import org.omg.CosNotification.Property;
import org.omg.CosNotifyChannelAdmin.ProxyType;
import org.omg.CosNotifyChannelAdmin.SupplierAdmin;
import org.omg.CosTypedNotifyChannelAdmin.TypedProxyPullConsumer;
import org.omg.CosTypedNotifyChannelAdmin.TypedProxyPullConsumerHelper;
import org.omg.CosTypedNotifyComm.TypedPullSupplier;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */
public class TypedProxyPullConsumerImplTest extends NotificationTestCase
{
    private TypedProxyPullConsumerImpl objectUnderTest_;

    private TypedProxyPullConsumer proxyPullConsumer_;

    private MockControl controlTypedPullSupplier_;

    private TypedPullSupplier mockTypedPullSupplier_;

    private MockControl controlAdmin_;

    private ITypedAdmin mockAdmin_;

    private MockControl controlSupplierAdmin_;

    private SupplierAdmin mockSupplierAdmin_;

    public void setUpTest() throws Exception
    {
        controlAdmin_ = MockControl.createNiceControl(ITypedAdmin.class);
        mockAdmin_ = (ITypedAdmin) controlAdmin_.getMock();
        mockAdmin_.getProxyID();
        controlAdmin_.setReturnValue(10);

        mockAdmin_.isIDPublic();
        controlAdmin_.setReturnValue(true);

        mockAdmin_.getContainer();
        controlAdmin_.setReturnValue(getContainer());

        mockAdmin_.getSupportedInterface();
        controlAdmin_.setDefaultReturnValue(PullCoffeeHelper.id());

        controlAdmin_.replay();

        controlSupplierAdmin_ = MockControl.createControl(SupplierAdmin.class);
        mockSupplierAdmin_ = (SupplierAdmin) controlSupplierAdmin_.getMock();

        controlSupplierAdmin_.replay();

        objectUnderTest_ = new TypedProxyPullConsumerImpl(mockAdmin_, mockSupplierAdmin_, getORB(),
                getPOA(), getConfiguration(), getTaskProcessor(), getMessageFactory(),
                new OfferManager(), new SubscriptionManager());

        objectUnderTest_.preActivate();

        proxyPullConsumer_ = TypedProxyPullConsumerHelper.narrow(objectUnderTest_.activate());

        controlTypedPullSupplier_ = MockControl.createControl(TypedPullSupplier.class);
        mockTypedPullSupplier_ = (TypedPullSupplier) controlTypedPullSupplier_.getMock();
    }

    public TypedProxyPullConsumerImplTest(String name, NotificationTestCaseSetup setup)
    {
        super(name, setup);
    }

    public void testId()
    {
        assertTrue(objectUnderTest_.isIDPublic());
        assertEquals(new Integer(10), objectUnderTest_.getID());
    }

    public void testMyType()
    {
        assertEquals(ProxyType.PULL_TYPED, proxyPullConsumer_.MyType());
    }

    public void testMyAdmin()
    {
        assertEquals(mockSupplierAdmin_, proxyPullConsumer_.MyAdmin());
    }

    public void testConnect() throws Exception
    {
        MockControl controlPullCoffeeOperations = MockControl
                .createControl(PullCoffeeOperations.class);

        PullCoffeeOperations mockPullCoffee = (PullCoffeeOperations) controlPullCoffeeOperations
                .getMock();

        PullCoffee pullCoffee = new PullCoffeePOATie(mockPullCoffee)._this(getORB());

        controlPullCoffeeOperations.replay();

        mockTypedPullSupplier_.get_typed_supplier();
        controlTypedPullSupplier_.setReturnValue(pullCoffee);

        controlTypedPullSupplier_.replay();

        proxyPullConsumer_.connect_typed_pull_supplier(mockTypedPullSupplier_);

        controlTypedPullSupplier_.verify();
        controlPullCoffeeOperations.verify();
    }

    public void testTryOperationsAreInvoked() throws Exception
    {
        final MockPullCoffee _coffee = new MockPullCoffee();

        _coffee.try_drinking_coffee_expect = 1;
        _coffee.try_cancel_coffee_expect = 1;

        mockTypedPullSupplier_.get_typed_supplier();
        controlTypedPullSupplier_.setReturnValue(_coffee._this(getORB()));

        controlTypedPullSupplier_.replay();

        proxyPullConsumer_.connect_typed_pull_supplier(mockTypedPullSupplier_);

        objectUnderTest_.runPullMessage();

        _coffee.verify();

        controlTypedPullSupplier_.verify();
    }

    public void testFormat() throws Exception
    {
        MockControl controlTaskProcessor = MockControl.createControl(TaskProcessor.class);
        TaskProcessor mockTaskProcessor = (TaskProcessor) controlTaskProcessor.getMock();

        mockTaskProcessor.processMessage(null);

        controlTaskProcessor.setMatcher(new AbstractMatcher()
        {
            protected boolean argumentMatches(Object expected, Object actual)
            {
                try
                {
                    Property[] _props = ((Message) actual).toTypedEvent();

                    assertEquals(3, _props.length);

                    assertEquals("event_type", _props[0].name);
                    EventType et = EventTypeHelper.extract(_props[0].value);
                    assertEquals(PullCoffeeHelper.id(), et.domain_name);

                    assertEquals(
                            "::org::jacorb::test::notification::typed::PullCoffee::drinking_coffee",
                            et.type_name);

                    assertEquals("jacorb", _props[1].value.extract_string());
                    assertEquals(20, _props[2].value.extract_long());
                } catch (Exception e)
                {
                    fail();
                }
                
                return true;
            }
        });

        objectUnderTest_ = new TypedProxyPullConsumerImpl(mockAdmin_, mockSupplierAdmin_, getORB(),
                getPOA(), getConfiguration(), mockTaskProcessor, getMessageFactory(),
                new OfferManager(), new SubscriptionManager());

        objectUnderTest_.preActivate();

        proxyPullConsumer_ = TypedProxyPullConsumerHelper.narrow(objectUnderTest_.activate());

        final MockPullCoffee _coffee = new MockPullCoffee()
        {
            public boolean try_drinking_coffee(StringHolder name, IntHolder minutes)
            {
                super.try_drinking_coffee(name, minutes);

                name.value = "jacorb";
                minutes.value = 20;

                return true;
            }
        };

        _coffee.try_drinking_coffee_expect = 1;
        _coffee.try_cancel_coffee_expect = 1;

        mockTypedPullSupplier_.get_typed_supplier();
        controlTypedPullSupplier_.setReturnValue(_coffee._this(getORB()));

        controlTypedPullSupplier_.replay();

        proxyPullConsumer_.connect_typed_pull_supplier(mockTypedPullSupplier_);

        objectUnderTest_.runPullMessage();

        controlTypedPullSupplier_.verify();
    }

    public static Test suite() throws Exception
    {
        return NotificationTestCase.suite(TypedProxyPullConsumerImplTest.class);
    }
}

class MockPullCoffee extends PullCoffeePOA
{
    int drinking_coffee_called;

    int drinking_coffee_expect;

    int try_drinking_coffee_called;

    int try_drinking_coffee_expect;

    int cancel_coffee_called;

    int cancel_coffee_expect;

    int try_cancel_coffee_called;

    int try_cancel_coffee_expect;

    public void drinking_coffee(StringHolder stringHolder, IntHolder intHolder)
    {
        drinking_coffee_called++;
    }

    public boolean try_drinking_coffee(StringHolder stringHolder, IntHolder intHolder)
    {
        try_drinking_coffee_called++;

        stringHolder.value = "";
        intHolder.value = 0;

        return false;
    }

    public void cancel_coffee(StringHolder stringHolder)
    {
        cancel_coffee_called++;
    }

    public boolean try_cancel_coffee(StringHolder stringHolder)
    {
        try_cancel_coffee_called++;

        stringHolder.value = "";

        return false;
    }

    public void verify()
    {
        Assert.assertEquals(cancel_coffee_expect, cancel_coffee_called);
        Assert.assertEquals(try_cancel_coffee_expect, try_cancel_coffee_called);
        Assert.assertEquals(drinking_coffee_expect, drinking_coffee_called);
        Assert.assertEquals(try_drinking_coffee_expect, try_drinking_coffee_called);
    }
}