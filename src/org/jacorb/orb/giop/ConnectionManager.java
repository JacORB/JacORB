package org.jacorb.orb.connection;

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

import java.io.*;
import java.net.*;
import java.util.*;
import java.lang.reflect.Constructor;

import org.jacorb.orb.*;
import org.jacorb.orb.factory.*;
import org.jacorb.util.*;

/**
 * This class manages connections.<br>
 *
 * @author Gerald Brose, FU Berlin
 * @version $Id$
 *
 */

public class ConnectionManager
{    
    private org.jacorb.orb.ORB orb;

    /** connection mgmt. */
    private Hashtable connections = new Hashtable();


    private SocketFactory socket_factory = null;
    private SocketFactory ssl_socket_factory = null;

    private RequestListener request_listener = null;

    private MessageReceptorPool receptor_pool = null;

    public ConnectionManager(ORB orb)
    {
        this.orb = orb;
        
        socket_factory = new SocketFactory(){
                public Socket createSocket( String host,
                                            int port )
                    throws IOException, UnknownHostException
                    {
                        return new Socket( host, port );
                    }
                
                public boolean isSSL( Socket socket )
                    {
                        //this factory doesn't know about ssl
                        return false;
                    }
            };
        
        if( Environment.supportSSL() )
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

    /**
     * Low-level lookup operation for existing connections to
     * destinations, <br> opens a new one if no connection exists.
     *
     * @param <code>String host_and_port</code> - in "host:xxx" notation
     * @return <code>Connection</code> */

    public synchronized ClientConnection getConnection( String host_and_port, 
                                                        boolean target_ssl )
    {
        if( host_and_port.indexOf('/') > 0)
        {
            host_and_port = 
                host_and_port.substring( host_and_port.indexOf('/') + 1 );
        }
        
        String host = host_and_port.substring(0,host_and_port.indexOf(":"));
        String port = host_and_port.substring(host_and_port.indexOf(":")+1);

        try
        {
            /** make sure we have a raw IP address here */
            InetAddress inet_addr = 
                InetAddress.getByName( host );
            
            host_and_port = inet_addr.getHostAddress() + ":" + port;
            
            host = host_and_port.substring( 0, 
                                            host_and_port.indexOf(":") );
        }
        catch( UnknownHostException uhe )
        {
            throw new org.omg.CORBA.TRANSIENT("Unknown host " + host);
        }
        
        /* look for an existing connection */
        
        ClientConnection c = 
            (ClientConnection)connections.get( host_and_port );

        if( c == null )
        {
            int _port = -1;
            try
            {
                _port = Integer.parseInt( port );
            }
            catch( NumberFormatException nfe )
            {
                Debug.output( 1, "Unable to create port int from string >" +
                              port + '<' );
                
                throw new org.omg.CORBA.BAD_PARAM();
            }
            
            if( _port < 0)
                _port += 65536;

            SocketFactory sf = null;

            if( target_ssl )
            {
                sf = ssl_socket_factory;
            }
            else
            {
                sf = socket_factory;
            }
            
            Transport transport =
                new Client_TCP_IP_Transport( host,
                                             _port,
                                             sf );

            GIOPConnection connection = 
                new GIOPConnection( transport,
                                    request_listener,
                                    null );
            
            c = new ClientConnection( connection, orb, host_and_port );
            
            connections.put( c.getInfo(), c );

            receptor_pool.connectionCreated( connection );
        }
        
        c.incClients();
        
        return c;
    }

    public synchronized void releaseConnection( ClientConnection c )
    {
        c.decClients();
        
        if( c.hasNoMoreClients() )
        {
            c.closeConnection();

            connections.remove( c.getInfo() );
        }
    }



    public void shutdown()
    {
        /* release all open connections */
        
        for( Enumeration e = connections.elements(); e.hasMoreElements(); )
        {
            ((ClientConnection) e.nextElement()).closeConnection();
        }
        
        Debug.output(3,"ConnectionManager shut down (all connections released)");
        
        connections.clear();
    }
}
