package org.jacorb.test.orb.connection;

/*
 *        JacORB  - a free Java ORB
 *
 *   Copyright (C) 1997-2001  Gerald Brose.
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

import junit.framework.*;
import junit.extensions.*;

import org.jacorb.test.common.*;
import org.jacorb.test.*;
import org.omg.CORBA.*;
import org.jacorb.orb.iiop.ClientIIOPConnection;

import java.util.*;

public class ClientConnectionTimeoutTest extends ClientServerTestCase
{
    private TestIf server;

    public ClientConnectionTimeoutTest(String name, ClientServerSetup setup)
    {
        super(name, setup);
    }

    public void setUp() throws Exception
    {
        server = TestIfHelper.narrow( setup.getServerObject() );
    }

    public static Test suite()
    {
        TestSuite suite = new TestSuite( "Client connection idle-timeout tests" );

        Properties client_props = new Properties();
        client_props.setProperty( "jacorb.connection.client.idle_timeout", "1000" );

        ClientServerSetup setup =
            new ClientServerSetup( suite,
                                   "org.jacorb.test.orb.connection.ConnectionTimeoutServerImpl",
                                   client_props,
                                   null );

        suite.addTest( new ClientConnectionTimeoutTest( "testTimeout", setup ));

        return setup;
    }

    public void testTimeout()
    {

        //call remote op with reply
        server.op();

        try
        {
            //wait 2 secs
            Thread.sleep( 2000 );
        }
        catch( Exception e ){ e.printStackTrace(); }

        //all transports must be down by now
        //NOTE: if this doesn't compile, please check if
        //openTransports is uncommented in ClientIIOPConnection
        assertTrue( ClientIIOPConnection.openTransports == 0 );

        //call oneway remote op
        server.onewayOp();

        try
        {
            //wait 2 secs
            Thread.sleep( 2000 );
        }
        catch( Exception e ){ e.printStackTrace(); }

        //all transports must be down by now
        //NOTE: if this doesn't compile, please check if
        //openTransports is uncommented in ClientIIOPConnection
        assertTrue( ClientIIOPConnection.openTransports == 0 );
    }
}
