package org.jacorb.test.notification;

import org.jacorb.notification.servant.QoSPropertySet;

import org.omg.CORBA.Any;
import org.omg.CORBA.ORB;
import org.omg.CosNotification.BestEffort;
import org.omg.CosNotification.ConnectionReliability;
import org.omg.CosNotification.DiscardPolicy;
import org.omg.CosNotification.FifoOrder;
import org.omg.CosNotification.NamedPropertyRangeSeqHolder;
import org.omg.CosNotification.Priority;
import org.omg.CosNotification.Property;
import org.omg.CosNotification.UnsupportedQoS;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.omg.CosNotification.QoSError_code;

public class PropertyValidatorTest
            extends TestCase
{

    QoSPropertySet propertyvalidator = null;

    ORB orb_;


    public PropertyValidatorTest(String name)
    {
        super(name);
    }


    public QoSPropertySet createInstance() throws Exception
    {
        return new QoSPropertySet(QoSPropertySet.CHANNEL_QOS);
    }


    protected void setUp() throws Exception
    {
        super.setUp();
        orb_ = ORB.init();
        propertyvalidator = createInstance();
    }


    protected void tearDown() throws Exception
    {
        propertyvalidator = null;
        super.tearDown();
    }


    public void testValidateQoS() throws Exception
    {
        Property[] _props = new Property[3];
        Any _bestEffortAny = orb_.create_any();
        _bestEffortAny.insert_short(BestEffort.value);
        _props[0] = new Property(ConnectionReliability.value, _bestEffortAny);

        Any _priorityAny = orb_.create_any();
        _priorityAny.insert_short((short)20);
        _props[1] = new Property(Priority.value, _priorityAny);

        Any _discardPolicyAny = orb_.create_any();
        _discardPolicyAny.insert_short(FifoOrder.value);

        _props[2] = new Property(DiscardPolicy.value, _discardPolicyAny);

        ////////////////////////////////////////

        propertyvalidator.validate_qos(_props, new NamedPropertyRangeSeqHolder());

        ////////////////////////////////////////

        _props[2] = new Property("OtherPolicy", _discardPolicyAny);
        try
        {
            propertyvalidator.validate_qos(_props, new NamedPropertyRangeSeqHolder());
            fail();
        }
        catch (UnsupportedQoS e)
        {}

        ////////////////////////////////////////

        Any wrongType = orb_.create_any();
        wrongType.insert_long(10);
        _props[2] = new Property(DiscardPolicy.value, wrongType);
        try
        {
            propertyvalidator.validate_qos(_props, new NamedPropertyRangeSeqHolder());
            fail();
        }
        catch (UnsupportedQoS ex)
        {
            for (int x=0; x<ex.qos_err.length; ++x) {
                if (ex.qos_err[x].name.equals(DiscardPolicy.value)) {
                    assertEquals(QoSError_code._BAD_TYPE, ex.qos_err[x].code.value());
                }
            }
        }

    }


    public static Test suite()
    {
        TestSuite suite = new TestSuite(PropertyValidatorTest.class);

        return suite;
    }


    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(suite());
    }
}
