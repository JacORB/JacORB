package org.jacorb.test.orb;

/*
 *        JacORB  - a free Java ORB
 *
 *   Copyright (C) 1997-2005  Gerald Brose.
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

import java.util.Properties;

import junit.framework.*;

import org.jacorb.test.*;
import org.jacorb.test.common.*;

/**
 * Tests components of type TAG_ALTERNATE_IIOP_ADDRESS within IORs.
 *
 * @jacorb-since 2.2
 * @author Andre Spiegel
 * @version $Id$
 */
public class AlternateIIOPAddressTest extends ClientServerTestCase
{
    protected IIOPAddressServer server = null;

    private static final String CORRECT_HOST = "127.0.0.1";
    private static final String WRONG_HOST   = "10.0.1.222";
    private static final String WRONG_HOST_2 = "10.0.1.223";

    private static final int CORRECT_PORT = 46000;
    private static final int WRONG_PORT   = 46001;

    public AlternateIIOPAddressTest(String name, ClientServerSetup setup)
    {
        super(name, setup);
    }

    protected void setUp() throws Exception
    {
        server = IIOPAddressServerHelper.narrow(setup.getServerObject());
    }

    protected void tearDown() throws Exception
    {
        // server.clearSocketAddress();
        server.setIORAddress (CORRECT_HOST, CORRECT_PORT);
        server.clearAlternateAddresses();
    }

    public static Test suite()
    {
        TestSuite suite = new JacORBTestSuite("Test TAG_ALTERNATE_IIOP_ADDRESS ",
                                              AlternateIIOPAddressTest.class);

        Properties client_props = new Properties();
        client_props.setProperty ("jacorb.retries", "0");
        client_props.setProperty ("jacorb.retry_interval", "50");
        client_props.setProperty ("jacorb.connection.client.connect_timeout","5000");

        Properties server_props = new Properties();
        server_props.setProperty
            ("org.omg.PortableInterceptor.ORBInitializerClass."
           + "org.jacorb.test.orb.IIOPAddressORBInitializer", "");
        server_props.setProperty ("OAPort", Integer.toString(CORRECT_PORT));

        // If security is not disabled it will not use the above host/port
        // combinations.
        client_props.setProperty("jacorb.regression.disable_security",
                                 "true");


        ClientServerSetup setup =
            new ClientServerSetup (suite,
                                   IIOPAddressServerImpl.class.getName(),
                                   client_props,
                                   server_props);

        TestUtils.addToSuite(suite, setup, AlternateIIOPAddressTest.class);

        return setup;
    }

    public void test_ping()
    {
        Sample sample = server.getObject();
        int result = sample.ping (17);
        assertEquals (18, result);
    }

    public void test_primary_ok()
    {
        server.setIORAddress( CORRECT_HOST, CORRECT_PORT );
        Sample s = server.getObject();
        int result = s.ping (77);
        assertEquals (78, result);
    }

    public void test_primary_wrong_host()
    {
        server.setIORAddress( WRONG_HOST, CORRECT_PORT );
        Sample sample = server.getObject();
        try
        {
            sample.ping (123);
            fail ("TRANSIENT or TIMEOUT exception expected");
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

    public void test_primary_wrong_port()
    {
        server.setIORAddress( CORRECT_HOST, WRONG_PORT );
        Sample sample = server.getObject();
        try
        {
            sample.ping (4);
            fail ("TRANSIENT or TIMEOUT exception expected");
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

    public void test_alternate_ok()
    {
        server.setIORAddress( WRONG_HOST, CORRECT_PORT );
        server.addAlternateAddress( CORRECT_HOST, CORRECT_PORT );
        Sample sample = server.getObject();
        int result = sample.ping (99);
        assertEquals (100, result);
    }

    public void test_alternate_ok_2()
    {
        server.setIORAddress( WRONG_HOST, CORRECT_PORT );
        server.addAlternateAddress( WRONG_HOST_2, CORRECT_PORT );
        server.addAlternateAddress( CORRECT_HOST, CORRECT_PORT );
        Sample sample = server.getObject();
        int result = sample.ping (187);
        assertEquals (188, result);
    }

    public void test_alternate_wrong()
    {
        server.setIORAddress( CORRECT_HOST, WRONG_PORT );
        server.addAlternateAddress( WRONG_HOST, CORRECT_PORT );
        server.addAlternateAddress( WRONG_HOST_2, WRONG_PORT );
        server.addAlternateAddress( WRONG_HOST_2, WRONG_PORT );
        Sample sample = server.getObject();

        try
        {
            sample.ping (33);
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
