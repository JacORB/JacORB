package org.jacorb.notification.util;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2003 Gerald Brose
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

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.log.Hierarchy;
import org.apache.log.LogEvent;
import org.apache.log.LogTarget;
import org.apache.log.Logger;
import org.apache.log.Priority;
import org.apache.log.format.PatternFormatter;
import org.apache.log.output.io.FileTarget;
import org.apache.log.output.io.SafeFileTarget;
import org.apache.log.output.io.StreamTarget;
import org.apache.log.output.net.SocketOutputTarget;
import org.jacorb.util.Environment;
import java.util.Iterator;

/**
 * LogConfiguration.java
 *
 *
 * @author Alphonse Bendt
 * @version $Id$
 */

public class LogConfiguration
{
    private static final String NO_PATTERN = "No Pattern applicable";

    private static final boolean DEBUG = false;

    private static void debug(String msg) {
	if (DEBUG) {
	    System.err.println(msg);
	}
    }

    private static void debug() {
	debug("");
    }

    ////////////////////////////////////////

    private static class LogListener
    {
        ServerSocket serverSocket;
        Thread serverThread;
        boolean runThread;
    }

    ////////////////////////////////////////

    private static LogConfiguration singleton = new LogConfiguration();

    private boolean isConfigured = false;

    public static final String LOG_IS_SERVER = ".logserver";
    public static final String LOG_CATEGORY = ".category";
    public static final String LOG_PATTERN = ".pattern";
    public static final String LOG_REMOTEHOST = ".remotehost";
    public static final String LOG_REMOTEPORT = ".remoteport";
    public static final String LOG_PRIORITY = ".priority";
    public static final String LOG_FILE = ".file";

    private static final String LOG_PREFIX = "jacorb.logging.";

    private static final String[] STRING_ARRAY_TEMPLATE = 
	new String[ 0 ];

    public static final String DEFAULT_PATTERN =
        "%7.7{priority} [%-.25{category}] (%{context}): %{message}\\n%{throwable}";

    Map logServerRegistrations = new Hashtable();

    private ConfigurationMap configurationMap_;

    ////////////////////////////////////////

    /**
     * Constructor is not accessible. Singleton.
     */
    private LogConfiguration()
    {}

    ////////////////////////////////////////

    /**
     * Singleton access to the instance.
     */
    public static LogConfiguration getInstance()
    {
        return singleton;
    }

    private static boolean isLogAttribute(String name) {
	if (name.indexOf(LOG_IS_SERVER) != -1) {
	    return true;
	} else if (name.indexOf(LOG_CATEGORY) != -1) {
	    return true;
	} else if (name.indexOf(LOG_PATTERN) != -1) {
	    return true;
	} else if (name.indexOf(LOG_REMOTEHOST) != -1) {
	    return true;
	} else if (name.indexOf(LOG_REMOTEPORT) != -1) {
	    return true;
	} else if (name.indexOf(LOG_PRIORITY) != -1) {
	    return true;
	} else if (name.indexOf(LOG_FILE) != -1) {
	    return true;
	}
	return false;
    }

    synchronized public boolean isConfigured()
    {
        return isConfigured;
    }

    private void collectNamedLoggerConfiguration( String loggerName,
						  ConfigurationMap configuration ) 
	throws Exception
    {
        debug( "collecting Configuration for Logger: " + loggerName );

        String target = getLogTarget( loggerName );

        if ( "console".equals( target ) )
        {

            configureConsoleLogger( loggerName, configuration );

        }
        else if ( "file".equals( target ) )
        {

            configureFileLogger( loggerName, configuration );

        }
        else if ( "remote".equals( target ) )
        {

            configureRemoteLogger( loggerName, configuration );

        }
	else {
	    debug("LogTarget " + target + " is unknown");
	}
    }

    private void collectAllNamedLoggerConfigurations( ConfigurationMap configuration ) throws Exception
    {
        String[] loggerNameList = getLoggerNameList();

        for ( int x = 0; x < loggerNameList.length; ++x )
        {

            collectNamedLoggerConfiguration( loggerNameList[ x ], configuration );

        }
    }

    private void setupWithConfiguration( ConfigurationMap configuration )
	throws Exception
    {

        debug();

        String[] categories =
            configuration.getCategories();

        for ( int x = 0; x < categories.length; ++x )
        {

            debug( "Processing Category: " + categories[ x ] );

            Logger _loggerForCategory =
                Hierarchy.getDefaultHierarchy().getLoggerFor( categories[ x ] );

            List listOfConfigurationForCategory =
                configuration.getListOfConfigurationFor( categories[ x ] );

            LogTarget[] listOfLogTargetForCategory =
                new LogTarget[ listOfConfigurationForCategory.size() ];

            for ( int y = 0; y < listOfLogTargetForCategory.length; ++y )
            {

                ConfigurationMap.Entry _entry =
                    ( ConfigurationMap.Entry ) listOfConfigurationForCategory.get( y );

                debug( "Found Configuration:\n" + _entry.toString("\t") );

                listOfLogTargetForCategory[ y ] = _entry.logTarget_;

                _loggerForCategory.setPriority( _entry.priority_ );
            }

            _loggerForCategory.setLogTargets( listOfLogTargetForCategory );
        }
    }

    synchronized public void configure()
    {
        if ( isConfigured() )
        {
            return;
        }

        try
        {

            ConfigurationMap configuration = new ConfigurationMap();

            collectAllNamedLoggerConfigurations( configuration );

            setupWithConfiguration( configuration );

	    configurationMap_ = configuration;

	    isConfigured = true;
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }
    }

    private String[] getLoggerNameList()
    {
	Map allKeys = Environment.getProperties(LOG_PREFIX);

	Vector v = new Vector();
	Iterator i = (allKeys.keySet().iterator());

	while (i.hasNext()) {
	    String value = (String)i.next();

	    String s = value.substring(LOG_PREFIX.length());

	    if (!isLogAttribute(s)) {	    
		v.add(s);
	    }
	}

	return (String[])v.toArray(STRING_ARRAY_TEMPLATE);
    }

    private void configureFileLogger( String name, 
				      ConfigurationMap configurationMap )
	throws IOException
    {
        File _file = 
	    new File( Environment.getProperty( LOG_PREFIX + name + LOG_FILE ) );

	String _pattern = getPattern(name);

        PatternFormatter _formatter = 
	    new PatternFormatter( _pattern );

        FileTarget _fileTarget =
            new SafeFileTarget( _file, true, _formatter );

        addConfiguration( configurationMap, 
			  name, 
			  getCategory( name ), 
			  _pattern,
			  _fileTarget, 
			  getPriority( name ) );
    }

    private void configureConsoleLogger( String name, 
					 ConfigurationMap configuration )
    {
	String _pattern = getPattern(name);

        PatternFormatter _formatter = 
	    new PatternFormatter( _pattern );

        StreamTarget _consoleTarget =
            new StreamTarget( System.out, _formatter );

        addConfiguration( configuration, 
			  name, 
			  getCategory( name ), 
			  _pattern,
			  _consoleTarget, 
			  getPriority( name ) );
    }

    private void configureRemoteLogger( String name, 
					ConfigurationMap configuration )
	throws IOException
    {
        SocketOutputTarget _socketTarget =
            new SocketOutputTarget( getLogHost( name ), 
				    getLogPort( name ) );

        addConfiguration( configuration, 
			  name, 
			  getCategory( name ), 
			  NO_PATTERN,
			  _socketTarget, 
			  getPriority( name ) );
    }

    private void addConfiguration( ConfigurationMap configuration,
                                   String name,
                                   String category,
				   String pattern,
                                   LogTarget logTarget,
                                   Priority priority )
    {

        configuration.addConfiguration( name, 
					category, 
					pattern,
					logTarget,
					priority, 
					isLogServer( name ) );
    }

    private LogTarget createConsoleLogger( String name,
                                           String category,
                                           String pattern,
                                           Priority priority )
    {
        Logger _logger =
            Hierarchy.
            getDefaultHierarchy().
            getLoggerFor( category );

        PatternFormatter _formatter = new PatternFormatter( pattern );

        StreamTarget _consoleTarget =
            new StreamTarget( System.out, _formatter );

        _logger.setLogTargets( new LogTarget[] {_consoleTarget} );

        _logger.setPriority( priority );

        return _consoleTarget;
    }

    public int getLogPortForNamedLogger( String name )
	throws Exception
    {
	
	ConfigurationMap.Entry _entry =
	    configurationMap_.getConfiguration(name);

	if (_entry != null) {
	    if (_entry.remoteEnabled_) {
		return startLogListener( name, _entry.logTarget_ );
	    } else {
		throw new RuntimeException("LogConfiguration " + name + " is not enabled for Remote Access");
	    }
	} else {
	    throw new RuntimeException("LogConfiguration " + name + " is unknown");
	}
    }

    private int startLogListener( String name, 
				  final LogTarget target ) 
	throws Exception
    {
	debug("Creating LogServer for Configuration: " + name);

        final LogListener registration = new LogListener();
        registration.serverSocket = new ServerSocket( 0 );

        debug( "ServerSocket listening at " + registration.serverSocket.getLocalPort() );

        registration.runThread = true;

        Runnable r = new Runnable()
                     {
                         public void run()
                         {
                             while ( registration.runThread )
                             {
                                 try
                                 {
                                     Socket clientSocket = 
					 registration.serverSocket.accept();

                                     ObjectInputStream objectInputStream=
                                         new ObjectInputStream( clientSocket.getInputStream() );

                                     try
                                     {
                                         while ( registration.runThread )
                                         {
                                             LogEvent logEvent = 
						 ( LogEvent ) objectInputStream.readObject();

                                             target.processEvent( logEvent );
                                         }
                                     }
                                     catch ( Exception e )
                                     {}
                                 }
                                 catch ( Exception e )
                                 {}
                             }
                         };
                     };

        registration.serverThread = new Thread( r );
        registration.serverThread.setDaemon( true );
        registration.serverThread.start();

        logServerRegistrations.put( name, registration );

        return registration.serverSocket.getLocalPort();
    }

    private int getLogServerPort( String name )
    {
        return ( ( LogListener ) logServerRegistrations.get( name ) ).serverSocket.getLocalPort();
    }

    private String getLogTarget( String name )
    {
        return Environment.getProperty( LOG_PREFIX + name );
    }

    private String getCategory( String name )
    {
        return Environment.getProperty( LOG_PREFIX + name + LOG_CATEGORY, null );
    }

    private String getPattern( String name )
    {
        return Environment.getProperty( LOG_PREFIX + name + LOG_PATTERN, DEFAULT_PATTERN );
    }

    private Priority getPriority( String name )
    {
        return Priority.getPriorityForName( Environment.getProperty( LOG_PREFIX + name + LOG_PRIORITY ) );
    }

    private boolean isLogServer( String name )
    {
        return Environment.isPropertyOn( LOG_PREFIX + name + LOG_IS_SERVER );
    }

    private String getLogHost( String name )
    {
        return Environment.getProperty( LOG_PREFIX + name + LOG_REMOTEHOST, "localhost" );
    }

    private int getLogPort( String name )
    {
        return Environment.getIntProperty( LOG_PREFIX + name + LOG_REMOTEPORT, 10 );
    }

    public static void setLogLevel(String logger, Priority priority) {
	Logger _logger = 
	    Hierarchy.
	    getDefaultHierarchy().
	    getLoggerFor(logger);

	_logger.setPriority(priority);
    }

    public static void main(String[] args) throws Exception {
	LogConfiguration.getInstance().configure();
    }


}

class ConfigurationMap
{
    private static final String[] STRING_ARRAY_TEMPLATE = 
	new String[ 0 ];

    static class Entry
    {
        String configurationName_;
        String category_;
	String pattern_;
        LogTarget logTarget_;
        Priority priority_;
        boolean remoteEnabled_ = false;

	public String toString() {
	    return toString("");
	}

        public String toString(String offset)
        {
            StringBuffer b = new StringBuffer();

	    b.append(offset);
            b.append( "Configurationname: " );
            b.append( configurationName_ );
            b.append( "\n" );

	    b.append(offset);
            b.append( "Category: " );
            b.append( category_ );
            b.append( "\n" );

	    b.append(offset);
	    b.append( "Pattern: " );
	    b.append( pattern_ );
	    b.append( "\n");

	    b.append(offset);
            b.append( "LogTarget: " );
            b.append( logTarget_.getClass().getName() );
            b.append( "\n" );

	    b.append(offset);
            b.append( "Remote accessible: " );
            b.append( remoteEnabled_ );
            b.append( "\n" );

	    b.append(offset);
	    b.append( priority_ );
	    b.append( "\n" );

            return b.toString();
        }

        Entry( String configurationName,
               String category,
	       String pattern,
               LogTarget logTarget,
               Priority priority,
               boolean remoteEnabled )
        {

            configurationName_ = configurationName;
            category_ = category;
            logTarget_ = logTarget;
	    pattern_ = pattern;
            priority_ = priority;
            remoteEnabled_ = remoteEnabled;
        }
    }

    private Map mapByCategory_ = new Hashtable();
    private Map mapByName_ = new Hashtable();

    private List getCreateList( String name )
    {
        if ( !mapByCategory_.containsKey( name ) )
        {
            Vector v = new Vector();
            mapByCategory_.put( name, v );
        }

        return ( List ) mapByCategory_.get( name );
    }

    public void addConfiguration( String configurationName,
                                  String category,
				  String pattern,
                                  LogTarget target,
                                  Priority priority,
                                  boolean remoteAccessible )
    {

	Entry _newEntry = 
	    new Entry( configurationName, 
		       category, 
		       pattern,
		       target, 
		       priority, 
		       remoteAccessible );

        getCreateList( category ).add( _newEntry );
	mapByName_.put(configurationName, _newEntry);
    }

    public List getListOfConfigurationFor( String category )
    {
        return Collections.unmodifiableList( getCreateList( category ) );
    }

    public Entry getConfiguration(String name) {
	return (Entry)mapByName_.get(name);
    }

    public String[] getConfigurationNames() {
	return (String[]) mapByName_.keySet().toArray (STRING_ARRAY_TEMPLATE );
    }

    public String[] getCategories()
    {
        return ( String[] ) mapByCategory_.keySet().toArray( STRING_ARRAY_TEMPLATE );
    }

}
