package org.jacorb.notification;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.omg.CORBA.IntHolder;
import org.omg.CORBA.ORB;
import org.omg.CosNotification.Property;
import org.omg.CosNotification.StructuredEvent;
import org.omg.CosNotifyChannelAdmin.EventChannel;
import org.omg.CosNotifyChannelAdmin.EventChannelFactory;
import org.omg.CosNotifyFilter.FilterFactory;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;

/*
 *        JacORB - a free Java ORB
 */

/**
 *
 *
 * @author <a href="mailto:bendt@inf.fu-berlin.de">Alphonse Bendt</a>
 * @version $Id$
 */

public class SequenceEventChannelTest extends TestCase {

    ORB orb_;
    POA poa_;
    Logger logger_ = Logger.getLogger("TEST.SequenceEventChannelTest");

    EventChannel channel_;
    EventChannelFactory channelFactory_;
    FilterFactory filterFactory_;
    StructuredEvent[] testEvent_;

    public SequenceEventChannelTest(String name) {
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
	testEvent_ = new StructuredEvent[] {TestUtils.getStructuredEvent(orb_)};
    }

    public void testDestroyChannelDisconnectsClients() throws Exception {
	Property[] _p = new Property[0];
	IntHolder _channelId = new IntHolder();

	EventChannel _channel = channelFactory_.create_channel(_p, _p, _channelId);

	SequencePushSender _pushSender = new SequencePushSender(testEvent_);
	SequencePullSender _pullSender = new SequencePullSender(testEvent_);
	SequencePushReceiver _pushReceiver = new SequencePushReceiver();
	SequencePullReceiver _pullReceiver = new SequencePullReceiver();

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
	SequencePushSender _sender = new SequencePushSender(testEvent_);
	SequencePushReceiver _receiver = new SequencePushReceiver();
	
	_sender.connect(orb_, poa_, channel_);
	_receiver.connect(orb_, poa_, channel_);

	_receiver.start();
	_sender.start();

	_sender.join();
	_receiver.join();

	assertTrue("Error while sending", !_sender.error_);
	assertTrue("Should have received something", _receiver.received_);
    }

    public void testSendPushPull() throws Exception {
	SequencePushSender _sender = new SequencePushSender(testEvent_);
	SequencePullReceiver _receiver = new SequencePullReceiver();
	
	_sender.connect(orb_, poa_, channel_);
	_receiver.connect(orb_, poa_, channel_);

	_receiver.start();
	_sender.start();

	_sender.join();
	_receiver.join();

	assertTrue("Error while sending", _sender.isEventHandled());
	assertTrue("Should have received something", _receiver.isEventHandled());
    }

    public void testSendPullPush() throws Exception {
	SequencePullSender _sender = new SequencePullSender(testEvent_);
	SequencePushReceiver _receiver = new SequencePushReceiver();
	
	_sender.connect(orb_, poa_, channel_);
	_receiver.connect(orb_, poa_, channel_);

	_receiver.start();
	_sender.start();

	_sender.join();
	_receiver.join();

	assertTrue("Error while sending", !_sender.isError());
	assertTrue("Should have received something", _receiver.received_);
    }

    public void testSendPullPull() throws Exception {
	SequencePullSender _sender = new SequencePullSender(testEvent_);
	SequencePullReceiver _receiver = new SequencePullReceiver();
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
	suite = new TestSuite(SequenceEventChannelTest.class);
	
	suite.addTest(new SequenceEventChannelTest("testDestroyChannelDisconnectsClients"));
	
	return suite;
    }

    static {
	BasicConfigurator.configure();
    }

    public static void main(String[] args) {
	junit.textui.TestRunner.run(suite());
    }

}// SequenceEventChannelTest

