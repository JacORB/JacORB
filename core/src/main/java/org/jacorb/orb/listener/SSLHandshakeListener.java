/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) Copyright (C) 2000-2012 Gerald Brose / The JacORB Team.
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

import java.security.cert.Certificate;
import javax.net.ssl.HandshakeCompletedEvent;
import javax.net.ssl.HandshakeCompletedListener;
import javax.net.ssl.SSLPeerUnverifiedException;
import org.jacorb.orb.iiop.IIOPAddress;
import org.slf4j.Logger;

/**
 * <code>SSLHandshakeListener</code> implements the SSL Handshake Listener
 * in order to detect a successful SSL connection. It then passes this information
 * on to the external SSLListener.
 *
 * @author Nick Cross
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
     *
     * @param event a <code>HandshakeCompletedEvent</code> value
     */
    public void handshakeCompleted(HandshakeCompletedEvent event)
    {
        Certificate[] certs = null;
        String localhost = null;

        try
        {
            certs = event.getPeerCertificates();
        }
        catch (SSLPeerUnverifiedException ex)
        {
            if (logger.isDebugEnabled())
            {
                logger.debug
                    ("handshakeCompleted - SSLPeerUnverifiedException", ex);
            }

            certs = new Certificate[0];
        }

        localhost = IIOPAddress.getLocalHostAddress (logger);

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
