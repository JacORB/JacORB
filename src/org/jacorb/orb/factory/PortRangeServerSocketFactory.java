package org.jacorb.orb.factory;

/*
 *        Written for JacORB - a free Java ORB
 *
 *   Copyright (C) 2000-2002 Nicolas Noffke, Gerald Brose.
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

import java.net.*;
import java.io.IOException;
import org.jacorb.util.*;
import org.jacorb.orb.*;

public class PortRangeServerSocketFactory extends PortRangeFactory implements ServerSocketFactory
{
    public static final String MIN_PROP = "jacorb.net.server_socket_factory.port.min";
    public static final String MAX_PROP = "jacorb.net.server_socket_factory.port.max";

    public PortRangeServerSocketFactory ()
    {
        super (MIN_PROP, MAX_PROP);
    }

    public ServerSocket createServerSocket (int port, int backlog)
        throws IOException
    {
        int localPort;
        ServerSocket socket;

        for (localPort = portMin; localPort <= portMax; localPort++)
        {
            try
            {
                socket = new ServerSocket (localPort, backlog);
                Debug.output (2, "PortRangeServerSocketFactory: Created server socket at "
                    + ":" + localPort);
                return socket;
            }
            catch (IOException ex)
            {
                // Ignore and continue
            }
        }

        Debug.output (Debug.IMPORTANT | Debug.ORB_CONNECT,
            "Cannot create server socket between ports " + portMin + " and "
            + portMax);
        throw new BindException ("PortRangeServerSocketFactory: no free port between "
            + portMin + " and " + portMax);
    }

    public ServerSocket createServerSocket
        (int port, int backlog, InetAddress ifAddress)
        throws IOException
    {
         int localPort;
        ServerSocket socket;

        for (localPort = portMin; localPort <= portMax; localPort++)
        {
            try
            {
                socket = new ServerSocket (localPort, backlog, ifAddress);
                Debug.output (2, "PortRangeServerSocketFactory: Created server socket at "
                    + ":" + localPort);
                return socket;
            }
            catch (IOException ex)
            {
                // Ignore and continue
            }
        }

        Debug.output (Debug.IMPORTANT | Debug.ORB_CONNECT,
            "Cannot create server socket between ports " + portMin + " and "
            + portMax);
        throw new BindException ("PortRangeServerSocketFactory: no free port between "
            + portMin + " and " + portMax);
    }

    public ServerSocket createServerSocket (int port)
        throws IOException
    {
        int localPort;
        ServerSocket socket;

        for (localPort = portMin; localPort <= portMax; localPort++)
        {
            try
            {
                socket = new ServerSocket (localPort);
                Debug.output (2, "PortRangeServerSocketFactory: Created server socket at "
                    + ":" + localPort);
                return socket;
            }
            catch (IOException ex)
            {
                // Ignore and continue
            }
        }

        Debug.output (Debug.IMPORTANT | Debug.ORB_CONNECT,
            "Cannot create server socket between ports " + portMin + " and "
            + portMax);
        throw new BindException ("PortRangeServerSocketFactory: no free port between "
            + portMin + " and " + portMax);
    }
}
