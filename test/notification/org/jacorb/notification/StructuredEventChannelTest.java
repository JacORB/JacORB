package org.jacorb.notification;

import junit.framework.TestCase;
import org.omg.CORBA.ORB;
import org.omg.PortableServer.POA;
import org.omg.CosNotifyChannelAdmin.EventChannelFactory;
import org.omg.CosNotifyFilter.FilterFactory;
import org.omg.PortableServer.POAHelper;
import org.omg.CosNotification.StructuredEvent;
import org.omg.CosNotification.EventType;
import org.omg.CosNotification.FixedEventHeader;
import org.omg.CosNotification.Property;
import org.omg.CosNotification.EventHeader;
import org.omg.CORBA.Any;
import org.jacorb.notification.test.Person;
import org.jacorb.notification.test.Address;
import org.jacorb.notification.test.NamedValue;
import org.jacorb.notification.test.Profession;
import org.jacorb.notification.test.PersonHelper;
import org.omg.CosNotifyComm.StructuredPushSupplierOperations;
import org.omg.CosNotifyComm.InvalidEventType;
import org.omg.CosNotifyChannelAdmin.EventChannel;
import org.omg.CosNotifyChannelAdmin.StructuredProxyPushConsumerHelper;
import org.omg.CORBA.IntHolder;
import org.omg.CosNotifyComm.StructuredPushSupplierPOATie;
import org.omg.CosNotifyChannelAdmin.SupplierAdmin;
import org.omg.CosNotifyChannelAdmin.ClientType;
import org.omg.CosNotifyChannelAdmin.AdminLimitExceeded;
import org.apache.log4j.Logger;
import org.omg.CosNotifyComm.PushConsumer;
import org.apache.log4j.BasicConfigurator;
import org.omg.CosNotifyChannelAdmin.StructuredProxyPushConsumer;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.omg.CosNotifyComm.StructuredPushSupplierHelper;
import org.omg.CosNotifyComm.StructuredPushConsumerPOA;
import org.omg.CosNotifyComm.StructuredPushConsumerOperations;
import org.omg.CosEventComm.Disconnected;
import org.omg.CosNotifyChannelAdmin.ConsumerAdmin;
import org.omg.CosNotifyComm.StructuredPushConsumerPOATie;
import org.omg.CosNotifyChannelAdmin.StructuredProxyPushSupplierHelper;
import org.omg.CosNotifyChannelAdmin.StructuredProxyPushSupplier;
import org.omg.CosNotifyComm.StructuredPushConsumerHelper;

/*
 *        JacORB - a free Java ORB
 */

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

    Logger logger_ = Logger.getLogger("StructuredEventChannelTest");

    EventChannel channel_;
    FilterFactory filterFactory_;
    StructuredEvent testEvent_;

    public StructuredEventChannelTest(String name) {
	super(name);
    }

    public void setUp() throws Exception {
	orb_ = ORB.init(new String[0], null);
	poa_ = POAHelper.narrow(orb_.resolve_initial_references("RootPOA"));

	ApplicationContext _appContext = new ApplicationContext();
	_appContext.setOrb(orb_);
	_appContext.setPoa(poa_);

	FilterFactoryImpl _filterFactoryServant =
	    new FilterFactoryImpl(orb_, poa_);
	filterFactory_ = _filterFactoryServant._this(orb_);

	EventChannelFactoryImpl _factoryServant = 
	    new EventChannelFactoryImpl(_appContext);
	
	EventChannelFactory _ecFactory = 
	    _factoryServant._this(orb_);

	Property[] qos = new Property[0];
	Property[] adm = new Property[0];
	IntHolder _channelId = new IntHolder();

	channel_ = _ecFactory.create_channel(qos, adm, _channelId);

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

	PersonHelper.insert(_personAny, _p);
	testEvent_.filterable_data[0] = new Property("person", _personAny);
	
	testEvent_.remainder_of_body = orb_.create_any();
    }

    public void testSendStructuredEvent() throws Exception {
	StructuredSender _sender = new StructuredSender(testEvent_);
	StructuredReceiver _receiver = new StructuredReceiver();
	
	_sender.connect(poa_, channel_);
	_receiver.connect(poa_, channel_);

	_receiver.start();
	_sender.start();

	_sender.join();
	_receiver.join();

	assertTrue("Error while sending", !_sender.error_);
	assertTrue("Should not receive anything", !_receiver.received_);
    }

    public static Test suite() {
	TestSuite suite;

	suite = new TestSuite();
	suite = new TestSuite(StructuredEventChannelTest.class);

	//suite.addTest(new TCLTest("..."));
	
	return suite;
    }

    static {
	BasicConfigurator.configure();
    }

    public static void main(String[] args) {
	junit.textui.TestRunner.run(suite());
    }

}// StructuredEventChannelTest

