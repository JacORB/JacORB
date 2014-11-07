package org.jacorb.test.orb;

/*
 *        JacORB  - a free Java ORB
 *
 *   Copyright (C) 2000-2014 Gerald Brose / The JacORB Team.
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

import static org.junit.Assert.assertTrue;
import java.util.Properties;
import org.jacorb.test.BasicServer;
import org.jacorb.test.BasicServerHelper;
import org.jacorb.test.harness.ClientServerSetup;
import org.jacorb.test.harness.ClientServerTestCase;
import org.jacorb.test.harness.TestUtils;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

/**
 * This is a client/server test case that accesses a server object
 * via corbaloc URLs.  Other functions related to corbaloc are also
 * tested here.
 * @author Andre Spiegel spiegel@gnu.org
 */
public class CorbalocTest extends ClientServerTestCase
{
    private BasicServer server;

    @Before
    public void setUp() throws Exception
    {
        server = BasicServerHelper.narrow (setup.getServerObject());
    }

    @BeforeClass
    public static void beforeClassSetUp() throws Exception
    {
        Assume.assumeFalse(TestUtils.isSSLEnabled);

        Properties serverProps = new Properties();
        serverProps.put ("jacorb.test.corbaloc.enable",   "true");
        serverProps.put ("jacorb.test.corbaloc.port",     "54321");
        serverProps.put ("jacorb.test.corbaloc.implname", "MyImpl");
        serverProps.put ("jacorb.test.corbaloc.poaname",  "MyPOA");
        serverProps.put ("jacorb.test.corbaloc.objectid", "MyObject");
        serverProps.put ("jacorb.test.corbaloc.shortcut", "Shortcut1");
        serverProps.put ("jacorb.orb.objectKeyMap.Shortcut2", "MyImpl/MyPOA/MyObject");

        setup = new ClientServerSetup(
                                   "org.jacorb.test.orb.BasicServerImpl",
                                   null, serverProps);
    }

    @Test
    public void test_ping()
    {
        server.ping();
    }

    @Test
    @Ignore
    public void test_rir_RootPOA()
    {
        // TODO this test case currently fails, because string_to_object
        // doesn't seem to be prepared to return a local object
        org.omg.CORBA.Object rootPOA =
            setup.getClientOrb().string_to_object("corbaloc:rir:/RootPOA");
        assertTrue (rootPOA._is_a("IDL:org.omg/PortableServer/POA:1.0"));
    }

    @Test
    public void test_resolve_iiop()
    {
        org.omg.CORBA.Object obj =
            setup.getClientOrb()
                 .string_to_object("corbaloc:iiop:localhost:54321/MyImpl/MyPOA/MyObject");
        BasicServer bserver = BasicServerHelper.narrow (obj);
        bserver.ping();
    }

    @Test
    public void test_resolve_shortcut_1()
    {
        org.omg.CORBA.Object obj =
            setup.getClientOrb()
                 .string_to_object("corbaloc:iiop:localhost:54321/Shortcut1");
        BasicServer bserver = BasicServerHelper.narrow (obj);
        bserver.ping();
    }

    @Test
    public void test_resolve_shortcut_2()
    {
        org.omg.CORBA.Object obj =
            setup.getClientOrb()
                 .string_to_object("corbaloc:iiop:localhost:54321/Shortcut2");
        BasicServer bserver = BasicServerHelper.narrow (obj);
        bserver.ping();
    }
}
