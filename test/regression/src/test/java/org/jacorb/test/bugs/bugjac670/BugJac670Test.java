package org.jacorb.test.bugs.bugjac670;

import static org.junit.Assert.fail;
import java.util.Properties;
import org.jacorb.test.harness.ClientServerSetup;
import org.jacorb.test.harness.FixedPortClientServerTestCase;
import org.jacorb.test.harness.ServerSetup;
import org.jacorb.test.harness.TestUtils;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.omg.CORBA.Policy;
import org.omg.CORBA.PolicyError;
import org.omg.CORBA.SetOverrideType;
import org.omg.Messaging.RELATIVE_RT_TIMEOUT_POLICY_TYPE;

/**
 * This tests the RealtiveRoundtripTimeoutPolicy where a
 * ForwardRequest is thrown and the forward server subsequently
 * fails with subsequent requests being directed back to the
 * original server.  One test has a long timeout and completes
 * successfully, the other has a shorter timeout and the
 * failover times out.
 */
public class BugJac670Test extends FixedPortClientServerTestCase
{
    private static String port = Integer.toString(getNextAvailablePort());

    private GreetingService server = null;
    private ServerSetup serverSetUp;

    @Before
    public void setUp() throws Exception
    {
         server = GreetingServiceHelper.narrow
             (setup.getClientOrb().resolve_initial_references ("greeting"));

         Properties serverprops = new java.util.Properties();
         serverprops.setProperty( "ORBInitRef.balancer", "corbaloc::localhost:" + port + "/GSLBService");

         serverprops.setProperty ("jacorb.test.timeout.server", Long.toString(15000));

         serverSetUp = new ServerSetup ("org.jacorb.test.bugs.bugjac670.GreetingServiceServer",
                                        "GreetingServiceImpl",
                                        serverprops);

         serverSetUp.setUp();
    }

    @After
    public void tearDown() throws Exception
    {
        server._release();
        serverSetUp.tearDown();
    }

    @BeforeClass
    public static void beforeClassSetUp() throws Exception
    {
        Assume.assumeFalse(TestUtils.isSSLEnabled);

        Properties clientprops = new java.util.Properties();
        clientprops.setProperty( "ORBInitRef.greeting",
                                 "corbaloc::localhost:" + port + "/GSLBService");

        Properties serverprops = new java.util.Properties();
        serverprops.setProperty( "org.omg.PortableInterceptor.ORBInitializerClass."
                                 + org.jacorb.test.bugs.bugjac670.GSLoadBalancerInitializer.class.getName(), "" );
        serverprops.setProperty("org.omg.CORBA.ORBClass", "org.jacorb.orb.ORB");
        serverprops.setProperty("org.omg.CORBA.ORBSingletonClass", "org.jacorb.orb.ORBSingleton");

        serverprops.setProperty ("jacorb.test.timeout.server", Long.toString(15000));

        serverprops.setProperty( "OAPort", port );

        setup = new ClientServerSetup
        (

         "org.jacorb.test.bugs.bugjac670.GSLoadBalancerServer",
         "GSLoadBalancerImpl",
         clientprops,
         serverprops
        );
    }

    /**
     * Sets a RelativeRoundtripTimeout which will
     * be met by the invocation.
     */
    @Test
    public void test_relative_roundtrip_sync_ok()
       throws Exception
    {
        server = clearPolicies (server);
        server = setRelativeRoundtripTimeout (server, TestUtils.isWindows () ? 30000 : 12000);

        try
        {
           int i = 0;

           while (i < 10)
           {
              server.greeting ("Hello");

              try
              {
                 Thread.sleep (3000);
              }
              catch (InterruptedException ie)
              {
              }

              i++;

              if (i == 5)
              {
                 serverSetUp.tearDown ();
                 Thread.sleep (5000);
              }
           }
        }
        catch (org.omg.CORBA.TIMEOUT t)
        {
           fail ("Unexpected TIMEOUT");
        }
    }

    @Test
    public void test_relative_roundtrip_sync_expired()
       throws Exception
    {
        server = clearPolicies (server);
        server = setRelativeRoundtripTimeout (server, 2000);

        try
        {
           int i = 0;

           while (i < 10)
           {
              server.greeting ("Hello");

              try
              {
                 Thread.sleep (3000);
              }
              catch (InterruptedException ie)
              {
              }

              i++;

              if (i == 5)
              {
                 serverSetUp.tearDown ();
              }
           }

           fail ("TIMEOUT expected");
        }
        catch (org.omg.CORBA.TIMEOUT t)
        {
           // OK
        }
    }

    private GreetingService clearPolicies (GreetingService server)
    {
        org.omg.CORBA.Object r = server._set_policy_override (new Policy[]{},
                                                              SetOverrideType.SET_OVERRIDE);

        return GreetingServiceHelper.narrow (r);
    }

    private GreetingService setRelativeRoundtripTimeout (GreetingService server,
                                                        long millis)
    {
        org.omg.CORBA.ORB orb = setup.getClientOrb();
        org.omg.CORBA.Any any = orb.create_any();
        any.insert_ulonglong (millis * 10000);

        try
        {
            Policy policy =
                orb.create_policy (RELATIVE_RT_TIMEOUT_POLICY_TYPE.value, any);

            org.omg.CORBA.Object r = server._set_policy_override (new Policy[]{ policy },
                                                                  SetOverrideType.ADD_OVERRIDE);

            return GreetingServiceHelper.narrow (r);
        }
        catch (PolicyError e)
        {
            throw new RuntimeException ("policy error: " + e);
        }
    }
}
