package org.jacorb.orb.connection;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2002  Gerald Brose.
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
 * This class manages Transports. On the one hand it creates them, and
 * on the other it enforces an upper limit on the open transports.
 *
 * @author Nicolas Noffke
 * @version $Id$
 * */

public class TransportManager
{    
    public static final String FACTORY_PROP = "jacorb.net.socket_factory";

    //private List client_transports = null;
    private List server_transports = null;

    private SocketFactory socket_factory = null;
    private SocketFactory ssl_socket_factory = null;

    private int max_server_transports = 0;

    private SelectionStrategy selection_strategy = null;
    private Class statistics_provider_class = null;

    private int wait_for_idle_interval = 0;

    public TransportManager( ORB orb )
    {
        socket_factory = SocketFactoryManager.getSocketFactory (orb);

        if( Environment.isPropertyOn( "jacorb.security.support_ssl" ))
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

        //client_transports = new LinkedList(); 
        server_transports = new LinkedList(); 
        
        max_server_transports = 
            Environment.getIntPropertyWithDefault( "jacorb.connection.max_server_transports",
                                        Integer.MAX_VALUE );
        
        selection_strategy = (SelectionStrategy)
            Environment.getObjectProperty( "jacorb.connection.selection_strategy_class" );
        
        if( Environment.hasProperty( "jacorb.connection.statistics_provider_class" ))
        {
            String s = Environment.getProperty( "jacorb.connection.statistics_provider_class" );

            if( s != null && s.length() > 0 )
            {
                try
                {
                    statistics_provider_class =
                        Class.forName( s );
                }
                catch( Exception e )
                {
                    Debug.output( 1, "ERROR: Unable to create class from property >jacorb.connection.statistics_provider_class<: " + e );
                               
                }
            }
        }

        wait_for_idle_interval =
            Environment.getIntPropertyWithDefault( "jacorb.connection.wait_for_idle_interval", 500 );
    }

    public Transport createClientTransport( String target_host,
                                            int target_port )
    {
        Transport transport =
            new Client_TCP_IP_Transport( target_host,
                                         target_port,
                                         socket_factory,
                                         null,
                                         this );

        return transport;
    }

    /*
    public void unregisterClientTransport( Transport transport )
    {
        for( Iterator it = client_transports.iterator();
             it.hasNext();
             )
        {
            if( it.next() == transport )
            {
                it.remove();
                return;
            }
        }        
    }
    */

    public Transport createServerTransport( Socket socket,
                                            boolean is_ssl )
        throws IOException
    {
        //if too many open transports, shut one down
        if( server_transports.size() >= max_server_transports )
        {
            if( selection_strategy != null )
            {
                while( server_transports.size() >= max_server_transports )
                {
                    Transport to_close = null;

                    synchronized( server_transports )
                    {
                        to_close = 
                            selection_strategy.selectForClose( server_transports );
                    }
                    
                    if( to_close != null &&
                        ((Server_TCP_IP_Transport) to_close).tryShutdown() )
                    {
                        break;
                    }
                    else
                    {
                        try
                        {
                            Thread.sleep( wait_for_idle_interval );
                        }
                        catch( Exception e )
                        {
                            Debug.output( 1, e );
                        }
                    }
                }
            }
            else
            {
                Debug.output( 1, "ERROR: no of max server transports set, but no SelectionStrategy present" );
            }
        }

        //create a new statistics provider for each new Transport
        StatisticsProvider provider = null;
        if( statistics_provider_class != null )
        {
            try
            {
                provider = (StatisticsProvider) 
                    statistics_provider_class.newInstance();
            }
            catch( Exception e )
            {
                Debug.output( 1, "ERROR: Unable to create instance from Class >" +
                              statistics_provider_class + '<');
                
            }
        }

        Transport transport = 
            new Server_TCP_IP_Transport( socket, 
                                         is_ssl,
                                         provider,
                                         this );

        synchronized( server_transports )
        {
            server_transports.add( transport );
        }

        return transport;
    }

    public void unregisterServerTransport( Transport transport )
    {
        synchronized( server_transports )
        {
            server_transports.remove( transport );
        }
    }
}



