package org.jacorb.test.orb;

import java.util.Properties;

import junit.framework.*;

import org.jacorb.orb.ParsedIOR;
import org.jacorb.orb.iiop.IIOPProfile;
import org.jacorb.orb.IIOPAddress;

import org.omg.CORBA.portable.Delegate;
import org.omg.IOP.IOR;
import org.omg.IOP.TAG_ALTERNATE_IIOP_ADDRESS;
import org.omg.IOP.TAG_INTERNET_IOP;
import org.omg.IOP.TaggedProfile;

import org.jacorb.test.*;
import org.jacorb.test.common.*;
import org.omg.CORBA.ORB;

/**
 * This test is similar to AlternateIIOPAddressTest, but it uses the
 * special IORInfoExt functions to set up the IORs.
 * 
 * @author Marc Heide
 * @version $Id$
 */
public class AlternateProfileTest extends ClientServerTestCase
{
    protected IIOPAddressServer server = null;
    protected ClientServerSetup _setup = null;

    private static final String CORRECT_HOST = "127.0.0.1";
    private static final String WRONG_HOST   = "10.0.1.223"; //"194.138.122.114"
    private static final String WRONG_HOST_2 = "10.0.1.223"; //"147.54.135.239"

    private static final int CORRECT_PORT = 50000;
    private static final int WRONG_PORT   = 50001;

    public AlternateProfileTest(String name, ClientServerSetup setup)
    {
        super(name, setup);
        _setup = setup;
    }

    protected void setUp() throws Exception
    {
        server = IIOPAddressServerHelper.narrow(setup.getServerObject());
    }

    protected void tearDown() throws Exception
    {
        // server.clearSocketAddress();
        server.setIORAddress (CORRECT_HOST, CORRECT_PORT);
        server.clearAlternateAddresses();
    }

    public static Test suite()
    {
        TestSuite suite = new TestSuite("Test TAG_ALTERNATE_IIOP_ADDRESS");

        Properties client_props = new Properties();
        client_props.setProperty ("jacorb.retries", "2");
        client_props.setProperty ("jacorb.retry_interval", "50");
        client_props.setProperty ("jacorb.connection.client.pending_reply_timeout", "2000");
        client_props.setProperty ("jacorb.log.verbosity", "4");

        Properties server_props = new Properties();
        server_props.setProperty
            ("org.omg.PortableInterceptor.ORBInitializerClass.IIOPProfileORBInitializer",
             "org.jacorb.test.orb.IIOPProfileORBInitializer");
        server_props.setProperty ("OAPort", Integer.toString(CORRECT_PORT));

        ClientServerSetup setup =
         new ClientServerSetup (suite,
                                   "org.jacorb.test.orb.IIOPAddressServerImpl",
                                   client_props,
                                   server_props);

        suite.addTest (new AlternateProfileTest("test_ping", setup));
        suite.addTest (new AlternateProfileTest("test_primary_ok", setup));
        suite.addTest (new AlternateProfileTest("test_primary_wrong_host", setup));
        suite.addTest (new AlternateProfileTest("test_primary_wrong_port", setup));
        suite.addTest (new AlternateProfileTest("test_alternate_ok", setup));
        suite.addTest (new AlternateProfileTest("test_alternate_ok_2", setup));
        suite.addTest (new AlternateProfileTest("test_alternate_wrong", setup));

        return setup;
    }

    public void test_ping()
    {
        Sample s = server.getObject();
        testNumberOfIIOPProfiles(1, s);
        int result = s.ping (17);
        assertEquals (18, result);
    }

    public void test_primary_ok()
    {
        server.setIORAddress( CORRECT_HOST, CORRECT_PORT );
        Sample s = server.getObject();
        testNumberOfIIOPProfiles(1, s);
        int result = s.ping (77);
        assertEquals (78, result);
    }

    public void test_primary_wrong_host()
    {
        server.setIORAddress( WRONG_HOST, CORRECT_PORT );
        Sample s = server.getObject();
        testNumberOfIIOPProfiles(1, s);
        try
        {
            int result = s.ping (123);
            fail ("TRANSIENT or TIMEOUT exception expected");
        }
        catch (org.omg.CORBA.TRANSIENT ex)
        {
            // ok
        }
    }

    public void test_primary_wrong_port()
    {
        server.setIORAddress( CORRECT_HOST, WRONG_PORT );
        Sample s = server.getObject();
        testNumberOfIIOPProfiles(1, s);

        try
        {
            int result = s.ping (4);
            fail ("TRANSIENT exception expected");
        }
        catch (org.omg.CORBA.TRANSIENT ex)
        {
            // ok
        }
    }

    public void test_alternate_ok()
    {
        server.setIORAddress( WRONG_HOST, CORRECT_PORT );
        server.addAlternateAddress( CORRECT_HOST, CORRECT_PORT );
        Sample s = server.getObject();
        testNumberOfIIOPProfiles(2, s);
        testHostAndPortInIIOPProfile(s, 0, WRONG_HOST, CORRECT_PORT);
        testHostAndPortInIIOPProfile(s, 1, CORRECT_HOST, CORRECT_PORT);

        ORB _myOrb = _setup.getClientOrb();
        String iorStr = _myOrb.object_to_string(s);
        System.out.println(iorStr);
        int result = s.ping (99);
        assertEquals (100, result);
    }

    public void test_alternate_ok_2()
    {
        server.setIORAddress( WRONG_HOST, CORRECT_PORT );
        server.addAlternateAddress( WRONG_HOST_2, CORRECT_PORT );
        server.addAlternateAddress( CORRECT_HOST, CORRECT_PORT );
        Sample s = server.getObject();
        testNumberOfIIOPProfiles(3, s);
        testHostAndPortInIIOPProfile(s, 0, WRONG_HOST, CORRECT_PORT);
        testHostAndPortInIIOPProfile(s, 1, WRONG_HOST_2, CORRECT_PORT);
        testHostAndPortInIIOPProfile(s, 2, CORRECT_HOST, CORRECT_PORT);
        int result = s.ping (187);
        assertEquals (188, result);
    }

    public void test_alternate_wrong()
    {
        server.setIORAddress( CORRECT_HOST, WRONG_PORT );
        server.addAlternateAddress( WRONG_HOST, CORRECT_PORT );
        server.addAlternateAddress( WRONG_HOST_2, WRONG_PORT );
        server.addAlternateAddress( WRONG_HOST_2, CORRECT_PORT );
        Sample s = server.getObject();
        testNumberOfIIOPProfiles(4, s);
        testHostAndPortInIIOPProfile(s, 0, CORRECT_HOST, WRONG_PORT);
        testHostAndPortInIIOPProfile(s, 1, WRONG_HOST, CORRECT_PORT);
        testHostAndPortInIIOPProfile(s, 2, WRONG_HOST_2, WRONG_PORT);
        testHostAndPortInIIOPProfile(s, 3, WRONG_HOST_2, CORRECT_PORT);
        try
        {
            int result = s.ping (33);
            fail ("TRANSIENT exception expected");
        }
        catch (org.omg.CORBA.TRANSIENT ex)
        {
            // ok
        }

    }

    /**
     * Tests the number of IOP profiles in given IOR.
     * @param numberExpected
     * @param obj
     */
    public void testNumberOfIIOPProfiles( int numberExpected, org.omg.CORBA.Object obj )
    {
       // try to get ORB delegate to object
       org.jacorb.orb.Delegate jacOrbDelegate = null;
       Delegate localObj = ((org.omg.CORBA.portable.ObjectImpl)obj)._get_delegate();
       jacOrbDelegate = (org.jacorb.orb.Delegate)localObj;

       ParsedIOR pior = jacOrbDelegate.getParsedIOR();
       org.omg.IOP.IOR ior = jacOrbDelegate.getIOR();

       TaggedProfile[] profiles = ior.profiles;
       int nrOfIOPProf = 0;
       for (int i = 0; i < profiles.length; i++)
       {
           if (profiles[i].tag == TAG_INTERNET_IOP.value)
           {
              nrOfIOPProf++;
           }
       }
       assertEquals(numberExpected, nrOfIOPProf);
    }

    /**
     * Tests if given host and port equal values in given IOR.
     * Since several IOP profiles may be coded in IOR, an position must be specified.
     * Position must be: 0 <= pos < max_number_of_profiles
     * @param obj
     * @param pos
     * @param host
     * @param port
     */
    public void testHostAndPortInIIOPProfile(org.omg.CORBA.Object obj, int pos, String host, int port)
    {
       // try to get ORB delegate to object
       org.jacorb.orb.Delegate jacOrbDelegate = null;
       Delegate localObj = ((org.omg.CORBA.portable.ObjectImpl)obj)._get_delegate();
       jacOrbDelegate = (org.jacorb.orb.Delegate)localObj;

       ParsedIOR pior = jacOrbDelegate.getParsedIOR();
       org.omg.IOP.IOR ior = jacOrbDelegate.getIOR();

       TaggedProfile[] profiles = ior.profiles;
       int cnt = pos;
       boolean found = false;
       for (int i = 0; i < profiles.length; i++)
       {
           if (profiles[i].tag == TAG_INTERNET_IOP.value)
           {
              if( cnt == 0 )
              {
                  IIOPProfile prof = new IIOPProfile(profiles[i].profile_data);
                  assertEquals(prof.getAddress().getIP(), host);
                  assertEquals(prof.getAddress().getPort(), port);
                  found = true;
                  break;
              }
              else
              {
                  cnt--;
                  continue;
              }
           }
       }
       assertEquals(true, found);
    }

    public static void main(String args[])
    {
      junit.textui.TestRunner.run(suite());
    }

}
