package org.jacorb.test.notification;

import org.omg.CORBA.Any;
import org.omg.CORBA.LongSeqHelper;
import org.omg.CORBA.ORB;
import org.omg.CosNotification.EventHeader;
import org.omg.CosNotification.EventType;
import org.omg.CosNotification.FixedEventHeader;
import org.omg.CosNotification.Property;
import org.omg.CosNotification.StructuredEvent;

/**
 * @author Alphonse Bendt
 */

public class TestUtils
{
    private final static ORB sOrb_ = ORB.init();

    ////////////////////////////////////////

    public StructuredEvent getStructuredEvent()
    {
        FixedEventHeader _fixedHeader = new FixedEventHeader();
        _fixedHeader.event_name = "ALARM";
        _fixedHeader.event_type = new EventType("TESTING", "TESTING");
        EventHeader _header = new EventHeader(_fixedHeader, new Property[0]);

        StructuredEvent _structuredEvent = new StructuredEvent(_header, new Property[0],
                getTestPersonAny());

        return _structuredEvent;
    }

    public Person getTestPerson()
    {
        // prepare test data
        Person _p = new Person();
        Address _a = new Address();

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
        _p.aliases = new String[] { "Alias0", "Alias1", "Alias2" };
        _p.numbers = new int[] { 10, 20, 30, 40, 50 };

        return _p;
    }

    public Any getTestPersonAny()
    {
        Any _testPerson;

        _testPerson = sOrb_.create_any();
        PersonHelper.insert(_testPerson, getTestPerson());

        return _testPerson;
    }

    public Any getSizedTestData(int size)
    {
        Any _testData = sOrb_.create_any();
        int[] _payload = new int[size];

        for (int x = 0; x < size; ++x)
        {
            _payload[x] = x;
        }

        LongSeqHelper.insert(_testData, _payload);

        return _testData;
    }
}