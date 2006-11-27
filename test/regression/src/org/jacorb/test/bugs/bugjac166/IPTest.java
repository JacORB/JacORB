package org.jacorb.test.bugs.bugjac166;

/*
 *        JacORB  - a free Java ORB
 *
 *   Copyright (C) 1997-2001  Gerald Brose.
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

import java.net.InetAddress;
import java.util.Properties;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.jacorb.test.bugs.bugjac74.Jac074Server;
import org.jacorb.test.bugs.bugjac74.Jac074ServerHelper;
import org.jacorb.test.common.ClientServerSetup;
import org.jacorb.test.common.ClientServerTestCase;
import org.jacorb.test.common.TestUtils;

public class IPTest extends ClientServerTestCase
{
    private Jac074Server server;

    public IPTest(String name, ClientServerSetup setup)
    {
        super(name, setup);
    }

    public void setUp() throws Exception
    {
        server = Jac074ServerHelper.narrow( setup.getServerObject() );
    }

    protected void tearDown() throws Exception
    {
        server = null;
    }

    public static Test suite()
    {
        TestSuite suite = new TestSuite( "IP Address test" );

        Properties props = new Properties();

        props.put("org.omg.PortableInterceptor.ORBInitializerClass.IPInitializer",
                  "org.jacorb.test.bugs.bugjac166.IPInitializer");

         ClientServerSetup setup = new ClientServerSetup
         (
             suite,
             "org.jacorb.test.bugs.bugjac166.ServerImpl",
             null,
             props
         );

         TestUtils.addToSuite(suite, setup, IPTest.class);

        return setup;
    }


    public void test_ip() throws Exception
    {
        String result = server.ping();

        assertTrue(result.length() > 0);

        InetAddress.getByName(result);
    }
}
