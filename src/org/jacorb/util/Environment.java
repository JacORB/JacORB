package org.jacorb.util;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2003  Gerald Brose.
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

import org.apache.avalon.framework.logger.*;

import java.text.SimpleDateFormat;
import java.util.*;
import java.io.*;
import org.omg.CORBA.BAD_QOS;


/**
 * JacORB configuration options are accessed through this class.<BR>
 * <p>
 * On initialization, this class loads ORB configuration properties
 * from different sources. The default configuration files are called
 * ".jacorb_properties" or "jacorb.properties". Properties areloaded
 * in the following order, with properties loaded later
 * overriding earlier settings:<break>
 * <ol>
 * <li>default file in JRE/lib
 * <li>default file in user.home
 * <li>default file on classpath
 * <li>additional custom properties files (custom.props)
 * <li>command line properties
 * </ol>
 * <break>
 * ORB.init() parameters are set using the addProperties() method and
 * override any settings from configuration files, so hard-coded
 * properties will always we honored.
 *
 * @author Gerald Brose <mailto:gerald.brose@acm.org>
 * @version $Id$
 */

/* !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
 * Applets are using a special init procedure (readFromURL) and do not
 * have access to all system properties. (semu)
 */

public class Environment
{
    /** the configuration properties */
    private static Properties   configurationProperties;

    /** default file names for ORB configurain files */
    private static final String propertiesFile1       = ".jacorb_properties";
    private static final String propertiesFile2       = "jacorb.properties";

    private static final String jacorbPrefix          = "jacorb.";
    private static final String poaPrefix             = jacorbPrefix + "poa.";

    private static Class identityHashMapClass = null;

    /* standard JacORB properties with default values follow */

    private static int                  _client_pending_reply_timeout = 0;
    private static int                  _retries = 10;
    private static long                 _retry_interval = 700;
    private static int                  maxManagedBufSize = 18;

    /**
     * <code>compactTypecodes</code> denotes whether to compact typecodes.
     * Levels are:
     * 0: No compaction (off) [Default].
     * 1: Compact all but member_names.
     * 2: Compact all
     */
    private static int                  compactTypecodes = 0;

    private static boolean              _locate_on_bind = false;
    private static boolean              _use_imr = false;
    private static boolean              _use_imr_endpoint = true;
    private static boolean              _cache_references = false;
    private static String               logFileName = null;
    private static long                  _max_log_size = 0;
    private static boolean              append = false;
    private static long                  _current_log_size = 0;
    /*
     * <code>_useIndirection</code> is whether to turn off indirection
     * for repeated typecodes to fix interop issue with broken ORBs
     */
    private static boolean              _useIndirection = false;


    /* threading properties */
    private static boolean              _monitoring_on = false;
    private static int                  _thread_pool_max = 20;
    private static int                  _thread_pool_min = 10;
    private static int                  _queue_max = 100;
    private static int                  _queue_min = 10;
    private static boolean              _queue_wait = false;

    /* IIOP proxy/appligator */
    private static boolean              _use_appligator_for_applets = true;
    private static boolean              _use_appligator_for_applications = false;

    public static java.net.URL          URL=null;

    private static byte[]               _impl_name = null;
    private static byte[]               _server_id = null;

    /* properties with a given prefix */
    private static Map untrimmedPrefixProps = new HashMap();
    private static Map trimmedPrefixProps = new HashMap();

    private static boolean _strict_check_on_tc_creation = false;

    /** remarshal on COMM_FAILURE or retrow */
    private static boolean _retry_on_failure = false;


    /**
     * static initializer, calling init() explicitly...
     */
    static
    {
        init();
    }

    /**
     * Starting point for initialization of the ORB's
     * environment. Locates configuration files and reads properties
     * from these, then initializes logging.  (This needs to be public
     * because we also call it from the regression suite.)
     */

    public static void init()
    {
        try
        {
            /* read configuration properties */
            configurationProperties = new Properties();

            String home = System.getProperty("user.home");
            String sep = System.getProperty("file.separator");
            String lib = System.getProperty("java.home");

            // look for config files in java.home/lib first
            try
            {
                loadProperties( lib + sep + "lib" + sep + propertiesFile1 );
            }
            catch (IOException e)
            { }

            try
            {
                loadProperties( lib + sep + "lib" + sep + propertiesFile2 );
            }
            catch (IOException e)
            { }

            // look in user's  home directory next
            try
            {
                loadProperties( home + sep + propertiesFile1 );
            }
            catch ( IOException e )
            { }

            try
            {
                loadProperties( home + sep + propertiesFile2 );
            }
            catch ( IOException e )
            { }

            /* load config files from the default ClassLoader's classpath
               next, if any (supported by Per Bockman mailto:pebo@enea.se ) */

            try
            {
                java.net.URL url = null;

                // try first file name
                url = ClassLoader.getSystemResource( propertiesFile1 );

                if (url == null)
                {
                    //first is not found, so try second
                    url = ClassLoader.getSystemResource( propertiesFile2 );
                }

                if (url != null)
                {
                    configurationProperties.load( url.openStream() );
                    if (true)
                        System.out.println("[configuration loaded from classpath resource " +
                                           url + "]");
                }
            }
            catch (java.io.IOException ioe)
            {
                // ignore
            }

            // load additional properties from custom properties files
            String customPropertyFileNames = System.getProperty("custom.props");

            if( customPropertyFileNames != null )
            {
                try
                {
                    StringTokenizer strtok =
                        new StringTokenizer(customPropertyFileNames, ",");

                    while (strtok.hasMoreTokens())
                    {
                        String fileName = strtok.nextToken();
                        loadProperties(fileName);
                    }
                }
                catch ( IOException e )
                {
                    //ignore
                }
            }

            // load system properties (including command line properties)
            configurationProperties.putAll( System.getProperties() );

            //read prop values to set fields of this class
            readValues();

            // NB: additional properties passed as arguments to
            // ORB.init() are later explicitly added


        }
        catch (SecurityException secex)
        {
            System.out.println("Could not read local jacorb properties.");
        }
    }

    /**
     * Loads properties from a file, overriding existing properties
     * in case of conflict.
     * @param fileName the name of a properties file
     * @throws IOException
     */

    private static void loadProperties(String fileName)
        throws java.io.IOException
    {
        BufferedInputStream bin =
            new BufferedInputStream(new FileInputStream(fileName));
        configurationProperties.load(bin);
        bin.close();
        if (true)
            System.out.println("[configuration loaded from " + fileName + "]");
    }


    /**
     * Adds more properties, overriding any existing settings. (Called
     * from ORB.init()).
     * @param otherProperties a Properties object with properties to be added
     */

    public static void addProperties(java.util.Properties otherProperties)
    {
        if (configurationProperties == null)
            configurationProperties = new java.util.Properties();

        if (otherProperties != null)
        {
            configurationProperties.putAll( otherProperties );
            readValues();
        }
    }

    private static void readValues()
    {
        _retries = getIntPropertyWithDefault("jacorb.retries", 5);
        _retry_interval = getIntPropertyWithDefault("jacorb.retry_interval", 500);
        _client_pending_reply_timeout = getIntPropertyWithDefault("jacorb.connection.client.pending_reply_timeout", 0);
        maxManagedBufSize = getIntPropertyWithDefault("jacorb.maxManagedBufSize", 18);
        compactTypecodes = getIntPropertyWithDefault("jacorb.compactTypecodes", 0);
        _locate_on_bind = isPropertyOn("jacorb.locate_on_bind");
        _cache_references = isPropertyOn("jacorb.reference_caching");
        _monitoring_on = isPropertyOn("jacorb.poa.monitoring");
        _use_imr = isPropertyOn("jacorb.use_imr");
        _use_imr_endpoint = isPropertyOn("jacorb.use_imr", "on");
        _thread_pool_max = getIntPropertyWithDefault("jacorb.poa.thread_pool_max", 20);
        _thread_pool_min = getIntPropertyWithDefault("jacorb.poa.thread_pool_min", 5);
        _queue_max = getIntPropertyWithDefault("jacorb.poa.queue_max", 100);
        _queue_min = getIntPropertyWithDefault("jacorb.poa.queue_min", 10);
        _queue_wait = isPropertyOn("jacorb.poa.queue_wait");
        _use_appligator_for_applets = isPropertyOn("jacorb.use_appligator_for_applets");
        _use_appligator_for_applications = isPropertyOn("jacorb.use_appligator_for_applications");
        _impl_name = getProperty("jacorb.implname", "").getBytes();
        _strict_check_on_tc_creation = isPropertyOn("jacorb.interop.strict_check_on_tc_creation", "on");
        _retry_on_failure = isPropertyOn("jacorb.connection.client.retry_on_failure");
        _useIndirection = ! isPropertyOn("jacorb.interop.indirection_encoding_disable");
    }

    // value getters
    public static final  boolean getStrictCheckOnTypecodeCreation()
    {
        return _strict_check_on_tc_creation;
    }

    public static final  boolean isMonitoringOn()
    {
        return _monitoring_on;
    }

    /**
     * @return the root logger object for JacORB
     */

    public static final Logger getLogger()
    {
        return logger;
    }


    /**
     * @return the max size of the log file in kilo bytes. A size of 0
     * means no limit, any other size requires log file rotation.
     */

    public static final long maxLogSize()
    {
        return _max_log_size;
    }

    public static final  long currentLogSize () { return _current_log_size; }

    public static final int clientPendingReplyTimeout()
    {
        return _client_pending_reply_timeout;
    }

    public static final int noOfRetries() { return _retries;   }
    public static final boolean locateOnBind() { return _locate_on_bind; }
    public static final boolean cacheReferences() { return _cache_references; }

    public static final boolean queueWait() { return _queue_wait;  }
    public static final int queueMin() { return _queue_min;  }
    public static final int queueMax() { return _queue_max;  }
    public static final long retryInterval() { return _retry_interval; }

    public static final boolean useImR()    { return _use_imr;    }
    public static final boolean useImREndpoint()    { return _use_imr_endpoint;}

    public static final  int threadPoolMax() { return _thread_pool_max; }
    public static final  int threadPoolMin() { return _thread_pool_min; }

    public static final byte[] implName()
    {
        return _impl_name;
    }

    public static final boolean useAppligator(boolean amIanApplet)
    {
        if (amIanApplet)
        {
            return _use_appligator_for_applets;
        }
        else
        {
            return _use_appligator_for_applications;
        }
    }

    public static int getMaxManagedBufSize()
    {
        return maxManagedBufSize;
    }

    public static int getCompactTypecodes ()
    {
        return compactTypecodes;
    }

    /*
     * <code>getUseIndirection</code> returns value of _useIndirection
     * @return a boolean value
     */
    public static boolean getUseIndirection ()
    {
        return _useIndirection;
    }

    public static String imrProxyHost()
    {
        return configurationProperties.getProperty (jacorbPrefix + "imr.ior_proxy_host");
    }

    public static int imrProxyPort()
    {
        return getIntPropertyWithDefault (jacorbPrefix + "imr.ior_proxy_port",
                                          -1);
    }

    public static String iorProxyHost()
    {
        return configurationProperties.getProperty (jacorbPrefix + "ior_proxy_host");
    }

    public static int iorProxyPort()
    {
        return getIntPropertyWithDefault (jacorbPrefix + "ior_proxy_port",
                                          -1);
    }

    public static int giopMinorVersion()
    {
        return getIntPropertyWithDefault (jacorbPrefix + "giop_minor_version",
                                          2);
    }

    public static boolean giopAdd_1_0_Profiles()
    {
        return isPropertyOn(jacorbPrefix + "giop.add_1_0_profiles");
    }

    public static final boolean retryOnFailure()
    {
        return _retry_on_failure;
    }

    /**
     * generic
     */

    public static String getProperty( String key )
    {
        return configurationProperties.getProperty(key);
    }

    public static String getProperty( String key, String def )
    {
        return configurationProperties.getProperty( key, def );
    }

    /**
     * This will return true if the property's value is
     * "on". Otherwise (i.e. value "off", or property not set), false
     * is returned.
     */

    public static boolean isPropertyOn(String key)
    {
        return isPropertyOn(key, "off");
    }

    public static boolean isPropertyOn(String key, String def)
    {
        String s = configurationProperties.getProperty (key, def);
        return "on".equals (s);
    }

    public static long getLongProperty(String key, int base, long def)
    {
        String s = configurationProperties.getProperty (key);

        try
        {
            return Long.parseLong (s, base);
        }
        catch (NumberFormatException nfe)
        {
            return def;
        }
    }

    public static long getLongProperty( String key, int base )
    {
        String s = configurationProperties.getProperty( key );

        try
        {
            return Long.parseLong (s, base);
        }
        catch( NumberFormatException nfe )
        {
            throw new BAD_QOS( "Unable to create long from string >>" +
                               s + "<<. " +
                               "Please check property \"" + key + '\"');
        }
    }

    public static int getIntProperty( String key, int base )
    {
        String s = configurationProperties.getProperty( key );

        try
        {
            return Integer.parseInt( s, base );
        }
        catch( NumberFormatException nfe )
        {
            throw new BAD_QOS( "Unable to create int from string >>" +
                               s + "<<. " +
                               "Please check property \"" + key + '\"');
        }
    }

    public static int getIntPropertyWithDefault( String key, int def )
    {
        String s = configurationProperties.getProperty( key );

        if( s != null && s.length() > 0 )
        {
            try
            {
                return Integer.parseInt( s );
            }
            catch( NumberFormatException nfe )
            {
                throw new BAD_QOS( "Unable to create int from string >>" +
                                   s + "<<. " +
                                   "Please check property \"" + key + '\"');
            }
        }
        else
        {
            return def;
        }
    }

    /**
     * Create an object from the given property. The class's default
     * constructor will be used.
     *
     * @return null or an object of the class of the keys value
     * @throws Error if reflection fails.
     */

    public static Object getObjectProperty( String key )
    {
        String s = configurationProperties.getProperty( key );

        if( s != null && s.length() > 0 )
        {
            try
            {
                Class c = classForName( s );
                return c.newInstance();
            }
            catch( Exception e )
            {
                throw new BAD_QOS( "Unable to build class from key >" +
                                   key +"<: " + e );
            }
        }
        else
        {
            return null;
        }
    }

    /**
     * For a property that has a list of comma-separated values,
     * this method returns these values as a list of Strings.
     * If the property is not set, an empty list is returned.
     */

    public static List getListProperty (String key)
    {
        List   result = new ArrayList();
        String value  = configurationProperties.getProperty (key);
        if (value != null)
        {
            StringTokenizer tok = new StringTokenizer (value, ",");
            while (tok.hasMoreTokens())
                result.add (tok.nextToken().trim());
        }
        return result;
    }

    public static boolean hasProperty( String key )
    {
        return configurationProperties.containsKey( key );
    }

    public static void setProperty( String key, String value )
    {
        configurationProperties.put( key, value );
    }

    public static String[] getPropertyValueList(String key)
    {
        String list =  configurationProperties.getProperty(key);

        if( list == null )
        {
            return new String[0];
        }

        StringTokenizer t = new StringTokenizer( list, "," );
        Vector v = new Vector();

        while( t.hasMoreTokens() )
        {
            v.addElement( t.nextToken());
        }

        String[] result = new String[v.size()];
        for( int i = 0; i < result.length; i++ )
        {
            result[i] = (String)v.elementAt(i);
        }

        return result;

    }

    public static final void readFromURL(java.net.URL _url)
    {
        URL=_url;
        System.out.println("Reading properties from url:"+URL.toString());
        try
        {
            configurationProperties=new Properties();
            configurationProperties.load(new java.io.BufferedInputStream(URL.openStream()));
        }
        catch(Exception e)
        {
            System.out.println("Could not read properties from URL, reason: "+
                               e.toString());
        }
        readValues();
        // configurationProperties.list( System.out );
    }

    public static final byte[] serverId()
    {
        if (_server_id == null)
            _server_id = String.valueOf((long)(Math.random()*9999999999L)).getBytes();
        return _server_id;
    }

    /**
     * Collects all properties with a given prefix
     *
     * @return a hash table with  key/value pairs where
     * key has the given prefix
     */

    public static Hashtable getProperties( String prefix )
    {
        return getProperties( prefix, false );
    }

    /**
     * Collects all properties with a given prefix. The prefix
     * will be removed from the hash key if trim is true
     *
     * @return a hash table with  key/value pairs
     */

    public static Hashtable getProperties( String prefix, boolean trim )
    {
        if( trim && trimmedPrefixProps.containsKey( prefix ))
            return (Hashtable)trimmedPrefixProps.get( prefix );
        else if( !trim && untrimmedPrefixProps.containsKey( prefix ))
            return (Hashtable)untrimmedPrefixProps.get( prefix );
        else
        {
            Enumeration prop_names = configurationProperties.propertyNames();
            Hashtable properties = new Hashtable();

            // Test EVERY property if prefix matches.
            while( prop_names.hasMoreElements() )
            {
                String name = (String) prop_names.nextElement();
                if ( name.startsWith( prefix ))
                {
                    if( trim )
                    {
                        properties.put( name.substring( prefix.length() + 1) ,
                                        configurationProperties.getProperty(name) );
                    }
                    else
                    {
                        properties.put( name ,
                                        configurationProperties.getProperty(name));
                    }
                }
            }

            /* record for later use */

            if (trim)
                trimmedPrefixProps.put( prefix, properties );
            else
                untrimmedPrefixProps.put( prefix, properties );

            return properties;
        }
    }

    /**
     * Creates an IdentityHashMap, using either the JDK 1.4 class or
     * JacORB's drop-in replacement class if the former is not available.
     *
     * @return a newly created IdentityHashMap instance
     */
    public static Map createIdentityHashMap()
    {
        if (identityHashMapClass == null)
        {
            try
            {
                identityHashMapClass =
                    classForName("java.util.IdentityHashMap");
            }
            catch (ClassNotFoundException ex)
            {
                try
                {
                    identityHashMapClass =
                        classForName("org.jacorb.util.IdentityHashMap");
                }
                catch (ClassNotFoundException e)
                {
                    throw new RuntimeException(e.toString());
                }
            }
        }
        try
        {
            return (Map)identityHashMapClass.newInstance();
        }
        catch (Exception exc)
        {
            throw new RuntimeException(exc.toString());
        }
    }
}
