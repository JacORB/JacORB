package org.jacorb.test.bugs.bug960;

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

import java.util.Properties;
import org.jacorb.test.harness.ClientServerSetup;
import org.jacorb.test.harness.ClientServerTestCase;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.omg.CORBA.NO_PERMISSION;
import org.omg.PortableServer.POAPackage.ServantNotActive;
import org.omg.PortableServer.POAPackage.WrongPolicy;

/**
 * @author Nick Cross
 *
 * Interceptors verification
 */
public class Bug960Test extends ClientServerTestCase
{
    private Hello server;

    @Before
    public void setUp() throws Exception
    {
        server = HelloHelper.narrow( setup.getServerObject() );
    }

    @BeforeClass
    public static void beforeClassSetUp() throws Exception
    {
        Properties props = new Properties();

        props.setProperty("org.omg.PortableInterceptor.ORBInitializerClass."
                          + "ORBInit", Initializer.class.getName());

        setup = new ClientServerSetup( "org.jacorb.test.bugs.bug960.HelloImpl", props, props );
    }

    @Test(expected = NO_PERMISSION.class)
    public void test_interceptor_hello()
    {
        server.sayHello();
    }


    /**
     * This test sets up a local object to call (and ignores the remote).
     */
    @Test(expected = NO_PERMISSION.class)
    public void test_localinterceptor_hello() throws ServantNotActive, WrongPolicy
    {
        HelloImpl hello = new HelloImpl();
        org.omg.CORBA.Object obj = setup.getClientRootPOA().servant_to_reference(hello);
        Hello reference = HelloHelper.narrow(obj);

        reference.sayHello();
    }
}
