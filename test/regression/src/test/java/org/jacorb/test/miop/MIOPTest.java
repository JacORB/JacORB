package org.jacorb.test.miop;

import static org.junit.Assert.fail;
import java.util.Properties;
import org.jacorb.test.harness.ClientServerSetup;
import org.jacorb.test.harness.ClientServerTestCase;
import org.jacorb.test.harness.TestUtils;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;
import org.omg.CORBA.INV_OBJREF;
import org.omg.CORBA.ORB;

public class MIOPTest extends ClientServerTestCase
{
    private String miopURL   = "corbaloc:miop:1.0@1.0-TestDomain-1/224.1.239.2:1234";

    private GreetingService server;

    @BeforeClass
    public static void beforeClassSetUp() throws Exception
    {
        Assume.assumeFalse(TestUtils.isSSLEnabled);

        Properties props = new Properties ();
        props.setProperty
            ("jacorb.transport.factories", "org.jacorb.orb.iiop.IIOPFactories,org.jacorb.orb.miop.MIOPFactories");
        props.setProperty
            ("jacorb.transport.client.selector", "org.jacorb.orb.miop.MIOPProfileSelector");

        setup = new ClientServerSetup(MIOPTestServer.class.getName(), GreetingImpl.class.getName(), props, props);
    }

    @Test
    public void testMIOP() throws InterruptedException
    {
        ORB orb = setup.getClientOrb();
        String ior = setup.getServerIOR();
        org.omg.CORBA.Object ref = orb.string_to_object( ior );
        // Use an unchecked narrow so it doesn't do an is_a call remotely.
        server = GreetingServiceHelper.unchecked_narrow(orb.string_to_object(miopURL));

        String s = "Oneway call";
        server.greeting_oneway(s);

        //Wait for the server receives the first request.
        Thread.sleep(1000);

        // A normal narrow should do a remote call. This will need the group IIOP profile which
        // may not have been transmitted so we do this part last.
        try
        {
            server = GreetingServiceHelper.narrow(ref);

           String response = server.greeting_check();
           if(!response.equals(s))
           {
               fail("Wrong response: expected \""+s+"\" received \""+response+"\"");
           }
        }
        catch (INV_OBJREF e)
        {
            fail("Unable to narrow due to no Group IIOP Profile");
        }
    }
}
