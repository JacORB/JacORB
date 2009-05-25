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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

import org.jacorb.orb.ORB;
import org.jacorb.util.ObjectUtil;
import org.slf4j.Logger;

/**
 * @author Gerald Brose
 * @version $Id$
 */
public class JacORBConfiguration implements Configuration
{
    private static final String CONFIG_LOG_VERBOSITY = "jacorb.config.log.verbosity";
    private static final String fileSuffix = ".properties";
    private static final String COMMON_PROPS = "orb" + fileSuffix;

    private static final String TRUE = "true";
    private static final String ON = "on";
    private static final String EMPTY_STR = "";

    private static final String ATTR_LOG_VERBOSITY       = "jacorb.log.default.verbosity";
    private static final String ATTR_LOG_FILE            = "jacorb.logfile";
    private static final String ATTR_LOG_APPEND          = "jacorb.logfile.append";
    private static final String ATTR_LOGGING_INITIALIZER = "jacorb.log.initializer";
    private static final int    DEFAULT_LOG_LEVEL    = 0;

    /** contains the actual configuration data */
    private Properties attributes;
    
    private String name;
    private final ORB orb;
    
    private Logger logger;

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
        super();
        this.name = name;
        this.orb = orb;
        this.attributes = new Properties();
        this.logger = getLogger ("jacorb.config");

        if (isApplet)
        {
            initApplet(name, orbProperties);
        }
        else
        {
            init(name, orbProperties);
        }

        // don't call this for the singleton orb
        if (orb != null) initLogging();
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

       // 2) look for orb.properties
       // look for common properties files in java.home/lib first
       String propFile = lib + separator + "lib" + separator + COMMON_PROPS;
       Properties commonProps = loadPropertiesFromFile (propFile);
       if (commonProps!= null)
       {
            setAttributes(commonProps);
            loaded = true;
            logger.info ("base configuration loaded from file " + propFile);
       }

       // look for common properties files in user.home next
       propFile = home + separator + COMMON_PROPS;
       commonProps = loadPropertiesFromFile (propFile);
       if (commonProps!= null)
       {
           setAttributes(commonProps);
           loaded = true;
           logger.info ("base configuration loaded from file " + propFile);
       }

       // look for common properties files on the classpath next
       commonProps = loadPropertiesFromClassPath( COMMON_PROPS );
       if (commonProps!= null)
       {
           loaded = true;
           setAttributes(commonProps);
           logger.info ("base configuration loaded from classpath "
                        + COMMON_PROPS);
       }

       // 3) look for specific properties file
       String configDir = getAttribute("jacorb.config.dir", "");

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
           logger.warn ("jacorb.home unset! Will use '.'");
           configDir = ".";
       }

       propFile = configDir + separator + name + fileSuffix;

       // now load properties file from file system
       Properties orbConfig = loadPropertiesFromFile (propFile);

       if (orbConfig!= null)
       {
           setAttributes(orbConfig);
           loaded = true;
           logger.info ("configuration " + name + 
                        " loaded from file " + propFile);
       }
       else
       {
           logger.warn ("File " + propFile + " for configuration " 
                        + name + " not found");
       }

       // now load properties file from classpath
       orbConfig = loadPropertiesFromClassPath (name + fileSuffix);
       if (orbConfig!= null)
       {
           setAttributes(orbConfig);
           loaded = true;
           logger.info ("configuration " + name + " loaded from classpath");
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
                    logger.info ("custom properties loaded from file "
                                 + fileName);
                }
                else
                {
                    logger.warn ("custom properties not found in " + fileName);
                }
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
           logger.warn ("no properties found for configuration " + name);
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
       if ( orbProperties != null )
       {
           setAttributes(orbProperties);
       }

       // 2) look for orb.properties
       // look for common properties files on the classpath next
       Properties commonProps = loadPropertiesFromClassPath (COMMON_PROPS);

       if (commonProps!= null)
       {
           setAttributes(commonProps);
           loaded = true;
           logger.info ("base configuration loaded from classpath " +
                        COMMON_PROPS);
       }

       // 3) look for specific properties file
       String propFile = name + fileSuffix;
       Properties orbConfig = loadPropertiesFromClassPath (propFile);

       if (orbConfig!= null)
       {
           setAttributes(orbConfig);
           loaded = true;
           logger.info ("configuration " + name +
                        " loaded from classpath " + propFile);
       }
       else
       {
           logger.warn ("File " + propFile +
                        " for configuration " + name +
                        " not found in classpath");
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
                    logger.info ("custom properties loaded from classpath "
                                 + fileName);
                }
                else
                {
                    logger.warn ("custom properties " + fileName +
                                 " not found in classpath");
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
           logger.warn ("no properties found for configuration " + name); 
       }
    }


    /**
     * Sets the value of a single attribute.
     */
    public void setAttribute(String key, String value)
    {
        attributes.put(key, value);
    }


    /**
     * set attributes of this configuration using properties
     */

    void setAttributes(Properties properties)
    {
        Enumeration e = properties.propertyNames ();
        while (e.hasMoreElements ())
        {
            Object obj = e.nextElement ();
            // Some lunatics illegally put non String objects into System props
            // as keys / values - we check for both and ignore them.
            if (!(obj instanceof String))
            {
                continue;
            }

            String key = (String)obj;
            Object value = properties.getProperty(key);
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

    private Properties loadPropertiesFromClassPath(String name)
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
            logger.error ("could not read config file: " + name, ioe);
        }
        return result;
    }

    /**
     * Configures the external logging backend from within JacORB,
     * but only if the backend is JDK, and if one of the legacy logging
     * properties, jacorb.log.default.verbosity or jacorb.logfile, is set.
     * This is only meant to ease the transition as we move to SLF4J.
     * Normally, configuration of the logging backend is completely
     * external to JacORB and left to the user.
     */
    private void initLogging()
    {
        LoggingInitializer li = (LoggingInitializer)getAttributeAsObject
        (
            ATTR_LOGGING_INITIALIZER,
            "org.jacorb.config.JdkLoggingInitializer"
        );
        li.init (this);
    }
    
    /**
     * @return the ORB for which this configuration was created
     */

    public ORB getORB()
    {
        return orb;
    }


    /**
     * Returns a Logger that logs JacORB system messages.  This uses
     * the SLF4J logging facade.  The actual logging backend is chosen
     * at deployment time, by putting a corresponding SLF4J adapter
     * jar on the classpath.
     * 
     * The JacORB root logger is named "jacorb".  Sublogger names all
     * start with this prefix.  If the property jacorb.implname is set,
     * and the property jacorb.log.split_on_implname is true, then
     * all loggers for that particular ORB instance are rooted in
     * jacorb.<implname>.
     * 
     * Here's a guideline how to use logging levels in the code:
     * 
     * error Conditions that indicate a bug in JacORB or user code,
     *       or a wrong configuration.  This includes, but is not
     *       limited to, errors that will lead to termination of the
     *       program (fatal errors).
     * 
     * warn  Conditions that demand attention, but are handled properly
     *       according to the CORBA spec.  For example, abnormal termination
     *       of a connection, reaching of a resource limit (queue full).
     * 
     * info  Start/stop of subsystems, establishing and closing of connections,
     *       registering objects with a POA.
     * 
     * debug Information that might be needed for finding bugs in JacORB
     *       or user code.  Anything that relates to the normal processing
     *       of individual messages should come under this level.  For each
     *       CORBA message, there should at least one debug message when
     *       subsystem boundaries are crossed (e.g. GIOPConnection -> POA
     *       -> User Code).
     */
    public org.slf4j.Logger getLogger (String name)
    {
        String loggerName = name;
        if (getAttributeAsBoolean ("jacorb.log.split_on_implname", false))
        {
            String implName = getAttribute ("jacorb.implname", null);
            if (implName != null && implName.length() > 0)
            {
                if (name.equals ("jacorb"))
                    loggerName = "jacorb." + implName;
                else if (name.startsWith ("jacorb."))
                    loggerName = "jacorb." + implName + "." + name.substring (7);
            }
        }
        return org.slf4j.LoggerFactory.getLogger (loggerName);
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
     * @see org.jacorb.config.Configuration#getAttribute(java.lang.String)
     */
    public String getAttribute(String key)
    {
        String result = attributes.getProperty(key);
        if (result != null)
        {
            return result;
        }
        else
        {
            throw new ConfigurationException 
            (
                "attribute " + key + " is not defined"
            );
        }
    }

    /**
     * @see org.jacorb.config.Configuration#getAttribute(java.lang.String, java.lang.String)
     */
    public String getAttribute(String key, String defaultValue)
    {
        return attributes.getProperty(key, defaultValue);
    }

    
    /**
     * @see org.jacorb.config.Configuration#getAttributeAsInteger(java.lang.String, int)
     */
    public int getAttributeAsInteger(String key, int defaultValue)
    {
        Object value = attributes.getProperty (key, null);
        if (value == null)
        {
            return defaultValue;
        }
        else if (value instanceof String) 
        {
            if (((String)value).trim().length() < 1)
            {
                // empty string is treated as 'null' value
                return defaultValue;
            }
            
            try
            {
                int i = Integer.parseInt (((String)value).trim());
                return i;
            }
            catch (NumberFormatException ex)
            {
                // fall through
            }
        }
        throw new ConfigurationException 
        (
            "value for attribute " + key + " is not numeric: " + value
        );
    }


    /**
     * @see org.jacorb.config.Configuration#getAttributeAsInteger(java.lang.String)
     */
    public int getAttributeAsInteger(String key)
    {
        Object value = attributes.getProperty (key, null);
        if (value == null)
        {
            throw new ConfigurationException
            (
                "value for attribute " + key + " is not set"
            );
        }
        else
        {
            // we know now that the attribute does exist, so it's safe
            // to call the other function with an arbitrary default value
            return getAttributeAsInteger (key, 0);
        }
    }


    /**
     * @see org.jacorb.config.Configuration#getAttributeAsLong(java.lang.String, long)
     */
    public long getAttributeAsLong(String key, long defaultValue)
    {
        Object value = attributes.getProperty (key, null);
        if (value == null)
        {
            return defaultValue;
        }
        else if (value instanceof String) 
        {
            try
            {
                long i = Long.parseLong (((String)value).trim());
                return i;
            }
            catch (NumberFormatException ex)
            {
                // fall through
            }
        }
        throw new ConfigurationException 
        (
            "value for attribute " + key + " is not numeric: " + value
        );
    }


    /**
     * @see org.jacorb.config.Configuration#getAttributeList(java.lang.String)
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
     * @see org.jacorb.config.Configuration#getAttributeAsObject(java.lang.String)
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

    /**
     * @see org.jacorb.config.Configuration#getAttributeAsObject(java.lang.String, java.lang.String)
     */
    public Object getAttributeAsObject(String key, String defaultClass) throws ConfigurationException
    {
        Object result = getAttributeAsObject(key);
        if (result == null)
        {
            return newInstance("default", defaultClass);
        }

        return result;
    }

    /**
     * @see org.jacorb.config.Configuration#getAttributeAsBoolean(java.lang.String)
     */
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

    /**
     * @see org.jacorb.config.Configuration#getAttributeAsBoolean(java.lang.String, boolean)
     */
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


    public String[] getAttributeNames()
    {
        return (String[])(attributes.keySet().toArray (new String[]{}));
    }
    
    /**
     * @see org.jacorb.config.Configuration#getAttributeNamesWithPrefix(java.lang.String)
     */
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
