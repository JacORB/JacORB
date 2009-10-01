package org.jacorb.test.orb;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.jacorb.test.BasicServer;
import org.jacorb.test.BasicServerHelper;
import org.jacorb.test.common.ClientServerSetup;
import org.jacorb.test.common.ClientServerTestCase;
import org.jacorb.test.common.CommonSetup;
import org.jacorb.test.common.TestUtils;
import org.omg.CORBA.BAD_PARAM;
import org.omg.CORBA.ORB;
import org.omg.CORBA.ORBPackage.InvalidName;

/**
 * @author Alphonse Bendt
 */
public class ORBInitRefTest extends ClientServerTestCase
{
    private final List orbs = new ArrayList();
    private final List args = new ArrayList();

    public ORBInitRefTest(String name, ClientServerSetup setup)
    {
        super(name, setup);
    }

    public static Test suite()
    {
        Properties props = new Properties();

        props.setProperty(CommonSetup.JACORB_REGRESSION_DISABLE_SECURITY, "true");
        props.setProperty(CommonSetup.JACORB_REGRESSION_DISABLE_IMR, "true");

        Properties serverProps = new Properties();

        serverProps.setProperty("jacorb.test.corbaloc.enable", "true");
        serverProps.setProperty("jacorb.test.corbaloc.implname", "ORBInitRefTestImpl");
        serverProps.setProperty("jacorb.test.corbaloc.poaname", "ORBInitRefTestPOA");
        serverProps.setProperty("jacorb.test.corbaloc.objectid", "ORBInitRefTestID");
        serverProps.setProperty("jacorb.test.corbaloc.port", "57231");

        serverProps.setProperty("jacorb.test.corbaloc.shortcut", "ORBInitRefServer");

        TestSuite suite = new TestSuite();
        ClientServerSetup setup = new ClientServerSetup(suite, BasicServerImpl.class.getName(), props, serverProps);

        TestUtils.addToSuite(suite, setup, ORBInitRefTest.class);

        return setup;
    }

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

    public void testORBInitRef() throws Exception
    {
        args.add("-ORBInitRef");
        args.add("BasicServer=" + setup.getServerIOR());

        testORB();
    }

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

    public void testJacORBSpecificORBInitRef() throws Exception
    {
        args.add("-ORBInitRef.BasicServer=" + setup.getServerIOR());

        testORB();
    }

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
        ORB orb = newORB(args);
        BasicServer server = BasicServerHelper.narrow(orb.resolve_initial_references("BasicServer"));
        assertFalse(server.bounce_boolean(false));

        return orb;
    }

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

    protected void tearDown() throws Exception
    {
        for (Iterator i = orbs.iterator(); i.hasNext();)
        {
            ORB orb = (ORB) i.next();
            orb.shutdown(true);
        }

        orbs.clear();
        args.clear();
    }

    public void _testListInitialReferences() throws Exception
    {
        args.add("-ORBInitRef");
        args.add("BasicServer=" + setup.getServerIOR());

        ORB orb = newORB(args);

        HashSet set = new HashSet(Arrays.asList(orb.list_initial_services()));

        assertTrue(set.contains("BasicServer"));
    }

    private ORB newORB(List args)
    {
        String[] arg = (String[]) args.toArray(new String[args.size()]);

        ORB orb = ORB.init(arg, null);

        orbs.add(orb);

        return orb;
    }
}
