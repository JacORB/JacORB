package org.jacorb.test.notification;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.jacorb.notification.ApplicationContext;
import org.jacorb.notification.NotificationEvent;
import org.jacorb.notification.NotificationEventFactory;
import org.omg.CORBA.Any;
import org.omg.CORBA.ORB;
import org.omg.CosNotification.EventHeader;
import org.omg.CosNotification.EventType;
import org.omg.CosNotification.FixedEventHeader;
import org.omg.CosNotification.Property;
import org.omg.CosNotification.StructuredEvent;
import org.omg.CosNotification.StructuredEventHelper;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;

/**
 * NotificationEventFactoryTest.java
 *
 *
 * @author Alphonse Bendt
 * @version $Id$
 */

public class NotificationEventFactoryTest extends TestCase {

    class DerivedNotificationEventFactory extends NotificationEventFactory {
	public DerivedNotificationEventFactory(ApplicationContext c) {
	    super(c);
	}

	// override protected methods to be visible for testing
	public NotificationEvent newEvent(StructuredEvent event) {
	    return super.newEvent(event);
	}

	public NotificationEvent newEvent(Any event) {
	    return super.newEvent(event);
	}
    }

    ApplicationContext appContext_;
    ORB orb_;
    DerivedNotificationEventFactory notificationEventFactory_;
    TestUtils testUtils_;
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
	POA _poa = POAHelper.narrow(orb_.resolve_initial_references("RootPOA"));
	testUtils_ = new TestUtils(orb_);

	appContext_ = new ApplicationContext(orb_, _poa, false);

	notificationEventFactory_ = new DerivedNotificationEventFactory(appContext_);
	notificationEventFactory_.init();

	testPerson_ = testUtils_.getTestPersonAny();

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

    public void tearDown() {
	appContext_.dispose();
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
	junit.textui.TestRunner.run(suite());
    }

}
