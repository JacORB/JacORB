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

import org.jacorb.util.Debug;
import org.jacorb.orb.*;

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
    private InternetIOPProfile profile;

    public Server_TCP_IP_Transport( Socket socket,
                                    boolean is_ssl,
                                    StatisticsProvider statistics_provider,
                                    TransportManager transport_manager )
        throws IOException
    {
        super( statistics_provider, transport_manager );

        this.socket = socket;
        //        socket.setTcpNoDelay( true );
        this.is_ssl = is_ssl;

        in_stream = socket.getInputStream();
        out_stream = new BufferedOutputStream(socket.getOutputStream());

        IIOPAddress address = new IIOPAddress
        (
            socket.getInetAddress().getHostAddress(),
            socket.getPort()
        );
        
        profile = new InternetIOPProfile (address, null);
        connection_info = address.toString(); 

        Debug.output( 2, "Opened new server-side TCP/IP transport to " +
                      connection_info );
    }

    public Socket getSocket()
    {
        return socket;
    }

    public synchronized void closeCompletely()
        throws IOException
    {
        //ignore the reasons since this transport can never be
        //reestablished.
        if( socket != null )
        {
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
            
            socket = null;

            Debug.output( 2, "Closed server-side transport to " +
                          connection_info );
        }
    }

    public void closeAllowReopen()
        throws IOException
    {
        throw new org.omg.CORBA.BAD_OPERATION();
    }

    protected boolean waitUntilConnected()
    {
        //can't reconnect
        return true;
    }

    protected void connect()
    {
        //can't reconnect
    }

    public boolean isSSL()
    {
        return is_ssl;
    }

    public org.omg.ETF.Profile get_server_profile()
    {
        return profile;
    }
}// Server_TCP_IP_Transport
