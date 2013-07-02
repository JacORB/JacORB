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

package org.jacorb.test.bugs.bugjac449;

import java.util.Properties;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.jacorb.test.common.ClientServerSetup;
import org.jacorb.test.common.ClientServerTestCase;
import org.jacorb.test.common.CommonSetup;
import org.jacorb.test.common.TestUtils;
import org.jacorb.test.orb.value.NodeImpl;
import org.jacorb.test.orb.value.ValueServer;
import org.jacorb.test.orb.value.ValueServerHelper;
import org.jacorb.test.orb.value.ValueServerImpl;

/**
 * @author Alphonse Bendt
 */
public class BugJac449Test extends ClientServerTestCase
{
    private ValueServer server;

    public BugJac449Test(String name, ClientServerSetup setup)
    {
        super(name, setup);
    }

    protected void setUp() throws Exception
    {
        server = ValueServerHelper.narrow(setup.getServerObject());

        // for warm up
        server.receive_string("a", "b");
    }

    public static Test suite()
    {
        Test result;
        if (!TestUtils.isJ2ME())
        {
            Properties clientProps = TestUtils.newForeignORBProperties();
            clientProps.setProperty(CommonSetup.JACORB_REGRESSION_DISABLE_SECURITY, "true");

            TestSuite suite = new TestSuite(BugJac449Test.class.getName());
            ClientServerSetup setup =
                new ClientServerSetup(suite, ValueServerImpl.class.getName(), clientProps, null);
            result = setup;
            TestUtils.addToSuite(suite, setup, BugJac449Test.class);
        }
        else
        {
            result = new TestSuite(BugJac449Test.class.getName() + " ignored because of J2ME");
        }

        return result;
    }

    public void testWithJacORBORB()
    {
        Properties props = new Properties();

        props.setProperty("org.omg.CORBA.ORBClass", "org.jacorb.orb.ORB");
        props.setProperty("org.omg.CORBA.ORBSingletonClass", "org.jacorb.orb.ORBSingleton");

        org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], props);

        try
        {
            assertTrue(orb instanceof org.jacorb.orb.ORB);

            ValueServer server = ValueServerHelper.narrow(orb.string_to_object(setup.getServerIOR()));

            String result = server.receive_list(new NodeImpl(1234));
            assertEquals("list of length: 1 -- 1234", result);
        }
        finally
        {
            orb.shutdown(true);
        }
    }

    public void testModifiedStubsAlsoWorkWithTheSunORB()
    {
        String result = server.receive_list(new NodeImpl(1234));
        assertEquals("list of length: 1 -- 1234", result);
    }
}
