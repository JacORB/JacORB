package org.jacorb.notification;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.omg.CosNotifyChannelAdmin.EventChannel;
import org.omg.CORBA.ORB;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;
import org.omg.CORBA.IntHolder;
import org.omg.CosNotification.Property;
import org.omg.CosEventChannelAdmin.EventChannelHelper;
import org.omg.CosEventChannelAdmin.SupplierAdmin;
import org.omg.CosEventChannelAdmin.ConsumerAdmin;
import org.omg.CosEventComm.PushConsumer;
import org.omg.CosEventComm.PushSupplier;
import org.omg.CosEventChannelAdmin.ProxyPushConsumer;
import org.omg.CosEventChannelAdmin.ProxyPushSupplier;
import org.omg.CORBA.Any;
import org.jacorb.notification.test.Address;
import org.jacorb.notification.test.AddressHelper;
import org.apache.log4j.BasicConfigurator;
import org.omg.CosEventChannelAdmin.ProxyPullSupplier;
import org.omg.CosEventChannelAdmin.ProxyPullConsumer;
import org.omg.CosNotifyChannelAdmin.EventChannelFactory;

/**
 *  Unit Test for class EventChannel.
 *  Test Backward compability. Access Notification Channel via the
 *  CosEvent Interfaces.
 *
 * Created: Fri Nov 22 18:10:06 2002
 *
 * @author <a href="mailto:bendt@inf.fu-berlin.de">Alphonse Bendt</a>
 * @version
 */

public class EventChannelTest extends TestCase {
    
    EventChannel channel_;
    EventChannelFactory factory_;
    ORB orb_;
    POA poa_;
    Any testData_;

    public void tearDown() throws Exception {
	channel_.destroy();
    }

    public void setUp() throws Exception {
	orb_ = ORB.init(new String[0], null);
	poa_ = POAHelper.narrow(orb_.resolve_initial_references("RootPOA"));

	ApplicationContext _context = new ApplicationContext(orb_, poa_);

	EventChannelFactoryImpl _factoryServant = new EventChannelFactoryImpl(_context);
	factory_ = _factoryServant._this(orb_);
	IntHolder _channelId = new IntHolder();
	channel_ = _factoryServant.create_channel(new Property[0], new Property[0], _channelId);

	testData_ = TestUtils.getTestPersonAny(orb_);
    }

    public void testPushPush() throws Exception {
	CosEventPushReceiver _receiver = new CosEventPushReceiver();
	_receiver.connect(orb_, poa_, channel_);

	CosEventPushSender _sender = new CosEventPushSender(testData_);
	_sender.connect(orb_, poa_, channel_);

	Thread _r = new Thread(_receiver);
	_r.start();

	Thread _s = new Thread(_sender);
	_s.start();
	_s.join();
	assertTrue(_sender.isEventHandled());

	_r.join();
	
	assertTrue(_receiver.isEventHandled());
    }

    public void testPushPull() throws Exception {
	CosEventPullReceiver _receiver = new CosEventPullReceiver();
	_receiver.connect(orb_, poa_, channel_);
	Thread _r = new Thread(_receiver);

	CosEventPushSender _sender = new CosEventPushSender(testData_);
	_sender.connect(orb_, poa_, channel_);
	Thread _s = new Thread(_sender);

	_r.start();

	_s.start();
	_s.join();
	assertTrue(_sender.isEventHandled());

	_r.join();

	assertTrue(_receiver.isEventHandled());
    }

    public void testPullPush() throws Exception {
	CosEventPushReceiver _receiver = new CosEventPushReceiver();
	_receiver.connect(orb_, poa_, channel_);

	CosEventPullSender _sender = new CosEventPullSender(testData_);
	_sender.connect(orb_, poa_, channel_);

	Thread _r = new Thread(_receiver);
	_r.start();
	Thread _s = new Thread(_sender);
	_s.start();

	_s.join();
	assertTrue(_sender.isEventHandled());

	_r.join();	
	assertTrue(_receiver.isEventHandled());
    }

    public void testPullPull() throws Exception {
	CosEventPullReceiver _receiver = new CosEventPullReceiver();
	_receiver.connect(orb_, poa_, channel_);
	Thread _r = new Thread(_receiver);

	CosEventPullSender _sender = new CosEventPullSender(testData_);
	_sender.connect(orb_, poa_, channel_);

	_r.start();

	_r.join();

	assertTrue(_receiver.isEventHandled());
    }

    public void testDestroyChannelDisconnectsClients() throws Exception {
	IntHolder _channelId = new IntHolder();
	EventChannel _channel = factory_.create_channel(new Property[0], new Property[0], _channelId);

	TestClientOperations[] _testClients = new TestClientOperations[] {
	    new CosEventPullSender(testData_),
	    new CosEventPushSender(testData_),
	    new CosEventPushReceiver(),
	    new CosEventPullReceiver()};

	for (int x=0; x<_testClients.length; ++x) {
	    _testClients[x].connect(orb_, poa_, _channel);
	    assertTrue(_testClients[x].isConnected());
	}

	_channel.destroy();

	for (int x=0; x<_testClients.length; ++x) {
	    assertTrue(!_testClients[x].isConnected());
	}
    }

    /** 
     * Creates a new <code>EventChannelTest</code> instance.
     *
     * @param name test name
     */
    public EventChannelTest (String name){
	super(name);
    }

    /**
     * @return a <code>TestSuite</code>
     */
    public static TestSuite suite(){
	TestSuite suite;

	suite = new TestSuite(EventChannelTest.class);
 	//suite = new TestSuite();
 	//suite.addTest(new EventChannelTest("testPullPush"));
 	suite.addTest(new EventChannelTest("testPullPush"));
	
	return suite;
    }

    static {
	BasicConfigurator.configure();
    }

    /** 
     * Entry point 
     */ 
    public static void main(String[] args) {
	junit.textui.TestRunner.run(suite());
    }
}// EventChannelTest
