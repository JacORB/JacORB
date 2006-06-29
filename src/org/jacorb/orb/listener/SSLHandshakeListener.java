/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) The JacORB project, 1997-2006.
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

package org.jacorb.orb.listener;

import javax.net.ssl.HandshakeCompletedEvent;
import javax.net.ssl.HandshakeCompletedListener;

import javax.security.cert.X509Certificate;
import javax.net.ssl.SSLPeerUnverifiedException;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.avalon.framework.logger.Logger;

/**
 * <code>SSLHandshakeListener</code> implements the SSL Handshake Listener
 * in order to detect a successful SSL connection. It then passes this information
 * on to the external SSLListener.
 *
 * @author Nick Cross
 * @version $Id$
 */
public class SSLHandshakeListener implements HandshakeCompletedListener
{
    private final Logger logger;
    private final SSLSessionListener sslListener;

    public SSLHandshakeListener(Logger logger, SSLSessionListener listener)
    {
        this.logger = logger;
        this.sslListener = listener;
    }

    /**
     * <code>handshakeCompleted</code> is the implementation that is invoked
     * when a SSL handshake is completed.
     * The getPeerCertificateChain method is used to ensure backward
     * compatibility with JDK 1.3
     * @param event a <code>HandshakeCompletedEvent</code> value
     */
    public void handshakeCompleted(HandshakeCompletedEvent event)
    {
        X509Certificate [] certs = null;
        String localhost = null;

        try
        {
            certs = event.getPeerCertificateChain();
        }
        catch (SSLPeerUnverifiedException ex)
        {
            if (logger.isDebugEnabled())
            {
                logger.debug
                    ("handshakeCompleted - SSLPeerUnverifiedException");
            }

            certs = new X509Certificate [0];
        }

        try
        {
            localhost = InetAddress.getLocalHost().getHostAddress();
        }
        catch (UnknownHostException uhe)
        {
            if (logger.isDebugEnabled())
            {
                logger.debug
                    ("Unable to resolve local IP address - using default");
            }

            localhost = "127.0.0.1";
        }

        sslListener.sessionCreated
        (
            new SSLSessionEvent
            (
                event.getSource(),
                event.getSocket().getInetAddress().getHostAddress(),
                event.getSocket().getPort(),
                certs,
                event.getSocket().getLocalPort(),
                localhost,
                null
            )
        );
    }
}
