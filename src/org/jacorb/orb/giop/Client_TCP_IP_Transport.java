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

package org.jacorb.orb.connection;

import java.io.*;
import java.net.*;
import java.util.*;

import org.jacorb.util.*;
import org.jacorb.orb.*;
import org.jacorb.orb.factory.*;

import org.omg.CORBA.COMM_FAILURE;

/**
 * Client_TCP_IP_Transport.java
 *
 *
 * Created: Sun Aug 12 20:56:32 2002
 *
 * @author Nicolas Noffke
 * @version $Id$
 */

public class Client_TCP_IP_Transport
    extends TCP_IP_Transport
{
    private InternetIOPProfile target_profile;
    private boolean use_ssl = false;
    private SocketFactory socket_factory = null;
    private int timeout = 0;
    private int sslPort = -1;

    private boolean closed = false;
    private boolean connected = false;

    //for testing purposes only: # of open transports
    //used by org.jacorb.test.orb.connection[Client|Server]ConnectionTimeoutTest
    public static int openTransports = 0;

    public Client_TCP_IP_Transport( InternetIOPProfile target_profile,
                                    boolean use_ssl,
                                    SocketFactory socket_factory,
                                    StatisticsProvider statistics_provider,
                                    TransportManager transport_manager )
    {
        super( statistics_provider, transport_manager );

        this.target_profile = target_profile;
        this.use_ssl        = use_ssl;
        this.socket_factory = socket_factory;

        if (use_ssl)
        {
            sslPort = target_profile.getSSL().port;
            if (sslPort < 0)
                sslPort += 65536;
            connection_info = target_profile.getAddress().getHost() + ':' + sslPort;
        }
        else
        {
            connection_info = target_profile.getAddress().toString();
        }

        //get the client-side timeout property value
        String prop =
            Environment.getProperty( "jacorb.connection.client_idle_timeout" );

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
                Debug.output( 1, "Please check property \"jacorb.connection.client_idle_timeout\"" );
            }
        }
    }

    protected synchronized boolean waitUntilConnected()
    {
        while( ! connected &&
               ! closed )
        {
            try
            {
                wait();
            }
            catch( InterruptedException ie )
            {
            }
        }

        return ! closed;
     }

    protected synchronized void connect()
    {
        if( ! connected )
        {
            Debug.output(3, "Trying to connect to " +
                         connection_info );

            int retries = Environment.noOfRetries();

            while( retries >= 0 )
            {
                try
                {
                    socket = createSocket();

                    //                    socket.setTcpNoDelay( true );

                    if( timeout != 0 )
                    {
                        /* re-set the socket timeout */
                        socket.setSoTimeout( timeout );
                    }

                    in_stream =
                        socket.getInputStream();

                    out_stream =
                        new BufferedOutputStream( socket.getOutputStream());

                    Debug.output( 1, "Connected to " +
                                  connection_info +
                                  " from local port " +
                                  socket.getLocalPort() +
                                  ( socket_factory.isSSL( socket ) ? " via SSL" : "" ));

                    connected = true;

                    notifyAll();

                    //for testing purposes
                    ++openTransports;

                    return;
                }
                catch ( IOException c )
                {
                    Debug.output( 3, c );

                    //only sleep and print message if we're actually
                    //going to retry
                    if( retries >= 0 )
                    {
                        Debug.output( 1, "Retrying to connect to " +
                                      connection_info );
                        try
                        {
                            Thread.sleep( Environment.retryInterval() );
                        }
                        catch( InterruptedException i )
                        {
                        }
                    }

                    retries--;
                }
            }

            if( retries < 0 )
            {
                throw new org.omg.CORBA.TRANSIENT("Retries exceeded, couldn't reconnect to " +
                                                  connection_info );
            }
        }
    }

    /**
     * Tries to create a socket connection to any of the addresses in
     * the target profile, starting with the primary IIOP address,
     * and then any alternate IIOP addresses that have been specified.
     */
    private Socket createSocket() throws IOException
    {
        Socket      result    = null;
        IOException exception = null;

        List addressList = new ArrayList();
        addressList.add    (target_profile.getAddress());
        addressList.addAll (target_profile.getAlternateAddresses());
        
        Iterator addressIterator = addressList.iterator();
        
        while (result == null && addressIterator.hasNext())
        {
            try
            {
                IIOPAddress address = (IIOPAddress)addressIterator.next();
                if (use_ssl)
                    address = new IIOPAddress (address.getHost(), sslPort);
                result = socket_factory.createSocket
                (
                    address.getHost(), address.getPort()
                );
                connection_info = address.toString();
            }
            catch (IOException e)
            {
                exception = e;
            }
        }

        if (result != null)
            return result;
        else if (exception != null)
            throw exception;
        else
            throw new IOException
                        ("connection failure without exception");        
    }

    public synchronized void closeCompletely()
        throws IOException
    {
        closeSocket();

        Debug.output( 2, "Closed client-side TCP/IP transport to " +
                      connection_info + " terminally");

        //terminate this transport
        closed = true;
        notifyAll();
    }
    
    public synchronized void closeAllowReopen()
        throws IOException
    {
        closeSocket();

        Debug.output( 2, "Closed client-side TCP/IP transport to " +
                      connection_info + " non-terminally (can be reopened)");
    }

    /**
     * Close socket layer down.
     */
    private void closeSocket()
        throws IOException
    {
        if (connected && socket != null)
        {
            socket.close ();

            //this will cause exceptions when trying to read from
            //the streams. Better than "nulling" them.
            if( in_stream != null )
            {
                in_stream.close();
            }

            if( out_stream != null )
            {
                out_stream.close();
            }

            //for testing purposes
            --openTransports;
        }

        connected = false;
    }

    public boolean isSSL()
    {
        return socket_factory.isSSL( socket );
    }
    
    public InternetIOPProfile get_server_profile()
    {
        return target_profile;
    }
    
}// Client_TCP_IP_Transport
