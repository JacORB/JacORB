package org.jacorb.test.notification;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.jacorb.notification.util.QoSPropertySet;
import org.jacorb.test.notification.common.NotificationTestCase;
import org.junit.Before;
import org.junit.Test;
import org.omg.CORBA.Any;
import org.omg.CosNotification.BestEffort;
import org.omg.CosNotification.ConnectionReliability;
import org.omg.CosNotification.DiscardPolicy;
import org.omg.CosNotification.FifoOrder;
import org.omg.CosNotification.NamedPropertyRangeSeqHolder;
import org.omg.CosNotification.Priority;
import org.omg.CosNotification.Property;
import org.omg.CosNotification.QoSError_code;
import org.omg.CosNotification.UnsupportedQoS;

public class PropertyValidatorTest extends NotificationTestCase
{
    private QoSPropertySet objectUnderTest_;

    public QoSPropertySet createInstance() throws Exception
    {
        QoSPropertySet _props = new QoSPropertySet(getConfiguration(), QoSPropertySet.CHANNEL_QOS);

        return _props;
    }

    @Before
    public void setUp() throws Exception
    {
        objectUnderTest_ = createInstance();
    }

    @Test
    public void testValidateQoS() throws Exception
    {
        Property[] _props = new Property[3];
        Any _bestEffortAny = getORB().create_any();
        _bestEffortAny.insert_short(BestEffort.value);
        _props[0] = new Property(ConnectionReliability.value, _bestEffortAny);

        Any _priorityAny = getORB().create_any();
        _priorityAny.insert_short((short) 20);
        _props[1] = new Property(Priority.value, _priorityAny);

        Any _discardPolicyAny = getORB().create_any();
        _discardPolicyAny.insert_short(FifoOrder.value);

        _props[2] = new Property(DiscardPolicy.value, _discardPolicyAny);

        ////////////////////////////////////////

        objectUnderTest_.validate_qos(_props, new NamedPropertyRangeSeqHolder());

        ////////////////////////////////////////

        _props[2] = new Property("OtherPolicy", _discardPolicyAny);
        try
        {
            objectUnderTest_.validate_qos(_props, new NamedPropertyRangeSeqHolder());
            fail();
        } catch (UnsupportedQoS e)
        {
            // expected
        }

        ////////////////////////////////////////

        Any wrongType = getORB().create_any();
        wrongType.insert_long(10);
        _props[2] = new Property(DiscardPolicy.value, wrongType);
        try
        {
            objectUnderTest_.validate_qos(_props, new NamedPropertyRangeSeqHolder());
            fail();
        } catch (UnsupportedQoS ex)
        {
            // expected. verify contents.
            for (int x = 0; x < ex.qos_err.length; ++x)
            {
                if (ex.qos_err[x].name.equals(DiscardPolicy.value))
                {
                    assertEquals(QoSError_code._BAD_TYPE, ex.qos_err[x].code.value());
                }
            }
        }
    }
}