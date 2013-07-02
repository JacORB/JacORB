package org.jacorb.test.notification;

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

import java.util.Date;
import junit.framework.Test;
import org.jacorb.notification.NoTranslationException;
import org.jacorb.notification.StructuredEventMessage;
import org.jacorb.test.notification.common.NotificationTestCase;
import org.jacorb.test.notification.common.NotificationTestCaseSetup;
import org.jacorb.util.Time;
import org.omg.CORBA.Any;
import org.omg.CosNotification.Property;
import org.omg.CosNotification.StopTime;
import org.omg.CosNotification.StructuredEvent;
import org.omg.TimeBase.UtcTHelper;

/**
 * @author Alphonse Bendt
 */
public class StructuredEventMessageTest extends NotificationTestCase
{
    private StructuredEventMessage objectUnderTest_;

    private StructuredEvent structuredEvent_;

    public StructuredEventMessageTest(String name, NotificationTestCaseSetup setup)
    {
        super(name, setup);
    }

    public void setUpTest() throws Exception
    {
        objectUnderTest_ = new StructuredEventMessage(getORB());
        structuredEvent_ = getTestUtils().getEmptyStructuredEvent();
    }

    public void testToTypedEvent() throws Exception
    {
        Property[] _props1 = new Property[] { new Property("operation", toAny("operationName")),
                new Property("p1", toAny("param1")), new Property("p2", toAny(20)) };

        structuredEvent_.filterable_data = _props1;

        objectUnderTest_.setStructuredEvent(structuredEvent_, false, false);

        Property[] _props2 = objectUnderTest_.toTypedEvent();

        assertEquals("operation", _props2[0].name);
        assertEquals("operationName", _props2[0].value.extract_string());
    }

    public void testToTypedEvent_FailedConversion()
    {
        Property[] _props = new Property[] { new Property("p1", toAny("param1")),
                new Property("p2", toAny(20)) };

        structuredEvent_.filterable_data = _props;

        objectUnderTest_.setStructuredEvent(structuredEvent_, false, false);

        try
        {
            objectUnderTest_.toTypedEvent();
            fail();
        } catch (NoTranslationException e)
        {
            // expected
        }
    }

    public void testTimeoutIsIgnoredIfConsumerDoesNotSupportTimeout()
    {
        Date _time = new Date(System.currentTimeMillis());

        Any _any = getClientORB().create_any();
        UtcTHelper.insert(_any, Time.corbaTime(_time));

        structuredEvent_.header.variable_header = new Property[1];
        structuredEvent_.header.variable_header[0] = new Property(StopTime.value, _any);

        objectUnderTest_.setStructuredEvent(structuredEvent_, true, false);

        assertFalse(objectUnderTest_.hasStopTime());
    }

    public static Test suite() throws Exception
    {
        return NotificationTestCase.suite(StructuredEventMessageTest.class);
    }
}