/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2004 Gerald Brose
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

import java.util.Date;

import junit.framework.Test;

import org.jacorb.test.notification.common.NotifyServerTestCase;
import org.jacorb.test.notification.common.NotifyServerTestSetup;
import org.jacorb.util.Time;
import org.omg.CORBA.Any;
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
    
    public StopTimeIntegrationTest(String name, NotifyServerTestSetup setup)
    {
        super(name, setup);
    }
    
    public void setUpTest() throws Exception
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

        structuredEvent_.remainder_of_body = getClientORB().create_any();
    }

    public void testA_SendEvent() throws Exception
    {
        // StartTime +1000ms, StopTime +500ms
        sendEvent(1000, 500, false);

        // StartTime +1000ms, StopTime +2000ms
        sendEvent(1000, 2000, true);

        // StartTime now, StopTime in the Past
        sendEvent(0, -1000, false);
    }

    public void testDisableStopTimeSupported() throws Exception
    {
        if (true)
        {
            return;
        }

        Any falseAny = getClientORB().create_any();
        falseAny.insert_boolean(false);

        eventChannel_.set_qos(new Property[] { new Property(StopTimeSupported.value, falseAny) });

        sendEvent(0, 1000, false);
    }

    public void sendEvent(long startOffset, long stopOffset, boolean expect) throws Exception
    {
        structuredEvent_.header.variable_header = new Property[2];

        Date _time = new Date(System.currentTimeMillis() + startOffset);

        Any _any = getClientORB().create_any();
        UtcTHelper.insert(_any, Time.corbaTime(_time));

        structuredEvent_.header.variable_header[0] = new Property(StartTime.value, _any);

        _time = new Date(System.currentTimeMillis() + stopOffset);

        _any = getClientORB().create_any();
        UtcTHelper.insert(_any, Time.corbaTime(_time));

        structuredEvent_.header.variable_header[1] = new Property(StopTime.value, _any);

        StructuredPushSender _sender = new StructuredPushSender(getClientORB());
        _sender.setStructuredEvent(new StructuredEvent[] {structuredEvent_});
        
        StructuredPushReceiver _receiver = new StructuredPushReceiver(getClientORB());

        _sender.connect(eventChannel_, false);

        _receiver.connect(eventChannel_, false);

        new Thread(_receiver).start();
        new Thread(_sender).start();

        Thread.sleep(startOffset + 1000);

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

    public static Test suite() throws Exception
    {
        return NotifyServerTestCase.suite(StopTimeIntegrationTest.class);
    }
}
