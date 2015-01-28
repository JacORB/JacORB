package org.jacorb.test.harness;

/*
 *        JacORB  - a free Java ORB
 *
 *   Copyright (C) 1997-2014 Gerald Brose / The JacORB Team.
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
 * @author Andre Spiegel &lt;spiegel@gnu.org&gt;
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
        this(testServer, servantName, null, optionalClientProperties, optionalServerProperties);

    }

    public ClientServerSetup( String testServer,
                              String sName,
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

        TestUtils.getLogger().debug("Configuring ClientServer for " +
                (testServer == null ? "" : testServer + '/') + sName);

        if (isIMREnabled(optionalClientProperties, optionalServerProperties))
        {
            final Properties imrServerProps = new Properties();

            imrServerProps.setProperty("jacorb.test.verbose", Boolean.toString(TestUtils.verbose));

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
            File imrTable = File.createTempFile("MyImR1_table", ".dat");
            imrTable.deleteOnExit();

            imrServerProps.setProperty ("jacorb.imr.ior_file", imrIOR.toString());
            imrServerProps.setProperty ("jacorb.imr.table_file", imrTable.toString());
            imrServerProps.setProperty ("jacorb.imr.allow_auto_register", "true");
            imrSetup = new ServerSetup(ImplementationRepositoryRunner.class.getName(), imrIOR.toString(), imrServerProps);

            final Properties imrProps = new Properties();
            imrProps.setProperty("jacorb.use_imr", "on");
            try
            {
                imrProps.setProperty("ORBInitRef.ImplementationRepository", imrIOR.toURI().toURL().toString());
            }
            catch (MalformedURLException e)
            {
                throw new RuntimeException(e);
            }

            optionalClientProperties.putAll(imrProps);
            optionalServerProperties.putAll(imrProps);
        }

        this.servantName = (sName == null ? "" : sName);

        orbProps.putAll(optionalClientProperties);
        orbProps.put (SERVANT_NAME, servantName);

        serverSetup = new ServerSetup(testServer, servantName, servantArgs, optionalServerProperties);

        if (imrSetup != null)
        {
            TestUtils.getLogger().debug("Starting ImR");

            imrSetup.setUp();

            TestUtils.getLogger().debug("ImR IOR " + imrSetup.getServerIOR());
        }

        ORBSetUp();
        serverSetup.setUp();
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
        if ( ior == null)
        {
            ior = serverSetup.getServerIOR();
        }

        return ior;
    }

    /**
     * Gets a reference to the object that was instantiated in the
     * server process.
     */
    public org.omg.CORBA.Object getServerObject()
    {
        if (serverObject == null)
        {
            serverObject = orb.string_to_object(getServerIOR());
        }

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

        return isEnabled;
    }
}
