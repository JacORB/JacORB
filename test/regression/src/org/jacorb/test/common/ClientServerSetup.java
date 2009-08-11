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

import java.util.Properties;

import junit.extensions.TestSetup;
import junit.framework.Test;

import org.omg.PortableServer.POA;

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

    public static final String JACORB_REGRESSION_DISABLE_IMR = "jacorb.regression.disable_imr";

    private final ServerSetup serverSetup;
    private final ORBSetup clientORBSetup;

    protected final String               servantName;
    protected org.omg.CORBA.Object       serverObject;

    private ClientServerSetup imrSetup;

    private String ior;

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
        this(test, servantName, null, null );
    }


    public ClientServerSetup( Test test,
                              String servantName,
                              Properties optionalClientProperties,
                              Properties optionalServerProperties)
    {
        this(test, null, servantName, optionalClientProperties, optionalServerProperties);
    }

    public ClientServerSetup( Test test,
                              String testServer,
                              String servantName,
                              Properties optionalClientProperties,
                              Properties optionalServerProperties )
    {
        super(test);

        if (isSSLDisabled(optionalClientProperties, optionalServerProperties))
        {
            // if ssl is disabled in one of the properties make sure to copy
            // this information to both property sets.
            if (optionalClientProperties == null)
            {
                optionalClientProperties = new Properties();
            }
            optionalClientProperties.setProperty(CommonSetup.JACORB_REGRESSION_DISABLE_SECURITY, "true");

            if (optionalServerProperties == null)
            {
                optionalServerProperties = new Properties();
            }
            optionalServerProperties.setProperty(CommonSetup.JACORB_REGRESSION_DISABLE_SECURITY, "true");
        }

        serverSetup = new ServerSetup(this, testServer, servantName, optionalServerProperties);
        clientORBSetup = new ORBSetup(this, optionalClientProperties);

        this.servantName = servantName;
    }

    private static boolean isSSLDisabled(Properties clientProps, Properties serverProps)
    {
        boolean result = false;

        if (clientProps != null)
        {
            result = TestUtils.getStringAsBoolean(clientProps.getProperty(CommonSetup.JACORB_REGRESSION_DISABLE_SECURITY, "false"));
        }

        if (!result && serverProps != null)
        {
            result = TestUtils.getStringAsBoolean(serverProps.getProperty(CommonSetup.JACORB_REGRESSION_DISABLE_SECURITY, "false"));
        }

        return result;
    }

    public static long getTestTimeout()
    {
        return ServerSetup.getTestTimeout();
    }

    public void setUp() throws Exception
    {
        setUpInternal();

        doSetUp();
    }

    protected void doSetUp() throws Exception
    {
    }

    private void setUpInternal() throws Exception
    {
        serverSetup.setUp();
        clientORBSetup.setUp();

        resolveServerObject(serverSetup.getServerIOR());
    }

    protected final void initSecurity()
    {
    }

    protected void resolveServerObject(String ior)
    {
        this.ior = ior;

        serverObject = clientORBSetup.getORB().string_to_object(ior);
    }

    public void tearDown() throws Exception
    {
        doTearDown();

        if (serverObject != null)
        {
            serverObject._release();
            serverObject = null;
        }

        clientORBSetup.tearDown();
        serverSetup.tearDown();

        if (imrSetup != null)
        {
            imrSetup.tearDown();
            imrSetup = null;
        }
    }

    protected void doTearDown() throws Exception
    {
    }

    protected final void shutdownClientORB()
    {
    }

    public String getServerIOR()
    {
        return ior;
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
        return clientORBSetup.getORB();
    }

    /**
     * Gets the fully qualified name of the servant class that
     * is instantiated in the server.
     */
    public String getServantName()
    {
        return servantName;
    }


    public POA getClientRootPOA()
    {
        return clientORBSetup.getRootPOA();
    }


    public boolean isSSLEnabled()
    {
        return clientORBSetup.isSSLEnabled() && serverSetup.isSSLEnabled();
    }
}
