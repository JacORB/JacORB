package org.jacorb.test.notification;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.jacorb.notification.filter.EvaluationContext;
import org.jacorb.notification.filter.EvaluationResult;
import org.jacorb.notification.filter.etcl.AbstractTCLNode;
import org.jacorb.notification.filter.etcl.ETCLComponentName;
import org.jacorb.notification.filter.etcl.TCLCleanUp;
import org.jacorb.notification.filter.etcl.TCLParser;
import org.jacorb.notification.impl.DefaultMessageFactory;
import org.jacorb.notification.interfaces.Message;
import org.jacorb.test.notification.common.NotificationTestCase;
import org.jacorb.test.notification.common.NotificationTestUtils;
import org.junit.Before;
import org.junit.Test;
import org.omg.CORBA.Any;
import org.omg.CosNotification.StructuredEvent;

/**
 * @author Alphonse Bendt
 */
public class NotificationEventTest extends NotificationTestCase {

    DefaultMessageFactory factory_;
    Any testPerson_;
    Any testUnion_;

    StructuredEvent testStructured_;
    EvaluationContext evaluationContext_;
    NotificationTestUtils testUtils_;

    @Before
    public void setUp() throws Exception
    {
        testUtils_ = new NotificationTestUtils(getORB());

        evaluationContext_ = new EvaluationContext(getEvaluator());

        factory_ = new DefaultMessageFactory(getORB(), getConfiguration());

        testPerson_ = testUtils_.getTestPersonAny();

        TestUnion _t1 = new TestUnion();
        _t1.default_person(testUtils_.getTestPerson());
        testUnion_ = getORB().create_any();
        TestUnionHelper.insert(testUnion_, _t1);

        testStructured_ = testUtils_.getStructuredEvent();
    }


    @Test
    public void testGetType() throws Exception {
        Message _event = factory_.newMessage(testPerson_);

        assertTrue(_event.getType() == Message.TYPE_ANY);
    }

    @Test
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

    @Test
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

    @Test
    public void testToStructuredEvent() throws Exception {
        Message _event = factory_.newMessage(testPerson_);
        StructuredEvent _structuredEvent = _event.toStructuredEvent();

        assertEquals("", _structuredEvent.header.fixed_header.event_type.domain_name);
        assertEquals("%ANY", _structuredEvent.header.fixed_header.event_type.type_name);

        Person _p = PersonHelper.extract(_structuredEvent.remainder_of_body);
        assertEquals("firstname", _p.first_name);
        assertEquals("lastname", _p.last_name);
    }
}
