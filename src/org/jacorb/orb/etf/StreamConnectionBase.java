/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2004 Gerald Brose.
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

import java.io.*;

import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.configuration.*;

/**
 * This an abstract base implementation of the ETF::Connection interface.
 *
 * 
 *
 * @author Nicolas Noffke / Andre Spiegel
 * @version $Id$
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
    }
    
    /**
    * Initialise this instance as a copy of another. Intended for use within subclass
    * constructors.
    */
    protected StreamConnectionBase(StreamConnectionBase other)
    {
        super((ConnectionBase)other);
        this.in_stream = other.in_stream;
        this.out_stream = other.out_stream;
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
                int soTimeout = getSoTimeout();
                
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


}

