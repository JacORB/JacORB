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
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;

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

    class DerivedNotificationEventFactory extends NotificationEventFactory {
	public DerivedNotificationEventFactory(ApplicationContext c) {
	    super(c);
	}

	// make protected methods visible for testing
	public NotificationEvent newEvent(StructuredEvent event) {
	    return super.newEvent(event);
	}

	public NotificationEvent newEvent(Any event) {
	    return super.newEvent(event);
	}
    }

    ORB orb_;
    DerivedNotificationEventFactory notificationEventFactory_;
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
	ApplicationContext _appContext = new ApplicationContext(orb_, _poa);

	DynAnyFactory _d = DynAnyFactoryHelper.narrow(orb_.resolve_initial_references("DynAnyFactory"));
	ResultExtractor _r = new ResultExtractor(_d);
	DynamicEvaluator _de = new DynamicEvaluator(orb_, _d);
	notificationEventFactory_ = new DerivedNotificationEventFactory(_appContext);
	notificationEventFactory_.init();

	testPerson_ = TestUtils.getTestPersonAny(orb_);

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
	suite= new TestSuite();
	suite.addTest(new NotificationEventFactoryTest("testStructuredToAny"));
	suite = new TestSuite(NotificationEventFactoryTest.class);
	return suite;
    }

    public static void main(String[] args) {
	BasicConfigurator.configure();
	junit.textui.TestRunner.run(suite());
    }

}// NotificationEventFactoryTest
