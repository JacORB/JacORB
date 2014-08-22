package org.jacorb.test.bugs.bug832;

import java.util.Properties;
import org.jacorb.test.bugs.bugjac182.JAC182;
import org.jacorb.test.bugs.bugjac182.JAC182Helper;
import org.jacorb.test.harness.ClientServerSetup;
import org.jacorb.test.harness.ClientServerTestCase;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;


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
    /**
     * <code>setUp</code> for junit.
     *
     * @exception Exception if an error occurs
     */
    @Before
    public void setUp() throws Exception
    {
        server = JAC182Helper.narrow( setup.getServerObject() );
    }

    /**
     * <code>suite</code> initialise the tests with the correct environment.
     */
    @BeforeClass
    public static void beforeClassSetUp() throws Exception
    {
        Properties client_props = new Properties();
        Properties server_props = new Properties();

        server_props.put("org.omg.PortableInterceptor.ORBInitializerClass.SInitializer",
                         "org.jacorb.test.bugs.bug832.SInitializer");
        // Set this on server side so we have a valid policy factory to retrieve on the test call.
        server_props.put("org.omg.PortableInterceptor.ORBInitializerClass.bidir_init",
                         "org.jacorb.orb.giop.BiDirConnectionInitializer" );


        setup = new ClientServerSetup(
                              Bug832TestServerRunner.class.getName(),
                              "org.jacorb.test.bugs.bug832.Bug832Impl",
                              client_props,
                              server_props);
    }

    @Test
    public void test_interceptornonretaindefaultservant()
    {
        server.test182Op();
    }


    @Test
    public void test_interceptornonretaindservantmanager()
    {
        server.test182Op();
    }
}
