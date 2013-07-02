package org.jacorb.test.notification;

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

import java.util.Date;
import java.util.HashSet;
import junit.framework.Test;
import org.jacorb.notification.StructuredEventMessage;
import org.jacorb.notification.engine.DefaultTaskFactory;
import org.jacorb.notification.engine.DefaultTaskProcessor;
import org.jacorb.notification.interfaces.Message;
import org.jacorb.test.notification.common.NotificationTestCase;
import org.jacorb.test.notification.common.NotificationTestCaseSetup;
import org.jacorb.util.Time;
import org.omg.CORBA.Any;
import org.omg.CosNotification.EventHeader;
import org.omg.CosNotification.EventType;
import org.omg.CosNotification.FixedEventHeader;
import org.omg.CosNotification.Property;
import org.omg.CosNotification.StopTime;
import org.omg.CosNotification.StructuredEvent;
import org.omg.TimeBase.UtcTHelper;

/**
 * @author Alphonse Bendt
 */

public class StopTimeTest extends NotificationTestCase
{
    private StructuredEvent structuredEvent_;

    public StopTimeTest(String name, NotificationTestCaseSetup setup)
    {
        super(name, setup);
    }

    public void setUpTest()
    {
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

    public void testProcessEventWithStopTime() throws Exception
    {
        processEventWithStopTime(-1000, 5000, false);
        processEventWithStopTime(1000, 5000, true);
    }

    public void processEventWithStopTime(long offset, long timeout, boolean receive)
            throws Exception
    {
        structuredEvent_.header.variable_header = new Property[1];

        final Date _time = new Date(System.currentTimeMillis() + offset);

        Any _any = getORB().create_any();

        UtcTHelper.insert(_any, Time.corbaTime(_time));

        structuredEvent_.header.variable_header[0] = new Property(StopTime.value, _any);

        StructuredEventMessage mesg = new StructuredEventMessage(getORB());
        mesg.setStructuredEvent(structuredEvent_, true, true);
        final Message _event = mesg.getHandle();

        final HashSet _received = new HashSet();

        final Object lock = new Object();

        // TODO check if MockTaskProcessor can be used here
        final DefaultTaskFactory _defaultTaskFactory = new DefaultTaskFactory(getConfiguration());
        addDisposable(_defaultTaskFactory);
        DefaultTaskProcessor _taskProcessor = new DefaultTaskProcessor(getConfiguration(), _defaultTaskFactory)
        {
            public void processMessageInternal(Message event)
            {
                synchronized (lock)
                {
                    try
                    {
                        long _recvTime = System.currentTimeMillis();
                        assertEquals("unexpected event", event, _event);
                        assertTrue("received too late", _recvTime <= _time.getTime());
                        _received.add(event);
                    } finally
                    {
                        lock.notifyAll();
                    }
                }
            }
        };

        _taskProcessor.processMessage(_event);

        long timeoutF = System.currentTimeMillis() + timeout;
        synchronized(lock)
        {
            while (!_received.contains(_event) && System.currentTimeMillis() < timeoutF)
            {
                lock.wait(timeout);
            }
        }

        if (receive)
        {
            assertTrue("should have received something", _received.contains(_event));
        }
        else
        {
            assertTrue("shouldn't", !_received.contains(_event));
        }

        _taskProcessor.dispose();
    }

    public static Test suite() throws Exception
    {
        return NotificationTestCase.suite(StopTimeTest.class);
    }
}