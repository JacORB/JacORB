package org.jacorb.orb.factory;

/*
 *        Written for JacORB - a free Java ORB
 *
 *   Copyright (C) 2000-2004 Nicolas Noffke, Gerald Brose.
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

import java.io.IOException;
import java.net.BindException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;

/**
 * a SocketFactory implementation that allows to specify the range
 * of local ports that should be used by a created socket.
 * the factory will read the attributes jacorb.net.socket_factory.port.min and
 * jacorb.net.socket_factory.port.max from the configuration and use the specified
 * values to configure the created sockets.
 *
 * @author Steve Osselton
 * @version $Id$
 */
public class PortRangeSocketFactory
    extends AbstractSocketFactory
{
    public static final String MIN_PROP = "jacorb.net.socket_factory.port.min";
    public static final String MAX_PROP = "jacorb.net.socket_factory.port.max";

    private int portMin;
    private int portMax;

    public void configure(Configuration config)
        throws ConfigurationException
    {
        super.configure(config);

       // Get configured max and min port numbers
        portMin = getPortProperty(config, MIN_PROP);
        portMax = getPortProperty(config, MAX_PROP);

        // Check min < max
        if (portMin > portMax)
        {
            throw new ConfigurationException("PortRangeFactory: minimum port number not less than or equal to maximum");
        }
    }

    public Socket createSocket(String host, int port)
        throws IOException, UnknownHostException
    {
        int localPort;
        InetAddress localHost = InetAddress.getLocalHost ();

        for (localPort = portMin; localPort <= portMax; localPort++)
        {
            try
            {
                final Socket socket = new Socket (host, port, localHost, localPort);
                if (logger.isDebugEnabled())
                {
                    logger.debug("PortRangeSocketFactory: Created server socket at "
                                 + ":" + localPort);
                }

                return socket;
            }
            catch (IOException ex) // NOPMD
            {
                // Ignore and continue
            }
        }

        if (logger.isWarnEnabled())
        {
            logger.warn("Cannot bind socket between ports " + portMin + " and "
                    + portMax + " to target " + host + ":" + port);
        }

        throw new BindException ("PortRangeSocketFactory: no free port between "
                                 + portMin + " and " + portMax);
    }

    public boolean isSSL(Socket socket)
    {
        return false;
    }

    private int getPortProperty(Configuration config, String name)
        throws ConfigurationException
    {
        int port = config.getAttributeAsInteger(name);

        // Check sensible port number
        if (port < 0)
        {
            port += 65536;
        }
        if ((port <= 0) || (port > 65535))
        {
            throw new ConfigurationException("PortRangeFactory: " + name + " invalid port number");
        }

        return port;
    }

    public Socket createSocket(String host, int port, int timeout) throws IOException
    {
        final InetAddress localHost = InetAddress.getLocalHost();
        int localPort;
        Socket socket;

        for (localPort = portMin; localPort <= portMax; localPort++)
        {
            try
            {
                socket = new Socket();
                socket.bind(new InetSocketAddress(localHost, localPort));
                socket.connect(new InetSocketAddress(host, port), timeout);

                if (logger.isWarnEnabled())
                {
                    logger.warn("PortRangeSocketFactory: Created socket between "
                            + localHost.getHostAddress () + ":" + localPort
                            + " and " + host + ":" + port);
                }
                return socket;
            }
            catch (IOException ex)
            {
                // Ignore and continue
            }
        }

        if (logger.isWarnEnabled())
        {
            logger.warn("Cannot bind socket between ports " + portMin + " and "
                    + portMax + " to target " + host + ":" + port);
        }

        throw new BindException ("PortRangeSocketFactory: no free port between "
            + portMin + " and " + portMax);
    }
}
