package org.jacorb.test.poa;

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

import static org.junit.Assert.assertTrue;
import java.util.Properties;
import org.jacorb.test.BasicServer;
import org.jacorb.test.BasicServerHelper;
import org.jacorb.test.harness.ClientServerSetup;
import org.jacorb.test.harness.ClientServerTestCase;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;


/**
 * <code>BiDirDelegateTest</code> is a test for JAC5; BiDir connections get
 * BAD_INV_ORDER/Delegate not set.
 *
 * @author <a href="mailto:rnc@prismtechnologies.com"></a>
 * @version 1.0
 */
public class BiDirDelegateTest extends ClientServerTestCase
{
    private BasicServer server;

    /**
     * <code>ping</code> is used to denote whether the ping thread
     * successfully completed.
     */
    private boolean ping = false;


    @Before
    public void setUp() throws Exception
    {
        server = BasicServerHelper.narrow( setup.getServerObject() );
    }

    @BeforeClass
    public static void beforeClassSetUp() throws Exception
    {

        Properties client_props = new Properties();
        Properties server_props = new Properties();

        client_props.setProperty ("org.omg.PortableInterceptor.ORBInitializerClass.bidir_init", "org.jacorb.orb.giop.BiDirConnectionInitializer");
        server_props.setProperty ("org.omg.PortableInterceptor.ORBInitializerClass.bidir_init", "org.jacorb.orb.giop.BiDirConnectionInitializer");

        // Note that I have activated BiDir connections through the above
        // property and am using my own ClientServerSetup class.
        setup = new ClientServerSetup("org.jacorb.test.poa.BiDirDelegateTestServerRunner",
                                   "org.jacorb.test.orb.BasicServerImpl",
                                   client_props,
                                   server_props);
    }

    /**
     * <code>test_ping</code> calls the ping method on BasicServer.
     *
     * We have to use a thread as otherwise if the test fails the harness will
     * hang. This allows the test to detect a broken server.
     */
    @Test
    public void test_ping()
    {
        Thread thread = new Thread("BiDirDelegateTest Ping Thread")
        {
            public void run()
            {
                server.ping();
                ping = true;
            }

        };
        thread.start();
        try
        {
            thread.join(3000);
        }
        catch (InterruptedException e) {}

        assertTrue(ping);
    }
}
