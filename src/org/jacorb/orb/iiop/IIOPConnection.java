/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2012 Gerald Brose / The JacORB Team.
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
import java.net.Socket;
import java.net.SocketException;
import org.jacorb.orb.etf.StreamConnectionBase;
import org.jacorb.orb.listener.SSLListenerUtil;
import org.omg.CORBA.COMM_FAILURE;


/**
 * @author Nicolas Noffke
 * @author Andre Spiegel
 */
public abstract class IIOPConnection
    extends StreamConnectionBase
{
    protected Socket socket;

    protected boolean use_ssl;

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

    protected COMM_FAILURE handleCommFailure(IOException e)
    {
        SSLListenerUtil.processException(orb, this, socket, e);

        return to_COMM_FAILURE(e);
    }

    protected synchronized int getTimeout()
    {
        try
        {
            return socket.getSoTimeout();
        }
        catch (SocketException ex)
        {
            throw handleCommFailure(ex);
        }
    }

    /**
     * <code>hashCode</code> returns the hash code value for the object. It
     * will return the hashCode of the underlying socket. If the socket is null
     * or closed it will return hash of the IIOPConnection itself.
     *
     * Note - if this is changed this may break the context minikey system.
     *
     * @return an <code>int</code> value
     */
    public int hashCode()
    {
        // Can't use socket.isClosed as does not exist in 1.3
        if (socket == null || (!connected))
        {
            return super.hashCode();
        }
        return socket.hashCode();
    }

    public Socket getSocket() 
    {
        return socket;
    }
}
