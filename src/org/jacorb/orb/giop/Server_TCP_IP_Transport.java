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

import org.jacorb.util.Debug;

/**
 * Server_TCP_IP_Transport.java
 *
 *
 * Created: Sun Aug 12 20:56:32 2001
 *
 * @author Nicolas Noffke
 * @version $Id$
 */

public class Server_TCP_IP_Transport 
    extends TCP_IP_Transport 
{
    private Socket socket = null;
    private boolean is_ssl = false;

    private String connection_info = null;

    public Server_TCP_IP_Transport( Socket socket,
                                    boolean is_ssl )
        throws IOException
    {
        super();

        this.socket = socket;
        this.is_ssl = is_ssl;

        in_stream = socket.getInputStream();
        out_stream = socket.getOutputStream();
        
        connection_info = socket.getInetAddress().getHostName() +
            ':' + socket.getPort();

        Debug.output( 2, "Opened new server-side TCP/IP transport to " +
                      connection_info );

    }

    private void close()
    {
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
            
            out_stream = null;
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
            
            in_stream = null;
        }

        if( socket != null )
        {
            try
            {
                socket.close();
            }
            catch( IOException ioe )
            {
                Debug.output( 1, ioe );
            }
            
            socket = null;
        }
    }
    
    protected void waitUntilConnected()
        throws IOException
    {
        //can't reconnect
    }

    protected void connect()
    {
        //can't reconnect
    }

    /**
     * close TCP connection down. This will generate exceptions to
     * free the thread that is handling incoming messages.  
     */
    public void connectionTerminated()
    {
        close();

        Debug.output( 2, "Closed server-side TCP/IP transport to " +
                      connection_info );
    }
    
    public void transportClosed()
        throws CloseConnectionException
    {
        //we can't reconnect, so just dump this transport
        close();

        throw new CloseConnectionException();
    }

    public boolean isSSL()
    {
        return is_ssl;
    }
}// Server_TCP_IP_Transport


