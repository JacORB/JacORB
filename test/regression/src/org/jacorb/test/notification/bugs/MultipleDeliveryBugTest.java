package org.jacorb.test.notification.bugs;

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

import org.jacorb.test.notification.NotificationTestCase;
import org.jacorb.test.notification.NotificationTestCaseSetup;
import org.jacorb.test.notification.StructuredPushReceiver;
import org.jacorb.test.notification.StructuredPushSender;

import org.omg.CORBA.Any;
import org.omg.CosNotification.EventHeader;
import org.omg.CosNotification.EventType;
import org.omg.CosNotification.FixedEventHeader;
import org.omg.CosNotification.Property;
import org.omg.CosNotification.StructuredEvent;
import org.omg.CosNotifyChannelAdmin.EventChannel;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.jacorb.test.common.TestUtils;

/**
 * Test to reveal bug reported by Matthew Leahy
 * (news://news.gmane.org:119/3FBE2F7D.6090503@ll.mit.edu)
 * Under high load Messages were delivered multiple times.
 *
 * @author Alphonse Bendt
 * @version $Id$
 */

public class MultipleDeliveryBugTest extends NotificationTestCase
{
    EventChannel channel_;

    public MultipleDeliveryBugTest (String name, NotificationTestCaseSetup setup)
    {
        super(name, setup);
    }


    public void setUp() throws Exception
    {
        channel_ = getDefaultChannel();
    }


    public static Test suite() throws Exception
    {
        TestSuite _suite;

        _suite = new TestSuite("Test of Structured EventChannel");

        NotificationTestCaseSetup _setup =
            new NotificationTestCaseSetup(_suite);

        String[] methodNames = TestUtils.getTestMethods(MultipleDeliveryBugTest.class);

        for (int x = 0; x < methodNames.length; ++x)
        {
            _suite.addTest(new MultipleDeliveryBugTest(methodNames[x], _setup));
        }

        return _setup;
    }


    public void testMultipleSendUnderHighLoad() throws Exception
    {
        int testSize = 200;

        StructuredEvent[] events = new StructuredEvent[testSize];

        FixedEventHeader fixedHeader = new FixedEventHeader();
        fixedHeader.event_name = "TEST";
        fixedHeader.event_type = new EventType("TESTING", "TESTING");
        EventHeader header = new EventHeader(fixedHeader, new Property[0]);

        for (int x = 0; x < events.length; ++x)
        {
            Any a = getORB().create_any();
            a.insert_long(x);
            events[x] = new StructuredEvent(header, new Property[0], a);
        }

        StructuredPushSender _sender = new StructuredPushSender(this, events, 10);
        StructuredPushReceiver _receiver = new StructuredPushReceiver(this, testSize);

        _receiver.setTimeOut(testSize * 100);

        _sender.connect(channel_, false);
        _receiver.connect(channel_, false);

        _receiver.start();

        _sender.run();
        _sender.join();

        _receiver.join();

        assertTrue(_receiver.isEventHandled());
    }


    public static void main(String[] args) throws Exception
    {
        junit.textui.TestRunner.run(suite());
    }
}
