package org.jacorb.test.config;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 2000-2014 Gerald Brose / The JacORB Team.
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
 *   Software Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *
 */

import static org.junit.Assert.assertEquals;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Properties;
import org.jacorb.config.Configuration;
import org.jacorb.config.JacORBConfiguration;
import org.jacorb.test.harness.TestUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.omg.CORBA.ORB;

/**
 * Tests the various configuration mechanisms, properties files, and
 * property precedence in JacORB.
 * @author Alphonse Bendt
 * @author Andre Spiegel
 */
public class ConfigurationTest
{
    private final Properties oldProps = new Properties();

    @Before
    public void setUp() throws Exception
    {
        oldProps.putAll(System.getProperties());
    }

    @After
    public void tearDown() throws Exception
    {
        System.setProperties(oldProps);
    }

    /**
     * Place an orb.properties file on the classpath and verify that it gets
     * loaded by the ORB.
     */
    @Test
    public void testOrbPropertiesClasspath() throws Exception
    {
        try
        {
            createPropertiesFile("target/test-classes/orb.properties",
                                 "jacorb.connection.client.connect_timeout=33099");

            Properties props = new Properties();
            props.put("org.omg.CORBA.ORBClass", "org.jacorb.orb.ORB");
            props.put("org.omg.CORBA.ORBSingletonClass",
                    "org.jacorb.orb.ORBSingleton");

            ORB orb = ORB.init(new String[] {}, props);

            int timeout = ((org.jacorb.orb.ORB) orb).getConfiguration()
                    .getAttributeAsInteger(
                            "jacorb.connection.client.connect_timeout", 0);
            assertEquals(33099, timeout);

            orb.destroy();
        }
        finally
        {
            deletePropertiesFile ("target/test-classes/orb.properties");
        }
    }

    @Test
    public void testOrbidPropertiesConfigDir() throws Exception
    {
        try
        {
            createPropertiesFile ("mytestorbid.properties",
                                  "jacorb.connection.client.connect_timeout=33098");
            Properties props = new Properties();
            props.put("org.omg.CORBA.ORBClass", "org.jacorb.orb.ORB");
            props.put("org.omg.CORBA.ORBSingletonClass",
                    "org.jacorb.orb.ORBSingleton");

            System.setProperty("jacorb.config.dir", TestUtils
                    .osDependentPath(TestUtils.testHome()));
            System.setProperty("ORBid", "mytestorbid");

            ORB orb = ORB.init(new String[] {}, props);

            int timeout = ((org.jacorb.orb.ORB) orb).getConfiguration()
                    .getAttributeAsInteger(
                            "jacorb.connection.client.connect_timeout", 0);
            assertEquals(33098, timeout);

            orb.destroy();
        }
        finally
        {
            deletePropertiesFile ("mytestorbid.properties");
        }
    }

    /**
     * Place an orbid.properties file on the classpath and verify
     * that it gets loaded.
     */
    @Test
    public void testOrbidPropertiesClasspath() throws Exception
    {
        try
        {
            createPropertiesFile("target/test-classes/myownorbid.properties",
                                 "jacorb.connection.client.connect_timeout=33077");

            Properties props = new Properties();
            props.put("org.omg.CORBA.ORBClass", "org.jacorb.orb.ORB");
            props.put("org.omg.CORBA.ORBSingletonClass",
                    "org.jacorb.orb.ORBSingleton");

            System.setProperty("ORBid", "myownorbid");

            ORB orb = ORB.init(new String[] {}, props);

            int timeout = ((org.jacorb.orb.ORB) orb).getConfiguration()
                    .getAttributeAsInteger(
                            "jacorb.connection.client.connect_timeout", 0);
            assertEquals(33077, timeout);

            orb.destroy();
        }
        finally
        {
            deletePropertiesFile ("target/test-classes/myownorbid.properties");
        }
    }

    /**
     * Get properties from a custom.props file and verify that they
     * have been loaded.
     */
    @Test
    public void testCustomProps() throws Exception
    {
        try
        {
            createPropertiesFile("custom.properties",
                                 "jacorb.connection.client.connect_timeout=33100");

            Properties props = new Properties();
            props.put("org.omg.CORBA.ORBClass", "org.jacorb.orb.ORB");
            props.put("org.omg.CORBA.ORBSingletonClass",
                    "org.jacorb.orb.ORBSingleton");

            System.setProperty("custom.props",
                               getConfigFilename ("custom.properties"));

            ORB orb = ORB.init(new String[] {}, props);

            int timeout = ((org.jacorb.orb.ORB) orb).getConfiguration()
                    .getAttributeAsInteger(
                            "jacorb.connection.client.connect_timeout", 0);
            assertEquals(33100, timeout);

            orb.destroy();
        }
        finally
        {
            deletePropertiesFile ("custom.properties");
        }
    }

    /**
     * Verify that system properties end up in the JacORB configuration.
     */
    @Test
    public void testSystemProperties() throws Exception
    {
        Properties props = new Properties();
        props.put("org.omg.CORBA.ORBClass", "org.jacorb.orb.ORB");
        props.put("org.omg.CORBA.ORBSingletonClass",
                  "org.jacorb.orb.ORBSingleton");

        System.setProperty ("jacorb.connection.client.connect_timeout", "33105");
        ORB orb = ORB.init(new String[] {}, props);

        int timeout = ((org.jacorb.orb.ORB) orb).getConfiguration()
                    .getAttributeAsInteger(
                            "jacorb.connection.client.connect_timeout", 0);
        assertEquals(33105, timeout);
    }

    /**
     * Verify that ORB.init() properties end up in the JacORB configuration.
     */
    @Test
    public void testOrbInitProperties() throws Exception
    {
        Properties props = new Properties();
        props.put("org.omg.CORBA.ORBClass", "org.jacorb.orb.ORB");
        props.put("org.omg.CORBA.ORBSingletonClass",
                  "org.jacorb.orb.ORBSingleton");
        props.put("jacorb.connection.client.connect_timeout", "33707");

        ORB orb = ORB.init(new String[] {}, props);

        int timeout = ((org.jacorb.orb.ORB) orb).getConfiguration()
                    .getAttributeAsInteger(
                            "jacorb.connection.client.connect_timeout", 0);
        assertEquals(33707, timeout);

        orb.destroy();
    }

    /**
     * A property is set in every conceivable way, and we verify that
     * the value from ORB.init() takes precedence over all the others.
     */
    @Test
    public void testOrbInitPrecedence() throws Exception
    {
        try
        {
            createPropertiesFile ("target/test-classes/orb.properties",
                                  "jacorb.connection.client.connect_timeout=33700");
            createPropertiesFile ("myorbid.properties",
                                  "jacorb.connection.client.connect_timeout=33701");
            createPropertiesFile ("special.properties",
                                  "jacorb.connection.client.connect_timeout=33702");
            createPropertiesFile ("target/test-classes/myorbid.properties",
                                  "jacorb.connection.client.connect_timeout=33703");

            Properties props = new Properties();
            props.put("org.omg.CORBA.ORBClass", "org.jacorb.orb.ORB");
            props.put("org.omg.CORBA.ORBSingletonClass",
                      "org.jacorb.orb.ORBSingleton");
            props.put("jacorb.connection.client.connect_timeout", "33705");

            System.setProperty("jacorb.config.log.verbosity", "4");
            System.setProperty("jacorb.config.dir", TestUtils.testHome());
            System.setProperty("ORBid", "myorbid");
            System.setProperty("custom.props", getConfigFilename("special.properties"));
            System.setProperty("jacorb.connection.client.connect_timeout", "33704");

            ORB orb = ORB.init(new String[] {}, props);

            int timeout = ((org.jacorb.orb.ORB) orb).getConfiguration()
                    .getAttributeAsInteger(
                            "jacorb.connection.client.connect_timeout", 0);
            assertEquals(33705, timeout);

            orb.destroy();
        }
        finally
        {
            deletePropertiesFile ("target/test-classes/orb.properties");
            deletePropertiesFile ("target/test-classes/myorbid.properties");
            deletePropertiesFile ("myorbid.properties");
            deletePropertiesFile ("special.properties");
        }
    }

    /**
     * Check that a property specified in the System properties overrides
     * most other ways of specifying that property.
     */
    @Test
    public void testSystemPrecedence() throws Exception
    {
        try
        {
            createPropertiesFile ("target/test-classes/orb.properties",
                                  "jacorb.connection.client.connect_timeout=33700");
            createPropertiesFile ("myorbid.properties",
                                  "jacorb.connection.client.connect_timeout=33701");
            createPropertiesFile ("special.properties",
                                  "jacorb.connection.client.connect_timeout=33702");
            createPropertiesFile ("target/test-classes/myorbid.properties",
                                  "jacorb.connection.client.connect_timeout=33703");

            Properties props = new Properties();
            props.put("org.omg.CORBA.ORBClass", "org.jacorb.orb.ORB");
            props.put("org.omg.CORBA.ORBSingletonClass",
                      "org.jacorb.orb.ORBSingleton");

            System.setProperty("jacorb.config.log.verbosity", "4");
            System.setProperty("jacorb.config.dir", TestUtils.testHome());
            System.setProperty("ORBid", "myorbid");
            System.setProperty("custom.props", getConfigFilename("special.properties"));
            System.setProperty("jacorb.connection.client.connect_timeout", "33704");

            ORB orb = ORB.init(new String[] {}, props);

            int timeout = ((org.jacorb.orb.ORB) orb).getConfiguration()
                    .getAttributeAsInteger(
                            "jacorb.connection.client.connect_timeout", 0);
            assertEquals(33704, timeout);

            orb.destroy();
        }
        finally
        {
            deletePropertiesFile ("target/test-classes/orb.properties");
            deletePropertiesFile ("myorbid.properties");
            deletePropertiesFile ("special.properties");
        }
    }

    /**
     * Check that a property specified in orbid.properties overrides
     * most other ways of specifying that property.
     */
    @Test
    public void testOrbIdClasspathPrecedence() throws Exception
    {
        try
        {
            createPropertiesFile ("target/test-classes/orb.properties",
                                  "jacorb.connection.client.connect_timeout=33700");
            createPropertiesFile ("myorbid.properties",
                                  "jacorb.connection.client.connect_timeout=33701");
            createPropertiesFile ("target/test-classes/myorbid.properties",
                                  "jacorb.connection.client.connect_timeout=33703");

            Properties props = new Properties();
            props.put("org.omg.CORBA.ORBClass", "org.jacorb.orb.ORB");
            props.put("org.omg.CORBA.ORBSingletonClass",
                      "org.jacorb.orb.ORBSingleton");

            System.setProperty("jacorb.config.log.verbosity", "4");
            System.setProperty("jacorb.config.dir", TestUtils.testHome());
            System.setProperty("ORBid", "myorbid");

            ORB orb = ORB.init(new String[] {}, props);

            int timeout = ((org.jacorb.orb.ORB) orb).getConfiguration()
                    .getAttributeAsInteger(
                            "jacorb.connection.client.connect_timeout", 0);
            assertEquals(33703, timeout);

            orb.destroy();
        }
        finally
        {
            deletePropertiesFile ("target/test-classes/orb.properties");
            deletePropertiesFile ("myorbid.properties");
            deletePropertiesFile ("target/test-classes/myorbid.properties");
        }
    }

    /**
     * Check that a property specified in custom.props overrides
     * most other ways of specifying that property.
     */
    @Test
    public void testCustomPropsPrecedence() throws Exception
    {
        try
        {
            createPropertiesFile ("target/test-classes/orb.properties",
                                  "jacorb.connection.client.connect_timeout=33700");
            createPropertiesFile ("myorbid.properties",
                                  "jacorb.connection.client.connect_timeout=33701");
            createPropertiesFile ("special.properties",
                                  "jacorb.connection.client.connect_timeout=33702");

            Properties props = new Properties();
            props.put("org.omg.CORBA.ORBClass", "org.jacorb.orb.ORB");
            props.put("org.omg.CORBA.ORBSingletonClass",
                      "org.jacorb.orb.ORBSingleton");

            System.setProperty("jacorb.config.log.verbosity", "4");
            System.setProperty("jacorb.config.dir", TestUtils.testHome());
            System.setProperty("ORBid", "myorbid");
            System.setProperty("custom.props", getConfigFilename("special.properties"));

            ORB orb = ORB.init(new String[] {}, props);

            int timeout = ((org.jacorb.orb.ORB) orb).getConfiguration()
                    .getAttributeAsInteger(
                            "jacorb.connection.client.connect_timeout", 0);
            assertEquals(33702, timeout);

            orb.destroy();
        }
        finally
        {
            deletePropertiesFile ("target/test-classes/orb.properties");
            deletePropertiesFile ("myorbid.properties");
            deletePropertiesFile ("special.properties");
        }
    }

    /**
     * Check that a property specified in orbid.properties overrides
     * the same property as defined in orb.properties.
     */
    @Test
    public void testOrbIdConfigDirPrecedence() throws Exception
    {
        try
        {
            createPropertiesFile ("target/test-classes/orb.properties",
                                  "jacorb.connection.client.connect_timeout=33700");
            createPropertiesFile ("myorbid.properties",
                                  "jacorb.connection.client.connect_timeout=33701");

            Properties props = new Properties();
            props.put("org.omg.CORBA.ORBClass", "org.jacorb.orb.ORB");
            props.put("org.omg.CORBA.ORBSingletonClass",
                      "org.jacorb.orb.ORBSingleton");

            System.setProperty("jacorb.config.log.verbosity", "4");
            System.setProperty("jacorb.config.dir", TestUtils.testHome());
            System.setProperty("ORBid", "myorbid");

            ORB orb = ORB.init(new String[] {}, props);

            int timeout = ((org.jacorb.orb.ORB) orb).getConfiguration()
                    .getAttributeAsInteger(
                            "jacorb.connection.client.connect_timeout", 0);
            assertEquals(33701, timeout);

            orb.destroy();
        }
        finally
        {
            deletePropertiesFile ("target/test-classes/orb.properties");
            deletePropertiesFile ("myorbid.properties");
        }
    }

    @Test
    public void testAppletConfiguration() throws Exception
    {
        try
        {
            createPropertiesFile ("target/test-classes/orb.properties",
                                  "jacorb.connection.client.connect_timeout=33320");
            createPropertiesFile ("target/test-classes/myapplet.properties",
                                  "jacorb.connection.client.connect_timeout=33321");
            createPropertiesFile ("target/test-classes/applet-special.properties",
                                  "jacorb.connection.client.connect_timeout=33322");

            ORB orb = ORB.init(new String[] {}, null);
            Properties props = new Properties();
            props.put ("jacorb.config.log.verbosity", "4");
            props.put ("jacorb.connection.client.connect_timeout", "33323");
            props.put ("ORBid", "myapplet");
            props.put ("custom.props", "applet-special.properties");
            Configuration config = JacORBConfiguration.getConfiguration(props, orb, true);
            int timeout = config.getAttributeAsInteger(
                            "jacorb.connection.client.connect_timeout", 0);
            assertEquals(33323, timeout);
            orb.destroy();
        }
        finally
        {
            deletePropertiesFile ("target/test-classes/orb.properties");
            deletePropertiesFile ("target/test-classes/myapplet.properties");
            deletePropertiesFile ("target/test-classes/applet-special.properties");
        }
    }


    /**
     * Verify that ORB.init() properties end up in the JacORB configuration.
     */
    @Test
    public void testOrbInitSingletonProperties() throws Exception
    {
        Properties props = new Properties();
        props.put("org.omg.CORBA.ORBClass", "org.jacorb.orb.ORB");
        props.put("org.omg.CORBA.ORBSingletonClass",
                  "org.jacorb.orb.ORBSingleton");
        props.put("jacorb.connection.client.connect_timeout", "33707");

        ORB orb = ORB.init(new String[] {}, props);

        int timeout = ((org.jacorb.orb.ORB) orb).getConfiguration()
                    .getAttributeAsInteger(
                            "jacorb.connection.client.connect_timeout", 0);
        assertEquals(33707, timeout);

        ORB singleton = ORB.init ();

        timeout = ((org.jacorb.orb.ORBSingleton)singleton).getConfiguration()
                 .getAttributeAsInteger(
                         "jacorb.connection.client.connect_timeout", 0);
        assertEquals(33707, timeout);

        orb.destroy();
    }




    /**
     * Verify that properties are cached in both string and optimised
     * caches.
     */
    @Test
    public void testCacheProperties() throws Exception
    {
        Properties props = new Properties();
        props.put("org.omg.CORBA.ORBClass", "org.jacorb.orb.ORB");
        props.put("org.omg.CORBA.ORBSingletonClass",
                  "org.jacorb.orb.ORBSingleton");
        props.put("jacorb.connection.client.connect_timeout", "33707");

        ORB orb = ORB.init(new String[] {}, props);

        int timeout = ((org.jacorb.orb.ORB) orb).getConfiguration()
                    .getAttributeAsInteger(
                            "jacorb.connection.client.connect_timeout", 0);
        assertEquals(33707, timeout);

        timeout = Integer.valueOf
        (
            ((org.jacorb.orb.ORB) orb).getConfiguration().getAttribute(
                "jacorb.connection.client.connect_timeout", "0")
        );
        assertEquals(33707, timeout);

        orb.destroy();
    }



    /**
     * Convenience method for creating an os-dependent filename relative
     * to the test home directory.
     */
    private String getConfigFilename (String name)
    {
        return TestUtils.osDependentPath
        (
            TestUtils.testHome() + "/" + name
        );
    }

    private void createPropertiesFile (String name, String content) throws IOException
    {
        File file = new File(TestUtils.testHome(), name);
        File parent = file.getParentFile();

        parent.mkdirs();
        PrintWriter out = new PrintWriter (new FileWriter (file));
        out.println (content);
        out.close();
    }

    private void deletePropertiesFile (String name)
    {
        File f = new File(TestUtils.testHome(), name);
        f.delete();
    }
}
