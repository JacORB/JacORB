package org.jacorb.notification;

/*
 *        JacORB - a free Java ORB
 *
 */

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.omg.CORBA.Any;
import org.omg.CORBA.IntHolder;
import org.omg.CORBA.ORB;
import org.omg.CosNotification.EventType;
import org.omg.CosNotification.Property;
import org.omg.CosNotifyChannelAdmin.ClientType;
import org.omg.CosNotifyChannelAdmin.ConsumerAdmin;
import org.omg.CosNotifyChannelAdmin.EventChannel;
import org.omg.CosNotifyChannelAdmin.EventChannelFactory;
import org.omg.CosNotifyChannelAdmin.EventChannelFactoryHelper;
import org.omg.CosNotifyChannelAdmin.ProxyPullConsumer;
import org.omg.CosNotifyChannelAdmin.ProxyPullConsumerHelper;
import org.omg.CosNotifyChannelAdmin.ProxyPullSupplier;
import org.omg.CosNotifyChannelAdmin.ProxyPullSupplierHelper;
import org.omg.CosNotifyChannelAdmin.ProxyPushConsumer;
import org.omg.CosNotifyChannelAdmin.ProxyPushConsumerHelper;
import org.omg.CosNotifyChannelAdmin.ProxyPushSupplier;
import org.omg.CosNotifyChannelAdmin.ProxyPushSupplierHelper;
import org.omg.CosNotifyChannelAdmin.SupplierAdmin;
import org.omg.CosNotifyFilter.ConstraintExp;
import org.omg.CosNotifyFilter.ConstraintInfo;
import org.omg.CosNotifyFilter.Filter;
import org.omg.CosNotifyFilter.FilterFactory;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;
import org.apache.log4j.Level;
import junit.extensions.RepeatedTest;

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
	logger_.debug("setup");
	orb_ = ORB.init(new String[0], null);
	poa_ = POAHelper.narrow(orb_.resolve_initial_references("RootPOA"));

	ApplicationContext _appContext= new ApplicationContext(orb_, poa_);

	FilterFactoryImpl _filterFactoryServant =
	    new FilterFactoryImpl(_appContext);
	filterFactory_ = _filterFactoryServant._this(orb_);

	EventChannelFactoryImpl _factoryServant = 
	    new EventChannelFactoryImpl(_appContext);

	factory_ = _factoryServant._this(orb_);
	//factory_ = EventChannelFactoryHelper.narrow(orb_.resolve_initial_references("NotificationService"));

	// prepare test data
	testPerson_ = TestUtils.getTestPersonAny(orb_);

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

	poa_.the_POAManager().activate();
    }

    public void tearDown() {
	channel_.destroy();
    }

    public NotificationChannelTest(String name) {
	super(name);
    }

    public void testSendEventPushPull() throws Exception {
	AnyPullReceiver _receiver = new AnyPullReceiver();
	_receiver.connect(orb_, poa_, channel_);
	AnyPushSender _sender = new AnyPushSender(testPerson_);
	_sender.connect(orb_, poa_, channel_);

	Thread _receiverThread = new Thread(_receiver);
	_receiverThread.start();
	_sender.run();

	_receiverThread.join();
	assertTrue(!_receiver.isError());
	assertTrue(_receiver.isEventHandled());
    }

    public void testSendEventPushPush() throws Exception {
	logger_.debug("testSendEventPushPush");
	// start a receiver thread
 	AnyPushReceiver _receiver = new AnyPushReceiver();
	_receiver.connect(orb_, poa_, channel_);

	logger_.debug("Connected");

 	Thread _receiverThread = new Thread(_receiver);

	logger_.debug("Receiver started");

	// start a sender
	AnyPushSender _sender = new AnyPushSender(testPerson_);
	_sender.connect(orb_, poa_, channel_);

	_receiverThread.start();

	_sender.run();
	logger_.debug("Sender started");	

	_receiverThread.join();

	assertTrue(_receiver.isEventHandled());
    }


    public void testSendEventPullPush() throws Exception {
	AnyPullSender _sender = new AnyPullSender(testPerson_);
	_sender.connect(orb_, poa_, channel_);

	AnyPushReceiver _receiver = new AnyPushReceiver();
	_receiver.connect(orb_, poa_, channel_);

	Thread _receiverThread = new Thread(_receiver);
	_receiverThread.start();

	_sender.run();

	logger_.info("Sent Event");

	_receiverThread.join();

	assertTrue(_sender.isEventHandled());
	assertTrue(_receiver.isEventHandled());
    }

    public void testSendEventPullPull() throws Exception {
	AnyPullSender _sender = new AnyPullSender(testPerson_);
	_sender.connect(orb_, poa_, channel_);

	AnyPullReceiver _receiver = new AnyPullReceiver();
	_receiver.connect(orb_, poa_, channel_);

	Thread _receiverThread = new Thread(_receiver);
	_receiverThread.start();

	_sender.run();

	_receiverThread.join();

	assertTrue(_sender.isEventHandled());
	assertTrue(_receiver.isEventHandled());
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

	AnyPullReceiver _anyPullReceiver = new AnyPullReceiver();
	_anyPullReceiver.connect(orb_, poa_, _channel);

	AnyPushReceiver _anyPushReceiver = new AnyPushReceiver();
	_anyPushReceiver.connect(orb_, poa_, _channel);

	AnyPullSender _anyPullSender = new AnyPullSender(testPerson_);
	AnyPushSender _anyPushSender = new AnyPushSender(testPerson_);

	_channel.destroy();

	assertTrue(!_anyPullReceiver.isConnected());
	assertTrue(!_anyPushReceiver.isConnected());
	assertTrue(!_anyPullSender.isConnected());
	assertTrue(!_anyPushSender.isConnected());
    }

    /**
     * Test if all EventChannel Clients are disconnected when the
     * Channel is Destroyed
     */
    public void testDestroyAdminDisconnectsClients() throws Exception {
	IntHolder _id = new IntHolder();

	EventChannel _channel = factory_.create_channel(new Property[0], new Property[0], _id);

	AnyPullReceiver _anyPullReceiver = new AnyPullReceiver();
	_anyPullReceiver.connect(orb_, poa_, _channel);
	AnyPushReceiver _anyPushReceiver = new AnyPushReceiver();
	_anyPushReceiver.connect(orb_, poa_, _channel);
	AnyPullSender _anyPullSender = new AnyPullSender(testPerson_);
	_anyPullSender.connect(orb_, poa_, _channel);

	AnyPushSender _anyPushSender = new AnyPushSender(testPerson_);
	_anyPushSender.connect(orb_, poa_, _channel);

	assertTrue(_anyPullReceiver.isConnected());
	assertTrue(_anyPushReceiver.isConnected());
	assertTrue(_anyPullSender.isConnected());
	assertTrue(_anyPushSender.isConnected());

	_channel.default_consumer_admin().destroy();
	_channel.default_supplier_admin().destroy();

	assertTrue(!_anyPullReceiver.isConnected());
	assertTrue(!_anyPushReceiver.isConnected());
	assertTrue(!_anyPullSender.isConnected());
	assertTrue(!_anyPushSender.isConnected());

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

	_channel.destroy();
    }

    public static Test suite() {
	TestSuite _suite;

	_suite = new TestSuite();


	_suite = new TestSuite(NotificationChannelTest.class);

	return _suite;
    }

    public static void main(String[] args) throws Exception {
	BasicConfigurator.configure();
	Logger.getRootLogger().setLevel(Level.OFF);
	Logger.getRootLogger().getLogger("TEST").setLevel(Level.OFF);

	junit.textui.TestRunner.run(suite());
    }
    
}// EventChannelTest
