package org.jacorb.test.notification;

import static org.junit.Assert.assertTrue;
import org.jacorb.test.notification.common.NotificationTestUtils;
import org.jacorb.test.notification.common.NotifyServerTestCase;
import org.junit.Before;
import org.junit.Test;
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

    @Before
    public void setUp() throws Exception
    {
        channel_ = getDefaultChannel();

        testData_ = new NotificationTestUtils(setup.getClientOrb()).getTestPersonAny();
    }

    @Test
    public void testPushPush() throws Exception
    {
        CosEventPushReceiver _receiver = new CosEventPushReceiver(setup.getClientOrb());

        _receiver.connect(channel_, false);

        CosEventPushSender _sender = new CosEventPushSender(setup.getClientOrb(), testData_);
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

    @Test
    public void testPushPull() throws Exception
    {
        CosEventPullReceiver _receiver = new CosEventPullReceiver(setup.getClientOrb());
        _receiver.connect(channel_, false);
        Thread _r = new Thread(_receiver);

        CosEventPushSender _sender = new CosEventPushSender(setup.getClientOrb(), testData_);
        _sender.connect(channel_, false);
        Thread _s = new Thread(_sender);

        _r.start();

        _s.start();
        _s.join();
        assertTrue(_sender.isEventHandled());

        _r.join();

        assertTrue(_receiver.isEventHandled());
    }

    @Test
    public void testPullPush() throws Exception
    {
        CosEventPushReceiver _receiver = new CosEventPushReceiver(setup.getClientOrb());
        _receiver.connect(channel_, false);

        CosEventPullSender _sender = new CosEventPullSender(setup.getClientOrb(), testData_);
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

    @Test
    public void testPullPull() throws Exception
    {
        CosEventPullReceiver _receiver = new CosEventPullReceiver(setup.getClientOrb());
        _receiver.connect(channel_, false);
        Thread _r = new Thread(_receiver);

        CosEventPullSender _sender = new CosEventPullSender(setup.getClientOrb(), testData_);
        _sender.connect(channel_, false);

        _r.start();

        _r.join();

        assertTrue(_receiver.isEventHandled());
    }

    @Test
    public void testDestroyChannelDisconnectsClients() throws Exception
    {
        EventChannel _channel =
            getEventChannelFactory().create_channel(new Property[0],
                    new Property[0],
                    new IntHolder());

        TestClientOperations[] _testClients = new TestClientOperations[] {
                new CosEventPullSender(setup.getClientOrb(), testData_),
                new CosEventPushSender(setup.getClientOrb(), testData_),
                new CosEventPushReceiver(setup.getClientOrb()),
                new CosEventPullReceiver(setup.getClientOrb()) };

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

/*    @BeforeClass
    public static void beforeClassSetUp() throws Exception throws Exception
    {
        return NotifyServerTestCase.suite("Basic CosEvent EventChannel Tests",
                CosEventChannelTest.class);
    }*/
}
