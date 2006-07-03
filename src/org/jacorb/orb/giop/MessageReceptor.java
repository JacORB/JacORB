package org.jacorb.orb.giop;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2004 Gerald Brose.
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

import java.net.Socket;

import org.apache.avalon.framework.logger.Logger;
import org.jacorb.config.Configuration;
import org.jacorb.orb.iiop.ServerIIOPConnection;
import org.jacorb.util.threadpool.Consumer;

/**
 * @author Nicolas Noffke
 * @version $Id$
 */

public class MessageReceptor
    implements Consumer
{
    private final Logger logger;
    private final boolean enhanceThreadName;

    public MessageReceptor(Configuration configuration)
    {
        logger = configuration.getNamedLogger("jacorb.orb.giop");
        enhanceThreadName = configuration.getAttributeAsBoolean("jacorb.enhanced_thread_name", false);
    }

    public void doWork( Object job )
    {
        try
        {
            final GIOPConnection connection = (GIOPConnection) job;
            final String oldName;

            if (enhanceThreadName)
            {
                oldName = Thread.currentThread().getName();

                org.omg.ETF.Connection transport = connection.getTransport();

                String enhancedName = getEnhancedName(transport, oldName);

                Thread.currentThread().setName(enhancedName);
            }
            else
            {
                oldName = null;
            }

            try
            {
                (connection).receiveMessages();
            }
            finally
            {
                if (enhanceThreadName)
                {
                    Thread.currentThread().setName(oldName);
                }
            }
        }
        catch( Exception e )
        {
            logger.error("unexpected exception during doWork", e);
        }
    }

    private String getEnhancedName(org.omg.ETF.Connection transport, final String oldName)
    {
        if (transport instanceof ServerIIOPConnection)
        {
            Socket socket =
                ((ServerIIOPConnection) transport).getSocket();
            int localPort = socket.getLocalPort();
            int port = socket.getPort();

            StringBuffer buffer = new StringBuffer();
            buffer.append(oldName);
            buffer.append('[');
            buffer.append(localPort);
            buffer.append(socket.getInetAddress().toString());
            buffer.append(':');
            buffer.append(port);
            buffer.append("] [");
            buffer.append(System.currentTimeMillis());
            buffer.append(']');

            return buffer.toString();
        }

        return oldName;
    }
}
