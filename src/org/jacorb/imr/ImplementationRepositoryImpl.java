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

import org.jacorb.util.ObjectUtil;

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
     * <code>orb</code> is the ORB instance for the IMR.
     */
    private org.omg.CORBA.ORB orb;

    private org.jacorb.config.Configuration configuration = null;

    /** the specific logger for this component */
    private Logger logger = null;

    private String iorFile = null;

    private File table_file;
    private ServerTable server_table;
    private File table_file_backup;
    private SocketListener listener;
    private Thread listenerThread;

    private int object_activation_retries = 5;
    private int object_activation_sleep = 50;

    private boolean allow_auto_register = false;
    private boolean check_object_liveness = false;

    private int connection_timeout = 2000;

    private WriteThread wt;
    private boolean updatePending;
    private Shutdown shutdownThread;

    /**
     * The constructor.
     * It builds up the server table and starts up the SocketListener thread.
     *
     * @param table_file the file containing the serialized server table. Also
     * used for writing the table to on shutdown. If null, an empty table is
     * created.
     * @param table_backup the file where backups are written to.
     * @param new_table set to true, if an empty server table should be created
     */
    public ImplementationRepositoryImpl(org.omg.CORBA.ORB orb)
    {
        this.orb = orb;

        shutdownThread = new Shutdown ();
        shutdownThread.setDaemon (true);
        shutdownThread.setName ("Shutdown Thread");
        addShutdownHook (shutdownThread);

        wt = new WriteThread ();
        wt.setName ("IMR Write Thread");
        wt.setDaemon (true);
        wt.start ();
    }

    public void configure(Configuration myConfiguration)
        throws ConfigurationException
    {
        configuration = (org.jacorb.config.Configuration)myConfiguration;

        logger = configuration.getNamedLogger("jacorb.imr");

        String defaultTableFile = "table.dat";
        String tableFileStr = configuration.getAttribute("jacorb.imr.table_file", 
                                                         defaultTableFile);

        //NOTE: deliberate use of ref equivalence check here. I need to find
        //out if the default case has taken place, in which case, i assume
        //that the default string ref is just passed through.
        if (tableFileStr == defaultTableFile)
        {
            if (this.logger.isWarnEnabled())
            {
                this.logger.warn("No file for the server table specified! Please configure the property jacorb.imr.table_file!");
                this.logger.warn("Will create \"table.dat\" in current directory, if necessary");
            }
        }

        table_file = new File(tableFileStr);
        boolean _new_table = false;

        // try to open table file
        if (! table_file.exists ())
        {
            _new_table = true;
            if (this.logger.isInfoEnabled())
            {
                this.logger.info("Table file " + tableFileStr + 
                                 " does not exist - autocreating it.");
            }

            try
            {
                table_file.createNewFile ();
            }
            catch (IOException ex)
            {
                throw new ConfigurationException("Failed to create table file", ex);
            }
        }
        else
        {
            if (table_file.isDirectory ())
            {
                throw new ConfigurationException("The table file is a directory! Please check " + table_file.getAbsolutePath());
            }

            if (! table_file.canRead())
            {
                throw new ConfigurationException("The table file is not readable! Please check " + table_file.getAbsolutePath());
            }

            if (! table_file.canWrite())
            {
                throw new ConfigurationException("The table file is not writable! Please check " + table_file.getAbsolutePath());
            }
        }

        try
        {
            if (_new_table)
            {
                this.server_table = new ServerTable();
                save_server_table(table_file);
            }
            else
            {
                try
                {
                    ObjectInputStream _in = 
                        new ObjectInputStream(new FileInputStream(table_file));
                    server_table = (ServerTable)_in.readObject();
                    _in.close();
                }
                catch (Exception ex)
                {
                    this.logger.debug("Failed to read ServerTable", ex);

                    server_table = new ServerTable();
                    save_server_table(table_file);
                }
            }
        }
        catch (FileOpFailed ex)
        {
            this.logger.debug("Failed to read ServerTable", ex);
        }


        //should be set. if not, throw
        this.iorFile = configuration.getAttribute("jacorb.imr.ior_file");

        String _backup_file_str = 
            configuration.getAttribute("jacorb.imr.backup_file", "");

        //set up server table backup file
        if (_backup_file_str.length() == 0)
        {
            this.logger.warn("No backup file specified!. No backup file will be created");
        }

        if (_backup_file_str.length() > 0)
        {
            table_file_backup = new File(_backup_file_str);

            // try to open backup file
            if ( ! table_file_backup.exists ())
            {
                _new_table = true;
                
                if (this.logger.isInfoEnabled())
                {
                    this.logger.info("Backup file " + _backup_file_str + 
                                     " does not exist - autocreating it.");
                }

                try
                {
                    table_file_backup.createNewFile();
                }
                catch (IOException ex)
                {
                    throw new ConfigurationException("Failed to create backup file",
                                                     ex);
                }
            }
            else
            {
                if (table_file_backup.isDirectory ())
                {
                    throw new ConfigurationException("The backup file is a directory! Please check " + table_file_backup.getAbsolutePath());
                }

                if (! table_file_backup.canRead())
                {
                    throw new ConfigurationException("The backup file is not readable! Please check " + table_file_backup.getAbsolutePath());
                }

                if (! table_file_backup.canWrite())
                {
                    throw new ConfigurationException("The backup file is not writable! Please check " + table_file_backup.getAbsolutePath());
                }
            }
        }

        this.object_activation_retries =
            configuration.getAttributeAsInteger("jacorb.imr.object_activation_retries",
                                            5);

        this.object_activation_sleep = 
            configuration.getAttributeAsInteger("jacorb.imr.object_activation_sleep",
                                            50);

        this.allow_auto_register = 
            configuration.getAttributeAsBoolean("jacorb.imr.allow_auto_register",
                                                false);
        this.check_object_liveness =
            configuration.getAttributeAsBoolean("jacorb.imr.check_object_liveness",
                                                false);

        this.connection_timeout = 
            configuration.getAttributeAsInteger("jacorb.imr.connection_timeout",
                                            2000 );

        this.listener = new SocketListener();
        this.listener.configure(configuration);
        
        this.listenerThread = new Thread(listener);
        this.listenerThread.setPriority(Thread.MAX_PRIORITY);
        this.listenerThread.start();        
    }

    public String getIORFile()
    {
        return this.iorFile;
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
        if (this.logger.isDebugEnabled())
        {
            this.logger.debug("ImR: server " + server + " is going down... ");
        }

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

        if (this.logger.isDebugEnabled())
        {
            this.logger.debug("ImR: registering poa " + name + " for server: " +
                              server + " on " + host);
        }

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

            this.logger.debug("ImR: new poa registered");
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
                    this.logger.debug("ImR: Remapping server/port");
                }
            }

            _poa.reactivate(host, port);
            this.logger.debug("ImR: register_poa, reactivated");
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
            this.logger.debug("Caught Exception", e);
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
        catch (Exception e)
        {
            this.logger.debug("Caught Exception", e);
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
            this.logger.debug("Caught Exception", e);
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
            this.logger.debug("ImR: Checking servers");

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
                            if (this.logger.isDebugEnabled())
                            {
                                this.logger.debug("ImR: Setting server " + 
                                                  servers[k].name + " down");
                            }

                            // Server is not active so set it down
                            server_table.getServer(servers[k].name).setDown();

                            // Save retrieving the list again.
                            servers[k].active = false;
                        }
                        catch (UnknownServerName e)
                        {
                            if (this.logger.isDebugEnabled())
                            {
                                this.logger.debug("ImR: Internal error - unknown server " + servers[k].name, e);
                            }
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
        throws UnknownServerName
    {
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

        if (this.logger.isDebugEnabled())
        {
            this.logger.debug("ImR: server " + name + " on " + 
                              host + " registered");
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
            this.logger.debug("Caught Exception", e);
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

        if (this.logger.isDebugEnabled())
        {
            this.logger.debug("ImR: server " + name + " unregistered");
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
            this.logger.debug("Caught Exception", e);
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

        if (this.logger.isDebugEnabled())
        {
            this.logger.debug("ImR: server " + name + " edited");
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
            this.logger.debug("Caught Exception", e);
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
                    listenerThread.join(5000);
                }
            }
            catch (InterruptedException e)
            {
                this.logger.debug("Caught Exception", e);
            }
        }
        try
        {
            save_server_table ();
        }
        catch (FileOpFailed f)
        {
            this.logger.debug("ImR: Failed to save backup table.", f);
        }
        this.logger.debug("ImR: Finished shutting down");
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
        catch (Exception e)
        {
            this.logger.debug("Caught Exception", e);
            throw new FileOpFailed();
        }
        updatePending = false;
    }


    /**
     * Prints the usage screen and exits.
     */
    public static void usage ()
    {
        System.out.println("Usage: The following properties are useful in conjunction with the \nImplementationRepository:");
        System.out.println("\t \"jacorb.imr.endpoint_host\" Address to listen on for requests");
        System.out.println("\t \"jacorb.imr.endpoint_port\" Port to listen on for requests");
        System.out.println("\t \"jacorb.imr.table_file\" The file to store the server table into");
        System.out.println("\t \"jacorb.imr.backup_file\" The file to store the server table backup into");
        System.out.println("\t \"jacorb.imr.ior_file\" The file to store the ImRs IOR into");
        System.out.println("\t \"jacorb.imr.allow_auto_register\" if set to \"on\", servers that don't \n\talready have an entry on their first call to the imr, will get \n\tautomatically registered. Otherwise, an UnknownServer exception \n\tis thrown.");
        System.exit(0);
    }

    /**
     * The main method. "Parses" the arguments and sets the corresponding
     * attributes up, creates a new ImplementationRepositoryImpl instance and
     * runs the ORB.
     */
    public static void main(String[] args) 
    {
        // translate any properties set on the commandline but after the 
        // class name to a properties
        java.util.Properties argProps = ObjectUtil.argsToProps( args );
        argProps.setProperty("jacorb.implname", "the_ImR");
        argProps.setProperty("jacorb.use_imr", "off");

        //Write IOR to file
        try
        {
            org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init( args, argProps );

            ImplementationRepositoryImpl _imr = 
                new ImplementationRepositoryImpl(orb);
            _imr.configure(((org.jacorb.orb.ORB) orb).getConfiguration());

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


            imr_poa.activate_object_with_id( id, _imr );

            PrintWriter _out = new PrintWriter
                (new FileOutputStream(new File(_imr.getIORFile())));

            _out.println(orb.object_to_string(imr_poa.servant_to_reference(_imr)));
            _out.flush();
            _out.close();

            orb.run();
        }
        catch (Exception _e)
        {
            _e.printStackTrace();
            usage();
            System.exit(1);
        }
    }

    private void restartServer(ImRServerInfo server)
        throws ServerStartupFailed
    {
        // server might be holding
        server.awaitRelease();

        if(! server.active )
        {        
            if (this.logger.isDebugEnabled())
            {
                this.logger.debug("ImR: server " + server.name + " is down");
            }

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

                        if (this.logger.isDebugEnabled())
                        {
                            this.logger.debug("ImR: will restart " + server.name);
                        }

                        _host.startServer(server.command, orb);
                    }
                    catch (ServerStartupFailed ssf)
                    {
                        server.setNotRestarting();

                        throw ssf;
                    }
                    catch (Exception e)
                    {
                        server.setNotRestarting();

                        this.logger.debug("Caught Exception", e);

                        // sth wrong with daemon, remove from table
                        server_table.removeHost(server.host);

                        throw new ServerStartupFailed("Failed to connect to host!");
                    }
                }
                else
                {
                    if (this.logger.isDebugEnabled())
                    {
                        this.logger.debug("ImR: somebody else is restarting " +
                                          server.name);
                    }
                }
            }
        }
        else
        {
            if (this.logger.isDebugEnabled())
            {
                this.logger.debug("ImR: server " + server.name + " is active");
            }
        }
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
            if (this.logger.isDebugEnabled())
            {
                this.logger.debug("Failed to invoke Runtime." + method.getName (), 
                                  ex);
            }
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
        implements Runnable
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
            transport_manager =
                new TransportManager( (org.jacorb.orb.ORB) orb );

            receptor_pool = MessageReceptorPool.getInstance();
            request_listener = new ImRRequestListener();
            reply_listener = new NoBiDirServerReplyListener();
        }

        public void configure(Configuration myConfiguration)
            throws ConfigurationException
        {
            try
            {
                int endpoint_port = 
                    configuration.getAttributeAsInteger(
                        "jacorb.imr.endpoint_port_number",0);

                String endpoint_host = 
                    configuration.getAttribute("jacorb.imr.endpoint_host", "");

                if (endpoint_host.length() > 0)
                {
                    server_socket =
                        new ServerSocket( endpoint_port,
                                          50, //default backlog, see jdk doc
                                          InetAddress.getByName(endpoint_host));
                }
                else
                {
                    //no explicit address given, listen anywhere
                    server_socket =
                        new ServerSocket(endpoint_port);
                }

                org.jacorb.orb.dns.DNSLookup lookup =
                    new org.jacorb.orb.dns.DNSLookup();
                lookup.configure(configuration);

                // First deal with DNS; if we are not using DNS do fallback.
                if( endpoint_host.length() > 0 )
                {
                    address = lookup.inverseLookup( 
                        InetAddress.getByName( endpoint_host ) );
                }
                else
                {
                    address = lookup.inverseLookup( 
                        InetAddress.getLocalHost() );
                }

                if( address == null )
                {
                    if( endpoint_host.length() > 0 )
                    {
                        address = endpoint_host;
                    }
                    else
                    {
                        address = InetAddress.getLocalHost().toString();
                    }
                }

                if( address.indexOf("/") >= 0 )
                    address = address.substring(address.indexOf("/") + 1);

                port = server_socket.getLocalPort();

                if (logger.isDebugEnabled())
                {
                    logger.debug("ImR Listener at " + port + ", " + address);
                }
            }
            catch (Exception e)
            {
                throw new ConfigurationException("Listener: Couldn't init", e);
            }
        }


        /**
         * Get the port this SocketListener is listening on.
         *
         * @return the port
         */
        public int getPort()
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("ImR Listener at " + port + ", " + address);
            }

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
                    {
                        logger.debug("Caught Exception", _e);
                    }
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
                logger.debug("Caught Exception", _e);
            }
        }
    }


    private boolean checkServerActive(String host, int port, byte []object_key)
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

        if (this.logger.isDebugEnabled())
        {
            this.logger.debug("Pinging " + host + " / " + port);
        }

        try
        {
            lros = new LocateRequestOutputStream (object_key, connection.getId(), 2);
            receiver = new LocateReplyReceiver((org.jacorb.orb.ORB)orb);

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
            this.logger.debug("Caught Exception", ex);

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

            if (logger.isDebugEnabled())
            {
                logger.debug("ImR: Looking up: " + _server.name);
            }

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
                if (logger.isDebugEnabled())
                {
                    logger.debug("Object (" + _server.name + ") on "
                                      + _poa.host + '/' + _poa.port + 
                                      " not reachable");
                }

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
                                       false,
                                       logger);

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
                    orb.string_to_object(
                        (new ParsedIOR( _ior, (org.jacorb.orb.ORB) orb, logger)).getIORString());

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
                        logger.debug("Caught Exception", _e);
                    }
                }
            }

            try
            {
                // write new location to stream
                out.write_IOR(_ior);

                if (logger.isDebugEnabled())
                {
                    logger.debug("ImR: Sending location forward for " +
                                      _server.name);
                }

                connection.sendReply( out );
            }
            catch( IOException _e )
            {
                logger.debug("Caught Exception", _e);

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
                                       false,
                                       logger);

            SystemExceptionHelper.write( out, sys_ex );

            try
            {
                connection.sendReply( out );
            }
            catch( IOException _e )
            {
                logger.debug("Caught Exception", _e);
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
                    logger.debug("Caught Exception", ex);
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

                    logger.debug("ImR: IMR write thread waking up to save server table... ");
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
            logger.debug("ImR: Shutting down");
            shutdown(true);
        }
    }

} // ImplementationRepositoryImpl
