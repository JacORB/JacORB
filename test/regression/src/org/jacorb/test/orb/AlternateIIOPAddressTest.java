package org.jacorb.test.orb;

import java.util.Properties;

import junit.framework.*;

import org.jacorb.test.*;
import org.jacorb.test.common.*;

/**
 * @author Andre Spiegel
 * @version $Id$
 */
public class AlternateIIOPAddressTest extends ClientServerTestCase
{
    protected IIOPAddressServer server = null;

    public AlternateIIOPAddressTest(String name, ClientServerSetup setup)
    {
        super(name, setup);
    }

    protected void setUp() throws Exception
    {
        server = IIOPAddressServerHelper.narrow(setup.getServerObject());
    }

    public static Test suite()
    {
        TestSuite suite = new TestSuite("Test TAG_ALTERNATE_IIOP_ADDRESS");

        Properties server_props = new Properties();
        server_props.setProperty 
            ("org.omg.PortableInterceptor.ORBInitializerClass."
           + "org.jacorb.test.orb.IIOPAddressORBInitializer", "");

        ClientServerSetup setup = 
        	new ClientServerSetup (suite,
                                   "org.jacorb.test.orb.IIOPAddressServerImpl",
                                   null,
                                   server_props);

        suite.addTest (new AlternateIIOPAddressTest("test_ping", setup));
        suite.addTest (new AlternateIIOPAddressTest("test_primary", setup));

        return setup;
    }
    
    public void test_ping()
    {
        Sample s = server.getObject();
        int result = s.ping (17);
        assertEquals (18, result);
    }

    public void test_primary()
    {
        server.setSocketAddress ( "10.0.1.2", -1 );
        server.setIORAddress( "10.0.1.2", -1 );
        Sample s = server.getObject();
        int result = s.ping (77);
        assertEquals (78, result);
    }



}
