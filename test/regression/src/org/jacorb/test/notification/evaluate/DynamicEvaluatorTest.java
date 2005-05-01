package org.jacorb.test.notification.evaluate;

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

import org.jacorb.notification.filter.impl.DefaultETCLEvaluator;
import org.jacorb.test.notification.NotificationTestCase;
import org.jacorb.test.notification.NotificationTestCaseSetup;
import org.omg.CORBA.Any;
import org.omg.CosNotification.Property;
import org.omg.CosNotification.PropertyHelper;
import org.omg.CosNotification.PropertySeqHelper;

/**
 * @author Alphonse Bendt
 */

public class DynamicEvaluatorTest extends NotificationTestCase {

    private DefaultETCLEvaluator objectUnderTest_;

    public DynamicEvaluatorTest (String name, NotificationTestCaseSetup setup){
        super(name, setup);
    }

    public static Test suite() throws Exception {
        return NotificationTestCase.suite(DynamicEvaluatorTest.class);
    }


    public void setUpTest() throws Exception {
        objectUnderTest_ = new DefaultETCLEvaluator(getConfiguration(), getDynAnyFactory());
    }


    public void testExtractAny() throws Exception {
        Any _any = getORB().create_any();
        _any.insert_long(10);
        Property p = new Property("number", _any);
        Any _testData = getORB().create_any();
        PropertyHelper.insert(_testData, p);
        Any a = objectUnderTest_.evaluateIdentifier(_testData, "name");
        assertEquals("number", a.extract_string());
    }


    public void testEvaluateNamedValueList() throws Exception {
        Any _any = getORB().create_any();
        _any.insert_long(10);
        Property[] p = new Property[1];
        p[0] = new Property("number", _any);
        Any _testData = getORB().create_any();
        PropertySeqHelper.insert(_testData, p);
        Any _result = objectUnderTest_.evaluateNamedValueList(_testData, "number");
        Any _second = _result.extract_any();
        assertTrue(_second.extract_long() == 10);
    }
}
