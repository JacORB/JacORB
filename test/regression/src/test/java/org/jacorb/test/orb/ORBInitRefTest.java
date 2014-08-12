package org.jacorb.test.orb;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import org.jacorb.test.BasicServer;
import org.jacorb.test.BasicServerHelper;
import org.jacorb.test.harness.ClientServerSetup;
import org.jacorb.test.harness.ClientServerTestCase;
import org.jacorb.test.harness.TestUtils;
import org.junit.After;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.omg.CORBA.BAD_PARAM;
import org.omg.CORBA.ORB;
import org.omg.CORBA.ORBPackage.InvalidName;

/**
 * @author Alphonse Bendt
 */
public class ORBInitRefTest extends ClientServerTestCase
{
    private final List<String> args = new ArrayList<String>();
    private ORB orb;

    @BeforeClass
    public static void beforeClassSetUp() throws Exception
    {
        Assume.assumeFalse(TestUtils.isSSLEnabled);

        Properties props = new Properties();

        Properties serverProps = new Properties();

        serverProps.setProperty("jacorb.test.corbaloc.enable", "true");
        serverProps.setProperty("jacorb.test.corbaloc.implname", "ORBInitRefTestImpl");
        serverProps.setProperty("jacorb.test.corbaloc.poaname", "ORBInitRefTestPOA");
        serverProps.setProperty("jacorb.test.corbaloc.objectid", "ORBInitRefTestID");
        serverProps.setProperty("jacorb.test.corbaloc.port", "57231");

        serverProps.setProperty("jacorb.test.corbaloc.shortcut", "ORBInitRefServer");

        setup = new ClientServerSetup(BasicServerImpl.class.getName(), props, serverProps);

    }

    @Test
    public void testResolveWithoutConfigShouldFail() throws Exception
    {
        try
        {
            testORB();
        }
        catch(InvalidName e)
        {
            // expected
        }
    }

    @Test
    public void testORBInitRef() throws Exception
    {
        args.add("-ORBInitRef");
        args.add("BasicServer=" + setup.getServerIOR());

        testORB();
    }

    @Test
    public void testORBInitRefIncomplete() throws Exception
    {
        args.add("-ORBInitRef");
        args.add("BasicServer=");

        try
        {
            testORB();
            fail();
        }
        catch (BAD_PARAM e)
        {
        }
    }

    @Test
    public void testORBInitRefIncomplete2() throws Exception
    {
        args.add("-ORBInitRef");
        args.add("BasicServer");

        try
        {
            testORB();
            fail();
        }
        catch (BAD_PARAM e)
        {
        }
    }

    @Test
    public void testJacORBSpecificORBInitRef() throws Exception
    {
        args.add("-ORBInitRef.BasicServer=" + setup.getServerIOR());

        testORB();
    }

    @Test
    public void testJacORBSpecificORBInitRefIncomplete() throws Exception
    {
        args.add("-ORBInitRef.BasicServer=");

        try
        {
            testORB();
            fail();
        }
        catch (BAD_PARAM e)
        {
        }
    }

    @Test
    public void testJacORBSpecificORBInitRefIncomplete2() throws Exception
    {
        args.add("-ORBInitRef.BasicServer");

        try
        {
            testORB();
            fail();
        }
        catch (BAD_PARAM e)
        {
        }
    }

    private ORB testORB() throws InvalidName
    {
        orb = newORB(args);
        BasicServer server = BasicServerHelper.narrow(orb.resolve_initial_references("BasicServer"));
        assertFalse(server.bounce_boolean(false));

        return orb;
    }

    @Test
    public void testORBDefaultInitRef() throws Exception
    {
        args.add("-ORBDefaultInitRef");
        args.add("corbaloc::localhost:57231");
        args.add("-ORBInitRef");
        args.add("BasicServer=" + setup.getServerIOR());

        ORB orb = testORB();
        BasicServer server = BasicServerHelper.narrow(orb.resolve_initial_references("ORBInitRefServer"));
        assertFalse(server.bounce_boolean(false));
    }

    @After
    public void tearDown() throws Exception
    {
        if (orb != null)
        {
            orb.shutdown(true);
        }
        args.clear();
    }

    @Ignore
    @Test
    public void testListInitialReferences() throws Exception
    {
        args.add("-ORBInitRef");
        args.add("BasicServer=" + setup.getServerIOR());

        ORB orb = newORB(args);

        HashSet<String> set = new HashSet<String>(Arrays.asList(orb.list_initial_services()));

        assertTrue(set.contains("BasicServer"));
    }

    private ORB newORB(List<String> args)
    {
        String[] arg = args.toArray(new String[args.size()]);

        Properties orbProps = new Properties();
        orbProps.setProperty("org.omg.CORBA.ORBClass", "org.jacorb.orb.ORB");
        orbProps.setProperty("org.omg.CORBA.ORBSingletonClass", "org.jacorb.orb.ORBSingleton");

        orb = ORB.init(arg, orbProps);

        return orb;
    }
}
