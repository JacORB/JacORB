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

public class FrameworkServerTest extends ORBTestCase
{
    private ORB clientORB;
    private ORB serverORB;
    private CurrentServer server_;

    @Before
    public void setUp() throws Exception
    {
    	ServerInterceptor.reset ();

        Properties serverProps = new Properties();

        // We need the TC functionality
        serverProps.put ("org.omg.PortableInterceptor.ORBInitializerClass.server_transport_current_interceptor",
                         "org.jacorb.transport.TransportCurrentInitializer");

        // Hook in the test interceptor
        serverProps.put ("org.omg.PortableInterceptor.ORBInitializerClass.server_test_interceptor",
                           "org.jacorb.test.transport.DefaultServerOrbInitializer");

        serverORB = this.getAnotherORB(serverProps);

        POA rootPOA = POAHelper.narrow(serverORB.resolve_initial_references("RootPOA"));
        rootPOA.the_POAManager().activate();
        org.omg.CORBA.Object obj = rootPOA.servant_to_reference(new CurrentServerImpl(serverORB, new DefaultTester()));
        String objString = serverORB.object_to_string(obj);

        Thread.sleep(1000);
        clientORB = this.getAnotherORB(null);
        server_ = CurrentServerHelper.narrow(clientORB.string_to_object(objString));
    }

    @Test
    public void testGenericServer() throws Exception
    {
        server_.invoked_by_client();

        // That's right! Three interceptions per invocation. JacORB has no collocation optimization, so servants
        // performing an invocation must go through the whole invocation stack - i.e. all interceptors will fire.
        assertEquals("Six interceptions required on the server side", 6, ServerInterceptor.interceptions());

        assertEquals("Unexpected failures", 0, ServerInterceptor.failures());
    }
}
