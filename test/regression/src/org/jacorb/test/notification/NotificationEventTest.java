package org.jacorb.test.notification;

import org.jacorb.notification.MessageFactory;
import org.jacorb.notification.filter.DynamicEvaluator;
import org.jacorb.notification.filter.EvaluationContext;
import org.jacorb.notification.filter.EvaluationResult;
import org.jacorb.notification.filter.etcl.AbstractTCLNode;
import org.jacorb.notification.filter.etcl.ETCLComponentName;
import org.jacorb.notification.filter.etcl.TCLCleanUp;
import org.jacorb.notification.filter.etcl.TCLParser;
import org.jacorb.notification.interfaces.Message;

import org.omg.CORBA.Any;
import org.omg.CosNotification.StructuredEvent;
import org.omg.DynamicAny.DynAnyFactory;
import org.omg.DynamicAny.DynAnyFactoryHelper;

import junit.framework.Test;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */
public class NotificationEventTest extends NotificationTestCase {

    MessageFactory factory_;
    Any testPerson_;
    Any testUnion_;

    StructuredEvent testStructured_;
    EvaluationContext evaluationContext_;
    NotificationTestUtils testUtils_;

    public void setUp() throws Exception {
        testUtils_ = new NotificationTestUtils(getORB());

        DynAnyFactory _dynAnyFactory =
            DynAnyFactoryHelper.narrow(getORB().resolve_initial_references("DynAnyFactory"));

        DynamicEvaluator _dynEval = new DynamicEvaluator(_dynAnyFactory);
        _dynEval.configure(getConfiguration());

        evaluationContext_ = new EvaluationContext();
        evaluationContext_.setDynamicEvaluator(_dynEval);

        factory_ = new MessageFactory();
        factory_.configure( getConfiguration() );

        testPerson_ = testUtils_.getTestPersonAny();

        TestUnion _t1 = new TestUnion();
        _t1.default_person(testUtils_.getTestPerson());
        testUnion_ = getORB().create_any();
        TestUnionHelper.insert(testUnion_, _t1);

        testStructured_ = testUtils_.getStructuredEvent();
    }


    public void testGetType() throws Exception {
        Message _event = factory_.newMessage(testPerson_);

        assertTrue(_event.getType() == Message.TYPE_ANY);
    }

    public void testEvaluate_Any() throws Exception {
        String _expr = "$.first_name";
        AbstractTCLNode _root = TCLParser.parse(_expr);

        _root.acceptPreOrder(new TCLCleanUp());

        Message _event = factory_.newMessage(testPerson_);

        EvaluationResult _result =
            _event.extractValue(evaluationContext_,
                            (ETCLComponentName) _root);

        assertEquals("firstname", _result.getString());
    }

    public void testEvaluate_Structured() throws Exception {
        String _expr = "$.header.fixed_header.event_type.domain_name";
        Message _event = factory_.newMessage(testStructured_);
        AbstractTCLNode _root = TCLParser.parse(_expr);

        _root.acceptPreOrder(new TCLCleanUp());

        EvaluationResult _result =
            _event.extractValue(evaluationContext_,
                                (ETCLComponentName) _root);

        assertEquals("TESTING", _result.getString());
    }

    public void testToStructuredEvent() throws Exception {
        Message _event = factory_.newMessage(testPerson_);
        StructuredEvent _structuredEvent = _event.toStructuredEvent();

        assertEquals("", _structuredEvent.header.fixed_header.event_type.domain_name);
        assertEquals("%ANY", _structuredEvent.header.fixed_header.event_type.type_name);

        Person _p = PersonHelper.extract(_structuredEvent.remainder_of_body);
        assertEquals("firstname", _p.first_name);
        assertEquals("lastname", _p.last_name);
    }


    public NotificationEventTest(String name, NotificationTestCaseSetup setup){
        super(name, setup);
    }


    public static Test suite() throws Exception {
        return NotificationTestCase.suite(NotificationEventTest.class);
    }
}
