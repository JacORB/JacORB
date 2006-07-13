package org.jacorb.test.bugs.bugjac192;

import java.util.Properties;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.jacorb.test.common.ClientServerSetup;
import org.jacorb.test.common.ClientServerTestCase;
import org.jacorb.test.common.TestUtils;
import org.omg.CORBA.ORB;

/**
 * <code>TestCaseImpl</code> tests that the context key system works even if
 * the ORB times out client and/or server side.
 *
 * @author Nick Cross
 * @version $Id$
 */
public class BugJac192Test extends ClientServerTestCase
{
    /**
     * <code>svcID</code> is the service context ID used by the interceptors.
     */
    public static final int svcID = 192;


    /**
     * <code>serverOrb</code> is a reference to the server ORB.
     */
    static ORB serverOrb;


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
    public BugJac192Test(String name, ClientServerSetup setup)
    {
        super(name, setup);
    }


    /**
     * <code>setUp</code> for junit.
     *
     * @exception Exception if an error occurs
     */
    public void setUp() throws Exception
    {
        server = JAC192Helper.narrow( setup.getServerObject() );
    }

    protected void tearDown() throws Exception
    {
        if (serverOrb != null)
        {
            serverOrb.shutdown(true);
        }
    }

    /**
     * <code>suite</code> initialise the tests with the correct environment.
     *
     * @return a <code>Test</code> value
     */
    public static Test suite()
    {
        TestSuite suite = new TestSuite( "Jac192 - Context Key test" );

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
        server_props.setProperty("jacorb.regression.disable_security", "true");

        ClientServerSetup setup = new ClientServerSetup
        (
            suite,
            "org.jacorb.test.bugs.bugjac192.JAC192Impl",
            client_props,
            server_props
        );

        TestUtils.addToSuite(suite, setup, BugJac192Test.class);

        return setup;
    }


    /**
     * <code>test_contexts</code> tests that .
     */
    public void test_contexts()
    {
        assertTrue("Failure when propagating service context.", server.test192Op());
    }
}
