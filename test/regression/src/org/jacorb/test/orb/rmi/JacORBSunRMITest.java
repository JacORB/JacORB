package org.jacorb.test.orb.rmi;

import java.util.Properties;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.jacorb.test.common.ClientServerSetup;
import org.jacorb.test.common.TestUtils;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2004 Gerald Brose.
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

/**
 * RMITests client: JacORB, server: Sun ORB
 */
public class JacORBSunRMITest extends RMITestCase
{
    public JacORBSunRMITest(String name, ClientServerSetup setup)
    {
        super(name, setup);
    }

    public static Test suite()
    {
        TestSuite suite = new TestSuite( "RMI/IIOP tests JacORB vs. Sun" );

        Properties client_props = new Properties();
        client_props.setProperty("jacorb.interop.strict_check_on_tc_creation", "off");
        client_props.setProperty("jacorb.interop.chunk_custom_rmi_valuetypes", "on");
        client_props.setProperty("org.omg.CORBA.ORBClass", "org.jacorb.orb.ORB");
        client_props.setProperty("org.omg.CORBA.ORBSingletonClass", "org.jacorb.orb.ORBSingleton");
        client_props.setProperty("jacorb.interop.sun", "on");
        client_props.setProperty("jacorb.regression.disable_security", "true");

        Properties server_props = TestUtils.newForeignORBProperties();

        ClientServerSetup setup =
            new ClientServerSetup( suite,
                                   "org.jacorb.test.orb.rmi.RMITestServant",
                                   client_props,
                                   server_props);

        TestUtils.addToSuite(suite, setup, JacORBSunRMITest.class);

        return setup;
    }
}
