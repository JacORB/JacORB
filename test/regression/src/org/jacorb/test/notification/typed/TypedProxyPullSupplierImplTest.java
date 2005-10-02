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

import junit.framework.Test;

import org.easymock.MockControl;
import org.jacorb.notification.OfferManager;
import org.jacorb.notification.SubscriptionManager;
import org.jacorb.notification.TypedEventMessage;
import org.jacorb.notification.servant.ITypedAdmin;
import org.jacorb.notification.servant.TypedProxyPullSupplierImpl;
import org.jacorb.test.notification.NotificationTestCase;
import org.jacorb.test.notification.NotificationTestCaseSetup;
import org.omg.CORBA.IntHolder;
import org.omg.CORBA.StringHolder;
import org.omg.CosNotification.EventType;
import org.omg.CosNotification.Property;
import org.omg.CosNotifyChannelAdmin.ConsumerAdmin;
import org.omg.CosNotifyChannelAdmin.ConsumerAdminPOATie;
import org.omg.CosNotifyComm.InvalidEventType;
import org.omg.CosNotifyComm.PullConsumerPOA;
import org.omg.CosTypedNotifyChannelAdmin.TypedProxyPullSupplier;
import org.omg.CosTypedNotifyChannelAdmin.TypedProxyPullSupplierHelper;

import edu.emory.mathcs.backport.java.util.concurrent.CountDownLatch;
import edu.emory.mathcs.backport.java.util.concurrent.TimeUnit;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */
public class TypedProxyPullSupplierImplTest extends NotificationTestCase
{
    private TypedProxyPullSupplierImpl objectUnderTest_;

    private TypedProxyPullSupplier proxyPullSupplier_;

    private static String DRINKING_COFFEE_ID = "::org::jacorb::test::notification::typed::Coffee::drinking_coffee";

    private MockControl controlAdmin_;

    private ITypedAdmin mockAdmin_;

    private ConsumerAdmin mockConsumerAdmin_;

    private MockControl controlConsumerAdmin_;

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
        controlAdmin_.setReturnValue(PullCoffeeHelper.id());

        controlAdmin_.replay();

        controlConsumerAdmin_ = MockControl.createControl(ConsumerAdmin.class);
        mockConsumerAdmin_ = (ConsumerAdmin) controlConsumerAdmin_.getMock();

        objectUnderTest_ = new TypedProxyPullSupplierImpl(mockAdmin_, new ConsumerAdminPOATie(
                mockConsumerAdmin_)._this(getORB()), getORB(), getPOA(), getConfiguration(),
                getTaskProcessor(), new OfferManager(), new SubscriptionManager(),
                getDynAnyFactory(), getRepository());

        proxyPullSupplier_ = TypedProxyPullSupplierHelper.narrow(objectUnderTest_.activate());
    }

    public TypedProxyPullSupplierImplTest(String name, NotificationTestCaseSetup setup)
    {
        super(name, setup);
    }

    public void testID()
    {
        assertEquals(new Integer(10), objectUnderTest_.getID());
        assertTrue(objectUnderTest_.isIDPublic());
    }

    public void testMyAdmin()
    {
        mockConsumerAdmin_.remove_all_filters();

        controlConsumerAdmin_.replay();
        proxyPullSupplier_.MyAdmin().remove_all_filters();

        controlConsumerAdmin_.verify();
    }

    public void testConnect() throws Exception
    {
        NullPullConsumer _pullConsumer = new NullPullConsumer();

        proxyPullSupplier_.connect_typed_pull_consumer(_pullConsumer._this(getClientORB()));
    }

    public void testEmptyPull() throws Exception
    {
        NullPullConsumer _pullConsumer = new NullPullConsumer();

        proxyPullSupplier_.connect_typed_pull_consumer(_pullConsumer._this(getClientORB()));

        org.omg.CORBA.Object _object = proxyPullSupplier_.get_typed_supplier();

        String _objectAsString = _object.toString();

        PullCoffee _pullCoffee = PullCoffeeHelper.narrow(getClientORB().string_to_object(
                _objectAsString));

        StringHolder _name = new StringHolder();
        IntHolder _minutes = new IntHolder();

        assertFalse(_pullCoffee.try_drinking_coffee(_name, _minutes));
    }

    public void testTryPullDrinkingCoffee() throws Exception
    {
        TypedEventMessage _mesg = new TypedEventMessage();

        _mesg.setTypedEvent(PullCoffeeHelper.id(), DRINKING_COFFEE_ID, new Property[] {
                new Property("name", toAny("jacorb")), new Property("minutes", toAny(10)) });

        NullPullConsumer _pullConsumer = new NullPullConsumer();

        proxyPullSupplier_.connect_typed_pull_consumer(_pullConsumer._this(getClientORB()));

        org.omg.CORBA.Object _object = proxyPullSupplier_.get_typed_supplier();

        String _objectAsString = _object.toString();

        PullCoffee _pullCoffee = PullCoffeeHelper.narrow(getClientORB().string_to_object(
                _objectAsString));

        StringHolder _name = new StringHolder();
        IntHolder _minutes = new IntHolder();

        objectUnderTest_.getMessageConsumer().queueMessage(_mesg.getHandle());

        assertTrue(_pullCoffee.try_drinking_coffee(_name, _minutes));

        assertEquals("jacorb", _name.value);
        assertEquals(10, _minutes.value);
    }

    public void testPullDrinkingCoffee() throws Exception
    {
        TypedEventMessage _mesg = new TypedEventMessage();

        _mesg.setTypedEvent(PullCoffeeHelper.id(), DRINKING_COFFEE_ID, new Property[] {
                new Property("name", toAny("jacorb")), new Property("minutes", toAny(10)) });

        NullPullConsumer _pullConsumer = new NullPullConsumer();

        proxyPullSupplier_.connect_typed_pull_consumer(_pullConsumer._this(getClientORB()));

        org.omg.CORBA.Object _object = proxyPullSupplier_.get_typed_supplier();

        String _objectAsString = _object.toString();

        final PullCoffee _pullCoffee = PullCoffeeHelper.narrow(getClientORB().string_to_object(
                _objectAsString));

        final StringHolder _name = new StringHolder();
        final IntHolder _minutes = new IntHolder();

        final CountDownLatch _latch = new CountDownLatch(1);

        new Thread()
        {
            public void run()
            {
                _pullCoffee.drinking_coffee(_name, _minutes);
                _latch.countDown();
            }
        }.start();

        objectUnderTest_.getMessageConsumer().queueMessage(_mesg.getHandle());

        assertTrue(_latch.await(1000, TimeUnit.MICROSECONDS));

        assertEquals("jacorb", _name.value);
        assertEquals(10, _minutes.value);
    }

    public static Test suite() throws Exception
    {
        return NotificationTestCase.suite(TypedProxyPullSupplierImplTest.class);
    }
}

class NullPullConsumer extends PullConsumerPOA
{
    public void offer_change(EventType[] eventTypeArray, EventType[] eventTypeArray1)
            throws InvalidEventType
    {
        // no op
    }

    public void disconnect_pull_consumer()
    {
        // no op
    }
}