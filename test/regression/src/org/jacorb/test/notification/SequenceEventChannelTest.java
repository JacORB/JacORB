package org.jacorb.test.notification;

import junit.framework.Test;

import org.omg.CORBA.Any;
import org.omg.CORBA.IntHolder;
import org.omg.CosNotification.MaximumBatchSize;
import org.omg.CosNotification.Property;
import org.omg.CosNotification.StructuredEvent;
import org.omg.CosNotifyChannelAdmin.EventChannel;

/**
 * @author Alphonse Bendt
 */

public class SequenceEventChannelTest extends NotificationTestCase {

    EventChannel channel_;
    StructuredEvent[] testEvent_;

    public SequenceEventChannelTest(String name, NotificationTestCaseSetup setup) {
        super(name, setup);
    }


    public void setUpTest() throws Exception {
       channel_ = getDefaultChannel();

        // set test event type and name
        testEvent_ = new StructuredEvent[] {getTestUtils().getStructuredEvent()};
    }


    public void testSetMaximumBatchSize() throws Exception {
        StructuredEvent[] _events = new StructuredEvent[] {
            getTestUtils().getStructuredEvent(),
            getTestUtils().getStructuredEvent()
        };

        Any _value = getORB().create_any();

        _value.insert_long(2);

        channel_.set_qos(new Property[] {new Property( MaximumBatchSize.value, _value )});

        SequencePushSender _pushSender = new SequencePushSender(getClientORB(), _events);

        SequencePushReceiver _pushReceiver = new SequencePushReceiver(getClientORB());

        _pushSender.connect(channel_, false);
        _pushReceiver.connect(channel_, false);

        _pushReceiver.start();
        _pushSender.start();

        _pushSender.join();
        _pushReceiver.join();
    }


    public void testDestroyChannelDisconnectsClients() throws Exception {
        Property[] _p = new Property[0];
        IntHolder _channelId = new IntHolder();

        EventChannel _channel = getFactory().create_channel(_p, _p, _channelId);

        SequencePushSender _pushSender = new SequencePushSender(getClientORB(), testEvent_);
        SequencePullSender _pullSender = new SequencePullSender(getClientORB(), testEvent_);
        SequencePushReceiver _pushReceiver = new SequencePushReceiver(getClientORB());
        SequencePullReceiver _pullReceiver = new SequencePullReceiver(getClientORB());

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
        } catch (InterruptedException ie) {
            // ignored
        }

        assertTrue(!_pushSender.isConnected());
        assertTrue(!_pullSender.isConnected());
        assertTrue(!_pushReceiver.isConnected());
        assertTrue(!_pullReceiver.isConnected());
    }


    public void testSendPushPush() throws Exception {
        SequencePushSender _sender = new SequencePushSender(getClientORB(), testEvent_);
        SequencePushReceiver _receiver = new SequencePushReceiver(getClientORB());

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
        SequencePushSender _sender = new SequencePushSender(getClientORB(), testEvent_);
        SequencePullReceiver _receiver = new SequencePullReceiver(getClientORB());

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
        SequencePullSender _sender = new SequencePullSender(getClientORB(), testEvent_);
        SequencePushReceiver _receiver = new SequencePushReceiver(getClientORB());

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
        SequencePullSender _sender = new SequencePullSender(getClientORB(), testEvent_);
        SequencePullReceiver _receiver = new SequencePullReceiver(getClientORB());
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
        return NotificationTestCase.suite("Tests for Sequenced Event Channel",
                                          SequenceEventChannelTest.class);
    }
}

