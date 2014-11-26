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

package org.jacorb.test.bugs.bug878;

import static org.junit.Assert.fail;
import java.util.Properties;
import org.jacorb.test.BasicServer;
import org.jacorb.test.BasicServerHelper;
import org.jacorb.test.harness.ClientServerSetup;
import org.jacorb.test.harness.ClientServerTestCase;
import org.jacorb.test.orb.BasicServerImpl;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.omg.CORBA.COMM_FAILURE;

/**
 * @author Alexander Birchenko
 */
public class Bug878MaxServerConnectionsPassTest extends ClientServerTestCase
{
    private BasicServer server;

    @BeforeClass
    public static void beforeClassSetUp() throws Exception
    {
        Properties serverProps = new Properties();

        serverProps.setProperty("jacorb.connection.max_server_connections", "1");

        setup = new ClientServerSetup(BasicServerImpl.class.getName(), null, serverProps);
    }

    @Before
    public void setUp() throws Exception
    {
        server = BasicServerHelper.narrow(setup.getServerObject());
    }

    @Test
    public void testServerDoesNotLikeWString()
    {
        try
        {
            server.ping();
        }
        catch(COMM_FAILURE e)
        {
            fail();
        }
    }
}
