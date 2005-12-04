package org.jacorb.test.notification;

import junit.framework.Test;

import org.jacorb.test.notification.common.NotificationTestUtils;
import org.jacorb.test.notification.common.NotifyServerTestCase;
import org.jacorb.test.notification.common.NotifyServerTestSetup;
import org.omg.CORBA.Any;
import org.omg.CORBA.IntHolder;
import org.omg.CosNotification.Property;
import org.omg.CosNotifyChannelAdmin.EventChannel;

/**
 * Unit Test for class EventChannel. Test Backward compability. Access Notification Channel via the
 * CosEvent Interfaces.
 * 
 * @author Alphonse Bendt
 */

public class CosEventChannelTest extends NotifyServerTestCase
{
    EventChannel channel_;

    Any testData_;

    public void setUpTest() throws Exception
    {
        channel_ = getDefaultChannel();

        testData_ = new NotificationTestUtils(getClientORB()).getTestPersonAny();
    }

    public void testPushPush() throws Exception
    {
        CosEventPushReceiver _receiver = new CosEventPushReceiver(getClientORB());

        _receiver.connect(channel_, false);

        CosEventPushSender _sender = new CosEventPushSender(getClientORB(), testData_);
        _sender.connect(channel_, false);

        Thread _r = new Thread(_receiver);
        _r.start();

        Thread _s = new Thread(_sender);
        _s.start();
        _s.join();
        assertTrue(_sender.isEventHandled());
        _r.join();
        assertTrue(_receiver.isEventHandled());
    }

    public void testPushPull() throws Exception
    {
        CosEventPullReceiver _receiver = new CosEventPullReceiver(getClientORB());
        _receiver.connect(channel_, false);
        Thread _r = new Thread(_receiver);

        CosEventPushSender _sender = new CosEventPushSender(getClientORB(), testData_);
        _sender.connect(channel_, false);
        Thread _s = new Thread(_sender);

        _r.start();

        _s.start();
        _s.join();
        assertTrue(_sender.isEventHandled());

        _r.join();

        assertTrue(_receiver.isEventHandled());
    }

    public void testPullPush() throws Exception
    {
        CosEventPushReceiver _receiver = new CosEventPushReceiver(getClientORB());
        _receiver.connect(channel_, false);

        CosEventPullSender _sender = new CosEventPullSender(getClientORB(), testData_);
        _sender.connect(channel_, false);

        Thread _r = new Thread(_receiver);
        _r.start();
        Thread _s = new Thread(_sender);
        _s.start();

        _s.join();
        assertTrue(_sender.isEventHandled());

        _r.join();
        assertTrue(_receiver.isEventHandled());
    }

    public void testPullPull() throws Exception
    {
        CosEventPullReceiver _receiver = new CosEventPullReceiver(getClientORB());
        _receiver.connect(channel_, false);
        Thread _r = new Thread(_receiver);

        CosEventPullSender _sender = new CosEventPullSender(getClientORB(), testData_);
        _sender.connect(channel_, false);

        _r.start();

        _r.join();

        assertTrue(_receiver.isEventHandled());
    }

    public void testDestroyChannelDisconnectsClients() throws Exception
    {
        EventChannel _channel = 
            getEventChannelFactory().create_channel(new Property[0],
                    new Property[0], 
                    new IntHolder());

        TestClientOperations[] _testClients = new TestClientOperations[] {
                new CosEventPullSender(getClientORB(), testData_),
                new CosEventPushSender(getClientORB(), testData_),
                new CosEventPushReceiver(getClientORB()), 
                new CosEventPullReceiver(getClientORB()) };

        for (int x = 0; x < _testClients.length; ++x)
        {
            _testClients[x].connect(_channel, false);
            assertTrue(_testClients[x].isConnected());
        }

        _channel.destroy();

        Thread.sleep(1000);

        for (int x = 0; x < _testClients.length; ++x)
        {
            assertTrue("Idx: " + x + " still connected", !_testClients[x].isConnected());
        }
    }

    public CosEventChannelTest(String name, NotifyServerTestSetup setup)
    {
        super(name, setup);
    }

    public static Test suite() throws Exception
    {
        return NotifyServerTestCase.suite("Basic CosEvent EventChannel Tests",
                CosEventChannelTest.class);
    }
}
