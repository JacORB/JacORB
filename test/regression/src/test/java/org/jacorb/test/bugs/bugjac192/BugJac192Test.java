package org.jacorb.test.bugs.bugjac192;

import static org.junit.Assert.assertTrue;
import java.util.Properties;
import org.jacorb.test.harness.ClientServerSetup;
import org.jacorb.test.harness.ClientServerTestCase;
import org.jacorb.test.harness.TestUtils;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * <code>TestCaseImpl</code> tests that the context key system works even if
 * the ORB times out client and/or server side.
 *
 * @author Nick Cross
 */
public class BugJac192Test extends ClientServerTestCase
{
    /**
     * <code>svcID</code> is the service context ID used by the interceptors.
     */
    public static final int svcID = 192;


    /**
     * <code>server</code> is the server reference.
     */
    private JAC192 server;


    /**
     * <code>TestCaseImpl</code> constructor for the suite.
     *
     * @param name a <code>String</code> value
     * @param setup a <code>ClientServerSetup</code> value
     */

    /**
     * <code>setUp</code> for junit.
     *
     * @exception Exception if an error occurs
     */
    @Before
    public void setUp() throws Exception
    {
        server = JAC192Helper.narrow( setup.getServerObject() );
    }

    @BeforeClass
    public static void beforeClassSetUp() throws Exception
    {
        Assume.assumeFalse(TestUtils.isSSLEnabled);

        Properties client_props = new Properties();
        Properties server_props = new Properties();

        client_props.put("org.omg.PortableInterceptor.ORBInitializerClass.CInitializer",
                         "org.jacorb.test.bugs.bugjac192.CInitializer");
        client_props.put
        (
            "jacorb.transport.factories",
            "org.jacorb.orb.iiop.IIOPFactories,org.jacorb.test.orb.etf.wiop.WIOPFactories"
        );

        server_props.put("org.omg.PortableInterceptor.ORBInitializerClass.SInitializer",
                         "org.jacorb.test.bugs.bugjac192.SInitializer");
        server_props.put
        (
            "jacorb.transport.factories",
            "org.jacorb.orb.iiop.IIOPFactories,org.jacorb.test.orb.etf.wiop.WIOPFactories"
        );

        setup = new ClientServerSetup
        (
            "org.jacorb.test.bugs.bugjac192.JAC192Impl",
            client_props,
            server_props
        );
    }


    /**
     * <code>test_contexts</code> tests that .
     */
    @Test
    public void test_contexts()
    {
        assertTrue("Failure when propagating service context.", server.test192Op());
    }
}
