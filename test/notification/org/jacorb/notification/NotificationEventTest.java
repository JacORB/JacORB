package org.jacorb.notification;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.jacorb.notification.NotificationEventFactory;
import org.omg.CORBA.ORB;
import org.jacorb.notification.evaluate.DynamicEvaluator;
import org.omg.DynamicAny.DynAnyFactoryHelper;
import org.omg.DynamicAny.DynAnyFactory;
import org.jacorb.notification.evaluate.ResultExtractor;
import org.omg.CORBA.Any;
import org.jacorb.notification.evaluate.ConstraintEvaluator;
import org.jacorb.notification.EvaluationContext;
import org.jacorb.notification.node.TCLNode;
import org.jacorb.notification.node.TCLCleanUp;
import org.jacorb.notification.node.EvaluationResult;
import org.apache.log4j.BasicConfigurator;
import org.jacorb.notification.test.TestUnion;
import org.jacorb.notification.test.TestUnionHelper;
import org.jacorb.notification.node.ComponentOperator;
import org.omg.CosNotification.StructuredEvent;
import org.jacorb.notification.test.PersonHelper;
import org.jacorb.notification.test.Person;
import org.apache.log4j.Logger;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;

/**
 *  Unit Test for class NotificationEvent
 *
 *
 * Created: Sat Dec 07 15:54:45 2002
 *
 * @author <a href="mailto:bendt@inf.fu-berlin.de">Alphonse Bendt</a>
 * @version $Id$
 */
public class NotificationEventTest extends TestCase {

    NotificationEventFactory factory_;
    Any testPerson_;
    Any testUnion_;
    ORB orb_;
    Logger logger_ = Logger.getLogger("TEST.NotificationEventTest");
    StructuredEvent testStructured_;
    EvaluationContext evaluationContext_;

    public void setUp() throws Exception {
	orb_ = ORB.init(new String[0], null);
	POA _poa = POAHelper.narrow(orb_.resolve_initial_references("RootPOA"));
	ApplicationContext _appContext = new ApplicationContext(orb_, _poa);

	DynAnyFactory _dynAnyFactory = DynAnyFactoryHelper.narrow(orb_.resolve_initial_references("DynAnyFactory"));
	DynamicEvaluator _dynEval = new DynamicEvaluator(orb_, _dynAnyFactory);
	ResultExtractor _resExtr = new ResultExtractor(_dynAnyFactory);

	evaluationContext_ = new EvaluationContext();
	evaluationContext_.setDynamicEvaluator(_dynEval);
	evaluationContext_.setResultExtractor(_resExtr);

	factory_ = new NotificationEventFactory(_appContext);
	factory_.init();

	testPerson_ = TestUtils.getTestPersonAny(orb_);

	TestUnion _t1 = new TestUnion();
	_t1.default_person(TestUtils.getTestPerson());
	testUnion_ = orb_.create_any();
	TestUnionHelper.insert(testUnion_, _t1);

	testStructured_ = TestUtils.getStructuredEvent(orb_);
    }

    public void testGetType() throws Exception {
	NotificationEvent _event = factory_.newEvent(testPerson_, evaluationContext_);

	assertTrue(_event.getType() == NotificationEvent.TYPE_ANY);
    }

    public void testTestExist_Any() throws Exception {
	NotificationEvent _event = factory_.newEvent(testPerson_, evaluationContext_);

	String _expr = "$.home_address.street";
	TCLNode _root = ConstraintEvaluator.parse(_expr);

	_root.acceptPreOrder(new TCLCleanUp());
	
	EvaluationResult _result = _event.testExists((ComponentOperator)_root);

	assertTrue(_result.getBool());
    }

    public void testTestExist_Structured() throws Exception {
	NotificationEvent _event = factory_.newEvent(testStructured_, evaluationContext_);

	String _expr = "$.header.fixed_header";
	TCLNode _root = ConstraintEvaluator.parse(_expr);
	
	_root.acceptPreOrder(new TCLCleanUp());

	EvaluationResult _result = _event.testExists((ComponentOperator)_root);

	assertTrue(_result.getBool());
    }

    public void testHasDefault_Any() throws Exception {
	String _expr = "$._d";
	TCLNode _root = ConstraintEvaluator.parse(_expr);

	_root.acceptPreOrder(new TCLCleanUp());

	NotificationEvent _event = factory_.newEvent(testUnion_, evaluationContext_);
	EvaluationResult _result = _event.hasDefault((ComponentOperator)_root);
	
	assertTrue(_result.getBool());
    }

    public void testEvaluate_Any() throws Exception {
	String _expr = "$.first_name";
	TCLNode _root = ConstraintEvaluator.parse(_expr);

	_root.acceptPreOrder(new TCLCleanUp());

	NotificationEvent _event = factory_.newEvent(testPerson_, evaluationContext_);
	EvaluationResult _result = _event.evaluate((ComponentOperator)_root);
	
	assertEquals("firstname", _result.getString());
    }

    public void testEvaluate_Structured() throws Exception {
	String _expr = "$.header.fixed_header.event_type.domain_name";
	NotificationEvent _event = factory_.newEvent(testStructured_, evaluationContext_);
	TCLNode _root = ConstraintEvaluator.parse(_expr);

	_root.acceptPreOrder(new TCLCleanUp());

	logger_.debug(_root.toStringTree());
	EvaluationResult _result = _event.evaluate((ComponentOperator)_root);

	assertEquals("TESTING", _result.getString());
    }

    public void testToStructuredEvent() throws Exception {
	NotificationEvent _event = factory_.newEvent(testPerson_, evaluationContext_);
	StructuredEvent _structuredEvent = _event.toStructuredEvent();

	assertEquals("", _structuredEvent.header.fixed_header.event_type.domain_name);
	assertEquals("%ANY", _structuredEvent.header.fixed_header.event_type.type_name);

	Person _p = PersonHelper.extract(_structuredEvent.remainder_of_body);
	assertEquals("firstname", _p.first_name);
	assertEquals("lastname", _p.last_name);
    }

    //    public 

    /** 
     * Creates a new <code>NotificationEventTest</code> instance.
     *
     * @param name test name
     */
    public NotificationEventTest(String name){
	super(name);
    }

    /**
     * @return a <code>TestSuite</code>
     */
    public static TestSuite suite(){
	TestSuite suite;

	suite = new TestSuite(NotificationEventTest.class);
	//suite = new TestSuite();
	suite.addTest(new NotificationEventTest("testTestExist_Structured"));
	return suite;
    }

    static {
	BasicConfigurator.configure();
    }

    /** 
     * Entry point 
     */ 
    public static void main(String[] args) {
	junit.textui.TestRunner.run(suite());
    }
}// NotificationEventTest
