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

package org.jacorb.orb;

/**
 * 
 * Class BasicAdapter, used by the POA.
 *
 * @author Gerald Brose, FU Berlin
 * @version $Id$
 */

import java.io.*;
import java.net.*;
import java.util.*;

import java.lang.reflect.Constructor;

import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;

import org.jacorb.orb.factory.*;
import org.jacorb.orb.connection.*;
import org.jacorb.util.*;

public class BasicAdapter
{
    private static SSLServerSocketFactory ssl_socket_factory = null;
    private static ServerSocketFactory socket_factory = null;
    private static SocketFactory client_socket_factory = null;

    static
    {
        socket_factory = new ServerSocketFactory(){
            public ServerSocket createServerSocket ( int port )
            throws IOException
            {
                return new ServerSocket( port );
            }

            public ServerSocket createServerSocket( int port,
                                                    int backlog )
            throws IOException
            {
                return new ServerSocket( port, backlog );
            }
            public ServerSocket createServerSocket( int port,
                                                    int backlog,
                                                    InetAddress ifAddress )
            throws IOException
            {
                return new ServerSocket( port, backlog, ifAddress );
            }
        };
    }
        

    /** the number of outstanding replies. */
    private  int pendingReplies = 0;

    private  org.jacorb.orb.ORB orb; 
    private  POA rootPOA; 
    private  Listener listener;
    private  Listener sslListener; // bnv

    private MessageReceptorPool receptor_pool = null;
    private RequestListener request_listener = null;
    private ReplyListener reply_listener = null;
    private int timeout = 0;

    public BasicAdapter(org.jacorb.orb.ORB orb, POA rootPOA)
    {
        this.orb = orb;
        this.rootPOA = rootPOA;

        if( Environment.isPropertyOn( "jacorb.security.support_ssl" ))
        {
            if( ssl_socket_factory == null )
            {
                String s = Environment.getProperty( "jacorb.ssl.server_socket_factory" );
                if( s == null || s.length() == 0 )
                {
                    throw new RuntimeException( "SSL support is on, but the property \"jacorb.ssl.server_socket_factory\" is not set!" );
                }

                try
                {
                    Class ssl = Class.forName( s );
                    
                    Constructor constr = ssl.getConstructor( new Class[]{
                        org.jacorb.orb.ORB.class });
                    
                    ssl_socket_factory = (SSLServerSocketFactory)
                        constr.newInstance( new Object[]{ orb });
                }
                catch (Exception e)
                {
                    Debug.output( Debug.IMPORTANT | Debug.ORB_CONNECT,
                                  e );

                    throw new RuntimeException( "SSL support is on, but the ssl server socket factory can't be instanciated (see trace)!" );
                }
            }
            
            if( client_socket_factory == null )
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
                        org.jacorb.orb.ORB.class });
                    
                    client_socket_factory = (SocketFactory)
                        constr.newInstance( new Object[]{ orb });
                }
                catch (Exception e)
                {
                    Debug.output( Debug.IMPORTANT | Debug.ORB_CONNECT,
                                  e );

                    throw new RuntimeException( "SSL support is on, but the ssl socket factory can't be instanciated (see trace)!" );
                }
            }

            sslListener =
                new Listener( Environment.getProperty( "OASSLPort" ),
                              ssl_socket_factory,
                              true );

            Debug.output( 1, "SSL Listener on port " + sslListener.port );
        }
        else
        {
            if( client_socket_factory == null )
            {
                client_socket_factory = new SocketFactory(){
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
            }
        }

        receptor_pool = MessageReceptorPool.getInstance();
        request_listener = new ServerRequestListener( orb, rootPOA );
        reply_listener = new NoBiDirServerReplyListener();

        /*
         * We always create a plain socket listener as well. If SSL is
         * required, we do not accept requests on this port, however
         * (see below). 
         */

        listener = new Listener( Environment.getProperty( "OAPort" ),
                                 socket_factory,
                                 false );

        String prop = 
            Environment.getProperty("jacorb.connection.server_timeout");

        if( prop != null )
        {
            timeout = Integer.parseInt(prop);
        } 
    }

    public RequestListener getRequestListener()
    {
        return request_listener;
    }

    public void replyPending()
    {
        pendingReplies++;
    }

    public int getPort()
    {
        return listener.getPort();
    }

    public int getSSLPort()
    {
        return sslListener.getPort();
    }

    public boolean hasSSLListener()
    {
        return sslListener != null;
    }

    /**
     * @returns the IP address we are listening on
     */

    public String getAddress()
    {
        return listener.getAddress();
    }
    
    /**
     * to be called from the POA, code duplicated for performance
     * reasons to avoid synchronization in the private version of this
     * method.  
     */
    public synchronized void deliverRequest( org.jacorb.orb.dsi.ServerRequest request, 
                                             org.omg.PortableServer.POA poa )
    {
        org.jacorb.poa.POA tmp_poa = (org.jacorb.poa.POA)poa;
        String scopes[] = request.remainingPOAName();

        try
        {
            for( int i=0; i < scopes.length-1; i++)
            {
                if( scopes[i].equals(""))
                {
                    request.setRemainingPOAName(null);
                    break;
                }
                try
                {
                    tmp_poa = tmp_poa._getChildPOA( scopes[i] );
                }
                catch ( org.jacorb.poa.except.ParentIsHolding p )
                {
                    /* 
                     * if one of the POAs is in holding state, we
                     * simply deliver deliver the request to this
                     * POA. It will forward the request to its child
                     * POAs if necessary when changing back to active
                     * For the POA to be able to forward this request
                     * to its child POAa, we need to supply the
                     * remaining part of the child's POA name 
                     */
                    String[] rest_of_name = new String[scopes.length - i];
                    for( int j = 0; j < i; j++ )
                        rest_of_name[j] = scopes[j+i];
                    request.setRemainingPOAName(rest_of_name);
                    break;
                }               
            }         

            if( tmp_poa == null )
            {
                throw new Error("request POA null!");
            }
            else
            {
                /* hand over to the POA */
                ((org.jacorb.poa.POA)tmp_poa)._invoke( request );
            }
              
        }
        catch( org.omg.PortableServer.POAPackage.WrongAdapter wa )
        {
            // unknown oid (not previously generated)
            request.setSystemException( new org.omg.CORBA.OBJECT_NOT_EXIST("unknown oid") );
            request.reply();
        }
        catch( org.omg.CORBA.SystemException one )
        {
            request.setSystemException( one );
            request.reply();
        }
        catch( Throwable th )
        {
            request.setSystemException( new org.omg.CORBA.UNKNOWN( th.toString()) );
            request.reply();
            th.printStackTrace(); // TODO
        }                       
    }

    /** 
     * to be called from the POA
     */

    public synchronized void return_result(org.jacorb.orb.dsi.ServerRequest request)
    {
        pendingReplies--;
        request.reply();
    }

    public void stopListeners()
    {
        listener.doStop();
        
        if( sslListener != null )
        {
            sslListener.doStop();
        }
    }

    /**
     * Inner class Listener, responsible for accepting connection requests
     */

    class Listener
        extends Thread
    {
        private ServerSocket serverSocket = null;

        private int port = 0;
        private String address_string = null;

        private boolean is_ssl = false;

        private ServerSocketFactory factory = null;

        private boolean do_run = true;

        public Listener( String oa_port, 
                         ServerSocketFactory factory,
                         boolean is_ssl )

        {
            if( factory == null )
            {
                throw new Error("No socket factory available!");
            }

            this.factory = factory;
            this.is_ssl = is_ssl;

            try
            {
                String ip_addr = Environment.getProperty("OAIAddr");

                if( ip_addr == null)
                {
                    if( oa_port != null )
                    {
                        serverSocket = 
                            factory.createServerSocket( 
                                Integer.parseInt( oa_port ));
                    }
                    else
                    {
                        serverSocket = factory.createServerSocket( 0 );
                    }

                    
                    setAddress( InetAddress.getLocalHost() );
                }
                else
                {
                    InetAddress target_addr = 
                        InetAddress.getByName( ip_addr );

                    if( target_addr == null )
                        target_addr = InetAddress.getLocalHost();

                    if( target_addr == null )
                    {
                        System.err.println("[ Listener: Couldn't initialize, illegal ip addr " + 
                                           ip_addr +" ]");
                        System.exit(1);
                    }

                    if( oa_port != null )
                    {
                        serverSocket = 
                            factory.createServerSocket( Integer.parseInt( oa_port), 
                                                        20, 
                                                        target_addr );
                    }
                    else
                    {
                        serverSocket = 
                            factory.createServerSocket( 0, 20, target_addr );
                    }

                    setAddress( target_addr );
                }

                port = serverSocket.getLocalPort();                
            } 
            catch (Exception e) 
            {
                Debug.output(2,e);
                System.err.println("[ Listener: Couldn't initialize. Illegal address configuration? ]");
                System.exit(1);
            }

            if( ssl_socket_factory == null )
            {
                //can't be SSL, if no corresponding factory is present
                is_ssl = false; 
            }
            else
            {
                //let the factory decide
                is_ssl = ssl_socket_factory.isSSL( serverSocket );
            }

            this.setName("JacORB Listener Thread on port " + port );
            setDaemon(true);
            start();
        }

        public int getPort()
        {
            return port;
        }

        private void setAddress( InetAddress addr )
        {
            address_string = 
                org.jacorb.orb.dns.DNSLookup.inverseLookup( addr );

            if( address_string == null )
            {
                address_string = addr.toString();

                if( address_string.indexOf( "/" ) > 0 )
                {
                    address_string = 
                        address_string.substring( 
                              address_string.indexOf( "/" ) + 1 );
                }
            }

            Debug.output( 2, "Set BasicListener address string to " +
                          address_string );            
        }

        private void setAddress( String ip )
        {
            address_string = 
                org.jacorb.orb.dns.DNSLookup.inverseLookup( ip );

            if( address_string == null )
            {
                address_string = ip;
            }

            Debug.output( 2, "Set BasicListener address string to " +
                          address_string );            
        }

        public String getAddress()
        {
            return address_string;
        }
                   
        public void run() 
        {
            // setPriority(Thread.MAX_PRIORITY);
            while( do_run )
            {
                try
                {                                      
                    Socket socket = serverSocket.accept();

                    if( timeout > 0 )
                    {
                        socket.setSoTimeout(timeout);
                    }

                    Transport transport = 
                        new Server_TCP_IP_Transport( socket, is_ssl );

                    GIOPConnection connection = 
                        new GIOPConnection( transport,
                                            request_listener,
                                            reply_listener );

                    receptor_pool.connectionCreated( connection );
                } 
                catch( Exception e )
                {
                    if( do_run )
                        Debug.output( Debug.IMPORTANT | Debug.ORB_CONNECT, e );
                }
            }
                        
            Debug.output( Debug.INFORMATION | Debug.ORB_CONNECT, 
                          "Listener exited");
        }               
        
        public void doStop()
        {
            do_run = false;
            
            try
            {
                serverSocket.close();
            }
            catch( java.io.IOException e )
            {
                Debug.output( Debug.INFORMATION | Debug.ORB_CONNECT, e );
            }
        }            
    }
}






