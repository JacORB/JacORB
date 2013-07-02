package org.jacorb.test.orb;

/*
 *        JacORB  - a free Java ORB
 *
 *   Copyright (C) 2000-2012 Gerald Brose / The JacORB Team.
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
import junit.framework.Test;
import junit.framework.TestSuite;
import org.jacorb.test.BasicServer;
import org.jacorb.test.BasicServerHelper;
import org.jacorb.test.common.ClientServerSetup;
import org.jacorb.test.common.ClientServerTestCase;
import org.jacorb.test.common.CommonSetup;
import org.jacorb.test.common.JacORBTestSuite;

/**
 * This is a client/server test case that accesses a server object
 * via corbaloc URLs.  Other functions related to corbaloc are also
 * tested here.
 * @author Andre Spiegel spiegel@gnu.org
 */
public class CorbalocTest extends ClientServerTestCase
{
    private BasicServer server;

    public CorbalocTest (String name, ClientServerSetup setup)
    {
        super(name, setup);
    }

    public void setUp() throws Exception
    {
        server = BasicServerHelper.narrow (setup.getServerObject());
    }

    public static Test suite()
    {
        TestSuite suite = new JacORBTestSuite ("Corbaloc Test",
                                               CorbalocTest.class);

        Properties serverProps = new Properties();
        serverProps.put (CommonSetup.JACORB_REGRESSION_DISABLE_SECURITY, "true");
        serverProps.put ("jacorb.test.corbaloc.enable",   "true");
        serverProps.put ("jacorb.test.corbaloc.port",     "54321");
        serverProps.put ("jacorb.test.corbaloc.implname", "MyImpl");
        serverProps.put ("jacorb.test.corbaloc.poaname",  "MyPOA");
        serverProps.put ("jacorb.test.corbaloc.objectid", "MyObject");
        serverProps.put ("jacorb.test.corbaloc.shortcut", "Shortcut1");
        serverProps.put ("jacorb.orb.objectKeyMap.Shortcut2", "MyImpl/MyPOA/MyObject");

	Properties clientProps = new Properties();
        clientProps.put (CommonSetup.JACORB_REGRESSION_DISABLE_SECURITY, "true");
        
        ClientServerSetup setup =
            new ClientServerSetup (suite,
                                   "org.jacorb.test.orb.BasicServerImpl",
                                   clientProps, serverProps);

        suite.addTest (new CorbalocTest ("test_ping", setup));
        // disabled because it currently fails
        //suite.addTest (new CorbalocTest ("test_rir_RootPOA", setup));
        suite.addTest (new CorbalocTest ("test_resolve_iiop", setup));
        suite.addTest (new CorbalocTest ("test_resolve_shortcut_1", setup));
        suite.addTest (new CorbalocTest ("test_resolve_shortcut_2", setup));
        
        return setup;
    }

    public void test_ping()
    {
        server.ping();
    }
    
    public void test_rir_RootPOA()
    {
        // TODO this test case currently fails, because string_to_object
        // doesn't seem to be prepared to return a local object
        org.omg.CORBA.Object rootPOA = 
            setup.getClientOrb().string_to_object("corbaloc:rir:/RootPOA");
        assertTrue (rootPOA._is_a("IDL:org.omg/PortableServer/POA:1.0"));
    }

    public void test_resolve_iiop()
    {
        org.omg.CORBA.Object obj = 
            setup.getClientOrb()
                 .string_to_object("corbaloc:iiop:localhost:54321/MyImpl/MyPOA/MyObject");
        BasicServer bserver = BasicServerHelper.narrow (obj);
        bserver.ping();
    }
    
    public void test_resolve_shortcut_1()
    {
        org.omg.CORBA.Object obj = 
            setup.getClientOrb()
                 .string_to_object("corbaloc:iiop:localhost:54321/Shortcut1");
        BasicServer bserver = BasicServerHelper.narrow (obj);
        bserver.ping();
    }
    
    public void test_resolve_shortcut_2()
    {
        org.omg.CORBA.Object obj = 
            setup.getClientOrb()
                 .string_to_object("corbaloc:iiop:localhost:54321/Shortcut2");
        BasicServer bserver = BasicServerHelper.narrow (obj);
        bserver.ping();
    }
}
