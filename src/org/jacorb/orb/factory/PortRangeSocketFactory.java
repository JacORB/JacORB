package org.jacorb.orb.factory;

/*
 *        Written for JacORB - a free Java ORB
 *
 *   Copyright (C) 2000-2003 Nicolas Noffke, Gerald Brose.
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

public class PortRangeSocketFactory extends PortRangeFactory implements SocketFactory
{
    public static final String MIN_PROP = "jacorb.net.socket_factory.port.min";
    public static final String MAX_PROP = "jacorb.net.socket_factory.port.max";

    public PortRangeSocketFactory ()
    {
        super (MIN_PROP, MAX_PROP);
    }                

    public Socket createSocket (String host, int port)
        throws IOException, UnknownHostException
    {
        int localPort;
        InetAddress localHost = InetAddress.getLocalHost ();
        Socket socket;

        for (localPort = portMin; localPort <= portMax; localPort++)
        {
            try
            {
                socket = new Socket (host, port, localHost, localPort);
                Debug.output (2, "PortRangeSocketFactory: Created socket between "
                    + localHost.getHostAddress () + ":" + localPort
                    + " and " + host + ":" + port);
                return socket;
            }
            catch (IOException ex)
            {
                // Ignore and continue
            }
        }

        Debug.output (Debug.IMPORTANT | Debug.ORB_CONNECT,
            "Cannot bind socket between ports " + portMin + " and "
            + portMax + " to target " + host + ":" + port);
        throw new BindException ("PortRangeSocketFactory: no free port between "
            + portMin + " and " + portMax);
    }

    public boolean isSSL (Socket socket)
    {
        return false;
    }
}
