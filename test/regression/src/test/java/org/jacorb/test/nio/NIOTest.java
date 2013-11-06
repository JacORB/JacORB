package org.jacorb.test.nio;

import static org.junit.Assert.fail;
import java.util.Properties;
import org.jacorb.test.TestIf;
import org.jacorb.test.TestIfHelper;
import org.jacorb.test.common.ClientServerSetup;
import org.jacorb.test.common.ClientServerTestCase;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;
import org.omg.CORBA.Any;
import org.omg.CORBA.INV_OBJREF;
import org.omg.CORBA.ORB;
import org.omg.CORBA.Policy;
import org.omg.CORBA.SetOverrideType;
import org.omg.CORBA.TIMEOUT;
import org.omg.Messaging.RELATIVE_RT_TIMEOUT_POLICY_TYPE;

/**
 * FrameworkClientTest.java
 *
 * Tests for the use of non-blocking connections
 */

public class NIOTest extends ClientServerTestCase
{
    public static final int MSEC_FACTOR = 10000;
    public static final int ONE_SECOND = 1000 * MSEC_FACTOR;

    private String nioTestURL = "corbaloc::localhost:6969/NIOTestServer/thePOA/ObjectID";

    protected TestIf server = null;

    @BeforeClass
    public static void beforeClassSetUp() throws Exception
    {
        Properties props = new Properties ();
        props.setProperty ("jacorb.connection.nonblocking", "on");
        props.setProperty ("jacorb.log.default.verbosity", "4");

        setup = new ClientServerSetup(NIOTestServer.class.getName(),
                                   TestIf.class.getName(), props, props);

        // NIO doesn't yet support SSL
        Assume.assumeFalse(setup.isSSLEnabled());
    }

    @Test
    public void testNIO() throws Exception
    {
        ORB orb = setup.getClientOrb();
        org.omg.CORBA.Object ref = orb.string_to_object( nioTestURL );

        Any rrtPolicyAny = orb.create_any();
        rrtPolicyAny.insert_ulonglong (ONE_SECOND*2);

        Policy policies[] = new Policy[1];
        policies[0] =
            orb.create_policy (RELATIVE_RT_TIMEOUT_POLICY_TYPE.value,
                               rrtPolicyAny);

        server = TestIfHelper.narrow(ref._set_policy_override (policies, SetOverrideType.ADD_OVERRIDE));

        try
        {
           server.op();
           fail ("Should have gotten a timeout exception");
        }
        catch (INV_OBJREF e)
        {
            fail("Unable to narrow due to no Group IIOP Profile");
        }
        catch (TIMEOUT e)
        {
            // as expected
        }

    }


}
