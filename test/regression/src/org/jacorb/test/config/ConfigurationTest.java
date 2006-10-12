package org.jacorb.test.config;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 2006 Gerald Brose
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

import java.util.Properties;
import java.io.*;
import org.omg.CORBA.*;
import junit.framework.*;
import org.jacorb.test.common.*;
import org.jacorb.config.*;

/**
 * Tests the various configuration mechanisms, properties files, and
 * property precedence in JacORB.
 * @author Alphonse Bendt / Andre Spiegel
 * @version $Id$
 */
public class ConfigurationTest extends JacORBTestCase
{
    public ConfigurationTest (String name)
    {
        super (name);
    }
    
    public static Test suite()
    {
        TestSuite result = new TestSuite("Configuration Tests");
        result.addTestSuite(ConfigurationTest.class);
        return result;
    }
    
    /**
     * Simply a unit test of method JacORBConfiguration.getLoggerName()
     */
    public void testGetLoggerName() throws Exception
    {
        Configuration config = JacORBConfiguration.getConfiguration(new Properties(), null, false);
        assertEquals("jacorb.test.config", config.getLoggerName(getClass()));
    }

    /**
     * Place an orb.properties file on the classpath and verify that it gets
     * loaded by the ORB.
     */
    public void testOrbPropertiesClasspath() throws Exception
    {
        try
        {
            createPropertiesFile("classes/orb.properties",
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
        } 
        finally
        {
            deletePropertiesFile ("classes/orb.properties");
        }
    }
    
    public void testOrbidPropertiesConfigDir() throws Exception
    {
        try
        {
            createPropertiesFile ("etc/mytestorbid.properties",
                                  "jacorb.connection.client.connect_timeout=33098");

            Properties props = new Properties();
            props.put("org.omg.CORBA.ORBClass", "org.jacorb.orb.ORB");
            props.put("org.omg.CORBA.ORBSingletonClass",
                    "org.jacorb.orb.ORBSingleton");

            // Make sure to copy the properties to a new Properties object,
            // because otherwise oldProps will just be an alias to the 
            // "live" set of properties.
            Properties oldProps = new Properties (System.getProperties());
            System.setProperty("jacorb.config.dir", TestUtils
                    .osDependentPath(TestUtils.testHome()));
            System.setProperty("ORBid", "mytestorbid");

            ORB orb = ORB.init(new String[] {}, props);

            System.setProperties(oldProps);

            int timeout = ((org.jacorb.orb.ORB) orb).getConfiguration()
                    .getAttributeAsInteger(
                            "jacorb.connection.client.connect_timeout", 0);
            assertEquals(33098, timeout);
        } 
        finally
        {
            deletePropertiesFile ("etc/mytestorbid.properties");
        }
    }
    
    /**
     * Place an orbid.properties file on the classpath and verify
     * that it gets loaded.
     */
    public void testOrbidPropertiesClasspath() throws Exception
    {
        try
        {
            createPropertiesFile("classes/myownorbid.properties",
                                 "jacorb.connection.client.connect_timeout=33077");

            Properties props = new Properties();
            props.put("org.omg.CORBA.ORBClass", "org.jacorb.orb.ORB");
            props.put("org.omg.CORBA.ORBSingletonClass",
                    "org.jacorb.orb.ORBSingleton");

            Properties oldProps = new Properties(System.getProperties());
            System.setProperty("ORBid", "myownorbid");

            ORB orb = ORB.init(new String[] {}, props);

            System.setProperties(oldProps);

            int timeout = ((org.jacorb.orb.ORB) orb).getConfiguration()
                    .getAttributeAsInteger(
                            "jacorb.connection.client.connect_timeout", 0);
            assertEquals(33077, timeout);
        }
        finally
        {
            deletePropertiesFile ("classes/myownorbid.properties");
        }
    }

    /**
     * Get properties from a custom.props file and verify that they
     * have been loaded.
     */
    public void testCustomProps() throws Exception
    {
        try
        {
            createPropertiesFile("etc/custom.properties",
                                 "jacorb.connection.client.connect_timeout=33100");

            Properties props = new Properties();
            props.put("org.omg.CORBA.ORBClass", "org.jacorb.orb.ORB");
            props.put("org.omg.CORBA.ORBSingletonClass",
                    "org.jacorb.orb.ORBSingleton");

            Properties oldProps = new Properties(System.getProperties());
            System.setProperty("custom.props", 
                               getConfigFilename ("etc/custom.properties"));

            ORB orb = ORB.init(new String[] {}, props);

            System.setProperties(oldProps);

            int timeout = ((org.jacorb.orb.ORB) orb).getConfiguration()
                    .getAttributeAsInteger(
                            "jacorb.connection.client.connect_timeout", 0);
            assertEquals(33100, timeout);
        } 
        finally
        {
            deletePropertiesFile ("etc/custom.properties");
        }
    }

    /**
     * Verify that system properties end up in the JacORB configuration.
     */
    public void testSystemProperties() throws Exception
    {
        Properties props = new Properties();
        props.put("org.omg.CORBA.ORBClass", "org.jacorb.orb.ORB");
        props.put("org.omg.CORBA.ORBSingletonClass",
                  "org.jacorb.orb.ORBSingleton");

        Properties oldProps = new Properties (System.getProperties());
        System.setProperty ("jacorb.connection.client.connect_timeout", "33105");
        ORB orb = ORB.init(new String[] {}, props);

        System.setProperties(oldProps);

        int timeout = ((org.jacorb.orb.ORB) orb).getConfiguration()
                    .getAttributeAsInteger(
                            "jacorb.connection.client.connect_timeout", 0);
        assertEquals(33105, timeout);
    }

    /**
     * Verify that ORB.init() properties end up in the JacORB configuration.
     */
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
    }

    /**
     * A property is set in every conceivable way, and we verify that
     * the value from ORB.init() takes precedence over all the others.
     */
    public void testOrbInitPrecedence() throws Exception
    {
        try
        {
            createPropertiesFile ("classes/orb.properties",
                                  "jacorb.connection.client.connect_timeout=33700");
            createPropertiesFile ("etc/myorbid.properties",
                                  "jacorb.connection.client.connect_timeout=33701");
            createPropertiesFile ("etc/special.properties",
                                  "jacorb.connection.client.connect_timeout=33702");
            createPropertiesFile ("classes/myorbid.properties",
                                  "jacorb.connection.client.connect_timeout=33703");
            
            Properties props = new Properties();
            props.put("org.omg.CORBA.ORBClass", "org.jacorb.orb.ORB");
            props.put("org.omg.CORBA.ORBSingletonClass",
                      "org.jacorb.orb.ORBSingleton");
            props.put("jacorb.connection.client.connect_timeout", "33705");
        
            Properties oldProps = new Properties(System.getProperties());
            System.setProperty("jacorb.config.log.verbosity", "4");
            System.setProperty("jacorb.config.dir", TestUtils.testHome());
            System.setProperty("ORBid", "myorbid");
            System.setProperty("custom.props", getConfigFilename("etc/special.properties"));
            System.setProperty("jacorb.connection.client.connect_timeout", "33704");
            
            ORB orb = ORB.init(new String[] {}, props);

            System.setProperties (oldProps);
            
            int timeout = ((org.jacorb.orb.ORB) orb).getConfiguration()
                    .getAttributeAsInteger(
                            "jacorb.connection.client.connect_timeout", 0);
            assertEquals(33705, timeout);
        }
        finally
        {
            deletePropertiesFile ("classes/orb.properties");
            deletePropertiesFile ("etc/myorbid.properties");
            deletePropertiesFile ("etc/special.properties");
        }
    }

    /**
     * Check that a property specified in the System properties overrides
     * most other ways of specifying that property.
     */
    public void testSystemPrecedence() throws Exception
    {
        try
        {
            createPropertiesFile ("classes/orb.properties",
                                  "jacorb.connection.client.connect_timeout=33700");
            createPropertiesFile ("etc/myorbid.properties",
                                  "jacorb.connection.client.connect_timeout=33701");
            createPropertiesFile ("etc/special.properties",
                                  "jacorb.connection.client.connect_timeout=33702");
            createPropertiesFile ("classes/myorbid.properties",
                                  "jacorb.connection.client.connect_timeout=33703");
            
            Properties props = new Properties();
            props.put("org.omg.CORBA.ORBClass", "org.jacorb.orb.ORB");
            props.put("org.omg.CORBA.ORBSingletonClass",
                      "org.jacorb.orb.ORBSingleton");
        
            Properties oldProps = new Properties (System.getProperties());
            System.setProperty("jacorb.config.log.verbosity", "4");
            System.setProperty("jacorb.config.dir", TestUtils.testHome());
            System.setProperty("ORBid", "myorbid");
            System.setProperty("custom.props", getConfigFilename("etc/special.properties"));
            System.setProperty("jacorb.connection.client.connect_timeout", "33704");
            
            ORB orb = ORB.init(new String[] {}, props);

            System.setProperties (oldProps);
            
            int timeout = ((org.jacorb.orb.ORB) orb).getConfiguration()
                    .getAttributeAsInteger(
                            "jacorb.connection.client.connect_timeout", 0);
            assertEquals(33704, timeout);
        }
        finally
        {
            deletePropertiesFile ("classes/orb.properties");
            deletePropertiesFile ("etc/myorbid.properties");
            deletePropertiesFile ("etc/special.properties");
        }
    }

    /**
     * Check that a property specified in orbid.properties overrides
     * most other ways of specifying that property.
     */
    public void testOrbIdClasspathPrecedence() throws Exception
    {
        try
        {
            createPropertiesFile ("classes/orb.properties",
                                  "jacorb.connection.client.connect_timeout=33700");
            createPropertiesFile ("etc/myorbid.properties",
                                  "jacorb.connection.client.connect_timeout=33701");
            createPropertiesFile ("classes/myorbid.properties",
                                  "jacorb.connection.client.connect_timeout=33703");
            
            Properties props = new Properties();
            props.put("org.omg.CORBA.ORBClass", "org.jacorb.orb.ORB");
            props.put("org.omg.CORBA.ORBSingletonClass",
                      "org.jacorb.orb.ORBSingleton");
        
            Properties oldProps = new Properties (System.getProperties());
            System.setProperty("jacorb.config.log.verbosity", "4");
            System.setProperty("jacorb.config.dir", TestUtils.testHome());
            System.setProperty("ORBid", "myorbid");
            
            ORB orb = ORB.init(new String[] {}, props);

            System.setProperties (oldProps);
            
            int timeout = ((org.jacorb.orb.ORB) orb).getConfiguration()
                    .getAttributeAsInteger(
                            "jacorb.connection.client.connect_timeout", 0);
            assertEquals(33703, timeout);
        }
        finally
        {
            deletePropertiesFile ("classes/orb.properties");
            deletePropertiesFile ("etc/myorbid.properties");
            deletePropertiesFile ("classes/myorbid.properties");
        }
    }

    /**
     * Check that a property specified in custom.props overrides
     * most other ways of specifying that property.
     */
    public void testCustomPropsPrecedence() throws Exception
    {
        try
        {
            createPropertiesFile ("classes/orb.properties",
                                  "jacorb.connection.client.connect_timeout=33700");
            createPropertiesFile ("etc/myorbid.properties",
                                  "jacorb.connection.client.connect_timeout=33701");
            createPropertiesFile ("etc/special.properties",
                                  "jacorb.connection.client.connect_timeout=33702");
            
            Properties props = new Properties();
            props.put("org.omg.CORBA.ORBClass", "org.jacorb.orb.ORB");
            props.put("org.omg.CORBA.ORBSingletonClass",
                      "org.jacorb.orb.ORBSingleton");
        
            Properties oldProps = new Properties (System.getProperties());
            System.setProperty("jacorb.config.log.verbosity", "4");
            System.setProperty("jacorb.config.dir", TestUtils.testHome());
            System.setProperty("ORBid", "myorbid");
            System.setProperty("custom.props", getConfigFilename("etc/special.properties"));
            
            ORB orb = ORB.init(new String[] {}, props);

            System.setProperties (null);
            System.setProperties (oldProps);
            
            int timeout = ((org.jacorb.orb.ORB) orb).getConfiguration()
                    .getAttributeAsInteger(
                            "jacorb.connection.client.connect_timeout", 0);
            assertEquals(33702, timeout);
        }
        finally
        {
            deletePropertiesFile ("classes/orb.properties");
            deletePropertiesFile ("etc/myorbid.properties");
            deletePropertiesFile ("etc/special.properties");
        }
    }

    /**
     * Check that a property specified in orbid.properties overrides
     * the same property as defined in orb.properties.
     */
    public void testOrbIdConfigDirPrecedence() throws Exception
    {
        try
        {
            createPropertiesFile ("classes/orb.properties",
                                  "jacorb.connection.client.connect_timeout=33700");
            createPropertiesFile ("etc/myorbid.properties",
                                  "jacorb.connection.client.connect_timeout=33701");
            
            Properties props = new Properties();
            props.put("org.omg.CORBA.ORBClass", "org.jacorb.orb.ORB");
            props.put("org.omg.CORBA.ORBSingletonClass",
                      "org.jacorb.orb.ORBSingleton");
        
            Properties oldProps = new Properties (System.getProperties());
            System.setProperty("jacorb.config.log.verbosity", "4");
            System.setProperty("jacorb.config.dir", TestUtils.testHome());
            System.setProperty("ORBid", "myorbid");
            
            ORB orb = ORB.init(new String[] {}, props);

            System.setProperties (oldProps);
            
            int timeout = ((org.jacorb.orb.ORB) orb).getConfiguration()
                    .getAttributeAsInteger(
                            "jacorb.connection.client.connect_timeout", 0);
            assertEquals(33701, timeout);
        }
        finally
        {
            deletePropertiesFile ("classes/orb.properties");
            deletePropertiesFile ("etc/myorbid.properties");
        }
    }

    public void testAppletConfiguration() throws Exception
    {
        try
        {
            createPropertiesFile ("classes/orb.properties",
                                  "jacorb.connection.client.connect_timeout=33320");
            createPropertiesFile ("classes/myapplet.properties",
                                  "jacorb.connection.client.connect_timeout=33321");
            createPropertiesFile ("classes/applet-special.properties",
                                  "jacorb.connection.client.connect_timeout=33322");

            Properties props = new Properties();
            props.put ("jacorb.config.log.verbosity", "4");
            props.put ("jacorb.connection.client.connect_timeout", "33323");
            props.put ("ORBid", "myapplet");
            props.put ("custom.props", "applet-special.properties");
            Configuration config = JacORBConfiguration.getConfiguration(props, null, true);
            int timeout = config.getAttributeAsInteger(
                            "jacorb.connection.client.connect_timeout", 0);
            assertEquals(33323, timeout);
        }
        finally
        {
            deletePropertiesFile ("classes/orb.properties");
            deletePropertiesFile ("classes/myapplet.properties");
            deletePropertiesFile ("classes/applet-special.properties");
        }
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
        String path = TestUtils.osDependentPath(TestUtils.testHome() + "/" + name);
        File parent = new File(path).getParentFile();
        parent.mkdirs();
        PrintWriter out = new PrintWriter (new FileWriter (path));
        out.println (content);
        out.close();
    }
    
    private void deletePropertiesFile (String name) throws IOException
    {
        File f = new File (TestUtils.osDependentPath(TestUtils.testHome() + "/" + name));
        f.delete();
    }
}
