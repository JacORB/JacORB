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

package org.jacorb.test.bugs.bugjac449;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import java.util.Properties;
import org.jacorb.test.harness.ClientServerSetup;
import org.jacorb.test.harness.ClientServerTestCase;
import org.jacorb.test.harness.IMRExcludedClientServerCategory;
import org.jacorb.test.harness.TestUtils;
import org.jacorb.test.orb.value.NodeImpl;
import org.jacorb.test.orb.value.ValueServer;
import org.jacorb.test.orb.value.ValueServerHelper;
import org.jacorb.test.orb.value.ValueServerImpl;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/**
 * @author Alphonse Bendt
 */
@Category(IMRExcludedClientServerCategory.class)
public class BugJac449Test extends ClientServerTestCase
{
    private ValueServer server;

    @Before
    public void setUp() throws Exception
    {
        server = ValueServerHelper.narrow(setup.getServerObject());

        // for warm up
        server.receive_string("a", "b");
    }

    @BeforeClass
    public static void beforeClassSetUp() throws Exception
    {
    	Assume.assumeFalse(TestUtils.isSSLEnabled);

        Properties clientProps = TestUtils.newForeignORBProperties();

        setup = new ClientServerSetup(ValueServerImpl.class.getName(), clientProps, null);
    }

    @Test
    public void testWithJacORBORB() throws Exception
    {
        Properties props = new Properties();
        props.setProperty("org.omg.CORBA.ORBClass", "org.jacorb.orb.ORB");
        props.setProperty("org.omg.CORBA.ORBSingletonClass", "org.jacorb.orb.ORBSingleton");

        org.omg.CORBA.ORB orb = setup.getAnotherORB(props);

        assertTrue(orb instanceof org.jacorb.orb.ORB);

        ValueServer server = ValueServerHelper.narrow(orb.string_to_object(setup.getServerIOR()));

        String result = server.receive_list(new NodeImpl(1234));
        assertEquals("list of length: 1 -- 1234", result);
    }

    @Test
    public void testModifiedStubsAlsoWorkWithTheSunORB()
    {
        String result = server.receive_list(new NodeImpl(1234));
        assertEquals("list of length: 1 -- 1234", result);
    }
}
