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

package org.jacorb.imr;

import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.configuration.*;

import org.omg.GIOP.*;

import org.jacorb.imr.RegistrationPackage.*;
import org.jacorb.imr.AdminPackage.*;

import org.jacorb.orb.*;
import org.jacorb.orb.giop.*;
import org.jacorb.orb.iiop.*;

import org.jacorb.poa.util.POAUtil;


import org.omg.PortableServer.*;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.InetAddress;
import java.lang.reflect.Method;

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
    /**
     * <code>default_port</code> is the port number for the IMR endpoint.
     */
    private static int default_port;

    /**
     * <code>default_host</code> is the host name for the IMR. It defaults to
     * localhost.
     */
    private static String default_host;

    /**
     * <code>orb</code> is the ORB instance for the IMR.
     */
    private static org.omg.CORBA.ORB orb;

    private static org.jacorb.config.Configuration configuration = null;

    /** the specific logger for this component */
    private static Logger logger = null;


    private File table_file;
    private ServerTable server_table;
    private File table_file_backup;
    private SocketListener listener;

    private int object_activation_retries = 5;
    private int object_activation_sleep = 50;

    private boolean allow_auto_register = false;
    private boolean check_object_liveness = false;

    private WriteThread wt;
    private boolean updatePending;
    private Shutdown shutdownThread;

    public static void configure(Configuration myConfiguration)
        throws ConfigurationException
    {
        configuration = (org.jacorb.config.Configuration)myConfiguration;
        logger = configuration.getNamedLogger("jacorb.imr");

        default_port = 
            configuration.getAttributeAsInteger("jacorb.imr.endpoint_port_number",0);
        port = 
            configuration.getAttributeAsInteger("OAPort", 0);

    }


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

        try
        {
            if (new_table)
            {
                server_table = new ServerTable ();
                save_server_table (table_file);
            }
            else
            {
                try
                {
                    ObjectInputStream _in = new ObjectInputStream (new FileInputStream (table_file));
                    server_table = (ServerTable)_in.readObject();
                    _in.close();
                }
                catch (Exception ex)
                {
                    Debug.output (4, ex);
                    server_table = new ServerTable ();
                    save_server_table (table_file);
                }
            }
        }
        catch (FileOpFailed ex)
        {
            Debug.output (4, ex);
        }

        shutdownThread = new Shutdown ();
        shutdownThread.setDaemon (true);
        shutdownThread.setName ("Shutdown Thread");
        addShutdownHook (shutdownThread);

        wt = new WriteThread ();
        wt.setName ("IMR Write Thread");
        wt.setDaemon (true);
        wt.start ();

        // Read in properties from Environment.
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
                (Environment.getProperty("jacorb.imr.object_activation_sleep"));

            if( _tmp != null )
            {
                object_activation_sleep = Integer.parseInt(_tmp);
            }
        }
        catch( NumberFormatException e )
        {
        }

        String _tmp =
            (Environment.getProperty("jacorb.imr.allow_auto_register"));

        if( _tmp != null )
        {
            _tmp = _tmp.toLowerCase();

            allow_auto_register = "on".equals( _tmp );
        }

        _tmp =
            (Environment.getProperty("jacorb.imr.check_object_liveness"));

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
        throws UnknownServerName
    {
        Debug.output(4, "ImR: server " + server + " is going down... ");

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
        ImRServerInfo             _server      = null;
        ImRPOAInfo                _poa         = null;
        boolean                   remap        = false;

        updatePending = true;

        Debug.output(4, "ImR: registering poa " + name + " for server: " +
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

        _server = server_table.getServer(server);
        _poa = server_table.getPOA(name);

        if (_poa == null)
        {
            //New POAInfo is to be created
            _poa = new ImRPOAInfo(name, host, port, _server);
            _server.addPOA(_poa);
            server_table.putPOA(name, _poa);
            Debug.output(4, "ImR: new poa registered");
        }
        else
        {
            // Existing POA is reactivated

            // Need to check whether the old server is alive. if it is then
            // throw an exception otherwise we can remap the currently
            // registered name.
            if ((_poa.active) ||  (! server.equals(_poa.server.name)))
            {
                byte[] first = _poa.name.getBytes ();
                byte[] id = new byte [ first.length + 1];
                System.arraycopy (first, 0, id, 0, first.length);
                id[first.length] = org.jacorb.poa.POAConstants.OBJECT_KEY_SEP_BYTE;

                // If host and port are the same then it must be a replacement as
                // we could not have got to here if the original was running - we
                // would have got a socket exception.
                if (_poa.host.equals (host) && _poa.port == port)
                {
                    remap = true;
                }
                else
                {
                    // Otherwise try a ping
                    remap = ! (checkServerActive (_poa.host, _poa.port, id));
                }

                if (remap == false)
                {
                    throw new DuplicatePOAName
                    (
                        "POA " + name +
                        " has already been registered " +
                        "for server " + _poa.server.name
                    );
                }
                else
                {
                    Debug.output(4, "ImR: Remapping server/port");
                }
            }

            _poa.reactivate(host, port);
            Debug.output(4,"ImR: register_poa, reactivated");
        }
        try
        {
            synchronized (wt)
            {
                wt.notify ();
            }
        }
        catch (IllegalMonitorStateException e)
        {
            Debug.output(4, e);
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
        throws IllegalHostName, InvalidSSDRef
    {

        if (host.name == null || host.name.length() == 0)
            throw new IllegalHostName(host.name);

        try
        {
            host.ssd_ref.get_system_load();
        }
        catch (Exception _e)
        {
            Debug.output(4, _e);
            throw new InvalidSSDRef();
        }
        updatePending = true;

        server_table.putHost(host.name, new ImRHostInfo(host));

        try
        {
            synchronized (wt)
            {
                wt.notify ();
            }
        }
        catch (IllegalMonitorStateException e)
        {
            Debug.output(4, e);
        }
    }


    /**
     * Get host and port (wrapped inside an ImRInfo object) of this repository.
     * @return the ImRInfo object of this repository.
     */

    public ImRInfo get_imr_info()
    {
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
    public HostInfo[] list_hosts()
    {
        return server_table.getHosts();
    }

    /**
     * List all registered server. The ServerInfo objects contain also
     * a list of the associated POAs.
     *
     * @return an array containing all registered servers.
     */
    public ServerInfo[] list_servers()
    {
        ServerInfo [] servers;

        if (check_object_liveness)
        {
            Debug.output
                (4, "ImR: Checking servers");

            servers = server_table.getServers();

            for (int k=0; k<servers.length; k++)
            {
                if (servers[k].active && servers[k].poas.length > 0)
                {
                    byte[] first = servers[k].poas[0].name.getBytes ();
                    byte[] id = new byte [ first.length + 1];
                    System.arraycopy (first, 0, id, 0, first.length);
                    id[first.length] = org.jacorb.poa.POAConstants.OBJECT_KEY_SEP_BYTE;

                    if ( ! checkServerActive
                         (servers[k].poas[0].host, servers[k].poas[0].port, id))
                    {
                        try
                        {
                            Debug.output
                                (4, "ImR: Setting server " + servers[k].name + " down");

                            // Server is not active so set it down
                            server_table.getServer(servers[k].name).setDown();

                            // Save retrieving the list again.
                            servers[k].active = false;
                        }
                        catch (UnknownServerName e)
                        {
                            Debug.output(4, "ImR: Internal error - unknown server " + servers[k].name );
                        }
                    }
                }
            }
        }
        else
        {
            servers = server_table.getServers();
        }

        return servers;
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
        throws IllegalServerName, DuplicateServerName
    {
        updatePending = true;

        ImRServerInfo _server = new ImRServerInfo(name, host, command);
        server_table.putServer(name, _server);

        Debug.output(4,"ImR: server " + name + " on " + host + " registered");
        try
        {
            synchronized (wt)
            {
                wt.notify ();
            }
        }
        catch (IllegalMonitorStateException e)
        {
            Debug.output(4, e);
        }
    }


    /**
     * Remove a logical server from the server table. If a server is removed, all of its POAs
     * are removed as well.
     *
     * @param name the servers name.
     * @exception org.jacorb.imr.UnknownServerName a server with <code>name</code> has not been registered.
     */
    public void unregister_server(String name) throws UnknownServerName
    {
        updatePending = true;

        ImRServerInfo _server = server_table.getServer(name);
        String[] _poas = _server.getPOANames();

        // remove POAs
        for (int _i = 0; _i < _poas.length; _i++)
            server_table.removePOA(_poas[_i]);

        server_table.removeServer(name);
        Debug.output(4,"ImR: server " + name + " unregistered");
        try
        {
            synchronized (wt)
            {
                wt.notify ();
            }
        }
        catch (IllegalMonitorStateException e)
        {
            Debug.output(4, e);
        }
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
        throws UnknownServerName
    {
        updatePending = true;

        ImRServerInfo _server = server_table.getServer(name);

        _server.command = command;
        _server.host = host;

        Debug.output(4,"ImR: server " + name + " edited");
        try
        {
            synchronized (wt)
            {
                wt.notify ();
            }
        }
        catch (IllegalMonitorStateException e)
        {
            Debug.output(4, e);
        }
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
    public void hold_server(String name)
        throws UnknownServerName
    {
        ImRServerInfo _server = server_table.getServer(name);
        _server.holding = true;
    }

    /**
     * Release a server from state "holding".
     *
     * @param name the servers name.
     * @exception org.jacorb.imr.UnknownServerName a server with <code>name</code> has not been registered.
     */
    public void release_server(String name)
        throws UnknownServerName
    {
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
        throws UnknownServerName, ServerStartupFailed
    {
        restartServer(server_table.getServer(name));
    }

    /**
     * Save the server table to a backup file.
     * @exception org.jacorb.imr.AdminPackage.FileOpFailed something went wrong.
     */
    public void save_server_table()
        throws FileOpFailed
    {
        if (table_file_backup != null)
        {
            save_server_table(table_file_backup);
        }
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
    public void shutdown(boolean wait)
    {
        synchronized (wt)
        {
            wt.shutdown ();
            wt.notify ();
        }
        if (listener != null)
        {
            listener.stopListening (wait);
            try
            {
                synchronized (listener)
                {
                    // Wait at most 5 seconds for the listener to shutdown.
                    listener.join (5000);
                }
            }
            catch (InterruptedException e)
            {
                Debug.output (4, e);
            }
        }
        try
        {
            save_server_table ();
        }
        catch (FileOpFailed f)
        {
            Debug.output (4, "ImR: Failed to save backup table.");
        }
        Debug.output (4, "ImR: Finished shutting down");
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
        }
        catch (Exception _e)
        {
            Debug.output(4, _e);
            throw new FileOpFailed();
        }
        updatePending = false;
    }


    /**
     * Prints the usage screen and exits.
     */
    public static void usage ()
    {
        System.out.println("Usage: ImplementationRepositoryImpl [Parameter]");
        System.out.println("Parameter:");
        System.out.println("\t -p <port> Port to listen on for requests");
        System.out.println("\t[ -f <file> Read in server table from this file");
        System.out.println("\t| -n Start with empty server table ]");
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
    public static void main(String[] args) 
    {
        // translate any properties set on the commandline but after the 
        // class name to a properties
        java.util.Properties argProps = ObjectUtil.argsToProps( args );
        
        java.util.Properties props = new java.util.Properties();
        props.setProperty("jacorb.implname", "the_ImR");
        props.setProperty("jacorb.use_imr", "off");

        String _table_file_str = null;
        boolean _new_table = false;
        String _ior_file_str = null;
        String _backup_file_str = null;
        String port = null;
        File _backup_file = null;


        default_host = Environment.getProperty ("jacorb.imr.host");

        if (default_host != null && default_host.length() > 0)
        {
            Debug.output(4, "ImR: Using host address " + default_host);

            if( props == null )
            {
                props = new java.util.Properties ();
            }
            props.setProperty ("OAIAddr", default_host);
        }

        // table file not specified, try via property
        if (_table_file_str == null){
            _table_file_str = Environment.getProperty("jacorb.imr.table_file");

            if (_table_file_str == null)
            {
                Debug.output( 1, "WARNING: No file for the server table specified! Configure the property jacorb.imr.table_file or use the -f switch");
                Debug.output( 1, "Will create \"table.dat\" in current directory, if necessary");
                _table_file_str = "table.dat";
            }
        }

        File _table_file = new File(_table_file_str);

        // try to open table file
        if ( ! _table_file.exists ())
        {
            _new_table = true;
            Debug.output( 1, "Table file " + _table_file_str + " does not exist - autocreating it.");
            try
            {
                _table_file.createNewFile ();
            }
            catch (IOException ex)
            {
                Debug.output (4, ex);
                System.exit (-1);
            }
        }
        else
        {
            if (_table_file.isDirectory ())
            {
                Debug.output(1, "ERROR: The table file is a directory! Please check " + _table_file.getAbsolutePath());
                System.exit (-1);
            }
            if (! _table_file.canRead())
            {
                Debug.output(1, "ERROR: The table file is not readable! Please check " + _table_file.getAbsolutePath());
                System.exit(-1);
            }
            if (! _table_file.canWrite())
            {
                Debug.output(1, "WARNING: The table file is not writable! Please check " + _table_file.getAbsolutePath());
                System.exit(-1);
            }
        }

        // no ior file specified, try via property
        if (_ior_file_str == null){
            _ior_file_str = Environment.getProperty("jacorb.imr.ior_file");
            if (_ior_file_str == null){
                System.out.println("ERROR: Please specify a file for the IOR string!");
                System.out.println("Property jacorb.imr.ior_file or use the -i switch");

                System.exit(-1);
            }
        }

        //set up server table backup file
        if (_backup_file_str == null || _backup_file_str.length() == 0)
        {
            _backup_file_str = Environment.getProperty("jacorb.imr.backup_file");
            if (_backup_file_str == null || _backup_file_str.length() == 0)
            {
                Debug.output( 1, "WARNING: No backup file specified!. No backup file will be created.");
            }
        }
        if (_backup_file_str != null)
        {
            _backup_file = new File(_backup_file_str);

            // try to open backup file
            if ( ! _backup_file.exists ())
            {
                _new_table = true;
                Debug.output( 1, "Backup file " + _backup_file_str + " does not exist - autocreating it.");
                try
                {
                    _backup_file.createNewFile ();
                }
                catch (IOException ex)
                {
                    Debug.output (4, ex);
                    System.exit (-1);
                }
            }
            else
            {
                if (_backup_file.isDirectory ())
                {
                    Debug.output( 1, "ERROR: The backup file is a directory! Please check " + _backup_file.getAbsolutePath());
                    System.exit (-1);
                }
                if (! _backup_file.canRead())
                {
                    Debug.output( 1, "ERROR: The backup file is not readable! Please check " + _backup_file.getAbsolutePath());
                    System.exit(-1);
                }
                if (! _backup_file.canWrite())
                {
                    Debug.output( 1, "WARNING: The backup file is not writable! Please check " + _backup_file.getAbsolutePath());
                    System.exit(-1);
                }
            }
        }

        orb = org.omg.CORBA.ORB.init( args, props );

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
            Debug.output(4, _e);

            Debug.output( 1, "ERROR: Failed to write IOR to file.\nPlease check the path." );
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
            Debug.output(4,"ImR: server " + server.name + " is down");

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

                        Debug.output(4,"ImR: will restart " + server.name);

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

                        Debug.output(4, _e);

                        // sth wrong with daemon, remove from table
                        server_table.removeHost(server.host);

                        throw new ServerStartupFailed("Failed to connect to host!");
                    }
                }
                else
                    Debug.output(4,"ImR: somebody else is restarting " +
                                 server.name);

            }
        }
        else
            Debug.output(4,"ImR: server " + server.name + " is active");
    }


    // Shutdown hook methods are done via reflection as these were
    // not supported prior to the JDK 1.3.

    private void addShutdownHook (Thread thread)
    {
        Method method = getHookMethod ("addShutdownHook");

        if (method != null)
        {
            invokeHookMethod (method, thread);
        }
    }

    private Method getHookMethod (String name)
    {
        Method method = null;
        Class[] params = new Class[1];

        params[0] = Thread.class;
        try
        {
            method = Runtime.class.getMethod (name, params);
        }
        catch (Throwable ex) {}

        return method;
    }

    private void invokeHookMethod (Method method, Thread thread)
    {
        Object[] args = new Object[1];

        args[0] = thread;
        try
        {
            method.invoke (Runtime.getRuntime (), args);
        }
        catch (Throwable ex)
        {
            Debug.output (4, "Failed to invoke Runtime." + method.getName () + " and exception " + ex);
        }
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

        private TransportManager transport_manager = null;

        /**
         * The constructor. It sets up the ServerSocket and starts the thread.
         */
        public SocketListener()
        {
            try
            {
                server_socket =
                new ServerSocket( default_port );

                transport_manager =
                new TransportManager( (org.jacorb.orb.ORB) orb );

                // First deal with DNS; if we are not using DNS do fallback.
                if( default_host != null && default_host.length() > 0 )
                {
                    address =
                    org.jacorb.orb.dns.DNSLookup.inverseLookup( InetAddress.getByName( default_host ) );
                }
                else
                {
                    address =
                    org.jacorb.orb.dns.DNSLookup.inverseLookup( InetAddress.getLocalHost() );
                }
                if( address == null )
                {
                    if( default_host != null && default_host.length() > 0 )
                    {
                        address = default_host;
                    }
                    else
                    {
                        address = InetAddress.getLocalHost().toString();
                    }
                }

                if( address.indexOf("/") >= 0 )
                    address = address.substring(address.indexOf("/") + 1);

                port = server_socket.getLocalPort();

                Debug.output(4,"ImR Listener at " + port + ", " + address );

                String s =
                Environment.getProperty( "jacorb.imr.connection_timeout",
                                         "2000" ); //default: 2 secs

                try
                {
                    timeout = Integer.parseInt( s );
                }
                catch( NumberFormatException nfe )
                {
                    Debug.output( 3,"ERROR: Unable to build timeout int from string >>" +
                                  s + "<<" );
                    Debug.output( 3,"Please check property \"jacorb.imr.connection_timeout\"" );
                }
            }
            catch (Exception e)
            {
                Debug.output(3, e);
                Debug.output(3,"Listener: Couldn't init");

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
            Debug.output(4,"ImR Listener at " + port + ", " + address );

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

                    org.omg.ETF.Connection transport =
                    new ServerIIOPConnection (socket,
                                              false); // no SSL

                    GIOPConnection connection =
                    new ServerGIOPConnection( transport.get_server_profile(),
                                              transport,
                                              request_listener,
                                              reply_listener,
                                              null,
                                              null);

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
                        Debug.output(4, _e);
                }
            }

            // doing the actual shutdown of the implementation
            // repository here
            orb.shutdown(wait);
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
                Debug.output(4, _e);
            }
        }
    }


    private static boolean checkServerActive(String host, int port, byte []object_key)
    {
        ClientConnectionManager   cm           = null;
        IIOPAddress               address      = null;
        ClientConnection          connection   = null;
        LocateRequestOutputStream lros         = null;
        LocateReplyReceiver       receiver     = null;
        LocateReplyInputStream    lris         = null;
        boolean                   result       = false;

        cm = ((org.jacorb.orb.ORB)orb).getClientConnectionManager ();
        address = new IIOPAddress (host, port);
        connection = cm.getConnection (new IIOPProfile (address, object_key));

        Debug.output(4,"Pinging " + host + " / " + port);
        try
        {
            lros = new LocateRequestOutputStream (object_key, connection.getId(), 2);
            receiver = new LocateReplyReceiver ();

            connection.sendRequest(
                lros,
                receiver,
                lros.getRequestId (),
                true ); // response expected

            lris = receiver.getReply();

            switch (lris.rep_hdr.locate_status.value ())
            {
                case LocateStatusType_1_2._UNKNOWN_OBJECT:
                case LocateStatusType_1_2._OBJECT_HERE:
                case LocateStatusType_1_2._OBJECT_FORWARD:
                case LocateStatusType_1_2._OBJECT_FORWARD_PERM:
                case LocateStatusType_1_2._LOC_SYSTEM_EXCEPTION:
                case LocateStatusType_1_2._LOC_NEEDS_ADDRESSING_MODE:
                default:
                {
                    result = true;
                    break;
                }
            }
        }
        catch (Throwable ex)
        {
            result = false;
        }
        finally
        {
            cm.releaseConnection (connection);
        }
        return result;
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
         * receive and dispatch requests
         *
         * @param request a <code>byte[]</code> value
         * @param connection a <code>GIOPConnection</code> value
         */
        public void requestReceived( byte[] request,
                                     GIOPConnection connection )
        {
            connection.incPendingMessages();

            RequestInputStream in = new RequestInputStream( orb, request );

            replyNewLocation( ((org.jacorb.orb.ORB)orb).mapObjectKey(
                                    ParsedIOR.extractObjectKey(in.req_hdr.target, (org.jacorb.orb.ORB)orb)),
                              in.req_hdr.request_id,
                              in.getGIOPMinor(),
                              connection );
        }

        public void locateRequestReceived( byte[] request,
                                           GIOPConnection connection )
        {
            connection.incPendingMessages();

            LocateRequestInputStream in =
            new LocateRequestInputStream( orb, request );

            replyNewLocation( ParsedIOR.extractObjectKey(in.req_hdr.target, (org.jacorb.orb.ORB) orb),
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

            Debug.output( 4,"ImR: Looking up: " + _server.name );

            // There is only point pinging the remote object if server
            // is active and either the QoS to ping returned objects
            // is true or the ServerStartUpDaemon is active and there
            // is a command to run - if not, even if the server isn't
            // actually active, we can't restart it so just allow this
            // to fall through and throw the TRANSIENT below.
            boolean ssd_valid =
            (
                (_server.command.length() != 0) &&
                (server_table.getHost(_server.host) != null)
            );
            if (_server.active && (check_object_liveness || ssd_valid))
            {
                // At this point the server *might* be running - we
                // just want to verify it.
                if (! checkServerActive (_poa.host, _poa.port, object_key))
                {
                    // Server is not active so set it down
                    _server.setDown ();
                }
            }

            try
            {
                restartServer( _server );
            }
            catch( ServerStartupFailed ssf )
            {
                Debug.output(4,"Object (" + _server.name + ") on "
                    + _poa.host + '/' + _poa.port + " not reachable"
                );

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
                                   giop_minor,
                                   false );

            // The typecode is for org.omg.CORBA.Object, but avoiding
            // creation of new ObjectHolder Instance.
            IIOPAddress addr = new IIOPAddress (_poa.host,(short)_poa.port);
            IIOPProfile p = new IIOPProfile (addr,object_key,giop_minor);
            org.omg.IOP.IOR _ior = ParsedIOR.createObjectIOR(p);

            if( !_old_poa_state )
            {
                // if POA has been reactivated, we have to wait for
                // the requested object to become ready again. This is
                // for avoiding clients to get confused by
                // OBJECT_NOT_EXIST exceptions which they might get
                // when trying to contact the server too early.

                org.omg.CORBA.Object _object =
                orb.string_to_object
                    ((new ParsedIOR( _ior, (org.jacorb.orb.ORB)orb )).getIORString());

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
                        Debug.output(4, _e);
                    }
                }
            }

            try
            {
                // write new location to stream
                out.write_IOR(_ior);

                Debug.output( 4,
                              "ImR: Sending location forward for " +
                              _server.name );

                connection.sendReply( out );
            }
            catch( IOException _e )
            {
                Debug.output(4, _e);
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
                                   giop_minor,
                                   false );

            SystemExceptionHelper.write( out, sys_ex );

            try
            {
                connection.sendReply( out );
            }
            catch( IOException _e )
            {
                Debug.output(4, _e);
            }
        }
    }


    /**
     * <code>WriteThread</code> runs as a background thread which will write the
     * server table out whenever any modifications are made.
     */
    private class WriteThread extends Thread
    {
        boolean done;

        public WriteThread ()
        {
        }

        /**
         * <code>run</code> continiously loops until the shutdown is called.
         */
        public void run ()
        {
            while (true)
            {
                try
                {
                    save_server_table (table_file);
                }
                catch (FileOpFailed ex)
                {
                    Debug.output(4, ex);
                }
                if (done)
                {
                    break;
                }

                // If by the time we have written the server table another request has arrived
                // which requires an update don't bother entering the wait state.
                if ( ! updatePending)
                {
                    try
                    {
                        synchronized (this)
                        {
                            this.wait ();
                        }
                    }
                    catch (InterruptedException ex) {}
                    Debug.output
                    (
                        4,
                        "ImR: IMR write thread waking up to save server table... "
                    );
                }
            }
        }

        /**
         * <code>shutdown</code> toggles the thread to shut itself down.
         */
        public void shutdown ()
        {
            done = true;
        }
    }


    /**
     * <code>Shutdown</code> is a thread that is run the Java 1.3 (and greater)
     * virtual machine upon receiving a Ctrl-C or kill -INT.
     */
    private class Shutdown extends Thread
    {
        public synchronized void run ()
        {
            Debug.output (4, "ImR: Shutting down");
            shutdown (true);
        }
    }

} // ImplementationRepositoryImpl
