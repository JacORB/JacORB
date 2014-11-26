package org.jacorb.test.orb;

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
 *   Software Foundation, 51 Franklin Street, Fifth Floor, Boston,
 *   MA 02110-1301, USA.
 */

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import java.util.Properties;
import org.jacorb.test.IIOPAddressServer;
import org.jacorb.test.IIOPAddressServerHelper;
import org.jacorb.test.Sample;
import org.jacorb.test.harness.ClientServerSetup;
import org.jacorb.test.harness.FixedPortClientServerTestCase;
import org.jacorb.test.harness.TestUtils;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests components of type TAG_ALTERNATE_IIOP_ADDRESS within IORs.
 *
 * @author Andre Spiegel
 */
public class AlternateIIOPAddress2Test extends FixedPortClientServerTestCase
{
    protected IIOPAddressServer server = null;

    private static final String CORRECT_HOST = "127.0.0.1";

    // pick really bogus host addresses
    private static final String WRONG_HOST   = "255.255.255.253";
    private static final String WRONG_HOST_2 = "255.255.255.254";

    private static final int CORRECT_PORT = getNextAvailablePort();
    private static final int WRONG_PORT   = getNextAvailablePort();

    private static final String PROTOCOL = "iiop://";

    private static final String LISTEN_EP = PROTOCOL + "localhost:" + CORRECT_PORT;



    @Before
    public void setUp() throws Exception
    {
        server = IIOPAddressServerHelper.narrow(setup.getServerObject());
    }

    @After
    public void tearDown() throws Exception
    {
        // server.clearSocketAddress();
        server.setIORProtAddr (PROTOCOL + CORRECT_HOST + ":" + CORRECT_PORT);
        server.clearAlternateAddresses();
        server = null;
    }

    @BeforeClass
    public static void beforeClassSetUp() throws Exception
    {
        // If security is not disabled it will not use the above host/port
        // combinations.
        Assume.assumeFalse(TestUtils.isSSLEnabled);

        Properties client_props = new Properties();
        client_props.setProperty ("jacorb.retries", "0");
        client_props.setProperty ("jacorb.retry_interval", "50");
        client_props.setProperty ("jacorb.connection.client.connect_timeout","5000");

        Properties server_props = new Properties();
        server_props.setProperty
            ("org.omg.PortableInterceptor.ORBInitializerClass."
           + "org.jacorb.test.orb.IIOPAddressORBInitializer", "");
        server_props.setProperty ("OAAddress", LISTEN_EP);

        setup = new ClientServerSetup(
                                   "org.jacorb.test.orb.IIOPAddressServerImpl",
                                   client_props,
                                   server_props);

    }

    @Test
    public void test_ping()
    {
        Sample s = server.getObject();
        int result = s.ping (17);
        assertEquals (18, result);
    }

    @Test
    public void test_primary_ok()
    {
        server.setIORProtAddr (PROTOCOL + CORRECT_HOST + ":" + CORRECT_PORT);
        Sample s = server.getObject();
        int result = s.ping (77);
        assertEquals (78, result);
    }

    @Test
    public void test_primary_wrong_host()
    {
        server.setIORProtAddr (PROTOCOL + WRONG_HOST + ":" + CORRECT_PORT);
        Sample s = server.getObject();
        try
        {
            s.ping (123);
            fail ("TRANSIENT or TIMEOUT  exception expected");
        }
        catch (org.omg.CORBA.TRANSIENT ex)
        {
            // ok - unable to resolve the address
        }
        catch (org.omg.CORBA.TIMEOUT ex)
        {
            // ok - client connection timeout configured.
        }
    }

    @Test
    public void test_primary_wrong_port()
    {
        server.setIORProtAddr (PROTOCOL + CORRECT_HOST + ":" + WRONG_PORT);
        Sample s = server.getObject();
        try
        {
            s.ping (4);
            fail ("TRANSIENT or TIMEOUT  exception expected");
        }
        catch (org.omg.CORBA.TRANSIENT ex)
        {
            // ok - unable to resolve the address
        }
        catch (org.omg.CORBA.TIMEOUT ex)
        {
            // ok - client connection timeout configured.
        }
    }

    @Test
    public void test_alternate_ok()
    {
        server.setIORProtAddr (PROTOCOL + WRONG_HOST + ":" + CORRECT_PORT);
        server.addAlternateAddress( CORRECT_HOST, CORRECT_PORT );
        Sample s = server.getObject();
        int result = s.ping (99);
        assertEquals (100, result);
    }

    @Test
    public void test_alternate_ok_2()
    {
        server.setIORProtAddr (PROTOCOL + WRONG_HOST + ":" + CORRECT_PORT);
        server.addAlternateAddress( WRONG_HOST_2, CORRECT_PORT );
        server.addAlternateAddress( CORRECT_HOST, CORRECT_PORT );
        Sample s = server.getObject();
        int result = s.ping (187);
        assertEquals (188, result);
    }

    @Test
    public void test_alternate_wrong()
    {
        server.setIORProtAddr (PROTOCOL + CORRECT_HOST + ":" + WRONG_PORT);
        server.addAlternateAddress( WRONG_HOST, CORRECT_PORT );
        server.addAlternateAddress( WRONG_HOST_2, WRONG_PORT );
        server.addAlternateAddress( WRONG_HOST_2, WRONG_PORT );
        Sample s = server.getObject();
        try
        {
            s.ping (33);
            fail ("TRANSIENT or TIMEOUT  exception expected");
        }
        catch (org.omg.CORBA.TRANSIENT ex)
        {
            // ok - unable to resolve the address
        }
        catch (org.omg.CORBA.TIMEOUT ex)
        {
            // ok - client connection timeout configured.
        }
    }

}
