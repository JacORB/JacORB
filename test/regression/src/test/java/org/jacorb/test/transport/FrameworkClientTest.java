package org.jacorb.test.transport;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import java.util.Properties;
import org.jacorb.test.common.ORBTestCase;
import org.jacorb.test.orb.transport.CurrentServer;
import org.jacorb.test.orb.transport.CurrentServerHelper;
import org.jacorb.transport.Current;
import org.jacorb.transport.CurrentHelper;
import org.jacorb.transport.NoContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.omg.CORBA.ORB;
import org.omg.CORBA.Object;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;

/**
 * FrameworkClientTest.java
 *
 * Tests for corect operation of the Transport Current framework
 */

public class FrameworkClientTest extends ORBTestCase
{
    private ORB server_orb_;
    private CurrentServer server_ = null;
    private Current transport_current_ = null;

    @Override
    protected void patchORBProperties (Properties props)
    {
        props.put ("org.omg.PortableInterceptor.ORBInitializerClass.client_transport_current_interceptor",
                "org.jacorb.transport.TransportCurrentInitializer");

        props.put ("org.omg.PortableInterceptor.ORBInitializerClass.client_test_interceptor",
                "org.jacorb.test.transport.DefaultClientOrbInitializer");
    }


    @Before
    public void setUp() throws Exception
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
        server_ = CurrentServerHelper.narrow(orb.string_to_object(objString));
        Object tcobject = orb.resolve_initial_references ("JacOrbTransportCurrent");
        transport_current_ = CurrentHelper.narrow (tcobject);

    }

    @After
    public void tearDown() throws Exception
    {
        server_orb_.shutdown(true);
    }

    @Test
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

    @Test
    public void testInContext() throws Exception {

        // verify in-context access
        server_.invoked_by_client ();
        assertEquals ("Two interceptions per invocation expected", 2, ClientInterceptor.interceptions ());
    }
}
