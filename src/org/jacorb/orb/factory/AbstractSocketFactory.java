/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2004 Gerald Brose
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
 *
 */

package org.jacorb.orb.factory;

import java.io.IOException;
import java.net.Socket;

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.logger.Logger;
import org.omg.CORBA.TIMEOUT;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */

public abstract class AbstractSocketFactory implements SocketFactory, Configurable
{
    protected Logger logger;

    public void configure(Configuration configuration) throws ConfigurationException
    {
        org.jacorb.config.Configuration config = (org.jacorb.config.Configuration) configuration;

        logger = config.getNamedLogger("jacorb.orb.socketfactory");
    }

    /*
     * the preferred way to use a timeout during connect is to use the new methods
     * that were introduced in JDK 1.4. for JDK 1.3 the timeout is
     * controlled here with an extra thread. unfortunately this code might NOT work across
     * all JDK/OS combinations. see http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6315293
     * for details.
     */
    public Socket createSocket(final String host, final int port, int timeout) throws IOException
    {
        final Socket[] socket = new Socket[1];
        final IOException[] exception = new IOException[1];

        final Thread connectThread = new Thread("SocketConnectorThread")
        {
            public void run()
            {
                try
                {
                    socket[0] = createSocket(host, port);
                }
                catch (IOException e)
                {
                    exception[0] = e;
                }
            }
        };

        connectThread.start();

        try
        {
            connectThread.join(timeout);
        }
        catch (InterruptedException e)
        {
        }

        final String connection_info = host + ":" + port;

        if (exception[0] != null)
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("connect to " + connection_info + " with timeout="
                             + timeout + " raised exception: " + exception[0].toString());
            }

            throw exception[0];
        }

        if (socket[0] == null)
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("connect to " + connection_info +
                             " with timeout=" + timeout + " timed out");
            }

            // due to the JDK bug it's possible that the SocketConnectorThread
            // keeps waiting in PlainSocketImpl.socketConnect() and is NOT interrupted.
            // this may cause SocketConnectorThread's to pile up.
            connectThread.interrupt();

            throw new TIMEOUT("connection timeout of " + timeout + " milliseconds expired");
        }

        return socket[0];
    }
}
