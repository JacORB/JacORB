package org.jacorb.test.orb;

/*
 *        JacORB  - a free Java ORB
 *
 *   Copyright (C) 1997-2015 Gerald Brose / The JacORB Team.
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
import org.jacorb.test.harness.ORBTestCase;
import org.jacorb.test.harness.TestUtils;
import org.jboss.byteman.contrib.bmunit.BMRule;
import org.jboss.byteman.contrib.bmunit.BMUnitRunner;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.LogMode;
import org.junit.contrib.java.lang.system.StandardOutputStreamLog;
import org.junit.runner.RunWith;
import org.omg.CORBA.ORB;
import org.omg.PortableServer.POAManager;

/**
 * @author Nick Cross
 */
@RunWith(BMUnitRunner.class)
public class SocketCloseTest extends ORBTestCase
{
    static
    {
        //    System.setProperty("org.jboss.byteman.verbose", "true");
        //    System.setProperty("org.jboss.byteman.debug", "true");
    }

    @Rule
    public final StandardOutputStreamLog log = new StandardOutputStreamLog
            (
                    TestUtils.verbose ? LogMode.LOG_AND_WRITE_TO_STREAM : LogMode.LOG_ONLY
            );

    private POAManager poaManager;

    @BeforeClass
    public static void beforeClassSetUp() throws Exception
    {
        Assume.assumeFalse(TestUtils.isSSLEnabled);
    }

    @Override
    protected void patchORBProperties(Properties props) throws Exception
    {
        props.setProperty("jacorb.iiop.enable_loopback", "false");

        props.setProperty("jacorb.connection.client.pending_reply_timeout", "5000");
        props.setProperty("jacorb.connection.client.pending_reply_timeout", "5000");
    }

    @Before
    public void setUp() throws Exception
    {
        poaManager = rootPOA.the_POAManager();
    }

    @Test(timeout = 60000)
    @BMRule(name = "inject-socket-failure",
            targetClass = "Socket",
            targetMethod = "close",
            targetLocation = "AT ENTRY",
            action = "throw new IOException()"
    )
    public void testGIOPLoop() throws Exception
    {
        poaManager.activate();

        BasicServerImpl servant = new BasicServerImpl();

        rootPOA.activate_object(servant);

        org.omg.CORBA.Object o = rootPOA.servant_to_reference(servant);

        String ior = getORB().object_to_string(o);

        ORB another = getAnotherORB(orbProps);
        BasicServer server = BasicServerHelper.narrow(another.string_to_object(ior));

        server.bounce_short((short) 14);
        server._release();
    }
}
