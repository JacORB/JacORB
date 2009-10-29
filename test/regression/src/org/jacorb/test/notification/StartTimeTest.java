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

import junit.framework.Test;

import org.jacorb.notification.engine.DefaultTaskFactory;
import org.jacorb.notification.engine.DefaultTaskProcessor;
import org.jacorb.notification.impl.DefaultMessageFactory;
import org.jacorb.notification.interfaces.FilterStage;
import org.jacorb.notification.interfaces.Message;
import org.jacorb.notification.servant.IProxyConsumer;
import org.jacorb.test.notification.common.NotificationTestCase;
import org.jacorb.test.notification.common.NotificationTestCaseSetup;
import org.jacorb.util.Time;
import org.omg.CORBA.Any;
import org.omg.CosNotification.EventHeader;
import org.omg.CosNotification.EventType;
import org.omg.CosNotification.FixedEventHeader;
import org.omg.CosNotification.Property;
import org.omg.CosNotification.StartTime;
import org.omg.CosNotification.StructuredEvent;
import org.omg.TimeBase.UtcT;
import org.omg.TimeBase.UtcTHelper;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * @author Alphonse Bendt
 */

public class StartTimeTest extends NotificationTestCase
{
    private DefaultMessageFactory messageFactory_;

    private StructuredEvent structuredEvent_;

    private IProxyConsumer proxyConsumerMock_ = new IProxyConsumer()
    {
        public boolean getStartTimeSupported()
        {
            return true;
        }

        public boolean getStopTimeSupported()
        {
            return true;
        }

        public FilterStage getFirstStage()
        {
            return null;
        }
    };

    ////////////////////////////////////////

    public StartTimeTest(String name, NotificationTestCaseSetup setup)
    {
        super(name, setup);
    }

    public void setUpTest() throws Exception
    {
        messageFactory_ = new DefaultMessageFactory(getORB(), getConfiguration());
        addDisposable(messageFactory_);

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

    public void testStructuredEventWithoutStartTimeProperty() throws Exception
    {
        Message _event = messageFactory_.newMessage(structuredEvent_);

        assertTrue(!_event.hasStartTime());
    }

    public void testAnyEventHasNoStartTime() throws Exception
    {
        Message _event = messageFactory_.newMessage(getORB().create_any());

        assertTrue(!_event.hasStartTime());
    }

    public void testStructuredEventWithStartTimeProperty() throws Exception
    {
        structuredEvent_.header.variable_header = new Property[1];

        Date _now = new Date();

        Any _startTimeAny = getORB().create_any();
        UtcT _startTime = Time.corbaTime(_now);
        UtcTHelper.insert(_startTimeAny, _startTime);

        structuredEvent_.header.variable_header[0] = new Property(StartTime.value, _startTimeAny);

        Message _event = messageFactory_.newMessage(structuredEvent_, proxyConsumerMock_);

        assertTrue(_event.hasStartTime());
        assertEquals(_now.getTime(), _event.getStartTime());
    }

    public void testProcessEventWithStartTime() throws Exception
    {
        processEventWithStartTime(0);
        processEventWithStartTime(-1000);
        processEventWithStartTime(-2000);
        processEventWithStartTime(1000);
        processEventWithStartTime(5000);
    }

    public void processEventWithStartTime(long offset) throws Exception
    {
        final AtomicBoolean failed = new AtomicBoolean(true);

        structuredEvent_.header.variable_header = new Property[1];

        final Date _startTime = new Date(System.currentTimeMillis() + offset);

        Any _startTimeAny = getORB().create_any();
        UtcTHelper.insert(_startTimeAny, Time.corbaTime(_startTime));

        structuredEvent_.header.variable_header[0] = new Property(StartTime.value, _startTimeAny);

        final Message _event = messageFactory_.newMessage(structuredEvent_, proxyConsumerMock_);

        final CountDownLatch _latch = new CountDownLatch(1);

        // TODO check if MockTaskProcessor can be used here
        final DefaultTaskFactory _defaultTaskFactory = new DefaultTaskFactory(getConfiguration());
        addDisposable(_defaultTaskFactory);
        DefaultTaskProcessor _taskProcessor = new DefaultTaskProcessor(getConfiguration(), _defaultTaskFactory)
        {
            public void processMessageInternal(Message event)
            {
                try
                {
                    long _recvTime = System.currentTimeMillis();
                    assertEquals(event, _event);
                    assertTrue(_recvTime >= _startTime.getTime());

                    failed.set(false);
                } finally
                {
                    _latch.countDown();
                }
            }
        };

        _taskProcessor.processMessage(_event);

        _latch.await();

        assertFalse(failed.get());

        _taskProcessor.dispose();
    }

    public static Test suite() throws Exception
    {
        return NotificationTestCase.suite(StartTimeTest.class);
    }
}