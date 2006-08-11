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

/**
 * FrameworkClientTest.java
 * 
 * Tests for corect operation of the Transport Current framework
 */

public class IIOPClientTest extends ClientServerTestCase /* CallbackTestCase */{

    protected CurrentServer server = null;

    protected Current transport_current_ = null;

    public static Test suite() {

        TestSuite suite = new TestSuite ("IIOPClientTest");

        Properties cp = new Properties ();
        cp.put ("org.omg.PortableInterceptor.ORBInitializerClass.client_transport_current_interceptor",
                "org.jacorb.transport.TransportCurrentInitializer");

        cp.put ("org.omg.PortableInterceptor.ORBInitializerClass.client_transport_current_iiop_interceptor",
                "org.jacorb.transport.IIOPTransportCurrentInitializer");

        cp.put ("org.omg.PortableInterceptor.ORBInitializerClass.client_test_interceptor",
                "org.jacorb.test.transport.IIOPClientOrbInitializer");

        ClientServerSetup setup = new ClientServerSetup (suite,
                                                         "org.jacorb.test.transport.CurrentServerImpl",
                                                         cp,
                                                         null);

        TestUtils.addToSuite (suite, setup, IIOPClientTest.class);

        return setup;
    }

    public IIOPClientTest(String name, ClientServerSetup setup) {

        super (name, setup);
    }

    protected void setUp() throws Exception {

        server = CurrentServerHelper.narrow (setup.getServerObject ());

        Object tcobject = setup.getClientOrb ()
                               .resolve_initial_references ("JacOrbIIOPTransportCurrent");
        transport_current_ = CurrentHelper.narrow (tcobject);

        ClientInterceptor.interceptions (0);

    }


    public void testInContext() throws Exception {

        // verify in-context access
        server.invoked_by_client ();
        assertEquals ("Two interceptions per invocation expected", 2, ClientInterceptor.interceptions ());
 
    }

    
}
