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

package org.jacorb.imr;

import org.jacorb.imr.RegistrationPackage.*;
import org.jacorb.imr.AdminPackage.*;

import org.jacorb.orb.*;
import org.jacorb.orb.connection.*;
import org.jacorb.orb.factory.SocketFactory;

import org.jacorb.poa.util.*;

import org.jacorb.util.Environment;
import org.jacorb.util.Debug;
import org.jacorb.util.threadpool.*;

import org.omg.IIOP.*;
import org.omg.PortableServer.*;

import java.io.*;
import java.net.*;

/**
 * This is the main class of the JacORB implementation repository.
 * It keeps track of the registered POAs with lifespan policy
 * PERSISTENT and provides a way for migrating and restarting
 * the POAS servers.
 *
 * @author Nicolas Noffke
 * 
 * $Id$
 */

public class ImplementationRepositoryImpl 
    extends ImplementationRepositoryPOA
{
    private static int default_port = 0;
    private static org.omg.CORBA.ORB orb;

    private File table_file;
    private ServerTable server_table;
    private File table_file_backup;
    private SocketListener listener;

    private int object_activation_retries = 5;
    private int object_activation_sleep = 50;

    private boolean allow_auto_register = false;
    private boolean check_object_liveness = false;

    /**
     * The constructor.
     * It builds up the server table and starts up the SocketListener thread.
     *
     * @param table_file the file containing the serialized server table. Also 
     * used for writing the table to on shutdown. If null, an empty table is created.
     * @param table_backup the file where backups are written to.
     * @param new_table set to true, if an empty server table should be created
     */
    public ImplementationRepositoryImpl(File table_file, 
                                        File table_backup,
					boolean new_table) 
    {
	this.table_file = table_file;
	table_file_backup = table_backup;

	//build up server table
	if( new_table )
        {
	    server_table = new ServerTable();
        }
	else
        {
	    try
            {
		ObjectInputStream _in = new ObjectInputStream(new FileInputStream(table_file));
		server_table = (ServerTable)_in.readObject();

		_in.close();
	    }
            catch (Exception _e)
            {
		Debug.output(Debug.IMR | Debug.INFORMATION, _e);
		server_table = new ServerTable();
	    }
	}
	
	//read in properties from Environment.
	try
        {
	    String _tmp = 
                Environment.getProperty("jacorb.imr.object_activation_retries");
            
            if( _tmp != null )
            {
                object_activation_retries = Integer.parseInt(_tmp);
            }
	}
        catch( NumberFormatException nfe )
        {            
        }
    
	try
        {
	    String _tmp = 
                Environment.getProperty("jacorb.imr.object_activation_sleep");
            
            if( _tmp != null )
            {
                object_activation_sleep = Integer.parseInt(_tmp);
            }
	}
        catch( NumberFormatException e )
        {
        }

        String _tmp = 
            Environment.getProperty("jacorb.imr.allow_auto_register");
            
        if( _tmp != null )
        {
            _tmp = _tmp.toLowerCase();            

            allow_auto_register = "on".equals( _tmp );
        }

        _tmp = 
            Environment.getProperty("jacorb.imr.check_object_liveness");
        
        if( _tmp != null )
        {
            _tmp = _tmp.toLowerCase();            
            
            check_object_liveness = "on".equals( _tmp );
        }
    
	listener = new SocketListener();
    }

    // implementation of org.jacorb.imr.RegistrationOperations interface

    /**
     * This method sets a server down, i.e. not.active. If a request for
     * that server is encountered, the server is tried to be restarted.
     *
     * @param server the servers name.
     * @exception org.jacorb.imr.UnknownServerName No server with name 
     * <code>server</code> has been registered.
     */
    public void set_server_down(String server) 
	throws UnknownServerName {
	Debug.output(Debug.IMR | Debug.INFORMATION,
                     "ImR: server " + server + " is going down... ");

	ImRServerInfo _server = server_table.getServer(server);
	_server.setDown();
    }


    /**
     * This method registers a POA. It has actually two functions:
     * <ul>
     * <li> Register a POA that has not yet been registered. It is the added to the 
     * server table. </li>
     * <li> Reactivating a POA that is not active, but has already an entry
     * in the server table</li> </ul>
     * The reason for using only one method for those two tasks is that it is
     * much more difficult for the ORB, which does the registering, to distinguish
     * between an newly created POA and a restarted one.
     *
     * @param name the POAs name.
     * @param server the logical server name of the server the running in.
     * @param host the POAs host.
     * @param port the POas port.
     * @exception org.jacorb.imr.RegistrationPackage.IllegalPOAName the POAs name is not valid.
     * @exception org.jacorb.imr.RegistrationPackage.DuplicatePOAName an active POA with
     * <code>name</code> is currently registered.
     * @exception org.jacorb.imr.UnknownServerName The server has not been registered.
     */
    public void register_poa(String name, String server, String host, int port) 
	throws IllegalPOAName, DuplicatePOAName, UnknownServerName 
    {
	
	Debug.output(Debug.IMR | Debug.INFORMATION,
                     "ImR: registering poa " + name + " for server: " + 
                     server + " on " + host );
	
        if( allow_auto_register &&
            ! server_table.hasServer( server ))
        {
            try
            {
                register_server( server, "", "" );            
            }
            catch( IllegalServerName isn )
            {
                //ignore
            }
            catch( DuplicateServerName dsn )
            {
                //ignore
            }
        }

        ImRServerInfo _server = server_table.getServer(server);

	ImRPOAInfo _poa = server_table.getPOA(name);

	if (_poa == null)
        {
	    //New POAInfo is to be created
	    _poa = new ImRPOAInfo(name, host, port, _server);
	    _server.addPOA(_poa);
	    server_table.putPOA(name, _poa);
	    Debug.output(Debug.IMR | Debug.INFORMATION,
                         "ImR: new poa registered");

	}
	else 
        {
	    //Existing POA is reactivated
            if ((_poa.active) ||  (! server.equals(_poa.server.name)))
                throw new DuplicatePOAName("POA " + name + 
                                           " has already been registered " +
                                           "for server " + _poa.server.name);
	    
	    _poa.reactivate(host, port);
	    Debug.output(Debug.IMR | Debug.INFORMATION,
                         "ImR: register_poa, reactivated");
	}
    }
 

    /**
     * Register a new host with a server startup daemon.
     * @param host a HostInfo object containing the hosts name and a reference to its
     * ServerStartupDaemon object.
     * 
     * @exception org.jacorb.imr.RegistrationPackage.IllegalHostName <code>name</code> is not valid.
     * @exception org.jacorb.imr.RegistrationPackage.InvalidSSDRef It was impossible to connect 
     * to the daemon.
     */
    public void register_host(HostInfo host) 
	throws IllegalHostName, InvalidSSDRef {
	
	if (host.name == null || host.name.length() == 0)
	    throw new IllegalHostName(host.name);

	try{
	    host.ssd_ref.get_system_load();
	} catch (Exception _e){
	    Debug.output(Debug.IMR | Debug.INFORMATION, _e);
	    throw new InvalidSSDRef();
	}
	
	server_table.putHost(host.name, new ImRHostInfo(host));
    }
    

    /**
     * Get host and port (wrapped inside an ImRInfo object) of this repository.
     * @return the ImRInfo object of this repository.
     */
    public ImRInfo get_imr_info(){
	return new ImRInfo(listener.getAddress(), listener.getPort());
    }
    

    // implementation of org.jacorb.imr.AdminOperations interface

    /**
     * List all hosts currently registered with this repository.
     * It is not guaranteed that the references inside the HostInfo
     * objects are still valid.
     *
     * @return an array containing all known hosts.
     */
    public HostInfo[] list_hosts() {
	return server_table.getHosts();
    }
    
    /**
     * List all registered server. The ServerInfo objects contain also
     * a list of the associated POAs.
     *
     * @return an array containing all registered servers.
     */
    public ServerInfo[] list_servers() {
	return server_table.getServers();
    }

    /**
     * Get the ServerInfo object of a specific server.
     *
     * @param server the servers name.
     * @return the ServerInfo object of the server with name <code>server</code>
     * @exception UnknownServerName the server <code>server</code> has not been registered.
     */
    public ServerInfo get_server_info(String server)
	throws UnknownServerName{
	return server_table.getServer(server).toServerInfo();
    }
   
    /**
     * Register a logical server. The logical server corresponds to a process
     * which has a number of POAs.
     *
     * @param name the servers name.
     * @param command the startup command for this server if it should be restarted
     * on demand. Has to be empty (NOT null) if the server should not be restarted.
     * @param host the host on which the server should be restarted. Should not
     * be null, but is ignored if no startup command is specified.
     *
     * @exception org.jacorb.imr.AdminPackage.IllegalServerName the servers name is not valid.
     * @exception org.jacorb.imr.AdminPackage.DuplicateServerName a server with <code>name</code>
     * has already been registered.
     */
    public void register_server(String name, String command, String host) 
	throws IllegalServerName, DuplicateServerName {
	ImRServerInfo _server = new ImRServerInfo(name, host, command);
	server_table.putServer(name, _server);
	Debug.output(Debug.IMR | Debug.INFORMATION,
                     "ImR: server " + name + " on " + host + " registered");
    }


    /**
     * Remove a logical server from the server table. If a server is removed, all of its POAs
     * are removed as well.
     *
     * @param name the servers name.
     * @exception org.jacorb.imr.UnknownServerName a server with <code>name</code> has not been registered.
     */
    public void unregister_server(String name) throws UnknownServerName {
	ImRServerInfo _server = server_table.getServer(name);
	String[] _poas = _server.getPOANames();

	// remove POAs
	for (int _i = 0; _i < _poas.length; _i++)
	    server_table.removePOA(_poas[_i]);

	server_table.removeServer(name);
	Debug.output(Debug.IMR | Debug.INFORMATION,
                     "ImR: server " + name + " unregistered");
    }

    /**
     * Updates the server with a new command and host. For migrating purposes.
     *
     * @param name the servers name.
     * @param command the new startup command for this server.
     * @param host the new host.
     * @exception org.jacorb.imr.AdminPackage.UnknownServerName a server with <code>name</code>
     * has not been registered.
     */
    public void edit_server(String name, String command, String host) 
	throws UnknownServerName {
	ImRServerInfo _server = server_table.getServer(name);

	_server.command = command;
	_server.host = host;

	Debug.output(Debug.IMR | Debug.INFORMATION,
                     "ImR: server " + name + " edited");
    }


    /**
     * Hold a server. This causes all requests for this server to be delayed
     * until it is released. Holding a server is useful for migrating or
     * maintaining it. There is not timeout set, so requests might be delayed
     * indefinetly (or, at least, until the communication layer protests;-).
     *
     * @param name the servers name.
     * @exception org.jacorb.imr.UnknownServerName a server with <code>name</code> has not been registered.
     */
    public void hold_server(String name) throws UnknownServerName {
	ImRServerInfo _server = server_table.getServer(name);
	_server.holding = true;
    }

    /**
     * Release a server from state "holding".
     *
     * @param name the servers name.
     * @exception org.jacorb.imr.UnknownServerName a server with <code>name</code> has not been registered.
     */
    public void release_server(String name) throws UnknownServerName {
	ImRServerInfo _server = server_table.getServer(name);
	_server.release();
    }
    
    /**
     * Start a server.
     *
     * @param name the servers name.
     * @exception org.jacorb.imr.UnknownServerName a server with <code>name</code> 
     * has not been registered.
     */
    public void start_server(String name) 
        throws UnknownServerName, ServerStartupFailed{
        restartServer(server_table.getServer(name));
    }

    /**
     * Save the server table to a backup file.
     * @exception org.jacorb.imr.AdminPackage.FileOpFailed something went wrong.
     */
    public void save_server_table() throws FileOpFailed {
	save_server_table(table_file_backup);
    }
    
    /**
     * Shut the repository down orderly, i.e. with saving of the server table.
     * The actual shutdown is done in the SocketListener thread because, if
     * done from here, the orb wont shut don correctly because this connection
     * is still active. (See end of SocketListener.run())
     *
     * @param wait wait_for_completion (from ORB.shutdown()). If false, then the ORB
     * is forced down, ignoring any open connection.
     */
    public void shutdown(boolean wait) {
	try{
	    save_server_table(table_file);
	}catch (Exception _e){
	    Debug.output(Debug.IMR | Debug.INFORMATION, _e);
	}

	listener.stopListening(wait);
    }

    /**
     * Remove a host from the servertable. Hosts are removed
     * automatically on server startup, if they can't be accessed.
     *
     * @param name the hosts name.
     * @exception no host with that name known.
     */
    public void unregister_host(String name)
	throws UnknownHostName{
	if (server_table.removeHost(name) == null)
	    throw new UnknownHostName(name);
    }

    /**
     * Convenience method which does the actual serialization.
     *
     * @param save_to the file where to write to.
     * @exception sth went wrong.
     */
    private void save_server_table(File save_to) throws FileOpFailed {
	try{
	    ObjectOutputStream _out = new ObjectOutputStream(new FileOutputStream(save_to));

	    server_table.table_lock.gainExclusiveLock();
	    _out.writeObject(server_table);
	    server_table.table_lock.releaseExclusiveLock();

	    _out.flush();
	    _out.close();
	}catch (Exception _e){
	    Debug.output(Debug.IMR | Debug.INFORMATION, _e);
	    throw new FileOpFailed();
	}
    }


    /**
     * Prints the usage screen and exits.
     */
    public static void usage(){
	System.out.println("Usage: ImplementationRepositoryImpl [Parameter]");
	System.out.println("Parameter:");
	System.out.println("\t -p <port> Port to listen on for requests");
	System.out.println("\t -f <file> Read in server table from this file");
	System.out.println("\t -n Start with empty server table");
	System.out.println("\t -i <iorfile> Place IOR in this file");
	System.out.println("\t -b <backupfile> Put server table in this file");
	System.out.println("\t -a Allow auto-registering of servers");
	System.out.println("\t -h Print this help");
 
	System.exit(0);
    }

    /**
     * The main method. "Parses" the arguments and sets the corresponding attributes up,
     * creates a new ImplementationRepositoryImpl instance and runs the ORB.
     */
    public static void main(String[] args) {
	// evaluate args
	if (args.length > 8)
	    usage();

	String _table_file_str = null;
	boolean _new_table = false;
	String _ior_file_str = null;
	String _backup_file_str = null;

        System.setProperty ("jacorb.implname", "the_ImR");
        System.setProperty ("jacorb.use_imr", "off");

	try{
	    for (int i = 0; i < args.length ; i++){
		switch (args[i].charAt(1)){
		case 'h' : {
		    usage();
		}
		case 'p' : {
		    default_port = Integer.parseInt(args[++i]);
		    break;
		}
		case 'f' : {
		    if (_new_table)
			// -f and -n together not allowed
			usage();
		    _table_file_str = args[++i];
		    break;
		}
		case 'n' : {
		    if (_table_file_str != null)
			usage();
		    _new_table = true;
		    break;
		}
		case 'i' :{
		    _ior_file_str = args[++i];
		    break;
		}
		case 'b' :{
		    _backup_file_str = args[++i];
		    break;
		}
		case 'a' :{
		    Environment.setProperty( "jacorb.imr.allow_auto_register",
                                             "on" );
		    break;
		}
                
		default:
		    usage();
		}
	    }
	}catch (Exception _e){
	    _e.printStackTrace();
	    usage();
	}
	
	// table file not specified, try via property
	if (_table_file_str == null){
	    _table_file_str = Environment.getProperty("jacorb.imr.table_file");

	    if (_table_file_str == null){
		System.out.println("WARNING: No file for the server table specified!");
		System.out.println("Property org.jacorb.imr.table_file or use the -f switch");
		System.out.println("Will create \"table.dat\" in current directory, if necessary");
		_table_file_str = "table.dat";
	    }
	}
	
	File _table_file = new File(_table_file_str);
	
	// try to open table file
	if (! _new_table){
	    if (!_table_file.exists()){ 	
		System.out.println("ERROR: The table file does not exist!");
		System.out.println("Please check " + _table_file.getAbsolutePath());
		System.out.println("Property org.jacorb.imr.table_file or use the -n or -f switch");
		System.exit(-1);
	    }

	    if (_table_file.isDirectory()){
		System.out.println("ERROR: The table file is a directory!");
		System.out.println("Please check " + _table_file.getAbsolutePath());
		System.out.println("Property org.jacorb.imr.table_file or use the -n or -f switch");
		System.exit(-1);
	    }
	
	    if (! _table_file.canRead()){
		System.out.println("ERROR: The table file is not readable!");
		System.out.println("Please check " + _table_file.getAbsolutePath());
		System.exit(-1);
	    }

	    if (! _table_file.canWrite()){
		System.out.println("WARNING: The table file is not writable!");
		System.out.println("Please check " + _table_file.getAbsolutePath());
	    }
	}
	else
	    try{
		//testing the hard way, if the file can be created
		//with jdk1.2, we might try createNewFile()
		FileOutputStream _out = new FileOutputStream(_table_file);
		_out.close();
		_table_file.delete(); //don't leave empty files lying around
	    
	    }catch (Exception _e){
		System.out.println("WARNING: Unable to create table file!");
		System.out.println("Please check " + _table_file.getAbsolutePath());
	    }

	// no ior file specified, try via property
	if (_ior_file_str == null){
	    _ior_file_str = Environment.getProperty("jacorb.imr.ior_file");
	    if (_ior_file_str == null){
		System.out.println("ERROR: Please specify a file for the IOR string!");
		System.out.println("Property org.jacorb.imr.ior_file or use the -i switch");

		System.exit(-1);
	    }
	}

	//set up server table backup file
	if (_backup_file_str == null){
	    _backup_file_str = Environment.getProperty("jacorb.imr.backup_file");      
	    if (_backup_file_str == null){
		System.out.println("WARNING: No backup file specified!\n" +
				   "Will create \"backup.dat\" in current directory, if necessary");
		_backup_file_str = "backup.dat";
	    }
	}

	File _backup_file = new File(_backup_file_str);
	try{
	    if ( _backup_file.exists()){
		if (! _backup_file.canWrite()){
		    System.out.println("WARNING: The backup file exists, but is not writable!");
		    System.out.println("Please check " + _backup_file.getAbsolutePath());
		}
		else{
		    System.out.println("WARNING: The backup file already exists and might get overwritten!");
		    System.out.println("Please check " + _backup_file.getAbsolutePath());
		}
 	    }
	    else{
		//testing the hard way, if the file can be created
		//with jdk1.2, we might try createNewFile()
		FileOutputStream _out = new FileOutputStream(_backup_file);
		_out.close();
		_backup_file.delete(); //don't leave empty files lying around
	    }
	}catch (Exception _e){
	    System.out.println("WARNING: The backup file is not accessible!");
	    System.out.println("Please check " + _backup_file.getAbsolutePath());
	}

	orb = org.omg.CORBA.ORB.init( args, null );

	//Write IOR to file
	try{	  
	    POA root_poa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
	    root_poa.the_POAManager().activate();

            org.omg.CORBA.Policy[] policies = new org.omg.CORBA.Policy[2];

	    policies[0] = 
                root_poa.create_lifespan_policy(LifespanPolicyValue.PERSISTENT);
	    policies[1] = 
                root_poa.create_id_assignment_policy(IdAssignmentPolicyValue.USER_ID);

	    POA imr_poa = root_poa.create_POA( "ImRPOA", 
                                               root_poa.the_POAManager(), 
                                               policies );

	    for (int i=0; i<policies.length; i++) 
            {
		policies[i].destroy();			
            }

            byte[] id = "ImR".getBytes();

	    ImplementationRepositoryImpl _imr = new ImplementationRepositoryImpl
                (_table_file, _backup_file, _new_table);

            imr_poa.activate_object_with_id( id, _imr );
	
	    PrintWriter _out = new PrintWriter
                (new FileOutputStream(new File(_ior_file_str)));

	    _out.println(orb.object_to_string(imr_poa.servant_to_reference(_imr)));
	    _out.flush();
	    _out.close();
	}
        catch (Exception _e)
        {
            Debug.output(Debug.IMR | Debug.INFORMATION, _e);

	    System.out.println("ERROR: Failed to write IOR to file.\nPlease check the path.");
	    System.out.println("Property org.jacorb.imr.ior_file or -i <file> switch");
	    System.exit(-1);
	}

	orb.run();
    }

    private void restartServer(ImRServerInfo server)
        throws ServerStartupFailed
    {
        // server might be holding
        server.awaitRelease();

        if(! server.active )
        {
            Debug.output(Debug.IMR | Debug.INFORMATION, 
                         "ImR: server " + server.name + " is down");

            //server is down;
            if (server.command.length() == 0){
                //server can't be restartet, send exception
                throw new ServerStartupFailed("Server " + server.name + 
                                              " can't be restarted because" +
                                              " of missing startup command");
            }
            else{
                // we have to synchronize here to avoid a server to be
                // restarted multiple times by requests that are
                // received in the gap between the first try to
                // restart and the reactivation of the POAs.
                // restarting is set back to false when the first POA
                // is reactivated and the server goes back to active
                // (see ImRPOAInfo.reactivate()).
                if (server.shouldBeRestarted()){
                    try{
                        // If there is no SSD for the host, we get an
                        // NullPointerException.  In a further
                        // version, we might choose another random
                        // SSD.
                        ImRHostInfo _host = server_table.getHost(server.host);

                        if( _host == null )
                        {
                            throw new ServerStartupFailed( "Unknown host: >>" +
                                                           server.host + "<<" );
                        }

                        Debug.output(Debug.IMR | Debug.INFORMATION, 
                                     "ImR: will restart " + server.name);

                        _host.startServer(server.command, orb);
                    } 
                    catch (ServerStartupFailed ssf)
                    {
                        server.setNotRestarting();

                        throw ssf;	
                    } 
                    catch (Exception _e)
                    {
                        server.setNotRestarting();

                        Debug.output(Debug.IMR | Debug.INFORMATION, _e);
			    
                        // sth wrong with daemon, remove from table
                        server_table.removeHost(server.host);
			    
                        throw new ServerStartupFailed("Failed to connect to host!");
                    }			
                }
                else
                    Debug.output(Debug.IMR | Debug.INFORMATION, 
                                 "ImR: somebody else is restarting " + 
                                 server.name);
		      
            }
        }
        else
            Debug.output(Debug.IMR | Debug.INFORMATION, 
                         "ImR: server " + server.name + " is active");
    }
    
    /**
     * Inner class SocketListener, responsible for accepting
     * connection requests.  *Very* close to inner class Listener in
     * orb/BasicAdapter.java.  
     * <br> When a connection is accepted a
     * new RequestReceptor thread is started.  
     */
    private class SocketListener 
	extends Thread 
    {
	private ServerSocket server_socket;
	private int port = 0;
	private String address;
	private int timeout = 0; 
	private boolean run = true;
	private boolean wait = false;

        private MessageReceptorPool receptor_pool = null;
        private RequestListener request_listener = null;
        private ReplyListener reply_listener = null;

	/**
	 * The constructor. It sets up the ServerSocket and starts the thread.
	 */
	public SocketListener()
        {
	    try 
            {
		server_socket = 
                    new ServerSocket( default_port );

		address = InetAddress.getLocalHost().toString();

		if( address.indexOf("/") > 0 )
		    address = address.substring(address.indexOf("/") + 1);
		
		port = server_socket.getLocalPort();

		Debug.output(Debug.IMR | Debug.INFORMATION,
                             "ImR Listener at " + port + ", " + address );
		
                String s = 
                    Environment.getProperty( "jacorb.imr.connection_timeout",
                                             "2000" ); //default: 2 secs

                try
                {
                    timeout = Integer.parseInt( s );
                }
                catch( NumberFormatException nfe )
                {
                    Debug.output( Debug.IMR | Debug.IMPORTANT,
                                  "ERROR: Unable to build timeout int from string >>" + 
                                  s + "<<" );
                    Debug.output( Debug.IMR | Debug.IMPORTANT,
                                  "Please check property \"jacorb.imr.connection_timeout\"" );
                }	                  
	    } 
	    catch (Exception e)
            {
		Debug.output(Debug.IMR | Debug.IMPORTANT, e);
		Debug.output(Debug.IMR | Debug.IMPORTANT, 
                             "Listener: Couldn't init");

		System.exit(1);
	    }

	    setDaemon(true);

            receptor_pool = MessageReceptorPool.getInstance();
            request_listener = new ImRRequestListener();
            reply_listener = new NoBiDirServerReplyListener();

	    start();
	}
	   
	/**
	 * Get the port this SocketListener is listening on.
	 *
	 * @return the port
	 */ 
	public int getPort()
	{
	    Debug.output(Debug.IMR | Debug.INFORMATION,
                         "ImR Listener at " + port + ", " + address );

	    return port;
	}

	/**
	 * The internet address of the Socket this thread is listening on.
	 *
	 * @return the address of the socket.
	 */
	public String getAddress()
	{
	    return address;
	}

	/**
	 * Set the connection timeout.
	 *
	 * @param timeout the timeout.
	 */
	public void setTimeout( int timeout )
	{
	    this.timeout = timeout;
	}
     
	/**
	 * The threads main event loop. Listenes on the socket
	 * and starts new RequestReceptor threads on accepting.
	 * <br> On termination does the actual shutdown of the 
	 * repository.
	 */
	public void run() 
	{
	    Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
	    while( run )
            {
		try
                {
                    Socket socket = server_socket.accept();
                    socket.setSoTimeout( timeout );
                    
                    Transport transport = 
                        new Server_TCP_IP_Transport( socket, false ); //no ssl

                    GIOPConnection connection = 
                        new GIOPConnection( transport,
                                            request_listener,
                                            reply_listener );

                    receptor_pool.connectionCreated( connection );
		}
                catch (Exception _e)
                {
		    // when finishing, we do a close() on
		    // server_socket from "outside" and that causes an
		    // exception here. But since we wanted it this
		    // way, we don't display the Exception to avoid
		    // confusing users.
		    if (run)
			Debug.output(Debug.IMR | Debug.INFORMATION, _e);
		}
	    }
	    
	    // doing the actual shutdown of the implementation
	    // repository here
	    orb.shutdown(wait);
	    System.exit(0);	    
	}

	/**
	 * Causes the event loop to terminate by closing the ServerSocket.
	 *
	 * @param wait for ORB.shutdown().
	 */
	public void stopListening(boolean wait)
        {
	    run = false;
	    this.wait = wait;

	    try
            {
		server_socket.close();
	    }
            catch (Exception _e)
            {
		Debug.output(Debug.IMR | Debug.INFORMATION, _e);
	    }	   
	}
    }


    /** 
     * Inner class ImRRequestListener. Receives messages.
     */
    private class ImRRequestListener 
        implements RequestListener 
    {
	public ImRRequestListener()
        {
	}

	/**
	 *	receive and dispatch requests 
	 */

        public void requestReceived( byte[] request,
                                     GIOPConnection connection )
        {
            RequestInputStream in = new RequestInputStream( orb, request );

            replyNewLocation( in.req_hdr.target.object_key(), 
                              in.req_hdr.request_id,
                              in.getGIOPMinor(),
                              connection );
        }
        
        public void locateRequestReceived( byte[] request,
                                           GIOPConnection connection )
        {
            LocateRequestInputStream in = 
                new LocateRequestInputStream( orb, request );

            replyNewLocation( in.req_hdr.target.object_key(),  
                              in.req_hdr.request_id,
                              in.getGIOPMinor(), 
                              connection );
        }        

        public void cancelRequestReceived( byte[] request,
                                           GIOPConnection connection )
        {
            //ignore
        }
        
        public void fragmentReceived( byte[] fragment,
                                      GIOPConnection connection )
        {
            //ignore
        }
        
        public void connectionClosed()
        {

        }

	/**
	 * The actual core method of the implementation repository.
	 * Causes servers to start, looks up new POA locations in
	 * the server table.
	 */
	private void replyNewLocation( byte[] object_key,
                                       int request_id,
                                       int giop_minor,
                                       GIOPConnection connection )
        {
	    String _poa_name = 
                POAUtil.extractImplName( object_key ) + '/' + 
                POAUtil.extractPOAName( object_key );

	    // look up POA in table
	    ImRPOAInfo _poa = server_table.getPOA( _poa_name );
	    if (_poa == null)
            {
		sendSysException( 
                   new org.omg.CORBA.OBJECT_NOT_EXIST( "POA " + 
                                                       _poa_name + 
                                                       " unknown" ),
                   connection,
                   request_id,
                   giop_minor );
		return;
	    }

	    // get server of POA
	    ImRServerInfo _server = _poa.server;

	    Debug.output( Debug.IMR | Debug.INFORMATION, 
                          "ImR: Looking up: " + _server.name );

	    try
            {
                restartServer( _server );
	    }
            catch( ServerStartupFailed ssf )
            {
                sendSysException( new org.omg.CORBA.TRANSIENT(ssf.reason),
                                  connection,
                                  request_id,
                                  giop_minor );
                return;
	    }
	    
	    // POA might not be active
	    boolean _old_poa_state = _poa.active;

	    // wait for POA to be reregistered.
	    if( ! _poa.awaitActivation() )
            {
		// timeout reached
		sendSysException( new org.omg.CORBA.TRANSIENT("Timeout exceeded"),
                                  connection,
                                  request_id,
                                  giop_minor );
		return;
	    }
            
	    ReplyOutputStream out = 
                new ReplyOutputStream( request_id,
                                       org.omg.GIOP.ReplyStatusType_1_2.LOCATION_FORWARD,
                                       giop_minor );

	    // The typecode is for org.omg.CORBA.Object, but avoiding 
            // creation of new ObjectHolder Instance.
	    org.omg.IOP.IOR _ior = 
                ParsedIOR.createObjectIOR( _poa.host,
                                           (short) _poa.port,
                                           object_key,
                                           giop_minor );

	    if( !_old_poa_state )
            {
		// if POA has been reactivated, we have to wait for
		// the requested object to become ready again. This is
		// for avoiding clients to get confused by
		// OBJECT_NOT_EXIST exceptions which they might get
		// when trying to contact the server too early.

		org.omg.CORBA.Object _object = 
                    orb.string_to_object(
                        (new ParsedIOR( _ior )).getIORString());

		// Sort of busy waiting here, no other way possible
		for( int _i = 0; _i < object_activation_retries; _i++ )
                {
		    try
                    {
			Thread.sleep( object_activation_sleep );

			// This will usually throw an OBJECT_NOT_EXIST
			if( ! _object._non_existent() ) // "CORBA ping"
                        {
			    break; 
                        }
		    }
                    catch(Exception _e)
                    {
			Debug.output(Debug.IMR | Debug.DEBUG1, _e);
		    }
		}		
	    }
            
            //test, if the object is alive
            if( check_object_liveness )
            {
                try
                {
                    org.omg.CORBA.Object _object = 
                        orb.string_to_object(
                            (new ParsedIOR( _ior )).getIORString());
                    
                    if( _object._non_existent() )
                    {

                        sendSysException(new org.omg.CORBA.TRANSIENT("object not reachable"),
                                         connection,
                                         request_id,
                                         giop_minor );
                        return;
                    }
                }
                catch( Exception e )
                {
                    //TODO: Also set server to "down"?
                    sendSysException(new org.omg.CORBA.TRANSIENT("object not reachable"),
                                     connection,
                                     request_id,
                                     giop_minor );
                    return;                
                }
            }

	    try
            {
		// write new location to stream
		out.write_IOR(_ior);

		Debug.output( Debug.IMR | Debug.INFORMATION,
                              "ImR: Sending location forward for " + 
                              _server.name );

		connection.sendMessage( out );
	    }
            catch( IOException _e )
            {
		Debug.output(Debug.IMR | Debug.INFORMATION, _e);
		sendSysException( new org.omg.CORBA.UNKNOWN(_e.toString()),
                                  connection,
                                  request_id,
                                  giop_minor );
	    }
	}
    
	/**
	 * Convenience method for sending a CORBA System Exception back to
	 * the client.
	 *
	 * @param the exception to send back.
	 */
	private void sendSysException( org.omg.CORBA.SystemException sys_ex,
                                       GIOPConnection connection,
                                       int request_id,
                                       int giop_minor )
        {
	    ReplyOutputStream out = 
                new ReplyOutputStream( request_id,
                                       org.omg.GIOP.ReplyStatusType_1_2.SYSTEM_EXCEPTION,
                                      giop_minor );
	    
	    SystemExceptionHelper.write( out, sys_ex );

	    try
            {
		connection.sendMessage( out );
	    }
            catch( IOException _e )
            {
		Debug.output(Debug.IMR | Debug.INFORMATION, _e);
	    }
	}
    }

} // ImplementationRepositoryImpl
