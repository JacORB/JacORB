package org.jacorb.test.bugs.bugjac670;

import java.util.Properties;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.jacorb.test.common.ClientServerSetup;
import org.jacorb.test.common.ClientServerTestCase;
import org.jacorb.test.common.ServerSetup;
import org.jacorb.test.common.TestUtils;
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
public class BugJac670Test extends ClientServerTestCase
{
    private GreetingService server = null;

    private ServerSetup serverSetUp;

    public BugJac670Test (String name, ClientServerSetup setup)
    {
        super (name, setup);
    }

    protected void setUp() throws Exception
    {
         server = (GreetingService) GreetingServiceHelper.narrow
             (setup.getClientOrb().resolve_initial_references ("greeting"));

         Properties serverprops = new java.util.Properties();
         serverprops.setProperty( "ORBInitRef.balancer",
                                  "corbaloc::localhost:19000/GSLBService");

         serverprops.setProperty ("jacorb.test.timeout.server", Long.toString(15000));
         serverprops.setProperty("jacorb.test.ssl", "false");

         serverSetUp = new ServerSetup (setup,
                                        "org.jacorb.test.bugs.bugjac670.GreetingServiceServer",
                                        "GreetingServiceImpl",
                                        serverprops);

         serverSetUp.setUp();
    }

    protected void tearDown() throws Exception
    {
        server = null;
        serverSetUp.tearDown();
    }

    public static Test suite()
    {
        TestSuite suite = new TestSuite ("RelativeRTTimeoutPolicy with loadbalancer");

        Properties clientprops = new java.util.Properties();
        clientprops.setProperty( "ORBInitRef.greeting",
                                 "corbaloc::localhost:19000/GSLBService");
        clientprops.setProperty("jacorb.test.ssl", "false");

        Properties serverprops = new java.util.Properties();
        serverprops.setProperty( "org.omg.PortableInterceptor.ORBInitializerClass."
                                 + org.jacorb.test.bugs.bugjac670.GSLoadBalancerInitializer.class.getName(), "" );

        serverprops.setProperty ("jacorb.test.timeout.server", Long.toString(15000));

        serverprops.setProperty( "OAPort",
                                 "19000" );
        serverprops.setProperty("jacorb.test.ssl", "false");

        ClientServerSetup setup =
        new ClientServerSetup
        (suite,
         "org.jacorb.test.bugs.bugjac670.GSLoadBalancerServer",
         "GSLoadBalancerImpl",
         clientprops,
         serverprops
        );

        TestUtils.addToSuite (suite, setup, BugJac670Test.class);

        return setup;
    }

    /**
     * Sets a RelativeRoundtripTimeout which will
     * be met by the invocation.
     */
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
           t.printStackTrace ();
           fail ("Unexpected TIMEOUT");
        }
    }

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
