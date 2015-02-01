package org.jacorb.test.orb;

/*
 *        JacORB  - a free Java ORB
 *
 *   Copyright (C) 2000-2014 Gerald Brose / The JacORB Team.
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
import org.jacorb.test.harness.FixedPortClientServerTestCase;
import org.jacorb.test.harness.ORBTestCase;
import org.jacorb.test.harness.TestUtils;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.omg.CORBA.OBJECT_NOT_EXIST;
import org.omg.CORBA.ORB;

import static org.junit.Assert.assertTrue;

public class ClientReconnectionTest extends FixedPortClientServerTestCase
{
    private Properties serverProps = new Properties();
    private ORBTestCase clientORBTestCase = new ORBTestCase () {};
    private ORB clientOrb;

    @Before
    public void setUp() throws Exception
    {
        clientORBTestCase.ORBSetUp ();
        clientOrb = clientORBTestCase.getAnotherORB(null);

        serverProps.put ("OAPort", Integer.toString(getNextAvailablePort()));
        serverProps.put ("jacorb.implname", "");

        setup = new ClientServerSetup(
                                   "org.jacorb.test.orb.BasicServerImpl",
                                   null, serverProps);
    }

    @After
    public void tearDown() throws Exception
    {
        setup.tearDown ();
        clientORBTestCase.ORBTearDown ();
    }

    @BeforeClass
    public static void beforeClassSetUp() throws Exception
    {
        Assume.assumeFalse(TestUtils.isSSLEnabled);
    }

    @Test
    public void test_duplicate_stringtoobject() throws Exception
    {
        org.omg.CORBA.Object obj1 = clientOrb.string_to_object(setup.getServerIOR());
        org.omg.CORBA.Object obj2 = clientOrb.string_to_object(setup.getServerIOR());

        assertTrue ( ((org.omg.CORBA.portable.ObjectImpl)obj1)._get_delegate() !=
                ((org.omg.CORBA.portable.ObjectImpl)obj2)._get_delegate() );
    }

    @Test(timeout = 30000)
    public void test_reconnect_restarted_server() throws Exception
    {
        String ior  = setup.getServerIOR();
        boolean restart = true;

        for ( int i=0; i < 5; i++)
        {
            org.omg.CORBA.Object obj = clientOrb.string_to_object(ior);
            BasicServer server = BasicServerHelper.narrow(obj);

            try
            {
                server.bounce_boolean(true);

                if ( restart )
                {
                    TestUtils.getLogger().debug("Restarting server...");
                    restart = false;
                    setup.tearDown ();
                    setup = new ClientServerSetup(
                        "org.jacorb.test.orb.BasicServerImpl",
                        null, serverProps);
                }
            }
            catch (OBJECT_NOT_EXIST e)
            {
               ior = setup.getServerIOR();
            }
        }
    }
}
