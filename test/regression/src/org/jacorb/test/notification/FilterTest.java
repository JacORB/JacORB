package org.jacorb.test.notification;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.log.Hierarchy;
import org.apache.log.Logger;
import org.apache.log.Priority;
import org.jacorb.notification.FilterFactoryImpl;
import org.omg.CORBA.Any;
import org.omg.CosNotification.EventType;
import org.omg.CosNotifyFilter.ConstraintExp;
import org.omg.CosNotifyFilter.ConstraintInfo;
import org.omg.CosNotifyFilter.Filter;
import org.omg.CosNotifyFilter.FilterFactory;
import java.util.Random;
import org.omg.CosNotifyFilter.FilterFactoryHelper;

/**
 * FilterTest.java
 *
 *
 * Created: Sat Oct 12 20:50:59 2002
 *
 * @author Alphonse Bendt
 * @version $Id$
 */

public class FilterTest extends NotificationTestCase {

    static Random random_ = new Random(System.currentTimeMillis());

    Logger logger_ = 
	Hierarchy.getDefaultHierarchy().getLoggerFor(getClass().getName());

    FilterFactory factory_;
    Any testPerson_;
    TestUtils testUtils_;

    public FilterTest(String name, NotificationTestCaseSetup setup) {
	super(name, setup);
    }
    
    public void setUp() throws Exception {
	factory_ = FilterFactoryHelper.narrow(setup.getServerObject());

	testPerson_ = getTestUtils().getTestPersonAny();
    }

    /**
     * create remote filter object and invoke match operation on it
     */
    public void testMatch() throws Exception {
	logger_.debug("enter testMatch()");

	Filter _filter = factory_.create_filter("EXTENDED_TCL");

	logger_.debug("created Filter");

	// filter is empty. should not match
	assertTrue(!_filter.match(testPerson_));

	// add some filter data
	ConstraintExp[] _constraintExp = new ConstraintExp[1];
	EventType[] _eventType = new EventType[1];
	_eventType[0] = new EventType("*", "*");
	
	_constraintExp[0] = new ConstraintExp(_eventType, "$.first_name == 'firstname'");
	ConstraintInfo[] _info = _filter.add_constraints(_constraintExp);

	// this should match
	assertTrue(_filter.match(testPerson_));
    }

    public void testMatchModify() throws Exception {
	Filter _filter = factory_.create_filter("EXTENDED_TCL");

	// empty filter won't match
	assertTrue(!_filter.match(testPerson_));

	// add a filter
	ConstraintExp[] _constraintExp = new ConstraintExp[1];
	EventType[] _eventType = new EventType[1];
	_eventType[0] = new EventType("*", "*");
	_constraintExp[0] = new ConstraintExp(_eventType, "$.first_name == 'something'");
	ConstraintInfo[] _info = _filter.add_constraints(_constraintExp);

	// oops wrong
	assertTrue(!_filter.match(testPerson_));

	// modify the filter
	_info[0].constraint_expression.constraint_expr = "$.first_name == 'firstname'";
	_filter.modify_constraints(new int[0], _info);

	// this one should match
	assertTrue(_filter.match(testPerson_));
    }

    public void testCreateFilter() throws Exception {
	Filter _filter = factory_.create_filter("EXTENDED_TCL");

	assertEquals("EXTENDED_TCL", _filter.constraint_grammar());
    }

    public void testAddConstraints() throws Exception {
	Filter _filter = factory_.create_filter("EXTENDED_TCL");

	ConstraintExp[] _constraintExp = new ConstraintExp[1];

	EventType[] _eventType = new EventType[1];
	_eventType[0] = new EventType("domain", "name");
	String _expression = "1 + 1";
	_constraintExp[0] = new ConstraintExp(_eventType, _expression);

	ConstraintInfo[] _info = _filter.add_constraints(_constraintExp);

	assertTrue(_info.length == 1);
	assertTrue(_info[0].constraint_expression.event_types.length == 1);
	assertEquals(_expression, _info[0].constraint_expression.constraint_expr);
	assertEquals(_eventType[0].domain_name, _info[0].constraint_expression.event_types[0].domain_name);
	assertEquals(_eventType[0].type_name, _info[0].constraint_expression.event_types[0].type_name);
    }

    public void testDeleteConstraints() throws Exception {
	Filter _filter = factory_.create_filter("EXTENDED_TCL");

	ConstraintExp[] _constraintExp = new ConstraintExp[2];

	EventType[] _eventType = new EventType[1];
	_eventType[0] = new EventType("domain", "name");
	String _expression = "1 + 1";
	String _expression2 = "2 + 2";
	_constraintExp[0] = new ConstraintExp(_eventType, _expression);

	_eventType[0] = new EventType("domain2", "name");
	_constraintExp[1] = new ConstraintExp(_eventType, _expression2);

	ConstraintInfo[] _info = _filter.add_constraints(_constraintExp);

	assertTrue(_info.length == 2);
	assertTrue(_info[0].constraint_expression.event_types.length == 1);

	assertTrue(_info[1].constraint_expression.event_types.length == 1);

	int[] _delete = {_info[0].constraint_id};

	_filter.modify_constraints(_delete, new ConstraintInfo[0]);

	ConstraintInfo[] _info2 = _filter.get_all_constraints();
	assertTrue(_info2.length == 1);
	assertEquals(_info[1].constraint_id, _info2[0].constraint_id);

	assertEquals(_info[1].constraint_expression.constraint_expr, 
		     _info2[0].constraint_expression.constraint_expr);
    }    

    /**
     * multithreaded test. Some Writers modify the Constraints of a
     * Filter. Some Readers constantly access the Filter. They should
     * always get consistent data.
     *
     */
    public void testModifyConcurrent() throws Exception {
	Filter _filter = factory_.create_filter("EXTENDED_TCL");

	FilterRead _fr1 = new FilterRead(this, _filter, 100);
	FilterRead _fr2 = new FilterRead(this, _filter, 100);
	FilterRead _fr3 = new FilterRead(this, _filter, 100);
	FilterRead _fr4 = new FilterRead(this, _filter, 100);

	FilterModify _mod1 = new FilterModify(this, _filter, "true", 50);
	FilterModify _mod2 = new FilterModify(this, _filter, "false", 50);

	_fr1.start();
	_fr2.start();
 	_fr3.start();
 	_fr4.start();

	_mod1.start();
	_mod2.start();

	_fr1.join();
	_fr2.join();
	_fr3.join();
	_fr4.join();
	
	_mod1.join();
	_mod2.join();
    }

    public static Test suite() throws Exception {
	TestSuite _suite = new TestSuite("Test Filters");

	NotificationTestCaseSetup _setup =
	    new NotificationTestCaseSetup(_suite, FilterFactoryImpl.class.getName());
	
	String[] _testMethodNames = org.jacorb.test.common.TestUtils.getTestMethods(FilterTest.class);

 	for (int x=0; x<_testMethodNames.length; ++x) {
 	    _suite.addTest(new FilterTest(_testMethodNames[x], _setup));
 	}

	return _setup;
    }

    public static void main(String[] args) throws Exception {
	//	NotificationTestCase.setLogLevel("org.jacorb.test.notification", Priority.DEBUG);
	//	NotificationTestCase.setLogLevel("org.jacorb.notification", Priority.DEBUG);

	junit.textui.TestRunner.run(suite());
    }
}

class FilterRead extends Thread {
    Filter filter_;
    int iterations_;
    boolean lengthOk_ = true;
    boolean countOk_ = true;
    Logger logger_ = 
	Hierarchy.getDefaultHierarchy().getLoggerFor(getClass().getName());

    TestCase testCase_;
    static int sCounter = 0;

    void debug(String msg) {
	logger_.debug(msg);
    }

    FilterRead(TestCase testCase, Filter filter, int iterations) {
	super();
	testCase_ = testCase;
	filter_ = filter;
	iterations_ = iterations;
    }

    public void run() {
	try {
	    sleep(FilterTest.random_.nextInt(1000));
	} catch (InterruptedException e) {}

	CounterMap _counter = new CounterMap();
	for (int x=0; x<iterations_; x++) {
	    logger_.info("Reader: " + x);
	    // constraint count should always be a multiple of 10
	    ConstraintInfo[] _info = filter_.get_all_constraints();
	    testCase_.assertTrue(_info.length % 10 == 0);

	    for (int y=0; y<_info.length; y++) {
		_counter.incr(_info[y].constraint_expression.constraint_expr);
	    }
	    
	    Iterator _i = _counter.allCounters(); 

	    // constraint type count should always be a multiple of 10
	    while (_i.hasNext()) {
		Counter _c = (Counter)_i.next();
		testCase_.assertTrue(_c.value() % 10 == 0);
	    }

	    _counter.reset();

	    try {
		Thread.sleep(FilterTest.random_.nextInt(110));
	    } catch (InterruptedException ie) {}
	}
    }
}

class CounterMap {
    Map counters_ = new Hashtable();

    public void incr(Object t) {
	Counter _c = (Counter)counters_.get(t);
	if (_c == null) {
	    _c = new Counter();
	    counters_.put(t, _c);
	}
	_c.incr();
    }

    public int value(Object t) {
	Counter _c = (Counter)counters_.get(t);
	if (_c == null) {
	    return 0;
	} else {
	    return _c.value();
	}
    }

    public void reset() {
	counters_.clear();
    }

    Iterator allCounters() {
	return counters_.values().iterator();
    }
}

class Counter {
    int counter_ = 0;

    public void incr() {
	++counter_ ;
    }

    public int value() {
	return counter_;
    }
}

class FilterModify extends Thread {


    TestCase testCase_;
    Filter filter_;
    int iterations_ = 100;
    ConstraintExp[] constraintExp_;
    Logger logger_ = Hierarchy.getDefaultHierarchy().getLoggerFor(getClass().getName());

    FilterModify(TestCase testCase, Filter filter, String expression, int iterations) {
	super();
	testCase_ = testCase;
	filter_ = filter;
	iterations_ = iterations;

	constraintExp_ = new ConstraintExp[10];

	EventType[] _eventType = new EventType[1];
	_eventType[0] = new EventType("domain", expression);

	for (int x=0; x < constraintExp_.length; x++) {
	    constraintExp_[x] = new ConstraintExp(_eventType, expression);
	}
    }

    public void run() {

	try {
	    sleep(FilterTest.random_.nextInt(1000));
	} catch (InterruptedException e) {}

	ConstraintInfo[] _info = null;
	for (int x=0; x<iterations_; x++) {
	    try {
		if (_info != null) {
		    int[] _toBeDeleted = new int[_info.length];
		    for (int y=0; y<_info.length; y++) {
			_toBeDeleted[y] = _info[y].constraint_id;
		    }
		    logger_.debug("delete some");
		    // delete the constraints this thread added earlier
		    filter_.modify_constraints(_toBeDeleted, new ConstraintInfo[0]);
		    
		    try {
			Thread.sleep(FilterTest.random_.nextInt(20));
		    } catch (InterruptedException ie) {}
		    
		}
		logger_.debug("add some");
		// add some constraints
		_info = filter_.add_constraints(constraintExp_);
		
		try {
		    Thread.sleep(FilterTest.random_.nextInt(200));
		} catch (InterruptedException ie) {}
	    } catch (Exception e) {
		e.printStackTrace();
		testCase_.fail();
	    }
	}
    }
}
