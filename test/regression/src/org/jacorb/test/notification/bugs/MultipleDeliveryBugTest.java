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

/**
 * Test to reveal bug reported by Matthew Leahy
 * (news://news.gmane.org:119/3FBE2F7D.6090503@ll.mit.edu) Under high load Messages were delivered
 * multiple times.
 * 
 * @author Alphonse Bendt
 */
public class MultipleDeliveryBugTest extends NotificationTestCase
{
    private EventChannel channel_;

    public MultipleDeliveryBugTest(String name, NotificationTestCaseSetup setup)
    {
        super(name, setup);
    }

    public void setUpTest() throws Exception
    {
        channel_ = getDefaultChannel();
    }

    public static Test suite() throws Exception
    {
        return NotificationTestCase.suite("Test to reveal a Bug", MultipleDeliveryBugTest.class);
    }

    public void testMultipleSendUnderHighLoad() throws Exception
    {
        int testSize = 200;

        StructuredEvent[] events = new StructuredEvent[testSize];

        FixedEventHeader fixedHeader = new FixedEventHeader();
        fixedHeader.event_name = "TEST";
        fixedHeader.event_type = new EventType("TESTING", "TESTING");
        EventHeader header = new EventHeader(fixedHeader, new Property[0]);

        StructuredPushReceiver _receiver = new StructuredPushReceiver(getClientORB(), testSize);
        StructuredPushSender _sender = new StructuredPushSender(getClientORB());
        
        _receiver.setTimeOut(testSize * 100);

        _sender.connect(channel_, false);
        _receiver.connect(channel_, false);

        _receiver.start();

        for (int x = 0; x < events.length; ++x)
        {
            Any a = getORB().create_any();
            a.insert_long(x);
            events[x] = new StructuredEvent(header, new Property[0], a);
        }
        
        _sender.pushEvents(events);

        _receiver.join();

        assertTrue(_receiver.isEventHandled());
    }
}
