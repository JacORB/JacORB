package org.jacorb.test.notification;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2003 Gerald Brose
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

import java.util.Date;

import org.jacorb.notification.MessageFactory;
import org.jacorb.notification.interfaces.FilterStage;
import org.jacorb.notification.interfaces.Message;
import org.jacorb.notification.servant.AbstractProxyConsumerI;
import org.jacorb.util.Time;

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

import junit.framework.Test;

/**
 * @author Alphonse Bendt
 */

public class TimeoutTest extends NotificationTestCase
{
    MessageFactory messageFactory_;
    StructuredEvent structuredEvent_;
    EventChannel eventChannel_;

    public TimeoutTest (String name, NotificationTestCaseSetup setup)
    {
        super(name, setup);
    }


    public void setUp() throws Exception
    {
        eventChannel_ = getDefaultChannel();

        messageFactory_ = new MessageFactory();
        messageFactory_.configure(getConfiguration());

        structuredEvent_ = new StructuredEvent();
        EventHeader _header = new EventHeader();
        FixedEventHeader _fixed = new FixedEventHeader();
        _fixed.event_name = "eventname";
        _fixed.event_type = new EventType("domain", "type");
        _header.fixed_header = _fixed;
        _header.variable_header = new Property[0];

        structuredEvent_.header = _header;

        structuredEvent_.filterable_data = new Property[0];

        structuredEvent_.remainder_of_body = getORB().create_any();
    }

    public void tearDown() throws Exception
    {
        super.tearDown();

        messageFactory_.dispose();
    }


    public void testSendEventWithTimeout() throws Exception
    {
        sendEvent(0, 1000, true);

        sendEvent(2000, 500, false);

        sendEvent(1000, 2000, true);
    }


    public void sendEvent(long startOffset, long timeout, boolean expect) throws Exception
    {
        structuredEvent_.header.variable_header = new Property[2];

        Date _time = new Date(System.currentTimeMillis() + startOffset);

        Any _startTimeAny = getORB().create_any();

        UtcTHelper.insert(_startTimeAny, Time.corbaTime(_time));

        structuredEvent_.header.variable_header[0] =
            new Property(StartTime.value, _startTimeAny);

        Any _timeoutAny = getORB().create_any();
        TimeTHelper.insert(_timeoutAny, timeout);

        structuredEvent_.header.variable_header[1] =
            new Property(Timeout.value, _timeoutAny);

        StructuredPushSender _sender =
            new StructuredPushSender(this, structuredEvent_);

        StructuredPushReceiver _receiver =
            new StructuredPushReceiver(this);

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


    public void testStructuredEventWithoutTimeoutProperty() throws Exception
    {
        Message _event = messageFactory_.newMessage(structuredEvent_);
        assertTrue(!_event.hasTimeout());
    }


    public void testAnyEventHasNoStopTime() throws Exception
    {
        Message _event = messageFactory_.newMessage(getORB().create_any());

        assertTrue(!_event.hasTimeout());
    }


    public void testStructuredEventWithTimeoutProperty() throws Exception
    {
        structuredEvent_.header.variable_header = new Property[1];

        long _timeout = 1234L;

        Any _any = getORB().create_any();

        TimeTHelper.insert(_any, _timeout);

        structuredEvent_.header.variable_header[0] = new Property(Timeout.value, _any);

        Message _event = messageFactory_.newMessage(structuredEvent_,
                                                    new AbstractProxyConsumerI() {
                                                        public boolean isStartTimeSupported() {
                                                            return true;
                                                        }

                                                        public boolean isTimeOutSupported() {
                                                            return true;
                                                        }

                                                        public FilterStage getFirstStage() {
                                                            return null;
                                                        }
                                                        });
        assertTrue(_event.hasTimeout());
        assertEquals(_timeout, _event.getTimeout());
    }


    public static Test suite() throws Exception
    {
        return NotificationTestCase.suite(TimeoutTest.class);
    }
}
