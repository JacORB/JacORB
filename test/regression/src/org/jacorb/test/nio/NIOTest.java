package org.jacorb.test.nio;

import java.util.Properties;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.jacorb.test.common.ClientServerSetup;
import org.jacorb.test.common.ClientServerTestCase;
import org.jacorb.test.common.TestUtils;
import org.omg.CORBA.INV_OBJREF;
import org.omg.CORBA.TIMEOUT;
import org.omg.CORBA.ORB;
import org.omg.CORBA.Any;
import org.omg.CORBA.Policy;
import org.omg.Messaging.RELATIVE_RT_TIMEOUT_POLICY_TYPE;

import org.jacorb.test.TestIf;
import org.jacorb.test.TestIfHelper;

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

    public static Test suite()
    {
        TestSuite suite = new TestSuite(NIOTest.class);

        Properties props = new Properties ();
        props.setProperty ("jacorb.connection.nonblocking", "on");
        ClientServerSetup setup = 
            new ClientServerSetup(suite, NIOTestServer.class.getName(), 
                                  TestIf.class.getName(), props, props);
        
        // NIO doesn't yet support SSL 
        if (!setup.isSSLEnabled ())
        {
            TestUtils.addToSuite(suite, setup, NIOTest.class);
        }
        else
        {
            System.err.println("Test ignored as SSL doesn't supported (" + NIOTest.class.getName() + ")");
        }
        return setup;
    }

    public NIOTest(String name, ClientServerSetup setup) 
    {
        super (name, setup);
    }

    private Properties getClientProperties()
    {
        Properties cp = new Properties ();
        cp.put ("org.jacorb.connection.nonblocking","on");
        return cp;
    }

    public void testNIO() throws Exception
    {
        ORB orb = setup.getClientOrb();
        //String ior = setup.getServerIOR();
        //System.out.println ("getServerIOR returned " + ior);
        org.omg.CORBA.Object ref = orb.string_to_object( nioTestURL );
        // Use an unchecked narrow so it doesn't do an is_a call remotely.
        server = TestIfHelper.narrow(ref);

        Any rrtPolicyAny = orb.create_any();
        rrtPolicyAny.insert_ulonglong (ONE_SECOND*2);
 
        Policy rrtPolicy =
            orb.create_policy (RELATIVE_RT_TIMEOUT_POLICY_TYPE.value,
                               rrtPolicyAny);


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
