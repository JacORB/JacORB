package org.jacorb.test.notification;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.jacorb.notification.ApplicationContext;
import org.jacorb.notification.MessageFactory;
import org.jacorb.notification.filter.DynamicEvaluator;
import org.jacorb.notification.filter.EvaluationContext;
import org.jacorb.notification.filter.EvaluationResult;
import org.jacorb.notification.filter.FilterConstraint;
import org.jacorb.notification.filter.etcl.AbstractTCLNode;
import org.jacorb.notification.filter.etcl.ETCLFilterConstraint;
import org.jacorb.notification.filter.etcl.TCLCleanUp;
import org.jacorb.notification.filter.etcl.TCLParser;
import org.jacorb.notification.interfaces.Message;
import org.omg.CORBA.Any;
import org.omg.CORBA.LongSeqHelper;
import org.omg.CORBA.ORB;
import org.omg.CosNotification.EventHeader;
import org.omg.CosNotification.EventType;
import org.omg.CosNotification.FixedEventHeader;
import org.omg.CosNotification.Property;
import org.omg.CosNotification.StructuredEvent;
import org.omg.CosNotification.StructuredEventHelper;
import org.omg.DynamicAny.DynAnyFactory;

/**
 * @author Alphonse Bendt
 */

public class NotificationTestUtils {

    ORB orb_;
    StructuredEvent structuredEvent_;
    Any structuredEventAny_;

    public NotificationTestUtils(ORB orb) {
        orb_ = orb;
    }

    public StructuredEvent getStructuredEvent() {
        StructuredEvent _structuredEvent = getEmptyStructuredEvent();

        _structuredEvent.header.fixed_header.event_name = "ALARM";
        _structuredEvent.header.fixed_header.event_type.domain_name = "TESTING";
        _structuredEvent.header.fixed_header.event_type.type_name = "TESTING";

        _structuredEvent.remainder_of_body = getTestPersonAny();

        return _structuredEvent;
    }

    public StructuredEvent getEmptyStructuredEvent() {
        FixedEventHeader _fixedHeader = new FixedEventHeader();
        _fixedHeader.event_name = "";
        _fixedHeader.event_type = new EventType("", "");
        EventHeader _header = new EventHeader(_fixedHeader, new Property[0]);

        StructuredEvent _structuredEvent =
            new StructuredEvent(_header, new Property[0], orb_.create_any());

        return _structuredEvent;
    }


    public Any getStructuredEventAny() {
        Any _structuredEventAny = orb_.create_any();

        StructuredEventHelper.insert(_structuredEventAny, getStructuredEvent());

        return _structuredEventAny;
    }


    public Person getTestPerson() {
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

    public Any getTestPersonAny() {
        Any _testPerson;

        _testPerson = orb_.create_any();
        PersonHelper.insert(_testPerson, getTestPerson());

        return _testPerson;
    }

    public Any getSizedTestData(int  size) {
        Any _testData = orb_.create_any();
        int[] _payload = new int[size];
        for (int x=0; x<size; ++x) {
            _payload[x] = x;
        }

        LongSeqHelper.insert(_testData, _payload);

        return _testData;
    }

    static StructuredEvent invalidStructuredEvent_;

    public static StructuredEvent getInvalidStructuredEvent(ORB orb) {
        if (invalidStructuredEvent_ == null) {
            synchronized(NotificationTestUtils.class.getName()) {
                if (invalidStructuredEvent_ == null) {
                    FixedEventHeader _fixedHeader = new FixedEventHeader();
                    _fixedHeader.event_name = "";
                    _fixedHeader.event_type = new EventType("","");
                    EventHeader _header = new EventHeader(_fixedHeader, new Property[0]);

                    invalidStructuredEvent_ =
                        new StructuredEvent(_header, new Property[0], orb.create_any());
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

        MessageFactory _notificationEventFactory =
            appContext.getMessageFactory();

        Message _event = null;
        try {
            _event = _notificationEventFactory.newMessage(any);
            runEvaluation(testCase, appContext, _event, expr, expect);

        } finally {
            _event.dispose();
        }
    }


    public static void runEvaluation(TestCase testCase,
                                     ApplicationContext appContext,
                                     StructuredEvent event,
                                     String expr) throws Exception {

        runEvaluation(testCase, appContext, event, expr, "TRUE");
    }

    public static void runEvaluation(TestCase testCase,
                                     ApplicationContext appContext,
                                     StructuredEvent event,
                                     String expr,
                                     String expect) throws Exception {

        MessageFactory _notificationEventFactory =
            appContext.getMessageFactory();

        Message _event = null;
        try {
            _event = _notificationEventFactory.newMessage(event);
            runEvaluation(testCase, appContext, _event, expr, expect);

        } finally {
            _event.dispose();
        }
    }


    static void runEvaluation(TestCase testCase,
                              ApplicationContext appContext,
                              Message event,
                              String expr,
                              String expect) throws Exception {

        ORB _orb = appContext.getOrb();

        DynAnyFactory _dynAnyFactory =
            appContext.getDynAnyFactory();

        DynamicEvaluator _dynamicEvaluator = appContext.getDynamicEvaluator();

        MessageFactory _notificationEventFactory =
            appContext.getMessageFactory();

        AbstractTCLNode _root = TCLParser.parse(expr);
        AbstractTCLNode _expect = TCLParser.parse(expect);

        FilterConstraint _evaluator = new ETCLFilterConstraint( _root);
        EvaluationResult _res;
        _root.acceptPostOrder(new TCLCleanUp());

        EvaluationContext _context = new EvaluationContext();
        _context.setDynamicEvaluator(_dynamicEvaluator);

        _res = _evaluator.evaluate(_context, event);

        Assert.assertEquals("expected "
                              + _root.toStringTree()
                              + " == "
                              + _expect.toStringTree(),
                              _expect.evaluate(null),
                              _res);
    }
}
