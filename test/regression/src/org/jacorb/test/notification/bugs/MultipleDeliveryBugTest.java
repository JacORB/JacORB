package org.jacorb.test.notification.bugs;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2012 Gerald Brose / The JacORB Team.
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
import org.jacorb.test.notification.StructuredPushReceiver;
import org.jacorb.test.notification.StructuredPushSender;
import org.jacorb.test.notification.common.NotifyServerTestCase;
import org.jacorb.test.notification.common.NotifyServerTestSetup;
import org.omg.CORBA.Any;
import org.omg.CosNotification.EventHeader;
import org.omg.CosNotification.EventType;
import org.omg.CosNotification.FixedEventHeader;
import org.omg.CosNotification.Property;
import org.omg.CosNotification.StructuredEvent;
import org.omg.CosNotifyChannelAdmin.EventChannel;

/**
 * Test to reveal bug reported by Matthew Leahy
 * (news://news.gmane.org:119/3FBE2F7D.6090503@ll.mit.edu) Under high load Messages were delivered
 * multiple times.
 *
 * @author Alphonse Bendt
 */
public class MultipleDeliveryBugTest extends NotifyServerTestCase
{
    private EventChannel objectUnderTest_;

    public MultipleDeliveryBugTest(String name, NotifyServerTestSetup setup)
    {
        super(name, setup);
    }

    public void setUpTest() throws Exception
    {
        objectUnderTest_ = getDefaultChannel();
    }

    public void testMultipleSendUnderHighLoad() throws Exception
    {
        int testSize = 100;

        StructuredEvent[] events = new StructuredEvent[testSize];

        FixedEventHeader fixedHeader = new FixedEventHeader();
        fixedHeader.event_name = "TEST";
        fixedHeader.event_type = new EventType("TESTING", "TESTING");
        EventHeader header = new EventHeader(fixedHeader, new Property[0]);

        StructuredPushReceiver _receiver = new StructuredPushReceiver(getClientORB(), testSize);
        StructuredPushSender _sender = new StructuredPushSender(getClientORB());

        _receiver.setTimeOut(testSize * 100);

        _sender.connect(objectUnderTest_, false);
        _receiver.connect(objectUnderTest_, false);

        _receiver.start();

        for (int x = 0; x < events.length; ++x)
        {
            Any any = getClientORB().create_any();
            any.insert_long(x);
            events[x] = new StructuredEvent(header, new Property[0], any);
        }

        _sender.pushEvents(events);

        _receiver.join();

        assertTrue(_receiver.toString(), _receiver.isEventHandled());
    }

    public static Test suite() throws Exception
    {
        return NotifyServerTestCase.suite(MultipleDeliveryBugTest.class);
    }
}
