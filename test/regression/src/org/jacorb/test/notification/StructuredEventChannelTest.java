package org.jacorb.test.notification;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.jacorb.notification.util.LogConfiguration;
import org.jacorb.test.notification.Address;
import org.jacorb.test.notification.NamedValue;
import org.jacorb.test.notification.Person;
import org.jacorb.test.notification.PersonHelper;
import org.jacorb.test.notification.Profession;
import org.omg.CORBA.Any;
import org.omg.CORBA.IntHolder;
import org.omg.CORBA.ORB;
import org.omg.CosNotification.EventHeader;
import org.omg.CosNotification.EventType;
import org.omg.CosNotification.FixedEventHeader;
import org.omg.CosNotification.Property;
import org.omg.CosNotification.StructuredEvent;
import org.omg.CosNotifyChannelAdmin.EventChannel;
import org.omg.CosNotifyChannelAdmin.EventChannelFactory;
import org.omg.CosNotifyFilter.FilterFactory;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;
import org.omg.CosNotifyFilter.Filter;
import org.omg.CosNotifyFilter.ConstraintExp;
import org.omg.CosNotifyFilter.ConstraintInfo;
import org.apache.log.Logger;
import org.apache.log.Hierarchy;
import org.apache.log.Priority;

/**
 * StructuredEventChannelTest.java
 *
 * @author Alphonse Bendt
 * @version $Id$
 */

public class StructuredEventChannelTest extends NotificationTestCase {

    Logger logger_ = Hierarchy.getDefaultHierarchy().getLoggerFor(getClass().getName());

    EventChannel channel_;
    EventChannelFactory channelFactory_;
    StructuredEvent testEvent_;

    Filter trueFilter_;
    Filter falseFilter_;
    TestUtils testUtils_; 

    public StructuredEventChannelTest(String name, NotificationTestCaseSetup setup) {
	super(name, setup);
    }

    public void tearDown() {
	super.tearDown();
	channel_.destroy();
    }

    public void setUp() throws Exception {
	channelFactory_ = getEventChannelFactory();

	Property[] qos = new Property[0];
	Property[] adm = new Property[0];
	IntHolder _channelId = new IntHolder();

 	channel_ = channelFactory_.create_channel(qos, adm, _channelId);

 	// set test event type and name
	testEvent_ = new StructuredEvent();
	EventType _type = new EventType("testDomain", "testType");
	FixedEventHeader _fixed = new FixedEventHeader(_type, "testing");

	// complete header date
	Property[] _variable = new Property[0];
	testEvent_.header = new EventHeader(_fixed, _variable);

	// set filterable event body data
	testEvent_.filterable_data = new Property[1];

	Any _personAny = getORB().create_any();

	// prepare filterable body data
	Person _p = getTestUtils().getTestPerson();
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

	PersonHelper.insert(_personAny, _p);
	testEvent_.filterable_data[0] = new Property("person", _personAny);
	
	testEvent_.remainder_of_body = getORB().create_any();
	
	trueFilter_ = channel_.default_filter_factory().create_filter("EXTENDED_TCL");

	ConstraintExp[] _constraintExp = new ConstraintExp[1];
	EventType[] _eventType = new EventType[1];
	_eventType[0] = new EventType("*", "*");
	
	_constraintExp[0] = new ConstraintExp(_eventType, "true");
	ConstraintInfo[] _info = trueFilter_.add_constraints(_constraintExp);

	falseFilter_ = channel_.default_filter_factory().create_filter("EXTENDED_TCL");

	_constraintExp = new ConstraintExp[1];
	_eventType = new EventType[1];
	_eventType[0] = new EventType("*", "*");
	
	_constraintExp[0] = new ConstraintExp(_eventType, "false");
	_info = falseFilter_.add_constraints(_constraintExp);
    }

    public void testDestroyChannelDisconnectsClients() throws Exception {	
	Property[] _p = new Property[0];
	IntHolder _channelId = new IntHolder();

	EventChannel _channel = channelFactory_.create_channel(_p, _p, _channelId);
	
	StructuredPushSender _pushSender = new StructuredPushSender(this,testEvent_);
	StructuredPullSender _pullSender = new StructuredPullSender(this,testEvent_);
	StructuredPushReceiver _pushReceiver = new StructuredPushReceiver(this);
	StructuredPullReceiver _pullReceiver = new StructuredPullReceiver(this);

	_pushSender.connect(getSetup(), _channel,false);
	_pullSender.connect(getSetup(), _channel,false);
	_pushReceiver.connect(getSetup(), _channel,false);
	_pullReceiver.connect(getSetup(), _channel,false);

	assertTrue(_pushSender.isConnected());
	assertTrue(_pullSender.isConnected());
	assertTrue(_pushReceiver.isConnected());
	assertTrue(_pullReceiver.isConnected());
	
	_channel.destroy();

	assertTrue(!_pushSender.isConnected());
	assertTrue(!_pullSender.isConnected());
	assertTrue(!_pushReceiver.isConnected());
	assertTrue(!_pullReceiver.isConnected());
    }

    public void testSendPushPush() throws Exception {	
	StructuredPushSender _sender = new StructuredPushSender(this, testEvent_);
	StructuredPushReceiver _receiver = new StructuredPushReceiver(this);
	
	_sender.connect(getSetup(), channel_, false);
	_receiver.connect(getSetup(), channel_, false);

	_receiver.start();
	_sender.start();

	_sender.join();
	_receiver.join();

	assertTrue("Error while sending", !_sender.error_);
	assertTrue("Should have received something", _receiver.isEventHandled());
    }

    public void testSendPushPull() throws Exception {
	StructuredPushSender _sender = new StructuredPushSender(this,testEvent_);
	StructuredPullReceiver _receiver = new StructuredPullReceiver(this);
	
	_sender.connect(getSetup(), channel_,false);
	_receiver.connect(getSetup(), channel_,false);

	_receiver.start();
	_sender.start();

	_sender.join();
	_receiver.join();

	assertTrue("Error while sending", !_sender.error_);
	assertTrue("Should have received something", _receiver.received_);
    }

    public void testSendPullPush() throws Exception {
	StructuredPullSender _sender = new StructuredPullSender(this,testEvent_);
	StructuredPushReceiver _receiver = new StructuredPushReceiver(this);
	_receiver.setTimeOut(2000);

	_sender.connect(getSetup(), channel_,false);
	_receiver.connect(getSetup(), channel_,false);

	_receiver.start();
	_sender.start();

	_sender.join();
	_receiver.join();

	assertTrue("Error while sending", !_sender.isError());
	assertTrue("Should have received something", _receiver.isEventHandled());
    }

    public void testSendPullPull() throws Exception {
	StructuredPullSender _sender = new StructuredPullSender(this,testEvent_);
	StructuredPullReceiver _receiver = new StructuredPullReceiver(this);
	    _sender.connect(getSetup(), channel_,false);

	_receiver.connect(getSetup(), channel_,false);

 	_receiver.start();
 	_sender.start();

 	_sender.join();
 	_receiver.join();

	boolean _senderError = ((TestClientOperations)_sender).isError();
	assertTrue("Error while sending", !_senderError);
	assertTrue("Should have received something", _receiver.isEventHandled());
    }

    public static Test suite() throws Exception {
	TestSuite _suite;

	_suite = new TestSuite("Test of Structured EventChannel");

	NotificationTestCaseSetup _setup =
	    new NotificationTestCaseSetup(_suite);
	
	String[] methodNames = org.jacorb.test.common.TestUtils.getTestMethods(StructuredEventChannelTest.class);

	for (int x=0; x<methodNames.length; ++x) {
	    _suite.addTest(new StructuredEventChannelTest(methodNames[x], _setup));
	}


	return _setup;
    }

    public static void main(String[] args) throws Exception {
	LogConfiguration.getInstance().configure();

	junit.textui.TestRunner.run(suite());
    }
}

