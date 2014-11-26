package org.jacorb.test.notification;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2014 Gerald Brose / The JacORB Team.
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.jacorb.notification.AnyMessage;
import org.jacorb.notification.NoTranslationException;
import org.jacorb.notification.interfaces.Message;
import org.jacorb.test.notification.common.NotificationTestCase;
import org.junit.Before;
import org.junit.Test;
import org.omg.CORBA.Any;
import org.omg.CosNotification.Property;
import org.omg.CosNotification.StructuredEvent;

/**
 * @author Alphonse Bendt
 */
public class AnyMessageTest extends NotificationTestCase
{
    private AnyMessage objectUnderTest_;

    @Before
    public void setUp() throws Exception
    {
        objectUnderTest_ = new AnyMessage();
    }

    @Test
    public void testType()
    {
        assertEquals(Message.TYPE_ANY, objectUnderTest_.getType());
    }
    
    @Test
    public void testToAny()
    {
        Any any = toAny("my precious");
        
        objectUnderTest_.setAny(any);
        
        assertEquals(any, objectUnderTest_.toAny());
    }
    
    @Test
    public void testToStructuredEvent()
    {
        Any any = toAny("value");
        
        objectUnderTest_.setAny(any);
        
        StructuredEvent event = objectUnderTest_.toStructuredEvent();
        
        assertEquals(any, event.remainder_of_body);
        assertEquals("%ANY", event.header.fixed_header.event_type.type_name);
    }
    
    @Test
    public void testToTypedEvent() throws Exception
    {
        Property[] _props = new Property[] { new Property("operation", toAny("operationName")),
                new Property("p1", toAny("param1")), new Property("p2", toAny(10)) };

        objectUnderTest_.setAny(toAny(_props));

        Property[] ps = objectUnderTest_.toTypedEvent();

        assertEquals("operation", ps[0].name);
        assertEquals("operationName", ps[0].value.extract_string());
    }

    @Test
    public void testNoTranslationPossible_1() throws Exception
    {
        Property[] _props = new Property[] { new Property("p1", toAny("param1")),
                new Property("p2", toAny(10)) };

        objectUnderTest_.setAny(toAny(_props));

        try
        {
            objectUnderTest_.toTypedEvent();

            fail();
        } catch (NoTranslationException e)
        {
            // expected
        }
    }

    @Test
    public void testNoTranslationPossible_2() throws Exception
    {
        objectUnderTest_.setAny(toAny("operation"));

        try
        {
            objectUnderTest_.toTypedEvent();

            fail();
        } catch (NoTranslationException e)
        {
            // expected
        }
    }
}