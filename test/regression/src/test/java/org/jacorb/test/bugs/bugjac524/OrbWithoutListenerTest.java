package org.jacorb.test.bugs.bugjac524;

/*
 *        JacORB  - a free Java ORB
 *
 *   Copyright (C) 1997-2014 Gerald Brose / The JacORB Team.
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


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Properties;
import javax.net.ssl.SSLSocket;
import org.jacorb.test.BasicServer;
import org.jacorb.test.BasicServerHelper;
import org.jacorb.test.harness.ClientServerSetup;
import org.jacorb.test.harness.FixedPortClientServerTestCase;
import org.jacorb.test.harness.TestUtils;
import org.jacorb.test.orb.BasicServerImpl;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Alphonse Bendt
 */
public class OrbWithoutListenerTest extends FixedPortClientServerTestCase
{
    private BasicServer server;
    private static int port = getNextAvailablePort();

    @BeforeClass
    public static void beforeClassSetUp() throws Exception
    {
        Properties clientProps = new Properties();

        clientProps.setProperty("OAPort", Integer.toString(port));
        clientProps.setProperty("jacorb.transport.server.listeners", "off");
        setup = new ClientServerSetup(BasicServerImpl.class.getName(), clientProps, null);
    }

    @Before
    public void setUp() throws Exception
    {
        server = BasicServerHelper.narrow(setup.getServerObject());
    }

    @Test
    public void testCanConnectToOtherServer()
    {
        long now = System.currentTimeMillis();
        assertEquals(now, server.bounce_long_long(now));
    }

    @Test
    public void testORBDoesNotOpenListenSocket() throws Exception
    {
        server.ping();

        Socket socket = new Socket();
        try
        {
            socket.connect(new InetSocketAddress("localhost", port), TestUtils.isWindows () ? 5000 : 1000);

            if ( ! (socket instanceof SSLSocket) && ! socket.isClosed ())
            {
                socket.shutdownOutput ();
            }
            fail();
        }
        catch(ConnectException e)
        {
            // expected
        }
        socket.close ();
    }
}
