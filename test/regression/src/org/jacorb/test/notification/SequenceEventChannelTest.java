package org.jacorb.test.notification;

import org.omg.CORBA.IntHolder;
import org.omg.CosNotification.Property;
import org.omg.CosNotification.StructuredEvent;
import org.omg.CosNotifyChannelAdmin.EventChannel;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */

public class SequenceEventChannelTest extends NotificationTestCase {

    EventChannel channel_;
    StructuredEvent[] testEvent_;

    public SequenceEventChannelTest(String name, NotificationTestCaseSetup setup) {
        super(name, setup);
    }


    public void tearDown() throws Exception {
        super.tearDown();
    }


    public void setUp() throws Exception {
        channel_ = getDefaultChannel();

        // set test event type and name
        testEvent_ = new StructuredEvent[] {getTestUtils().getStructuredEvent()};
    }


    public void testDestroyChannelDisconnectsClients() throws Exception {
        Property[] _p = new Property[0];
        IntHolder _channelId = new IntHolder();

        EventChannel _channel = getFactory().create_channel(_p, _p, _channelId);

        SequencePushSender _pushSender = new SequencePushSender(this, testEvent_);
        SequencePullSender _pullSender = new SequencePullSender(this, testEvent_);
        SequencePushReceiver _pushReceiver = new SequencePushReceiver(this);
        SequencePullReceiver _pullReceiver = new SequencePullReceiver(this);

        _pushSender.connect(_channel,false);
        _pullSender.connect(_channel,false);
        _pushReceiver.connect(_channel,false);
        _pullReceiver.connect(_channel,false);

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
        SequencePushSender _sender = new SequencePushSender(this, testEvent_);
        SequencePushReceiver _receiver = new SequencePushReceiver(this);

        _sender.connect(channel_,false);
        _receiver.connect(channel_,false);

        _receiver.start();
        _sender.start();

        _sender.join();
        _receiver.join();

        assertTrue("Error while sending", !_sender.error_);
        assertTrue("Should have received something", _receiver.received_);
    }


    public void testSendPushPull() throws Exception {
        SequencePushSender _sender = new SequencePushSender(this, testEvent_);
        SequencePullReceiver _receiver = new SequencePullReceiver(this);

        _sender.connect(channel_,false);
        _receiver.connect(channel_,false);

        _receiver.start();
        _sender.start();

        _sender.join();
        _receiver.join();

        assertTrue("Error while sending", _sender.isEventHandled());
        assertTrue("Should have received something", _receiver.isEventHandled());
    }


    public void testSendPullPush() throws Exception {
        SequencePullSender _sender = new SequencePullSender(this, testEvent_);
        SequencePushReceiver _receiver = new SequencePushReceiver(this);

        _receiver.connect(channel_,false);
        _sender.connect(channel_,false);

        _receiver.start();
        _sender.start();

        _sender.join();
        _receiver.join();

        assertTrue("Error while sending", !_sender.isError());
        assertTrue("Should have received something", _receiver.received_);
    }


    public void testSendPullPull() throws Exception {
        SequencePullSender _sender = new SequencePullSender(this, testEvent_);
        SequencePullReceiver _receiver = new SequencePullReceiver(this);
            _sender.connect(channel_,false);

        _receiver.connect(channel_,false);

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

        _suite = new TestSuite("Tests for Sequenced Event Channel");

        NotificationTestCaseSetup _setup =
            new NotificationTestCaseSetup(_suite);

        String[] methodNames =
            org.jacorb.test.common.TestUtils.getTestMethods(SequenceEventChannelTest.class);

        for (int x=0; x<methodNames.length; ++x) {
            _suite.addTest(new SequenceEventChannelTest(methodNames[x], _setup));
        }

        return _setup;
    }


    public static void main(String[] args) throws Exception {
        junit.textui.TestRunner.run(suite());
    }
}

