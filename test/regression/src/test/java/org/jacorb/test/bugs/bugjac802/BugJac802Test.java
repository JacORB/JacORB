package org.jacorb.test.bugs.bugjac802;

import java.util.Arrays;
import java.util.Collection;
import java.util.Properties;
import org.jacorb.test.BasicServer;
import org.jacorb.test.BasicServerHelper;
import org.jacorb.test.harness.ClientServerSetup;
import org.jacorb.test.harness.ClientServerTestCase;
import org.jacorb.test.harness.CommonSetup;
import org.jacorb.test.harness.TestUtils;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class BugJac802Test extends ClientServerTestCase
{
    private BasicServer server;

    @Parameter
    public String serverOptions;

    @Parameter(value = 1)
    public String clientOptions;

    @Parameters
    public static Collection<Object[]> data()
    {
        return Arrays.asList(new Object [][] {
                { "20", "20" },
                { "40", "40" },
                { "20", "60" },
                { "40", "60" },
                { "60", "60" },
// Currently this fails in both the ssl demo and this test. Is it valid?
//                { "60", "20" },
                { "60", "40" },
                { "1", "1" }
               } );
    }

    @BeforeClass
    public static void beforeClassSetUp() throws Exception
    {
        Assume.assumeTrue(TestUtils.isSSLEnabled);
    }

    @Before
    public void setUp() throws Exception
    {
        Properties clientProperties = new Properties ();
        clientProperties.putAll(CommonSetup.loadSSLProps("jsse_client_props", "jsse_client_ks"));
        clientProperties.put ("jacorb.security.ssl.client.supported_options", clientOptions);
        clientProperties.put ("jacorb.security.ssl.client.required_options", clientOptions);

        Properties serverProperties = new Properties ();
        serverProperties.putAll(CommonSetup.loadSSLProps("jsse_server_props", "jsse_server_ks"));
        serverProperties.put ("jacorb.security.ssl.server.supported_options", serverOptions);
        serverProperties.put ("jacorb.security.ssl.server.required_options", serverOptions);

        setup = new ClientServerSetup("org.jacorb.test.orb.BasicServerImpl", clientProperties, serverProperties);

        server = BasicServerHelper.narrow( setup.getServerObject() );
    }

    @After
    public void tearDown() throws Exception
    {
        server._release();
        setup.tearDown();
    }

    @Test
    public void test_ping()
    {
        server.ping();
    }
}


