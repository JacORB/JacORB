package org.jacorb.test.orb;

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

import java.util.Properties;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.jacorb.test.BasicServer;
import org.jacorb.test.BasicServerHelper;
import org.jacorb.test.common.ClientServerSetup;
import org.jacorb.test.common.ClientServerTestCase;
import org.jacorb.test.common.JacORBTestSuite;

/**
 * Tests the configuration option jacorb.iiop.alternate_addresses.
 * @author Andre Spiegel <spiegel@gnu.org>
 */
public class AlternateIIOPAddressConfigTest extends ClientServerTestCase
{
    private static final String CORRECT_HOST = "127.0.0.1";
    private static final String WRONG_HOST = "255.255.255.254";

    private static final int CORRECT_PORT = 12435;
    private static final int WRONG_PORT = 12436;

    private BasicServer server;

    public AlternateIIOPAddressConfigTest(String name, ClientServerSetup setup)
    {
        super(name, setup);
    }

    public void setUp() throws Exception
    {
        server = BasicServerHelper.narrow( setup.getServerObject() );
    }

    protected void tearDown() throws Exception
    {
        server = null;
    }

    public static Test suite()
    {
        TestSuite suite = new JacORBTestSuite ("AlternateIIOPAddress Configuration Test",
                                               AlternateIIOPAddressConfigTest.class);

        Properties clientProps = new Properties();
        clientProps.setProperty ("jacorb.retries", "0");
        clientProps.setProperty ("jacorb.retry_interval", "50");
        clientProps.setProperty ("jacorb.connection.client.connect_timeout","5000");

        Properties serverProps = new Properties();
        // make sure the server-side adapter listens on the following address...
        serverProps.put ("OAAddress", "iiop://" + CORRECT_HOST + ":" + CORRECT_PORT);
        // ... but make the primary address in the URL wrong ...
        serverProps.put ("jacorb.ior_proxy_host", WRONG_HOST);
        // ... so that the alternate addresses get used
        serverProps.put ("jacorb.iiop.alternate_addresses",
                         CORRECT_HOST + ":" + WRONG_PORT + "," +
                         CORRECT_HOST + ":" + CORRECT_PORT);
        ClientServerSetup setup =
            new ClientServerSetup( suite,
                                   "org.jacorb.test.orb.BasicServerImpl",
                                   clientProps, serverProps);

        suite.addTest( new AlternateIIOPAddressConfigTest( "test_ping", setup ));

        return setup;
    }

    public void test_ping()
    {
        server.ping();
    }

}
