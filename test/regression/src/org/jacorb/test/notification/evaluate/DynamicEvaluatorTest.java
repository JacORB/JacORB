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

import org.omg.CORBA.Any;
import org.omg.CORBA.ORB;
import org.omg.CosNotification.Property;
import org.omg.CosNotification.PropertyHelper;
import org.omg.CosNotification.PropertySeqHelper;
import org.omg.DynamicAny.DynAnyFactory;
import org.omg.DynamicAny.DynAnyFactoryHelper;

import org.jacorb.notification.filter.DynamicEvaluator;

import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 *
 *
 *
 * @author Alphonse Bendt
 * @version $Id$
 */
public class DynamicEvaluatorTest extends TestCase {

    ORB orb_;
    DynamicEvaluator evaluator_;

    /**
     * Creates a new <code>TestDynamicEvaluator</code> instance.
     *
     * @param name test name
     */
    public DynamicEvaluatorTest (String name){
        super(name);
    }

    /**
     * @return a <code>TestSuite</code>
     */
    public static TestSuite suite(){
        TestSuite suite = new TestSuite(DynamicEvaluatorTest.class);

        return suite;
    }

    public void setUp() throws Exception {
        orb_ = ORB.init(new String[0], null);

        DynAnyFactory _dynAnyFactory =
            DynAnyFactoryHelper.narrow(orb_.resolve_initial_references("DynAnyFactory"));

        evaluator_ = new DynamicEvaluator(_dynAnyFactory);
    }

    public void testExtractAny() throws Exception {
        Any _any = orb_.create_any();
        _any.insert_long(10);
        Property p = new Property("number", _any);
        Any _testData = orb_.create_any();
        PropertyHelper.insert(_testData, p);
        Any a = evaluator_.evaluateIdentifier(_testData, "name");
        assertEquals("number", a.extract_string());
    }

    public void testEvaluateNamedValueList() throws Exception {
        Any _any = orb_.create_any();
        _any.insert_long(10);
        Property[] p = new Property[1];
        p[0] = new Property("number", _any);
        Any _testData = orb_.create_any();
        PropertySeqHelper.insert(_testData, p);
        Any _result = evaluator_.evaluateNamedValueList(_testData, "number");
        Any _second = _result.extract_any();
        assertTrue(_second.extract_long() == 10);
    }

    /**
     * Entry point
     */
    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }
}
