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

package org.jacorb.orb.iiop;

import java.net.*;
import java.io.*;

import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.configuration.*;

/**
 * IIOPConnection.java
 *
 *
 * Created: Sun Aug 12 20:18:47 2002
 *
 * @author Nicolas Noffke / Andre Spiegel
 * @version $Id$
 */

public abstract class IIOPConnection
    extends org.omg.ETF._ConnectionLocalBase
    implements Configurable
{
    protected boolean connected = false;

    protected InputStream in_stream = null;
    protected OutputStream out_stream = null;

    private ByteArrayOutputStream b_out = null;
    private boolean dump_incoming = false;

    protected String connection_info;
    protected Socket socket;

    private int finalTimeout = 20000;

    /** shared with sub classes */
    protected Logger logger;
    protected org.jacorb.config.Configuration configuration;

    public IIOPConnection (IIOPConnection other)
    {
        this.in_stream = other.in_stream;
        this.out_stream = other.out_stream;
        this.b_out = other.b_out;
        this.dump_incoming = other.dump_incoming;
        this.connection_info = other.connection_info;
        this.finalTimeout = other.finalTimeout;
    }

    public IIOPConnection()
    {
    }


    public void configure(Configuration configuration)
        throws ConfigurationException
    {
        this.configuration = (org.jacorb.config.Configuration)configuration;
        logger = this.configuration.getNamedLogger("jacorb.iiop.conn");

        if( configuration.getAttribute("jacorb.debug.dump_outgoing_messages","off").equals("on"))
        {
            b_out = new ByteArrayOutputStream();
        }

        finalTimeout =
            configuration.getAttributeAsInteger("jacorb.connection.timeout_after_closeconnection",
                                                20000 );
    }



    /**
     * read actual messages
     */

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
                    if (logger.isDebugEnabled())
                    {
                        logger.debug("Socket timeout (timeout period: " +
                                     soTimeout + ")" );
                    }
                    throw new org.omg.CORBA.TIMEOUT();
                }
                else
                {
                    throw new org.omg.CORBA.TRANSIENT ("Interrupted I/O: " + e);
                }
            }
            catch( IOException se )
            {
                if (logger.isDebugEnabled())
                {
                    logger.debug("Transport to " + connection_info +
                                 ": stream closed " + se.getMessage() );
                }
                throw to_COMM_FAILURE (se);
            }

            if( n < 0 )
            {
                if (logger.isDebugEnabled())
                {
                    logger.debug("Transport to " + connection_info +
                                 ": stream closed on read  < 0" );
                }
                throw new org.omg.CORBA.COMM_FAILURE ("read() did not return any data");
            }

            read += n;
        }
    }

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
                if (logger.isInfoEnabled())
                    logger.info("sendMessages(): " + new String( b) );
                b_out.reset();
            }
            out_stream.flush();
        }
        catch (IOException ex)
        {
            throw to_COMM_FAILURE (ex);
        }

    }

    public synchronized boolean is_connected()
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
                if (logger.isInfoEnabled())
                    logger.info ("Socket timeout set to " + finalTimeout + " ms");
                socket.setSoTimeout( finalTimeout );
            }
            catch( SocketException se )
            {
                if (logger.isInfoEnabled())
                    logger.info("SocketException", se);
            }
        }
    }

    public abstract boolean isSSL();

    protected org.omg.CORBA.COMM_FAILURE to_COMM_FAILURE (IOException ex)
    {
        return new org.omg.CORBA.COMM_FAILURE("IOException: "
                                               + ex.toString());
    }

    /**
     * Simply return true if calling a read on this instance would
     * find data in the connection. Otherwise, the function shall
     * return false.
     */
    public boolean is_data_available()
    {
        try
        {
            return in_stream.available() > 0;
        }
        catch (IOException ex)
        {
            throw to_COMM_FAILURE (ex);
        }
    }

    /**
     * Wait for the given time_out period for incoming data on this
     * connection. It shall return false if this call times out and
     * no data is available. It may not throw a  TIMEOUT  exception.
     * If data can already be read or arrives before the end of the
     * time out, this function shall return true, immediately.
     */
    public boolean wait_next_data (long time_out)
    {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }


    /**
     * A boolean flag describing whether this connection supports the
     * Bidirectional GIOP mechanism as described by GIOP-1.2 in CORBA 2.3.1
     * (OMG Document: formal/99-10-07). It shall return true if it does,
     * and false if it does not.
     */
    public boolean supports_callback()
    {
        return true;
    }

    /**
     * A flag directing the ORB to use either the Handle class to perform
     * data queries with a time_out, or the transport layer (through this
     * connection). The instance shall return true, if the Handle should
     * signal time outs for read operations. Then the ORB may not call
     * wait_next_data. Otherwise, a false shall be returned, and the
     * function wait_next_data shall be implemented by this class.
     */
    public boolean use_handle_time_out()
    {
        // We have neither mechanism in JacORB.
        // I wonder if we should.  AS.
        return false;
    }

}
// TCP_IP_Transport
