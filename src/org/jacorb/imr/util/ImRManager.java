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
package org.jacorb.imr.util;

import org.jacorb.imr.*;
import org.jacorb.util.Debug;

/**
 * This class is a command-line tool for administering 
 * the implementation repository.
 * 
 * @author Nicolas Noffke
 * 
 * $Id$
 */

public class ImRManager
{
    private static org.jacorb.orb.ORB m_orb;

    /**
     * This method registers a server with the imr. To be called from within
     * a program. Leave command and host to "" (not null), if automatic startup
     * is not desired.
     *
     * @param edit_existing if set to true and the server already exist, 
     * the entry will be set to the supplied new values.
     */
    public static void autoRegisterServer( org.omg.CORBA.ORB orb, 
                                           String server,
                                           String command, 
                                           String host,
                                           boolean edit_existing )
    {
        try
        {
            Admin admin = 
                AdminHelper.narrow( orb.resolve_initial_references("ImplementationRepository"));

            ServerInfo info = null;

            try
            {
                info = admin.get_server_info(server);
            }
            catch (UnknownServerName n)
            {
                Debug.output(3, n);
            }

            if (info == null)
            {
                admin.register_server(server, command, host);
            }
            else if ((info != null) && edit_existing)
            {
                admin.edit_server(server, command, host);
            }
        }
        catch (Exception e)
        {
            Debug.output(3, e);
        }
    }

    /**
     * Returns the name of the local host to be supplied to the imr.
     * If this can't be queried, an empty String is returned.
     */
    public static String getLocalHostName()
    {
        try
        {
            return java.net.InetAddress.getLocalHost().getHostName();
        }
        catch (java.net.UnknownHostException e)
        {
            Debug.output(3, e);
        }

        return "";
    }

    /**
     * Returns an arbitrary host, on which an imr_ssd is running,
     * or an empty String, if none is present. 
     */
    public static String getAnyHostName(org.omg.CORBA.ORB orb)
    {
        try
        {
            Admin admin = 
                AdminHelper.narrow( orb.resolve_initial_references("ImplementationRepository"));

            HostInfo[] hosts = admin.list_hosts();

            if (hosts.length > 0)
            {
                return hosts[0].name;
            }     
        }
        catch (Exception e)
        {
            Debug.output(3, e);
        }

        return "";
    }

    private static Admin getAdmin()
    {

        Admin _admin = null;
        try
        {
            _admin = 
                AdminHelper.narrow( m_orb.resolve_initial_references("ImplementationRepository"));
        }
        catch( org.omg.CORBA.ORBPackage.InvalidName in )
        {
            Debug.output(0, "WARNING: Could not contact Impl. Repository!");
        }
        if (_admin == null)
        {
            System.out.println("Unable to connect to repository process!");
            System.exit(-1);
        }

        return _admin;
    }

    /**
     * Add a server to the repository or edit an existing server.
     *
     */
    private static void addServer(String[] args)
    {
        if (args.length == 1)
        {
            System.out.println("Please specify at least server name");
            shortUsage();
        }

        String _server_name = args[1];
        String _host = null;
        String _command = null;

        try 
        {
            //evaluate parameters
            for (int i = 2; i < args.length ; i++)
            {
                if( args[i].equals("-h") )
                {           
                    //is next arg available?
                    if ( (++i) < args.length )
                    {
                        _host = args[i];
                    }
                    else
                    {
                        System.out.println("Please provide a hostname after the -h switch");

                        shortUsage();
                    }
                }
                else if( args[i].equals("-c") )
                {		
                    StringBuffer sb = new StringBuffer();
                    
                    for( int j = (i + 1); j < args.length; j++ )
                    {
                        sb.append( args[j] );
                        
                        if( j < (args.length - 1) )
                        {
                            sb.append( ' ' ); //append whitespace
                            //don't append, if last arg
                        }
                    }
                    
                    _command = sb.toString();
                    break; //definitely at end of args
                }
                else
                {
                    System.out.println("Unrecognized switch: " + args[i]);
                    shortUsage();
                }
            }

            if (_host == null)
            {
                _host = getLocalHostName();
            }		    
        }
        catch (Exception _e)
        {
            _e.printStackTrace();
            usage();
        }
	
        if (_command == null)
        {
            _command = "";
        }
	
        Admin _admin = getAdmin();

        try
        {
            if (args[0].equals("add"))
            {
                _admin.register_server(_server_name, _command, _host); 
            }
            else
            {
                //else case already checked in main
                _admin.edit_server(_server_name, _command, _host);
            }

            System.out.println("Server " + _server_name + " successfully " + 
                               args[0] + "ed");
            System.out.println("Host: >>" + _host + "<<");
            
            if( _command.length() > 0 )
            {
                System.out.println("Command: >>" + _command + "<<");
            }
            else
            {
                System.out.println("No command specified. Server can't be restarted!");
            }
        }
        catch (Exception _e)
        {
            _e.printStackTrace();
        }

        System.exit(0);
    }

    /**
     * Remove a server or host from the repository.
     */
    private static void remove(String[] args)
    {
        if (args.length == 1)
        {
            System.out.println(" Please specify if you want to remove a server or a host");
            shortUsage();
        }

        if (args.length == 2)
        {
            System.out.println(" Please specify a servername / hostname");
            shortUsage();
        }
    
        Admin _admin = getAdmin();

        if (args[1].equals("server"))
        {
            try
            {
                _admin.unregister_server(args[2]);
	    
                System.out.println("Server " + args[2] + 
                                   " successfully removed");	      
            }
            catch (Exception _e)
            {
                _e.printStackTrace();
            }
        }
        else if (args[1].equals("host")) 
        {
            try
            {
                _admin.unregister_host(args[2]);
	    
                System.out.println("Host " + args[2] + 
                                   " successfully removed");	      
            }
            catch (Exception _e)
            {
                _e.printStackTrace();
            }
        }
        else
        {
            System.out.println("Unknown command " + args[1]);            
            shortUsage();
        }

        System.exit(0);
    }

    /**
     * List servers or hosts.
     */
    private static void list(String[] args)
    {
        if (args.length == 1)
        {
            System.out.println("Please use (servers | hosts) in command");
            shortUsage();
        }

        Admin _admin = getAdmin();
	
        try
        {
            if (args[1].equals("servers"))
            {
                ServerInfo[] _info = _admin.list_servers();

                System.out.println("Servers (total: " + _info.length + "):");

                for(int _i = 0; _i < _info.length; _i++)
                {
                    System.out.println((_i + 1) + ") " +_info[_i].name);

                    System.out.println("   " + "Host: " + _info[_i].host);

                    System.out.println("   " + "Command: " + 
                                       _info[_i].command);

                    System.out.println("   " + "active: " + 
                                       ((_info[_i].active)?"yes":"no"));

                    System.out.println("   " + "holding: " + 
                                       ((_info[_i].holding)?"yes":"no"));
                }
            }
            else if (args[1].equals("hosts"))
            {
                HostInfo[] _info = _admin.list_hosts();

                System.out.println("Hosts (total: " + _info.length + "):");

                for(int _i = 0; _i < _info.length; _i++)
                {
                    System.out.println((_i + 1) + ") " +_info[_i].name);
                }
            }
            else 
            {
                System.out.println("Unrecognized option: " + args[1]);
                shortUsage();
            }
        }
        catch (Exception _e)
        {
            _e.printStackTrace();
        }
        System.exit(0);
    }

    /**
     * Hold a server.
     */
    private static void holdServer(String[] args)
    {
        if (args.length == 1)
        {
            System.out.println("Please specify a server name");
            shortUsage();
        }

        String _server_name = args[1];
        int _timeout = 0;

        Admin _admin = getAdmin();

        try
        {
            if (args.length == 3)
            {
                _timeout = Integer.parseInt(args[2]);
            }

            _admin.hold_server(_server_name);
            System.out.println("Server " + _server_name + " set to holding");
 	    
            if (_timeout > 0)
            {
                Thread.sleep(_timeout);

                _admin.release_server(_server_name);

                System.out.println("Server " + _server_name + " released");
            }

        }
        catch (Exception _e)
        {
            _e.printStackTrace();
        }
        System.exit(0);
    }

    /**
     * Release a server.
     */
    private static void releaseServer(String[] args)
    {
        if (args.length == 1)
        {
            System.out.println("Please specify a server name");
            shortUsage();
        }

        String _server_name = args[1];
        int _timeout = 0;

        Admin _admin = getAdmin();

        try
        {
            _admin.release_server(_server_name);

            System.out.println("Server " + _server_name + " released");
        }
        catch (Exception _e)
        {
            _e.printStackTrace();
        }
        System.exit(0);
    }

    /**
     * Start a server.
     */
    private static void startServer(String[] args)
    {
        if (args.length == 1)
        {
            System.out.println("Please specify a server name");
            shortUsage();
        }

        String _server_name = args[1];

        Admin _admin = getAdmin();

        try
        {
            _admin.start_server(_server_name);

            System.out.println("Server " + _server_name + " started");
        }
        catch (Exception _e)
        {
            _e.printStackTrace();
        }
        System.exit(0);
    }

    /**
     * Write a backup of the server table.
     */
    private static void saveTable()
    {
        Admin _admin = getAdmin();

        try
        {
            _admin.save_server_table();

            System.out.println("Backup of server table was successfull");
        }
        catch (Exception _e)
        {
            _e.printStackTrace();
        }
        System.exit(0);
    }

    /**
     * Shut the repository down.
     */
    private static void shutdownImR(String[] args)
    {
        Admin _admin = getAdmin();

        boolean _wait = true;
	
        if (args.length == 2)
        {
            if (args[1].toLowerCase().equals("force"))
            {
                _wait = false;
            }
            else 
            {
                System.out.println("Unrecognized option: " + args[1]);
                System.out.println("The only possible option is \"force\"");
                shortUsage();
            }
        }
		
        try
        {
            _admin.shutdown(_wait);

            System.out.println("The Implementation Repository has been shut down without exceptions");
        }
        catch (Exception _e)
        {
            _e.printStackTrace();
        }
        System.exit(0);
    }
    
    /**
     * Set a server to not active.
     */    
    private static void setDown(String[] args)
    {
        if (args.length == 1)
        {
            System.out.println("Please specify a server name");
            shortUsage();
        }

        Registration _reg = RegistrationHelper.narrow(getAdmin());
	
        try
        {
            _reg.set_server_down(args[1]);
            
            System.out.println("Server " + args[1] + " set down");
        }
        catch (Exception _e)
        {
            _e.printStackTrace();
        }
        System.exit(0);
    }

    private static void shortUsage()
    {
        System.out.println("\nYour command has not been understood possibly due to\n" +
                           "one or more missing arguments");
        System.out.println("Type \"imr_mg help\" to display the help screen");
        System.exit(-1);
    }
        

    /**
     * Print help messages.
     */
    private static void usage()
    {
        System.out.println("Usage: ImRManager <command> [<servername>] [switches]");
        System.out.println("Command: (add | edit) <servername> [-h <hostname> -c <startup cmd>]");
        System.out.println("\t -h <hostname> Restart server on this host");
        System.out.println("\t -c <command> Restart server with this command");
        System.out.println("\t If -h is not set, the local hosts name (that of the manager) is used.");
        System.out.println("\t Note: The -c switch must always follow after the -h switch,");
        System.out.println("\t because all arguments after -c are interpreted as the");
        System.out.println("\t startup command.");

        System.out.println("\nCommand: remove (server | host) <name>");
        System.out.println("\t Removes the server or host <name> from the repository");

        System.out.println("\nCommand: list (servers | hosts)");
        System.out.println("\t Lists all servers or all hosts");

        System.out.println("\nCommand: hold <servername> [<time>]");
        System.out.println("\t Holds the server <servername> (if <time> is specified,");
        System.out.println("\t it is released automatically)");

        System.out.println("\nCommand: release <servername>");
        System.out.println("\t Releases the server <servername>");

        System.out.println("\nCommand: start <servername>");
        System.out.println("\t Starts the server <servername> on its given host with " +
                           "its given command");

        System.out.println("\nCommand: setdown <servername>");
        System.out.println("\t Declares the server <servername> as \"down\" to the repository.");
        System.out.println("\t This means that the repository tries to start the server up after ");
        System.out.println("\t receiving the next request for it.");
        System.out.println("\t This is actually an operation only committed by the ORB, but it");
        System.out.println("\t might be useful for server migration and recovery of crashed servers.");
        System.out.println("\t Note: Use \"hold\" before, to avoid the server being restarted at the");
        System.out.println("\t wrong moment.");

        System.out.println("\nCommand: savetable");
        System.out.println("\t Makes a backup of the server table");

        System.out.println("\nCommand: shutdown [force]");
        System.out.println("\t Shuts the ImR down orderly. If \"force\" is specified, the ORB ");
        System.out.println("\t is forced down, ignoring open connections.");

        System.out.println("\nCommand: gui");
        System.out.println("\t Bring up manager GUI window");

        System.out.println("\nCommand: help");
        System.out.println("\t This screen");

        System.exit(1);
    }

    /**
     * Main method.
     */
    public static void main(String[] args) 
    {
        if( args.length == 0 )
        {
            usage();
        }

        m_orb = (org.jacorb.orb.ORB) org.omg.CORBA.ORB.init(args, null);

        try
        {
            if (args[0].equals("add") || args[0].equals("edit"))
                addServer(args);
            else if (args[0].equals("remove"))
                remove(args);
            else if (args[0].equals("list"))
                list(args);
            else if (args[0].equals("hold"))
                holdServer(args);
            else  if (args[0].equals("release"))
                releaseServer(args);
            else if (args[0].equals("start"))
                startServer(args);
            else  if (args[0].equals("savetable"))
                saveTable();
            else  if (args[0].equals("shutdown"))
                shutdownImR(args);
            else  if (args[0].equals("setdown"))
                setDown(args);
            else  if (args[0].equals("gui"))
                Class.forName("org.jacorb.imr.util.ImRManagerGUI").newInstance();
            else  if (args[0].equals("help"))
                usage();
            else 
            {
                System.out.println("Unrecognized command: " + args[0]);
                usage();
            }
        }
        catch (Exception _e)
        {
            _e.printStackTrace();
            System.exit(0);
        }
    }
    
} // ImRManager








