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

/* We follow the design of socket factories in jsse (package javax.net and javax.net.ssl).
 * Because this package don't the JDK yet, and not exported in Europe, we don't extend its classes.
 * But we remain compatible, and could actualy use the jsse instead.
 *
 * The basic idea is to setup policies related to the sockets being constructed,
 * in the factory: no special configuration is done in the code which asks for the sockets.
 *
 * We will use the polymorphism of both factories and sockets, to enable poeple
 * to use different SSL implementations. The ORB will get different kinds of factories.
 * The Factories will customize the special parameters used in socket construction.
 * But the sockets returned to the application have to be subclasses of java.net.Socket,
 * Which factory classes is used will be decide in org.jacorb.util.Environment as this is specific
 * to the environment configuration.
 * So the getDefault method could return null if no SSL support at all, or a factory that encapsulates
 * a particular implementation and take care of initialising and pass specific parameters.
 */

import java.io.IOException;
import java.net.Socket;
import org.omg.CORBA.TIMEOUT;


/**
 * SocketFactory is used by JacORB to create the client-side socket
 * for an outgoing connection.
 *
 * @author Gerald Brose
 */

public interface SocketFactory
{
    /**
     * create a connected stream Socket.
     *
     * @param host the host name
     * @param port the port number
     * @return a connected stream Socket
     * @throws IOException
     */
    Socket createSocket(String host,
                        int port )
        throws IOException;

    /**
     * create a connected stream Socket.
     * Compliant implementations must ensure to throw org.omg.CORBA.TIMEOUT
     * in case a timeout occurs
     * instead of the SocketTimeoutException thats available in the JDK.
     *
     * @param host the host name
     * @param port the port number
     * @param timeout the timeout value to be used in milliseconds
     * @return a connected stream Socket
     * @throws IOException
     * @throws TIMEOUT if a timeout occurs during connect.
     */
    Socket createSocket(String host, int port, int timeout)
        throws IOException, TIMEOUT;

    /**
     * @return true if the specified socket supports SSL.
     */
    boolean isSSL( Socket socket );
}
