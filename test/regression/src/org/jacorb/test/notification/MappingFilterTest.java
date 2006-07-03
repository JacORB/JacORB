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

import org.jacorb.notification.filter.AbstractFilter;
import org.jacorb.notification.filter.MappingFilterImpl;
import org.jacorb.notification.filter.etcl.ETCLFilter;
import org.jacorb.notification.impl.DefaultEvaluationContextFactory;
import org.jacorb.notification.impl.DefaultMessageFactory;
import org.jacorb.test.notification.common.NotificationTestCase;
import org.jacorb.test.notification.common.NotificationTestCaseSetup;
import org.omg.CORBA.Any;
import org.omg.CORBA.AnyHolder;
import org.omg.CosNotification.EventType;
import org.omg.CosNotifyFilter.ConstraintExp;
import org.omg.CosNotifyFilter.MappingConstraintInfo;
import org.omg.CosNotifyFilter.MappingConstraintPair;
import org.omg.CosNotifyFilter.MappingFilterOperations;

/**
 * @author Alphonse Bendt
 */

public class MappingFilterTest extends NotificationTestCase
{
    Any testPerson_;

    AbstractFilter filter_;

    // //////////////////////////////////////

    public MappingFilterTest(String name, NotificationTestCaseSetup setup)
    {
        super(name, setup);
    }

    // //////////////////////////////////////

    public void setUpTest() throws Exception
    {
        filter_ = new ETCLFilter(getConfiguration(), new DefaultEvaluationContextFactory(
                getEvaluator()), new DefaultMessageFactory(getORB(), getConfiguration()), getORB(), getPOA());

        testPerson_ = getTestUtils().getTestPersonAny();
    }

    public void testMatch() throws Exception
    {
        Any defaultValue = getORB().create_any();

        MappingFilterOperations _mappingFilter = new MappingFilterImpl(getORB(),
                getConfiguration(), filter_, defaultValue);

        AnyHolder anyHolder = new AnyHolder();

        // filter is empty. should not match
        assertTrue(!_mappingFilter.match(testPerson_, anyHolder));

        // add some filter data
        Any resultToSet = getORB().create_any();

        resultToSet.insert_string("this indicates success");

        EventType[] _eventType = new EventType[1];
        _eventType[0] = new EventType("*", "*");
        ConstraintExp constraintExp = new ConstraintExp(_eventType, "$.first_name == 'firstname'");

        MappingConstraintPair[] mappingConstraintPair = new MappingConstraintPair[1];
        mappingConstraintPair[0] = new MappingConstraintPair(constraintExp, resultToSet);

        MappingConstraintInfo[] _info = _mappingFilter
                .add_mapping_constraints(mappingConstraintPair);

        assertTrue(_info.length == 1);

        assertEquals("$.first_name == 'firstname'", _info[0].constraint_expression.constraint_expr);
        assertEquals(resultToSet, _info[0].value);

        // this should match
        assertTrue(_mappingFilter.match(testPerson_, anyHolder));
        assertEquals(resultToSet, anyHolder.value);
    }

    public void testMatch2() throws Exception
    {
        Any defaultValue = getORB().create_any();

        MappingFilterOperations _mappingFilter = new MappingFilterImpl(getORB(),
                getConfiguration(), filter_, defaultValue);

        AnyHolder anyHolder = new AnyHolder();

        // filter is empty. should not match
        assertTrue(!_mappingFilter.match(testPerson_, anyHolder));

        // add some filter data
        Any resultToSet = getORB().create_any();

        resultToSet.insert_string("this is 10");

        EventType[] _eventType = new EventType[1];
        _eventType[0] = new EventType("*", "*");
        ConstraintExp constraintExp = new ConstraintExp(_eventType, "$ == 10");

        MappingConstraintPair[] mappingConstraintPair = new MappingConstraintPair[2];
        mappingConstraintPair[0] = new MappingConstraintPair(constraintExp, resultToSet);

        constraintExp = new ConstraintExp(_eventType, "$ == 20");
        resultToSet = getORB().create_any();
        resultToSet.insert_string("this is 20");
        mappingConstraintPair[1] = new MappingConstraintPair(constraintExp, resultToSet);

        MappingConstraintInfo[] _info = _mappingFilter
                .add_mapping_constraints(mappingConstraintPair);

        assertTrue(_info.length == 2);

        Any testMessage = getORB().create_any();
        testMessage.insert_long(10);

        // this should match
        assertTrue(_mappingFilter.match(testMessage, anyHolder));
        assertEquals("this is 10", anyHolder.value.extract_string());

        testMessage = getORB().create_any();
        testMessage.insert_long(20);

        assertTrue(_mappingFilter.match(testMessage, anyHolder));
        assertEquals("this is 20", anyHolder.value.extract_string());
    }

    public static Test suite() throws Exception
    {
        return NotificationTestCase.suite(MappingFilterTest.class);
    }
}
