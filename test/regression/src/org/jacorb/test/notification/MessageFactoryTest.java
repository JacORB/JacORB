package org.jacorb.test.notification;

import java.util.Date;

import junit.framework.Test;

import org.jacorb.notification.impl.DefaultMessageFactory;
import org.jacorb.notification.interfaces.FilterStage;
import org.jacorb.notification.interfaces.Message;
import org.jacorb.notification.servant.IProxyConsumer;
import org.jacorb.util.Time;
import org.omg.CORBA.Any;
import org.omg.CosNotification.EventHeader;
import org.omg.CosNotification.EventType;
import org.omg.CosNotification.FixedEventHeader;
import org.omg.CosNotification.Property;
import org.omg.CosNotification.StopTime;
import org.omg.CosNotification.StructuredEvent;
import org.omg.CosNotification.StructuredEventHelper;
import org.omg.TimeBase.UtcT;
import org.omg.TimeBase.UtcTHelper;

/**
 * @author Alphonse Bendt
 */

public class MessageFactoryTest extends NotificationTestCase
{
    DefaultMessageFactory messageFactory_;

    NotificationTestUtils testUtils_;

    Any testPerson_;

    StructuredEvent testStructured_;

    // //////////////////////////////////////

    public void testStructuredEventWithStopTimeProperty() throws Exception
    {
        testStructured_.header.variable_header = new Property[1];

        Date _now = new Date();

        Any _any = getORB().create_any();
        UtcT _utc = Time.corbaTime(_now);
        UtcTHelper.insert(_any, _utc);

        testStructured_.header.variable_header[0] = new Property(StopTime.value, _any);

        Message _event = messageFactory_.newMessage(testStructured_);
        assertTrue(_event.hasStopTime());
        assertEquals(_now.getTime(), _event.getStopTime());
    }

    
    public void testStructuredEventWithoutStopTimeProperty() throws Exception
    {
        Message _event = messageFactory_.newMessage(testStructured_);
        assertTrue(!_event.hasStopTime());
    }

    public void testAnyEventHasNoStopTime() throws Exception
    {
        Message _event = messageFactory_.newMessage(getORB().create_any());
        assertTrue(!_event.hasStopTime());
    }
    
    public void testNewEventStructured() throws Exception
    {
        assertNotNull(messageFactory_.newMessage(testStructured_));
    }

    public void testStructuredToAny() throws Exception
    {
        Message _notifyEvent = messageFactory_.newMessage(testStructured_);
        assertNotNull(_notifyEvent);
        Any _any = _notifyEvent.toAny();
        StructuredEvent _event = StructuredEventHelper.extract(_any);
        assertNotNull(_event);
        assertEquals("domain", _event.header.fixed_header.event_type.domain_name);
        assertEquals("type", _event.header.fixed_header.event_type.type_name);
    }

    public void testStructuredToStructured() throws Exception
    {
        Message _notifyEvent = messageFactory_.newMessage(testStructured_);
        assertNotNull(_notifyEvent);
        StructuredEvent _event = _notifyEvent.toStructuredEvent();
        assertNotNull(_event);
        assertEquals("domain", _event.header.fixed_header.event_type.domain_name);
        assertEquals("type", _event.header.fixed_header.event_type.type_name);
    }

    public void testNewEventAny() throws Exception
    {
        Message _notifyEvent = messageFactory_.newMessage(testPerson_);

        assertNotNull(_notifyEvent);
    }

    public void testAnyToStructured() throws Exception
    {
        Message _notifyEvent = messageFactory_.newMessage(testPerson_);
        StructuredEvent _structured = _notifyEvent.toStructuredEvent();
        assertNotNull(_structured);
        assertEquals("%ANY", _structured.header.fixed_header.event_type.type_name);
        assertEquals("", _structured.header.fixed_header.event_type.domain_name);
        assertEquals("", _structured.header.fixed_header.event_name);

        Person _p = PersonHelper.extract(_structured.remainder_of_body);
        assertNotNull(_p);
        assertEquals("firstname", _p.first_name);
        assertEquals("lastname", _p.last_name);
    }

    public void testAnyToAny() throws Exception
    {
        Message _notifyEvent = messageFactory_.newMessage(testPerson_);
        Any _anyEvent = _notifyEvent.toAny();
        assertNotNull(_anyEvent);

        Person _p = PersonHelper.extract(_anyEvent);
        assertNotNull(_p);
        assertEquals("firstname", _p.first_name);
        assertEquals("lastname", _p.last_name);
    }

    public void testWrappedStructuredEventToStructuredEvent() throws Exception
    {
        Any _wrappedStructuredEvent = getORB().create_any();

        StructuredEventHelper.insert(_wrappedStructuredEvent, testStructured_);

        Message _mesg = messageFactory_.newMessage(_wrappedStructuredEvent,
                new IProxyConsumer()
                {
                    public boolean getStartTimeSupported()
                    {
                        return false;
                    }

                    public boolean getTimeOutSupported()
                    {
                        return false;
                    }

                    public FilterStage getFirstStage()
                    {
                        return null;
                    }
                });

        StructuredEvent _recvd = _mesg.toStructuredEvent();

        assertEquals(testStructured_.header.fixed_header.event_name,
                _recvd.header.fixed_header.event_name);

        assertEquals(testStructured_.remainder_of_body, _recvd.remainder_of_body);
        assertEquals(testStructured_.remainder_of_body, _recvd.remainder_of_body);
    }

    public void testWrappedAnyToAny() throws Exception
    {
        StructuredEvent _wrappedAny = new StructuredEvent();

        EventHeader _header = new EventHeader();
        FixedEventHeader _fixed = new FixedEventHeader();
        _fixed.event_name = "";
        _fixed.event_type = new EventType("", "%ANY");
        _header.fixed_header = _fixed;
        _header.variable_header = new Property[0];

        _wrappedAny.header = _header;

        _wrappedAny.filterable_data = new Property[0];

        _wrappedAny.remainder_of_body = testPerson_;

        Message _mesg = messageFactory_.newMessage(_wrappedAny, new IProxyConsumer()
        {
            public boolean getStartTimeSupported()
            {
                return false;
            }

            public boolean getTimeOutSupported()
            {
                return false;
            }

            public FilterStage getFirstStage()
            {
                return null;
            }
        });

        assertEquals(testPerson_, _mesg.toAny());
    }

    public void setUpTest() throws Exception
    {
        testUtils_ = new NotificationTestUtils(getORB());

        messageFactory_ = new DefaultMessageFactory(getConfiguration());

        testPerson_ = testUtils_.getTestPersonAny();

        testStructured_ = new StructuredEvent();
        EventHeader _header = new EventHeader();
        FixedEventHeader _fixed = new FixedEventHeader();
        _fixed.event_name = "eventname";
        _fixed.event_type = new EventType("domain", "type");
        _header.fixed_header = _fixed;
        _header.variable_header = new Property[0];

        testStructured_.header = _header;

        testStructured_.filterable_data = new Property[0];

        testStructured_.remainder_of_body = getORB().create_any();
    }

    public MessageFactoryTest(String test, NotificationTestCaseSetup setup)
    {
        super(test, setup);
    }

    public static Test suite() throws Exception
    {
        return NotificationTestCase.suite(MessageFactoryTest.class);
    }
}
