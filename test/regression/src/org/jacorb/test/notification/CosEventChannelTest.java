package org.jacorb.test.notification;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.apache.log.Hierarchy;
import org.apache.log.Logger;
import org.omg.CORBA.Any;
import org.omg.CORBA.IntHolder;
import org.omg.CosNotification.Property;
import org.omg.CosNotifyChannelAdmin.EventChannel;
import org.omg.CosNotifyChannelAdmin.EventChannelFactory;
import org.omg.CosNotifyChannelAdmin.EventChannelFactoryHelper;

/**
 *  Unit Test for class EventChannel.
 *  Test Backward compability. Access Notification Channel via the
 *  CosEvent Interfaces.
 *
 * @author Alphonse Bendt
 * @version $Id$
 */

public class CosEventChannelTest extends NotificationTestCase {

    Logger logger_ = 
	Hierarchy.getDefaultHierarchy().getLoggerFor(getClass().getName());

    EventChannel channel_;
    EventChannelFactory factory_;
    Any testData_;

    public void setUp() throws Exception {
	factory_ = getEventChannelFactory();

	IntHolder _channelId = new IntHolder();
	channel_ = factory_.create_channel(new Property[0], 
					   new Property[0], 
					   _channelId);

	testData_ = getTestUtils().getTestPersonAny();
    }

    public void tearDown() throws Exception {
	channel_.destroy();
    }

    public void testPushPush() throws Exception {
	CosEventPushReceiver _receiver = new CosEventPushReceiver(this);

	_receiver.connect(getSetup(), channel_, false);

	CosEventPushSender _sender = new CosEventPushSender(this, testData_);
	_sender.connect(getSetup(), channel_, false);

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
	_receiver.connect(getSetup(), channel_,false);
	Thread _r = new Thread(_receiver);

	CosEventPushSender _sender = new CosEventPushSender(this,testData_);
	_sender.connect(getSetup(), channel_,false);
	Thread _s = new Thread(_sender);

	_r.start();

	_s.start();
	_s.join();
	assertTrue(_sender.isEventHandled());

	_r.join();

	assertTrue(_receiver.isEventHandled());
    }

    public void testPullPush() throws Exception {
	CosEventPushReceiver _receiver = new CosEventPushReceiver(this);
	_receiver.connect(getSetup(), channel_,false);

	CosEventPullSender _sender = new CosEventPullSender(testData_);
	_sender.connect(getSetup(), channel_,false);

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
	_receiver.connect(getSetup(), channel_,false);
	Thread _r = new Thread(_receiver);

	CosEventPullSender _sender = new CosEventPullSender(testData_);
	_sender.connect(getSetup(), channel_,false);

	_r.start();

	_r.join();

	assertTrue(_receiver.isEventHandled());
    }

    public void testDestroyChannelDisconnectsClients() throws Exception {
	IntHolder _channelId = new IntHolder();
	EventChannel _channel = factory_.create_channel(new Property[0], new Property[0], _channelId);

	TestClientOperations[] _testClients = new TestClientOperations[] {
	    new CosEventPullSender(testData_),
	    new CosEventPushSender(this,testData_),
	    new CosEventPushReceiver(this),
	    new CosEventPullReceiver()};

	for (int x=0; x<_testClients.length; ++x) {
	    _testClients[x].connect(getSetup(), _channel,false);
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
    public CosEventChannelTest (String name, NotificationTestCaseSetup setup){
	super(name, setup);
    }

    /**
     * @return a <code>TestSuite</code>
     */
    public static Test suite() throws Exception {
	TestSuite _suite;

 	_suite = new TestSuite("Basic CosEvent EventChannel Tests");

	NotificationTestCaseSetup _setup =
	    new NotificationTestCaseSetup( _suite );

	String[] methodNames = org.jacorb.test.common.TestUtils.getTestMethods(CosEventChannelTest.class);

	for (int x=0; x<methodNames.length; ++x) {
	    _suite.addTest(new CosEventChannelTest(methodNames[x], _setup));
	}
	
	return _setup;
    }

    /** 
     * Entry point 
     */ 
    public static void main(String[] args) throws Exception {
	junit.textui.TestRunner.run(suite());
    }

}
