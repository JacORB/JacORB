package org.jacorb.test.orb;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.jacorb.test.BasicServer;
import org.jacorb.test.BasicServerHelper;
import org.jacorb.test.common.ClientServerSetup;
import org.jacorb.test.common.ClientServerTestCase;
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
        TestSuite suite = new TestSuite();
        ClientServerSetup setup = new ClientServerSetup(suite, BasicServerImpl.class.getName());

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
        args.add("BasicServer=" + setup.getServerObject().toString());

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
        args.add("-ORBInitRef.BasicServer="+setup.getServerObject().toString());

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

    private void testORB() throws InvalidName
    {
        ORB orb = newORB(args);
        BasicServer server = BasicServerHelper.narrow(orb.resolve_initial_references("BasicServer"));
        assertFalse(server.bounce_boolean(false));
    }

    public void testORBDefaultInitRef() throws Exception
    {
        // TODO
    }

    protected void setUp() throws Exception
    {
        System.err.println("TEST " + getName());
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

    private ORB newORB(List args)
    {
        String[] arg = (String[]) args.toArray(new String[args.size()]);

        ORB orb = ORB.init(arg, null);

        orbs.add(orb);

        return orb;
    }
}
