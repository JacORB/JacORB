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
    private static final String fileSuffix = ".properties";

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
     * 2) Configuration file, if any
     * 3) ORB properties (set in the code). Note that these will thus 
     *    always take effect!
     */

    private void init(String name, Properties orbProperties)
    {
        BufferedInputStream bin = null;
        Properties properties = new Properties();

        // first include system properties
        setAttributes( System.getProperties() );

        // find config directory:
        String separator = 
            System.getProperty("file.separator");
        String jacorbHome = 
            System.getProperty("jacorb.home");

        if (jacorbHome == null)
        {
            System.err.println("[ jacorb.home unset! Will use '.']");
            jacorbHome = ".";
        }

        String path = jacorbHome + separator + "etc" + separator;

        // now load properties file from file system
        try
        {
            bin =
                new BufferedInputStream(new FileInputStream( path + 
                                                             name +
                                                             fileSuffix));
            properties.load(bin);
            bin.close();

            setAttributes(properties);
            System.out.println("[configuration " + name + " loaded]");
            return;
        }
        catch(java.io.IOException io )
        {
        }

        // now load properties file from classpath
        try
        {
            java.net.URL url = null;
            
            // try first file name
            url = ClassLoader.getSystemResource(name + fileSuffix);
            if (url!=null)
            {
                properties.load( url.openStream() );
                setAttributes(properties);
                System.out.println("[configuration " + name + 
                                   " loaded from classpath resource " +
                                   url + "]");
                return;
            }
            else
            {
                System.err.println("[No configuration for " + name + 
                                   " found on classpath ]");

            }
        }
        catch (java.io.IOException ioe)
        {
            System.err.println("[Error loading configuration " + name + 
                               ": " + ioe.getMessage() + "]");
        }

        // now load propertoes passed to ORB.init(), these will override any
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

}
