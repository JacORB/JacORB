package org.jacorb.test.orb.connection;

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

import java.util.Properties;
import org.jacorb.test.TestIf;
import org.jacorb.test.TestIfHelper;
import org.jacorb.test.harness.ClientServerSetup;
import org.jacorb.test.harness.ClientServerTestCase;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Nicolas Noffke
 */
public class ServerConnectionTimeoutTest extends ClientServerTestCase
{
    private TestIf server;

    @Before
    public void setUp() throws Exception
    {
        server = TestIfHelper.narrow( setup.getServerObject() );
    }

    @BeforeClass
    public static void beforeClassSetUp() throws Exception
    {

        Properties server_props = new Properties();
        server_props.setProperty( "jacorb.connection.server.timeout", "1000" );

        setup = new ClientServerSetup(ConnectionTimeoutServerImpl.class.getName(),
                                   null,
                                   server_props );

    }

    @Test
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
