package org.jacorb.test.notification;

import org.jacorb.notification.PropertyValidator;

import org.omg.CORBA.Any;
import org.omg.CORBA.ORB;
import org.omg.CosNotification.BestEffort;
import org.omg.CosNotification.ConnectionReliability;
import org.omg.CosNotification.DiscardPolicy;
import org.omg.CosNotification.FifoOrder;
import org.omg.CosNotification.Priority;
import org.omg.CosNotification.Property;
import org.omg.CosNotification.UnsupportedQoS;

import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.TestSuite;

public class PropertyValidatorTest
    extends TestCase
{

    PropertyValidator propertyvalidator = null;
    ORB orb_;


    public PropertyValidatorTest(String name) {
        super(name);
    }

    public PropertyValidator createInstance() throws Exception {
        return new PropertyValidator();
    }

  protected void setUp() throws Exception {
    super.setUp();
    orb_ = ORB.init();
    propertyvalidator = createInstance();
  }

  protected void tearDown() throws Exception {
    propertyvalidator = null;
    super.tearDown();

  }

  public void testCheckQoSPropertySeq() throws Exception {
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

      propertyvalidator.checkQoSPropertySeq(_props);

      try {
          _props[2] = new Property("OtherPolicy", _discardPolicyAny);
          propertyvalidator.checkQoSPropertySeq(_props);
          fail();
      } catch (UnsupportedQoS e) {
          assertTrue(e.qos_err.length == 1);
      }
  }

  public void testCheckAdminPropertySeq() throws Exception {
  }

  public void testGetUniqueProperties() throws Exception {
  }

  public void testValidateAdminPropertySeq() throws Exception {
  }

    public static Test suite() {
        TestSuite suite = new TestSuite("PropertyValidatorTest");

        suite.addTest(new PropertyValidatorTest("testCheckQoSPropertySeq"));

        return suite;
    }

  public static void main(String[] args) {
    junit.textui.TestRunner.run(suite());
  }
}
