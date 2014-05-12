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

/**
 * empty implementation of the SSLSessionListener interface
 *
 * @author Alphonse Bendt
 */
public class NullSSLSessionListener implements SSLSessionListener
{
    public void sessionCreated(SSLSessionEvent e)
    {
        // empty implementation
    }

    public void handshakeException(SSLSessionEvent e)
    {
        // empty implementation
    }

    public void keyException(SSLSessionEvent e)
    {
        // empty implementation
    }

    public void peerUnverifiedException(SSLSessionEvent e)
    {
        // empty implementation
    }

    public void protocolException(SSLSessionEvent e)
    {
        // empty implementation
    }

    public void sslException(SSLSessionEvent e)
    {
        // empty implementation
    }
}
