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
import org.jacorb.test.BasicServer;
import org.jacorb.test.BasicServerHelper;
import org.jacorb.test.harness.ClientServerSetup;
import org.jacorb.test.harness.ClientServerTestCase;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests the "auto" value in the configuration option
 * jacorb.iiop.alternate_addresses.  In the default form, this test only
 * makes sure that the option does not cause any havoc, but the primary
 * address will still be used to connect to the server.  See comments below
 * in the suite() method how to change that (if your test machine supports it).
 *
 * @author Andre Spiegel <spiegel@gnu.org>
 */
public class AlternateIIOPAddressAutoTest extends ClientServerTestCase
{
    private BasicServer server;

    @Before
    public void setUp() throws Exception
    {
        server = BasicServerHelper.narrow( setup.getServerObject() );
    }

    @BeforeClass
    public static void beforeClassSetUp() throws Exception
    {
        Properties clientProps = new Properties();
        clientProps.setProperty ("jacorb.retries", "0");
        clientProps.setProperty ("jacorb.retry_interval", "50");
        clientProps.setProperty ("jacorb.connection.client.connect_timeout","5000");

        Properties serverProps = new Properties();
        // Uncomment the following line to replace the primary address with
        // a bogus one.  If there is a valid alternate address, that will
        // be used to make the connection.  Commented out because not every
        // test machine has such a setup.
        // serverProps.put ("jacorb.ior_proxy_host", WRONG_HOST);
        serverProps.put ("jacorb.iiop.alternate_addresses", "auto");
        setup = new ClientServerSetup("org.jacorb.test.orb.BasicServerImpl",
                                   clientProps, serverProps);
    }

    @Test
    public void test_ping()
    {
        server.ping();
    }

}
