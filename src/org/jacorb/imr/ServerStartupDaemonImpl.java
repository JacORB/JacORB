package org.jacorb.imr;

/**
 * This class is used to start servers on (from the view of the repository)
 * remote hosts. It has a thread for forwarding output of started servers.
 *
 * @author Nicolas Noffke
 * 
 * $Id$
 *
 */

import org.jacorb.orb.ORB;
import org.jacorb.util.*;
import org.jacorb.util.threadpool.*;

import java.lang.*;
import java.net.*;
import java.io.*;

import org.omg.PortableServer.*;

public class ServerStartupDaemonImpl 
    extends org.jacorb.imr.ServerStartupDaemonPOA 
{
    private static ORB orb = null;
    private static final String out_prefix = ">> ";

    private ThreadPool stdout_pool = null;
    private ThreadPool stderr_pool = null;

    /**
     * The constructor. It registers this daemon at the repository.
     *
     * @exception Exception any exception that is thrown inside is propagated upwards.
     */
    public ServerStartupDaemonImpl() 
	throws Exception
    {
	Registration _registration = null;
        
	_registration = 
            RegistrationHelper.narrow( orb.resolve_initial_references("ImplementationRepository"));
	if( _registration == null )
	    throw new java.lang.Error("ImR not found");

	_this_object( orb );

	HostInfo _me = new HostInfo(InetAddress.getLocalHost().getHostName(),_this(),
				    orb.object_to_string(_this()));

	_registration.register_host(_me);
        
        stdout_pool = new ThreadPool( new OutputForwarderFactory( new InputStreamSelector(){
                    public InputStream getInputStream( Process p )
                    {
                        return p.getInputStream();
                    }
                }),
                                      100, //max threads
                                      10 );//max idle threads

        stderr_pool = new ThreadPool( new OutputForwarderFactory( new InputStreamSelector(){
                    public InputStream getInputStream( Process p )
                    {
                        return p.getErrorStream();
                    }
                }),
                                      100, //max threads
                                      10 );//max idle threads
    }

    /**
     * NOT IMPLEMENTED, but currently used for "pinging" purposes.
     * @return 0 always
     */

    public int get_system_load() 
    {
	// Dummy method, not supported yet.
	return 0;
    }
    
    /**
     * This method starts a server on this host as specified by 'command'.
     *
     * @param command The server startup command, i.e. the servers class name and
     * parameters for its main method. The interpreter is inserted automatically.
     *
     * @exception org.jacorb.imr.ServerStartupDaemonPackage.ServerStartupFailed Runtime.exec 
     * failed to execute the command.
     */

    public void start_server(String command) 
        throws ServerStartupFailed 
    {
	try
        {
	    Debug.output(Debug.IMR | Debug.INFORMATION, 
                         "Starting: " + command );

	    Process _server = Runtime.getRuntime().exec( command );

	    stdout_pool.putJob(_server);
	    stderr_pool.putJob(_server);
	}
        catch (Exception _e)
        {
	    Debug.output(Debug.IMR | Debug.INFORMATION, _e);
	    throw new ServerStartupFailed( _e.toString() );
	}
    }

    /**
     * main method. Creates a new ServerStartupDaemonImpl instance and runs the orb.
     **/  
    public static void main( String[] args )
    {
	try
	{
	    orb = (org.jacorb.orb.ORB) ORB.init( args, null );	
	    POA poa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));

	    poa.the_POAManager().activate();

	    ServerStartupDaemonImpl _ssd = new ServerStartupDaemonImpl();
	    
	    orb.run();
	}
        catch( Exception _e )
        {
	    _e.printStackTrace();
	}

	System.exit(0);
    } 
    
    /**
     * Inner class used to forward output of servers, since that would be 
     * invisible otherwise.
     */
    private class OutputForwarder 
        implements Consumer
    {
        /** 
         * prefix to help distinguish between output of a 
         * started server and output of this SSD 
         */
        private InputStreamSelector selector = null;

	public OutputForwarder( InputStreamSelector selector )
        {
            this.selector = selector;
	}

	public void doWork( Object job )
        {
            Process p = (Process) job;

	    BufferedReader _in = new BufferedReader(new InputStreamReader(selector.getInputStream( p )));
	    String _line = null;
	                           
	    try
            {
		// If we get null from readLine() we assume that the process has exited.
		// Unfortunately there is no exception thrown when trying to read from
		// a dead processes output stream.
		while((_line = _in.readLine()) != null)
                {
		    System.out.println(out_prefix + _line);
                }

		 _in.close();
	    }
            catch( Exception _e )
            {
		_e.printStackTrace();
	    }
	    
	    Debug.output( Debug.IMR | Debug.INFORMATION, 
                         "A server process exited" );
	}
    }//OutputForwarder

    private interface InputStreamSelector
    {
        public InputStream getInputStream( Process p );
    }

    private class OutputForwarderFactory
        implements ConsumerFactory
    {
        private InputStreamSelector selector = null;

        public OutputForwarderFactory( InputStreamSelector selector )
        {
            this.selector = selector;
        }

        public Consumer create()
        {
            return new OutputForwarder( selector );
        }
    }
} // ServerStartupDaemonImpl








