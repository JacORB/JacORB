package org.jacorb.notification;

/*
 *        JacORB - a free Java ORB
 */

import org.omg.CORBA.Any;
import org.jacorb.notification.test.Person;
import org.jacorb.notification.test.Address;
import org.jacorb.notification.test.NamedValue;
import org.jacorb.notification.test.Profession;
import org.jacorb.notification.test.PersonHelper;
import org.omg.CORBA.ORB;
import org.omg.CosNotification.StructuredEvent;
import org.omg.CosNotification.FixedEventHeader;
import org.omg.CosNotification.EventType;
import org.omg.CosNotification.EventHeader;
import org.omg.CosNotification.Property;
import org.jacorb.notification.node.TCLNode;
import org.jacorb.notification.evaluate.ConstraintEvaluator;
import org.jacorb.notification.node.EvaluationResult;
import org.jacorb.notification.node.TCLCleanUp;
import org.omg.DynamicAny.DynAnyFactory;
import org.omg.DynamicAny.DynAnyFactoryHelper;
import org.jacorb.notification.evaluate.ResultExtractor;
import org.jacorb.notification.evaluate.DynamicEvaluator;
import junit.framework.TestCase;
import org.omg.CosNotification.StructuredEventHelper;

/**
 * TestUtils.java
 *
 *
 * Created: Sat Dec 07 16:04:32 2002
 *
 * @author <a href="mailto:bendt@inf.fu-berlin.de">Alphonse Bendt</a>
 * @version $Id$
 */

public class TestUtils {

    public static StructuredEvent getStructuredEvent(ORB orb) {
	FixedEventHeader _fixedHeader = new FixedEventHeader();
	_fixedHeader.event_name = "ALARM";
	_fixedHeader.event_type = new EventType("TESTING", "TESTING");
	EventHeader _header = new EventHeader(_fixedHeader, new Property[0]);

	StructuredEvent _event = new StructuredEvent(_header, new Property[0], getTestPersonAny(orb));
	return _event;
    }

    public static Any getStructuredEventAny(ORB orb) {
	Any _any = orb.create_any();
	StructuredEventHelper.insert(_any, getStructuredEvent(orb));
	return _any;
    }

    public static Person getTestPerson() {
    	// prepare test data
	Person _p = new Person();
	Address _a = new Address();
	NamedValue _nv = new NamedValue();

	_p.first_name = "firstname";
	_p.last_name =  "lastname";
	_p.age =        5;
	_p.phone_numbers = new String[2];
	_p.phone_numbers[0] = "12345678";
	_p.phone_numbers[1] = "";
	_p.nv = new NamedValue[2];
	_p.nv[0] = new NamedValue();
	_p.nv[1] = new NamedValue();
	_p.person_profession = Profession.STUDENT;
	_a.street = "Takustr.";
	_a.number = 9;
	_a.city = "Berlin";
	_p.home_address = _a;
	_p.aliases = new String[] {"Alias0", "Alias1", "Alias2"};
	_p.numbers = new int[] {10, 20, 30, 40, 50};

	return _p;
    }

    public static Any getTestPersonAny(ORB orb) {
	Any _testPerson;

	_testPerson = orb.create_any();
	PersonHelper.insert(_testPerson, getTestPerson());
	
	return _testPerson;
    }

    static StructuredEvent invalidStructuredEvent_;
    public static StructuredEvent getInvalidStructuredEvent(ORB orb) {
	if (invalidStructuredEvent_ == null) {
	    synchronized(TestUtils.class) {
		if (invalidStructuredEvent_ == null) {
		    FixedEventHeader _fixedHeader = new FixedEventHeader();
		    _fixedHeader.event_name = "";
		    _fixedHeader.event_type = new EventType("","");
		    EventHeader _header = new EventHeader(_fixedHeader, new Property[0]);
		    invalidStructuredEvent_ = new StructuredEvent(_header, new Property[0], orb.create_any());
		}
	    }
	}
	return invalidStructuredEvent_;
    }
    
    public static void runEvaluation(TestCase testCase, 
				     ApplicationContext appContext, 
				     Any any, 
				     String expr) throws Exception {

	runEvaluation(testCase, appContext, any, expr, "TRUE");
    }
    
    public static void runEvaluation(TestCase testCase, 
				     ApplicationContext appContext, 
				     Any any, 
				     String expr, 
				     String expect) throws Exception {
	
	ORB _orb = appContext.getOrb();

	DynAnyFactory _dynAnyFactory = DynAnyFactoryHelper.narrow(_orb.resolve_initial_references("DynAnyFactory"));
	ResultExtractor _resultExtractor = new ResultExtractor(_dynAnyFactory);
	DynamicEvaluator _dynamicEvaluator = new DynamicEvaluator(_orb, _dynAnyFactory);
	NotificationEventFactory _notificationEventFactory = 
	    new NotificationEventFactory(appContext);

	_notificationEventFactory.init();

	TCLNode _root = ConstraintEvaluator.parse(expr);
	TCLNode _expect = ConstraintEvaluator.parse(expect);
	

	ConstraintEvaluator _evaluator = new ConstraintEvaluator(_orb, _root);
	EvaluationResult _res;
	_root.acceptPreOrder(new TCLCleanUp());

	debug(_root.toStringTree());

	EvaluationContext _context = new EvaluationContext();
	_context.setDynamicEvaluator(_dynamicEvaluator);
	_context.setResultExtractor(_resultExtractor);

	NotificationEvent _event = null;
	try {
	    _event = _notificationEventFactory.newEvent(any, _context);

	    _res = _evaluator.evaluate(_event);

	    testCase.assertEquals("expected " + _root.toStringTree() + " == " + _expect.toStringTree(),
				  _expect.evaluate(null),
				  _res);
	} finally {
	    _event.release();
	}
    }

    static boolean DEBUG = false;
    static void debug(Object o) {
	if (DEBUG) {
	    System.err.println("[TestUtils]: " + o.toString());
	}
    }

}// TestUtils
