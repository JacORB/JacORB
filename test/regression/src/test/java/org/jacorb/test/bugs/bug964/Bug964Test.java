package org.jacorb.test.bugs.bug964;

import static org.junit.Assert.assertTrue;
import java.util.Properties;
import org.jacorb.test.bugs.bugjac670.GSLoadBalancer;
import org.jacorb.test.bugs.bugjac670.GSLoadBalancerHelper;
import org.jacorb.test.harness.ClientServerSetup;
import org.jacorb.test.harness.FixedPortClientServerTestCase;
import org.jacorb.test.harness.TestUtils;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.omg.CORBA.ORB;
import org.omg.PortableServer.POA;

public class Bug964Test extends FixedPortClientServerTestCase
{
    private GSLoadBalancer server;

    private static String remotePort = Integer.toString(getNextAvailablePort());
    private static String localPort = Integer.toString(getNextAvailablePort());

    private final static String remoteCorbaloc = "corbaloc::localhost:" + remotePort + "/GSLBService";
    private final static String localCorbaloc = "corbaloc::localhost:" + localPort + "/GSLBService";

    @Before
    public void setUp() throws Exception
    {
        POA poa = setup.getClientRootPOA();
        ORB orb = setup.getClientOrb();
        GSLoadBalancerImpl servant = new GSLoadBalancerImpl();

        byte[] id = poa.activate_object(servant);
        org.omg.CORBA.Object obj = poa.id_to_reference( id ) ;

        String IOR = orb.object_to_string(obj);

        ((org.jacorb.orb.ORB)orb).addObjectKey("GSLBService", IOR);

        poa.the_POAManager().activate();

        GSLoadBalancerImpl.ID = "LOCAL";
        server = GSLoadBalancerHelper.narrow(orb.string_to_object(localCorbaloc));
    }

    @BeforeClass
    public static void beforeClassSetUp() throws Exception
    {
        Assume.assumeFalse(TestUtils.isSSLEnabled);

        Properties clientprops = new java.util.Properties();
        clientprops.setProperty( "OAPort", localPort );
        clientprops.setProperty("org.omg.CORBA.ORBClass", "org.jacorb.orb.ORB");
        clientprops.setProperty("org.omg.CORBA.ORBSingletonClass", "org.jacorb.orb.ORBSingleton");

        Properties serverprops = new java.util.Properties();
        serverprops.setProperty( "OAPort", remotePort );
        serverprops.setProperty("org.omg.CORBA.ORBClass", "org.jacorb.orb.ORB");
        serverprops.setProperty("org.omg.CORBA.ORBSingletonClass", "org.jacorb.orb.ORBSingleton");

        setup = new ClientServerSetup
        (

         "org.jacorb.test.bugs.bug964.GSLoadBalancerServer",
         "GSLoadBalancerImpl",
         clientprops,
         serverprops
        );
    }

    @Test
    public void corbaloccall1()
       throws Exception
    {
        org.omg.CORBA.Object o = setup.getClientOrb().string_to_object(remoteCorbaloc);
        GSLoadBalancer sr = GSLoadBalancerHelper.narrow(o);

        assertTrue (sr.greeting("1").indexOf("LOCAL") == -1);
        assertTrue (server.greeting("2").indexOf("LOCAL") != -1);
    }
}
