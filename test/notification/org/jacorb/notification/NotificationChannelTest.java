package org.jacorb.notification;

/*
 *        JacORB - a free Java ORB
 *
 */

import junit.framework.TestSuite;
import junit.framework.Test;
import junit.framework.TestCase;

import org.omg.CORBA.ORB;
import org.omg.CosNotifyChannelAdmin.EventChannelFactory;
import org.omg.CosNotifyChannelAdmin.EventChannelFactoryHelper;
import org.omg.CORBA.IntHolder;
import org.omg.CosNotification.Property;
import org.omg.CosNotifyChannelAdmin.EventChannel;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;
import org.omg.CosNotifyChannelAdmin.InterFilterGroupOperator;
import org.omg.CosNotifyChannelAdmin.ConsumerAdmin;
import org.omg.CosNotifyChannelAdmin.SupplierAdmin;
import org.omg.CosNotifyChannelAdmin.ClientType;
import org.omg.CosEventComm.PushConsumer;
import org.omg.CosEventComm.PushSupplier;
import org.omg.CosEventComm.PushConsumerOperations;
import org.omg.CORBA.Any;
import org.omg.CosEventComm.Disconnected;
import org.omg.CosNotifyChannelAdmin.ProxySupplier;
import org.omg.CosNotifyChannelAdmin.ProxyPushSupplier;
import org.omg.CosNotifyChannelAdmin.ProxyPushSupplierHelper;
import org.omg.CosEventComm.PushConsumerHelper;
import org.omg.CosEventComm.PushConsumerPOA;
import org.jacorb.notification.test.Person;
import org.jacorb.notification.test.Address;
import org.jacorb.notification.test.NamedValue;
import org.jacorb.notification.test.Profession;
import org.jacorb.notification.test.PersonHelper;
import org.omg.CosEventComm.PushSupplierPOA;
import org.omg.CosNotifyChannelAdmin.ProxyPushConsumer;
import org.omg.CosNotifyChannelAdmin.ProxyPushConsumerHelper;
import org.omg.CosEventChannelAdmin.AlreadyConnected;
import org.omg.CosNotifyFilter.FilterFactory;
import org.omg.CosNotifyFilter.Filter;
import org.omg.CosNotifyFilter.ConstraintExp;
import org.omg.CosNotification.EventType;
import org.omg.CosNotifyFilter.ConstraintInfo;
import org.apache.log4j.BasicConfigurator;
import org.omg.CosNotifyChannelAdmin.ProxyConsumerHelper;
import org.omg.CosNotifyChannelAdmin.ProxyConsumer;
import org.omg.CosNotifyChannelAdmin.ProxySupplierHelper;
import org.omg.CosNotifyChannelAdmin.StructuredProxyPushSupplier;
import org.omg.CosNotifyChannelAdmin.StructuredProxyPushSupplierHelper;
import org.omg.CosNotifyComm.StructuredPushConsumerPOA;
import org.omg.CosNotification.StructuredEvent;
import org.omg.CosNotifyComm.StructuredPushConsumerPOATie;
import org.omg.CosNotifyChannelAdmin.ProxyPullSupplier;
import org.omg.CosNotifyChannelAdmin.ProxyPullSupplierHelper;
import org.omg.CosNotifyChannelAdmin.ProxyPullConsumer;
import org.omg.CosNotifyChannelAdmin.ProxyPullConsumerHelper;
import org.apache.log4j.Logger;

/**
 * EventChannelTest.java
 *
 *
 * Created: Mon Oct 07 17:34:44 2002
 *
 * @author <a href="mailto:bendt@inf.fu-berlin.de">Alphonse Bendt</a>
 * @version $Id$
 */

public class NotificationChannelTest extends TestCase {
    ORB orb_;
    POA poa_;

    EventChannelFactory factory_;
    FilterFactory filterFactory_;
    Any testPerson_;
    EventChannel channel_;
    IntHolder channelId_;
    SupplierAdmin supplierAdmin_;
    ConsumerAdmin consumerAdmin_;
    Filter trueFilter_;

    Logger logger_ = Logger.getLogger("NotificationChannelTest");

    /**
     * setup EventChannelFactory, FilterFactory and Any with Testdata
     */
    public void setUp() throws Exception {
	orb_ = ORB.init(new String[0], null);
	poa_ = POAHelper.narrow(orb_.resolve_initial_references("RootPOA"));

	FilterFactoryImpl _filterFactoryServant =
	    new FilterFactoryImpl(orb_, poa_);
	filterFactory_ = _filterFactoryServant._this(orb_);

	ApplicationContext _context = new ApplicationContext();
	_context.setOrb(orb_);
	_context.setPoa(poa_);

	EventChannelFactoryImpl _factoryServant = 
	    new EventChannelFactoryImpl(_context);

	factory_ = _factoryServant._this(orb_);

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

	testPerson_ = orb_.create_any();
	PersonHelper.insert(testPerson_, _p);

	// setup a channel
	channelId_ = new IntHolder();
	channel_ = factory_.create_channel(new Property[0], new Property[0], channelId_);


	supplierAdmin_ = channel_.default_supplier_admin();
	consumerAdmin_ = channel_.default_consumer_admin();

	trueFilter_ = filterFactory_.create_filter("EXTENDED_TCL");
	ConstraintExp[] _constraintExp = new ConstraintExp[1];
	EventType[] _eventType = new EventType[1];
	_eventType[0] = new EventType("*", "*");
	String _expression = "TRUE";
	_constraintExp[0] = new ConstraintExp(_eventType, _expression);
	ConstraintInfo[] _info = trueFilter_.add_constraints(_constraintExp);
    }

    public void tearDown() {
	channel_.destroy();
    }

    public NotificationChannelTest(String name) {
	super(name);
    }

    public void testSendEventPushPull() throws Exception {
	IntHolder _proxyId = new IntHolder();
	ProxyPushConsumer _proxyPushConsumer =  
	    ProxyPushConsumerHelper.narrow(
				       supplierAdmin_.obtain_notification_push_consumer(ClientType.ANY_EVENT, _proxyId));
	assertNotNull(_proxyPushConsumer);

	ProxyPullSupplier _proxyPullSupplier = 
	    ProxyPullSupplierHelper.narrow(consumerAdmin_.obtain_notification_pull_supplier(ClientType.ANY_EVENT, _proxyId));
	assertNotNull(_proxyPullSupplier);
	
	AnyPullReceiver _receiver = new AnyPullReceiver(orb_, poa_, _proxyPullSupplier);
	AnyPushSender _sender = new AnyPushSender(orb_, poa_, _proxyPushConsumer);

	Thread _receiverThread = new Thread(_receiver);
	_receiverThread.start();
	_sender.send(testPerson_);

	_receiverThread.join();
	assertTrue(!_receiver.error());
	assertTrue(_receiver.received());

	supplierAdmin_.add_filter(trueFilter_);
	consumerAdmin_.add_filter(trueFilter_);
	_proxyPullSupplier.add_filter(trueFilter_);
	_proxyPushConsumer.add_filter(trueFilter_);

	_receiver.reset();
	_receiverThread = new Thread(_receiver);
	_receiverThread.start();
	_sender.send(testPerson_);

	_receiverThread.join();
	assertTrue(!_receiver.error());
	assertTrue(_receiver.received());
    }

    public void testSendEventPushPush() throws Exception {
	IntHolder _proxyId = new IntHolder();

	ProxyConsumer _proxyConsumer =
	    ProxyConsumerHelper.narrow(supplierAdmin_.obtain_notification_push_consumer(ClientType.ANY_EVENT, _proxyId));
	assertNotNull(_proxyConsumer);

 	ProxySupplier _proxySupplier  = 
	    ProxySupplierHelper.narrow(
				       consumerAdmin_.obtain_notification_push_supplier(ClientType.ANY_EVENT, _proxyId));
 	assertNotNull(_proxySupplier);

	// start a receiver thread
 	AnyPushReceiver _receiver = new AnyPushReceiver(orb_, poa_);
	ProxyPushSupplier _proxyPushSupplier = ProxyPushSupplierHelper.narrow(_proxySupplier);
 	_proxyPushSupplier.connect_any_push_consumer(_receiver._this(orb_));
 	Thread _receiverThread = new Thread(_receiver);

	// start a sender thread
	ProxyPushConsumer _proxyPushConsumer = ProxyPushConsumerHelper.narrow(_proxyConsumer);
	AnyPushSender _sender = new AnyPushSender(orb_, poa_, _proxyPushConsumer);

	// we should not have received anything
	assertTrue(!_receiver.received());

	supplierAdmin_.add_filter(trueFilter_);
	consumerAdmin_.add_filter(trueFilter_);
	_proxyPushConsumer.add_filter(trueFilter_);
	_proxyPushSupplier.add_filter(trueFilter_);

	_receiverThread = new Thread(_receiver);
	_receiverThread.start();

	_sender.send(testPerson_);
	_receiverThread.join();

	assertTrue(_receiver.received());
    }

    public void testSendEventPullPush() throws Exception {
	IntHolder _proxyId = new IntHolder();

	ProxyPullConsumer _ppc = 
	    ProxyPullConsumerHelper.narrow(supplierAdmin_.obtain_notification_pull_consumer(ClientType.ANY_EVENT, _proxyId));
	ProxyPushSupplier _pps = 
	    ProxyPushSupplierHelper.narrow(consumerAdmin_.obtain_notification_push_supplier(ClientType.ANY_EVENT, _proxyId));
	assertNotNull(_ppc);
	assertNotNull(_pps);

	supplierAdmin_.add_filter(trueFilter_);
	consumerAdmin_.add_filter(trueFilter_);

	_ppc.add_filter(trueFilter_);
	_pps.add_filter(trueFilter_);

	AnyPullSender _sender = new AnyPullSender(orb_, poa_, _ppc);
	AnyPushReceiver _receiver = new AnyPushReceiver(orb_, poa_, _pps);

	Thread _receiverThread = new Thread(_receiver);
	_receiverThread.start();

	_sender.send(testPerson_);

	logger_.info("Sent Event");

	_receiverThread.join();

	assertTrue(_sender.sent());
	assertTrue(_receiver.received());
    }

    public void testSendEventPullPull() throws Exception {
	IntHolder _proxyId = new IntHolder();

	ProxyPullConsumer _ppc = 
	    ProxyPullConsumerHelper.narrow(supplierAdmin_.obtain_notification_pull_consumer(ClientType.ANY_EVENT, _proxyId));
	ProxyPullSupplier _pps = 
	    ProxyPullSupplierHelper.narrow(consumerAdmin_.obtain_notification_pull_supplier(ClientType.ANY_EVENT, _proxyId));
	assertNotNull(_ppc);
	assertNotNull(_pps);

	supplierAdmin_.add_filter(trueFilter_);
	consumerAdmin_.add_filter(trueFilter_);
	_ppc.add_filter(trueFilter_);
	_pps.add_filter(trueFilter_);

	AnyPullSender _sender = new AnyPullSender(orb_, poa_, _ppc);
	AnyPullReceiver _receiver = new AnyPullReceiver(orb_, poa_, _pps);

	Thread _receiverThread = new Thread(_receiver);
	_receiverThread.start();

	_sender.send(testPerson_);

	_sender.shutdown();

	_receiverThread.join();

	assertTrue(_sender.sent());
	assertTrue(_receiver.received());
    }

    /**
     * Test if all EventChannel Clients are disconnected when the
     * Channel is Destroyed
     */
    public void testDestroyChannelDisconnectsClients() throws Exception {
	IntHolder _id = new IntHolder();

	EventChannel _channel = factory_.create_channel(new Property[0], new Property[0], _id);

	ConsumerAdmin _consumerAdmin = _channel.default_consumer_admin();
	SupplierAdmin _supplierAdmin = _channel.default_supplier_admin();

	ProxyPullSupplier _pullSupplier = 
	    ProxyPullSupplierHelper.narrow(_consumerAdmin.obtain_notification_pull_supplier(ClientType.ANY_EVENT, _id));
	ProxyPushSupplier _pushSupplier = 
	    ProxyPushSupplierHelper.narrow(_consumerAdmin.obtain_notification_push_supplier(ClientType.ANY_EVENT, _id));
	ProxyPushConsumer _pushConsumer = 
	    ProxyPushConsumerHelper.narrow(_supplierAdmin.obtain_notification_push_consumer(ClientType.ANY_EVENT, _id));
	ProxyPullConsumer _pullConsumer = 
	    ProxyPullConsumerHelper.narrow(_supplierAdmin.obtain_notification_pull_consumer(ClientType.ANY_EVENT, _id));

	AnyPullReceiver _anyPullReceiver = new AnyPullReceiver(orb_, poa_, _pullSupplier);
	AnyPushReceiver _anyPushReceiver = new AnyPushReceiver(orb_, poa_, _pushSupplier);
	AnyPullSender _anyPullSender = new AnyPullSender(orb_, poa_, _pullConsumer);
	AnyPushSender _anyPushSender = new AnyPushSender(orb_, poa_, _pushConsumer);

	_channel.destroy();

	assertTrue(_anyPullReceiver.disconnected());
	assertTrue(_anyPushReceiver.disconnected());
	assertTrue(_anyPullSender.disconnected());
	assertTrue(_anyPushSender.disconnected());
    }

    /**
     * Test if all EventChannel Clients are disconnected when the
     * Channel is Destroyed
     */
    public void testDestroyAdminDisconnectsClients() throws Exception {
	IntHolder _id = new IntHolder();

	EventChannel _channel = factory_.create_channel(new Property[0], new Property[0], _id);

	ConsumerAdmin _consumerAdmin = _channel.new_for_consumers(InterFilterGroupOperator.AND_OP, _id);
	SupplierAdmin _supplierAdmin = _channel.new_for_suppliers(InterFilterGroupOperator.AND_OP, _id);

	ProxyPullSupplier _pullSupplier = 
	    ProxyPullSupplierHelper.narrow(_consumerAdmin.obtain_notification_pull_supplier(ClientType.ANY_EVENT, _id));
	ProxyPushSupplier _pushSupplier = 
	    ProxyPushSupplierHelper.narrow(_consumerAdmin.obtain_notification_push_supplier(ClientType.ANY_EVENT, _id));
	ProxyPushConsumer _pushConsumer = 
	    ProxyPushConsumerHelper.narrow(_supplierAdmin.obtain_notification_push_consumer(ClientType.ANY_EVENT, _id));
	ProxyPullConsumer _pullConsumer = 
	    ProxyPullConsumerHelper.narrow(_supplierAdmin.obtain_notification_pull_consumer(ClientType.ANY_EVENT, _id));

	AnyPullReceiver _anyPullReceiver = new AnyPullReceiver(orb_, poa_, _pullSupplier);
	AnyPushReceiver _anyPushReceiver = new AnyPushReceiver(orb_, poa_, _pushSupplier);
	AnyPullSender _anyPullSender = new AnyPullSender(orb_, poa_, _pullConsumer);
	AnyPushSender _anyPushSender = new AnyPushSender(orb_, poa_, _pushConsumer);

	_consumerAdmin.destroy();
	_supplierAdmin.destroy();

	assertTrue(_anyPullReceiver.disconnected());
	assertTrue(_anyPushReceiver.disconnected());
	assertTrue(_anyPullSender.disconnected());
	assertTrue(_anyPushSender.disconnected());

	_channel.destroy();
    }

    public void testCreateChannel() throws Exception {
	IntHolder _id = new IntHolder();

	EventChannel _channel = factory_.create_channel(new Property[0], 
							new Property[0], 
							_id);

	// test if channel id appears within channel list
	int[] _allFactories = factory_.get_all_channels();
	boolean _seen = false;
	for (int x=0; x<_allFactories.length; ++x) {
	    if (_allFactories[x] == _id.value) {
		_seen = true;
	    }
	}
	assertTrue(_seen);

	EventChannel _sameChannel = factory_.get_event_channel(_id.value);
	assertTrue(_channel._is_equivalent(_sameChannel));
	assertTrue(_channel.MyFactory()._is_equivalent(factory_));
    }

    public static Test suite() {
	TestSuite _suite;

	_suite = new TestSuite();
	_suite.addTest(new NotificationChannelTest("testDestroyAdminDisconnectsClients"));

	_suite = new TestSuite(NotificationChannelTest.class);

	return _suite;
    }

    static {
	BasicConfigurator.configure();
    }

    public static void main(String[] args) throws Exception {
	junit.textui.TestRunner.run(suite());
    }
    
}// EventChannelTest
