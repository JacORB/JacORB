package org.jacorb.test.notification;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.log.Hierarchy;
import org.apache.log.Logger;
import org.jacorb.notification.ApplicationContext;
import org.jacorb.notification.EvaluationContext;
import org.jacorb.notification.interfaces.Message;
import org.jacorb.notification.MessageFactory;
import org.jacorb.notification.evaluate.DynamicEvaluator;
import org.jacorb.notification.evaluate.ResultExtractor;
import org.jacorb.notification.node.ComponentName;
import org.jacorb.notification.node.EvaluationResult;
import org.jacorb.notification.node.TCLCleanUp;
import org.jacorb.notification.node.AbstractTCLNode;
import org.jacorb.notification.parser.TCLParser;
import org.omg.CORBA.Any;
import org.omg.CORBA.ORB;
import org.omg.CosNotification.StructuredEvent;
import org.omg.DynamicAny.DynAnyFactory;
import org.omg.DynamicAny.DynAnyFactoryHelper;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;
import org.apache.log.Priority;

/**
 *  Unit Test for class NotificationEvent
 *
 *
 * @author Alphonse Bendt
 * @version $Id$
 */
public class NotificationEventTest extends TestCase {

    MessageFactory factory_;
    Any testPerson_;
    Any testUnion_;
    ORB orb_;
    Logger logger_ = Hierarchy.getDefaultHierarchy().getLoggerFor(getClass().getName());

    StructuredEvent testStructured_;
    EvaluationContext evaluationContext_;
    NotificationTestUtils testUtils_;

    ApplicationContext appContext_;

    public void setUp() throws Exception {
        orb_ = ORB.init(new String[0], null);
        POA _poa = POAHelper.narrow(orb_.resolve_initial_references("RootPOA"));
        appContext_ = new ApplicationContext(orb_, _poa);

        testUtils_ = new NotificationTestUtils(orb_);

        DynAnyFactory _dynAnyFactory =
            DynAnyFactoryHelper.narrow(orb_.resolve_initial_references("DynAnyFactory"));

        DynamicEvaluator _dynEval = new DynamicEvaluator(orb_, _dynAnyFactory);
        ResultExtractor _resExtr = new ResultExtractor(_dynAnyFactory);

        evaluationContext_ = new EvaluationContext();
        evaluationContext_.setDynamicEvaluator(_dynEval);
        evaluationContext_.setResultExtractor(_resExtr);

        factory_ = new MessageFactory();
        factory_.init();

        testPerson_ = testUtils_.getTestPersonAny();

        TestUnion _t1 = new TestUnion();
        _t1.default_person(testUtils_.getTestPerson());
        testUnion_ = orb_.create_any();
        TestUnionHelper.insert(testUnion_, _t1);

        testStructured_ = testUtils_.getStructuredEvent();
    }

    public void tearDown() {
        appContext_.dispose();
    }

    public void testGetType() throws Exception {
        Message _event = factory_.newEvent(testPerson_);

        assertTrue(_event.getType() == Message.TYPE_ANY);
    }

    public void testEvaluate_Any() throws Exception {
        String _expr = "$.first_name";
        AbstractTCLNode _root = TCLParser.parse(_expr);

        _root.acceptPreOrder(new TCLCleanUp());

        Message _event = factory_.newEvent(testPerson_);

        EvaluationResult _result =
            _event.extractValue(evaluationContext_,
                            (ComponentName) _root);

        assertEquals("firstname", _result.getString());
    }

    public void testEvaluate_Structured() throws Exception {
        String _expr = "$.header.fixed_header.event_type.domain_name";
        Message _event = factory_.newEvent(testStructured_);
        AbstractTCLNode _root = TCLParser.parse(_expr);

        _root.acceptPreOrder(new TCLCleanUp());

        EvaluationResult _result =
            _event.extractValue(evaluationContext_,
                                (ComponentName) _root);

        assertEquals("TESTING", _result.getString());
    }

    public void testToStructuredEvent() throws Exception {
        Message _event = factory_.newEvent(testPerson_);
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
    public static Test suite(){
        TestSuite suite;

        suite = new TestSuite(NotificationEventTest.class);

        return suite;
    }

    /**
     * Entry point
     */
    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

}
