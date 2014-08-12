package org.jacorb.test.bugs.bug976;

import static org.junit.Assert.fail;
import java.util.Properties;
import org.jacorb.test.harness.ORBTestCase;
import org.junit.Before;
import org.junit.Test;
import org.omg.CORBA.NO_PERMISSION;

public final class Bug976Test extends ORBTestCase
{
    /**
     * Bit of a shortcut : allows the interceptor to get the name of the
     * currently running test case so it change its behaviour per test.
     */
    static String testName;

    private Hello reference;


    @Override
    protected void patchORBProperties(Properties props) throws Exception
    {
        props.setProperty("org.omg.PortableInterceptor.ORBInitializerClass."
                          + "ORBInit", Initializer.class.getName());
    }

    @Before
    public void setUp() throws Exception
    {
        HelloImpl hello = new HelloImpl();

        org.omg.CORBA.Object obj = rootPOA.servant_to_reference(hello);
        reference = HelloHelper.narrow(obj);

        testName = name.getMethodName();
    }

    @Test
    public void testSlotReceiveException ()
    {
        try
        {
            reference.sayHello();
            fail ("Should have thrown an exception");
        }
        catch (NO_PERMISSION e)
        {
            // Succeed
        }
    }

    @Test
    public void testSlotReceiveReply ()
    {
        reference.sayHello();
    }
}
