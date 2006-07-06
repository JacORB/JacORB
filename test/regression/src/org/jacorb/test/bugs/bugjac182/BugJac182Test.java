package org.jacorb.test.bugs.bugjac182;

import java.io.IOException;
import java.util.Properties;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.jacorb.test.common.ClientServerSetup;
import org.jacorb.test.common.ClientServerTestCase;
import org.jacorb.test.common.TestUtils;

/**
 * tests that the context key system works even if
 * the ORB times out client and/or server side.
 *
 * @author Nick Cross
 * @version $Id$
 */
public class BugJac182Test extends ClientServerTestCase
{
    /**
     * <code>svcID</code> is the service context ID used by the interceptors.
     */
    public static final int svcID = 182;



    /**
     * <code>server</code> is the server reference.
     */
    private JAC182 server;


    /**
     * <code>TestCaseImpl</code> constructor for the suite.
     *
     * @param name a <code>String</code> value
     * @param setup a <code>ClientServerSetup</code> value
     */
    public BugJac182Test(String name, ClientServerSetup setup)
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
        server = JAC182Helper.narrow( setup.getServerObject() );
    }

    /**
     * <code>suite</code> initialise the tests with the correct environment.
     */
    public static Test suite()
    {
        TestSuite suite = new TestSuite( "Jac182 - Context Key test" );

        Properties client_props = new Properties();
        Properties server_props = new Properties();

        client_props.put("org.omg.PortableInterceptor.ORBInitializerClass.CInitializer",
                         "org.jacorb.test.bugs.bugjac182.CInitializer");
        // This is only used by test_local. As that test instantiates a local
        // server, we need to, in this JVM, initialise the server interceptors.
        client_props.put("org.omg.PortableInterceptor.ORBInitializerClass.SInitializer",
                         "org.jacorb.test.bugs.bugjac182.SInitializer");

        server_props.put("org.omg.PortableInterceptor.ORBInitializerClass.SInitializer",
                         "org.jacorb.test.bugs.bugjac182.SInitializer");

        ClientServerSetup setup =
            new ClientServerSetup(suite,
                    "org.jacorb.test.bugs.bugjac182.JAC182Impl",
                    client_props,
                    server_props)
            {
                public String getTestServerMain()
                {
                    return BugJac182TestServerRunner.class.getName();
                }

                // override to do nothing!
                protected void initSecurity() throws IOException
                {
                }
            };

        TestUtils.addToSuite(suite, setup, BugJac182Test.class);

        return setup;
    }

    /**
     * <code>test_notlocal</code> tests whether an object in an interceptor is local.
     */
    public void test_notlocal()
    {
        assertFalse("Object was determined to be local", server.test182Op());
    }

    /**
     * <code>test_local</code> tests whether an object in an interceptor is not local.
     * It does this by creating an object locally.
     */
    public void test_local() throws Exception
    {
        JAC182Impl localServant = new JAC182Impl(setup.getClientOrb());
        byte []oid = setup.getClientRootPOA().servant_to_id(localServant);
        org.omg.CORBA.Object obj = setup.getClientRootPOA().id_to_reference(oid);
        JAC182 localServer = JAC182Helper.narrow (obj);

        assertTrue("Object was determined not to be local", localServer.test182Op());
    }
}
