package org.jacorb.test.bugs.bugjac220;

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

/**
 * @author Nick Cross
 */
public class BugJac220Test extends ClientServerTestCase
{
    private TestEnum server;

    @Before
    public void setUp() throws Exception
    {
        server = TestEnumHelper.narrow( setup.getServerObject() );
    }

    @BeforeClass
    public static void beforeClassSetUp() throws Exception
    {

        Properties client_props = new Properties();
        Properties server_props = new Properties();

        /* Compact typecodes needs to be set to perform the test*/
        client_props.setProperty ("jacorb.compactTypecodes", "on");
        server_props.setProperty ("jacorb.compactTypecodes", "on");

        setup = new ClientServerSetup
        (

                 TestEnumImpl.class.getName(),
                 client_props,
                 server_props);
    }


    /**
     * <code>test_enum_tc</code>
     */
    @Test
    public void test_enum_tc()
    {
        org.omg.CORBA.ORB orb = setup.getClientOrb();

        org.omg.CORBA.Any any = orb.create_any();

        any.insert_TypeCode
            (orb.create_enum_tc
                 ("IDL:org/jacorb/test/bugs/bugjac220/TestEnum:1.0",
                  "TestCase",
                  new String [] {"Test"}));

        server.push (any);
    }
}
