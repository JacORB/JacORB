package org.jacorb.test.bugs.bugjac359;

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
import org.jacorb.test.common.TestUtils;
import org.jacorb.test.orb.BasicServerImpl;

public class BugJac359Test extends ClientServerTestCase
{
    private BasicServer server;

    public BugJac359Test(String name, ClientServerSetup setup)
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
        TestSuite suite = new JacORBTestSuite("Basic client/server tests",
                BugJac359Test.class);

        Properties clientProps = new Properties();

        // this property will cause the handshake between client and server to fail!
        clientProps.setProperty("jacorb.security.ssl.client.cipher_suites", "SSL_RSA_WITH_RC4_128_MD5");

        ClientServerSetup setup =
            new ClientServerSetup( suite,
                    BasicServerImpl.class.getName(),
                    clientProps,
                    null);

        if (setup.isSSLEnabled())
        {
            TestUtils.addToSuite(suite, setup, BugJac359Test.class);
        }
        else
        {
            System.err.println("Test ignored as SSL is not enabled (" + BugJac359Test.class.getName() + ")");
        }

        return setup;
    }

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
        thread.join(2000);

        assertFalse(result[0]);
        assertNotNull(exception[0]);

        thread.interrupt();
    }
}
