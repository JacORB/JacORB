package org.jacorb.config;

/*
 *        JacORB - a free Java ORB
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
 *   Software Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.logging.Level;
import org.jacorb.util.ObjectUtil;
import org.omg.CORBA.NO_IMPLEMENT;
import org.omg.CORBA.ORB;
import org.slf4j.Logger;

/**
 * The Class JacORBConfiguration.
 *
 * @author Gerald Brose
 */
public class JacORBConfiguration implements Configuration
{
    /**
     * The Constant fileSuffix.
     */
    private static final String fileSuffix = ".properties";

    /**
     * The Constant COMMON_PROPS.
     */
    private static final String COMMON_PROPS = "orb" + fileSuffix;

    /**
     * The Constant TRUE.
     */
    private static final String TRUE = "true";

    /**
     * The Constant ON.
     */
    private static final String ON = "on";

    /**
     * The Constant ATTR_LOGGING_INITIALIZER.
     */
    private static final String ATTR_LOGGING_INITIALIZER = "jacorb.log.initializer";


   /*
    * <code>useTCCL</code> controls which class loader policy JacORB should use throughout
    * the codebase. By default it will attempt to load using the Thread Context Class Loader.
    * To support integration in some deployment scenarios it is also possible to use Class.forName.
    * This may be set by setting jacorb.classloaderpolicy system property to either tccl or forname.
    *
    * Note that this is duplicated within org.omg.CORBA.ORBSingleton
    * (to avoid cross-dependencies)
    */
    public static final boolean useTCCL;

    static
    {
        String clpolicy = System.getProperty ("jacorb.classloaderpolicy", "tccl");
        if (clpolicy.equalsIgnoreCase ("forname"))
        {
            useTCCL = false;
        }
        else
        {
            useTCCL = true;
        }
    }


    /**
     * Contains the actual configuration data.
     *
     * To speed up the access of frequently requested configuration values a set
     * of dedicated hashmaps provide String, Boolean and Number storage.
     */
    private HashMap<String,String> stringAttributes = new HashMap<String,String>();

    /**
     * The boolean attributes.
     */
    private HashMap<String,Boolean> booleanAttributes = new HashMap<String,Boolean>();

    /**
     * The number attributes.
     */
    private HashMap<String, Number> numberAttributes = new HashMap<String, Number> ();

    /**
     * The orb.
     */
    private final ORB orb;

    /**
     * The logger.
     */
    private Logger logger;

    /**
     * The li.
     */
    private LoggingInitializer li;


    /**
     * Factory method.
     *
     * @param props the props
     * @param orb the orb
     * @param isApplet the is applet
     * @return the configuration
     * @throws ConfigurationException the configuration exception
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
        if ( !isApplet )
        {
            try
            {
                myOrbID = System.getProperty("ORBid");
            }
            catch ( SecurityException e )
            {
                isApplet = true;
                System.err.println ("Could not access system property 'ORBid' - will use default...");
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
     *
     * @param name the name
     * @param orbProperties the orb properties
     * @param orb the orb
     * @param isApplet the is applet
     * @throws ConfigurationException the configuration exception
     */

    private JacORBConfiguration(String name,
                                Properties orbProperties,
                                ORB orb,
                                boolean isApplet)
        throws ConfigurationException
    {
        super();
        this.orb = orb;
        this.logger = getLogger ("jacorb.config");

        LinkedHashMap<Level,String> delayedLogging = new LinkedHashMap<Level,String> ();
        if (isApplet)
        {
           initApplet(delayedLogging, name, orbProperties);
        }
        else
        {
           init(delayedLogging, name, orbProperties);
        }

        initLogging();

        // This delays logging out any information about the loading of the properties
        // or configuration until any logging subsystem has been setup.
        for (Entry<Level, String> e : delayedLogging.entrySet())
        {
           if (e.getKey () == Level.INFO)
           {
              logger.info (e.getValue ());
           }
           else if (e.getKey () == Level.WARNING)
           {
              logger.warn (e.getValue ());
           }
           else if (e.getKey () == Level.FINE)
           {
              logger.debug (e.getValue ());
           }
           else
           {
              throw new NO_IMPLEMENT("Only info/warn delayed logging implemented.");
           }
        }
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
     * @param delayedLogging the delayed logging
     * @param name the name for the ORB instance, may not be null.
     * @param orbProperties the orb properties
     * @throws ConfigurationException the configuration exception
     */

    private void init(LinkedHashMap<Level, String> delayedLogging, String name, Properties orbProperties)
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

       // 1.5) load passed-in properties that might influence further
       // property loading.  This will allow me to pass in jacorb.home,
       // jacorb.config.dir, and jacorb.config.log.verbosity.
       if (orbProperties != null)
       {
           loaded = true;
           setAttributes(orbProperties);
       }
       // 2) look for orb.properties
       // look for common properties files in java.home/lib first
       String propFile = lib + separator + "lib" + separator + COMMON_PROPS;
       Properties commonProps = loadPropertiesFromFile (propFile);
       if (commonProps!= null)
       {
            setAttributes(commonProps);
            loaded = true;
            delayedLogging.put (Level.FINE, "base configuration loaded from file " + propFile);
       }

       // look for common properties files in user.home next
       propFile = home + separator + COMMON_PROPS;
       commonProps = loadPropertiesFromFile (propFile);
       if (commonProps!= null)
       {
           setAttributes(commonProps);
           loaded = true;
           delayedLogging.put (Level.FINE, "base configuration loaded from file " + propFile);
       }

       // look for common properties files on the classpath next
       commonProps = loadPropertiesFromClassPath( COMMON_PROPS );
       if (commonProps!= null)
       {
           loaded = true;
           setAttributes(commonProps);
           delayedLogging.put (Level.FINE, "base configuration loaded from classpath " + COMMON_PROPS);
       }

       // 3) look for specific properties file
       String configDir = getAttribute("jacorb.config.dir", "");

       if (configDir.length() == 0)
       {
           configDir = getAttribute ("jacorb.home", "");

           if (configDir.length() != 0 )
           {
               configDir += separator + "etc";
           }
           else
           {
               delayedLogging.put (Level.FINE, "jacorb.home unset! Will use '.'");
               configDir = ".";
           }
       }

       propFile = configDir + separator + name + fileSuffix;

       // now load properties file from file system
       Properties orbConfig = loadPropertiesFromFile (propFile);
       if (orbConfig!= null)
       {
           setAttributes(orbConfig);
           loaded = true;

           delayedLogging.put (Level.FINE, "configuration " + name + " loaded from file " + propFile +
                               (orb == null ? " for ORBSingleton" : " for " + orb));
       }

       // now load properties file from classpath
       orbConfig = loadPropertiesFromClassPath (name + fileSuffix);
       if (orbConfig!= null)
       {
           setAttributes(orbConfig);
           loaded = true;
           delayedLogging.put (Level.FINE, "configuration " + name + " loaded from classpath" +
                               (orb == null ? " for ORBSingleton" : " for " + orb));
       }

       // 4) look for additional custom properties files
       List<String> customPropFileNames = getAttributeList("custom.props");

       if (!customPropFileNames.isEmpty())
       {
           for (String fileName : customPropFileNames)
           {
                Properties customProps = loadPropertiesFromFile(fileName);
                if (customProps!= null)
                {
                    setAttributes(customProps);
                    loaded = true;
                    delayedLogging.put (Level.FINE, "custom properties loaded from file " + fileName);
                }
                else
                {
                    delayedLogging.put (Level.WARNING, "custom properties not found in " + fileName);
                }
           }
       }

       // 5) load system properties again, so that
       //    command line args override properties from files
       setAttributes( System.getProperties() );

       // 6) load properties passed to ORB.init() again, these will override any
       // settings in config files or system properties!
       if (orbProperties != null)
       {
           loaded = true;
           setAttributes(orbProperties);

           // This is in case ORBClass/ORBSingleton are not in system properties
           // and have been specified via ORB.init props. If this is not done
           // and JacORB jars are not in bootclasspath/endorsed then it is possible
           // that a Sun Singleton is created by mistake in the config init.
           if (orbProperties.containsKey ("org.omg.CORBA.ORBClass") &&
               System.getProperty ("org.omg.CORBA.ORBClass") == null)
           {
               System.setProperty
               (
                   "org.omg.CORBA.ORBClass",
                   orbProperties.getProperty ("org.omg.CORBA.ORBClass")
               );
           }
           if (orbProperties.containsKey ("org.omg.CORBA.ORBSingletonClass") &&
               System.getProperty ("org.omg.CORBA.ORBSingletonClass") == null)
           {
               System.setProperty
               (
                   "org.omg.CORBA.ORBSingletonClass",
                   orbProperties.getProperty ("org.omg.CORBA.ORBSingletonClass")
               );
           }
       }

       if (!loaded)
       {
           delayedLogging.put (Level.WARNING, "no properties found for configuration " + name);
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
     * @param delayedLogging the delayed logging
     * @param name the name for the ORB instance, may not be null.
     * @param orbProperties the orb properties
     * @throws ConfigurationException the configuration exception
     */

    private void initApplet(LinkedHashMap<Level, String> delayedLogging, String name, Properties orbProperties)
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
           delayedLogging.put (Level.FINE, "base configuration loaded from classpath " + COMMON_PROPS);
       }

       // 3) look for specific properties file
       String propFile = name + fileSuffix;
       Properties orbConfig = loadPropertiesFromClassPath (propFile);

       if (orbConfig!= null)
       {
           setAttributes(orbConfig);
           loaded = true;
           delayedLogging.put (Level.FINE, "configuration " + name + " loaded from classpath " + propFile);
       }
       else
       {
           delayedLogging.put (Level.WARNING, "File " + propFile +
                   " for configuration " + name +
                   " not found in classpath");
       }

       // 4) look for additional custom properties files
       List<String> customPropFileNames = getAttributeList("custom.props");

       if (!customPropFileNames.isEmpty())
       {
           for (String fileName : customPropFileNames)
           {
                Properties customProps = loadPropertiesFromClassPath(fileName);
                if (customProps!= null)
                {
                    setAttributes(customProps);
                    loaded = true;
                    delayedLogging.put (Level.FINE, "custom properties loaded from classpath " + fileName);
                }
                else
                {
                    delayedLogging.put (Level.WARNING, "custom properties " + fileName + " not found in classpath");
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
           delayedLogging.put (Level.WARNING, "no properties found for configuration " + name);
       }
    }


    /**
     * Sets the value of a single attribute.
     *
     * @param key the key
     * @param value the value
     */
    public void setAttribute(String key, String value)
    {
        stringAttributes.put(key, value);
    }

    /* (non-Javadoc)
     * @see org.jacorb.config.Configuration#setAttribute(java.lang.String, int)
     */
    public void setAttribute(String key, int value)
    {
        numberAttributes.put(key, value);
    }


    /**
     * set attributes of this configuration using properties.
     *
     * @param properties the new attributes
     */

    public void setAttributes(Properties properties)
    {
        Enumeration<?> e = properties.propertyNames ();
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

            if (!(value instanceof String))
            {
                continue;
            }
            setAttribute(key, (String)value);
        }
    }


    /**
     * Loads properties from a file.
     *
     * @param fileName the name of a properties file
     * @return a properties object or null, if fileName not found
     */

    private Properties loadPropertiesFromFile(String fileName)
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
            System.err.println("could not read config file: " + fileName);
            e.printStackTrace();
            return null;
        }
    }


    /**
     * load properties file from classpath.
     *
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
            // This is a more severe problem: write to the terminal, because
            // we have no logging yet.
            System.err.println("could not read config file: " + name);
            ioe.printStackTrace();
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
     *
     * @throws ConfigurationException the configuration exception
     */
    private void initLogging() throws ConfigurationException
    {
        li = (LoggingInitializer)getAttributeAsObject
        (
            ATTR_LOGGING_INITIALIZER,
            "org.jacorb.config.JdkLoggingInitializer"
        );
        li.init (this);
    }


    /**
     * Calls shutdown on the logging sub-system. This may be a no-op
     * depending upon the logging backend.
     */
    public void shutdownLogging ()
    {
        li.shutdownLogging ();
    }


    /**
     * Gets the oRB.
     *
     * @return the ORB for which this configuration was created
     */

    public org.jacorb.orb.ORB getORB()
    {
        return (org.jacorb.orb.ORB)orb;
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
     * or a wrong configuration.  This includes, but is not
     * limited to, errors that will lead to termination of the
     * program (fatal errors).
     *
     * warn  Conditions that demand attention, but are handled properly
     * according to the CORBA spec.  For example, abnormal termination
     * of a connection, reaching of a resource limit (queue full).
     *
     * info  Start/stop of subsystems, establishing and closing of connections,
     * registering objects with a POA.
     *
     * debug Information that might be needed for finding bugs in JacORB
     * or user code.  Anything that relates to the normal processing
     * of individual messages should come under this level.  For each
     * CORBA message, there should at least one debug message when
     * subsystem boundaries are crossed (e.g. GIOPConnection -> POA
     * -> User Code).
     *
     * @param name the name
     * @return the logger
     * @throws ConfigurationException the configuration exception
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
              {
                 loggerName = "jacorb." + implName;
              }
              else if (name.startsWith ("jacorb."))
              {
                 loggerName = "jacorb." + implName + "." + name.substring (7);
              }
           }
        }
        return org.slf4j.LoggerFactory.getLogger (loggerName);
    }



    /* (non-Javadoc)
     * @see org.jacorb.config.Configuration#getLoggerName(java.lang.Class)
     */
    public String getLoggerName(Class<?> clz)
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
     * Gets the attribute
     *
     * @param key the key
     * @return the attribute
     * @throws ConfigurationException the configuration exception
     * @see org.jacorb.config.Configuration#getAttribute(java.lang.String)
     */
    public String getAttribute(String key) throws ConfigurationException
    {
       String result = getAttribute (key, null);

       if (result == null)
       {
           throw new ConfigurationException
           (
               "attribute " + key + " is not defined"
           );
       }

       return result;
    }

    /**
     * getAttribute
     * @see org.jacorb.config.Configuration#getAttribute(java.lang.String, java.lang.String)
     */
    public String getAttribute(String key, String defaultValue)
    {
       String result = stringAttributes.get (key);

       if (result == null && defaultValue != null)
       {
          stringAttributes.put (key, defaultValue);

          result = defaultValue;
       }
       return result;
   }

    /**
     * Gets the attribute as integer.
     *
     * @param key the key
     * @param defaultValue the default value
     * @return the attribute as integer
     * @throws ConfigurationException the configuration exception
     * @see org.jacorb.config.Configuration#getAttributeAsInteger(java.lang.String, int, int)
     */
    public int getAttributeAsInteger(String key, int defaultValue) throws ConfigurationException
    {
        return getAttributeAsInteger (key, defaultValue, 10);
    }

    /**
     * Returns the integer value of the specified key.
     *
     * @param key the key
     * @param defaultValue the default value
     * @param radix the radix
     * @return the attribute as integer
     * @throws ConfigurationException the configuration exception
     */
    public int getAttributeAsInteger(String key, int defaultValue, int radix) throws ConfigurationException
    {
       Number result = numberAttributes.get (key);

       if (result == null)
       {
           // This used to be .remove but removing it from the string
           // cache can cause inconsistency if another caller uses
           // getAttribute.
          String value = stringAttributes.get (key);

          if (value == null)
          {
             result = Integer.valueOf (defaultValue);
          }
          else if (value.trim().length() < 1)
          {
              // treat empty values as non-defined (null)
              result = Integer.valueOf (defaultValue);
          }
          else
          {
             try
             {
                result = Integer.parseInt (value.trim(), radix);
             }
             catch (NumberFormatException ex)
             {
                throw new ConfigurationException
                (
                 "value for attribute " + key + " is not numeric: " + value
                );
             }
          }
          numberAttributes.put (key, result);

       }

       return result.intValue ();
    }


    /**
     * Validates the key exists and then returns the value.
     *
     * @param key the key
     * @return the attribute as integer
     * @throws ConfigurationException the configuration exception
     */
    public int getAttributeAsInteger(String key) throws ConfigurationException
    {
        if ( ! stringAttributes.containsKey (key) && ! numberAttributes.containsKey (key))
        {
            throw new ConfigurationException
            (
                "Value for attribute " + key + " is not set"
            );
        }
        return getAttributeAsInteger (key, -1, 10);
    }


    /**
     * Gets the attribute as long.
     *
     * @param key the key
     * @param defaultValue the default value
     * @return the attribute as long
     * @throws ConfigurationException the configuration exception
     * @see org.jacorb.config.Configuration#getAttributeAsLong(java.lang.String, long)
     */
    public long getAttributeAsLong(String key, long defaultValue) throws ConfigurationException
    {
       Number result = numberAttributes.get (key);

       if (result == null)
       {
           // This used to be .remove but removing it from the string
           // cache can cause inconsistency if another caller uses
           // getAttribute.
          String value = stringAttributes.get (key);

          if (value == null)
          {
             result = Long.valueOf (defaultValue);
          }
          else if (value.trim().length() < 1)
          {
              // treat empty values as non-defined (null)
              result = Long.valueOf (defaultValue);
          }
          else
          {
             try
             {
                result = Long.parseLong (value.trim());
             }
             catch (NumberFormatException ex)
             {
                throw new ConfigurationException
                (
                 "value for attribute " + key + " is not numeric: " + value
                );
             }
          }
          numberAttributes.put (key, result);
       }

       return result.longValue ();
    }


    /**
     * Gets the attribute list.
     *
     * @param key the key
     * @return the attribute list
     * @see org.jacorb.config.Configuration#getAttributeList(java.lang.String)
     */
    public List<String> getAttributeList(String key)
    {
        List<String> result = new ArrayList<String>();
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

    public String[] getAttributeAsStringsArray(String key)
    {
        String value = null;

        try
        {
            value = getAttribute(key);
        }
        catch( ConfigurationException ce)
        {
            // ignore
        }

        if (value == null)
        {
            return null;
        }

        List<String> values = getAttributeList (key);

        // Return null if key is defined but has empty value
        if (values.size () < 1)
        {
            return null;
        }

        return (String[]) values.toArray (new String[values.size ()]);
    }


    /**
     * New instance.
     *
     * @param key the key
     * @param className the class name
     * @return the object
     * @throws ConfigurationException the configuration exception
     */
    private Object newInstance(String key, String className) throws ConfigurationException
    {
        try
        {
            Class<?> clazz = ObjectUtil.classForName(className);
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
     * Gets the attribute as object.
     *
     * @param key the key
     * @return the attribute as object
     * @throws ConfigurationException the configuration exception
     * @see org.jacorb.config.Configuration#getAttributeAsObject(java.lang.String)
     */
    public Object getAttributeAsObject( String key )
        throws ConfigurationException
    {
        return getAttributeAsObject(key, "");
    }

    /**
     * Gets the attribute as object.
     *
     * @param key the key
     * @param defaultClass the default class
     * @return the attribute as object
     * @throws ConfigurationException the configuration exception
     * @see org.jacorb.config.Configuration#getAttributeAsObject(java.lang.String, java.lang.String)
     */
    public Object getAttributeAsObject(String key, String defaultClass) throws ConfigurationException
    {
       Object result = null;
       String classname = getAttribute (key, "");

       if (classname.length() > 0 )
       {
          result = newInstance(key, classname);
       }
       else if (defaultClass != null && defaultClass.length () > 0)
       {
          result = newInstance("default", defaultClass);
       }

       return result;
    }

    /**
     * Return the attribute as a boolean. A default value must be supplied and the
     * key/value is cached.
     *
     * @param key the key
     * @param defaultValue the default value
     * @return the attribute as boolean
     */
    public boolean getAttributeAsBoolean(String key, boolean defaultValue)
    {
       Boolean result = booleanAttributes.get (key);

       if (result == null)
       {
           // This used to be .remove but removing it from the string
           // cache can cause inconsistency if another caller uses
           // getAttribute.
          String value = stringAttributes.get (key);

          if (value == null)
          {
             result = Boolean.valueOf (defaultValue);
          }
          else if (value.trim().length() < 1)
          {
              // treat empty values as non-defined (null)
              result = Boolean.valueOf (defaultValue);
          }
          else
          {
             value = value.trim().toLowerCase();
             result = Boolean.valueOf ((ON.equals(value) || TRUE.equals(value)));
          }
          booleanAttributes.put (key, result);
       }

       return result;
    }


    /**
     * Gets the attribute names.
     *
     * @return the attribute names
     */
    private String[] getAttributeNames()
    {
        return (String[])(stringAttributes.keySet().toArray (new String[]{}));
    }

    /**
     * Gets the attribute names with prefix.
     *
     * @param prefix the prefix
     * @return the attribute names with prefix
     * @see org.jacorb.config.Configuration#getAttributeNamesWithPrefix(java.lang.String)
     */
    public List<String> getAttributeNamesWithPrefix(String prefix)
    {
        final ArrayList<String> attributesWithPrefix = new ArrayList<String>();

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


    /* (non-Javadoc)
     * @see org.jacorb.config.Configuration#getAttributeAsFloat(java.lang.String, double)
     */
    public double getAttributeAsFloat (String key, double defaultValue) throws ConfigurationException
    {
       Number result = numberAttributes.get (key);

       if (result == null)
       {
           // This used to be .remove but removing it from the string
           // cache can cause inconsistency if another caller uses
           // getAttribute.
          String value = stringAttributes.get (key);

          if (value == null)
          {
             result = Double.valueOf (defaultValue);
          }
          else if (value.trim().length() < 1)
          {
              // treat empty values as non-defined (null)
              result = Double.valueOf (defaultValue);
          }
          else
          {
             try
             {
                result = Double.parseDouble (value.trim());
             }
             catch (NumberFormatException ex)
             {
                throw new ConfigurationException
                (
                 "value for attribute " + key + " is not numeric: " + value
                );
             }
          }
          numberAttributes.put (key, result);
       }

       return result.doubleValue ();
    }
}
