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

import org.jacorb.notification.TypedEventMessage;
import org.jacorb.notification.servant.TypedProxyPullSupplierImpl;
import org.jacorb.test.notification.NotificationTestCase;
import org.jacorb.test.notification.NotificationTestCaseSetup;

import org.omg.CORBA.IntHolder;
import org.omg.CORBA.StringHolder;
import org.omg.CosNotification.EventType;
import org.omg.CosNotification.Property;
import org.omg.CosNotifyComm.InvalidEventType;
import org.omg.CosNotifyComm.PullConsumerPOA;
import org.omg.CosTypedNotifyChannelAdmin.TypedProxyPullSupplier;
import org.omg.CosTypedNotifyChannelAdmin.TypedProxyPullSupplierHelper;

import EDU.oswego.cs.dl.util.concurrent.Latch;
import junit.framework.Test;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */
public class TypedProxyPullSupplierImplTest extends NotificationTestCase {

    TypedProxyPullSupplierImpl objectUnderTest_;

    TypedProxyPullSupplier supplier_;

    public void setUp() throws Exception {
        objectUnderTest_ = new TypedProxyPullSupplierImpl(PullCoffeeHelper.id());

        getChannelContext().resolveDependencies(objectUnderTest_);

        objectUnderTest_.preActivate();

        supplier_ = TypedProxyPullSupplierHelper.narrow(objectUnderTest_.activate());
    }


    public TypedProxyPullSupplierImplTest(String name, NotificationTestCaseSetup setup) {
        super(name, setup);
    }


    public void testConnect() throws Exception {
        NullPullConsumer _pullConsumer = new NullPullConsumer();

        supplier_.connect_typed_pull_consumer(_pullConsumer._this(getORB()));
    }


    public void testEmptyPull() throws Exception {
        NullPullConsumer _pullConsumer = new NullPullConsumer();

        supplier_.connect_typed_pull_consumer(_pullConsumer._this(getORB()));

        org.omg.CORBA.Object _object = supplier_.get_typed_supplier();

        String _objectAsString = _object.toString();

        PullCoffee _pullCoffee = PullCoffeeHelper.narrow(getClientORB().string_to_object(_objectAsString));

        StringHolder _name = new StringHolder();
        IntHolder _minutes = new IntHolder();

        assertFalse(_pullCoffee.try_drinking_coffee(_name, _minutes));
    }


    public void testTryPullDrinkingCoffee() throws Exception {
        TypedEventMessage _mesg = new TypedEventMessage();

        _mesg.setTypedEvent(PullCoffeeHelper.id(),
                            "drinking_coffee",
                            new Property[] {
                                new Property("name", toAny("jacorb")),
                                new Property("minutes", toAny(10))
                            });

        NullPullConsumer _pullConsumer = new NullPullConsumer();

        supplier_.connect_typed_pull_consumer(_pullConsumer._this(getORB()));

        org.omg.CORBA.Object _object = supplier_.get_typed_supplier();

        String _objectAsString = _object.toString();

        PullCoffee _pullCoffee =
            PullCoffeeHelper.narrow(getClientORB().string_to_object(_objectAsString));

        StringHolder _name = new StringHolder();
        IntHolder _minutes = new IntHolder();

        objectUnderTest_.getMessageConsumer().deliverMessage(_mesg.getHandle());

        assertTrue(_pullCoffee.try_drinking_coffee(_name, _minutes));

        assertEquals("jacorb", _name.value);
        assertEquals(10, _minutes.value);
    }


    public void testPullDrinkingCoffee() throws Exception {
        TypedEventMessage _mesg = new TypedEventMessage();

        _mesg.setTypedEvent(PullCoffeeHelper.id(),
                            "drinking_coffee",
                            new Property[] {
                                new Property("name", toAny("jacorb")),
                                new Property("minutes", toAny(10))
                            });

        NullPullConsumer _pullConsumer = new NullPullConsumer();

        supplier_.connect_typed_pull_consumer(_pullConsumer._this(getORB()));

        org.omg.CORBA.Object _object = supplier_.get_typed_supplier();

        String _objectAsString = _object.toString();

        final PullCoffee _pullCoffee =
            PullCoffeeHelper.narrow(getClientORB().string_to_object(_objectAsString));

        final StringHolder _name = new StringHolder();
        final IntHolder _minutes = new IntHolder();

        final Latch _latch = new Latch();

        new Thread() {
            public void run() {
                _pullCoffee.drinking_coffee(_name, _minutes);
                _latch.release();
            }
        }.start();

        objectUnderTest_.getMessageConsumer().deliverMessage(_mesg.getHandle());

        assertTrue(_latch.attempt(1000));

        assertEquals("jacorb", _name.value);
        assertEquals(10, _minutes.value);
    }


    public static Test suite() throws Exception {
        return NotificationTestCase.suite(TypedProxyPullSupplierImplTest.class);
    }
}

class NullPullConsumer extends PullConsumerPOA {

    public void offer_change(EventType[] eventTypeArray,
                             EventType[] eventTypeArray1) throws InvalidEventType {

    }

    public void disconnect_pull_consumer() {

    }
}
