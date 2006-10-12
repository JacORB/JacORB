package org.jacorb.test.transport;

import java.util.Properties;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.jacorb.test.orb.transport.CurrentServer;
import org.jacorb.test.orb.transport.CurrentServerHelper;
import org.jacorb.transport.Current;
import org.jacorb.transport.CurrentHelper;
import org.jacorb.transport.NoContext;
import org.omg.CORBA.ORB;
import org.omg.CORBA.Object;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;

/**
 * FrameworkClientTest.java
 * 
 * Tests for corect operation of the Transport Current framework
 */

public class FrameworkClientTest extends TestCase {

    private ORB client_orb_;
    private ORB server_orb_;
    private CurrentServer server_ = null;
    private Current transport_current_ = null;

    // Client-side ORB configuration
    public static Properties getClientProperties() {

        Properties cp = new Properties ();
        cp.put ("org.omg.PortableInterceptor.ORBInitializerClass.client_transport_current_interceptor",
                "org.jacorb.transport.TransportCurrentInitializer");

        cp.put ("org.omg.PortableInterceptor.ORBInitializerClass.client_test_interceptor",
                "org.jacorb.test.transport.DefaultClientOrbInitializer");
        return cp;
    }

   
    public FrameworkClientTest(String name) {

        super (name);
 
    }

    protected void setUp() throws Exception
    {
        ServerInterceptor.reset ();

        server_orb_ = ORB.init(new String[0], new Properties());

        POA rootPOA = POAHelper.narrow(server_orb_.resolve_initial_references("RootPOA"));
        rootPOA.the_POAManager().activate();
        org.omg.CORBA.Object obj = rootPOA.servant_to_reference(new CurrentServerImpl(server_orb_, null));
        String objString = server_orb_.object_to_string(obj);

        new Thread()
        {
            public void run() {
                server_orb_.run();
            };
        }.start();
        
        Thread.sleep(1000);
        client_orb_ = ORB.init(new String[0], getClientProperties());

        server_ = CurrentServerHelper.narrow(client_orb_.string_to_object(objString));
        Object tcobject = client_orb_.resolve_initial_references ("JacOrbTransportCurrent");
        transport_current_ = CurrentHelper.narrow (tcobject);

    }

    protected void tearDown() throws Exception
    {
        server_orb_.shutdown(true);
        Thread.sleep(1000);
        client_orb_.shutdown(true);
    }

    public static Test suite() {
        return new TestSuite(FrameworkClientTest.class);
    }

    
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
        server_.invoked_by_client ();
        assertEquals ("Two interceptions per invocation expected", 2, ClientInterceptor.interceptions ());
    }
}
