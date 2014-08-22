package org.jacorb.test.bugs.bugjac181;

import static org.junit.Assert.assertTrue;
import java.util.Properties;
import org.jacorb.orb.factory.SocketFactoryManager;
import org.jacorb.test.harness.ORBTestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.omg.CORBA.ORB;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;

public class BugJac181ServerListenerTest extends ORBTestCase
{
    private ORB clientORB;
    private ORB serverORB;
    private JAC181 server;

    @Before
    public void setUp() throws Exception
    {
        TCPListener.reset();

        Properties clientProps = new Properties();
        clientProps.put("jacorb.iiop.enable_loopback", "off");

        Properties serverProps = new Properties();
        serverProps.put(SocketFactoryManager.TCP_LISTENER, TCPListener.class.getName());
        serverProps.put("jacorb.iiop.enable_loopback", "off");

        clientORB = this.getAnotherORB(clientProps);
        serverORB = this.getAnotherORB(serverProps);

        POA rootPOA = POAHelper.narrow(serverORB.resolve_initial_references("RootPOA"));
        rootPOA.the_POAManager().activate();

        org.omg.CORBA.Object obj = rootPOA.servant_to_reference(new JAC181Impl());
        String objString = serverORB.object_to_string(obj);
        server = JAC181Helper.narrow(clientORB.string_to_object(objString));
    }

    @After
    public void tearDown() throws Exception
    {
        server._release();
        server = null;
        clientORB.shutdown(true);
        clientORB = null;
        serverORB.shutdown(true);
        serverORB = null;
    }

    @Test
    public void testListener() throws Exception
    {
        server.ping2();

        Thread.sleep(1000);

        assertTrue("No open message from listener", TCPListener.isListenerOpen());

        server._release();

        Thread.sleep(1000);

        assertTrue("No close message from listener", TCPListener.isListenerClose());

        assertTrue(TCPListener.isEventOfCorrectType());
    }
}
