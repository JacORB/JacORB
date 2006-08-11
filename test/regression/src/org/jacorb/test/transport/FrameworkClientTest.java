package org.jacorb.test.transport;

import java.util.Properties;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.jacorb.test.common.ClientServerSetup;
import org.jacorb.test.common.ClientServerTestCase;
import org.jacorb.test.common.TestUtils;
import org.jacorb.test.orb.transport.CurrentServer;
import org.jacorb.test.orb.transport.CurrentServerHelper;
import org.jacorb.transport.Current;
import org.jacorb.transport.CurrentHelper;
import org.jacorb.transport.NoContext;

/**
 * FrameworkClientTest.java
 * 
 * Tests for corect operation of the Transport Current framework
 */

public class FrameworkClientTest extends ClientServerTestCase /* CallbackTestCase */{

    protected CurrentServer server = null;

    protected Current transport_current_ = null;

    public static Test suite() {

        TestSuite suite = new TestSuite ("TransportCurrent");
        ClientServerSetup setup = new ClientServerSetup (suite,
                                                         "org.jacorb.test.transport.CurrentServerImpl",
                                                         getClientProperties (),
                                                         null);

        TestUtils.addToSuite (suite, setup, FrameworkClientTest.class);

        return setup;
    }


    // Client-side ORB configuration
    public static Properties getClientProperties() {

        Properties cp = new Properties ();
        cp.put ("org.omg.PortableInterceptor.ORBInitializerClass.client_transport_current_interceptor",
                "org.jacorb.transport.TransportCurrentInitializer");

        cp.put ("org.omg.PortableInterceptor.ORBInitializerClass.client_test_interceptor",
                "org.jacorb.test.transport.DefaultClientOrbInitializer");
        return cp;
    }

    public FrameworkClientTest(String name, ClientServerSetup setup) {

        super (name, setup);
 
    }

    protected void setUp() throws Exception {
        
        server = CurrentServerHelper.narrow (setup.getServerObject ());


        Object tcobject = setup.getClientOrb ()
                               .resolve_initial_references ("JacOrbTransportCurrent");
        transport_current_ = CurrentHelper.narrow (tcobject);
        
        ClientInterceptor.interceptions (0);
//        super.setUp ();

    }
    
//    protected void tearDown() throws Exception {
//        ClientInterceptor.interceptions (0);
////        super.tearDown ();
//    }


    // tests

    
    public void testOutOfContext() throws Exception {

        // verify out of context access
        try {
            transport_current_.id();
            fail ("Expected NoContext exception was not thrown");
        }
        catch (NoContext ex) {
            // The exception is expected, since we're trying to obtain
            // Transport Traits outside of the defined context
        }
    }

    public void testInContext() throws Exception {

        // verify in-context access
        server.invoked_by_client ();
        assertEquals ("Two interceptions per invocation expected", 2, ClientInterceptor.interceptions ());
    }
}
