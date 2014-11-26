package org.jacorb.test.bugs.bugjac195;

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
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Properties;
import org.jacorb.test.harness.ClientServerSetup;
import org.jacorb.test.harness.FixedPortClientServerTestCase;
import org.jacorb.test.harness.TestUtils;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * <code>TestCaseImpl</code> is a test to check that connectionOpened and
 * connectionClosed events are reported correctly
 *
 * @author Carol Jordon
 */
public class BugJac195Test extends FixedPortClientServerTestCase
{
    private JAC195Server server;

    private static int port;

    @Before
    public void setUp() throws Exception
    {
        server = JAC195ServerHelper.narrow( setup.getServerObject() );
    }

    @BeforeClass
    public static void beforeClassSetUp() throws Exception
    {
        Assume.assumeFalse(TestUtils.isSSLEnabled);

        Properties client_props = new Properties();
        Properties server_props = new Properties();

        port = getNextAvailablePort();

        server_props.setProperty ("OAPort", Integer.toString(port));

        server_props.setProperty ("jacorb.net.tcp_listener",
                                  "org.jacorb.test.bugs.bugjac195.TCPListener");
        setup = new ClientServerSetup(
                  JAC195ServerImpl.class.getName(),
                  client_props,
                  server_props);
    }

    /**
     * <code>test_connections</code>
     *
     * Tests that the correct number of connectionOpen and connectionClosed calls are made
     */
    @Test
    public void test_connections() throws Exception
    {
        String ipAddress;

        try
        {
            ipAddress = InetAddress.getLocalHost().getHostAddress();
        }
        catch (UnknownHostException uhe)
        {
            ipAddress = "127.0.0.1";
        }

        assertEquals("Should be no connections opened", 1,
                    server.getConnectionsOpened());

        assertEquals("Should be no connections closed", 0,
                    server.getConnectionsClosed());

        for (int i = 0; i < 5; i++)
        {
            Socket sock = new Socket (InetAddress.getByName(ipAddress),
                    port);

            sock.close();

            try
            {
                Thread.sleep(1000);
            }
            catch (InterruptedException ie)
            {
                // ignored
            }
        }

        assertEquals("Incorrect number of connections opened", 6,
                server.getConnectionsOpened());

        assertEquals("Incorrect number of connections closed", 5,
                server.getConnectionsClosed());
    }
}
