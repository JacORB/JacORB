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

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.logger.*;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import org.jacorb.orb.ORB;
import org.jacorb.util.ObjectUtil;

/**
 * @author Gerald Brose
 * @version $Id$
 */

public class JacORBConfiguration
    extends org.apache.avalon.framework.configuration.DefaultConfiguration implements Configuration
{
    private static final String CONFIG_LOG_VERBOSITY = "jacorb.config.log.verbosity";
    private static final String fileSuffix = ".properties";
    private static final String COMMON_PROPS = "orb" + fileSuffix;

    private static final String TRUE = "true";
    private static final String ON = "on";
    private static final String EMPTY_STR = "";

    private static final int DEFAULT_LOG_LEVEL = 0;

    private final ORB orb;

    /** root logger instance for this configuration */
    private Logger logger = null;

    /**  logger factory used to create loggers */
    private LoggerFactory loggerFactory = null;

    /**  default class name for logger factory */
    private static final String loggerFactoryClzName =
       "org.jacorb.config.LogKitLoggerFactory";

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
        String myOrbID = null;
        if ( !isApplet ) {
            try
            {
                myOrbID = System.getProperty("ORBid");
            }
            catch ( SecurityException e )
            {
                isApplet = true;
                println("Could not access system property 'ORBid' - will use default...");
            }
        }

        if( props != null )
        {
            // props override system properties
            String tmp = (String)props.get("ORBid");
            if( tmp != null )
            {
                myOrbID = tmp;
            }
        }

        if (myOrbID != null )
        {
            // check for legal values
            if (myOrbID.equals("orb") || myOrbID.equals("jacorb"))
            {
                throw new ConfigurationException("Illegal orbID, <" +
                                                  myOrbID + "> is reserved");
            }
            orbID = myOrbID;
        }

        return new JacORBConfiguration(orbID, props, orb, isApplet);
    }


    /**
     * Create a configuration using the properties passed
     * into ORB.init()
     */

    private JacORBConfiguration(String name,
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

    private static void println(String mesg)
    {
        System.out.println(mesg); // NOPMD
    }

    private static void printErr(String mesg)
    {
        System.err.println(mesg); // NOPMD
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
       {
           throw new ConfigurationException("Illegal null value for ORB name!");
       }

       String separator = System.getProperty("file.separator");
       String home = System.getProperty("user.home");
       String lib = System.getProperty("java.home");
       boolean loaded = false;

       // 1) load system properties to grab any command line properties
       //    that will influence further property loading
       setAttributes(System.getProperties());

       int logLevel = getAttributeAsInteger(CONFIG_LOG_VERBOSITY,DEFAULT_LOG_LEVEL);

       // 2) look for orb.properties
       // look for common properties files in java.home/lib first
       Properties commonProps =
           loadPropertiesFromFile( lib + separator + "lib" + separator + COMMON_PROPS);

       if (commonProps!= null)
       {
            setAttributes(commonProps);
            // we don't have proper logging at this stage yet, so we can only
            // log to the console, but we check if that is explicitly disallowed
            logLevel = getAttributeAsInteger(CONFIG_LOG_VERBOSITY,DEFAULT_LOG_LEVEL);
            loaded = true;

            if (logLevel > 2)
            {
                println("[ base configuration loaded from file " +
                                   lib + separator + "lib" + separator + COMMON_PROPS + " ]");
            }
       }

       // look for common properties files in user.home next
       commonProps =
           loadPropertiesFromFile( home + separator + COMMON_PROPS );

       if (commonProps!= null)
       {
           setAttributes(commonProps);
           loaded = true;

           logLevel = getAttributeAsInteger(CONFIG_LOG_VERBOSITY,DEFAULT_LOG_LEVEL);
           if (logLevel > 2)
           {
               println("[ base configuration loaded from file " +
                                   home + separator + COMMON_PROPS + " ]");
           }
       }

       // look for common properties files on the classpath next
       commonProps =
           loadPropertiesFromClassPath( COMMON_PROPS );

       if (commonProps!= null)
       {
           loaded = true;
           setAttributes(commonProps);
           logLevel = getAttributeAsInteger(CONFIG_LOG_VERBOSITY,DEFAULT_LOG_LEVEL);
           if (logLevel > 2)
           {
               println("[ base configuration loaded from classpath " +
                                    COMMON_PROPS + " ]");
           }
       }


       // 3) look for specific properties file
       String configDir =
           getAttribute("jacorb.config.dir", "");

       if (configDir.length() == 0)
       {
           configDir = getAttribute ("jacorb.home", "");
       }

       if (configDir.length() != 0 )
       {
           configDir += separator + "etc";
       }
       else
       {
           logLevel = getAttributeAsInteger(CONFIG_LOG_VERBOSITY,DEFAULT_LOG_LEVEL);
           if (logLevel > 0)
           {
               printErr("[ jacorb.home unset! Will use '.' ]");
           }
           configDir = ".";
       }

       String propFileName = configDir + separator + name + fileSuffix;

       // now load properties file from file system
       Properties orbConfig = loadPropertiesFromFile(propFileName );

       if (orbConfig!= null)
       {
           setAttributes(orbConfig);
           loaded = true;

           logLevel = getAttributeAsInteger(CONFIG_LOG_VERBOSITY,DEFAULT_LOG_LEVEL);
           if (logLevel > 2)
           {
               println("[ configuration " + name +
                                  " loaded from file " + propFileName + " ]");
           }
       }
       else
       {
           logLevel = getAttributeAsInteger(CONFIG_LOG_VERBOSITY,DEFAULT_LOG_LEVEL);
           if (logLevel > 0)
           {
               printErr("[ File " + propFileName + " for configuration " + name +
                                  " not found ]");
           }
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

                    logLevel = getAttributeAsInteger(CONFIG_LOG_VERBOSITY,DEFAULT_LOG_LEVEL);
                    if (logLevel > 2)
                    {
                        println("[ custom properties loaded from file " +
                                           fileName + " ]");
                    }
                }
                else
                {
                    if (logLevel > 0)
                    {
                        printErr("[ custom properties not found in "  +
                                           fileName + " ]");
                    }
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
               getAttributeAsInteger(CONFIG_LOG_VERBOSITY,DEFAULT_LOG_LEVEL);
           if (logLevel > 2)
           {
               println("[ configuration " + name + " loaded from classpath]");
           }
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
           println("[ No configuration properties found for configuration " + name + " ]");
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
       {
           throw new ConfigurationException("Illegal null value for ORB name!");
       }
       boolean loaded = false;

       // 1) load system properties to grab any command line properties
       //    that will influence further property loading
       if ( orbProperties != null ) {
           setAttributes(orbProperties);
       }

       int logLevel = getAttributeAsInteger(CONFIG_LOG_VERBOSITY,
                                            DEFAULT_LOG_LEVEL);

       // 2) look for orb.properties
       // look for common properties files on the classpath next
       Properties commonProps =
           loadPropertiesFromClassPath( COMMON_PROPS );

       if (commonProps!= null)
       {
           loaded = true;
           setAttributes(commonProps);
           logLevel = getAttributeAsInteger(CONFIG_LOG_VERBOSITY,
                                            DEFAULT_LOG_LEVEL);
           if (logLevel > 2)
           {
               println("[ base configuration loaded from classpath " +
                                    COMMON_PROPS + " ]");
           }
       }


       // 3) look for specific properties file
       String propFileName = name + fileSuffix;
       Properties orbConfig = loadPropertiesFromClassPath(propFileName );

       if (orbConfig!= null)
       {
           setAttributes(orbConfig);
           loaded = true;

           logLevel = getAttributeAsInteger(CONFIG_LOG_VERBOSITY,
                                            DEFAULT_LOG_LEVEL);
           if (logLevel > 2)
           {
               println("[ configuration " + name +
                                  " loaded from classpath " + propFileName +
                                  " ]");
           }
       }
       else
       {
           logLevel = getAttributeAsInteger(CONFIG_LOG_VERBOSITY,
                                            DEFAULT_LOG_LEVEL);
           if (logLevel > 0)
           {
               printErr("[ File " + propFileName +
                                  " for configuration " + name +
                                  " not found in classpath]");
           }
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
                        getAttributeAsInteger(CONFIG_LOG_VERBOSITY,
                                              DEFAULT_LOG_LEVEL);
                    if (logLevel > 2)
                    {
                        println(
                            "[ custom properties loaded from classpath " +
                            fileName + " ]");
                    }
                }
                else
                {
                    if (logLevel > 0)
                    {
                        printErr(
                            "[ custom properties " + fileName +
                            "not found in classpath ]");
                    }
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
           println(
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
            Object obj = iter.next();
            // Some lunatics illegally put non String objects into System props
            // as keys / values - we check for both and ignore them.
            if (!(obj instanceof String))
            {
                continue;
            }

            String key = (String)obj;
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
            InputStream stream = new FileInputStream(fileName);
            try
            {
                Properties result = new Properties();
                result.load(stream);
                return result;
            }
            finally
            {
                stream.close();
            }
        }
        catch (java.io.FileNotFoundException e)
        {
            // It's okay to ignore this silently: There was just no
            // config file there, so the caller is going to look elsewhere.
            return null;
        }
        catch (java.io.IOException e)
        {
            // This is probably a more severe problem with the config file.
            // Write to the terminal, because we have no logging yet.
            println("could not read config file: " + fileName);
            e.printStackTrace();
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
            final ClassLoader clazzLoader;

            if (Thread.currentThread().getContextClassLoader() != null)
            {
                clazzLoader = Thread.currentThread().getContextClassLoader();
            }
            else
            {
                clazzLoader = JacORBConfiguration.class.getClassLoader();
            }

            java.net.URL url = clazzLoader.getResource(name);
            if (url!=null)
            {
                result = new Properties();
                final InputStream stream = url.openStream();
                try
                {
                    result.load(stream);
                }
                finally
                {
                    stream.close();
                }
            }
        }
        catch (FileNotFoundException ex)
        {
            // It's okay to ignore this silently: the caller will just look
            // elsewhere.
        }
        catch (java.io.IOException ioe)
        {
            // This is a more severe problem: write to the terminal, because
            // we have no logging yet.
            println("could not read config file: " + name);
            ioe.printStackTrace();
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
    {
        final DateFormat dateFormatter = new SimpleDateFormat("yyyyMdHm");

        String logFileName =
            getAttribute("jacorb.logfile", "");

        int maxLogSize =
            getAttributeAsInteger( "jacorb.logfile.maxLogSize", 0 );

        if ( !logFileName.equals(""))
        {
            if (orb == null) // this is the case for the singleton ORB
            {
                final String singletonLogFile = getAttribute("jacorb.logfile.singleton", "");
                if (singletonLogFile.equals(""))
                {
                    // setting to "" effectively disables logging for the singleton orb.
                    logFileName = "";
                }
                else
                {
                    // If it ends with implname File can't handle it so do it manually.
                    if (logFileName.endsWith ("$implname"))
                    {
                        logFileName = logFileName.substring
                            (0, logFileName.indexOf("$implname") - 1);
                        logFileName += File.separatorChar + singletonLogFile + dateFormatter.format(new Date()) + ".log";
                    }
                    else
                    {
                        final File file = new File(logFileName);
                        final String parent = file.getParent();

                        if (parent != null)
                        {
                            logFileName += singletonLogFile + dateFormatter.format(new Date()) + ".log";
                        }
                        else
                        {
                            logFileName = singletonLogFile + dateFormatter.format(new Date()) + ".log";
                        }
                    }
                }
            }
            else if (logFileName.endsWith("$implname"))
            {
                // Convert $implname postfix to implementation name
                logFileName = logFileName.substring (0, logFileName.length () - 9);

                final String serverId = new String(orb.getServerId());
                String implName = getAttribute("jacorb.implname", serverId);
                logFileName += implName + ".log";
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
            printErr("Configuration Error, could not create logger!");
        }
        if (!logFileName.equals(""))
        {
            try
            {
                loggerFactory.setDefaultLogFile(logFileName, maxLogSize);
                logger = loggerFactory.getNamedLogger("jacorb",logFileName, maxLogSize);
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

    public String getLoggerName(Class clz)
    {
        final String clazzName = clz.getName();
        final String packageName = clazzName.substring(0, clazzName.lastIndexOf('.'));

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
            {
                result.add(tok.nextToken().trim());
            }
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
        String className = getAttribute(key, null);

        if(  className != null && className.length() > 0 )
        {
            return newInstance(key, className);
        }

        return null;
    }


    private Object newInstance(String key, String className) throws ConfigurationException
    {
        try
        {
            Class clazz = ObjectUtil.classForName(className);

            final Object instance = clazz.newInstance();

            if (instance instanceof Configurable)
            {
                ((Configurable)instance).configure(this);
            }
            return instance;
        }
        catch( Exception e )
        {
            throw new ConfigurationException( "Unable to build class from key >" +
                                              key +"<: " + e );
        }
    }

    public Object getAttributeAsObject(String key, String defaultValue) throws ConfigurationException
    {
        Object result = getAttributeAsObject(key);

        if (result == null)
        {
            return newInstance("default", defaultValue);
        }

        return result;
    }

    public boolean getAttributeAsBoolean(String key)
        throws ConfigurationException
    {
        String value = getAttribute(key);

        if (value != null && value.length() > 0)
        {
            value = value.trim().toLowerCase();
            return ON.equals(value) || TRUE.equals(value);
        }

        return false;
    }

    public boolean getAttributeAsBoolean(String key, boolean defaultValue)
    {
        String value = getAttribute(key, EMPTY_STR);

        if (value.length() > 0)
        {
            value = value.trim().toLowerCase();
            return ON.equals(value) || TRUE.equals(value);
        }

        return defaultValue;
    }


    public List getAttributeNamesWithPrefix(String prefix)
    {
        final List attributesWithPrefix = new ArrayList();

        final String[] allAttributes = getAttributeNames();

        for (int x = 0; x < allAttributes.length; ++x)
        {
            if (allAttributes[x].startsWith(prefix))
            {
                attributesWithPrefix.add(allAttributes[x]);
            }
        }

        return Collections.unmodifiableList(attributesWithPrefix);
    }
}
