/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2004 Gerald Brose
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

import org.jacorb.util.threadpool.*;

import java.lang.*;
import java.net.*;
import java.io.*;

import org.omg.PortableServer.*;

import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.configuration.*;

public class ServerStartupDaemonImpl
    extends org.jacorb.imr.ServerStartupDaemonPOA
{
    private org.omg.CORBA.ORB orb = null;
    private static final String out_prefix = ">> ";

    private ThreadPool stdout_pool = null;
    private ThreadPool stderr_pool = null;

    private Logger logger;

    /**
     * The constructor. It registers this daemon at the repository.
     *
     * @exception Exception any exception that is thrown inside is propagated upwards.
     */
    public ServerStartupDaemonImpl(org.omg.CORBA.ORB orb)
    {
        this.orb = orb;
    }

    public void configure(Configuration myConfiguration)
        throws ConfigurationException
    {
        this.logger = ((org.jacorb.config.Configuration) myConfiguration).getNamedLogger("jacorb.imr");

        try
        {
            Registration _registration = null;

            _registration =
                RegistrationHelper.narrow( orb.resolve_initial_references("ImplementationRepository"));
            if( _registration == null )
                throw new ConfigurationException("ImR not found");

            POA poa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
            poa.the_POAManager().activate();

            ServerStartupDaemon ssd = 
                ServerStartupDaemonHelper.narrow(poa.servant_to_reference(this));

            HostInfo _me = new HostInfo(InetAddress.getLocalHost().getHostName(),
                                        ssd,
                                        orb.object_to_string(ssd));

            _registration.register_host(_me);
        }
        catch (Exception e)
        {
            throw new ConfigurationException("Caught Exception", e);
        }

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
     * @exception org.jacorb.imr.ServerStartupDaemonPackage.ServerStartupFailed
     * Runtime.exec() failed to execute the command.
     */

    public void start_server(String command)
        throws ServerStartupFailed
    {
        try
        {
            if (this.logger.isDebugEnabled())
            {
                this.logger.debug("Starting: " + command);
            }

            Process _server = Runtime.getRuntime().exec( command );

            stdout_pool.putJob(_server);
            stderr_pool.putJob(_server);
        }
        catch (Exception _e)
        {
            this.logger.debug("Caught Exception", _e);
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
            org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init( args, null );
            ServerStartupDaemonImpl _ssd = new ServerStartupDaemonImpl(orb);
            _ssd.configure(((org.jacorb.orb.ORB) orb).getConfiguration());

            orb.run();
        }
        catch( Exception _e )
        {
            _e.printStackTrace();
            System.exit(1);
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
                // If we get null from readLine() we assume that the process
                // has exited.  Unfortunately there is no exception thrown
                // when trying to read from a dead processes output stream.
                while((_line = _in.readLine()) != null)
                {
                    System.out.println(out_prefix + _line);
                }

                _in.close();
            }
            catch( Exception _e )
            {
                logger.debug("Caught Exception", _e);
            }

            logger.debug("A server process exited");
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
