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

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.logger.Logger;

/**
 * @author Nicolas Noffke
 * @version $Id$
 */
public class FixedAddressSocketFactory implements SocketFactory, Configurable
{
    private InetAddress localEndpoint;
    private Logger logger;

    public Socket createSocket (String host, int port)
        throws IOException, UnknownHostException
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

    public boolean isSSL (Socket socket)
    {
        return false;
    }

    public void configure(Configuration arg0) throws ConfigurationException
    {
        logger = ((org.jacorb.config.Configuration)arg0).getNamedLogger("jacorb.orb.iiop");

        String oaiAddr = arg0.getAttribute("OAIAddr", "");
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
