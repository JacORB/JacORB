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

import java.net.*;
import java.io.*;

import org.jacorb.orb.BufferManager;
import org.jacorb.util.Debug;

/**
 * TCP_IP_Transport.java
 *
 *
 * Created: Sun Aug 12 20:18:47 2001
 *
 * @author Nicolas Noffke
 * @version $Id$
 */

public abstract class TCP_IP_Transport 
    implements Transport  
{
    protected InputStream in_stream = null;
    protected OutputStream out_stream = null;

    private byte[] msg_header = null;
    private BufferManager buff_mg = null;

    public TCP_IP_Transport()
    {
        msg_header = new byte[ Messages.MSG_HEADER_SIZE ];        
        
        buff_mg = BufferManager.getInstance();
    }

    protected abstract void transportClosed()
        throws IOException;

    protected abstract void connect();
    protected abstract void waitUntilConnected()
        throws IOException;

    
    private int readToBuffer( byte[] buffer, 
                              int start_pos,
                              int length )
        throws IOException
    {
        int read = 0;

        while( read < length )
        {
            int n = 0;

            try
            {
                n = in_stream.read( buffer, 
                                    start_pos + read, 
                                    length - read );
            }
            catch( InterruptedIOException e )
            {
                //read timed out
                transportClosed();

                throw e;
            }

            if( n < 0 )
            {
                //ended to early
                transportClosed();
                
                throw new CloseConnectionException();
            }  

            read += n;
        }

        return read;
    }

    // implementation of org.jacorb.orb.connection.Transport interface

    /**
     *
     * @return <description>
     * @exception java.io.IOException <description>
     */
    public byte[] getMessage() 
        throws IOException 
    {
        waitUntilConnected();

        int read = readToBuffer( msg_header, 0, Messages.MSG_HEADER_SIZE );

        if( read == -1 )
        {
            //transport closed. Try again to wait until it reopens
            return null;
        }

        if( read != Messages.MSG_HEADER_SIZE )
        {
            //TODO: resynching?
            
            Debug.output( 1, "ERROR: Failed to read GIOP message header" );
            Debug.output( 1, (Messages.MSG_HEADER_SIZE - read) + 
                          " Bytes less than the expected " +
                          Messages.MSG_HEADER_SIZE + " Bytes" );
            Debug.output( 3, "TCP_IP_GIOPTransport.getMessage()",
                          msg_header, 0, read );

            return null;          
        }
        
        //(minimally) decode GIOP message header. Main checks should
        //be done one layer above.
        
        if( (char) msg_header[0] == 'G' && (char) msg_header[1] == 'I' && 
            (char) msg_header[2] == 'O' && (char) msg_header[3] == 'P')
	{
	    //determine message size
	    int msg_size = Messages.getMsgSize( msg_header );
   
	    if( msg_size < 0 )
	    {
                Debug.output( 1, "ERROR: Negative GIOP message size: " + msg_size );
                Debug.output( 3, "TCP_IP_GIOPTransport.getMessage()",
                              msg_header, 0, read );

		return null;
	    }
	    
	    byte[] inbuf = buff_mg.getBuffer( msg_size + 
                                              Messages.MSG_HEADER_SIZE );
	    
	    /* copy header */
	    System.arraycopy( msg_header, 0, inbuf, 0, Messages.MSG_HEADER_SIZE );

            read = readToBuffer( inbuf, Messages.MSG_HEADER_SIZE, msg_size );
            if( read != msg_size )
            {
                Debug.output( 1, "ERROR: Failed to read GIOP message" );
                Debug.output( 1, (msg_size - read) + 
                              " Bytes less than the expected " +
                              msg_size + " Bytes" );
                Debug.output( 3, "TCP_IP_GIOPTransport.getMessage()",
                              inbuf, 0, read );

                return null;
            }

            //Debug.output( 3, "Received Msg", inbuf, 0, read + Messages.MSG_HEADER_SIZE );
            
            return inbuf;
        }
        else
        {
                Debug.output( 1, "ERROR: Failed to read GIOP message" );
                Debug.output( 1, "Magic start doesn't match" );
                Debug.output( 3, "TCP_IP_GIOPTransport.getMessage()",
                              msg_header );

            return null;
        }            
    }

    /**
     *
     * @param message <description>
     * @param size <description>
     * @exception java.io.IOException <description>
     */
    public void addOutgoingMessage( byte[] message,
                                    int start,
                                    int size ) 
        throws IOException 
    {
        connect();

        out_stream.write( message, start, size );
    }
    
    public void sendMessages()
        throws IOException
    {
        out_stream.flush();
    }
}// TCP_IP_Transport





