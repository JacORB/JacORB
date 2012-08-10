package org.jacorb.orb.factory;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 2000-2012 Gerald Brose / The JacORB Team.
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
import java.net.ServerSocket;
import org.jacorb.config.Configurable;
import org.jacorb.config.Configuration;
import org.jacorb.config.ConfigurationException;
import org.slf4j.Logger;

/**
 * @author Steve Osselton
 */
public class PortRangeServerSocketFactory
    implements ServerSocketFactory, Configurable
{
    public static final String MIN_PROP = "jacorb.net.server_socket_factory.port.min";
    public static final String MAX_PROP = "jacorb.net.server_socket_factory.port.max";

    private final ServerSocketFactory delegate;

    private Logger logger;
    private int portMin;
    private int portMax;

    public PortRangeServerSocketFactory(ServerSocketFactory delegate)
    {
        super();

        this.delegate = delegate;
    }

    public PortRangeServerSocketFactory()
    {
        this(new DefaultServerSocketFactory());
    }

    public void configure(Configuration config)
        throws ConfigurationException
    {
        logger = config.getLogger("jacorb.orb.port_rang_fctry");

        // Get configured max and min port numbers
        portMin = getPortProperty(config, MIN_PROP);
        portMax = getPortProperty(config, MAX_PROP);

        // Check min < max
        if (portMin > portMax)
        {
            throw new ConfigurationException("PortRangeFactory: minimum port number not less than or equal to maximum");
        }
    }

    public ServerSocket createServerSocket (int port, int backlog)
        throws IOException
    {
        if (port <= portMax && port >= portMin)
        {
            try
            {
                return doCreateServerSocket(port, backlog);
            }
            catch (IOException e) // NOPMD
            {
                // ignored. will retry
            }
        }

        for (int localPort = portMin; localPort <= portMax; localPort++)
        {
            try
            {
                return doCreateServerSocket(localPort, backlog);
            }
            catch (IOException ex) // NOPMD
            {
                // Ignore and continue
            }
        }

        return handleCreationFailed();
    }

    private ServerSocket doCreateServerSocket(int port, int backlog)
        throws IOException
    {
        final ServerSocket socket = delegate.createServerSocket(port, backlog);
        if (logger.isDebugEnabled())
        {
            logger.debug("PortRangeServerSocketFactory: Created server socket at "
                         + ":" + port);
        }
        return socket;
    }

    public ServerSocket createServerSocket(int port, int backlog, InetAddress ifAddress)
        throws IOException
    {
        if (port <= portMax && port >= portMin)
        {
            try
            {
                return doCreateServerSocket(port, backlog, ifAddress);
            }
            catch (IOException e) // NOPMD
            {
                // ignore will retry
            }
        }

        for (int localPort = portMin; localPort <= portMax; localPort++)
        {
            try
            {
                return doCreateServerSocket(localPort, backlog, ifAddress);
            }
            catch (IOException ex) // NOPMD
            {
                // Ignore and continue
            }
        }

        return handleCreationFailed();
    }

    private ServerSocket doCreateServerSocket(int localPort, int backlog, InetAddress ifAddress)
        throws IOException
    {
        final ServerSocket socket = delegate.createServerSocket(localPort, backlog, ifAddress);
        if (logger.isDebugEnabled())
        {
            logger.debug("Created server socket at "
                         + ":" + localPort);
        }
        return socket;
    }

    public ServerSocket createServerSocket(int port)
        throws BindException
    {
        if (port <= portMax && port >= portMin)
        {
            try
            {
                return doCreateServerSocket(port);
            }
            catch (IOException e) // NOPMD
            {
                // ignored will retry
            }
        }

        for (int localPort = portMin; localPort <= portMax; localPort++)
        {
            try
            {
                return doCreateServerSocket(localPort);
            }
            catch (IOException ex)
            {
                // Ignore and continue
            }
        }

        return handleCreationFailed();
    }

    private ServerSocket doCreateServerSocket(int port) throws IOException
    {
        final ServerSocket socket = delegate.createServerSocket(port);
        if (logger.isDebugEnabled())
        {
            logger.debug("Created server socket at "
                         + ":" + port);
        }

        return socket;
    }

    private ServerSocket handleCreationFailed() throws BindException
    {
        if (logger.isDebugEnabled())
        {
            logger.debug("Cannot create server socket between ports " +
                         portMin + " and " + portMax);
        }

        throw new BindException ("PortRangeServerSocketFactory: no free port between "
                                 + portMin + " and " + portMax);
    }

    protected int getPortProperty(Configuration config, String name)
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
}

