package org.jacorb.orb.factory;

/*
 *        Written for JacORB - a free Java ORB
 *
 *   Copyright (C) 2000-2012 Gerald Brose / The JacORB Team.
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
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * the default SocketFactory implementation.
 *
 * @author Steve Osselton
 */
public class DefaultSocketFactory extends AbstractSocketFactory
{
    public Socket createSocket (String host, int port)
        throws IOException, UnknownHostException
    {
        return new Socket (host, port);
    }

    protected Socket doCreateSocket(String host, int port, int timeout) throws IOException, UnknownHostException
    {
        final Socket socket = new Socket();

        socket.connect(new InetSocketAddress(host, port), timeout);

        return socket;
    }

    public boolean isSSL(Socket socket)
    {
        return false;
    }
}
