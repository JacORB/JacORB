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
/*
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

import java.util.EventObject;

import org.jacorb.orb.iiop.IIOPConnection;

/**
 * <code>TCPConnectionEvent</code> defines an event state object for a
 * TCP Socket Connection.
 *
 * @author Nick Cross
 * @version $Id$
 */
public class TCPConnectionEvent extends EventObject
{
    /**
     * <code>remoteIP</code> is the remote IP the socket is connecting to.
     */
    private final String remoteIP;


    /**
     * <code>remotePort</code> is the remote port the socket is connecting to.
     */
    private final int remotePort;


    /**
     * <code>localPort</code> is the local port the socket is using.
     */
    private final int localPort;

    /**
     * <code>localIP</code> is the local IP the socket is using.
     */
    private final String localIP;

    /**
     * Creates a new <code>TCPConnectionEvent</code> instance passing in the
     * source of the event and relevant connection data.
     *
     * @param source an <code>Object</code> value
     * @param remoteIP a <code>String</code> value, maybe blank if a Socket has
     *                 just been created and not connected.
     * @param remotePort an <code>int</code> value, maybe -1 if a Socket has
     *                 just been created and not connected.
     * @param localPort an <code>int</code> value
     * @param localIP a <String> value representing the local IP address
     * @deprecated use the other c'tor
     * TODO remove this c'tor. change callers to use the other one
     */
    public TCPConnectionEvent
        (Object source,
         String remoteIP,
         int remotePort,
         int localPort,
         String localIP)
    {
        super (source);

        this.remoteIP   = remoteIP;
        this.remotePort = remotePort;
        this.localPort  = localPort;
        this.localIP    = localIP;
    }

    public TCPConnectionEvent
        (IIOPConnection source,
         String remoteIP,
         int remotePort,
         int localPort,
         String localIP)
    {
        this((Object)source, remoteIP, remotePort, localPort, localIP);
    }


    /**
     * <code>getRemoteIP</code> is an accessor for the remote IP.
     *
     * @return a <code>String</code> value
     */
    public String getRemoteIP()
    {
        return remoteIP;
    }


    /**
     * <code>getRemotePort</code> is an accessor for the remote port.
     *
     * @return a <code>int</code> value
     */
    public int getRemotePort()
    {
        return remotePort;
    }


    /**
     * <code>getLocalPort</code> is an accessor for the local port.
     *
     * @return a <code>int</code> value
     */
    public int getLocalPort()
    {
        return localPort;
    }

    /**
     * <code>getLocalIP</code> is an accessor for the local IP.
     *
     * @return a <code>String</code> value
     */
    public String getLocalIP()
    {
        return localIP;
    }

    /**
     * Returns a String representation of this EventObject.
     *
     * @return  A String representation of this EventObject.
     */
    public String toString()
    {
        return (super.toString() + " with local port " + localPort
                + " and local IP " + localIP
                + " and remote port " + remotePort + " and remote IP "
                + remoteIP);
    }
}
