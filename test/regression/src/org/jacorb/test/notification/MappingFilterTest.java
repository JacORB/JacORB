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
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.omg.CosNotifyFilter.FilterFactory;
import org.omg.CosNotifyFilter.FilterFactoryHelper;
import org.omg.CORBA.Any;
import org.omg.CosNotifyFilter.MappingFilter;
import org.omg.CORBA.AnyHolder;
import org.omg.CosNotifyFilter.MappingConstraintInfo;
import org.omg.CosNotifyFilter.ConstraintExp;
import org.omg.CosNotification.EventType;
import org.omg.CosNotifyFilter.MappingConstraintPair;
import org.jacorb.notification.FilterFactoryImpl;
import org.jacorb.notification.ApplicationContext;
import org.omg.CORBA.ORB;
import org.omg.PortableServer.POAHelper;
import org.jacorb.notification.FilterImpl;
import org.jacorb.notification.MappingFilterImpl;
import org.apache.log.Priority;
import org.omg.CosNotifyFilter.MappingFilterOperations;
import org.omg.CosNotifyFilter.InvalidGrammar;

/**
 *  Unit Test for class MappingFilter
 *
 *
 * Created: Mon Jun  9 19:04:49 2003
 *
 * @author Alphonse Bendt
 * @version $Id$
 */

public class MappingFilterTest extends NotificationTestCase 
{

    FilterFactory filterFactory_;
    Any testPerson_;
    TestUtils testUtils_;

    FilterImpl filter_;
    ApplicationContext appContext_;

    /** 
     * Creates a new <code>MappingFilterTest</code> instance.
     *
     * @param name test name
     */
    public MappingFilterTest (String name, NotificationTestCaseSetup setup)
    {
	super(name, setup);
    }

    public void setUp() throws Exception {
	ORB orb_ = ORB.init(new String[0], null);

	appContext_ = 
	    new ApplicationContext(orb_, POAHelper.narrow(orb_.resolve_initial_references("RootPOA")));

	filter_ = new FilterImpl(appContext_, FilterFactoryImpl.CONSTRAINT_GRAMMAR);

	filterFactory_ = new FilterFactoryImpl(appContext_).getFilterFactory();

	testPerson_ = getTestUtils().getTestPersonAny();

    }

    public void testCreate() throws Exception {
	Any _defaultValue = getORB().create_any();

	MappingFilter _filter = filterFactory_.create_mapping_filter("EXTENDED_TCL", _defaultValue);

	assertEquals("EXTENDED_TCL", _filter.constraint_grammar());

	try {
	    filterFactory_.create_mapping_filter("SOMETHING_ELSE", _defaultValue);
	    fail();
	} catch (InvalidGrammar e) {}
    }

    public void testMatch() throws Exception {
	Any defaultValue = getORB().create_any();

	MappingFilterOperations _mappingFilter = 
	    filterFactory_.create_mapping_filter(FilterFactoryImpl.CONSTRAINT_GRAMMAR, defaultValue);

	_mappingFilter = new MappingFilterImpl(appContext_, filter_, defaultValue);

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

	MappingConstraintInfo[] _info = _mappingFilter.add_mapping_constraints(mappingConstraintPair);

	assertTrue(_info.length == 1);

	assertEquals("$.first_name == 'firstname'", _info[0].constraint_expression.constraint_expr);
	assertEquals(resultToSet, _info[0].value);

	// this should match
	assertTrue(_mappingFilter.match(testPerson_, anyHolder));
	assertEquals(resultToSet, anyHolder.value);
    }


    public void testMatch2() throws Exception {
	Any defaultValue = getORB().create_any();

	MappingFilterOperations _mappingFilter = 
	    filterFactory_.create_mapping_filter(FilterFactoryImpl.CONSTRAINT_GRAMMAR, defaultValue);

	_mappingFilter = new MappingFilterImpl(appContext_, filter_, defaultValue);

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

	MappingConstraintInfo[] _info = _mappingFilter.add_mapping_constraints(mappingConstraintPair);

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

    /**
     * @return a <code>TestSuite</code>
     */
    public static Test suite() throws Exception 
    {
	TestSuite _suite = new TestSuite("Test MappingFilters");

	NotificationTestCaseSetup _setup =
	    new NotificationTestCaseSetup(_suite);
	
	String[] _testMethodNames = 
	    org.jacorb.test.common.TestUtils.getTestMethods(MappingFilterTest.class);

 	for (int x=0; x<_testMethodNames.length; ++x) {
 	    _suite.addTest(new MappingFilterTest(_testMethodNames[x], _setup));
 	}

	return _setup;
    }

    /** 
     * Entry point 
     */ 
    public static void main(String[] args) throws Exception
    {
	junit.textui.TestRunner.run(suite());
    }

}
