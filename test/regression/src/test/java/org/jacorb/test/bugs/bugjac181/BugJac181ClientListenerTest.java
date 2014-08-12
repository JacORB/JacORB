package org.jacorb.test.bugs.bugjac181;

import static org.junit.Assert.assertTrue;
import java.util.Properties;
import org.jacorb.orb.factory.SocketFactoryManager;
import org.jacorb.test.harness.ClientServerSetup;
import org.jacorb.test.harness.ClientServerTestCase;
import org.jacorb.test.harness.TestUtils;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class BugJac181ClientListenerTest extends ClientServerTestCase
{
    /**
     * <code>server</code> is the server reference.
     */
    protected JAC181 server;

    /**
     * <code>setUp</code> sets up this test.
     *
     * @exception Exception if an error occurs
     */
    @Before
    public void setUp() throws Exception
    {
        TCPListener.reset();

        server = JAC181Helper.narrow( setup.getServerObject() );
    }

    @BeforeClass
    public static void beforeClassSetUp() throws Exception
    {
        Assume.assumeFalse(TestUtils.isSSLEnabled);

        Properties client_props = new Properties();
        Properties server_props = new Properties();

        client_props.setProperty (SocketFactoryManager.TCP_LISTENER, TCPListener.class.getName());

        setup = new ClientServerSetup
        (
                "org.jacorb.test.bugs.bugjac181.JAC181Impl",
                client_props,
                server_props
        );
    }

    /**
     * <code>test_client_listener</code> tests for Client listener actions.
     *
     */
    @Test
    public void test_client_listener() throws Exception
    {
        server.ping1();
        // Wait for preceeding call to finish its thread.
        Thread.sleep(1000);

        assertTrue ("No open message from listener", TCPListener.isListenerOpen());
        assertTrue ("No close message from listener", TCPListener.isListenerClose());
        assertTrue(TCPListener.isEventOfCorrectType());
    }
}
