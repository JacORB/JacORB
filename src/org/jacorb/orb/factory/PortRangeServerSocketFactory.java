package org.jacorb.orb.factory;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 2000-2004 Gerald Brose.
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

import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.configuration.*;

import org.jacorb.orb.*;

public class PortRangeServerSocketFactory 
    extends PortRangeFactory 
    implements ServerSocketFactory
{
    public static final String MIN_PROP = "jacorb.net.server_socket_factory.port.min";
    public static final String MAX_PROP = "jacorb.net.server_socket_factory.port.max";

    private Logger logger;

    public void configure(Configuration configuration)
        throws ConfigurationException
    {
        this.configuration = (org.jacorb.config.Configuration)configuration;
        logger = this.configuration.getNamedLogger("jacorb.orb.port_rang_fctry");

       // Get configured max and min port numbers
        portMin = getPortProperty(MIN_PROP);
        portMax = getPortProperty(MAX_PROP);

        // Check min < max
        if (portMin > portMax)
        {
            throw new ConfigurationException("PortRangeFactory: minimum port number not less than or equal to maximum");
        }
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
                if (logger.isDebugEnabled())
                    logger.debug("PortRangeServerSocketFactory: Created server socket at "
                                 + ":" + localPort);
                return socket;
            }
            catch (IOException ex)
            {
                // Ignore and continue
            }
        }

        if (logger.isDebugEnabled())
        {
            logger.debug("Cannot create server socket between ports " + 
                         portMin + " and " + portMax);
        }

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
                socket = new ServerSocket(localPort, backlog, ifAddress);
                if (logger.isDebugEnabled())
                {
                    logger.debug("Created server socket at "
                                 + ":" + localPort);
                }
                return socket;
            }
            catch (IOException ex)
            {
                // Ignore and continue
            }
        }

        if (logger.isDebugEnabled())
        {
            logger.debug("Cannot create server socket between ports " + 
                         portMin + " and " + portMax);
        }

        throw new BindException ("PortRangeServerSocketFactory: no free port between "
                                 + portMin + " and " + portMax);
    }

    public ServerSocket createServerSocket(int port)
        throws IOException
    {
        int localPort;
        ServerSocket socket;

        for (localPort = portMin; localPort <= portMax; localPort++)
        {
            try
            {
                socket = new ServerSocket(localPort);
                if (logger.isDebugEnabled())
                {
                    logger.debug("Created server socket at "
                                 + ":" + localPort);
                }
                return socket;
            }
            catch (IOException ex)
            {
                // Ignore and continue
            }
        }

        if (logger.isDebugEnabled())
        {
            logger.debug("Cannot create server socket between ports " + 
                         portMin + " and " + portMax);
        }
        throw new BindException ("PortRangeServerSocketFactory: no free port between "
                                 + portMin + " and " + portMax);
    }
}

