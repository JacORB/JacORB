package org.jacorb.test.notification;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.omg.CORBA.Any;
import org.omg.CORBA.IntHolder;
import org.omg.CosNotification.Property;
import org.omg.CosNotifyChannelAdmin.ConsumerAdmin;
import org.omg.CosNotifyChannelAdmin.EventChannel;
import org.omg.CosNotifyChannelAdmin.EventChannelFactory;
import org.omg.CosNotifyChannelAdmin.SupplierAdmin;
import org.omg.CosNotifyFilter.FilterFactory;

import org.jacorb.test.common.TestUtils;
import org.apache.avalon.framework.logger.Logger;
import org.jacorb.util.Debug;

/**
 * EventChannelTest.java
 *
 * @author Alphonse Bendt
 * @version $Id$
 */

public class EventChannelTest extends NotificationTestCase {

    Logger logger_ = Debug.getNamedLogger(getClass().getName());

    EventChannelFactory factory_;
    Any testPerson_;
    EventChannel channel_;
    IntHolder channelId_;
    SupplierAdmin supplierAdmin_;
    ConsumerAdmin consumerAdmin_;

    /**
     * setup EventChannelFactory, FilterFactory and Any with Testdata
     */
    public void setUp() throws Exception {
        factory_ = getEventChannelFactory();

        testPerson_ = getTestUtils().getTestPersonAny();

        channelId_ = new IntHolder();

        channel_ = factory_.create_channel(new Property[0], new Property[0], channelId_);

        supplierAdmin_ = channel_.default_supplier_admin();
        consumerAdmin_ = channel_.default_consumer_admin();
    }

    public void tearDown() {
        super.tearDown();
        channel_.destroy();
    }

    public EventChannelTest(String name, NotificationTestCaseSetup setup) {
        super(name, setup);
    }

    public void testSendEventPushPull() throws Exception {
        AnyPullReceiver _receiver = new AnyPullReceiver(this);
        _receiver.connect(getSetup(), channel_,false);
        AnyPushSender _sender = new AnyPushSender(this, testPerson_);
        _sender.connect(getSetup(), channel_, false);

        Thread _receiverThread = new Thread(_receiver);
        _receiverThread.start();
        _sender.run();

        _receiverThread.join();
        assertTrue(!_receiver.isError());
        assertTrue(_receiver.isEventHandled());

        _receiver.shutdown();
        _sender.shutdown();
    }

    public void testSendEventPushPush() throws Exception {
        logger_.debug("testSendEventPushPush");
        // start a receiver thread
        AnyPushReceiver _receiver = new AnyPushReceiver(this);
        _receiver.connect(getSetup(), channel_, false);

        logger_.debug("Connected");

        Thread _receiverThread = new Thread(_receiver);

        logger_.debug("Receiver started");

        // start a sender
        AnyPushSender _sender = new AnyPushSender(this, testPerson_);

        _sender.connect(getSetup(),  channel_, false);

        _receiverThread.start();

        _sender.run();

        logger_.debug("Sender started");

        _receiverThread.join();

        assertTrue(_receiver.isEventHandled());

        _receiver.shutdown();
        _sender.shutdown();
    }

    public void testSendEventPullPush() throws Exception {
        AnyPullSender _sender = new AnyPullSender(this,testPerson_);
        _sender.connect(getSetup(),  channel_, false);

        AnyPushReceiver _receiver = new AnyPushReceiver(this);
        _receiver.connect(getSetup(), channel_, false);

        Thread _receiverThread = new Thread(_receiver);
        _receiverThread.start();

        _sender.run();

        logger_.info("Sent Event");

        _receiverThread.join();

        assertTrue(_sender.isEventHandled());
        assertTrue(_receiver.isEventHandled());

        _receiver.shutdown();
        _sender.shutdown();

    }

    public void testSendEventPullPull() throws Exception {
        AnyPullSender _sender = new AnyPullSender(this,testPerson_);
        _sender.connect(getSetup(),  channel_, false);

        AnyPullReceiver _receiver = new AnyPullReceiver(this);
        _receiver.connect(getSetup(), channel_, false);

        Thread _receiverThread = new Thread(_receiver);
        _receiverThread.start();

        _sender.run();

        _receiverThread.join();

        assertTrue(_sender.isEventHandled());
        assertTrue(_receiver.isEventHandled());

        _receiver.shutdown();
        _sender.shutdown();

    }

    /**
     * Test if all EventChannel Clients are disconnected when the
     * Channel is Destroyed
     */
    public void testDestroyChannelDisconnectsClients() throws Exception {
        IntHolder _id = new IntHolder();

        EventChannel _channel = factory_.create_channel(new Property[0], new Property[0], _id);

        AnyPullReceiver _anyPullReceiver = new AnyPullReceiver(this);
        _anyPullReceiver.connect(getSetup(), _channel, false);

        AnyPushReceiver _anyPushReceiver = new AnyPushReceiver(this);
        _anyPushReceiver.connect(getSetup(), _channel, false);

        AnyPullSender _anyPullSender = new AnyPullSender(this,testPerson_);
        _anyPullSender.connect(getSetup(), _channel, false);

        AnyPushSender _anyPushSender = new AnyPushSender(this,testPerson_);
        _anyPushSender.connect(getSetup(), _channel, false);

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

        AnyPullReceiver _anyPullReceiver = new AnyPullReceiver(this);
        _anyPullReceiver.connect(getSetup(), _channel, false);

        AnyPushReceiver _anyPushReceiver = new AnyPushReceiver(this);
        _anyPushReceiver.connect(getSetup(), _channel, false);

        AnyPullSender _anyPullSender = new AnyPullSender(this, testPerson_);
        _anyPullSender.connect(getSetup(), _channel, false);

        AnyPushSender _anyPushSender = new AnyPushSender(this, testPerson_);
        _anyPushSender.connect(getSetup(), _channel, false);

        assertTrue(_anyPullReceiver.isConnected());
        assertTrue(_anyPushReceiver.isConnected());
        assertTrue(_anyPullSender.isConnected());
        assertTrue(_anyPushSender.isConnected());

        _anyPullReceiver.myAdmin_.destroy();
        _anyPushReceiver.myAdmin_.destroy();
        _anyPullSender.myAdmin_.destroy();
        _anyPushSender.myAdmin_.destroy();

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

        _channel.destroy();
    }

    public static Test suite() throws Exception {
        TestSuite _suite;

        _suite = new TestSuite("Basic CosNotification EventChannel Tests");

        NotificationTestCaseSetup _setup =
            new NotificationTestCaseSetup(_suite);

        String[] methodNames = TestUtils.getTestMethods( EventChannelTest.class);

        for (int x=0; x<methodNames.length; ++x) {
            _suite.addTest(new EventChannelTest(methodNames[x], _setup));
        }

        return _setup;
    }

    public static void main(String[] args) throws Exception {
        junit.textui.TestRunner.run(suite());
    }
}
