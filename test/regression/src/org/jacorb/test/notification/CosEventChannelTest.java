package org.jacorb.test.notification;

import org.omg.CORBA.Any;
import org.omg.CORBA.IntHolder;
import org.omg.CosNotification.Property;
import org.omg.CosNotifyChannelAdmin.EventChannel;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.apache.avalon.framework.logger.Logger;

/**
 *  Unit Test for class EventChannel.
 *  Test Backward compability. Access Notification Channel via the
 *  CosEvent Interfaces.
 *
 * @author Alphonse Bendt
 * @version $Id$
 */

public class CosEventChannelTest extends NotificationTestCase
{
    EventChannel channel_;
    Any testData_;

    public void setUp() throws Exception
    {
        channel_ = getDefaultChannel();

        testData_ = getTestUtils().getTestPersonAny();
    }

    public void tearDown() throws Exception
    {
        super.tearDown();
    }


    public void testPushPush() throws Exception
    {
        CosEventPushReceiver _receiver = new CosEventPushReceiver(this);

        _receiver.connect(channel_, false);

        CosEventPushSender _sender = new CosEventPushSender(this, testData_);
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
        CosEventPullReceiver _receiver = new CosEventPullReceiver(this);
        _receiver.connect(channel_, false);
        Thread _r = new Thread(_receiver);

        CosEventPushSender _sender = new CosEventPushSender(this, testData_);
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
        CosEventPushReceiver _receiver = new CosEventPushReceiver(this);
        _receiver.connect(channel_, false);

        CosEventPullSender _sender = new CosEventPullSender(this, testData_);
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
        CosEventPullReceiver _receiver = new CosEventPullReceiver(this);
        _receiver.connect(channel_, false);
        Thread _r = new Thread(_receiver);

        CosEventPullSender _sender = new CosEventPullSender(this, testData_);
        _sender.connect(channel_, false);

        _r.start();

        _r.join();

        assertTrue(_receiver.isEventHandled());
    }

    public void testDestroyChannelDisconnectsClients() throws Exception
    {

        EventChannel _channel = getFactory().create_channel(new Property[0],
                                new Property[0],
                                new IntHolder());

        TestClientOperations[] _testClients = new TestClientOperations[] {
                                                  new CosEventPullSender(this, testData_),
                                                  new CosEventPushSender(this, testData_),
                                                  new CosEventPushReceiver(this),
                                                  new CosEventPullReceiver(this)};

        for (int x = 0; x < _testClients.length; ++x)
        {
            _testClients[x].connect(_channel, false);
            assertTrue(_testClients[x].isConnected());
        }

        _channel.destroy();

        for (int x = 0; x < _testClients.length; ++x)
        {
            assertTrue("Idx: " + x + " still connected", !_testClients[x].isConnected());
        }
    }

    /**
     * Creates a new <code>EventChannelTest</code> instance.
     *
     * @param name test name
     */
    public CosEventChannelTest (String name, NotificationTestCaseSetup setup)
    {
        super(name, setup);
    }

    /**
     * @return a <code>TestSuite</code>
     */
    public static Test suite() throws Exception
    {
        TestSuite _suite;

        _suite = new TestSuite("Basic CosEvent EventChannel Tests");

        NotificationTestCaseSetup _setup =
            new NotificationTestCaseSetup( _suite );

        String[] methodNames = org.jacorb.test.common.TestUtils.getTestMethods(CosEventChannelTest.class);

        for (int x = 0; x < methodNames.length; ++x)
        {
            _suite.addTest(new CosEventChannelTest(methodNames[x], _setup));
        }

        return _setup;
    }

    /**
     * Entry point
     */
    public static void main(String[] args) throws Exception
    {
        junit.textui.TestRunner.run(suite());
    }

}
