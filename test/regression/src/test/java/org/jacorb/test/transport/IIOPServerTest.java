package org.jacorb.test.transport;

import static org.junit.Assert.assertEquals;
import java.util.Properties;
import org.jacorb.test.harness.ORBTestCase;
import org.jacorb.test.orb.transport.CurrentServer;
import org.jacorb.test.orb.transport.CurrentServerHelper;
import org.junit.Before;
import org.junit.Test;
import org.omg.CORBA.ORB;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;
import org.omg.PortableServer.Servant;

public class IIOPServerTest extends ORBTestCase
{
    private ORB clientORB;
    private ORB serverORB;
    private CurrentServer server_;

    @Before
    public void setUp() throws Exception
    {
        ServerInterceptor.reset ();

        Properties clientProps = new Properties();

        Properties serverProps = new Properties();

        // We need the TC functionality
        serverProps.put ("org.omg.PortableInterceptor.ORBInitializerClass.server_transport_current_interceptor",
                         "org.jacorb.transport.TransportCurrentInitializer");

        serverProps.put ("org.omg.PortableInterceptor.ORBInitializerClass.server_transport_current_iiop_interceptor",
                        "org.jacorb.transport.IIOPTransportCurrentInitializer");

        // Hook in the test interceptor
        serverProps.put ("org.omg.PortableInterceptor.ORBInitializerClass.server_test_interceptor",
                        "org.jacorb.test.transport.IIOPServerOrbInitializer");

        serverORB = this.getAnotherORB(serverProps);

        POA rootPOA = POAHelper.narrow(serverORB.resolve_initial_references("RootPOA"));
        rootPOA.the_POAManager().activate();

        Servant si = new CurrentServerImpl(serverORB, new IIOPTester());
        org.omg.CORBA.Object obj = rootPOA.servant_to_reference(si);
        String objString = serverORB.object_to_string(obj);

        clientORB = this.getAnotherORB(clientProps);
        server_ = CurrentServerHelper.narrow(clientORB.string_to_object(objString));
    }

    @Test
    public void testServerSideInterceptions() throws Exception
    {
        server_.invoked_by_client();
        assertEquals("Three interceptions required on the server side", 6, ServerInterceptor.interceptions());
        assertEquals("Unexpected failures", 0, ServerInterceptor.failures());
    }
}

