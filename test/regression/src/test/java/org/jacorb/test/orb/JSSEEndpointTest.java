package org.jacorb.test.orb;

import static org.junit.Assert.assertTrue;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import org.jacorb.orb.ParsedIOR;
import org.jacorb.orb.iiop.IIOPAddress;
import org.jacorb.orb.iiop.IIOPProfile;
import org.jacorb.test.BasicServer;
import org.jacorb.test.BasicServerHelper;
import org.jacorb.test.harness.ClientServerSetup;
import org.jacorb.test.harness.CommonSetup;
import org.jacorb.test.harness.FixedPortClientServerTestCase;
import org.jacorb.test.harness.TestUtils;
import org.junit.After;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
import org.omg.CORBA.BAD_PARAM;
import org.omg.CORBA.ORB;
import org.omg.ETF.Profile;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;

/**
 * Verify startup using server props and endpoint.
 */
@RunWith(Parameterized.class)
public class JSSEEndpointTest extends FixedPortClientServerTestCase
{
    @Rule
    public Timeout testTimeout = new Timeout(10000);

    @BeforeClass
    public static void beforeClassSetUp() throws Exception
    {
        Assume.assumeFalse(TestUtils.isSSLEnabled);
    }

    private BasicServer server;

    @Parameter
    public String key;

    @Parameter(value = 1)
    public String value;

    @Parameters
    public static Collection<Object[]> data()
    {
        return Arrays.asList(new Object [][] {
                { "-ORBListenEndpoints", "'iiop://localhost:" + getNextAvailablePort() + "'" },
                { "-ORBListenEndpoints", "'iiop://:" + getNextAvailablePort() + "'" },
                // Test that 0.0.0.0 does not cause a null pointer.
                { "-ORBListenEndpoints", "'iiop://0.0.0.0:" + getNextAvailablePort() + "'" },
                { "-ORBListenEndpoints", "'iiop://:" + getNextAvailablePort() + "/ssl_port=" + getNextAvailablePort() + "'" },
                { "-ORBListenEndpoints", "'ssliiop://localhost:" + getNextAvailablePort() + "'" }

        } );
    }

    @Test
    public void jsseEndpoint() throws Exception
    {
        Properties clientProps = CommonSetup.loadSSLProps("jsse_client_props", "jsse_client_ks");
        Properties serverProps = CommonSetup.loadSSLProps("jsse_server_props", "jsse_server_ks");

        String [] orbargs = new String [] { key, value };

        setup = new ClientServerSetup("org.jacorb.test.orb.JSSEEndpointTest", "org.jacorb.test.orb.BasicServerImpl", orbargs, clientProps, serverProps);

        server = BasicServerHelper.narrow( setup.getServerObject() );

        server.ping ();

        ParsedIOR p = new ParsedIOR (setup.getORB(), setup.getServerIOR());
        List<Profile> profiles = p.getProfiles();
        String host = ((IIOPAddress)((IIOPProfile)profiles.get(0)).getAddress()).getOriginalHost();
        assertTrue ("Host must not be blank", host.length () > 0);
    }

    @Test
    public void standardEndpoint() throws Exception
    {
        String [] orbargs = new String [] { key, value };

        setup = new ClientServerSetup("org.jacorb.test.orb.JSSEEndpointTest", "org.jacorb.test.orb.BasicServerImpl", orbargs, null, null);

        server = BasicServerHelper.narrow( setup.getServerObject() );

        server.ping ();

        ParsedIOR p = new ParsedIOR (setup.getORB(), setup.getServerIOR());
        List<Profile> profiles = p.getProfiles();
        String host = ((IIOPAddress)((IIOPProfile)profiles.get(0)).getAddress()).getOriginalHost();
        assertTrue ("Host must not be blank", host.length () > 0);
    }

    @After
    public void tearDown() throws Exception
    {
        if (setup != null)
        {
            setup.tearDown();
            server._release();
        }
    }


    public static void main(String[] args) throws Exception
    {
        ORB orb;
        try
        {
            //init ORB
            orb = ORB.init( args, null );
        }
        catch (BAD_PARAM e)
        {
            TestUtils.getLogger ().debug ("Successfully caught BAD_PARAM " , e);

            // Just create it without any arguments to avoid the init issue.
            orb = ORB.init (new String[]{}, null);
        }

        //init POA
        POA rootPOA = POAHelper.narrow( orb.resolve_initial_references( "RootPOA" ));
        rootPOA.the_POAManager().activate();

        BasicServerImpl servant = new BasicServerImpl();

        rootPOA.activate_object(servant);

        BasicServer server = BasicServerHelper.narrow(rootPOA.servant_to_reference(servant));

        System.out.println ("SERVER IOR: " + orb.object_to_string(server));
        System.out.flush();

        orb.run();
    }
}
