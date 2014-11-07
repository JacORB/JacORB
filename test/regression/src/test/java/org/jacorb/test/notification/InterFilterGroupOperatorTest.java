package org.jacorb.test.notification;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2014 Gerald Brose / The JacORB Team.
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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.jacorb.test.notification.common.NotificationTestUtils;
import org.jacorb.test.notification.common.NotifyServerTestCase;
import org.junit.Before;
import org.junit.Test;
import org.omg.CORBA.Any;
import org.omg.CosNotification.EventType;
import org.omg.CosNotifyChannelAdmin.ConsumerAdmin;
import org.omg.CosNotifyChannelAdmin.EventChannel;
import org.omg.CosNotifyChannelAdmin.SupplierAdmin;
import org.omg.CosNotifyFilter.ConstraintExp;
import org.omg.CosNotifyFilter.Filter;

/**
 * Test the various InterFilterGroupOperator settings.
 * naming scheme for the testMethods:
 *
 * test[PCF][IFGOp][SAF]_[CAF][IFGOp][PSF]()
 *
 * where
 * <ul>
 * <li>PCF: ProxyConsumerFilter (True/False)
 * <li>IFGOp: InterFilterGroupOperator (OR/AND)
 * <li>SAF: SupplierAdminFilter (True/False)
 * <li>CAF: ConsumerAdminFilter (True/False)
 * <li>PSF: ProxySupplierFilter (True/False)
 * </ul>
 *
 * for example testTrueOrTrue_FalseAndTrue() means
 * ProxyConsumerFilter set to TRUE, SupplierAdminFilter set to TRUE
 * and InterFilterGroupOperator set to OR.
 * ConsumerAdminFilter set to FALSE and InterFilterGroupOperator set
 * to AND. ProxySupplierfilter set to TRUE.
 *
 * @author Alphonse Bendt
 */
public class InterFilterGroupOperatorTest extends NotifyServerTestCase
{
    Any testPerson_;
    EventChannel channel_;
    SupplierAdmin supplierAdmin_;
    ConsumerAdmin consumerAdmin_;
    Filter trueFilter_;
    Filter falseFilter_;

    @Before
    public void setUp() throws Exception
    {
        testPerson_ = new NotificationTestUtils(setup.getClientOrb()).getTestPersonAny();

        channel_ = getDefaultChannel();

        trueFilter_ = channel_.default_filter_factory().create_filter("EXTENDED_TCL");

        ConstraintExp[] _constraintExp = new ConstraintExp[1];
        EventType[] _eventType = new EventType[1];
        _eventType[0] = new EventType("*", "*");
        String _expression = "TRUE";
        _constraintExp[0] = new ConstraintExp(_eventType, _expression);
        trueFilter_.add_constraints(_constraintExp);

        falseFilter_ = channel_.default_filter_factory().create_filter("EXTENDED_TCL");
        _constraintExp = new ConstraintExp[1];
        _eventType = new EventType[1];
        _eventType[0] = new EventType("*", "*");

        _constraintExp[0] = new ConstraintExp(_eventType, "FALSE");
       falseFilter_.add_constraints(_constraintExp);

        assertTrue(trueFilter_.match(testPerson_));
        assertFalse(falseFilter_.match(testPerson_));
    }


    @Test
    public void testTrueORFalse_NoneOrNone() throws Exception
    {
        AnyPushSender _sender = new AnyPushSender(setup.getClientOrb(), testPerson_);
        AnyPushReceiver _receiver = new AnyPushReceiver(setup.getClientOrb());

        _sender.connect(channel_, true);
        _receiver.connect(channel_, true);
        _sender.addProxyFilter(trueFilter_);
        _sender.addAdminFilter(falseFilter_);

        Thread _senderThread = new Thread(_sender);
        Thread _receiverThread = new Thread(_receiver);

        _receiverThread.start();
        _senderThread.start();

        _senderThread.join();
        _receiverThread.join();

        assertTrue("Error while sending", !_sender.error_);
        assertTrue("Should have received something", _receiver.isEventHandled());
    }


    @Test
    public void testFalseORTrue_NoneOrNone() throws Exception
    {
        AnyPushSender _sender = new AnyPushSender(setup.getClientOrb(), testPerson_);
        AnyPushReceiver _receiver = new AnyPushReceiver(setup.getClientOrb());
        _sender.connect(channel_, true);
        _receiver.connect(channel_, true);

        _sender.addProxyFilter(falseFilter_);
        _sender.addAdminFilter(trueFilter_);

        Thread _senderThread = new Thread(_sender);
        Thread _receiverThread = new Thread(_receiver);

        _receiverThread.start();
        _senderThread.start();

        _senderThread.join();
        _receiverThread.join();

        assertTrue("Error while sending", !_sender.error_);
        assertTrue("Should have received something", _receiver.isEventHandled());
    }


    @Test
    public void testTrueANDFalse_NoneOrNone() throws Exception
    {
        AnyPushSender _sender = new AnyPushSender(setup.getClientOrb(), testPerson_);
        AnyPushReceiver _receiver = new AnyPushReceiver(setup.getClientOrb());
        _sender.connect(channel_, false);
        _receiver.connect(channel_, true);

        _sender.addProxyFilter(trueFilter_);
        _sender.addAdminFilter(falseFilter_);

        Thread _senderThread = new Thread(_sender);
        Thread _receiverThread = new Thread(_receiver);

        _receiverThread.start();
        _senderThread.start();

        _senderThread.join();
        _receiverThread.join();

        assertTrue("Error while sending", !_sender.error_);
        assertTrue("Should have received something", !_receiver.isEventHandled());
    }


    @Test
    public void testFalseANDTrue_NoneOrNone() throws Exception
    {
        AnyPushSender _sender = new AnyPushSender(setup.getClientOrb(), testPerson_);
        AnyPushReceiver _receiver = new AnyPushReceiver(setup.getClientOrb());
        _sender.connect(channel_, false);
        _receiver.connect(channel_, true);

        _sender.addProxyFilter(falseFilter_);
        _sender.addAdminFilter(trueFilter_);

        Thread _senderThread = new Thread(_sender);
        Thread _receiverThread = new Thread(_receiver);

        _receiverThread.start();
        _senderThread.start();

        _senderThread.join();
        _receiverThread.join();

        assertTrue("Error while sending", !_sender.error_);
        assertTrue("Should have received something", !_receiver.isEventHandled());
    }


    @Test
    public void testNoneOrNone_TrueORFalse() throws Exception
    {
        AnyPushSender _sender = new AnyPushSender(setup.getClientOrb(), testPerson_);
        AnyPushReceiver _receiver = new AnyPushReceiver(setup.getClientOrb());
        _sender.connect(channel_, true);
        _receiver.connect(channel_, true);

        _receiver.addProxyFilter(falseFilter_);
        _receiver.addAdminFilter(trueFilter_);

        Thread _senderThread = new Thread(_sender);
        Thread _receiverThread = new Thread(_receiver);

        _receiverThread.start();
        _senderThread.start();

        _senderThread.join();
        _receiverThread.join();

        assertTrue("Error while sending", !_sender.error_);
        assertTrue("Should have received something", _receiver.isEventHandled());
    }


    @Test
    public void testNoneOrNone_FalseORTrue() throws Exception
    {
        AnyPushSender _sender = new AnyPushSender(setup.getClientOrb(), testPerson_);
        AnyPushReceiver _receiver = new AnyPushReceiver(setup.getClientOrb());
        _sender.connect(channel_, true);
        _receiver.connect(channel_, true);

        _receiver.addProxyFilter(trueFilter_);
        _receiver.addAdminFilter(falseFilter_);

        Thread _senderThread = new Thread(_sender);
        Thread _receiverThread = new Thread(_receiver);

        _receiverThread.start();
        _senderThread.start();

        _senderThread.join();
        _receiverThread.join();

        assertTrue("Error while sending", !_sender.error_);
        assertTrue("Should have received something", _receiver.isEventHandled());
    }


    @Test
    public void testNoneOrNone_TrueANDFalse() throws Exception
    {
        AnyPushSender _sender = new AnyPushSender(setup.getClientOrb(), testPerson_);
        AnyPushReceiver _receiver = new AnyPushReceiver(setup.getClientOrb());

        _sender.connect(channel_, true);

        _receiver.connect(channel_, false);

        _receiver.addProxyFilter(falseFilter_);
        _receiver.addAdminFilter(trueFilter_);

        Thread _senderThread = new Thread(_sender);
        Thread _receiverThread = new Thread(_receiver);

        _receiverThread.start();
        _senderThread.start();

        _senderThread.join();
        _receiverThread.join();

        assertTrue("Error while sending", !_sender.error_);
        assertTrue("Should have received something", !_receiver.isEventHandled());
    }


    @Test
    public void testNoneOrNone_FalseANDTrue() throws Exception
    {
        AnyPushSender _sender = new AnyPushSender(setup.getClientOrb(), testPerson_);
        AnyPushReceiver _receiver = new AnyPushReceiver(setup.getClientOrb());

        _sender.connect(channel_, true);

        _receiver.connect(channel_, false);

        _receiver.addProxyFilter(trueFilter_);
        _receiver.addAdminFilter(falseFilter_);

        Thread _senderThread = new Thread(_sender);
        Thread _receiverThread = new Thread(_receiver);

        _receiverThread.start();
        _senderThread.start();

        _senderThread.join();
        _receiverThread.join();

        assertTrue("Error while sending", !_sender.error_);
        assertTrue(_receiver + " should have received something", !_receiver.isEventHandled());
    }
}
