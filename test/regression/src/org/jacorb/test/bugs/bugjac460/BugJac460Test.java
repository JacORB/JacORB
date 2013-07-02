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

package org.jacorb.test.bugs.bugjac460;

import java.util.Properties;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.jacorb.test.BasicServer;
import org.jacorb.test.BasicServerHelper;
import org.jacorb.test.common.ClientServerSetup;
import org.jacorb.test.common.ClientServerTestCase;
import org.jacorb.test.common.ORBSetup;
import org.jacorb.test.common.TestUtils;
import org.jacorb.test.orb.BasicServerImpl;

/**
 * @author Alphonse Bendt
 */
public class BugJac460Test extends ClientServerTestCase
{
    private BasicServer server;
    private ORBSetup orb2;

    public BugJac460Test(String name, ClientServerSetup setup)
    {
        super(name, setup);
    }

    protected void setUp() throws Exception
    {
        server = BasicServerHelper.narrow(setup.getServerObject());
        orb2 = new ORBSetup(this);
        orb2.setUp();
    }

    protected void tearDown() throws Exception
    {
        server = null;
        orb2.tearDown();
    }

    public void testPingWithTwoConnections() throws Exception
    {
        server.ping();

        final BasicServer server2 = BasicServerHelper.narrow(orb2.getORB().string_to_object(setup.getServerIOR()));
        final boolean[] sucess = new boolean[1];
        final Exception[] exception = new Exception[1];

        Thread thread = new Thread()
        {
            public void run()
            {
                try
                {
                    server2.ping();
                    sucess[0] = true;
                }
                catch (Exception e)
                {
                    exception[0] = e;
                }
            }
        };

        thread.start();

        thread.join(4000);
        assertFalse(sucess[0]);
        assertNotNull(exception[0]);
    }

    public static Test suite()
    {
        Properties serverProps = new Properties();

        serverProps.setProperty("jacorb.connection.server.max_receptor_threads", "1");

        TestSuite suite = new TestSuite(BugJac460Test.class.getName());
        ClientServerSetup setup = new ClientServerSetup(suite, BasicServerImpl.class.getName(), null, serverProps);
        TestUtils.addToSuite(suite, setup, BugJac460Test.class);
        return setup;
    }
}
