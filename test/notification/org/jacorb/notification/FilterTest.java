package org.jacorb.notification;

/*
 *        JacORB - a free Java ORB
 */

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.jacorb.notification.test.Address;
import org.jacorb.notification.test.NamedValue;
import org.jacorb.notification.test.Person;
import org.jacorb.notification.test.PersonHelper;
import org.jacorb.notification.test.Profession;
import org.omg.CORBA.Any;
import org.omg.CORBA.ORB;
import org.omg.CosNotification.EventType;
import org.omg.CosNotifyFilter.ConstraintExp;
import org.omg.CosNotifyFilter.ConstraintInfo;
import org.omg.CosNotifyFilter.Filter;
import org.omg.CosNotifyFilter.FilterFactory;
import org.omg.CosNotifyFilter.FilterFactoryHelper;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

/**
 * FilterTest.java
 *
 *
 * Created: Sat Oct 12 20:50:59 2002
 *
 * @author <a href="mailto:bendt@inf.fu-berlin.de">Alphonse Bendt</a>
 * @version $Id$
 */

public class FilterTest extends TestCase {

    static {
	BasicConfigurator.configure();
    }

    ORB orb_;
    Logger logger_ = Logger.getLogger("TEST.FilterTest");

    FilterFactory factory_;
    Any testPerson_;

    public FilterTest(String name) {
	super(name);
    }
    
    public void setUp() throws Exception {
	orb_ = ORB.init(new String[0], null);
	POA _poa = POAHelper.narrow(orb_.resolve_initial_references("RootPOA"));
	ApplicationContext _appContext = new ApplicationContext(orb_, _poa);

	FilterFactoryImpl _filterFactoryServant = 
	    new FilterFactoryImpl(_appContext);

	factory_ = _filterFactoryServant._this(orb_);

	testPerson_ = TestUtils.getTestPersonAny(orb_);
    }

    /**
     * create remote filter object and invoke match operation on it
     */
    public void testMatch() throws Exception {
	Filter _filter = factory_.create_filter("EXTENDED_TCL");

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
	assertEquals(_info[1].constraint_expression.constraint_expr, _info2[0].constraint_expression.constraint_expr);
    }
    

    /**
     * multithreaded test. Some Writers modify the Constraints of a
     * Filter. Some Readers constantly access the Filter. They should
     * always get consistent data.
     *
     */
    public void testModifyConcurrent() throws Exception {
	Filter _filter = factory_.create_filter("EXTENDED_TCL");

	FilterRead _fr1 = new FilterRead(_filter, 10);
	FilterRead _fr2 = new FilterRead(_filter, 10);
	FilterRead _fr3 = new FilterRead(_filter, 10);
	FilterRead _fr4 = new FilterRead(_filter, 10);

	FilterModify _mod1 = new FilterModify(_filter, "true", 2);
	FilterModify _mod2 = new FilterModify(_filter, "false", 2);

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

	assertTrue(_fr1.checkStatus());
	assertTrue(_fr2.checkStatus());
	assertTrue(_fr3.checkStatus());	
	assertTrue(_fr4.checkStatus());

	assertTrue(_mod1.checkStatus());
	assertTrue(_mod2.checkStatus());
    }

    public static Test suite() {
	TestSuite suite;

	suite = new TestSuite();
	suite.addTest(new FilterTest("testMatch"));
	suite = new TestSuite(FilterTest.class);
	
	return suite;
    }

    public static void main(String[] args) {
	BasicConfigurator.configure();
	junit.textui.TestRunner.run(suite());
    }
}// FilterTest

class FilterRead extends Thread {
    Filter filter_;
    int iterations_;
    boolean lengthOk_ = true;
    boolean countOk_ = true;
    Logger logger_ = Logger.getLogger("Test.FilterRead");
    static int sCounter = 0;

    void debug(String msg) {
	logger_.debug(msg);
    }

    FilterRead(Filter filter, int iterations) {
	super();
	filter_ = filter;
	iterations_ = iterations;
    }

    public boolean checkStatus() {
	if (!lengthOk_) {
	    return false;
	}
	if (!countOk_) {
	    return false;
	}
	return true;
    }

    public void run() {
	CounterMap _counter = new CounterMap();
	for (int x=0; x<iterations_; x++) {
	    // constraint count should always be a multiple of 10
	    ConstraintInfo[] _info = filter_.get_all_constraints();
	    if ((_info.length % 10) != 0) {
		logger_.debug("length: " + _info.length);
		lengthOk_ = false;
	    }

	    logger_.debug("got " + _info.length + " entries");

	    for (int y=0; y<_info.length; y++) {
		_counter.incr(_info[y].constraint_expression.constraint_expr);
	    }
	    
	    Iterator _i = _counter.allCounters(); 

	    // constraint type count should always be a multiple of 10
	    while (_i.hasNext()) {
		Counter _c = (Counter)_i.next();
		if ((_c.value() % 10 ) != 0) {
		    logger_.fatal("Wrong count: " + _c.value() + "% 10 = " + (_c.value() % 10));
		    countOk_ = false;
		}
	    }
	    _counter.reset();
	    try {
		Thread.sleep(10);
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
    Filter filter_;
    boolean lengthOk_ = true;
    int iterations_ = 100;
    ConstraintExp[] constraintExp_;
    boolean exceptionOk_ = true;
    Logger logger_ = Logger.getLogger("Test.FilterModifier");

    FilterModify(Filter filter, String expression, int iterations) {
	super();
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
			Thread.sleep(10);
		    } catch (InterruptedException ie) {}
		    
		}
		logger_.debug("add some");
		// add some constraints
		_info = filter_.add_constraints(constraintExp_);
		
		try {
		    Thread.sleep(100);
		} catch (InterruptedException ie) {}
	    } catch (Exception e) {
		e.printStackTrace();
		exceptionOk_ = false;
	    }
	}
    }
    
    boolean checkStatus() {
	if (!lengthOk_) {
	    return false;
	}
	if (!exceptionOk_) {
	    return false;
	}
	return true;
    }
}
