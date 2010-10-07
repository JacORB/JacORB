/*
 *        JacORB  - a free Java ORB
 *
 *   Copyright (C) 1997-2006 The JacORB project.
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

package org.jacorb.test.bugs.bug879;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;

import org.jacorb.orb.iiop.ServerIIOPConnection;
import org.jacorb.test.common.ORBTestCase;

/**
 * @author Alexander Birchenko
 */
public class Bug879Test extends ORBTestCase
{
    protected void patchORBProperties(Properties props)
    {
        props.setProperty("jacorb.giop_minor_version", "1");
    }

    public void testInitGiop1_1() throws IOException
    {
        ServerSocket sSocket = new ServerSocket(1234);
        Socket socket = new Socket((String)null, 1234);

        ServerIIOPConnection serverIIOPConnection = new ServerIIOPConnection(socket, false, null);
        serverIIOPConnection.configure(((org.jacorb.orb.ORB)orb).getConfiguration());

        assertEquals(1, serverIIOPConnection.get_server_profile().version().minor);

        socket.close();
        sSocket.close();
    }
}
