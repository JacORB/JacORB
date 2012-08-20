/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) Copyright (C) 2000-2012 Gerald Brose / The JacORB Team.
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
package org.jacorb.orb.etf;

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import org.jacorb.util.ObjectUtil;

/**
 * This an abstract base implementation of the ETF::Connection interface.
 *
 * @author Nicolas Noffke
 * @author Andre Spiegel
 */

public abstract class StreamConnectionBase
    extends ConnectionBase
{
   /**
    * Reads performed on this stream.
    */
    protected InputStream in_stream = null;

    /**
    * Writes performed on this stream.
    */
    protected OutputStream out_stream = null;

    protected StreamConnectionBase()
    {
        super();
    }

    /**
    * Initialise this instance as a copy of another. Intended for use within subclass
    * constructors.
    */
    protected StreamConnectionBase(StreamConnectionBase other)
    {
        super(other);
        this.in_stream = other.in_stream;
        this.out_stream = other.out_stream;
    }

    /**
     * Reads bytes from the connection.
     * 
     * @param data holds a byte array to which the bytes will be written.  The
     * field <code>data.value</code> must be initialized with a valid byte
     * array already, it cannot be null.
     * @param offset the index in <code>data.value</code> at which the first
     * byte will be written.
     * @param min_length the minimum number of bytes that shall be read from
     * the Connection.  The method will block until at least this many bytes
     * have been read.  If <code>min_length</code> is 0, the method will always
     * return immediately without reading any data.
     * @param max_length the maximum number of bytes that shall be read from
     * the Connection.  If <code>max_length</code> is greater than
     * <code>min_length</code>, then the transport is free to read
     * (<code>max_length</code> - <code>min_length</code>) additional bytes
     * beyond <code>min_length</code>.
     * @param time_out timeout for this particular read operation.  Currently
     * ignored in JacORB; we use socket-level timeouts.
     *
     * @return the number of bytes actually read.  The last byte written to
     * <code>data.value</code> is at the index <code>offset</code> + this return
     * value.  This return type is a change to the ETF draft spec in JacORB.
     * It is needed because the mechanism suggested in the draft does not work
     * in Java.
     *
     * @exception org.omg.CORBA.TIMEOUT if the socket-level timeout expires
     * before the read operation completes.
     * @exception org.omg.CORBA.TRANSIENT if the I/O is interrupted.
     * @exception org.omg.CORBA.COMM_FAILURE if the read operation fails,
     * for example because the connection has been closed. 
     */
    public int read (org.omg.ETF.BufferHolder data,
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
                                    max_length - read );

            }
            catch( InterruptedIOException e )
            {
                int soTimeout = getTimeout();

                if (soTimeout != 0)
                {
                    if (logger.isDebugEnabled())
                    {
                        logger.debug("Socket timeout (timeout period: " +
                                     soTimeout + ")" );
                    }
                    throw new org.omg.CORBA.TIMEOUT();
                }

                throw new org.omg.CORBA.TRANSIENT ("Interrupted I/O: " + e);
            }
            catch( IOException se )
            {
                if (logger.isDebugEnabled())
                {
                    logger.debug("Transport to " + connection_info +
                                 ": stream closed " + se.getMessage() );
                }
                throw handleCommFailure(se);
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
        
        if (logger.isDebugEnabled())
        {
            // guard this with isDebugEnabled() because auto-boxing of
            // parameter "read" would cost too much time
            logger.debug ("read {} bytes from {}", read, connection_info);
        }
        
        return read;
    }

    /**
     * Writes bytes to this Connection.
     * 
     * @param is_first Currently not used in JacORB.
     * @param is_last Currently not used in JacORB.
     * @param data the buffer that holds the data that is to be written.
     * @param offset index of the first byte in <code>data</code> that shall
     * be written to the Connection.
     * @param length the number of bytes in data that shall be written.  The 
     * last byte in <code>data</code> that is written is at the index
     * <code>offset + length</code>.
     * @param time_out timeout for this particular write operation.  Currently
     * ignored in JacORB.
     * 
     * @exception org.omg.CORBA.COMM_FAILURE if anything goes wrong during
     * the write operation.
     */
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
            if (logger.isDebugEnabled())
            {
                logger.debug ("wrote {} bytes to {}", length, connection_info);
            }
        }
        catch (IOException ex)
        {
            throw handleCommFailure(ex);
        }

    }

    /**
     * Causes any buffered data to be actually written to the Connection.
     */
    public void flush()
    {
        try
        {
            if( b_out != null )
            {
                if (logger.isInfoEnabled())
                {
                    byte[] buffer = b_out.toByteArray();
                    logger.info("sendMessages(): " + ObjectUtil.bufToString(buffer, 0, buffer.length) );
                }
                b_out.reset();
            }
            out_stream.flush();
        }
        catch (IOException ex)
        {
            throw handleCommFailure(ex);
        }
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
            throw handleCommFailure(ex);
        }
    }

    public abstract boolean isSSL();
}
