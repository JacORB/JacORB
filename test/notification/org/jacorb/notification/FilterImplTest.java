package org.jacorb.notification;

/*
 *        JacORB - a free Java ORB
 */

import junit.framework.TestCase;
import org.omg.CORBA.ORB;
import org.omg.DynamicAny.DynAnyFactory;
import org.jacorb.notification.evaluate.ResultExtractor;
import org.jacorb.notification.evaluate.DynamicEvaluator;
import org.omg.DynamicAny.DynAnyFactoryHelper;
import org.omg.CosNotifyFilter.ConstraintExp;
import org.omg.CosNotification.EventType;
import java.util.Iterator;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.apache.log4j.BasicConfigurator;
import org.omg.CosNotifyFilter.ConstraintInfo;
import org.omg.PortableServer.POAHelper;

/**
 * FilterImplTest.java
 *
 *
 * Created: Sat Nov 09 16:34:05 2002
 *
 * @author <a href="mailto:bendt@inf.fu-berlin.de">Alphonse Bendt</a>
 * @version $Id$
 */

public class FilterImplTest extends TestCase {
    ORB orb_;
    DynAnyFactory dynAnyFactory_;
    ResultExtractor resultExtractor_;
    DynamicEvaluator dynamicEvaluator_;
    FilterImpl filter_;

    public FilterImplTest(String test) {
	super(test);
    }

    public void setUp() throws Exception {
	orb_ = ORB.init(new String[0], null);
	ApplicationContext _appContext = 
	    new ApplicationContext(orb_, POAHelper.narrow(orb_.resolve_initial_references("RootPOA")));

	dynAnyFactory_ = DynAnyFactoryHelper.narrow(orb_.resolve_initial_references("DynAnyFactory"));
	resultExtractor_ = new ResultExtractor(dynAnyFactory_);
	dynamicEvaluator_ = new DynamicEvaluator(orb_, dynAnyFactory_);
	filter_ = new FilterImpl(FilterFactoryImpl.CONSTRAINT_GRAMMAR, 
				 _appContext, 
				 dynAnyFactory_, 
				 resultExtractor_, 
				 dynamicEvaluator_);
    }

    public void testIterator() throws Exception {
	ConstraintExp[] _exp = new ConstraintExp[1];
	for (int x=0; x<_exp.length; ++x) {
	    _exp[x] = new ConstraintExp();
	}
	EventType[] _eventType = new EventType[2];
	_eventType[0] = new EventType("*", "*");
	_eventType[1] = new EventType("domain*", "type*");
	_exp[0] = new ConstraintExp(_eventType, "1");
	filter_.add_constraints(_exp);

	Iterator _i = filter_.getIterator(FilterUtils.calcConstraintKey("domain1", "type1"));
	int _count = 0;
	while (_i.hasNext()) {
	    _count++;
	    _i.next();
	}
	assertTrue(_count == 2);
    }

    public void testIterator2() throws Exception {
	ConstraintExp[] _exp = new ConstraintExp[1];
	for (int x=0; x<_exp.length; ++x) {
	    _exp[x] = new ConstraintExp();
	}
	EventType[] _eventType = new EventType[2];
	_eventType[0] = new EventType("*", "*");
	_eventType[1] = new EventType("domain*", "type*");
	_exp[0] = new ConstraintExp(_eventType, "1");
	filter_.add_constraints(_exp);

	Iterator _i = filter_.getIterator(FilterUtils.calcConstraintKey("domain1", "type1"));
	int _count = 0;
	while (_i.hasNext()) {
	    _count++;
	    ConstraintEntry _e = (ConstraintEntry)_i.next();
	    assertEquals("1", _e.constraintEvaluator_.constraint_);
	}
	assertTrue(_count == 2);

	ConstraintExp[] _exp2 = new ConstraintExp[1];
	_exp2[0] = new ConstraintExp();
	
	EventType[] _eventType2 = new EventType[2];
	_eventType2[0] = new EventType("*", "*");
	_eventType2[1] = new EventType("domain*", "type*");
	_exp2[0] = new ConstraintExp(_eventType2, "2");
	filter_.add_constraints(_exp2);

	_i = filter_.getIterator(FilterUtils.calcConstraintKey("domain1", "type1"));
	_count = 0;
	while (_i.hasNext()) {
	    _count++;
	    ConstraintEntry _e = (ConstraintEntry)_i.next();
	    assertTrue(_e.constraintEvaluator_.constraint_.equals("1") || 
		       _e.constraintEvaluator_.constraint_.equals("2"));
	}
	assertTrue(_count == 4);
    }

    public void testAddRemove() throws Exception {
	ConstraintExp[] _exp = new ConstraintExp[1];
	for (int x=0; x<_exp.length; ++x) {
	    _exp[x] = new ConstraintExp();
	}
	EventType[] _eventType = new EventType[2];
	_eventType[0] = new EventType("*", "*");
	_eventType[1] = new EventType("domain*", "type*");
	_exp[0] = new ConstraintExp(_eventType, "1");
	filter_.add_constraints(_exp);

	ConstraintExp[] _exp2 = new ConstraintExp[1];
	_exp2[0] = new ConstraintExp();
	
	EventType[] _eventType2 = new EventType[2];
	_eventType2[0] = new EventType("*", "*");
	_eventType2[1] = new EventType("domain*", "type*");
	_exp2[0] = new ConstraintExp(_eventType2, "2");
	ConstraintInfo[] _info = filter_.add_constraints(_exp2);

	Iterator _i = filter_.getIterator(FilterUtils.calcConstraintKey("domain1", "type1"));
	int _count = 0;
	while (_i.hasNext()) {
	    _count++;
	    ConstraintEntry _e = (ConstraintEntry)_i.next();
	    assertTrue(_e.constraintEvaluator_.constraint_.equals("1") || 
		       _e.constraintEvaluator_.constraint_.equals("2"));
	}
	assertTrue(_count == 4);

	int[] _delete_ids = new int[_info.length];
	for (int x=0; x<_delete_ids.length; ++x) {
	    _delete_ids[x] = _info[x].constraint_id;
	}
	filter_.modify_constraints(_delete_ids, new ConstraintInfo[0]);

	_i = filter_.getIterator(FilterUtils.calcConstraintKey("domain1", "type1"));
	_count = 0;
	while (_i.hasNext()) {
	    _count++;
	    ConstraintEntry _e = (ConstraintEntry)_i.next();
	    assertTrue(_e.constraintEvaluator_.constraint_.equals("1"));
	}
	assertTrue(_count == 2);
    }


    public static Test suite() {
	TestSuite suite;

	//      suite = new TestSuite();
	//	suite.addTest(new FilterImplTest("testDeleteConstraints"));
	suite = new TestSuite(FilterImplTest.class);
	
	return suite;
    }

    static {
	BasicConfigurator.configure();
    }

    public static void main(String[] args) {
	junit.textui.TestRunner.run(suite());
    }

}// FilterImplTest
