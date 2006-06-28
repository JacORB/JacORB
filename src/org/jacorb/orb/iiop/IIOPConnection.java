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

package org.jacorb.orb.iiop;

import java.io.IOException;
import java.net.*;

import org.jacorb.orb.listener.SSLListenerUtil;

/**
 * @author Nicolas Noffke
 * @author Andre Spiegel
 * @version $Id$
 */
public abstract class IIOPConnection
    extends org.jacorb.orb.etf.StreamConnectionBase
{
    protected Socket socket;

    protected boolean use_ssl;

    public IIOPConnection (IIOPConnection other)
    {
        super(other);
        this.use_ssl = other.use_ssl;
    }

    public IIOPConnection()
    {
        super();
    }

    public boolean isSSL()
    {
        return use_ssl;
    }

    protected synchronized void setTimeout(int timeout)
    {
        if (socket != null)
        {
            try
            {
                if (logger.isInfoEnabled())
                {
                    logger.info ("Socket timeout set to " + timeout + " ms");
                }
                socket.setSoTimeout(timeout);
            }
            catch( SocketException se )
            {
                if (logger.isInfoEnabled())
                {
                    logger.info("SocketException", se);
                }
            }
        }
    }

    protected final org.omg.CORBA.COMM_FAILURE to_COMM_FAILURE(IOException exception, Socket socket)
    {
        SSLListenerUtil.processException(orb, this, socket, exception);

        return to_COMM_FAILURE(exception);
    }

    protected synchronized int getTimeout()
    {
        try
        {
            return socket.getSoTimeout();
        }
        catch (SocketException ex)
        {
            throw to_COMM_FAILURE (ex, socket);
        }
    }
}
