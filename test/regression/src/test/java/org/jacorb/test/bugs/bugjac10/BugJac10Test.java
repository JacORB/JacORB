package org.jacorb.test.bugs.bugjac10;

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

import static org.junit.Assert.assertTrue;
import java.util.Arrays;
import java.util.Collection;
import java.util.Properties;
import org.jacorb.test.harness.ClientServerSetup;
import org.jacorb.test.harness.ClientServerTestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

/**
 * @author Carol Jordon
 */
@RunWith(Parameterized.class)
public class BugJac10Test extends ClientServerTestCase
{
    private TypeCodeTestServer server;

    @Parameter
    public String value;

    @Parameters(name="{index} : {0}")
    public static Collection<Object[]> data()
    {
        return Arrays.asList(new Object [][] { { "on" } , { "off" } } );
    }

    @Before
    public void setUp() throws Exception
    {
        Properties props = new Properties ();
        props.setProperty ("jacorb.compactTypecodes", value);
        setup = new ClientServerSetup(TypeCodeTestImpl.class.getName(), props, props);
        server = TypeCodeTestServerHelper.narrow( setup.getServerObject());
    }

    @After
    public void tearDown() throws Exception
    {
        server._release();
        setup.tearDown();
    }

    /**
     * <code>test_compact_tc_on</code>
     */
    @Test
    public void test_compact_tc()
    {
        final org.omg.CORBA.TypeCode argin = C_exceptHelper.type();
        final org.omg.CORBA.TypeCodeHolder argout = new org.omg.CORBA.TypeCodeHolder();
        final org.omg.CORBA.TypeCodeHolder arginout = new org.omg.CORBA.TypeCodeHolder();
        arginout.value = C_exceptHelper.type();

        if (value.equals("on"))
        {
            final org.omg.CORBA.TypeCode result = server.respond(true, argin, argout, arginout);
            assertTrue(C_exceptHelper.type().get_compact_typecode().equal(result));

            assertTrue(C_exceptHelper.type().get_compact_typecode().equal(argout.value));

            assertTrue(C_exceptHelper.type().get_compact_typecode().equal(arginout.value));
        }
        else
        {
            final org.omg.CORBA.TypeCode result = server.respond(false, argin, argout, arginout);

            assertTrue(C_exceptHelper.type().equal(result));

            assertTrue(C_exceptHelper.type().equal(argout.value));

            assertTrue(C_exceptHelper.type().equal(arginout.value));
        }
    }
}
