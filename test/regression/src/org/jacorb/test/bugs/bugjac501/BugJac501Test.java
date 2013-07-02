/*
 *        JacORB  - a free Java ORB
 *
 *   Copyright (C) 1997-2006 The JacORB project.
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

package org.jacorb.test.bugs.bugjac501;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.jacorb.test.BasicServer;
import org.jacorb.test.BasicServerHelper;
import org.jacorb.test.common.ClientServerSetup;
import org.jacorb.test.common.ClientServerTestCase;
import org.jacorb.test.common.CommonSetup;
import org.jacorb.test.common.TestUtils;
import org.jacorb.test.orb.BasicServerImpl;
import org.omg.CORBA.ORB;

/**
 * @author Alphonse Bendt
 */
public class BugJac501Test extends ClientServerTestCase
{
    public BugJac501Test(String name, ClientServerSetup setup)
    {
        super(name, setup);
    }

    public static Test suite()
    {
        Properties props = new Properties();
        props.setProperty(CommonSetup.JACORB_REGRESSION_DISABLE_SECURITY, "true");

        TestSuite suite = new TestSuite(BugJac501Test.class.getName());
        ClientServerSetup setup = new ClientServerSetup(suite, BasicServerImpl.class.getName(), props, props);
        TestUtils.addToSuite(suite, setup, BugJac501Test.class);
        return setup;
    }

    public void testCorbalocRIR() throws Exception
    {
        String ior = setup.getServerIOR();

        Properties props = new Properties();
        props.setProperty("ORBInitRef.MyServer", ior);

        ORB orb = ORB.init(new String[0], props);

        BasicServer server = BasicServerHelper.narrow(orb.string_to_object("corbaloc:rir:/MyServer"));
        assertTrue(new HashSet(Arrays.asList(orb.list_initial_services())).contains("MyServer"));

        long now = System.currentTimeMillis();
        assertEquals(now, server.bounce_long_long(now));
    }
}
