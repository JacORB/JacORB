package org.jacorb.notification;

import junit.framework.TestCase;
import org.jacorb.notification.node.TCLNode;
import org.jacorb.notification.evaluate.ConstraintEvaluator;
import org.jacorb.notification.node.TCLCleanUp;
import org.omg.CORBA.ORB;
import org.omg.PortableServer.POAHelper;
import org.omg.PortableServer.POA;
import org.omg.CORBA.Any;
import org.jacorb.notification.node.ComponentOperator;
import org.apache.log4j.BasicConfigurator;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.jacorb.notification.evaluate.DynamicEvaluator;
import org.omg.DynamicAny.DynAnyFactoryHelper;
import org.jacorb.notification.evaluate.ResultExtractor;

/**
 * NotificationEventUtilsTest.java
 *
 *
 * Created: Sat Dec 14 19:54:11 2002
 *
 * @author <a href="mailto:bendt@inf.fu-berlin.de">Alphonse Bendt</a>
 * @version $Id$
 */

public class NotificationEventUtilsTest extends TestCase {

    ApplicationContext appContext_;
    EvaluationContext context_;

    public NotificationEventUtilsTest(String name) {
	super(name);
    }
    
    public void setUp() throws Exception {
	ORB _orb = ORB.init(new String[0], null);
	POA _poa = POAHelper.narrow(_orb.resolve_initial_references("RootPOA"));
	appContext_ = new ApplicationContext(_orb, _poa);

	context_ = new EvaluationContext();
	context_.setDynamicEvaluator(new DynamicEvaluator(appContext_.getOrb(), 
							  DynAnyFactoryHelper.narrow(appContext_.getOrb().resolve_initial_references("DynAnyFactory"))));

	context_.setResultExtractor(new ResultExtractor(DynAnyFactoryHelper.narrow(appContext_.getOrb().resolve_initial_references("DynAnyFactory"))));

    }

    public void testEvaluateCachesResult() throws Exception {
	TCLNode _root = ConstraintEvaluator.parse("$.first_name");
	_root.acceptPreOrder(new TCLCleanUp());

	Any _event = TestUtils.getTestPersonAny(appContext_.getOrb());
	NotificationEventUtils.evaluateComponent(context_, _event, (ComponentOperator)_root);

	assertNotNull(context_.lookupResult("$.first_name"));

	NotificationEventUtils.evaluateComponent(context_, _event, (ComponentOperator)_root);
	
	assertEquals("firstname", context_.lookupResult("$.first_name").getString());
    }

    public void testEvaluateCachesAny() throws Exception {
	TCLNode _root = ConstraintEvaluator.parse("$.home_address.street");
	_root.acceptPreOrder(new TCLCleanUp());

	Any _event = TestUtils.getTestPersonAny(appContext_.getOrb());
	NotificationEventUtils.evaluateComponent(context_, _event, (ComponentOperator)_root);

	assertNotNull(context_.lookupAny("$.home_address"));
	assertNotNull(context_.lookupAny("$.home_address.street"));

	NotificationEventUtils.evaluateComponent(context_, _event, (ComponentOperator)_root);

	context_.eraseResult("$.home_address.street");

	NotificationEventUtils.evaluateComponent(context_, _event, (ComponentOperator)_root);
    }

    public static Test suite() {
	TestSuite suite;

	suite = new TestSuite(NotificationEventUtilsTest.class);
	//	suite = new TestSuite();
	suite.addTest(new NotificationEventUtilsTest("testEvaluateCachesAny"));
	return suite;
    }

    static {
	BasicConfigurator.configure();
    }

    public static void main(String[] args) {
	junit.textui.TestRunner.run(suite());
    }
}// NotificationEventUtilsTest
