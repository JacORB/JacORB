/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2002  Gerald Brose.
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
 * Created: Sun Aug 12 20:56:32 2002
 *
 * @author Nicolas Noffke
 * @version $Id$
 */

public class Server_TCP_IP_Transport
    extends TCP_IP_Transport
{
    private boolean is_ssl;

    public Server_TCP_IP_Transport( Socket socket,
                                    boolean is_ssl )
        throws IOException
    {
        super();

        this.socket = socket;
        //        socket.setTcpNoDelay( true );
        this.is_ssl = is_ssl;

        in_stream = socket.getInputStream();
        out_stream = new BufferedOutputStream(socket.getOutputStream());

        connection_info = socket.getInetAddress().getHostName() +
            ':' + socket.getPort();

        Debug.output( 2, "Opened new server-side TCP/IP transport to " +
                      connection_info );
    }

    public Socket getSocket()
    {
        return socket;
    }

    protected void close( int reason )
        throws IOException
    {
        // read timeouts should only close the connection, if it is
        // idle, i.e. has no pending messages.
        if( reason == READ_TIMED_OUT &&
            ! isIdle() )
        {
            return;
        }

        Debug.output( 2, "Closing TCP connection, reason " + reason );

        //ignore the reasons since this transport can never be
        //reestablished.
        if( socket != null )
        {
            try
            {
                java.lang.reflect.Method method
                = (socket.getClass().getMethod ("shutdownOutput", new Class [0]));
                method.invoke (socket, new java.lang.Object[0]);

                method = (socket.getClass().getMethod ("shutdownInput", new Class [0]));
                method.invoke (socket, new java.lang.Object[0]);
            }
            catch (Throwable ex)
            {
                // If Socket does not support shutdownOutput method (i.e JDK < 1.3)
            }

            socket.close();

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
        }

        Debug.output( 2, "Closed connection (server-side) " +
                      connection_info );

        throw new CloseConnectionException();
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

    public boolean isSSL()
    {
        return is_ssl;
    }
}// Server_TCP_IP_Transport
