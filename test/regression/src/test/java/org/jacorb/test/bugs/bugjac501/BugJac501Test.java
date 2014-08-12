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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;
import org.jacorb.test.BasicServer;
import org.jacorb.test.BasicServerHelper;
import org.jacorb.test.harness.ClientServerSetup;
import org.jacorb.test.harness.ClientServerTestCase;
import org.jacorb.test.harness.TestUtils;
import org.jacorb.test.orb.BasicServerImpl;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;
import org.omg.CORBA.ORB;

/**
 * @author Alphonse Bendt
 */
public class BugJac501Test extends ClientServerTestCase
{
    @BeforeClass
    public static void beforeClassSetUp() throws Exception
    {
        Assume.assumeFalse(TestUtils.isSSLEnabled);

        setup = new ClientServerSetup(BasicServerImpl.class.getName());
    }

    @Test
    public void testCorbalocRIR() throws Exception
    {
        String ior = setup.getServerIOR();

        Properties props = new Properties();
        props.setProperty("ORBInitRef.MyServer", ior);

        ORB orb = setup.getAnotherORB(props);

        BasicServer server = BasicServerHelper.narrow(orb.string_to_object("corbaloc:rir:/MyServer"));
        assertTrue(new HashSet<String>(Arrays.asList(orb.list_initial_services())).contains("MyServer"));

        long now = System.currentTimeMillis();
        assertEquals(now, server.bounce_long_long(now));
    }
}
