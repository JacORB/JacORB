package org.jacorb.test.notification;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2003  Gerald Brose.
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

import org.jacorb.notification.TypedEventMessage;

import org.omg.CORBA.Any;
import org.omg.CORBA.TCKind;
import org.omg.CosNotification.EventType;
import org.omg.CosNotification.EventTypeHelper;
import org.omg.CosNotification.Property;
import org.omg.CosNotification.StructuredEvent;
import org.omg.DynamicAny.DynAnyFactoryHelper;

import junit.framework.Test;
import org.omg.CosNotification.PropertySeqHelper;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */
public class TypedEventMessageTest extends NotificationTestCase {

    private static final Property[] EMPTY_PROPS = new Property[0];

    private TypedEventMessage objectUnderTest_;

    public void setUp() throws Exception {
        objectUnderTest_ = new TypedEventMessage();
    }


    public TypedEventMessageTest(String name, NotificationTestCaseSetup setup) {
        super(name, setup);
    }


    public void testToProperty() {
        objectUnderTest_.setTypedEvent("IDL:Coffee:1.0", "doSomething", EMPTY_PROPS);

        Property[] _props = objectUnderTest_.toTypedEvent();

        assertEquals(1, _props.length);
        assertEquals("event_type", _props[0].name);

        EventType et = EventTypeHelper.extract(_props[0].value);
        assertEquals("IDL:Coffee:1.0", et.domain_name);
        assertEquals("doSomething", et.type_name);
    }


    public void testToAny() {
        objectUnderTest_.setTypedEvent("IDL:Coffee:1.0", "operation1", EMPTY_PROPS);

        Any _any = objectUnderTest_.toAny();

        assertEquals(PropertySeqHelper.type(), _any.type());
    }


    public void testToStructured() {
        objectUnderTest_.setTypedEvent("IDL:Coffee:1.0", "operationName", EMPTY_PROPS);

        StructuredEvent _structEvent = objectUnderTest_.toStructuredEvent();

        assertEquals(1, _structEvent.filterable_data.length);
        assertEquals("operation", _structEvent.filterable_data[0].name);
        assertEquals("operationName", _structEvent.filterable_data[0].value.extract_string());

        assertEquals("%TYPED", _structEvent.header.fixed_header.event_type.type_name);
    }


    public static Test suite() throws Exception {
        return NotificationTestCase.suite(TypedEventMessageTest.class);
    }
}
