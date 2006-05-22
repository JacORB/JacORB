package org.jacorb.test.common;

/*
 *        JacORB  - a free Java ORB
 *
 *   Copyright (C) 1997-2005  Gerald Brose.
 *
 *   This library is free software; you can redistribute it and/or
 *   modify it under the terms of the GNU Library General Public
 *   License as published by the Free Software Foundation; either
 *   version 2 of the License, or (at your option) any later version.
 *
 *   This library is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *   Library General Public License for more details.
 *
 *   You should have received a copy of the GNU Library General Public
 *   License along with this library; if not, write to the Free
 *   Software Foundation, 51 Franklin Street, Fifth Floor, Boston,
 *   MA 02110-1301, USA.
 */

import java.util.*;

import org.omg.CORBA.*;
import org.omg.PortableServer.*;

import junit.framework.*;
import junit.extensions.*;

import org.jacorb.test.common.launch.*;

/**
 * A special TestSetup that creates a separate CORBA server process,
 * and allows JUnit test cases to talk to a CORBA object supplied
 * by that server.
 * <p>
 * A <code>ClientServerSetup</code> should be used together with a
 * {@link ClientServerTestCase}, which provides an easy way so that
 * the individual test cases can actually see the setup.
 * The following example shows how to set this up in the static
 * <code>suite</code> method:
 *
 * <p><blockquote><pre>
 * public class MyTest extends ClientServerTestCase
 * {
 *     ...
 *
 *     public static Test suite()
 *     {
 *         TestSuite suite = new TestSuite ("My CORBA Test");
 *
 *         // Wrap the setup around the suite, specifying
 *         // the name of the servant class that should be
 *         // instantiated by the server process.
 *
 *         ClientServerSetup setup =
 *             new ClientServerSetup (suite,
 *                                    "my.corba.ServerImpl");
 *
 *         // Add test cases, passing the setup as an
 *         // additional constructor parameter.
 *
 *         suite.addTest (new MyTest ("testSomething", setup));
 *         ...
 *
 *         // Return the setup, not the suite!
 *         return setup;
 *     }
 * }
 * </pre></blockquote><p>
 *
 * The individual test cases can then access the setup in a convenient way.
 * For details, see {@link ClientServerTestCase}.
 *
 * @author Andre Spiegel <spiegel@gnu.org>
 * @version $Id$
 */
public class ClientServerSetup extends TestSetup {

    protected String                     servantName;
    protected Process                    serverProcess;
    protected StreamListener             outListener, errListener;
    protected org.omg.CORBA.Object       serverObject;
    protected org.omg.CORBA.ORB          clientOrb;
    protected org.omg.PortableServer.POA clientRootPOA;

    private Properties clientOrbProperties = null;
    private Properties serverOrbProperties = null;

    private static Comparator comparator = new JacORBVersionComparator();

    /**
     * Constructs a new ClientServerSetup that is wrapped
     * around the specified Test.  When the test is run,
     * the setup spawns a server process in which an instance
     * of the class servantName is created and registered
     * with the ORB.
     * @param test The test around which the new setup
     * should be wrapped.
     * @param servantName The fully qualified name of the
     * servant class that should be instantiated in the
     * server process.
     */
    public ClientServerSetup ( Test test, String servantName )
    {
        super ( test );
        this.servantName = servantName;
        clientOrbProperties = new Properties();
        clientOrbProperties.put ("org.omg.CORBA.ORBClass",
                                 "org.jacorb.orb.ORB");
        clientOrbProperties.put ("org.omg.CORBA.ORBSingletonClass",
                                 "org.jacorb.orb.ORBSingleton");
    }

    public ClientServerSetup( Test test,
                              String servantName,
                              Properties clientOrbProperties,
                              Properties serverOrbProperties )
    {
        this( test, servantName );
        if (clientOrbProperties != null)
            this.clientOrbProperties.putAll (clientOrbProperties);
        this.serverOrbProperties = serverOrbProperties;
    }

    public void setUp() throws Exception
    {
        clientOrb = ORB.init (new String[0], clientOrbProperties );
        clientRootPOA = POAHelper.narrow
                          ( clientOrb.resolve_initial_references( "RootPOA" ) );
        clientRootPOA.the_POAManager().activate();

        String serverVersion = System.getProperty ("jacorb.test.server.version",
                                                   "cvs");
        String testID = System.getProperty("jacorb.test.id", "");
        String cs = System.getProperty ("jacorb.test.coverage", "false");
        boolean coverage = cs.equals("true") || cs.equals("on") || cs.equals("yes");
        String outStr = System.getProperty("jacorb.test.outputfile.testname", "false");
        boolean outputFileTestName =
            (outStr.equals("true") || outStr.equals("on") || outStr.equals("yes"));

        Properties serverProperties = new Properties();
        if (serverOrbProperties != null)
            serverProperties.putAll (serverOrbProperties);
        serverProperties.put ("jacorb.implname", servantName);

        JacORBLauncher launcher = JacORBLauncher.getLauncher (serverVersion,
                                                              coverage);

        if (coverage)
        {
            serverProperties.put ("emma.coverage.out.file",
                                  launcher.getJacorbHome() +
                                  "/test/regression/output/" +
                                  (outputFileTestName == true ? "" : testID) +
                                  "/coverage-server.ec");
        }

        serverProcess = launcher.launch
        (
            TestUtils.testHome() + "/classes",
            serverProperties,
            getTestServerMain(),
            new String[] { servantName }
        );

        outListener = new StreamListener (serverProcess.getInputStream(),
                                          "OUT");
        errListener = new StreamListener (serverProcess.getErrorStream(),
                                          "ERR");
        outListener.start();
        errListener.start();
        String ior = outListener.getIOR(5000);
        if (ior == null)
        {
            String exc = errListener.getException(1000);

            fail("could not access IOR. cause maybe: " + exc);
        }
        resolveServerObject(ior);
    }

    protected void resolveServerObject(String ior)
    {
        serverObject = clientOrb.string_to_object(ior);
    }

    public void tearDown() throws Exception
    {
        serverProcess.destroy();
        outListener.setDestroyed();
        errListener.setDestroyed();
    }

    public String getTestServerMain()
    {
        String serverVersion = System.getProperty ("jacorb.test.server.version",
                                                   "cvs");
        if (comparator.compare (serverVersion, "2.2") >= 0)
        {
            return "org.jacorb.test.common.TestServer";
        }
        return "org.jacorb.test.common.TestServer_before_2_2";
    }

    /**
     * Gets a reference to the object that was instantiated in the
     * server process.
     */
    public org.omg.CORBA.Object getServerObject()
    {
        return serverObject;
    }

    /**
     * Gets the client ORB that is used to communicate with the server.
     */
    public org.omg.CORBA.ORB getClientOrb()
    {
        return clientOrb;
    }

    /**
     * Gets the fully qualified name of the servant class that
     * is instantiated in the server.
     */
    public String getServantName()
    {
        return servantName;
    }

    /**
     * Gets the server process.
     */
    public Process getServerProcess()
    {
        return serverProcess;
    }

    public POA getClientRootPOA()
    {
        return clientRootPOA;
    }
}
