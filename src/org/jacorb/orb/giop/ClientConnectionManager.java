package org.jacorb.orb.connection;

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

import java.io.*;
import java.net.*;
import java.util.*;
import java.lang.reflect.Constructor;

import org.jacorb.orb.*;
import org.jacorb.orb.factory.*;
import org.jacorb.orb.iiop.*;
import org.jacorb.util.*;

/**
 * This class manages connections.<br>
 *
 * @author Gerald Brose, FU Berlin
 * @version $Id$
 *
 */

public class ClientConnectionManager
{    
    public static final String FACTORY_PROP = "jacorb.net.socket_factory";

    private org.jacorb.orb.ORB orb = null;

    /** connection mgmt. */
    private Map connections = new HashMap();

    private SocketFactory socket_factory = null;
    private SocketFactory ssl_socket_factory = null;

    private RequestListener request_listener = null;

    private MessageReceptorPool receptor_pool = null;

    private TransportManager transport_manager = null;
    private GIOPConnectionManager giop_connection_manager = null;

    public ClientConnectionManager( ORB orb,
                                    TransportManager transport_manager,
                                    GIOPConnectionManager giop_connection_manager )
    {
        this.orb = orb;
        this.transport_manager = transport_manager;
        this.giop_connection_manager = giop_connection_manager;

        socket_factory = SocketFactoryManager.getSocketFactory (orb);

        if (Environment.isPropertyOn( "jacorb.security.support_ssl" ))
        {
            String s = Environment.getProperty( "jacorb.ssl.socket_factory" );
            if( s == null || s.length() == 0 )
            {
                throw new RuntimeException( "SSL support is on, but the property \"jacorb.ssl.socket_factory\" is not set!" );
            }
            
            try
            {
                Class ssl = Class.forName( s );
                
                Constructor constr = ssl.getConstructor( new Class[]{
                    ORB.class });
                
                ssl_socket_factory = (SocketFactory)
                    constr.newInstance( new Object[]{ orb });
            }
            catch (Exception e)
            {
                Debug.output( Debug.IMPORTANT | Debug.ORB_CONNECT,
                              e );
                
                throw new RuntimeException( "SSL support is on, but the ssl socket factory can't be instanciated (see trace)!" );
            }
        }

        request_listener = new NoBiDirClientRequestListener();

        receptor_pool = MessageReceptorPool.getInstance();
    }

    public static String unifyTargetAddress( String host_and_port )
    {
        int separator_index = host_and_port.indexOf( ":" );

        if( separator_index < 0 )
        {
            throw new org.omg.CORBA.BAD_PARAM( "Missing port in host_and_port string: >" + host_and_port + '<' );
        }

        String host = host_and_port.substring( 0, separator_index );
        String port = host_and_port.substring( separator_index + 1 );

        try
        {
            /** make sure we have a raw IP address here */
            InetAddress inet_addr = 
                InetAddress.getByName( host );
            
            host_and_port = inet_addr.getHostAddress() + ':' + port;
        }
        catch( UnknownHostException uhe )
        {
            throw new org.omg.CORBA.TRANSIENT("Unknown host " + host);
        }

        return host_and_port;
    }


    public void setRequestListener( RequestListener listener )
    {
        request_listener = listener;
    }

    /**
     * @param <code>String host_and_port</code> - in "host:xxx" notation
     * @return <code>Connection</code> */

    public synchronized ClientConnection getConnection( IIOPProfile profile,
                                                        boolean use_ssl )
    {
        /* look for an existing connection */
        
        ClientConnection c = 
            (ClientConnection)connections.get( profile );

        if (c == null)
        {
            Transport transport = 
                transport_manager.createClientTransport( profile,
                                                         use_ssl );

            GIOPConnection connection = 
                giop_connection_manager.createClientGIOPConnection( 
                    transport,
                    request_listener,
                    null );
            
            c = new ClientConnection( connection, orb, this, 
                                      profile.getAddress().toString(), true );

            Debug.output( 2, "ClientConnectionManager: created new conn to target " +
                          c.getInfo() );
            
            connections.put( profile, c );

            receptor_pool.connectionCreated( connection );
        }
        else
        {
            Debug.output( 2, "ClientConnectionManager: found conn to target " +
                          c.getInfo() );
        }

        c.incClients();

        return c;
    }

    public synchronized void releaseConnection( ClientConnection c )
    {
        c.decClients();
        
        if( c.hasNoMoreClients() )
        {
            c.close();

            connections.remove (c.get_server_profile()); 
        }
    }

    public synchronized void removeConnection( ClientConnection c )
    {
        connections.remove( c.get_server_profile() );
    }




    public synchronized void addConnection( GIOPConnection connection )
    {
        addConnection ( connection,
                        connection.getTransport().get_server_profile() );
    }

    public synchronized void addConnection( GIOPConnection connection,
                                            org.omg.ETF.Profile profile )
    {
        if( !connections.containsKey( profile ))
        {
            ClientConnection c = new ClientConnection
            ( 
                connection, orb, this, 
                profile.toString(),
                false 
            );

            //this is a bit of a hack: the bidirectional client
            //connections have to persist until their underlying GIOP
            //connection is closed. Therefore, we set the initial
            //client count to 1, so the connection will be kept even
            //if there are currently no associated Delegates.
        
            c.incClients();
        
            connections.put( profile, c );
        }
    }

    public void shutdown()
    {
        /* release all open connections */
        
        for( Iterator i = connections.values().iterator(); i.hasNext(); )
        {
            ((ClientConnection) i.next()).close();
        }
        
        Debug.output(3,"ClientConnectionManager shut down (all connections released)");
        
        connections.clear();
    }
}
