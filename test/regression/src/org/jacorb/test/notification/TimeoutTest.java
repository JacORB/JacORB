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

import junit.framework.Test;

import org.jacorb.notification.impl.DefaultMessageFactory;
import org.jacorb.notification.interfaces.FilterStage;
import org.jacorb.notification.interfaces.Message;
import org.jacorb.notification.servant.AbstractProxyConsumerI;
import org.omg.CORBA.Any;
import org.omg.CosNotification.EventHeader;
import org.omg.CosNotification.EventType;
import org.omg.CosNotification.FixedEventHeader;
import org.omg.CosNotification.Property;
import org.omg.CosNotification.StructuredEvent;
import org.omg.CosNotification.Timeout;
import org.omg.TimeBase.TimeTHelper;

/**
 * @author Alphonse Bendt
 */

public class TimeoutTest extends NotificationTestCase
{
    DefaultMessageFactory messageFactory_;
    StructuredEvent structuredEvent_;

    public TimeoutTest (String name, NotificationTestCaseSetup setup)
    {
        super(name, setup);
    }


    public void setUpTest() throws Exception
    {
        messageFactory_ = new DefaultMessageFactory(getConfiguration());

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

    public void tearDownTest() throws Exception
    {
        messageFactory_.dispose();
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

        long _timeout = 1000000;

        Any _any = getORB().create_any();

        TimeTHelper.insert(_any, _timeout);

        structuredEvent_.header.variable_header[0] = new Property(Timeout.value, _any);

        Message _event = messageFactory_.newMessage(structuredEvent_,
                                                    new AbstractProxyConsumerI() {
                                                        public boolean getStartTimeSupported() {
                                                            return true;
                                                        }

                                                        public boolean getTimeOutSupported() {
                                                            return true;
                                                        }

                                                        public FilterStage getFirstStage() {
                                                            return null;
                                                        }
                                                        });
        assertTrue(_event.hasTimeout());
        assertEquals(100, _event.getTimeout());
    }

    public static Test suite() throws Exception
    {
        return NotificationTestCase.suite(TimeoutTest.class);
    }
}
