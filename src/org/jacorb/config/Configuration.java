package org.jacorb.config;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2004 Gerald Brose.
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
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.logger.*;

import java.io.*;
import java.util.*;

import org.jacorb.orb.ORB;
import org.jacorb.util.ObjectUtil;

/**
 * ORB configuration objects are read-only representations of files with
 * configuration properties.
 *
 * ORB configuration options for a given name are looked up and loaded as follows:
 * <ol>
 * <li>System properties are loaded first, but only to get properties
 *     that affect further property loading
 * <li>the file <tt>orb.properties</tt> is loaded from java.home/lib
 *     and user.home if it exists
 * <li>if the ORBid is property is set, the file <tt>ORBid.properties</tt> is 
 *     loaded from jacorb.config.dir/etc, if that exists, or jacorb.home/etc, or '.'
 *     If ORBid is not set, the default file <tt>jacorb.properties</tt>
 *     is loaded from these places.
 * <li>Custom properties are loaded from each file name listed int the system property
 *     <tt>custom.props</tt>
 * <li>To also support packaged servers in jar files, the configuration
 *     file lookup mechanism finally tries to load named properties files 
 *     (<tt>ORBid.properties</tt>, or <tt>jacorb.properties</tt>) from 
 *     the classpath, if it cannot find them in the config dictionary.
 * <li> After all property files have been loaded, the System properties are
 *      loaded again, so that command-line properties take precedence
 * <li> Finally, properties hard-coded and passed in through ORB.init() are
 *      loaded.
 *</ol>
 *
 * The Configuration object is also used by JacORB components to
 * retreive their Logger objects.
 * 
 * @author Gerald Brose, XTRADYNE Technologies
 * @version $Id$
 */

public class Configuration
    extends org.apache.avalon.framework.configuration.DefaultConfiguration
{
    private static final String fileSuffix = ".properties";
    private static final String COMMON_PROPS = "orb" + fileSuffix;

    private static final String TRUE = "true";
    private static final String ON = "on";
    private static final String EMPTY_STR = "";
    
    private static final int DEFAULT_LOG_LEVEL = 0;

    private Configuration config;
    private String configName; 
    private ORB orb = null;

    /** root logger instance for this configuration */
    private Logger logger = null;
    
    /**  logger factory used to create loggers */
    private LoggerFactory loggerFactory = null;

    /**  default class name for logger factory */
    private final String loggerFactoryClzName = 
       "org.jacorb.config.LogKitLoggerFactory";


    /**
     * Create a configuration with a given name and load configuration
     * properties from the file <name>.properties
     */

//     Configuration(String name)
//         throws ConfigurationException
//     {
//         super(name);
//         init(name, null);
//         initLogging();
//     }

    /**
     * Factory method
     */

    public static Configuration getConfiguration(Properties props, 
                                                 ORB orb,
                                                 boolean isApplet)
        throws ConfigurationException
    {
        // determine the ORBId, if set, so we can locate the corresponding
        // configuration
        String orbID = "jacorb"; // default id 
        String myOrbID = isApplet ? null : System.getProperty("ORBid");

        if( props != null )
        {
            // props override system properties
            String tmp = (String)props.get("ORBid");
            if( tmp != null )
                myOrbID = tmp; 
        }

        if (myOrbID != null )
        {
            // check for legal values
            if (myOrbID.equals("orb") || myOrbID.equals("jacorb"))
                throw new ConfigurationException("Illegal orbID, <" + 
                                                  myOrbID + "> is reserved");
            else
                orbID = myOrbID;
        }

        return new Configuration(orbID, props, orb, isApplet);
    }


    /**
     * Create a configuration using the properties passed
     * into ORB.init()
     */

    private Configuration(String name, 
                          Properties orbProperties, 
                          ORB orb,
                          boolean isApplet)
        throws ConfigurationException
    {
        super(name);
        this.orb = orb;
        
        if (isApplet)
        {
            initApplet(name, orbProperties);
        }
        else
        {
            init(name, orbProperties);
        }

        initLogging();
    }

    /**
     * loads properties from files. 
     *
     * Properties are loaded in the following order, with later
     * properties overriding earlier ones: 1) System properties
     * (incl. command line), to get properties that affect further
     * property loading 2) orb.properties file 3) specific
     * configuration file for the ORB (if any) 4) System properties
     * again, so that command line args take precedence over the above
     * 5) the ORB properties set in the client code and passed in
     * through ORB.init().
     * (Note that these will thus always take effect!)
     *
     * @param name the name for the ORB instance, may not be null. 
     */

    private void init(String name, Properties orbProperties)
        throws ConfigurationException
   {
       if( name == null )
           throw new ConfigurationException("Illegal null value for ORB name!");
       String separator = System.getProperty("file.separator");
       String home = System.getProperty("user.home");
       String lib = System.getProperty("java.home");
       boolean loaded = false;

       // 1) load system properties to grab any command line properties
       //    that will influence further property loading
       setAttributes(System.getProperties());

       int logLevel = getAttributeAsInteger("jacorb.config.log.verbosity",DEFAULT_LOG_LEVEL);
       
       // 2) look for orb.properties       
       // look for common properties files in java.home/lib first
       Properties commonProps = 
           loadPropertiesFromFile( lib + separator + "lib" + separator + COMMON_PROPS);
       
       if (commonProps!= null)
       {
            setAttributes(commonProps);
            // we don't have proper logging at this stage yet, so we can only
            // log to the console, but we check if that is explicitly disallowed
            logLevel = getAttributeAsInteger("jacorb.config.log.verbosity",DEFAULT_LOG_LEVEL);
            loaded = true;

            if (logLevel > 2)
                System.out.println("[ base configuration loaded from file " + 
                                   lib + separator + "lib" + separator + COMMON_PROPS + " ]");
       }
       
       // look for common properties files in user.home next
       commonProps = 
           loadPropertiesFromFile( home + separator + COMMON_PROPS );
       
       if (commonProps!= null)
       {
           setAttributes(commonProps);
           loaded = true;

           logLevel = getAttributeAsInteger("jacorb.config.log.verbosity",DEFAULT_LOG_LEVEL);
           if (logLevel > 2)
               System.out.println("[ base configuration loaded from file " + 
                                   home + separator + COMMON_PROPS + " ]");
       }
       
       // look for common properties files on the classpath next
       commonProps = 
           loadPropertiesFromClassPath( COMMON_PROPS );
       
       if (commonProps!= null)
       {
           loaded = true;
           setAttributes(commonProps);
           logLevel = getAttributeAsInteger("jacorb.config.log.verbosity",DEFAULT_LOG_LEVEL);
           if (logLevel > 2)
               System.out.println("[ base configuration loaded from classpath " + 
                                    COMMON_PROPS + " ]");
       }
       
 
       // 3) look for specific properties file
       String configDir = 
           getAttribute("jacorb.config.dir", "");
       
       if (configDir.length() == 0)
           configDir = getAttribute ("jacorb.home", "");
       
       if (configDir.length() != 0 )
           configDir += separator + "etc";
       else
       {
           logLevel = getAttributeAsInteger("jacorb.config.log.verbosity",DEFAULT_LOG_LEVEL);
           if (logLevel > 0)
               System.err.println("[ jacorb.home unset! Will use '.' ]");
           configDir = ".";
       }
       
       String propFileName = configDir + separator + name + fileSuffix;
       
       // now load properties file from file system
       Properties orbConfig = loadPropertiesFromFile(propFileName );
       
       if (orbConfig!= null)
       {
           setAttributes(orbConfig);
           loaded = true;

           logLevel = getAttributeAsInteger("jacorb.config.log.verbosity",DEFAULT_LOG_LEVEL);
           if (logLevel > 2)
               System.out.println("[ configuration " + name + 
                                  " loaded from file " + propFileName + " ]");
       }
       else
       {
           logLevel = getAttributeAsInteger("jacorb.config.log.verbosity",DEFAULT_LOG_LEVEL);
           if (logLevel > 0)
               System.err.println("[ File " + propFileName + " for configuration " + name + 
                                  " not found ]");
       }
       
       // 4) look for additional custom properties files
       List customPropFileNames = getAttributeList("custom.props");

       if (!customPropFileNames.isEmpty())
       {
           for (Iterator iter = customPropFileNames.iterator(); iter.hasNext();)
           {
                String fileName = ((String)iter.next());
                Properties customProps = loadPropertiesFromFile(fileName);
                if (customProps!= null)
                {
                    setAttributes(customProps);
                    loaded = true;

                    logLevel = getAttributeAsInteger("jacorb.config.log.verbosity",DEFAULT_LOG_LEVEL);
                    if (logLevel > 2)
                        System.out.println("[ custom properties loaded from file " + 
                                           fileName + " ]");
                }
                else
                {
                    if (logLevel > 0)
                        System.err.println("[ custom properties not found in "  + 
                                           fileName + " ]");
                }
           }
       }

       // now load properties file from classpath
       orbConfig = loadPropertiesFromClassPath( name + fileSuffix );
       if (orbConfig!= null)
       {
           setAttributes(orbConfig);
           loaded = true;

           logLevel = 
               getAttributeAsInteger("jacorb.config.log.verbosity",DEFAULT_LOG_LEVEL);
           if (logLevel > 2)
               System.out.println("[ configuration " + name + " loaded from classpath]");
       }

       // 5) load system properties again, so that
       //    command line args override properties from files
       setAttributes( System.getProperties() );
       
       // 6) load properties passed to ORB.init(), these will override any
       // settings in config files or system properties!
       if (orbProperties != null)
       {
           loaded = true;
           setAttributes(orbProperties);
       }

       if (!loaded)
       {
           // print a warning....
           System.out.println("[ No configuration properties found for configuration " + name + " ]");
       }

    }

    /**
     * loads properties via classloader. 
     *
     * Properties are loaded in the following order, with later properties
     * overriding earlier ones: 1) Properties from ORB.init(), to get
     * properties that affect further property loading 2) orb.properties 3)
     * specific configuration file for the ORB (if any) 4) the ORB properties
     * set in the client code and passed in through ORB.init().  (Note that
     * these will thus always take effect!)
     *
     * @param name the name for the ORB instance, may not be null. 
     */

    private void initApplet(String name, Properties orbProperties)
        throws ConfigurationException
   {
       if( name == null )
           throw new ConfigurationException("Illegal null value for ORB name!");
       boolean loaded = false;

       // 1) load system properties to grab any command line properties
       //    that will influence further property loading
       setAttributes(orbProperties);

       int logLevel = getAttributeAsInteger("jacorb.config.log.verbosity",
                                            DEFAULT_LOG_LEVEL);
       
       // 2) look for orb.properties
       // look for common properties files on the classpath next
       Properties commonProps = 
           loadPropertiesFromClassPath( COMMON_PROPS );
       
       if (commonProps!= null)
       {
           loaded = true;
           setAttributes(commonProps);
           logLevel = getAttributeAsInteger("jacorb.config.log.verbosity",
                                            DEFAULT_LOG_LEVEL);
           if (logLevel > 2)
               System.out.println("[ base configuration loaded from classpath " + 
                                    COMMON_PROPS + " ]");
       }
       
 
       // 3) look for specific properties file
       String propFileName = name + fileSuffix;
       Properties orbConfig = loadPropertiesFromClassPath(propFileName );
       
       if (orbConfig!= null)
       {
           setAttributes(orbConfig);
           loaded = true;

           logLevel = getAttributeAsInteger("jacorb.config.log.verbosity",
                                            DEFAULT_LOG_LEVEL);
           if (logLevel > 2)
               System.out.println("[ configuration " + name + 
                                  " loaded from classpath " + propFileName + 
                                  " ]");
       }
       else
       {
           logLevel = getAttributeAsInteger("jacorb.config.log.verbosity",
                                            DEFAULT_LOG_LEVEL);
           if (logLevel > 0)
               System.err.println("[ File " + propFileName + 
                                  " for configuration " + name + 
                                  " not found in classpath]");
       }
       
       // 4) look for additional custom properties files
       List customPropFileNames = getAttributeList("custom.props");

       if (!customPropFileNames.isEmpty())
       {
           for (Iterator iter = customPropFileNames.iterator(); iter.hasNext();)
           {
                String fileName = ((String)iter.next());
                Properties customProps = loadPropertiesFromClassPath(fileName);
                if (customProps!= null)
                {
                    setAttributes(customProps);
                    loaded = true;

                    logLevel = 
                        getAttributeAsInteger("jacorb.config.log.verbosity",
                                              DEFAULT_LOG_LEVEL);
                    if (logLevel > 2)
                        System.out.println(
                            "[ custom properties loaded from classpath " + 
                            fileName + " ]");
                }
                else
                {
                    if (logLevel > 0)
                        System.err.println(
                            "[ custom properties " + fileName + 
                            "not found in classpath ]");
                }
           }
       }
       
       // 5) load properties passed to ORB.init(), these will override any
       // settings in config files or system properties!
       if (orbProperties != null)
       {
           loaded = true;
           setAttributes(orbProperties);
       }

       if (!loaded)
       {
           // print a warning....
           System.out.println(
               "[ No configuration properties found for configuration " + 
               name + " ]");
       }
    }


    /**
     * set attributes of this configuration using properties
     */

    void setAttributes(Properties properties)
    {
        for (Iterator iter=properties.keySet().iterator(); iter.hasNext();)
        {
            Object k = iter.next();
            // Some lunatics illegally put non String objects into System props
            // as keys / values - we check for both and ignore them.      
            if (!(k instanceof String)) continue;
            String key = (String)k;
            Object value = properties.get(key);
            if (value instanceof String || value == null)
            {
                setAttribute(key, (String)value);
            }
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
            //            io.printStackTrace(); //debug only
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
            java.net.URL url = 
                Thread.currentThread().getContextClassLoader().getResource(name);
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


    /**
     * Set up JacORB logging. Will create logger factory and root
     * logger object according to configuration parameters. The value
     * of the property <tt>jacorb.log.loggerFactory</tt> determines the
     * logger factory class name that is used to create the root logger.
     *
     * @since 2.0 beta 3
     */

    private void initLogging()
        throws ConfigurationException
    {
        String logFileName = 
            getAttribute("jacorb.logfile", "");

        int maxLogSize = 
            getAttributeAsInteger( "jacorb.logfile.maxLogSize", 0 );

        if ( !logFileName.equals(""))
        {
            // Convert $implname postfix to implementation name
            if (logFileName.endsWith("$implname"))
            {
                logFileName = logFileName.substring (0, logFileName.length () - 9);

                if ( !getAttribute("jacorb.implname","").equals(""))
                {
                    logFileName += getAttribute("jacorb.implname","");
                }
                else
                {
                    // Just in case implname has not been set
                    logFileName += "log";
                }
            }
        }

        String clzName = getAttribute("jacorb.log.loggerFactory","");
        Class loggerFactoryClz = null;

        try
        {
            if ( !clzName.equals(""))
            {
                loggerFactoryClz = org.jacorb.util.ObjectUtil.classForName(clzName);
            }
            else
            {
                loggerFactoryClz = org.jacorb.util.ObjectUtil.classForName(loggerFactoryClzName);
            }
            loggerFactory = (LoggerFactory)loggerFactoryClz.newInstance();
            loggerFactory.configure( this );
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        if (loggerFactory == null)
        {
            System.err.println("Configuration Error, could not create logger!");
        }

        if (!logFileName.equals(""))
        {
            try
            {
                loggerFactory.setDefaultLogFile(logFileName, maxLogSize);
                //logger = loggerFactory.getNamedRootLogger("jacorb");
                logger = 
                    loggerFactory.getNamedLogger("jacorb",logFileName, maxLogSize);
            }
            catch (IOException e)
            {
                logger = loggerFactory.getNamedRootLogger("jacorb");
                if( logger.isErrorEnabled())
                {
                    logger.error("Could not create logger with file target: " + logFileName +
                                 ", falling back to console log!");
                }
            }
        }
        else
        {
            logger = loggerFactory.getNamedRootLogger("jacorb" );
        }
    }

    /**
     * @return the ORB for which this configuration was created
     */

    public ORB getORB()
    {
        return orb;
    }


    /**
     * @param name the name of the logger, which also functions
     *        as a log category
     * @return a Logger for a given name
     */

    public Logger getNamedLogger(String name)
    {
        return loggerFactory.getNamedLogger(name);
    }
    
    public static final String getLoggerName(Class clz)
    {
        String packageName = clz.getPackage().getName();
        if (packageName != null && packageName.startsWith("org.jacorb"))
        {
            return packageName.substring(4);
        }
        return packageName;
    }

    /**
     * For a property that has a list of comma-separated values,
     * this method returns these values as a list of Strings.
     * If the property is not set, an empty list is returned.
     */

    public List getAttributeList(String key)
    {
        List result = new ArrayList();
        String value = null;

        try
        {
            value = getAttribute(key);
        }
        catch( ConfigurationException ce)
        {
            // ignore
        }

        if (value != null)
        {
            StringTokenizer tok = new StringTokenizer(value, ",");
            while (tok.hasMoreTokens())
                result.add(tok.nextToken().trim());
        }
        return result;
    }

    /**
     * Create an object from the given property. The class's default
     * constructor will be used.
     *
     * @return an object of the class of the keys value, or null, if 
     * no class name is found for the key
     * @throws ConfigurationException if object creation fails.
     */

    public Object getAttributeAsObject( String key )
        throws ConfigurationException
    {
        String className = null;
        try
        {
            className = getAttribute( key );
        }
        catch( Exception e )
        {
            // ignore
        }

        if(  className != null && className.length() > 0 )
        {
            try
            {
                Class c = ObjectUtil.classForName(className);
                return c.newInstance();
            }
            catch( Exception e )
            {
                throw new ConfigurationException( "Unable to build class from key >" +
                                                  key +"<: " + e );
            }
        }
        else
        {
            return null;
        }
    }

    public boolean getAttributeAsBoolean(String key)
        throws ConfigurationException
    {
        String s = getAttribute(key);
        
        if (s != null && s.length() > 0)
        {
            s = s.trim().toLowerCase();
            return ON.equals(s) || TRUE.equals(s);
        }
        else
        {
            return false;
        }
    }

    public boolean getAttributeAsBoolean(String key, boolean defaultValue)
    {
        String s = getAttribute(key, EMPTY_STR);
        
        if (s.length() > 0)
        {
            s = s.trim().toLowerCase();
            return ON.equals(s) || TRUE.equals(s);
        }
        else
        {
            return defaultValue;
        }
    }
}
