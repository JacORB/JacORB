package org.jacorb.test.orb.rmi;

import java.util.Properties;
import org.jacorb.test.harness.ClientServerSetup;
import org.jacorb.test.harness.TestUtils;
import org.junit.Assume;
import org.junit.BeforeClass;

/*
 *        JacORB - a free Java ORB
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

/**
 * RMITests client: Sun ORB, server: JacORB
 */
public class SunJacORBRMITest extends AbstractRMITestCase
{
    @BeforeClass
    public static void beforeClassSetUp() throws Exception
    {
        Assume.assumeFalse(TestUtils.isSSLEnabled);

        Properties server_props = new Properties();

        server_props.setProperty("org.omg.CORBA.ORBClass", "org.jacorb.orb.ORB");
        server_props.setProperty("org.omg.CORBA.ORBSingletonClass", "org.jacorb.orb.ORBSingleton");
        server_props.setProperty("jacorb.interop.sun", "on");
        server_props.setProperty("jacorb.interop.strict_check_on_tc_creation", "off");
        server_props.setProperty("jacorb.interop.chunk_custom_rmi_valuetypes", "on");
        server_props.setProperty("org.omg.PortableInterceptor.ORBInitializerClass.standard_init", "org.jacorb.orb.standardInterceptors.IORInterceptorInitializer");

        Properties client_props = TestUtils.newForeignORBProperties();


        setup = new ClientServerSetup("org.jacorb.test.orb.rmi.RMITestServant",
                                   client_props,
                                   server_props);

    }
}
