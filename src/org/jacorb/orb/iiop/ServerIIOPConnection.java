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

import java.io.*;
import java.net.*;

import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.configuration.*;

import org.jacorb.orb.*;
import org.jacorb.orb.iiop.*;

/**
 * ServerIIOPConnection.java
 *
 *
 * Created: Sun Aug 12 20:56:32 2002
 *
 * @author Nicolas Noffke / Andre Spiegel
 * @version $Id$
 */

public class ServerIIOPConnection
    extends IIOPConnection
{
    private boolean is_ssl;
    private IIOPProfile profile;

    public ServerIIOPConnection( Socket socket,
                                 boolean is_ssl )
        throws IOException
    {
        super();

        this.socket = socket;
        //        socket.setTcpNoDelay( true );
        this.is_ssl = is_ssl;

        in_stream = socket.getInputStream();
        out_stream = new BufferedOutputStream(socket.getOutputStream());

    }


    public void configure(Configuration configuration)
        throws ConfigurationException
    {
        super.configure(configuration);
        //get the client-side timeout property value


        IIOPAddress address = new IIOPAddress
        (
            socket.getInetAddress().getHostAddress(),
            socket.getPort()
        );
        
        profile = new IIOPProfile(address, null);
        profile.configure(configuration);

        connection_info = address.toString(); 
        connected = true;

        if (logger.isInfoEnabled())
            logger.info("Opened new server-side TCP/IP transport to " +
                        connection_info );
    }


    public Socket getSocket()
    {
        return socket;
    }

    public synchronized void close()
    {
        if( socket != null )
        {
            try
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
            }
            catch (IOException ex)
            {
                throw to_COMM_FAILURE (ex);
            }
            
            socket = null;
            connected = false;

            if (logger.isInfoEnabled())
                logger.info("Closed server-side transport to " +
                            connection_info );
        }
    }

    public void connect (org.omg.ETF.Profile server_profile, long time_out)
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
