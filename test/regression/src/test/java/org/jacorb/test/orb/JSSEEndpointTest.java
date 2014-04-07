package org.jacorb.test.orb;

import static org.junit.Assert.assertTrue;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Properties;
import org.jacorb.orb.ParsedIOR;
import org.jacorb.orb.etf.ProfileBase;
import org.jacorb.orb.iiop.IIOPAddress;
import org.jacorb.test.BasicServer;
import org.jacorb.test.BasicServerHelper;
import org.jacorb.test.common.ClientServerSetup;
import org.jacorb.test.common.ClientServerTestCase;
import org.jacorb.test.common.CommonSetup;
import org.jacorb.test.common.TestUtils;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
import org.omg.ETF.Profile;
import org.omg.IOP.TAG_ALTERNATE_IIOP_ADDRESS;
import org.omg.IOP.TAG_CODE_SETS;
import org.omg.IOP.TaggedComponent;

/**
 * Verify startup using server props and endpoint.
 */
@RunWith(Parameterized.class)
public class JSSEEndpointTest extends ClientServerTestCase
{
    @Rule
    public Timeout testTimeout = new Timeout(30000);

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
                { "-ORBListenEndpoints", "'iiop://localhost:45678'" },
                { "-ORBListenEndpoints", "'iiop://:45678'" }
        } );
    }

    @Test
    public void jsseEndpoint() throws Exception
    {
        Properties clientProps = CommonSetup.loadSSLProps("jsse_client_props", "jsse_client_ks");
        Properties serverProps = CommonSetup.loadSSLProps("jsse_server_props", "jsse_server_ks");

        String [] orbargs = new String [] { key, value };

        setup = new ClientServerSetup(null, "org.jacorb.test.orb.BasicServerImpl", orbargs, clientProps, serverProps);

        server = BasicServerHelper.narrow( setup.getServerObject() );

        server.ping ();
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
}
