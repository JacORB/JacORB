package org.jacorb.orb.factory;

/*
 *        Written for JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2004 Gerald Brose, Andre Benvenuti.
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

/**
 * We follow the design of socket factories in package javax.net and
 * javax.net.ssl. Because this package isn't in the JDK yet we don't
 * extend its classes, but we are fully compatible.
 *
 * The basic idea is to setup policies related to the sockets being
 * constructed, in the factory: no special configuration is done in
 * the code which asks for the sockets.
 *
 * @author Andre Benvenuti
 * $Id$
 */

import java.net.*;
import java.io.IOException;

public interface ServerSocketFactory
{

    public ServerSocket createServerSocket ( int port )
        throws IOException;

    /**
     * Returns a server socket which uses all network interfaces on
     * the host, and is bound to the specified port.
     *
     * @param port - the port to listen to
     * @param backlog - how many connections are queued
     * @exception IOException - for networking errors
     */
    public ServerSocket createServerSocket( int port,
                                            int backlog )
        throws IOException;

    /**
     * Returns a server socket which uses all network interfaces on
     * the host, is bound to a the specified port, and uses the
     * specified connection backlog. The socket is configured with the
     * socket options (such as accept timeout) given to this factory.
     *
     * @param port - the port to listen to
     * @param backlog - how many connections are queued
     *
     * @exception IOException - for networking errors
     */
    public ServerSocket createServerSocket( int port,
                                            int backlog,
                                            InetAddress ifAddress )
        throws IOException;
}
