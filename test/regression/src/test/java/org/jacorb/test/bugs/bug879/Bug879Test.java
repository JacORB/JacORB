/*
 *        JacORB  - a free Java ORB
 *
 *   Copyright (C) 1997-2012 Gerald Brose / The JacORB Team.
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

import static org.junit.Assert.assertEquals;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;
import org.jacorb.orb.iiop.ServerIIOPConnection;
import org.jacorb.test.common.ORBTestCase;
import org.junit.Test;

/**
 * @author Alexander Birchenko
 */
public class Bug879Test extends ORBTestCase
{
    @Override
    protected void patchORBProperties(Properties props) throws Exception
    {
        props.setProperty("jacorb.giop_minor_version", "1");
    }

    @Test
    public void testInitGiop1_1() throws Exception
    {
        ServerSocket sSocket = new ServerSocket(4321);
        Socket socket = new Socket((String)null, 4321);

        ServerIIOPConnection serverIIOPConnection = new ServerIIOPConnection(socket, false, null);
        serverIIOPConnection.configure(((org.jacorb.orb.ORB)orb).getConfiguration());

        assertEquals(1, serverIIOPConnection.get_server_profile().version().minor);

        socket.close();
        sSocket.close();
    }
}
