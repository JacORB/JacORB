package org.jacorb.notification;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.jacorb.notification.test.Address;
import org.jacorb.notification.test.NamedValue;
import org.jacorb.notification.test.Person;
import org.jacorb.notification.test.PersonHelper;
import org.jacorb.notification.test.Profession;
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

/**
 * StructuredEventChannelTest.java
 *
 *
 * Created: Sun Nov 03 17:28:29 2002
 *
 * @author <a href="mailto:bendt@inf.fu-berlin.de">Alphonse Bendt</a>
 * @version $Id$
 */

public class StructuredEventChannelTest extends TestCase {

    ORB orb_;
    POA poa_;
    Logger logger_ = Logger.getLogger("TEST.StructuredEventChannelTest");

    EventChannel channel_;
    EventChannelFactory channelFactory_;
    FilterFactory filterFactory_;
    StructuredEvent testEvent_;

    public StructuredEventChannelTest(String name) {
	super(name);
    }

    public void tearDown() throws Exception {
	channel_.destroy();
    }

    public void setUp() throws Exception {
	orb_ = ORB.init(new String[0], null);
	poa_ = POAHelper.narrow(orb_.resolve_initial_references("RootPOA"));

	ApplicationContext _appContext = new ApplicationContext(orb_, poa_);

	FilterFactoryImpl _filterFactoryServant =
	    new FilterFactoryImpl(_appContext);
	filterFactory_ = _filterFactoryServant._this(orb_);

	EventChannelFactoryImpl _factoryServant = 
	    new EventChannelFactoryImpl(_appContext);
	
	channelFactory_ = 
	    _factoryServant._this(orb_);

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

	Any _personAny = orb_.create_any();

	// prepare filterable body data
	Person _p = TestUtils.getTestPerson();
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
	
	testEvent_.remainder_of_body = orb_.create_any();
    }

    public void testDestroyChannelDisconnectsClients() throws Exception {	
	Property[] _p = new Property[0];
	IntHolder _channelId = new IntHolder();

	EventChannel _channel = channelFactory_.create_channel(_p, _p, _channelId);

	StructuredPushSender _pushSender = new StructuredPushSender(testEvent_);
	StructuredPullSender _pullSender = new StructuredPullSender(testEvent_);
	StructuredPushReceiver _pushReceiver = new StructuredPushReceiver();
	StructuredPullReceiver _pullReceiver = new StructuredPullReceiver();

	_pushSender.connect(orb_, poa_, _channel);
	_pullSender.connect(orb_, poa_, _channel);
	_pushReceiver.connect(orb_, poa_, _channel);
	_pullReceiver.connect(orb_, poa_, _channel);

	assertTrue(_pushSender.isConnected());
	assertTrue(_pullSender.isConnected());
	assertTrue(_pushReceiver.isConnected());
	assertTrue(_pullReceiver.isConnected());

	_channel.destroy();

	try {
	    Thread.sleep(1000);
	} catch (InterruptedException ie) {}

	assertTrue(!_pushSender.isConnected());
	assertTrue(!_pullSender.isConnected());
	assertTrue(!_pushReceiver.isConnected());
	assertTrue(!_pullReceiver.isConnected());
    }

    public void testSendPushPush() throws Exception {
	System.out.println("testSendPushPush");
	
	StructuredPushSender _sender = new StructuredPushSender(testEvent_);
	StructuredPushReceiver _receiver = new StructuredPushReceiver();
	
	_sender.connect(orb_, poa_, channel_);
	_receiver.connect(orb_, poa_, channel_);

	_receiver.start();
	_sender.start();

	_sender.join();
	_receiver.join();

	assertTrue("Error while sending", !_sender.error_);
	assertTrue("Should have received something", _receiver.isEventHandled());
    }

    public void testSendPushPull() throws Exception {
	StructuredPushSender _sender = new StructuredPushSender(testEvent_);
	StructuredPullReceiver _receiver = new StructuredPullReceiver();
	
	_sender.connect(orb_, poa_, channel_);
	_receiver.connect(orb_, poa_, channel_);

	_receiver.start();
	_sender.start();

	_sender.join();
	_receiver.join();

	assertTrue("Error while sending", !_sender.error_);
	assertTrue("Should have received something", _receiver.received_);
    }

    public void testSendPullPush() throws Exception {
	StructuredPullSender _sender = new StructuredPullSender(testEvent_);
	StructuredPushReceiver _receiver = new StructuredPushReceiver();
	
	_sender.connect(orb_, poa_, channel_);
	_receiver.connect(orb_, poa_, channel_);

	_receiver.start();
	_sender.start();

	_sender.join();
	_receiver.join();

	assertTrue("Error while sending", !_sender.isError());
	assertTrue("Should have received something", _receiver.isEventHandled());
    }

    public void testSendPullPull() throws Exception {
	StructuredPullSender _sender = new StructuredPullSender(testEvent_);
	StructuredPullReceiver _receiver = new StructuredPullReceiver();
	    _sender.connect(orb_, poa_, channel_);

	_receiver.connect(orb_, poa_, channel_);

 	_receiver.start();
 	_sender.start();

 	_sender.join();
 	_receiver.join();

	boolean _senderError = ((TestClientOperations)_sender).isError();
	assertTrue("Error while sending", !_senderError);
	assertTrue("Should have received something", _receiver.isEventHandled());
    }

    public static Test suite() {
	TestSuite suite;

	suite = new TestSuite();
	suite = new TestSuite(StructuredEventChannelTest.class);

	suite.addTest(new StructuredEventChannelTest("testSendPushPush"));
	
	return suite;
    }

    static {
	BasicConfigurator.configure();
    }

    public static void main(String[] args) {
	junit.textui.TestRunner.run(suite());
    }

}// StructuredEventChannelTest

