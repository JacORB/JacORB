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

/**
 * JacORB configuration options are accessed through this class.
 *
 * Properties originate from three sources, listed in order of precedence:
 *
 * 1) command line arguments (-Dx=y)
 * 2) properties passed to ORB.init() by an application
 * 3) properties read from configuration files
 *
 * This means that in case of conflicts,  command line args
 * override application properties which in turn may override
 * options from configuration files.
 *
 * Configuration files may be called ".jacorb_properties" or "jacorb.properties"
 * and are searched in the following order: if a configuration file is found
 * in "user.home", it is loaded first. If another file is found in the
 * current directory, it is also loaded. If properties of the same name
 * are found in both files, those loaded later override earlier ones,
 * so properties from a file found in "." take precedence.
 *
 * @author Gerald Brose
 * @version $Id$
 */

/* !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
 * Applets are using a special init procedure (readFromURL) and do not
 * have access to all system properties. Due to this special init
 * DO NOT USE Debug.Output here in Environment (at leat not until you are
 * very sure) semu
 */

public class Environment
{
    private static String propertiesFile1       = ".jacorb_properties";
    private static String propertiesFile2       = "jacorb.properties";
    private static java.util.Vector propertiesFiles = new java.util.Vector();
    private static String jacorbPrefix          = "jacorb.";
    private static String poaPrefix             = jacorbPrefix + "poa.";

    private static Properties   _props;

    private static int                  _retries = 10;
    private static long                 _retry_interval = 700;
    private static int                  _outbuf_size = 4096;
    private static int                  maxManagedBufSize = 18;
    /**
     * <code>compactTypecodes</code> denotes whether to compact typecodes.
     * Levels are:
     * 0: No compaction (off) [Default].
     * 1: Compact all but member_names.
     * 2: Compact all
     */
    private static int                  compactTypecodes = 0;

    private static String               _default_context = "<undefined>";

    /** domain-specific */

    private static int                  _verbosity = 2;

    private static boolean              _locate_on_bind = false;
    private static boolean              _use_imr = false;
    private static boolean              _use_imr_endpoint = true;
    private static boolean              _cache_references = false;
    private static String               logFileName = null;

    private static long                  _max_log_size = 0;
    private static boolean              append = false;
    private static long                  _current_log_size = 0;

    // domain service configuration
    /** indicates whether the domain service is used or not */
    private static boolean          _use_domain= false;

    /** if set to true (default), every orb domain gets mounted as child domain to
     *  the domain server on creation */
    private static boolean          _mount_orb_domain= true;

    /** the filename to which the IOR of the orb domain (local domain service) is written
     *  if set to null or the empty string (""), no IOR is written */
    private static String           _orb_domain_filename= null;

    /** the time how long an entry in the policy cache is valid, value in ms */
    private static long _cache_entry_lifetime  = 1000 * 60 * 5;      // 5 minutes

    /** the pathname of the domains the poa maps newly created object references by default */
    private static String           _default_domains= null;

    /** threading properties */
    private static boolean              _monitoring_on = false;
    private static int                  _thread_pool_max = 20;
    private static int                  _thread_pool_min = 10;
    private static int                  _queue_max = 100;
    private static int                  _queue_min = 10;
    private static boolean              _queue_wait = false;

    /** IIOP proxy/appligator */
    private static boolean              _use_appligator_for_applets = true;
    private static boolean              _use_appligator_for_applications = false;
    private static Hashtable            _use_httptunneling_for = new Hashtable();

    public static java.net.URL          URL=null;

    private static byte[]               _impl_name = null;
    private static byte[]               _server_id = null;

    /* properties with a given prefix */
    private static Hashtable untrimmedPrefixProps = new Hashtable();
    private static Hashtable trimmedPrefixProps = new Hashtable();

    private static SimpleDateFormat dateFormatter;
    private static SimpleDateFormat timeFormatter;

    private static boolean strict_check_on_tc_creation;

    /** root logger instance for JacORB */
    private static LoggerFactory loggerFactory = null;

    /**  logger factory used to create loggers */
    private static org.apache.avalon.framework.logger.Logger logger = null;

    /**  default class name for logger factory */
    private static String loggerFactoryClzName = "org.jacorb.util.LogKitLoggerFactory";

    private static Class identityHashMapClass = null;

    static
    {
        _init();
    }

    public static void _init()
    {
        try
        {
            _props = new Properties();

            String customPropertyFileNames =
                System.getProperty("custom.props");

            String home = System.getProperty("user.home");
            String sep = System.getProperty("file.separator");
            String lib = System.getProperty("java.home");

            /* look for home directory config files first */

            propertiesFiles.addElement(home + sep + propertiesFile1);
            propertiesFiles.addElement(home + sep + propertiesFile2);

            /* look for config files in "." */

            propertiesFiles.addElement(propertiesFile1);
            propertiesFiles.addElement(propertiesFile2);


            /* look for config files in java.home/lib */

            propertiesFiles.addElement(lib + sep + "lib" + sep + propertiesFile1);
            propertiesFiles.addElement(lib + sep + "lib" + sep + propertiesFile2);
            boolean loaded = false;

            // Full name of the config file
            String sConfigFile = null;

            /*
             * load config files from the default ClassLoader's classpath
             *
             * supported by Per Bockman (pebo@enea.se)
             */
            //#ifjdk 1.2
            try
            {
                java.net.URL url = null;

                //try first file name
                url =
                    ClassLoader.getSystemResource( propertiesFile1 );

                if( url == null )
                {
                    //first is not found, so try second
                    url =
                        ClassLoader.getSystemResource( propertiesFile2 );
                }

                if( url != null )
                {
                    _props.load( url.openStream() );

                    sConfigFile = url.toString();

                    loaded = true;
                }
            }
            catch(java.io.IOException ioe)
            {
                // ignore it
            }
            //#endif

            if( ! loaded ) //no props file found in classpath
            {
                for( int i = 0; i < propertiesFiles.size(); ++i )
                {
                    try
                    {
                        _props.load(
                            new BufferedInputStream(
                                new FileInputStream(
                                    (String) propertiesFiles.elementAt( i ) )));

                        loaded = true;
                        sConfigFile = (String) propertiesFiles.elementAt( i );

                        break;
                    }
                    catch ( Exception e )
                    {
                        //ignore
                    }
                }
            }

            /*  load additional  properties from  a  custom properties
                file in  addition to the ones loaded  from the various
                jacorb.properties files.  This is loadded last so that
                its    properties    override    any    settings    in
                jacorb.properties files
            */

            if( customPropertyFileNames != null )
            {
                try
                {
                    StringTokenizer strtok =
                        new StringTokenizer(customPropertyFileNames, ",");

                    while( strtok.hasMoreTokens() )
                    {
                        _props.load(
                            new BufferedInputStream(
                                new FileInputStream( strtok.nextToken() )));
                    }

                    loaded = true;
                }
                catch ( IOException e )
                {
                    //ignore
                }
            }

            _props.putAll( System.getProperties() );

            //read prop values to set fields ov this class
            readValues();

            // initialize default logger factory and create a logger:
            initLogging();

            if( logger == null )
                throw new Error("Logger is null!");

            if( _verbosity > 0 &&
                    ! loaded &&
                    ! _props.getProperty( "jacorb.suppress_no_props_warning", "off" ).equals( "on" )) // rt
            {
                System.err.println( "#####################################################################" );

                System.err.println("WARNING: no properties file found! This warning can be ignored \nfor applets or suppressed by setting jacorb.suppress_no_props_warning\nto on. A file called \"jacorb.properties\" or \".jacorb_properties\"\nshould be present in the classpath, the home directory (" + home + "), \nthe current directory (.) or in Javas lib directory (" + lib + ')'  );

                System.err.println( "#####################################################################\n" );
            }

            if( _verbosity > 2 && sConfigFile != null)
            {
                System.err.println("Setup Info: properties was file loaded from: " + sConfigFile );
            }
        }
        catch(SecurityException secex)
        {
            System.out.println("Could not read local jacorb properties.");
        }


    }


    /** creating an object ensures that Environment is properly initialized*/

    private Environment()
    {
    }

    /**
     * called from ORB.init()
     */

    public static void addProperties(java.util.Properties other_props)
    {
        if( _props == null )
            _props = new java.util.Properties();

        if( other_props != null )
        {
            try
            {
                _props.putAll( System.getProperties() );
            }
            catch( SecurityException se )
            {
                // not allowed for applets
                se.printStackTrace();
            }

            _props.putAll( other_props );

            readValues();
        }
    }


    /**
     * Tries to read value from propname and then suffix locations. Returns
     * null if value was not found.
     */
    private static String readValue(String propname,String prefix)
    {
        if (_props.getProperty(propname) != null)
            return _props.getProperty(propname);
        else if (prefix!=null && _props.getProperty(prefix) != null)
            return _props.getProperty(prefix);
        else return null;
    }

    /**
     * Uses reflection to set field varName to value get from readValue(String,String).
     * Converts into String,long,int and boolean ("on"==true).
     */
    private static void readValue(String varName,String propname,String prefix)
    {
        String o = readValue(propname,prefix);
        if( o == null)
            return;
        if( varName.equals("_retries"))
            _retries = Integer.parseInt(o);
        else if( varName.equals("_retry_interval"))
            _retry_interval = Integer.parseInt(o);
        else if( varName.equals("_cache_entry_lifetime"))
            _cache_entry_lifetime = Long.parseLong(o);
        else if( varName.equals("_outbuf_size"))
            _outbuf_size = Integer.parseInt(o);
        else if( varName.equals("_max_managed_bufsize"))
            maxManagedBufSize = Integer.parseInt(o);
        else if( varName.equals("_compactTypecodes"))
            compactTypecodes = Integer.parseInt(o);
        else if( varName.equals("_default_context"))
            _default_context = o;
        else    if( varName.equals("_orb_domain_filename"))
            _orb_domain_filename = o;
        else    if( varName.equals("_default_domains"))
            _default_domains= o;
        else    if( varName.equals("_verbosity"))
            _verbosity = Integer.parseInt(o);
        else    if( varName.equals("_locate_on_bind"))
            _locate_on_bind = (o.equalsIgnoreCase("on")? true : false);
        else    if( varName.equals("_cache_references"))
            _cache_references = (o.equalsIgnoreCase("on")? true : false);
        else if( varName.equals("_monitoring_on"))
            _monitoring_on = (o.equalsIgnoreCase("on")? true : false);
        else  if( varName.equals("_use_imr"))
            _use_imr =  (o.equalsIgnoreCase("on")? true : false);
        else  if( varName.equals("_use_imr_endpoint"))
            _use_imr_endpoint =  (o.equalsIgnoreCase("on")? true : false);
        else  if( varName.equals("_use_domain"))
            _use_domain =  (o.equalsIgnoreCase("on")? true : false);
        else  if( varName.equals("_mount_orb_domain"))
            _mount_orb_domain =
                (o.equalsIgnoreCase("off")? false : true);
        else    if( varName.equals("_thread_pool_max"))
            _thread_pool_max = Integer.parseInt(o);
        else    if( varName.equals("_thread_pool_min"))
            _thread_pool_min = Integer.parseInt(o);
        else    if( varName.equals("_queue_max"))
            _queue_max = Integer.parseInt(o);
        else if (varName.equals("_queue_min"))
            _queue_min = Integer.parseInt(o);
        else if (varName.equals("_queue_wait"))
            _queue_wait = o.equalsIgnoreCase("off") ? false : true;
        else if( varName.equals("_use_appligator_for_applets"))
            _use_appligator_for_applets = (o.equalsIgnoreCase("off")? false : true );
        else if( varName.equals("_use_appligator_for_applications"))
            _use_appligator_for_applications = (o.equalsIgnoreCase("off")? false : true );
        else if( varName.equals("_use_httptunneling_for")){
            StringTokenizer tokenizer=new StringTokenizer((String)o,",");
            while(tokenizer.hasMoreTokens()){
                String s=tokenizer.nextToken();
                System.out.println("HTTP Tunneling set for:"+s);
                _use_httptunneling_for.put(s,new Object());
            }
        }
        else  if( varName.equals("_impl_name"))
            _impl_name = o.getBytes();
        else if( varName.equals("strict_check_on_tc_creation"))
            strict_check_on_tc_creation = (o.equalsIgnoreCase("on")? true : false);
    }

    private static void readValues()
    {
        readValue("_verbosity", "verbosity", jacorbPrefix + "verbosity");
        readValue("_retries","retries",jacorbPrefix+"retries");
        readValue("_retry_interval","retry_interval",jacorbPrefix+"retry_interval");
        readValue("_cache_entry_lifetime","_cache_entry_lifetime",jacorbPrefix+"domain.cache_entry.lifetime");
        readValue("_outbuf_size","outbuf_size",jacorbPrefix+"outbuf_size");
        readValue("_max_managed_bufsize","maxManagedBufSize",jacorbPrefix+"maxManagedBufSize");
        readValue("_compactTypecodes","compactTypecodes",jacorbPrefix+"compactTypecodes");
        readValue("_orb_domain_filename","ds",jacorbPrefix+"orb_domain.filename");
        readValue("_default_domains","ds",jacorbPrefix+"poa.default_domains");
        readValue("_locate_on_bind","locate_on_bind",jacorbPrefix+"locate_on_bind");
        readValue("_cache_references","reference_caching",jacorbPrefix+"reference_caching");
        readValue("_monitoring_on","monitoring",poaPrefix+"monitoring");
        readValue("_use_imr","use_imr",jacorbPrefix+"use_imr");
        readValue("_impl_name","implname",jacorbPrefix+"implname");
        readValue("_use_imr_endpoint","use_imr_endpoint",jacorbPrefix+"use_imr_endpoint");
        readValue("_use_domain","use_domain",jacorbPrefix+"use_domain");
        readValue("_mount_orb_domain","_mount_orb_domain", jacorbPrefix+"orb_domain.mount");
        readValue("_thread_pool_max","thread_pool_max",poaPrefix+"thread_pool_max");
        readValue("_thread_pool_min","thread_pool_min",poaPrefix+"thread_pool_min");
        readValue("_queue_max","queue_max",poaPrefix+"queue_max");
        readValue("_queue_min","queue_min",poaPrefix+"queue_min");
        readValue("_queue_wait","queue_wait",poaPrefix+"queue_wait");
        readValue("_use_appligator_for_applets", jacorbPrefix+"use_appligator_for_applets", null);
        readValue("_use_appligator_for_applications", jacorbPrefix+"use_appligator_for_applications", null);
        readValue("_use_httptunneling_for",jacorbPrefix+"use_httptunneling_for", null);
        readValue("strict_check_on_tc_creation","strict_check_on_tc_creation",jacorbPrefix+"interop.strict_check_on_tc_creation");
    }

    /**
     * Set up JacORB logging. Will create logger factory and root
     * logger object according to configuration parameters. The value
     * of the property <tt>jacorb.log.loggerFactory</tt> determines the
     * logger factory class name that is used to create the root logger.
     *
     * @since 2.0 beta 3 
     */

    private static void initLogging()
    {
        append = isPropertyOn("jacorb.logfile.append");

        if ( _props.getProperty("logfile") != null)
        {
            logFileName = _props.getProperty("logfile");
        }
        else if (_props.getProperty( jacorbPrefix+"logfile") != null)
        {
            logFileName = _props.getProperty(jacorbPrefix+"logfile");
        }

        String size = _props.getProperty( jacorbPrefix + "logfile.maxLogSize" );
        if( size != null )
        {
            _max_log_size = Integer.parseInt( size );
        }

        if (logFileName != null && !logFileName.equals (""))
        {
            // Convert $implname postfix to implementation name
            if (logFileName.endsWith ("$implname"))
            {
                logFileName = logFileName.substring (0, logFileName.length () - 9);

                if (_props.getProperty ("implname") != null)
                {
                    logFileName += _props.getProperty ("implname");
                }
                else if (_props.getProperty (jacorbPrefix + "implname") != null)
                {
                    logFileName += _props.getProperty (jacorbPrefix + "implname");
                }
                else
                {
                    // Just in case implname has not been set
                    logFileName += "log";
                }
            }
        }

        // If log file already set force append (prevent file corruption)
        if ( logger != null)
        {
            append = true;
        }

        String clzName = getProperty("jacorb.log.loggerFactory");
        Class loggerFactoryClz = null;

        try
        {
            if ( clzName != null)
            {
                loggerFactoryClz = classForName(clzName);
            }
            else
            {
                loggerFactoryClz = classForName(loggerFactoryClzName);
            }
            loggerFactory = (LoggerFactory)loggerFactoryClz.newInstance();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        
        if (loggerFactory == null)
        {
            System.err.println("Configuration Error, could not create logger!"); 
        }
        
        if (logFileName != null)
        {
            try
            {
                logger = loggerFactory.getNamedLogger("jacorb",logFileName,_max_log_size);
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

    // value getters
    public static final  boolean getStrictCheckOnTypecodeCreation()
    {
        return strict_check_on_tc_creation; 
    }

    public static final  boolean isMonitoringOn() 
    {
        return _monitoring_on;   
    }

    public static final  Properties jacorbProperties() 
    { 
        return _props;   
    }

    /**
     * @return the root logger object for JacORB
     */

    public static final Logger getLogger()
    {
        return logger;
    }

    /**
     * @return the root logger object for JacORB
     * @deprecated
     */

    public static final Logger logFileOut()
    {
        return getLogger();
    }

    /*    public static final  String logFileName() { return logFileName; }*/


    /**
     * @return the max size of the log file in kilo bytes. A size of 0 
     * means no limit, any other size requires log file rotation.
     */

    public static final  long maxLogSize()
    {
        return _max_log_size;
    }

    public static final  long currentLogSize () { return _current_log_size; }

    public static final int noOfRetries() { return _retries;   }
    public static final  int outBufSize() { return _outbuf_size; }
    public static final boolean locateOnBind() { return _locate_on_bind; }
    public static final boolean cacheReferences() { return _cache_references; }

    public static final boolean queueWait() { return _queue_wait;  }
    public static final int queueMin() { return _queue_min;  }
    public static final int queueMax() { return _queue_max;  }
    public static final long retryInterval() { return _retry_interval; }

    public static final String ORBDomainFilename()    { return _orb_domain_filename;  }
    public static final String DefaultDomains()       { return _default_domains;  }
    public static final long   LifetimeOfCacheEntry() { return _cache_entry_lifetime; }

    public static final boolean useImR()    { return _use_imr;    }
    public static final boolean useImREndpoint()    { return _use_imr_endpoint;    }
    public static final boolean useDomain()      { return _use_domain; }
    public static final boolean mountORBDomain() { return _mount_orb_domain; }


    public static final  int threadPoolMax() { return _thread_pool_max; }
    public static final  int threadPoolMin() { return _thread_pool_min; }

    public static final int verbosityLevel()
    {
        return _verbosity;
    }

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

    public static final boolean useHTTPTunneling(String ipaddr)
    {
        Object o=_use_httptunneling_for.get(ipaddr);
        return (o!=null);
    }


    public static int getMaxManagedBufSize()
    {
        return maxManagedBufSize;
    }

    public static int getCompactTypecodes ()
    {
        return compactTypecodes;
    }

    public static String imrProxyHost()
    {
        return _props.getProperty (jacorbPrefix + "imr.ior_proxy_host");
    }

    public static int imrProxyPort()
    {
        return getIntPropertyWithDefault (jacorbPrefix + "imr.ior_proxy_port",
                                          -1);
    }

    public static String iorProxyHost()
    {
        return _props.getProperty (jacorbPrefix + "ior_proxy_host");
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

    /**
     * generic
     */

    public static String getProperty( String key )
    {
        return _props.getProperty(key);
    }

    public static String getProperty( String key, String def )
    {
        return _props.getProperty( key, def );
    }

    /**
     * This will return true if the property's value is
     * "on". Otherwise (i.e. value "off", or property not set), false
     * is returned.
     */

    public static boolean isPropertyOn(String key)
    {
        String s = _props.getProperty (key, "off");
        return "on".equals (s);
    }

    public static boolean isPropertyOff(String key)
    {
        return (!isPropertyOn (key));
    }

    public static long getLongProperty(String key, int base, long def)
    {
        String s = _props.getProperty (key);

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
        String s = _props.getProperty( key );

        try
        {
            return Long.parseLong (s, base);
        }
        catch( NumberFormatException nfe )
        {
            throw new Error( "Unable to create long from string >>" +
                             s + "<<. " +
                             "Please check property \"" + key + '\"');
        }
    }

    public static int getIntProperty( String key, int base )
    {
        String s = _props.getProperty( key );

        try
        {
            return Integer.parseInt( s, base );
        }
        catch( NumberFormatException nfe )
        {
            throw new Error( "Unable to create int from string >>" +
                             s + "<<. " +
                             "Please check property \"" + key + '\"');
        }
    }

    public static int getIntPropertyWithDefault( String key, int def )
    {
        String s = _props.getProperty( key );

        if( s != null && s.length() > 0 )
        {
            try
            {
                return Integer.parseInt( s );
            }
            catch( NumberFormatException nfe )
            {
                throw new Error( "Unable to create int from string >>" +
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
     * Create an object from the give property. The classes default
     * constructor will be used.
     *
     * @return null or an object of the class of the keys value
     * @throws Error if reflection fails.
     */
    public static Object getObjectProperty( String key )
    {
        String s = _props.getProperty( key );

        if( s != null && s.length() > 0 )
        {
            try
            {
                Class c = classForName( s );
                return c.newInstance();
            }
            catch( Exception e )
            {
                throw new Error( "Unable to build class from key >" +
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
        String value  = _props.getProperty (key);
        if (value != null)
        {
            StringTokenizer tok = new StringTokenizer (value, ",");
            while (tok.hasMoreTokens())
                result.add (tok.nextToken().trim());
        }
        return result;
    }

    /*
      public static int getIntProperty( String key )
      {
      return getIntProperty( key, 10 );
      }
    */

    public static boolean hasProperty( String key )
    {
        return _props.containsKey( key );
    }

    public static void setProperty( String key, String value )
    {
        //for jdk1.1 compatibility (was _props.setProperty())
        _props.put( key, value );
    }

    public static String[] getPropertyValueList(String key)
    {
        String list =  _props.getProperty(key);

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

    /** returns a copy of the org.jacorb properties. */
    public static Properties getProperties()
    {
        return (Properties) _props.clone();
    }

    public static final String date()
    {
        if (dateFormatter == null)
        {
            dateFormatter = new SimpleDateFormat ("dd:MM:yyyy");
        }
        return dateFormatter.format (new Date ());
    }

    public static final String time()
    {
        if (timeFormatter == null)
        {
            timeFormatter = new SimpleDateFormat ("H:mm:ss:SSS");
        }
        return timeFormatter.format (new Date ());
    }

    public static final void readFromURL(java.net.URL _url)
    {
        URL=_url;
        System.out.println("Reading properties from url:"+URL.toString());
        try
        {
            _props=new Properties();
            _props.load(new java.io.BufferedInputStream(URL.openStream()));
        }
        catch(Exception e)
        {
            System.out.println("Could not read properties from URL, reason: "+
                               e.toString());
        }
        readValues();
        // _props.list( System.out );
    }

    public static final byte[] serverId()
    {
        if (_server_id == null)
            _server_id = String.valueOf((long)(Math.random()*9999999999L)).getBytes();
        return _server_id;
    }

    /**
     * Collects all properties with prefix "org.omg.PortableInterceptor.ORBInitializerClass."
     * and try to instanciate their values as ORBInitializer-Classes.
     *
     * @return a Vector containing ORBInitializer instances
     */

    public static Vector getORBInitializers()
    {
        Enumeration prop_names = _props.propertyNames();
        Vector orb_initializers = new Vector();

        String initializer_prefix =
            "org.omg.PortableInterceptor.ORBInitializerClass.";

        //Test EVERY property if prefix matches.
        //I'm open to suggestions for more efficient ways (noffke)
        while(prop_names.hasMoreElements())
        {
            String prop = (String) prop_names.nextElement();
            if ( prop.startsWith( initializer_prefix ))
            {
                String name = _props.getProperty( prop );
                if( name == null ||
                        name.length() == 0 )
                {
                    if( prop.length() > initializer_prefix.length() )
                    {
                        name =
                            prop.substring( initializer_prefix.length() );
                    }
                }

                if( name == null )
                {
                    continue;
                }

                try
                {
                    orb_initializers.addElement(classForName(name).newInstance());
                    if( logger.isDebugEnabled())
                        logger.debug("Build: " + name);
                }
                catch (Exception e)
                {
                    Debug.output(1, e);
                    Debug.output( 1, "Unable to build ORBInitializer from >>" +
                                  name + "<<" );
                }
            }
        }

        return orb_initializers;
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
            Enumeration prop_names = _props.propertyNames();
            Hashtable properties = new Hashtable();

            // Test EVERY property if prefix matches.
            while( prop_names.hasMoreElements() )
            {
                String name = (String) prop_names.nextElement();
                if ( name.startsWith( prefix ))
                {
                    if( trim )
                    {
                        properties.put( name.substring( prefix.length() + 1) , _props.getProperty(name) );
                    }
                    else
                    {
                        properties.put( name , _props.getProperty(name));
                    }
                }
            }

            /* record for later use */

            if( trim )
                trimmedPrefixProps.put( prefix, properties );
            else
                untrimmedPrefixProps.put( prefix, properties );

            return properties;
        }
    }

    /**
     * Returns the <code>Class</code> object for the class or interface 
     * with the given string name. This method is a replacement for 
     * <code>Class.forName(String name)</code>. Unlike 
     * <code>Class.forName(String name)</code> (which always uses the 
     * caller's loader or one of its ancestors), <code>classForName</code>
     * uses a thread-specific loader that has no delegation relationship
     * with the caller's loader. It attempts the load the desired class 
     * with the thread-specific context class loader and falls back to 
     * <code>Class.forName(String name)</code> only if the context class 
     * loader cannot load the class.  
     * <p>
     * Loading a class with a loader that is not necessarily an ancestor
     * of the caller's loader is a crucial thing in many scenarios. As an
     * example, assume that JacORB was loaded by the boot class loader,
     * and suppose that some code in JacORB contains a call
     * <code>Class.forName(someUserClass)</code>. Such usage of 
     * <code>Class.forName</code> effectively forces the user to place 
     * <code>someUserClass</code> in the boot class path. If 
     * <code>classForName(someUserClass)</code> were used instead, the user
     * class would be loaded by the context class loader, which by default 
     * is set to the system (CLASSPATH) classloader. 
     * <p>
     * In this simple example above, the default setting of the context class
     * loader allows classes in the boot classpath to reach classes in the 
     * system classpath. In other scenarios, the context class loader might 
     * be different from the system classloader. Middleware systems like 
     * servlet containers or EJB containers set the context class loader so 
     * that a given thread can reach user-provided classes that are not in 
     * the system classpath.
     * <p>
     * For maximum flexibility, <code>classForName</code> should replace
     * <code>Class.forName(String name)</code> in nearly all cases.
     * 
     * @param name the fully qualified name of a class
     *
     * @return the Class object for that class
     *
     * @throws IllegalArgumentException if <code>name</code> is null
     * @throws ClassNotFoundException if the named class cannot be found
     * @throws LinkageError if the linkage fails
     * @throws ExceptionInInitializerError if the class initialization fails
     */

    public static Class classForName(String name) 
        throws ClassNotFoundException, IllegalArgumentException
    {
        if (name == null)
            throw new IllegalArgumentException("Class name must not be null!");
        try 
        {
            // Here we prefer classLoader.loadClass() over the three-argument 
            // form of Class.forName(), as the latter is reported to cause 
            // caching of stale Class instances (due to a buggy cache of 
            // loaded classes).
            return Thread.currentThread().getContextClassLoader().loadClass(name);
        }
        catch (Exception e) 
        {
            // As a fallback, we prefer Class.forName(name) because it loads 
            // array classes (i.e., it handles arguments like 
            // "[Lsome.class.Name;" or "[[I;", which classLoader.loadClass()
            // does not handle).
            return Class.forName(name);
        }
    }

    /**
     * @return the default logger factory
     * @since JacORB 2.0 beta 3
     */

    public static LoggerFactory getLoggerFactory()
    {
        return loggerFactory;
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
                    throw new RuntimeException(e);
                }
            }
        }
        try
        {
            return (Map)identityHashMapClass.newInstance();
        }
        catch (Exception exc)
        {
            throw new RuntimeException(exc);
        }   
    }
 
}
