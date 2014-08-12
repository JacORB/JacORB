package org.jacorb.test.bugs.bug975;

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
import org.jacorb.test.harness.ClientServerTestCase;
import org.jacorb.test.harness.ORBTestCase;
import org.jacorb.test.harness.TestUtils;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class Bug975Test extends ClientServerTestCase
{
    private Properties serverProps = new Properties();
    private ORBTestCase clientORBTestCase = new ORBTestCase ()
    {
        @Override
        protected void patchORBProperties(Properties props) throws Exception
        {
            props.put ("jacorb.connection.client.pending_reply_timeout", "2000");
        }
    };

    @Before
    public void setUp() throws Exception
    {
        clientORBTestCase.ORBSetUp ();

        serverProps.put ("jacorb.test.corbaloc.enable",   "true");
        serverProps.put ("jacorb.test.corbaloc.port",     "54321");
        serverProps.put ("jacorb.test.corbaloc.implname", "MyImpl");
        serverProps.put ("jacorb.test.corbaloc.poaname",  "MyPOA");
        serverProps.put ("jacorb.test.corbaloc.objectid", "MyObject");
        serverProps.put ("jacorb.test.corbaloc.shortcut", "Shortcut1");
        serverProps.put ("jacorb.orb.objectKeyMap.Shortcut2", "MyImpl/MyPOA/MyObject");

        setup = new ClientServerSetup(
                                   "org.jacorb.test.bugs.bugjac330.CustomBasicServerImpl",
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
    public void test_reconnect_restarted_server() throws Exception
    {
        org.omg.CORBA.Object obj = clientORBTestCase.getORB ().string_to_object
            ("corbaloc:iiop:localhost:54321/MyImpl/MyPOA/MyObject");
        BasicServer server = BasicServerHelper.narrow (obj);
        server.ping();

        // Server should now exit (CustomBasicImpl has a wait of 5000).
        setup.tearDown ();

        setup = new ClientServerSetup(
                                   "org.jacorb.test.bugs.bugjac330.CustomBasicServerImpl",
                                   null, serverProps);

        server.bounce_long (10);
    }
}
