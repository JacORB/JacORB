package org.jacorb.test.bugs.bugjac359;

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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import java.util.Properties;
import org.jacorb.test.BasicServer;
import org.jacorb.test.BasicServerHelper;
import org.jacorb.test.harness.ClientServerSetup;
import org.jacorb.test.harness.ClientServerTestCase;
import org.jacorb.test.harness.TestUtils;
import org.jacorb.test.orb.BasicServerImpl;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class BugJac359Test extends ClientServerTestCase
{
    private BasicServer server;

    @Before
    public void setUp() throws Exception
    {
        server = BasicServerHelper.narrow( setup.getServerObject() );
    }

    @After
    public void tearDown() throws Exception
    {
        server._release();;
    }

    @BeforeClass
    public static void beforeClassSetup() throws Exception
    {
        Assume.assumeTrue(TestUtils.isSSLEnabled);

        Properties clientProps = new Properties();

        // this property will cause the handshake between client and server to fail!
        clientProps.setProperty("jacorb.security.ssl.client.cipher_suites", "SSL_RSA_WITH_RC4_128_MD5");

        setup = new ClientServerSetup( BasicServerImpl.class.getName(),
                clientProps,
                null);
    }

    @Test
    public void testHandshakeExceptionDoesNotCauseHang() throws Exception
    {
        final boolean[] result = new boolean[1];
        final Exception[] exception = new Exception[1];

        // due to a bug an exception in the handshake
        // caused the operation to 'hang'
        // therefor we start the operation in an extra
        // thread and check after some time
        // that it has properly caused an exception
        Thread thread = new Thread()
        {
            public void run()
            {
                try
                {
                    server.ping();
                    result[0] = true;
                }
                catch (Exception e)
                {
                    exception[0] = e;
                }
            }
        };

        thread.start();
        thread.join(TestUtils.getMediumTimeout());

        assertFalse(result[0]);
        assertNotNull(exception[0]);

        thread.interrupt();
    }
}
