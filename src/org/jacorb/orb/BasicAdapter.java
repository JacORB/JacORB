/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2000  Gerald Brose.
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
import org.jacorb.orb.connection.http.*;
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

    /**
     * The new BasicAdapter will create a Listener and, if the environment
     * supports it also
     */

    public BasicAdapter( org.jacorb.orb.ORB orb, POA rootPOA)
    {
        this.orb = orb;
        this.rootPOA = rootPOA;

        if( Environment.supportSSL() )
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

        if ( org.jacorb.util.Environment.supportSSL()) 
        {
            sslListener =
                new Listener( orb,
                              rootPOA,
                              Environment.getProperty( "OASSLPort" ),
                              ssl_socket_factory );

            sslListener.is_ssl = true;

            Debug.output( 1, "SSL Listener on port = " + sslListener.port );
        }


        if( org.jacorb.util.Environment.enforceSSL() )
        {
            /* gb: sanity check: requiring SSL requires supporting it */
            if( !jacorb.util.Environment.supportSSL ())
            {
                throw new java.lang.Error("SSL required but not supported, cannot continue!");
            }
        }

        /*
         * we always create a plain socket listener as well,
         * if SSL is required, we do not accept requests on this port, however
         * (see below)
         */

        listener = new Listener( orb, 
                                 rootPOA, 
                                 Environment.getProperty( "OAPort" ),
                                 socket_factory );

        String prop = 
            org.jacorb.util.Environment.getProperty("jacorb.connection.server_timeout");

        if( prop != null )
        {
            setTimeout(Integer.parseInt(prop));
        } 
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

    /**
     * @returns the IP address we are listening on
     */

    public String getAddress()
    {
        return listener.getAddress();
    }

    /**
     * Set the server-side socket timeout. The socket will
     * be closed after timeout msecs. inactivity.
     */

    public void setTimeout( int timeout )
    {
        listener.setTimeout( timeout );
    }
    
    /**
     * to be called from the POA, code duplicated for performance reasons to avoid
     * synchronization in the private version of this method.
     */

    public synchronized void deliverRequest( org.jacorb.orb.dsi.ServerRequest request, 
                                             org.omg.PortableServer.POA poa )
    {
        org.jacorb.poa.POA tmp_poa = ( org.jacorb.poa.POA)poa;
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
                    /* if one of the POAs is in holding state, we simply deliver 
                       deliver the request to this POA. It will forward the request
                       to its child POAs if necessary when changing back to active
                       For the POA to be able to forward this request to its child POAa,
                       we need to supply the remaining part of the child's POA name */

                    String [] rest_of_name = new String[scopes.length - i];
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
                (( org.jacorb.poa.POA)tmp_poa)._invoke( request );
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

    public synchronized void return_result( org.jacorb.orb.dsi.ServerRequest request)
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

    static class Listener
        extends Thread
    {
        java.net.ServerSocket serverSocket;
        int port = 0;
        String address;
        int timeout = 0;
        org.jacorb.orb.ORB orb;
        POA rootPOA;
        private boolean is_ssl = false;

        private org.jacorb.orb.factory.ServerSocketFactory factory = null;

        private boolean do_run = true;

        public Listener( org.jacorb.orb.ORB orb,
                         POA poa,
                         String oa_port,
                         org.jacorb.orb.factory.ServerSocketFactory factory )

        {
            this.orb = orb;
            rootPOA = poa;

            this.factory = factory;

            if( factory == null )
                throw new java.lang.Error("No socket factory available!");

            try
            {
                String ip_addr = org.jacorb.util.Environment.getProperty("OAIAddr");

                if( ip_addr == null)
                {
                    if( oa_port != null )
                        serverSocket = factory.createServerSocket ( Integer.parseInt( oa_port));
                    else
                        serverSocket = factory.createServerSocket ( 0 );

                    address = java.net.InetAddress.getLocalHost().toString();
                    if( address.indexOf("/") > 0 )
                    {
                        address = address.substring(address.indexOf("/")+1);
                    }
                }
                else
                {
                    InetAddress target_addr = InetAddress.getByName( ip_addr );

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
                            factory.createServerSocket( Integer.parseInt( oa_port), 20, target_addr );
                    }
                    else
                    {
                        serverSocket = factory.createServerSocket ( 0, 20, target_addr );
                    }
                    address = ip_addr;
                }
                port = serverSocket.getLocalPort();
            } 
            catch (Exception e) 
            {
                org.jacorb.util.Debug.output(2,e);
                System.err.println("[ Listener: Couldn't initialize. Illegal address configuration? ]");
                System.exit(1);
            }

            this.setName("JacORB Listener Thread on port " + port );
            setDaemon(true);
            start();
        }

        public int getPort()
        {
            return port;
        }

        public String getAddress()
        {
            return address;
        }

        public void setTimeout( int timeout )
        {
            this.timeout = timeout;
        }
    
               
        public void run() 
        {
            // setPriority(Thread.MAX_PRIORITY);
            while( do_run )
            {
                try
                {
                    new RequestReceptor( orb, 
                                         rootPOA, 
                                         serverSocket.accept(), 
                                         timeout,
                                         (ssl_socket_factory == null)? false : 
                                         ssl_socket_factory.isSSL( serverSocket ));
                } 
                catch (Exception e)
                {
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

    /** 
     * Inner class RequestReceptor, instantiated only by Listener
     * receives messages. There is one object of this class per 
     * client connection.
     */

    static class RequestReceptor
        extends Thread
    {
        private java.net.Socket clientSocket;
        private org.jacorb.orb.connection.ServerConnection connection;
        private int timeout;
        private org.jacorb.orb.ORB orb;
        private POA rootPOA;
        private boolean done = false;
  	private static final byte[] HTTPPostHeader = {(byte) 'P', (byte) 'O', (byte) 'S', (byte) 'T'};
	private static final int HEADER_SIZE = 4;
        private boolean is_ssl; // bnv

        public RequestReceptor( org.jacorb.orb.ORB orb, 
                                POA rootPOA, 
                                java.net.Socket s, 
                                int timeout,
                                boolean is_ssl )
        {
            this.orb = orb;
            this.rootPOA = rootPOA;       
            clientSocket = s;

            this.is_ssl = is_ssl;

            if( is_ssl )
            {
                ssl_socket_factory.switchToClientMode( s );
            }

            this.timeout = timeout;
            InetAddress remote_host = s.getInetAddress();
            InetAddress local_host = null;
            try
            {
                local_host = InetAddress.getLocalHost();
            } 
            catch ( UnknownHostException uhe )
            {
                // debug:
                uhe.printStackTrace();
            }

            this.setName("JacORB Request Receptor Thread on " + local_host );
            this.start();
        }


        /**
         *      receive and dispatch requests 
         */

        public void run() 
        {
            /* set up a connection object */        
            try
            {
                //check incoming connection type (GIOP vs. HTTP)
		InputStream in_stream = null;
                in_stream = new BufferedInputStream(clientSocket.getInputStream());

                byte data[] = new byte[HEADER_SIZE];
		boolean isHTTP=true;
		try 
                {
                    in_stream.mark(HEADER_SIZE);
                    int b;
                    int length = 0;
                    for (int i = 0; i < HEADER_SIZE; i++) 
                    {
                        b = in_stream.read();
                        if (b < 0) 
                        {
                            close();
                            return;
                        }                                        
                        data[i] = (byte) b;
                    }
		} 
                catch (IOException ioe) 
                {
                    org.jacorb.util.Debug.output(1, "Can not read from socket");                            
                    ioe.printStackTrace();
                
		}
                finally
                {
                    in_stream.reset();
        	}     

		for (int i = 0; i < HEADER_SIZE; i++) 
                {
                    isHTTP = (isHTTP && (data[i] == HTTPPostHeader[i]));               
		}                 


		if (isHTTP)
                {
                    org.jacorb.util.Debug.output(2,"Incoming HTTP Request");
                    connection = 
                        new org.jacorb.orb.connection.http.ServerConnection( orb, 
                                                                         is_ssl,
                                                                         clientSocket,
                                                                         in_stream );
		}
                else
                {
                    org.jacorb.util.Debug.output(2,"Incoming GIOP Request");
                    connection = 
                        new org.jacorb.orb.connection.ServerConnection( orb, 
                                                                    is_ssl,
                                                                    clientSocket,
                                                                    in_stream );
		}

                if( timeout != 0 )
                {
                    try
                    {
                        connection.setTimeOut( timeout );
                    } 
                    catch( SocketException s )
                    {
                        s.printStackTrace();
                    }
                }

            }
            catch(java.io.IOException ioex)
            {
                org.jacorb.util.Debug.output(2, ioex); 
                org.jacorb.util.Debug.output(0,"Error in " + (is_ssl? "SSL ":"") + "session setup.");
                return;
            }

            /* receive requests */
            try
            {           
                while( !done ) 
                {
                    byte [] buf = connection.readBuffer();
                    
                    // debug:
                    //System.out.println("BasicAdapter: got Buffer");
                    
                    /* let the message-level interceptors do their job */

                    int msg_type = buf[7];
                    //if( Environment.serverInterceptMessages())
                    //buf = (( org.jacorb.orb.ORB)orb).server_messageIntercept_pre( buf );
                    
                    switch( msg_type )
                    {
                    case org.omg.GIOP.MsgType_1_0._Request:
                        {
                            // bnv: default SSL security policy
                            if( org.jacorb.util.Environment.enforceSSL() && 
                                !connection.isSSL()) 
                            {
                                org.jacorb.orb.dsi.ServerRequest request = 
                                    new org.jacorb.orb.dsi.ServerRequest( orb, buf, connection );
                                request.setSystemException (
                                      new org.omg.CORBA.NO_PERMISSION ( 
                                             "Connection should be SSL, but isn't",
                                             3,  
                                             // SERVER_POLICY
                                             org.omg.CORBA.CompletionStatus.COMPLETED_NO
                                             )
                                          );
                                request.reply();
                            } 
                            else 
                            {
                                // bnv: as before
                                org.jacorb.orb.dsi.ServerRequest request = 
                                    new org.jacorb.orb.dsi.ServerRequest(orb, buf, connection );
                                orb.getBasicAdapter().replyPending();
                                
                                // devik: look for codeset context if not negotiated yet
                                if(!connection.isTCSNegotiated())
                                {
                                    // look for codeset service context
                                    connection.setServerCodeSet(request.getServiceContext());
                                }
                                
                                deliverRequest( request );
                            }
                            break;
                        } 
                    case org.omg.GIOP.MsgType_1_0._CancelRequest:
                        {
                            //  org.omg.GIOP.CancelRequestHeader cancel_req_hdr =
                            //  org.omg.GIOP.CancelRequestHeaderHelper.read( ois );                     
                            break;
                        }
                    case org.omg.GIOP.MsgType_1_0._LocateRequest:
                        {
                            org.jacorb.orb.connection.LocateRequest request = 
                                new org.jacorb.orb.connection.LocateRequest(orb, buf, connection );
                            deliverRequest( request );
                            break;
                        }
                    default:
                        {
                            org.jacorb.util.Debug.output(0,"SessionServer, message_type " + 
                                                     msg_type + " not understood.");
                        }
                    }
                } 
            }
            //      catch ( java.io.InterruptedIOException eof )
            //          {
            //              org.jacorb.util.Debug.output(2,"RequestReceptor: Connection timed out");

            //              while( pendingReplies > 0 )
            //                  try{ sleep( 5 ); } catch ( Exception e ){}
            //              connection.sendCloseConnection();
            //          }
            catch ( java.io.EOFException eof )
            {
                org.jacorb.util.Debug.output(4,eof);
		close();
		
            } 
            catch ( org.omg.CORBA.COMM_FAILURE cf )
            {
                org.jacorb.util.Debug.output(1,cf);
                close();
            } 
            catch ( java.io.IOException i )
            {
                org.jacorb.util.Debug.output(4,i);
                close();
            }
	   
        }       
        
        public void close() 
        {
            try
            {

                if( clientSocket != null )
                {
                    if (connection != null){
                        connection.closeConnection();
		    }
                    clientSocket.close();
                }
            } 
            catch ( Exception e ) 
            {
                org.jacorb.util.Debug.output(2,e);
                // ignore exceptions on closing sockets which would occur e.g.
                // when closing sockets without ever having opened one...
            }
            done = true;
        }


        /* private code */

        private void deliverRequest( org.jacorb.orb.dsi.ServerRequest request )
        {
            org.jacorb.poa.POA tmp_poa = ( org.jacorb.poa.POA)rootPOA;
        
            try
            {
                //              String obj_key = new String(request.objectKey());
                String poa_name = org.jacorb.poa.util.POAUtil.extractPOAName(request.objectKey());

                /** strip scoped poa name (first part of the object key before "::",
                 *  will be empty for the root poa
                 */
                /*
                  if( !(obj_key.startsWith( org.jacorb.poa.POAConstants.OBJECT_KEY_SEPARATOR+
                  org.jacorb.poa.POAConstants.OBJECT_KEY_SEPARATOR)))
                  {
                  poa_name = obj_key.substring(0,
                  obj_key.indexOf( org.jacorb.poa.POAConstants.OBJECT_KEY_SEPARATOR+
                  org.jacorb.poa.POAConstants.OBJECT_KEY_SEPARATOR) );
                  }
                */
                java.util.StringTokenizer strtok = 
                    new java.util.StringTokenizer(poa_name, org.jacorb.poa.POAConstants.OBJECT_KEY_SEPARATOR );

                String scopes[]  = new String[strtok.countTokens()];

                for( int i = 0; strtok.hasMoreTokens(); scopes[i++] = strtok.nextToken() );

                for( int i = 0; i < scopes.length; i++)
                {
                    if( scopes[i].equals(""))
                        break;

                    /* the following is a call to a method in the private
                       interface between the ORB and the POA. It does the
                       necessary synchronization between incoming,
                       potentially concurrent requests to activate a POA
                       using its adapter activator. This call will block
                       until the correct POA is activated and ready to
                       service requests. Thus, concurrent calls
                       originating from a single, multi-threaded client
                       will be serialized because the thread that accepts
                       incoming requests from the client process is
                       blocked. Concurrent calls from other destinations
                       are not serialized unless they involve activating
                       the same adapter.  */

                    try
                    {
                        tmp_poa = tmp_poa._getChildPOA( scopes[i] );
                    }
                    catch ( org.jacorb.poa.except.ParentIsHolding p )
                    {
                        /* if one of the POAs is in holding state, we simply deliver 
                           deliver the request to this POA. It will forward the request
                           to its child POAs if necessary when changing back to active
                           For the POA to be able to forward this request to its child POAa,
                           we need to supply the remaining part of the child's POA name */

                        String [] rest_of_name = new String[scopes.length - i];
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
                    (( org.jacorb.poa.POA)tmp_poa)._invoke( request );
                }
              
            }
            //      catch( org.omg.PortableServer.POAPackage.AdapterNonExistent ane )
            //{
            //request.setSystemException( new org.omg.CORBA.OBJECT_NOT_EXIST("POA: AdapterNonExistent"));
            //request.reply();
            //}
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

    }


}
