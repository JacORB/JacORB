package org.jacorb.test.bugs.bugjac181;

import java.util.Properties;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.jacorb.orb.factory.SocketFactoryManager;
import org.jacorb.test.common.ClientServerSetup;
import org.jacorb.test.common.ClientServerTestCase;
import org.jacorb.test.common.TestUtils;

public class BugJac181ClientListenerTest extends ClientServerTestCase
{
    /**
     * <code>server</code> is the server reference.
     */
    protected JAC181 server;

    public BugJac181ClientListenerTest(String name, ClientServerSetup setup)
    {
        super(name, setup);
    }

    /**
     * <code>setUp</code> sets up this test.
     *
     * @exception Exception if an error occurs
     */
    public void setUp() throws Exception
    {
        TCPListener.reset();

        server = JAC181Helper.narrow( setup.getServerObject() );
    }

    protected void tearDown() throws Exception
    {
        server = null;
    }

    public static Test suite()
    {
        TestSuite suite = new TestSuite();

        Properties client_props = new Properties();
        Properties server_props = new Properties();

        client_props.put("jacorb.regression.disable_security", "true");
        server_props.put("jacorb.regression.disable_security", "true");

        client_props.setProperty (SocketFactoryManager.TCP_LISTENER, TCPListener.class.getName());

        ClientServerSetup setup = new ClientServerSetup
        (
                suite,
                "org.jacorb.test.bugs.bugjac181.JAC181Impl",
                client_props,
                server_props
        );

        TestUtils.addToSuite(suite, setup, BugJac181ClientListenerTest.class);

        return setup;
    }

    /**
     * <code>test_client_listener</code> tests for Client listener actions.
     *
     */
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
