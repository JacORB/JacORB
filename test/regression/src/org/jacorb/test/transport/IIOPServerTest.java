package org.jacorb.test.transport;

import java.util.Properties;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.jacorb.test.orb.transport.CurrentServer;
import org.jacorb.test.orb.transport.CurrentServerHelper;
import org.omg.CORBA.ORB;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;
import org.omg.PortableServer.Servant;

public class IIOPServerTest extends TestCase
{
    private ORB clientORB;
    private ORB serverORB;
    private CurrentServer server_;

    protected void setUp() throws Exception
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

        serverORB = ORB.init(new String[0], serverProps);

        POA rootPOA = POAHelper.narrow(serverORB.resolve_initial_references("RootPOA"));
        rootPOA.the_POAManager().activate();
        
        Servant si = new CurrentServerImpl(serverORB, new IIOPTester());
        org.omg.CORBA.Object obj = rootPOA.servant_to_reference(si);
        String objString = serverORB.object_to_string(obj);

        clientORB = ORB.init(new String[0], clientProps);
        server_ = CurrentServerHelper.narrow(clientORB.string_to_object(objString));

        new Thread()
        {
            public void run() {
                serverORB.run();
            };
        }.start();
    }

    protected void tearDown() throws Exception
    {
        serverORB.shutdown(true);
        Thread.sleep(1000);
        clientORB.shutdown(true);
    }

    public void testServerSideInterceptions() throws Exception
    {
        server_.invoked_by_client();
        assertEquals("Three interceptions required on the server side", 6, ServerInterceptor.interceptions());
        assertEquals("Unexpected failures", 0, ServerInterceptor.failures());
    }

    public static Test suite() {
        return new TestSuite(IIOPServerTest.class);
    }

}
