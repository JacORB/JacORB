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

import java.util.EventListener;


/**
 * The <code>SSLSessionListener</code> interface defines methods for a
 * developer to implement in order to receive notifications of SSL
 * events from JacORB.
 *
 * @author Nick Cross
 * @version $Id$
 */
public interface SSLSessionListener extends EventListener
{
    /**
     * <code>sessionCreated</code> is called whenever a successful SSL
     * connection has been made.
     *
     * @param e a <code>SSLSessionEvent</code> value
     */
    void sessionCreated(SSLSessionEvent e);


    /**
     * <code>handshakeException</code> is called whenever the client
     * and server could not negotiate the desired level of security.
     *
     * @param e a <code>SSLSessionEvent</code> value
     */
    void handshakeException(SSLSessionEvent e);


    /**
     * <code>keyException</code> is called whenever a bad SSL key is
     * reported. Normally, this indicates misconfiguration of the server or
     * client SSL certificate and private key.

     *
     * @param e a <code>SSLSessionEvent</code> value
     */
    void keyException(SSLSessionEvent e);


    /**
     * <code>peerUnverifiedException</code> is called whenever the peer was not
     * able to identify itself (for example; no certificate, the particular
     * cipher suite being used does not support authentication, or no peer
     * authentication was established during SSL handshaking)
     *
     * @param e a <code>SSLSessionEvent</code> value
     */
    void peerUnverifiedException(SSLSessionEvent e);


    /**
     * <code>protocolException</code> is called whenever there is an error in
     * the operation of the SSL protocol. Normally this indicates a flaw in one
     * of the protocol implementations.
     *
     * @param e a <code>SSLSessionEvent</code> value
     */
    void protocolException(SSLSessionEvent e);


    /**
     * <code>sslException</code> is called whenever there is an error in the SSL
     * subsystem. This may be called if the error does not fit into one of the above
     * categories.
     *
     * @param e a <code>SSLSessionEvent</code> value
     */
    void sslException(SSLSessionEvent e);
}
