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

import java.net.*;
import java.io.*;

import org.jacorb.util.*;

/**
 * TCP_IP_Transport.java
 *
 *
 * Created: Sun Aug 12 20:18:47 2002
 *
 * @author Nicolas Noffke
 * @version $Id$
 */

public abstract class TCP_IP_Transport
    implements Transport
{
    protected boolean connected = false;
    
    protected InputStream in_stream = null;
    protected OutputStream out_stream = null;

    private ByteArrayOutputStream b_out = null;
    private boolean dump_incoming = false;

    String connection_info;
    Socket socket;

    //used to unregister this transport
    protected TransportManager transport_manager = null;

    private int finalTimeout = 20000;
    
    public TCP_IP_Transport (TCP_IP_Transport other)
    {
        this.in_stream = other.in_stream;
        this.out_stream = other.out_stream;
        this.b_out = other.b_out;
        this.dump_incoming = other.dump_incoming;
        this.connection_info = other.connection_info;
        this.transport_manager = other.transport_manager;
        this.finalTimeout = other.finalTimeout;
    }

    public TCP_IP_Transport( TransportManager transport_manager )
    {
        this.transport_manager = transport_manager;

        String dump_outgoing =
            Environment.getProperty( "jacorb.debug.dump_outgoing_messages",
                                     "off" );

        if( "on".equals( dump_outgoing ))
        {
            b_out = new ByteArrayOutputStream();
        }

        finalTimeout = 
            Environment.getIntPropertyWithDefault( "jacorb.connection.timeout_after_closeconnection", 
                                                   20000 );
    }

    public void read (org.omg.ETF.BufferHolder data, 
                      int offset, 
                      int min_length, 
                      int max_length, 
                      long time_out)
    {
        int read = 0;

        while( read < min_length )
        {
            int n = 0;

            try
            {
                n = in_stream.read( data.value,
                                    offset + read,
                                    min_length - read );
            }
            catch( InterruptedIOException e )
            {
                int soTimeout = 0;
                try
                {
                    soTimeout = socket.getSoTimeout();
                }
                catch (SocketException ex)
                {
                    throw to_COMM_FAILURE (ex);
                }

                if (soTimeout != 0)
                {
                    Debug.output
                        (
                         2,
                         "Socket timed out with timeout period of " +
                         soTimeout
                        ); 
                    throw new org.omg.CORBA.TIMEOUT();
                }
                else
                {
                    throw new org.omg.CORBA.TRANSIENT ("Interrupted I/O: " + e);
                }
            }
            catch( IOException se )
            {
                Debug.output( 2, "Transport to " + connection_info +
                              ": stream closed" );
                throw to_COMM_FAILURE (se);
            }

            if( n < 0 )
            {
                Debug.output( 2, "Transport to " + connection_info +
                              ": stream closed" );
                throw new org.omg.CORBA.COMM_FAILURE ("read() did not return any data");
            }

            read += n;
        }
    }

    // implementation of org.jacorb.orb.connection.Transport interface


    public void write (boolean is_first,
                       boolean is_last, 
                       byte[] data,
                       int offset,
                       int length,
                       long time_out )
    {
        try
        {
            out_stream.write( data, offset, length );
            if( b_out != null )
            {
                b_out.write( data, offset, length );
            }
        }
        catch (IOException ex)
        {
            throw to_COMM_FAILURE (ex);
        }

    }


    public void flush()
    {
        try
        {
            if( b_out != null )
            {
                byte[] b = b_out.toByteArray();
                Debug.output( 1, "sendMessages()", b );
                b_out.reset();
            }
            out_stream.flush();
        }
        catch (IOException ex)
        {
            throw to_COMM_FAILURE (ex);
        }

    }
    
    public boolean is_connected()
    {
        return connected;
    }

    /**
     * This is used to tell the transport that a CloseConnection has
     * been sent, and that it should set a timeout in case the client
     * doesn't close its side of the connection right away.
     *
     * This should only be called on the thread that listens on the
     * socket because timeouts are not applied until read() is called
     * the next time.  
     */
    public void turnOnFinalTimeout()
    {
        if( socket != null )
        {
            try
            {
                socket.setSoTimeout( finalTimeout );
            }
            catch( SocketException se )
            {
                Debug.output( 2, se );
            }
        }
    }
    
    public abstract boolean isSSL();
    
    protected org.omg.CORBA.COMM_FAILURE to_COMM_FAILURE (IOException ex)
    {
        return new org.omg.CORBA.COMM_FAILURE ("IOException: "
                                               + ex.toString());
    }

}
// TCP_IP_Transport

