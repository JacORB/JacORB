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

package org.jacorb.orb.connection;

import java.io.*;
import java.net.*;

import org.jacorb.util.*;
import org.jacorb.orb.factory.*;

/**
 * Client_TCP_IP_Transport.java
 *
 *
 * Created: Sun Aug 12 20:56:32 2001
 *
 * @author Nicolas Noffke
 * @version $Id$
 */

public class Client_TCP_IP_Transport 
    extends TCP_IP_Transport 
{
    private String target_host = null;
    private int target_port = -1;
    private SocketFactory socket_factory = null;
    private int timeout = 0;
    private String connection_info = null;

    private Socket socket = null;

    private boolean terminated = false;
    private boolean connected = false;
    
    public Client_TCP_IP_Transport( String target_host,
                                    int target_port,
                                    SocketFactory socket_factory )
    {
        this.target_host = target_host;
        this.target_port = target_port;
        this.socket_factory = socket_factory;

        connection_info = target_host + ':' + target_port;

        //get the client-side timeout property value
        String prop = 
            Environment.getProperty( "jacorb.connection.client_timeout" );
        
        if( prop != null )
        {
            try
            {
                timeout = Integer.parseInt( prop );
            }
            catch( NumberFormatException nfe )
            {
                Debug.output( 1, "Unable to create int from string >" +
                              prop + '<' );
                Debug.output( 1, "Please check property \"jacorb.connection.client_timeout\"" );
            }
        }
    }
    
    protected synchronized void waitUntilConnected()
        throws IOException
    {
        while( ! connected && 
               ! terminated )
        {
            try
            {
                wait();
            }
            catch( InterruptedException ie )
            {
            }
        }
        
        if( terminated )
        {
            throw new CloseConnectionException();
        }
     }

    protected synchronized void connect()
    {
        if( ! connected )
        {
            Debug.output(1,"Trying to connect to " + 
                         connection_info );
            
            int retries = Environment.noOfRetries();

            while( retries > 0 ) 
            {
                try 
                {
                    //noffke: by now, the factory knows if to provide
                    //ssl or not
                    socket = socket_factory.createSocket( target_host, 
                                                          target_port );
                    
                    socket.setTcpNoDelay( true );
                    
                    if( timeout != 0 )
                    {
                        /* re-set the socket timeout */
                        socket.setSoTimeout( timeout );
                    }

                    in_stream = socket.getInputStream();
                    
                    out_stream = socket.getOutputStream();

                    Debug.output( 2, "Succeeded to connect to " +
                                  connection_info +
                                  ( socket_factory.isSSL( socket ) ? " via SSL" : "" ));

                    connected = true;

                    notifyAll();

                    return;
                } 
                catch ( IOException c ) 
                { 
                    Debug.output(1,"Retrying to connect to " + 
                                 connection_info );
                    try 
                    {
                        Thread.sleep( Environment.retryInterval() );
                    } 
                    catch ( InterruptedException i ){}

                    retries--;
                }
            }

            if( retries == 0 )
            {
                throw new org.omg.CORBA.TRANSIENT("Retries exceeded, couldn't reconnect to " + 
                                                  connection_info );
            }
        }
    }

    /**
     * close TCP connection down.
     */
    public synchronized void connectionTerminated()
    {
        //means: "close transport"
        try
        {
            transportClosed();
        }
        catch( CloseConnectionException cce )
        {
            //can't happen
        }

        Debug.output( 2, "Closed client-side TCP/IP transport to " +
                      connection_info );

        terminated = true;            
        notifyAll();
    }

    protected synchronized void transportClosed()
        throws CloseConnectionException
    {
        if( terminated )
        {
            //this will be propagated up to the MessageReceptor thread
            //who will stop trying to receive messages on this
            //connection
            throw new CloseConnectionException();
        }

        if( ! connected )
        {
            //do nothing if already closed
            return;
        }
/*
        if( out_stream != null )
        {
            try
            {
                out_stream.close();
            }
            catch( IOException ioe )
            {
                Debug.output( 1, ioe );
            }
        }

        if( in_stream != null )
        {
            try
            {
                in_stream.close();
            }
            catch( IOException ioe )
            {
                Debug.output( 1, ioe );
            }
        }
*/
        if( socket != null )
        {
            try
            {
                socket.close();
                
                socket.shutdownInput();
                socket.shutdownOutput();
            }
            catch( IOException ioe )
            {
                Debug.output( 1, ioe );
            }
        }
        
        connected = false;
    }
    
    public boolean isSSL()
    {
        return socket_factory.isSSL( socket );
    }
}// Client_TCP_IP_Transport



