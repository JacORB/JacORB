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
import java.util.HashSet;

import org.jacorb.notification.ApplicationContext;
import org.jacorb.notification.MessageFactory;
import org.jacorb.notification.engine.TaskProcessor;
import org.jacorb.notification.interfaces.Message;
import org.jacorb.util.Debug;
import org.jacorb.util.Time;

import org.omg.CORBA.Any;
import org.omg.CORBA.IntHolder;
import org.omg.CORBA.ORB;
import org.omg.CosNotification.EventHeader;
import org.omg.CosNotification.EventType;
import org.omg.CosNotification.FixedEventHeader;
import org.omg.CosNotification.Property;
import org.omg.CosNotification.StartTime;
import org.omg.CosNotification.StopTime;
import org.omg.CosNotification.StructuredEvent;
import org.omg.CosNotifyChannelAdmin.EventChannel;
import org.omg.TimeBase.UtcT;
import org.omg.TimeBase.UtcTHelper;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.apache.avalon.framework.logger.Logger;
import org.omg.CosNotification.StopTimeSupported;
import org.omg.CORBA.BAD_QOS;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */

public class StopTimeTest extends NotificationTestCase
{
    Logger logger_ = Debug.getNamedLogger(getClass().getName());

    MessageFactory messageFactory_;

    StructuredEvent structuredEvent_;

    EventChannel eventChannel_;


    public StopTimeTest (String name, NotificationTestCaseSetup setup)
    {
        super(name, setup);
    };

    public void setUp() throws Exception {
        eventChannel_ = getDefaultChannel();

        messageFactory_ = new MessageFactory();
        messageFactory_.init();

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

    public void tearDown() {
        messageFactory_.dispose();

        super.tearDown();
    }


    public void testA_SendEvent() throws Exception {
        logger_.info("testSendEvent");

        // StartTime +1000ms, StopTime +500ms
        sendEvent(1000, 500, false);

        // StartTime +1000ms, StopTime +2000ms
        sendEvent(1000, 2000, true);

        // StartTime now, StopTime in the Past
        sendEvent(0, -1000, false);
    }


    public void testDisableStopTimeSupported() throws Exception {
        if (true) return;

        Any falseAny = getORB().create_any();
        falseAny.insert_boolean(false);

        eventChannel_.set_qos(new Property[] {new Property(StopTimeSupported.value, falseAny)});

        sendEvent(0, 1000, false);
    }


    public void sendEvent(long startOffset, long stopOffset, boolean expect) throws Exception {
        structuredEvent_.header.variable_header = new Property[2];

        Date _time = new Date(System.currentTimeMillis() + startOffset);

        Any _any = getORB().create_any();
        UtcTHelper.insert(_any, Time.corbaTime(_time));

        structuredEvent_.header.variable_header[0] =
            new Property(StartTime.value, _any);

        _time = new Date(System.currentTimeMillis() + stopOffset);

        _any = getORB().create_any();
        UtcTHelper.insert(_any, Time.corbaTime(_time));

        structuredEvent_.header.variable_header[1] = new Property(StopTime.value, _any);

        StructuredPushSender _sender = new StructuredPushSender(this, structuredEvent_);
        StructuredPushReceiver _receiver = new StructuredPushReceiver(this);

        _sender.connect(getSetup(), eventChannel_, false);

        _receiver.connect(getSetup(), eventChannel_, false);

        new Thread(_receiver).start();
        new Thread(_sender).start();

        Thread.sleep(startOffset + 1000);

        if (expect) {
            assertTrue("Receiver should have received something", _receiver.isEventHandled());
        } else {
            assertTrue("Receiver shouldn't have received anything", !_receiver.isEventHandled());
        }

        _receiver.shutdown();
        _sender.shutdown();
    }


    public void testStructuredEventWithoutStopTimeProperty() throws Exception {
        logger_.info("testStructuredEventWithoutStopTimeProperty");

        Message _event = messageFactory_.newMessage(structuredEvent_);
        assertTrue(!_event.hasStopTime());
    }


    public void testAnyEventHasNoStopTime() throws Exception {
        Message _event = messageFactory_.newMessage(getORB().create_any());
        assertTrue(!_event.hasStopTime());
    }


    public void testStructuredEventWithStopTimeProperty() throws Exception {
        logger_.debug("testStructuredEventWithStopTimeProperty");

        structuredEvent_.header.variable_header = new Property[1];

        Date _now = new Date();

        Any _any = getORB().create_any();
        UtcT _utc = Time.corbaTime(_now);
        UtcTHelper.insert(_any, _utc);

        structuredEvent_.header.variable_header[0] = new Property(StopTime.value, _any);

        Message _event = messageFactory_.newMessage(structuredEvent_);
        assertTrue(_event.hasStopTime());
        assertEquals(_now, _event.getStopTime());
    }


    public void testProcessEventWithStopTime() throws Exception {
        logger_.debug("testProcessEventWithStopTime");

        processEventWithStopTime(-10000, 5000, false);
        processEventWithStopTime(-2000, 5000, false);
        processEventWithStopTime(1000, 5000, true);
        processEventWithStopTime(5000, 10000, true);
    }


    public void processEventWithStopTime(long offset, long timeout, boolean receive) throws Exception {
        structuredEvent_.header.variable_header = new Property[1];

        final Date _time = new Date(System.currentTimeMillis() + offset);

        Any _any = getORB().create_any();

        UtcTHelper.insert(_any, Time.corbaTime(_time));

        logger_.debug("insert StopTime: " + _time);

        logger_.debug("now: " + new Date());

        structuredEvent_.header.variable_header[0] = new Property(StopTime.value, _any);

        final Message _event = messageFactory_.newMessage(structuredEvent_);

        final HashSet _received = new HashSet();

        final Object lock = new Object();

        TaskProcessor _taskProcessor = new TaskProcessor() {
                public void processEventInternal(Message event) {
                    try {
                        logger_.debug("processEventInternal called");

                        long _recvTime = System.currentTimeMillis();
                        assertEquals("unexpected event", event, _event);
                        assertTrue("received too late", _recvTime <= _time.getTime());
                        _received.add(event);
                    } finally {
                        synchronized(lock) {
                            lock.notifyAll();
                        }
                    }
                }
            };

        _taskProcessor.processMessage(_event);

        if (!_received.contains(_event)) {
            synchronized(lock) {
                lock.wait(timeout);
            }
        }

        if (receive) {
            assertTrue("should have received something", _received.contains(_event));
        } else {
            assertTrue("shouldn't", !_received.contains(_event));
        }

        _taskProcessor.dispose();
    }


    public static Test suite() throws Exception
    {
        TestSuite _suite = new TestSuite();

        NotificationTestCaseSetup _setup =
            new NotificationTestCaseSetup(_suite);

        String[] methodNames = org.jacorb.test.common.TestUtils.getTestMethods(StopTimeTest.class);

        for (int x=0; x<methodNames.length; ++x) {
            _suite.addTest(new StopTimeTest(methodNames[x], _setup));
        }

        return _setup;
    }


    public static void main(String[] args) throws Exception
    {
        junit.textui.TestRunner.run(suite());
    }
}
