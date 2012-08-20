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

package org.jacorb.test.bugs.bugjac512;

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
import org.omg.CORBA.MARSHAL;

/**
 * @author Alphonse Bendt
 */
public class BugJac512Giop1_0ServerTest extends ClientServerTestCase
{
    private BasicServer server;

    public BugJac512Giop1_0ServerTest(String name, ClientServerSetup setup)
    {
        super(name, setup);
    }

    public static Test suite()
    {
        Properties serverProps = new Properties();

        serverProps.setProperty("jacorb.giop_minor_version", "0");
        serverProps.setProperty(CommonSetup.JACORB_REGRESSION_DISABLE_IMR, "true");
        serverProps.setProperty(CommonSetup.JACORB_REGRESSION_DISABLE_SECURITY, "true");

        TestSuite suite = new TestSuite(BugJac512Giop1_0ServerTest.class.getName());
        ClientServerSetup setup = new ClientServerSetup(suite, BasicServerImpl.class.getName(), null, serverProps);
        TestUtils.addToSuite(suite, setup, BugJac512Giop1_0ServerTest.class);
        return setup;
    }

    protected void setUp() throws Exception
    {
        server = BasicServerHelper.narrow(setup.getServerObject());
    }

    public void testServerDoesNotLikeWChar()
    {
        try
        {
            server.bounce_wchar('a');
            fail();
        }
        catch(MARSHAL e)
        {
            assertEquals(5, e.minor);
        }
    }

    public void testServerDoesNotLikeWString()
    {
        try
        {
            server.bounce_wstring("value");
            fail();
        }
        catch(MARSHAL e)
        {
            assertEquals(5, e.minor);
        }
    }
}
