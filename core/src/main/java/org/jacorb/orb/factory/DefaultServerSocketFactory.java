package org.jacorb.orb.factory;

/*
 *        Written for JacORB - a free Java ORB
 *
 *   Copyright (C) 2000-2014 Gerald Brose / The JacORB Team.
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

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;

/**
 * @author Steve Osselton
 */
public class DefaultServerSocketFactory implements ServerSocketFactory
{
    public ServerSocket createServerSocket (int port)
        throws IOException
    {
        return new ServerSocket (port);
    }

    public ServerSocket createServerSocket (int port, int backlog)
        throws IOException
    {
        return new ServerSocket (port, backlog);
    }

    public ServerSocket createServerSocket
        (int port, int backlog, InetAddress ifAddress)
        throws IOException
    {
        return new ServerSocket (port, backlog, ifAddress);
    }
}
