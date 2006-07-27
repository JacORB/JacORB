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

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;

/**
 * a SocketFactory implementation that allows
 * to specify the local address the socket should be bound to.
 * the factory will read the attribute OAIAddr from the configuration
 * and use the specified value to configure the sockets it creates.
 *
 * @author Nicolas Noffke
 * @version $Id$
 */
public class FixedAddressSocketFactory extends AbstractSocketFactory implements SocketFactory, Configurable
{
    /**
     * optional local address the socket should be bound to.
     * may be null if no local address is specified.
     */
    private InetAddress localEndpoint;

    public Socket createSocket(String host, int port)
        throws IOException
    {
        if (localEndpoint != null)
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("Will create client socket bound to endpoint " +
                             localEndpoint );
            }

            return new Socket(host, port, localEndpoint, 0);
        }

        return new Socket(host, port);
    }

    public Socket createSocket(String host, int port, int timeout) throws IOException
    {
        Socket socket = new Socket();

        if (localEndpoint != null)
        {
            socket.bind(new InetSocketAddress(localEndpoint, 0));
        }

        socket.connect(new InetSocketAddress(host, port), timeout);

        return socket;
    }

    public boolean isSSL(Socket socket)
    {
        return false;
    }

    public void configure(Configuration config) throws ConfigurationException
    {
        super.configure(config);

        String oaiAddr = config.getAttribute("OAIAddr", "");
        if (oaiAddr.length() > 0)
        {
            try
            {
                localEndpoint = InetAddress.getByName(oaiAddr);
            }
            catch (UnknownHostException e)
            {
                if (logger.isErrorEnabled())
                {
                    logger.error(
                        "Failed to create InetAddress from property OAIAddr >" +
                        oaiAddr + "<", e);
                }
            }
        }
    }
}
