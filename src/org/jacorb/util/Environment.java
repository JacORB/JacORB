package org.jacorb.util;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2001  Gerald Brose.
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
    private static int                  _charset_flags = 0x0D;

    private static String               _default_context = "<undefined>";

    /** domain-specific */

    private static int                  _verbosity = 0;

    private static boolean              _locate_on_bind = false;
    private static boolean              _use_imr = false;
    private static boolean              _cache_references = false;
    private static PrintWriter          _log_file_out = null;

    // domain service configuration
    /** indicates whether the domain service is used or not */
    private static boolean          _use_domain= false;

    /** if set to true (default), every orb domain gets mounted as child domain to 
     *	the domain server on creation */
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

    /** IIOP proxy/appligator */
    private static String               _proxy_server=null;
    private static boolean              _use_appligator_for_applets = true;
    private static boolean              _use_appligator_for_applications = false;
    private static Hashtable            _use_httptunneling_for = new Hashtable();

    public static java.net.URL          URL=null;
    
    private static byte[]               _impl_name = null;
    private static byte[]               _server_id = null;

    /** security-related settings */
    private static String               _keyStore = ".keystore";

    //
    // bnv: security setup information for SSL
    //
    private static boolean _enforce_ssl             = false;
    private static short   _supported_options       = 0x0067;
    private static short   _required_options        = 0x0066;
    private static boolean _support_ssl             = false;
    private static String  _default_user            = null;
    private static String  _default_password        = null;
    // rt: ssl client/server is changed per default
    private static boolean _change_ssl_roles        = true;
    
    //
    // bnv: security features for the default user and SecInvocationPolicy objects for default security domain
    //
    //    private static 

    static
    {
        _init();
    }   

    private static void _init()
    {
        try
        {
            /* load system properties to get home dir and file separator */

            _props = new Properties( System.getProperties() );
            
            String customPropertyFileNames = _props.getProperty("custom.props");
            String home = _props.getProperty("user.home");
            String sep = _props.getProperty("file.separator");
            String lib = _props.getProperty("java.home");

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
            for( int i=0; !loaded && i < propertiesFiles.size(); ++i ) 
            {
                try 
                {
                    _props.load( 
                          new BufferedInputStream( 
                               new FileInputStream(
                                    (String)propertiesFiles.elementAt( i ) )));
                    loaded = true;
                } 
                catch ( Exception e ) 
                {
                    
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
                } 
                catch ( IOException e ) 
                {                    
                }
            }

            /* we have to refresh system properties here because otherwise
               command line options would be overriden by properties from 
               files */

            merge( _props, System.getProperties());

            //   _props.putAll(System.getProperties()); 

            if(!loaded)
            { 
                StringBuffer buf = new StringBuffer();
                for(int i=0; i < propertiesFiles.size(); ++i) 
                {
                    if(i > 0) 
                    {
                        buf.append(" or ");
                    }
                    buf.append((String)propertiesFiles.elementAt(i));
                }       
                if( _verbosity > 3)
                {
                    System.err.println("Setup Warning: no properties file found! This warning can be ignored\n for applets. (A properties file should be in the current directory or in \n" + buf.toString() + "t)");
                } 
            }

            readValues();
            if ( _enforce_ssl )
            {
                if( !_support_ssl ) {// bnv
                    System.err.println ( "Security Policy violation: SSL is not supported."
                                         + "Check your environment please."
                                         );
                    System.exit( 0 );
                }
                else
                    org.jacorb.util.Debug.output( 1, "Security policy will enforce SSL connections" );
            }
        }
        catch(SecurityException secex)
        {
            System.out.println("Could not read local org.jacorb properties.");
        }
    }   


    /** creating an object ensures that Environment is properly initialized*/

    public Environment()
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
                merge( _props, System.getProperties());
            }
            catch( SecurityException se )
            {
                // not allowed for applets
            }
            merge( _props, other_props );
            //_props.putAll( other_props );
            //_props.putAll(System.getProperties()); 
            readValues();
            //      _props.list( System.out );
        }
    }

    /**
     * Merges two sets of properties into the first argument.
     * If a key appears on both Properties, the value from the 
     * second Properties object takes precedence.
     * 
     * Replaces calls to properties.putAll(), which is JDK 1.2 only
     */

    private static void merge( java.util.Properties target, 
                               java.util.Properties source )
    {
        for( Enumeration e = source.propertyNames(); e.hasMoreElements();  )
        {
            String key = (String) e.nextElement();
            //although nicer, setProperty doesn't exist in jdk1.1
            target.put( key, source.getProperty(key) );
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
        else if( varName.equals("_proxy_server"))
            _proxy_server = o;  
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
        else    if( varName.equals("_charset_flags"))
            _charset_flags = Integer.parseInt(o);
        else    if( varName.equals("_keyStore")) {
            String home = _props.getProperty("user.home");
            String sep = _props.getProperty("file.separator");
            _keyStore = home + sep + o;
        }
        else    if( varName.equals("_impl_name"))
            _impl_name = o.getBytes();
        else    if ( varName.equals ( "_support_ssl")) // gb
            _support_ssl = ( o.equalsIgnoreCase ( "on" ) ? true : false );
        else    if ( varName.equals ( "_supported_options")) // bnv
            _supported_options = Integer.valueOf( o, 16 ).shortValue();
        else    if ( varName.equals ( "_required_options")) // bnv
            _required_options = Integer.valueOf( o, 16 ).shortValue();
        else    if ( varName.equals ( "_enforce_ssl")) // bnv
            _enforce_ssl = ( o.equalsIgnoreCase ( "on" ) ? true : false );
        else    if( varName.equals("_default_user")) // bnv
            _default_user = o;
        else    if( varName.equals("_default_password")) // bnv
            _default_password = o;
        else    if ( varName.equals ( "_change_ssl_roles")) // rt
            _change_ssl_roles = ( o.equalsIgnoreCase ( "on" ) ? true : false );
    }
        
    private static void readValues()
    {
        String logFileName = null;                      
        if (_props.getProperty("logfile") != null)
            logFileName = _props.getProperty("logfile");
        else if (_props.getProperty(jacorbPrefix+"logfile") != null)
            logFileName = _props.getProperty(jacorbPrefix+"logfile");
                    
        if (logFileName != null && !logFileName.equals("")) 
        {
            try 
            {
                _log_file_out = new PrintWriter(new FileOutputStream(logFileName));
                System.out.println("Write output to log file \""+logFileName+"\"");
            }
            catch (java.io.IOException ioe) 
            {
                System.out.println("Cannot access log file \""+logFileName+"\"");
            }
        }   

        readValue("_retries","retries",jacorbPrefix+"retries");
        readValue("_retry_interval","retry_interval",jacorbPrefix+"retry_interval");
        readValue("_cache_entry_lifetime","_cache_entry_lifetime",jacorbPrefix+"domain.cache_entry.lifetime");
        readValue("_outbuf_size","outbuf_size",jacorbPrefix+"outbuf_size");
        readValue("_max_managedbufsize","maxManagedBufSize",jacorbPrefix+"maxManagedBufSize");
        readValue("_orb_domain_filename","ds",jacorbPrefix+"orb_domain.filename");
        readValue("_default_domains","ds",jacorbPrefix+"poa.default_domains");

        readValue("_verbosity","verbosity",jacorbPrefix+"verbosity");
        readValue("_locate_on_bind","locate_on_bind",jacorbPrefix+"locate_on_bind");
        readValue("_cache_references","reference_caching",jacorbPrefix+"reference_caching");

        readValue("_monitoring_on","monitoring",poaPrefix+"monitoring");
        readValue("_use_imr","use_imr",jacorbPrefix+"use_imr");
        readValue("_use_domain","use_domain",jacorbPrefix+"use_domain");
        readValue("_mount_orb_domain","_mount_orb_domain", jacorbPrefix+"orb_domain.mount");
        readValue("_thread_pool_max","thread_pool_max",poaPrefix+"thread_pool_max");
        readValue("_thread_pool_min","thread_pool_min",poaPrefix+"thread_pool_min");
        readValue("_queue_max","queue_max",poaPrefix+"queue_max");
        readValue("_proxy_server","proxy",jacorbPrefix+"ProxyServerURL");
        readValue("_use_appligator_for_applets", jacorbPrefix+"use_appligator_for_applets", null);
        readValue("_use_appligator_for_applications", jacorbPrefix+"use_appligator_for_applications", null);
        readValue("_use_httptunneling_for",jacorbPrefix+"use_httptunneling_for", null);

        // devik: load charset_flags, bits have this meaning:
        // 1 - insert CHARSET profile into MULTICOMPONENT part of IOR
        // 2 - insert CHARSET profile into IOP part of IOR
        // 4 - send negotiated contexts to server
        // 8 - switch TCS on charset context receipt
        readValue("_charset_flags","charset_flags",jacorbPrefix+"charset_flags");

        readValue("_impl_name","implname",jacorbPrefix+"implname");

        // bnv: read SSL policy and default user alias and passphrase for accessing the key store
        readValue("_enforce_ssl", jacorbPrefix + "security.enforce_ssl",null);
        readValue("_supported_options", jacorbPrefix + "security.ssl.supported_options",null);
        readValue("_required_options", jacorbPrefix + "security.ssl.required_options",null);
        readValue("_support_ssl", jacorbPrefix + "security.support_ssl",null);
        readValue("_default_user",jacorbPrefix+"security.default_user",null);
        readValue("_default_password",jacorbPrefix+"security.default_password",null);
        readValue("_change_ssl_roles", jacorbPrefix + "security.change_ssl_roles",null); // rt
        // the name of the Java keystore file
        readValue("_keyStore",jacorbPrefix+"security.keystore",null);

    }

    // value getters
    public static final boolean charsetUpdateMulti() { return (_charset_flags & 1)!=0;  }
    public static final boolean charsetUpdateIOP() { return (_charset_flags & 2)!=0;  }
    public static final boolean charsetSendCtx() { return (_charset_flags & 4)!=0;  }
    public static final boolean charsetScanCtx() { return (_charset_flags & 8)!=0;  }

    public static final  boolean isMonitoringOn() { return _monitoring_on;   }
    public static final  Properties jacorbProperties() { return _props;   }
    public static final  PrintWriter logFileOut() {     return _log_file_out;  }
    public static final int noOfRetries() { return _retries;   }
    public static final  int outBufSize() { return _outbuf_size; }
    public static final boolean locateOnBind() { return _locate_on_bind; }
    public static final boolean cacheReferences() { return _cache_references; }

    public static final int queueMax() { return _queue_max;  }
    public static final long retryInterval() { return _retry_interval; }

    public static final String ORBDomainFilename()    { return _orb_domain_filename;  }
    public static final String DefaultDomains()       { return _default_domains;  }
    public static final long   LifetimeOfCacheEntry() { return _cache_entry_lifetime; }
    
    public static final boolean useImR()    { return _use_imr;    }
    public static final boolean useDomain()      { return _use_domain; }
    public static final boolean mountORBDomain() { return _mount_orb_domain; }
        

    public static final  int threadPoolMax() { return _thread_pool_max; }
    public static final  int threadPoolMin() { return _thread_pool_min; }

    public static final boolean enforceSSL () { return _enforce_ssl; } // bnv
    public static final boolean supportSSL () { return _support_ssl; } // bnv
    public static final boolean changeSSLRoles () { return _change_ssl_roles; } // rt
    public static final short supportedBySSL () // bnv
    {
        return _supported_options;
    }
    public static final short requiredBySSL () // bnv
    {
        return _required_options;
    }

    public static final String defaultUser() { return _default_user; }
    public static final String defaultPassword() { return _default_password; }

    public static final int verbosityLevel() 
    { 
        return _verbosity; 
    }

    public static final String proxyURL() { return _proxy_server; }
    public static final String keyStore() { return _keyStore; }

    public static final byte[] implName() { return _impl_name; }

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

    public static void setProxyURL(String url) 
    {
        _proxy_server=url;  
    }


    public static int getMaxManagedBufSize()
    {
        return maxManagedBufSize;
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

    public static final String time() 
    {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        return cal.get(Calendar.HOUR_OF_DAY) +
            ":" +
            ( (cal.get(Calendar.MINUTE)<10) ? 
              "0"+cal.get(Calendar.MINUTE) : 
              ""+cal.get(Calendar.MINUTE) ) +
            ":" +
            ( (cal.get(Calendar.SECOND)<10) ? 
              "0"+cal.get(Calendar.SECOND) : 
              ""+cal.get(Calendar.SECOND) );
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
            if ( prop.startsWith("org.omg.PortableInterceptor.ORBInitializerClass."))
            {
                String name = _props.getProperty(prop);
                try
                {
                    orb_initializers.addElement(Class.forName(name).newInstance());
                    Debug.output(Debug.INTERCEPTOR | Debug.DEBUG1, 
                                 "Build: " + name);
                }
                catch (Exception e)
                {
                    Debug.output(1, e);
                }
            }
        }
        
        return orb_initializers;
    }

    /**
     * Collects all properties with a given prefix 
     *
     * @return a Vector of strings (propery values)
     */

    public static Hashtable getProperties(String prefix)
    {
        Enumeration prop_names = _props.propertyNames();
        Hashtable _properties = new Hashtable();
        
        // Test EVERY property if prefix matches.
        while(prop_names.hasMoreElements())
        {
            String name = (String) prop_names.nextElement();
            if ( name.startsWith( prefix ))
            {
                _properties.put(name, _props.getProperty(name));
            }
        }
        
        return _properties;
    }
  

}







