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

import org.jacorb.notification.ApplicationContext;
import org.jacorb.notification.MessageFactory;
import org.jacorb.notification.servant.AbstractProxyConsumerI;
import org.jacorb.notification.engine.TaskProcessor;
import org.jacorb.notification.interfaces.Message;
import org.jacorb.util.Debug;
import org.jacorb.util.Time;

import org.omg.CORBA.Any;
import org.omg.CORBA.ORB;
import org.omg.CosNotification.EventHeader;
import org.omg.CosNotification.EventType;
import org.omg.CosNotification.FixedEventHeader;
import org.omg.CosNotification.Property;
import org.omg.CosNotification.StartTime;
import org.omg.CosNotification.StructuredEvent;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;
import org.omg.TimeBase.UtcT;
import org.omg.TimeBase.UtcTHelper;

import java.util.Date;

import EDU.oswego.cs.dl.util.concurrent.Latch;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.avalon.framework.logger.Logger;
import org.jacorb.notification.interfaces.FilterStage;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */

public class StartTimeTest extends TestCase
{
    Logger logger_ = Debug.getNamedLogger(getClass().getName());

    MessageFactory notificationEventFactory_;

    ApplicationContext applicationContext_;

    StructuredEvent structuredEvent_;

    ORB orb_;

    AbstractProxyConsumerI proxyConsumerMock_ =
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
        };

    ////////////////////////////////////////

    public StartTimeTest (String name)
    {
        super(name);
    }


    public void setUp() throws Exception {
        orb_ = ORB.init(new String[0], null);
        POA _poa = POAHelper.narrow(orb_.resolve_initial_references("RootPOA"));
        applicationContext_ = new ApplicationContext(orb_, _poa, true);

        notificationEventFactory_ = new MessageFactory();
        notificationEventFactory_.init();

        structuredEvent_ = new StructuredEvent();
        EventHeader _header = new EventHeader();
        FixedEventHeader _fixed = new FixedEventHeader();
        _fixed.event_name = "eventname";
        _fixed.event_type = new EventType("domain", "type");
        _header.fixed_header = _fixed;
        _header.variable_header = new Property[0];

        structuredEvent_.header = _header;

        structuredEvent_.filterable_data = new Property[0];

        structuredEvent_.remainder_of_body = orb_.create_any();
    }


    public void tearDown() throws Exception {
        super.tearDown();
        notificationEventFactory_.dispose();
        applicationContext_.dispose();
    }


    public void testStructuredEventWithoutStartTimeProperty() throws Exception {
        Message _event = notificationEventFactory_.newMessage(structuredEvent_);
        assertTrue(!_event.hasStartTime());
    }


    public void testAnyEventHasNoStartTime() throws Exception {
        Message _event = notificationEventFactory_.newMessage(orb_.create_any());
        assertTrue(!_event.hasStartTime());
    }


    public void testStructuredEventWithStartTimeProperty() throws Exception {
        structuredEvent_.header.variable_header = new Property[1];

        Date _now = new Date();

        Any _startTimeAny = orb_.create_any();
        UtcT _startTime = Time.corbaTime(_now);
        UtcTHelper.insert(_startTimeAny, _startTime);

        structuredEvent_.header.variable_header[0] = new Property(StartTime.value, _startTimeAny);

        Message _event = notificationEventFactory_.newMessage(structuredEvent_,
                                                              proxyConsumerMock_);

        assertTrue(_event.hasStartTime());
        assertEquals(_now, _event.getStartTime());
    }


    public void testProcessEventWithStartTime() throws Exception {
        processEventWithStartTime(0);
        processEventWithStartTime(-1000);
        processEventWithStartTime(-2000);
        processEventWithStartTime(1000);
        processEventWithStartTime(5000);
    }


    public void processEventWithStartTime(long offset) throws Exception {
        structuredEvent_.header.variable_header = new Property[1];

        final Date _startTime = new Date(System.currentTimeMillis() + offset);

        Any _startTimeAny = orb_.create_any();
        UtcTHelper.insert(_startTimeAny, Time.corbaTime(_startTime));

        structuredEvent_.header.variable_header[0] = new Property(StartTime.value, _startTimeAny);

        final Message _event = notificationEventFactory_.newMessage(structuredEvent_,
                                                                    proxyConsumerMock_);

        final Latch _latch = new Latch();

        TaskProcessor _taskProcessor = new TaskProcessor() {
                public void processEventInternal(Message event) {
                    try {
                        long _recvTime = System.currentTimeMillis();
                        assertEquals(event, _event);
                        assertTrue(_recvTime >= _startTime.getTime());
                    } finally {
                        _latch.release();
                    }
                }

            };

        _taskProcessor.processMessage(_event);

        _latch.acquire();

        _taskProcessor.dispose();
    }



    public static Test suite()
    {
        TestSuite suite = new TestSuite(StartTimeTest.class);

        return suite;
    }


    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(suite());
    }
}
