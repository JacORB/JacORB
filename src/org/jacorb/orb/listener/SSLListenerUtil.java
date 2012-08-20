package org.jacorb.orb.listener;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2012 Gerald Brose / The JacORB Team.
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

import java.io.IOException;
import java.net.*;

import javax.net.ssl.*;

import org.jacorb.orb.ORB;
import org.jacorb.orb.iiop.IIOPConnection;
import org.jacorb.orb.iiop.IIOPAddress;
import org.slf4j.Logger;



/**
 * <code>SSLListenerUtil</code> contains utility methods for processing
 * SSL exceptions and adding a listener to a SSLSocket. This class is used
 * with the listeners.
 *
 * @author Nick Cross
 */
public class SSLListenerUtil
{
    /**
     * <code>addListener</code> adds a handshake listener to a specific
     * socket.SSLListenerUtil
     *
     * @param socket a <code>Socket</code> value
     */
    public static void addListener(ORB orb, Socket socket)
    {
         if (socket instanceof SSLSocket)
        {
             SSLSessionListener listener = orb.getTransportManager().getSocketFactoryManager().getSSLListener();
             Logger logger = orb.getConfiguration().getLogger("jacorb.ssl.sessionlistener"); // TODO kategorie
                ((SSLSocket)socket).addHandshakeCompletedListener(new SSLHandshakeListener(logger, listener));
        }
    }


    /**
     * <code>processException</code> examines the supplied exception for an
     * SSLException and can notify a listener.
     *
     * @param ex an <code>IOException</code> value
     */
    public static void processException(ORB orb, IIOPConnection iiop, Socket socket, IOException ex)
    {
        final SSLSessionListener listener = orb.getTransportManager().getSocketFactoryManager().getSSLListener();
        final Logger logger = orb.getConfiguration().getLogger("jacorb.ssl.sessionlistener"); // TODO kategorie

        String localhost = IIOPAddress.getLocalHostAddress(logger);

        // Not nice, but as a javax.net.ssl.SSLException extends
        // java.io.IOException we need to work out which one it is in order
        // to call the correct listener.
        if (ex instanceof SSLHandshakeException)
        {
            listener.handshakeException
            (
                    new SSLSessionEvent
                    (
                            iiop,
                            socket.getInetAddress().getHostAddress(),
                            socket.getPort(),
                            null,
                            socket.getLocalPort(),
                            localhost,
                            ex
                    )
            );
        }
        else if (ex instanceof SSLKeyException)
        {
            listener.keyException
            (
                    new SSLSessionEvent
                    (
                            iiop,
                            socket.getInetAddress().getHostAddress(),
                            socket.getPort(),
                            null,
                            socket.getLocalPort(),
                            localhost,
                            ex
                    )
            );
        }
        else if (ex instanceof SSLPeerUnverifiedException)
        {
            listener.peerUnverifiedException
            (
                    new SSLSessionEvent
                    (
                            iiop,
                            socket.getInetAddress().getHostAddress(),
                            socket.getPort(),
                            null,
                            socket.getLocalPort(),
                            localhost,
                            ex
                    )
            );
        }
        else if (ex instanceof SSLProtocolException)
        {
            listener.protocolException
            (
                    new SSLSessionEvent
                    (
                            iiop,
                            socket.getInetAddress().getHostAddress(),
                            socket.getPort(),
                            null,
                            socket.getLocalPort(),
                            localhost,
                            ex
                    )
            );
        }
        else if (ex instanceof SSLException)
        {
            listener.sslException
            (
                    new SSLSessionEvent
                    (
                            iiop,
                            socket.getInetAddress().getHostAddress(),
                            socket.getPort(),
                            null,
                            socket.getLocalPort(),
                            localhost,
                            ex
                    )
            );
        }
        else
        {
            if (logger.isDebugEnabled())
            {
                logger.debug
                (
                        "Unknown exception type " +
                        ex.getClass().getName() +
                        " with exception " +
                        ex
                );
            }
        }
    }
}