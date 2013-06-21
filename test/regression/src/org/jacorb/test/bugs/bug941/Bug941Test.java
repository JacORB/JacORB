package org.jacorb.test.bugs.bug941;

/*
 *        JacORB  - a free Java ORB
 *
 *   Copyright (C) 1997-2013 Gerald Brose / The JacORB Team.
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

import java.util.Arrays;
import java.util.Properties;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.jacorb.test.common.ClientServerSetup;
import org.jacorb.test.common.ClientServerTestCase;
import org.jacorb.test.common.TestUtils;

public class Bug941Test extends ClientServerTestCase
{
    private org.omg.CORBA.Object server;

    public Bug941Test(String name, ClientServerSetup setup)
    {
        super(name, setup);
    }

    public static Test suite()
    {
        Properties severProp = new Properties();

        Properties clientProp = new Properties();
        clientProp.setProperty("org.omg.PortableInterceptor.ORBInitializerClass.a",
                               MyInitializer.class.getName());

        TestSuite suite = new TestSuite(Bug941Test.class.getName());

        ClientServerSetup setup = new ClientServerSetup(suite,
                                                        "org.jacorb.test.bugs.bug941.TestObjectImpl",
                                                        clientProp, severProp);
        TestUtils.addToSuite(suite, setup, Bug941Test.class);

        return setup;
    }

    protected void setUp() throws Exception
    {
        server = setup.getServerObject();
    }

    protected void tearDown() throws Exception
    {
        server = null;
    }

    public void testBug941Test()
    {
        MyServer grid;

        grid = MyServerHelper.narrow(server);

        System.out.println ("### In main" );
        byte [] petite_bite = new byte [1];
        petite_bite[0] = 1;
        Data tiny = new Data(petite_bite);
        grid.setData(tiny);

        byte [] grosse_bite = new byte [10000];
        Arrays.fill(grosse_bite, (byte) 1);
        Data biger = new Data(grosse_bite);
        grid.setBigData(biger);
    }
}
