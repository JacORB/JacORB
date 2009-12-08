package org.jacorb.test.bugs.bug832;

import java.util.Properties;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.jacorb.test.bugs.bugjac182.JAC182;
import org.jacorb.test.bugs.bugjac182.JAC182Helper;
import org.jacorb.test.common.ClientServerSetup;
import org.jacorb.test.common.ClientServerTestCase;
import org.jacorb.test.common.CommonSetup;
import org.jacorb.test.common.TestUtils;


public class Bug832Test extends ClientServerTestCase
{
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
    public Bug832Test(String name, ClientServerSetup setup)
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

    protected void tearDown() throws Exception
    {
        server = null;
    }

    /**
     * <code>suite</code> initialise the tests with the correct environment.
     */
    public static Test suite()
    {
        TestSuite suite = new TestSuite( "832 - nonretain test" );

        Properties client_props = new Properties();
        Properties server_props = new Properties();

        client_props.setProperty(CommonSetup.JACORB_REGRESSION_DISABLE_SECURITY, "true");
        server_props.setProperty(CommonSetup.JACORB_REGRESSION_DISABLE_SECURITY, "true");

        server_props.put("org.omg.PortableInterceptor.ORBInitializerClass.SInitializer",
                         "org.jacorb.test.bugs.bug832.SInitializer");
        // Set this on server side so we have a valid policy factory to retrieve on the test call.
        server_props.put("org.omg.PortableInterceptor.ORBInitializerClass.bidir_init",
                         "org.jacorb.orb.giop.BiDirConnectionInitializer" );


        ClientServerSetup setup =
        new ClientServerSetup(suite,
                              Bug832TestServerRunner.class.getName(),
                              "org.jacorb.test.bugs.bug832.Bug832Impl",
                              client_props,
                              server_props);

        TestUtils.addToSuite(suite, setup, Bug832Test.class);

        return setup;
    }

    public void test_interceptornonretaindefaultservant()
    {
        server.test182Op();
    }


    public void test_interceptornonretaindservantmanager()
    {
        server.test182Op();
    }
}
