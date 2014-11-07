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

package org.jacorb.test.notification;

import static org.junit.Assert.assertTrue;
import java.util.Date;
import org.jacorb.test.notification.common.NotifyServerTestCase;
import org.jacorb.util.Time;
import org.junit.Before;
import org.junit.Test;
import org.omg.CORBA.Any;
import org.omg.CosNotification.EventHeader;
import org.omg.CosNotification.EventType;
import org.omg.CosNotification.FixedEventHeader;
import org.omg.CosNotification.Property;
import org.omg.CosNotification.StartTime;
import org.omg.CosNotification.StructuredEvent;
import org.omg.CosNotification.Timeout;
import org.omg.CosNotifyChannelAdmin.EventChannel;
import org.omg.TimeBase.TimeTHelper;
import org.omg.TimeBase.UtcTHelper;

public class TimeoutIntegrationTest extends NotifyServerTestCase
{
    EventChannel eventChannel_;

    StructuredEvent structuredEvent_;

    @Before
    public void setUp() throws Exception
    {
        eventChannel_ = getDefaultChannel();

        structuredEvent_ = new StructuredEvent();
        EventHeader _header = new EventHeader();
        FixedEventHeader _fixed = new FixedEventHeader();
        _fixed.event_name = "eventname";
        _fixed.event_type = new EventType("domain", "type");
        _header.fixed_header = _fixed;
        _header.variable_header = new Property[0];

        structuredEvent_.header = _header;

        structuredEvent_.filterable_data = new Property[0];

        structuredEvent_.remainder_of_body = setup.getClientOrb().create_any();
    }

    @Test
    public void testSendEventWithTimeout() throws Exception
    {
        sendEvent(0, 10000000, true);

        sendEvent(2000, 5000000, false);

        sendEvent(1000, 20000000, true);
    }

    private void sendEvent(long startOffset, long timeout, boolean expect) throws Exception
    {
        structuredEvent_.header.variable_header = new Property[2];

        Date _time = new Date(System.currentTimeMillis() + startOffset);

        Any _startTimeAny = setup.getClientOrb().create_any();

        UtcTHelper.insert(_startTimeAny, Time.corbaTime(_time));

        structuredEvent_.header.variable_header[0] = new Property(StartTime.value, _startTimeAny);

        Any _timeoutAny = setup.getClientOrb().create_any();
        TimeTHelper.insert(_timeoutAny, timeout);

        structuredEvent_.header.variable_header[1] = new Property(Timeout.value, _timeoutAny);

        StructuredPushSender _sender = new StructuredPushSender(setup.getClientOrb());

        _sender.setStructuredEvent(structuredEvent_);

        StructuredPushReceiver _receiver = new StructuredPushReceiver(setup.getClientOrb());

        _sender.connect(eventChannel_, false);
        _receiver.connect(eventChannel_, false);

        new Thread(_receiver).start();
        new Thread(_sender).start();

        Thread.sleep(startOffset + 2000);

        if (expect)
        {
            assertTrue("Receiver should have received something", _receiver.isEventHandled());
        }
        else
        {
            assertTrue("Receiver shouldn't have received anything", !_receiver.isEventHandled());
        }

        _receiver.shutdown();
        _sender.shutdown();
    }
}
