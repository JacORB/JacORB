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

import static org.junit.Assert.assertEquals;
import org.jacorb.notification.TypedEventMessage;
import org.jacorb.test.notification.common.NotificationTestCase;
import org.jacorb.test.notification.typed.CoffeeHelper;
import org.junit.Before;
import org.junit.Test;
import org.omg.CORBA.Any;
import org.omg.CosNotification.EventType;
import org.omg.CosNotification.EventTypeHelper;
import org.omg.CosNotification.Property;
import org.omg.CosNotification.PropertySeqHelper;
import org.omg.CosNotification.StructuredEvent;

/**
 * @author Alphonse Bendt
 */
public class TypedEventMessageTest extends NotificationTestCase
{
    private static final Property[] EMPTY_PROPS = new Property[0];

    private TypedEventMessage objectUnderTest_;

    private static String DRINKING_COFFEE_ID = "::org::jacorb::test::notification::typed::Coffee::drinking_coffee";

    @Before
    public void setUp() throws Exception
    {
        objectUnderTest_ = new TypedEventMessage();
    }

    @Test
    public void testToProperty()
    {
        objectUnderTest_.setTypedEvent(CoffeeHelper.id(), "drinking_coffee", EMPTY_PROPS);

        Property[] _props = objectUnderTest_.toTypedEvent();

        assertEquals(1, _props.length);
        assertEquals("event_type", _props[0].name);

        EventType et = EventTypeHelper.extract(_props[0].value);
        assertEquals(CoffeeHelper.id(), et.domain_name);
        assertEquals("drinking_coffee", et.type_name);
    }

    @Test
    public void testToAny()
    {
        objectUnderTest_.setTypedEvent(CoffeeHelper.id(), DRINKING_COFFEE_ID, EMPTY_PROPS);

        Any _any = objectUnderTest_.toAny();

        assertEquals(PropertySeqHelper.type(), _any.type());

        Property[] _props = PropertySeqHelper.extract(_any);

        assertEquals(1, _props.length);

        assertEquals("operation", _props[0].name);

        assertEquals(DRINKING_COFFEE_ID, _props[0].value.extract_string());
    }

    @Test
    public void testToStructured()
    {
        objectUnderTest_.setTypedEvent(CoffeeHelper.id(), DRINKING_COFFEE_ID, EMPTY_PROPS);

        StructuredEvent _structEvent = objectUnderTest_.toStructuredEvent();

        assertEquals(1, _structEvent.filterable_data.length);

        assertEquals("operation", _structEvent.filterable_data[0].name);

        assertEquals(DRINKING_COFFEE_ID, _structEvent.filterable_data[0].value.extract_string());

        assertEquals("%TYPED", _structEvent.header.fixed_header.event_type.type_name);
    }
}
