package org.jacorb.test.common;

/*
 *        JacORB  - a free Java ORB
 *
 *   Copyright (C) 1997-2012 Gerald Brose / The JacORB Team.
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

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Properties;
import org.omg.PortableServer.POA;

/**
 * This class extends the ORBTestCase to also provide for a separate
 * CORBA server process, and allows JUnit test cases to talk to a
 * CORBA object supplied by that server.
 * <p>
 * See {@link ClientServerTestCase} for how to use this class in a
 * {@literal @}BeforeClass annotation.
 *
 * @author Andre Spiegel <spiegel@gnu.org>
 * @author Nick Cross
 */
public class ClientServerSetup extends ORBTestCase
{
    static final String SERVANT_NAME = "SERVANT_NAME";

    private ServerSetup serverSetup;
    private String servantName;
    private org.omg.CORBA.Object serverObject;
    private ServerSetup imrSetup;

    protected String ior;

    /**
     * Constructs a new ClientServerSetup which spawns a
     * server process in which an instance of the class
     * servantName is created and registered with the ORB.
     *
     * @param servantName The fully qualified name of the
     * servant class that should be instantiated in the
     * server process.
     * @throws Exception
     */
    public ClientServerSetup ( String servantName ) throws Exception
    {
        this(servantName, null, null );
    }


    public ClientServerSetup( String servantName,
                              Properties optionalClientProperties,
                              Properties optionalServerProperties) throws Exception
    {
        this(null, servantName, optionalClientProperties, optionalServerProperties);
    }

    public ClientServerSetup( String testServer,
                              String servantName,
                              Properties optionalClientProperties,
                              Properties optionalServerProperties ) throws Exception
    {
        this(testServer, new String[] { servantName}, optionalClientProperties, optionalServerProperties);

    }

    public ClientServerSetup( String testServer,
                              String[] servantArgs,
                              Properties optionalClientProperties,
                              Properties optionalServerProperties ) throws Exception
    {
        if (optionalClientProperties == null)
        {
            optionalClientProperties = new Properties();
        }
        if (optionalServerProperties == null)
        {
            optionalServerProperties = new Properties();
        }

        if (isSSLDisabled(optionalClientProperties, optionalServerProperties))
        {
            // if ssl is disabled in one of the properties make sure to copy
            // this information to both property sets.
            optionalClientProperties.setProperty(CommonSetup.JACORB_REGRESSION_DISABLE_SECURITY, "true");

            optionalServerProperties.setProperty(CommonSetup.JACORB_REGRESSION_DISABLE_SECURITY, "true");
        }

        if (isIMREnabled(optionalClientProperties, optionalServerProperties))
        {
            final Properties imrServerProps = new Properties();

            File imrIOR;
            try
            {
                imrIOR = File.createTempFile("imr", ".ior");
                imrIOR.deleteOnExit();
            }
            catch (IOException e)
            {
                throw new RuntimeException(e);
            }

            imrSetup = new ServerSetup(ImplementationRepositoryRunner.class.getName(), imrIOR.toString(), imrServerProps);

            final Properties imrProps = new Properties();
            imrProps.put("jacorb.use_imr", "on");
            try
            {
                imrProps.put("ORBInitRef.ImplementationRepository", imrIOR.toURI().toURL().toString());
            }
            catch (MalformedURLException e)
            {
                throw new RuntimeException(e);
            }

            optionalClientProperties.putAll(imrProps);
            optionalServerProperties.putAll(imrProps);
        }

        servantName = servantArgs[0];

        orbProps.putAll(optionalClientProperties);
        orbProps.put (SERVANT_NAME, servantName);

        serverSetup = new ServerSetup(testServer, servantArgs, optionalServerProperties);

        if (isSSLEnabled())
        {
            // In this case we have been configured to run all the tests
            // in SSL mode. For simplicity, we will use the demo/ssl keystore
            // and properties (partly to ensure that they always work)
            Properties cp = CommonSetup.loadSSLProps("jsse_client_props", "jsse_client_ks");

            orbProps.putAll(cp);
        }

        if (imrSetup != null)
        {
            TestUtils.log("starting ImR");
            imrSetup.setUp();

            imrSetup.getServerIOR();
        }

        ORBSetUp();
        serverSetup.setUp();
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

    private void resolveServerObject()
    {
        if (serverObject == null)
        {
            ior = serverSetup.getServerIOR();
            serverObject = orb.string_to_object(ior);
        }
    }

    public void tearDown() throws Exception
    {
        if (serverObject != null)
        {
            serverObject._release();
            serverObject = null;
        }

        if (orb != null)
        {
            ORBTearDown();
        }

        serverSetup.tearDown();

        if (imrSetup != null)
        {
            imrSetup.tearDown();
            imrSetup = null;
        }
    }

    public String getServerIOR()
    {
        resolveServerObject();

        return ior;
    }

    /**
     * Gets a reference to the object that was instantiated in the
     * server process.
     */
    public org.omg.CORBA.Object getServerObject()
    {
        resolveServerObject();

        return serverObject;
    }

    /**
     * Gets the client ORB that is used to communicate with the server.
     */
    public org.omg.CORBA.ORB getClientOrb()
    {
        return orb;
    }

    public POA getClientRootPOA()
    {
        return rootPOA;
    }

    /**
     * check is IMR testing is disabled for this setup
     */
    private boolean isIMREnabled(Properties clientProps, Properties serverProps)
    {
        boolean isEnabled = Boolean.getBoolean("jacorb.test.imr")
                || TestUtils.getStringAsBoolean(clientProps.getProperty("jacorb.test.imr"))
                || TestUtils.getStringAsBoolean(serverProps.getProperty("jacorb.test.imr"));

        boolean isDisabled = TestUtils.getStringAsBoolean(clientProps.getProperty(CommonSetup.JACORB_REGRESSION_DISABLE_IMR, "false"))
            || TestUtils.getStringAsBoolean(serverProps.getProperty(CommonSetup.JACORB_REGRESSION_DISABLE_IMR, "false"));

        boolean result = isEnabled && !isDisabled;

        if (isDisabled)
        {
            clientProps.setProperty("jacorb.use_imr", "off");
            serverProps.setProperty("jacorb.use_imr", "off");

            clientProps.setProperty("jacorb.use_tao_imr", "off");
            serverProps.setProperty("jacorb.use_tao_imr", "off");
        }

        return result;
    }

    public boolean isSSLEnabled()
    {
        String sslProperty = orbProps.getProperty("jacorb.test.ssl", System.getProperty("jacorb.test.ssl"));
        boolean clientUseSSL = TestUtils.getStringAsBoolean(sslProperty);
        boolean useSSL = clientUseSSL && !TestUtils.getStringAsBoolean(orbProps.getProperty(CommonSetup.JACORB_REGRESSION_DISABLE_SECURITY, "false"));

        return useSSL && serverSetup.isSSLEnabled();
    }
}
