package org.jacorb.test.transport;

import static org.junit.Assert.assertEquals;
import java.util.Properties;
import org.jacorb.test.harness.ORBTestCase;
import org.jacorb.test.orb.transport.CurrentServer;
import org.jacorb.test.orb.transport.CurrentServerHelper;
import org.jacorb.transport.Current;
import org.jacorb.transport.CurrentHelper;
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

public class IIOPClientTest extends ORBTestCase
{
    private ORB server_orb_;
    protected CurrentServer server = null;

    protected Current transport_current_ = null;


    @Before
    public void setUp() throws Exception {

        ServerInterceptor.reset ();

        server_orb_ = this.getAnotherORB(null);

        POA rootPOA = POAHelper.narrow(server_orb_.resolve_initial_references("RootPOA"));
        rootPOA.the_POAManager().activate();
        org.omg.CORBA.Object obj = rootPOA.servant_to_reference(new CurrentServerImpl(server_orb_, null));
        String objString = server_orb_.object_to_string(obj);

        new Thread()
        {
            @Override
            public void run() {
                server_orb_.run();
            };
        }.start();


        server = CurrentServerHelper.narrow(orb.string_to_object(objString));

        Object tcobject = orb.resolve_initial_references ("JacOrbIIOPTransportCurrent");
        transport_current_ = CurrentHelper.narrow (tcobject);

        ClientInterceptor.interceptions (0);

    }


    @Override
    protected void patchORBProperties(Properties props) throws Exception
    {
        props.put ("org.omg.PortableInterceptor.ORBInitializerClass.client_transport_current_interceptor",
                "org.jacorb.transport.TransportCurrentInitializer");

        props.put ("org.omg.PortableInterceptor.ORBInitializerClass.client_transport_current_iiop_interceptor",
                "org.jacorb.transport.IIOPTransportCurrentInitializer");

        props.put ("org.omg.PortableInterceptor.ORBInitializerClass.client_test_interceptor",
                "org.jacorb.test.transport.IIOPClientOrbInitializer");
    }

    @Test
    public void testInContext() throws Exception {

        // verify in-context access
        server.invoked_by_client ();
        assertEquals ("Two interceptions per invocation expected", 2, ClientInterceptor.interceptions ());
    }
}
