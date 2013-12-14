/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2012 Gerald Brose / The JacORB Team.
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
import org.omg.CORBA.IntHolder;
import org.omg.CosNotification.EventHeader;
import org.omg.CosNotification.EventType;
import org.omg.CosNotification.FixedEventHeader;
import org.omg.CosNotification.Property;
import org.omg.CosNotification.StartTime;
import org.omg.CosNotification.StopTime;
import org.omg.CosNotification.StopTimeSupported;
import org.omg.CosNotification.StructuredEvent;
import org.omg.CosNotifyChannelAdmin.EventChannel;
import org.omg.TimeBase.UtcTHelper;

public class StopTimeIntegrationTest extends NotifyServerTestCase
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
    public void testEventWithStartTimeAfterStopTimeIsNotDelivered() throws Exception
    {
        // StartTime +1000ms, StopTime +500ms
        sendEvent(eventChannel_, 1000, 500, false);
    }

    @Test
    public void testEventWithStopTimeAfterStartTimeIsDelivered() throws Exception
    {
        // StartTime +1000ms, StopTime +2000ms
        sendEvent(eventChannel_, 1000, 2000, true);
    }

    @Test
    public void testEventWithStopTimeInThePastIsNotDelivered() throws Exception
    {
        // StartTime now, StopTime in the Past
        sendEvent(eventChannel_, 0, -1000, false);
    }

    @Test
    public void testDisable_StopTimeSupported() throws Exception
    {
        Any falseAny = setup.getClientOrb().create_any();
        falseAny.insert_boolean(false);

        Property[] props = new Property[] { new Property(StopTimeSupported.value, falseAny) };
        EventChannel channel = getEventChannelFactory().create_channel(props, new Property[0], new IntHolder());

        sendEvent(channel, 1000, 500, true);
    }

    private void sendEvent(EventChannel channel, long startDelay, long stopOffset, boolean expect) throws Exception
    {
        structuredEvent_.header.variable_header = new Property[2];

        Date _time = new Date(System.currentTimeMillis() + startDelay);

        Any _any = setup.getClientOrb().create_any();
        UtcTHelper.insert(_any, Time.corbaTime(_time));

        structuredEvent_.header.variable_header[0] = new Property(StartTime.value, _any);

        _time = new Date(System.currentTimeMillis() + stopOffset);

        _any = setup.getClientOrb().create_any();
        UtcTHelper.insert(_any, Time.corbaTime(_time));

        structuredEvent_.header.variable_header[1] = new Property(StopTime.value, _any);

        StructuredPushSender _sender = new StructuredPushSender(setup.getClientOrb());
        _sender.setStructuredEvent(new StructuredEvent[] {structuredEvent_});

        StructuredPushReceiver _receiver = new StructuredPushReceiver(setup.getClientOrb());

        _sender.connect(channel, false);

        _receiver.connect(channel, false);

        new Thread(_receiver).start();
        new Thread(_sender).start();

        Thread.sleep(startDelay + 1000);

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
