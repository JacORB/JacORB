package org.jacorb.util;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2004  Gerald Brose.
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
 */

import org.apache.avalon.framework.configuration.DefaultConfiguration;

import java.io.*;
import java.util.Properties;
import java.util.Iterator;

/**
 * Configuration objects are read-only representations of files with
 * configuration properties. Configuration files for a given name are
 * looked up relative to a configuration directory, with the
 * configuration <name> in the file ${JACORB_CONFIG}/<name>.properties
 * The default value after installation is $JacORB_HOME/etc$.
 *
 * To also support packaged servers in jar files, the configuration
 * file lookup mechanism tries to load properties files from the
 * classpath, if it cannot find them in the config dictionary.
 *
 * Failure to retrieve the configuration file will result in an
 * exception raised byte ORB.init().
 * 
 * @author Gerald Brose, XTRADYNE Technologies
 * @version $Id$
 */

public class Configuration
    extends org.apache.avalon.framework.configuration.DefaultConfiguration
{
    private static final String fileSuffix = ".orb.properties";
    private static final String COMMON_PROPS = "common" + fileSuffix;

    private Configuration config;
    private String configName; 

    /**
     * Create a configuration with a given name and load configuration
     * properties from the file <name>.properties
     */

    public Configuration(String name)
    {
        super(name);
        init(name, null);
    }


    /**
     * Create a configuration using the properties passed
     * into ORB.init()
     */

    public Configuration(String name, Properties orbProperties)
    {
        super(name);
        init(name, orbProperties);
    }

    /**
     * loads properties from files. Properties are loaded int the
     * following order, with later properties overriding earlier ones,
     * 1) System properties (incl. command line)
     * 2) common.orb.properties file
     * 2) specific configuration file for the ORB (if any)
     * 3) the ORB properties set in the client code and passed int through ORB.init(). 
     *    (Note that these will thus always take effect!)
     */

    private void init(String name, Properties orbProperties)
    {
        String separator = System.getProperty("file.separator");
        String home = System.getProperty("user.home");
        String lib = System.getProperty("java.home");

        // 1) include system properties
        setAttributes( System.getProperties() );

        // 2) look for orb.common.properties

        // look for common properties files in java.home/lib first
        Properties commonProps = 
            loadPropertiesFromFile( lib + separator + "lib" + separator + COMMON_PROPS);

        if (commonProps!= null)
            setAttributes(commonProps);

        // look for common properties files in user.home next
        commonProps = 
            loadPropertiesFromFile( home + separator + COMMON_PROPS );
 
        if (commonProps!= null)
            setAttributes(commonProps);

        // look for common properties files on the classpath next
        commonProps = 
            loadPropertiesFromClassPath( COMMON_PROPS );

        if (commonProps!= null)
            setAttributes(commonProps);

        // 3) look for specific properties file

        String configDir = 
            System.getProperty("jacorb.config.dir");

        if (configDir == null)
            configDir = System.getProperty("jacorb.home");

        if (configDir != null )
            configDir += separator + "etc";
        else
        {
            System.err.println("[ jacorb.home unset! Will use '.']");
            configDir = ".";
        }
        
        String propFileName = configDir + separator + name + fileSuffix;

        // now load properties file from file system
        Properties orbConfig = loadPropertiesFromFile(propFileName );

        if (orbConfig!= null)
        {
            System.out.println("[configuration " + name + " loaded from file]");
            setAttributes(orbConfig);
        }

        // now load properties file from classpath
        orbConfig = loadPropertiesFromClassPath( name +  fileSuffix );
        if (orbConfig!= null)
        {
            System.out.println("[configuration " + name + " loaded from classpath]");
            setAttributes(orbConfig);
        }

        // 4) load properties passed to ORB.init(), these will override any
        // settings in config files or system properties!
        if (orbProperties != null)
            setAttributes(orbProperties);
    }

    /**
     * set attributes of this configuration using properties
     */

    void setAttributes(Properties properties)
    {
        System.out.println("--- set Attributes ----");
        for (Iterator iter=properties.keySet().iterator(); iter.hasNext();)
        {
            String key = (String)iter.next();

            System.out.println("setting (" + key + "," +
                               (String)properties.get(key) + ")");

            setAttribute(key, (String)properties.get(key));
        }
    }


    /**
     * Loads properties from a file
     * @param fileName the name of a properties file
     * @return a properties object or null, if fileName not found
     */

    private static Properties loadPropertiesFromFile(String fileName)
    {
        try
        {
            BufferedInputStream bin =
                new BufferedInputStream( new FileInputStream(fileName));
            Properties result = new Properties();
            result.load(bin);
            return result;
        }
        catch( java.io.IOException io )
        {
            io.printStackTrace(); //debug only
            return null;
        }
    }


    /**
     * load properties file from classpath
     * @param name the name of the properties file.
     * @return a properties object or null, if name not found
     */

    private static Properties loadPropertiesFromClassPath(String name)
    {
        Properties result = null;
        try
        {
            java.net.URL url = ClassLoader.getSystemResource(name);
            if (url!=null)           
            {
                result = new Properties();
                result.load( url.openStream() );
            }
        }
        catch (java.io.IOException ioe)
        {
            ioe.printStackTrace(); //debug only
        }
        return result;
    }





}
