package org.jacorb.test.notification;

import org.omg.CORBA.Any;
import org.omg.CORBA.IntHolder;
import org.omg.CORBA.NO_IMPLEMENT;
import org.omg.CosNotification.EventHeader;
import org.omg.CosNotification.EventType;
import org.omg.CosNotification.FixedEventHeader;
import org.omg.CosNotification.Property;
import org.omg.CosNotification.StructuredEvent;
import org.omg.CosNotifyChannelAdmin.EventChannel;
import org.omg.CosNotifyChannelAdmin.ObtainInfoMode;
import org.omg.CosNotifyFilter.ConstraintExp;
import org.omg.CosNotifyFilter.ConstraintInfo;
import org.omg.CosNotifyFilter.Filter;

import org.jacorb.test.common.TestUtils;
import org.jacorb.util.Debug;

import EDU.oswego.cs.dl.util.concurrent.SynchronizedInt;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.apache.avalon.framework.logger.Logger;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */

public class StructuredEventChannelTest extends NotificationTestCase
{
    private static final EventType[] EMPTY_EVENT_TYPE = new EventType[0];

    Logger logger_ = Debug.getNamedLogger(getClass().getName());

    EventChannel channel_;

    StructuredEvent testEvent_;

    Filter trueFilter_;
    Filter falseFilter_;
    NotificationTestUtils testUtils_;

    ////////////////////////////////////////

    public StructuredEventChannelTest(String name, NotificationTestCaseSetup setup)
    {
        super(name, setup);
    }

    ////////////////////////////////////////

    public void tearDown() throws Exception
    {
        super.tearDown();
    }

    public void setUp() throws Exception
    {
        channel_ = getDefaultChannel();

        // set test event type and name
        testEvent_ = new StructuredEvent();
        EventType _type = new EventType("testDomain", "testType");
        FixedEventHeader _fixed = new FixedEventHeader(_type, "testing");

        // complete header date
        Property[] _variable = new Property[0];
        testEvent_.header = new EventHeader(_fixed, _variable);

        // set filterable event body data
        testEvent_.filterable_data = new Property[1];

        Any _personAny = getORB().create_any();

        // prepare filterable body data
        Person _p = getTestUtils().getTestPerson();
        Address _a = new Address();
        NamedValue _nv = new NamedValue();

        _p.first_name = "firstname";
        _p.last_name = "lastname";
        _p.age = 5;
        _p.phone_numbers = new String[2];
        _p.phone_numbers[0] = "12345678";
        _p.phone_numbers[1] = "";
        _p.nv = new NamedValue[2];
        _p.nv[0] = new NamedValue();
        _p.nv[1] = new NamedValue();
        _p.person_profession = Profession.STUDENT;
        _a.street = "Takustr.";
        _a.number = 9;
        _a.city = "Berlin";
        _p.home_address = _a;

        PersonHelper.insert(_personAny, _p);
        testEvent_.filterable_data[0] = new Property("person", _personAny);

        testEvent_.remainder_of_body = getORB().create_any();

        trueFilter_ = channel_.default_filter_factory().create_filter("EXTENDED_TCL");

        ConstraintExp[] _constraintExp = new ConstraintExp[1];
        EventType[] _eventType = new EventType[1];
        _eventType[0] = new EventType("*", "*");

        _constraintExp[0] = new ConstraintExp(_eventType, "true");
        ConstraintInfo[] _info = trueFilter_.add_constraints(_constraintExp);

        falseFilter_ = channel_.default_filter_factory().create_filter("EXTENDED_TCL");

        _constraintExp = new ConstraintExp[1];
        _eventType = new EventType[1];
        _eventType[0] = new EventType("*", "*");

        _constraintExp[0] = new ConstraintExp(_eventType, "false");
        _info = falseFilter_.add_constraints(_constraintExp);
    }


    public void testDestroyChannelDisconnectsClients() throws Exception
    {
        Property[] _p = new Property[0];

        EventChannel _channel = getFactory().create_channel(_p, _p, new IntHolder());

        StructuredPushSender _pushSender = new StructuredPushSender(this, testEvent_);
        StructuredPullSender _pullSender = new StructuredPullSender(this, testEvent_);
        StructuredPushReceiver _pushReceiver = new StructuredPushReceiver(this);
        StructuredPullReceiver _pullReceiver = new StructuredPullReceiver(this);

        _pushSender.connect(_channel, false);
        _pullSender.connect(_channel, false);
        _pushReceiver.connect(_channel, false);
        _pullReceiver.connect(_channel, false);

        assertTrue(_pushSender.isConnected());
        assertTrue(_pullSender.isConnected());
        assertTrue(_pushReceiver.isConnected());
        assertTrue(_pullReceiver.isConnected());

        _channel.destroy();

        assertTrue(!_pushSender.isConnected());
        assertTrue(!_pullSender.isConnected());
        assertTrue(!_pushReceiver.isConnected());
        assertTrue(!_pullReceiver.isConnected());
    }


    public void testObtainSubscriptionTypes_sender_throws_NO_IMPLEMENT() throws Exception {
        final SynchronizedInt subscriptionChangeCounter = new SynchronizedInt(0);

        StructuredPushSender _sender = new StructuredPushSender(this, testEvent_) {
                public void subscription_change(EventType[] added, EventType[] removed) {
                    subscriptionChangeCounter.increment();

                    throw new NO_IMPLEMENT();
                }
            };
        StructuredPushReceiver _receiver = new StructuredPushReceiver(this);

        _sender.connect(channel_, false);

        _receiver.connect(channel_, false);

        _sender.pushConsumer_.obtain_subscription_types(ObtainInfoMode.NONE_NOW_UPDATES_ON);

        EventType[] offers = new EventType[] {new EventType("domain1", "type1")};

        _receiver.pushSupplier_.subscription_change(offers, EMPTY_EVENT_TYPE);

        offers = new EventType[] {new EventType("domain2", "type2")};

        _receiver.pushSupplier_.subscription_change(offers, EMPTY_EVENT_TYPE);

        Thread.sleep(1000);

        assertEquals(1, subscriptionChangeCounter.get());
    }


    public void testObtainSubscriptionTypes_NONE_NOW_UPDATE_ON() throws Exception {
        StructuredPushSender _sender = new StructuredPushSender(this, testEvent_);
        StructuredPushReceiver _receiver = new StructuredPushReceiver(this);

        _sender.connect(channel_, false);
        _receiver.connect(channel_, false);

        _sender.pushConsumer_.obtain_subscription_types(ObtainInfoMode.NONE_NOW_UPDATES_ON);

        EventType[] offers = new EventType[] {new EventType("domain1", "type1")};

        _receiver.pushSupplier_.subscription_change(offers, EMPTY_EVENT_TYPE);

        Thread.sleep(1000);

        assertEquals(1, _sender.addedSubscriptions_.size());
        assertEquals("domain1", ((EventType)_sender.addedSubscriptions_.get(0)).domain_name);
        assertEquals("type1", ((EventType)_sender.addedSubscriptions_.get(0)).type_name);
    }


    public void testObtainSubscriptionTypes_ALL_NOW_UPDATE_OFF() throws Exception {
        StructuredPushSender _sender = new StructuredPushSender(this, testEvent_);
        StructuredPushReceiver _receiver = new StructuredPushReceiver(this);

        _sender.connect(channel_, false);
        _receiver.connect(channel_, false);

        EventType[] offers = new EventType[] {new EventType("domain1", "type1")};

        _receiver.pushSupplier_.subscription_change(offers, EMPTY_EVENT_TYPE);

        EventType[] _subscriptionTypes =
            _sender.pushConsumer_.obtain_subscription_types(ObtainInfoMode.ALL_NOW_UPDATES_ON);

        Thread.sleep(1000);

        assertEquals(1, _subscriptionTypes.length);
        assertEquals("domain1", _subscriptionTypes[0].domain_name);
        assertEquals("type1", _subscriptionTypes[0].type_name);
    }


    public void testObtainOfferedTypes_NONE_NOW_UPDATES_ON() throws Exception {
        StructuredPushSender _sender = new StructuredPushSender(this, testEvent_);
        StructuredPushReceiver _receiver = new StructuredPushReceiver(this);

        _sender.connect(channel_, false);
        _receiver.connect(channel_, false);

        _receiver.pushSupplier_.obtain_offered_types(ObtainInfoMode.NONE_NOW_UPDATES_ON);

        EventType[] offers = new EventType[] {new EventType("domain1", "type1")};

        _sender.pushConsumer_.offer_change(offers, EMPTY_EVENT_TYPE);

        Thread.sleep(1000);

        assertEquals(1, _receiver.addedOffers.size());
        assertEquals("domain1", ((EventType)_receiver.addedOffers.get(0)).domain_name);
        assertEquals("type1", ((EventType)_receiver.addedOffers.get(0)).type_name);
    }


    public void testObtainOfferedTypes_ALL_NOW_UPDATES_ON() throws Exception {
        StructuredPushSender _sender = new StructuredPushSender(this, testEvent_);
        StructuredPushReceiver _receiver = new StructuredPushReceiver(this);

        _sender.connect(channel_, false);
        _receiver.connect(channel_, false);

        EventType[] offers = new EventType[] {new EventType("domain1", "type1"),
                                              new EventType("domain2", "type2") };


        _sender.pushConsumer_.offer_change(offers, EMPTY_EVENT_TYPE);


        EventType[] _offeredTypes =
            _receiver.pushSupplier_.obtain_offered_types(ObtainInfoMode.ALL_NOW_UPDATES_ON);


        assertEquals(2, _offeredTypes.length);
    }


    public void testObtainOfferedTypes_receiver_throws_NO_IMPLEMENT() throws Exception {
        final SynchronizedInt offerChangeCalled = new SynchronizedInt(0);

        StructuredPushSender _sender = new StructuredPushSender(this, testEvent_);

        StructuredPushReceiver _receiver = new StructuredPushReceiver(this) {
                public void offer_change(EventType[] added, EventType[] removed) {
                    offerChangeCalled.increment();

                    throw new NO_IMPLEMENT();
                }
            };

        _sender.connect(channel_, false);
        _receiver.connect(channel_, false);

        _receiver.pushSupplier_.obtain_offered_types(ObtainInfoMode.NONE_NOW_UPDATES_ON);

        EventType[] offers = new EventType[] {new EventType("domain1", "type1"),
                                              new EventType("domain2", "type2") };

        _sender.pushConsumer_.offer_change(offers, EMPTY_EVENT_TYPE);

        offers = new EventType[] {new EventType("domain3", "type3"),
                                  new EventType("domain4", "type4") };


        _sender.pushConsumer_.offer_change(offers, EMPTY_EVENT_TYPE);

        assertEquals(1, offerChangeCalled.get() );
    }


    public void testSendPushPush() throws Exception
    {
        StructuredPushSender _sender = new StructuredPushSender(this, testEvent_);
        StructuredPushReceiver _receiver = new StructuredPushReceiver(this);

        _sender.connect(channel_, false);
        _receiver.connect(channel_, false);

        _receiver.start();
        _sender.start();

        _sender.join();
        _receiver.join();

        assertTrue("Error while sending", !_sender.error_);
        assertTrue("Should have received something", _receiver.isEventHandled());
    }


    public void testSendPushPull() throws Exception
    {
        StructuredPushSender _sender = new StructuredPushSender(this, testEvent_);
        StructuredPullReceiver _receiver = new StructuredPullReceiver(this);

        _sender.connect(channel_, false);
        _receiver.connect(channel_, false);

        _receiver.start();
        _sender.start();

        _sender.join();
        _receiver.join();

        assertTrue("Error while sending", !_sender.error_);
        assertTrue("Should have received something", _receiver.received_);
    }

    public void testSendPullPush() throws Exception
    {
        StructuredPullSender _sender = new StructuredPullSender(this, testEvent_);
        StructuredPushReceiver _receiver = new StructuredPushReceiver(this);
        _receiver.setTimeOut(2000);

        _sender.connect(channel_, false);
        _receiver.connect(channel_, false);

        _receiver.start();
        _sender.start();

        _sender.join();
        _receiver.join();

        assertTrue("Error while sending", !_sender.isError());
        assertTrue("Should have received something", _receiver.isEventHandled());
    }

    public void testSendPullPull() throws Exception
    {
        StructuredPullSender _sender = new StructuredPullSender(this, testEvent_);
        StructuredPullReceiver _receiver = new StructuredPullReceiver(this);
        _sender.connect(channel_, false);

        _receiver.connect(channel_, false);

        _receiver.start();
        _sender.start();

        _sender.join();
        _receiver.join();

        boolean _senderError = ((TestClientOperations)_sender).isError();
        assertTrue("Error while sending", !_senderError);
        assertTrue("Should have received something", _receiver.isEventHandled());
    }


    public static Test suite() throws Exception
    {
        TestSuite _suite;

        _suite = new TestSuite("Test of Structured EventChannel");

        NotificationTestCaseSetup _setup =
            new NotificationTestCaseSetup(_suite);

        String[] methodNames = TestUtils.getTestMethods(StructuredEventChannelTest.class);

        for (int x = 0; x < methodNames.length; ++x)
        {
            _suite.addTest(new StructuredEventChannelTest(methodNames[x], _setup));
        }

        return _setup;
    }

    public static void main(String[] args) throws Exception
    {
        junit.textui.TestRunner.run(suite());
    }
}

