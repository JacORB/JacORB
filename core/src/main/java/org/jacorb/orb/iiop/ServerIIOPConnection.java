/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2012 Gerald Brose / The JacORB Team.
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

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import javax.net.ssl.SSLSocket;
import org.jacorb.config.Configuration;
import org.jacorb.config.ConfigurationException;
import org.jacorb.orb.listener.TCPConnectionEvent;
import org.jacorb.orb.listener.TCPConnectionListener;
/**
 * ServerIIOPConnection.java
 *
 * @author Nicolas Noffke
 * @author Andre Spiegel
 */

public class ServerIIOPConnection
    extends IIOPConnection
{
    private final TCPConnectionListener tcpListener;

    public ServerIIOPConnection( Socket socket,
                                 boolean is_ssl,
                                 TCPConnectionListener tcpListener )
        throws IOException
    {
        super();

        this.socket = socket;
        this.use_ssl = is_ssl;

        in_stream = socket.getInputStream();
        out_stream = new BufferedOutputStream(socket.getOutputStream());
        this.tcpListener = tcpListener;
    }


    public void configure(Configuration configuration)
        throws ConfigurationException
    {
        super.configure(configuration);

        IIOPAddress address = new IIOPAddress
        (
            socket.getInetAddress().getHostAddress(),
            socket.getPort()
        );
        address.configure (configuration);

        profile = new IIOPProfile(address, null, orb.getGIOPMinorVersion());
        profile.configure(configuration);

        connection_info = address.toString();
        connected = true;

        if (logger.isInfoEnabled())
        {
            logger.info("Opened new server-side TCP/IP transport to " +
                        connection_info );
        }
    }


    public synchronized void close()
    {
        if( socket != null )
        {
            try
            {
                if ( ! (socket instanceof SSLSocket) && ! socket.isClosed())
                {
                    socket.shutdownOutput();
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
            catch (IOException ex)
            {
                throw handleCommFailure(ex);
            }
            finally
            {
                if (tcpListener.isListenerEnabled())
                {
                    tcpListener.connectionClosed(
                            new TCPConnectionEvent
                            (
                                    this,
                                    socket.getInetAddress().toString(),
                                    socket.getPort(),
                                    socket.getLocalPort(),
                                    getLocalhost()
                            )
                    );
                }
            }
        }

        socket = null;
        connected = false;

        if (logger.isInfoEnabled())
        {
            logger.info("Closed server-side transport to " +
                    connection_info );
        }
    }

    public void connect (org.omg.ETF.Profile server_profile, long time_out)
    {
        //can't reconnect
    }
}
