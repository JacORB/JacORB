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

package org.jacorb.test.bugs.bugjac461;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Properties;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.jacorb.test.BasicServer;
import org.jacorb.test.BasicServerHelper;
import org.jacorb.test.common.ClientServerSetup;
import org.jacorb.test.common.ClientServerTestCase;
import org.jacorb.test.common.TestUtils;
import org.jacorb.test.orb.BasicServerImpl;

/**
 * @author Alphonse Bendt
 */
public class BugJac461Test extends ClientServerTestCase
{
    private BasicServer server;
    private String line;

    public BugJac461Test(String name, ClientServerSetup setup)
    {
        super(name, setup);
    }

    public static Test suite()
    {
        if (TestUtils.isJ2ME())
        {
            return new TestSuite();
        }

        Properties props = new Properties();

        props.setProperty("org.omg.PortableInterceptor.ORBInitializerClass.standard_init", "org.jacorb.orb.standardInterceptors.IORInterceptorInitializer");
        props.setProperty("jacorb.codeset", "on");
        props.setProperty("jacorb.native_char_codeset", "UTF8");

        TestSuite suite = new TestSuite(BugJac461Test.class.getName());
        ClientServerSetup setup = new ClientServerSetup(suite, BasicServerImpl.class.getName(), props, props);

        TestUtils.addToSuite(suite, setup, BugJac461Test.class);

        return setup;
    }

    protected void setUp() throws Exception
    {
        server = BasicServerHelper.narrow(setup.getServerObject());

        //read japanese from file
        BufferedReader in = new BufferedReader(
                new InputStreamReader(getClass().getResourceAsStream("japanese_case4537.txt"), "SJIS"));

        line = in.readLine();
        assertNotNull(line);
    }

    protected void tearDown() throws Exception
    {
        server = null;
    }

    public void testBounceString() throws Exception
    {
        assertEquals(line, server.bounce_string(line));
    }

    public void testBounceWString() throws Exception
    {
        assertEquals(line, server.bounce_wstring(line));
    }
}
