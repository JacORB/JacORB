package org.jacorb.notification;

import junit.framework.TestCase;
import org.apache.log4j.BasicConfigurator;
import org.omg.CORBA.ORB;
import org.omg.DynamicAny.DynAnyFactory;
import org.jacorb.notification.evaluate.ResultExtractor;
import org.jacorb.notification.evaluate.DynamicEvaluator;
import org.omg.DynamicAny.DynAnyFactoryHelper;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.omg.CORBA.Any;
import org.jacorb.notification.test.Person;
import org.jacorb.notification.test.Address;
import org.jacorb.notification.test.NamedValue;
import org.jacorb.notification.test.Profession;
import org.jacorb.notification.test.PersonHelper;
import org.omg.CosNotification.StructuredEvent;
import org.omg.CosNotification.EventHeader;
import org.omg.CosNotification.FixedEventHeader;
import org.omg.CosNotification.EventType;
import org.omg.CosNotification.Property;
import org.omg.CosNotification.StructuredEventHelper;

/*
 *        JacORB - a free Java ORB
 */

/**
 * NotificationEventFactoryTest.java
 *
 *
 * Created: Wed Nov 06 00:05:47 2002
 *
 * @author <a href="mailto:bendt@inf.fu-berlin.de">Alphonse Bendt</a>
 * @version $Id$
 */

public class NotificationEventFactoryTest extends TestCase {
    ORB orb_;
    NotificationEventFactory notificationEventFactory_;
    Any testPerson_;
    StructuredEvent testStructured_;

    public void testNewEventStructured() throws Exception {
	NotificationEvent _notifyEvent = notificationEventFactory_.newEvent(testStructured_);
    }

    public void testStructuredToAny() throws Exception {
	NotificationEvent _notifyEvent = notificationEventFactory_.newEvent(testStructured_);
	assertNotNull(_notifyEvent);
	Any _any = _notifyEvent.toAny();
	StructuredEvent _event = StructuredEventHelper.extract(_any);
	assertNotNull(_event);
	assertEquals("domain", _event.header.fixed_header.event_type.domain_name);
	assertEquals("type", _event.header.fixed_header.event_type.type_name);
    }

    public void testStructuredToStructured() throws Exception {
	NotificationEvent _notifyEvent = notificationEventFactory_.newEvent(testStructured_);
	assertNotNull(_notifyEvent);
	StructuredEvent _event = _notifyEvent.toStructuredEvent();
	assertNotNull(_event);
	assertEquals("domain", _event.header.fixed_header.event_type.domain_name);
	assertEquals("type", _event.header.fixed_header.event_type.type_name);
    }

    public void testNewEventAny() throws Exception {
	NotificationEvent _notifyEvent = notificationEventFactory_.newEvent(testPerson_);
	assertNotNull(_notifyEvent);
    }

    public void testAnyToStructured() throws Exception {
	NotificationEvent _notifyEvent = notificationEventFactory_.newEvent(testPerson_);
	StructuredEvent _structured = _notifyEvent.toStructuredEvent();
	assertNotNull(_structured);
	assertEquals("%ANY", _structured.header.fixed_header.event_type.type_name);
	assertEquals("", _structured.header.fixed_header.event_type.domain_name);
	assertEquals("", _structured.header.fixed_header.event_name);

	Person _p = PersonHelper.extract(_structured.remainder_of_body);
	assertNotNull(_p);
	assertEquals("firstname", _p.first_name);
	assertEquals("lastname", _p.last_name);
    }

    public void testAnyToAny() throws Exception {
	NotificationEvent _notifyEvent = notificationEventFactory_.newEvent(testPerson_);
	Any _anyEvent = _notifyEvent.toAny();
	assertNotNull(_anyEvent);

	Person _p = PersonHelper.extract(_anyEvent);
	assertNotNull(_p);
	assertEquals("firstname", _p.first_name);
	assertEquals("lastname" , _p.last_name);
    }

    public void setUp() throws Exception {
	orb_ = ORB.init(new String[0], null);
	DynAnyFactory _d = DynAnyFactoryHelper.narrow(orb_.resolve_initial_references("DynAnyFactory"));
	ResultExtractor _r = new ResultExtractor(_d);
	DynamicEvaluator _de = new DynamicEvaluator(orb_, _d);
	notificationEventFactory_ = new NotificationEventFactory(orb_, _de, _r);

	Person _person = new Person();
	Address _addr = new Address();
	NamedValue _nv = new NamedValue();
	
	_person.first_name = "firstname";
	_person.last_name = "lastname";
	_person.age = 20;
	_person.phone_numbers = new String[2];
	_person.phone_numbers[0] = "12345678";
	_person.phone_numbers[1] = "87654322";
	_person.nv = new NamedValue[2];
	_person.person_profession = Profession.STUDENT;

	_person.home_address = _addr;
	_addr.street = "Street";
	_addr.number = 20;
	_addr.city = "Berlin";

	_person.nv[0] = _nv;
	_person.nv[1] = _nv;
	_nv.name = "name";
	_nv.value = "value";

	testPerson_ = orb_.create_any();
	PersonHelper.insert(testPerson_, _person);

	testStructured_ = new StructuredEvent();
	EventHeader _header = new EventHeader();
	FixedEventHeader _fixed = new FixedEventHeader();
	_fixed.event_name = "eventname";
	_fixed.event_type = new EventType("domain", "type");
	_header.fixed_header = _fixed;
	_header.variable_header = new Property[0];

	testStructured_.header = _header;

	testStructured_.filterable_data = new Property[0];

	testStructured_.remainder_of_body = orb_.create_any();
    }

    public NotificationEventFactoryTest(String test) {
	super(test);
    }
    
    public static Test suite() {
	TestSuite suite;
	suite = new TestSuite(NotificationEventFactoryTest.class);
	return suite;
    }

    public static void main(String[] args) {
	BasicConfigurator.configure();
	junit.textui.TestRunner.run(suite());
    }

}// NotificationEventFactoryTest
