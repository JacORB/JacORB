package org.jacorb.test.orb;

/*
 *        JacORB  - a free Java ORB
 *
 *   Copyright (C) 1997-2013 Gerald Brose / The JacORB Team.
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

import junit.framework.Test;
import junit.framework.TestSuite;
import org.jacorb.test.BasicServer;
import org.jacorb.test.BasicServerHelper;
import org.jacorb.test.common.ClientServerSetup;
import org.jacorb.test.common.ClientServerTestCase;
import org.jacorb.test.common.TestUtils;
import org.omg.CORBA.OBJECT_NOT_EXIST;
import org.omg.CORBA.ORB;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;


/**
 * @author Nick Cross
 *
 * Verify non_existent works correctly with remote calls
 */
public class NonExistentRemoteTest extends ClientServerTestCase
{
    private BasicServer server;

    public NonExistentRemoteTest(String name, ClientServerSetup setup)
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
        TestSuite suite = new TestSuite( "NonExistentRemoteTest" );

        ClientServerSetup setup = new ClientServerSetup (
            suite,
            NonExistentRemoteTest.class.getName(),
            "org.jacorb.test.orb.BasicServerImpl",
            null,
            null);

        TestUtils.addToSuite(suite, setup, NonExistentRemoteTest.class);

        return setup;
    }

    public void testNonExist() throws Exception
    {
        try
        {
            boolean result = server._non_existent();

            assertTrue (result == true);
        }
        catch (OBJECT_NOT_EXIST e)
        {
            e.printStackTrace();
            fail ("Should not have thrown an exception");
        }
    }

    public static void main(String[] args) throws Exception
    {
        //init ORB
        ORB orb = ORB.init( args, null );

        //init POA
        POA rootPOA = POAHelper.narrow( orb.resolve_initial_references( "RootPOA" ));
        rootPOA.the_POAManager().activate();

        BasicServerImpl servant = new BasicServerImpl();

        rootPOA.activate_object(servant);

        BasicServer server = BasicServerHelper.narrow(rootPOA.servant_to_reference(servant));

        rootPOA.deactivate_object(rootPOA.servant_to_id(servant));

        System.out.println ("SERVER IOR: " + orb.object_to_string(server));
        System.out.flush();

        orb.run();
    }
}
