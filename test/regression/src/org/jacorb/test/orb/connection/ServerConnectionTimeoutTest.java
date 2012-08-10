package org.jacorb.test.orb.connection;

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
import org.jacorb.test.TestIf;
import org.jacorb.test.TestIfHelper;
import org.jacorb.test.common.ClientServerSetup;
import org.jacorb.test.common.ClientServerTestCase;
import org.jacorb.test.common.TestUtils;

/**
 * @author Nicolas Noffke
 */
public class ServerConnectionTimeoutTest extends ClientServerTestCase
{
    private TestIf server;

    public ServerConnectionTimeoutTest(String name, ClientServerSetup setup)
    {
        super(name, setup);
    }

    public void setUp() throws Exception
    {
        server = TestIfHelper.narrow( setup.getServerObject() );
    }

    protected void tearDown() throws Exception
    {
        server = null;
    }

    public static Test suite()
    {
        TestSuite suite = new TestSuite( "Server connection idle-timeout tests" );

        Properties server_props = new Properties();
        server_props.setProperty( "jacorb.connection.server.timeout", "1000" );

        ClientServerSetup setup =
            new ClientServerSetup( suite,
                                   ConnectionTimeoutServerImpl.class.getName(),
                                   null,
                                   server_props );

        TestUtils.addToSuite(suite, setup, ServerConnectionTimeoutTest.class);

        return setup;
    }

    public void testTimeout() throws Exception
    {
        //call remote op with reply
        server.op();

        ClientConnectionTimeoutTest.verifyOpenTransports(0, 4000);

        //call oneway remote op
        server.onewayOp();

        ClientConnectionTimeoutTest.verifyOpenTransports(0, 4000);
    }
}
