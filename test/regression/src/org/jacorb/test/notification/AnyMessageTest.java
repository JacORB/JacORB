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

import org.jacorb.notification.AnyMessage;
import org.jacorb.notification.NoTranslationException;

import org.omg.CosNotification.Property;

import junit.framework.Test;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */
public class AnyMessageTest extends NotificationTestCase {

    AnyMessage objectUnderTest_;

    public AnyMessageTest(String name, NotificationTestCaseSetup setup) {
        super(name, setup);
    }

    public void setUp() throws Exception {
        objectUnderTest_ = new AnyMessage();

        objectUnderTest_.configure(getConfiguration());
    }


    public void testToTypedEvent() throws Exception {
        Property[] _props = new Property[] {
            new Property("operation", toAny("operationName")),
            new Property("p1", toAny("param1")),
            new Property("p2", toAny(10))
        };

        objectUnderTest_.setAny(toAny(_props));

        Property[] ps = objectUnderTest_.toTypedEvent();

        assertEquals("operation", ps[0].name);
        assertEquals("operationName", ps[0].value.extract_string());
    }


    public void testNoTranslationPossible_1() throws Exception {
        Property[] _props = new Property[] {
            new Property("p1", toAny("param1")),
            new Property("p2", toAny(10))
        };

        objectUnderTest_.setAny(toAny(_props));

        try {
            objectUnderTest_.toTypedEvent();

            fail();
        } catch (NoTranslationException e) {}
    }


    public void testNoTranslationPossible_2() throws Exception {
        objectUnderTest_.setAny(toAny("operation"));

        try {
            objectUnderTest_.toTypedEvent();

            fail();
        } catch (NoTranslationException e) {}
    }


    public static Test suite() throws Exception {
        return NotificationTestCase.suite(AnyMessageTest.class);
    }
}
