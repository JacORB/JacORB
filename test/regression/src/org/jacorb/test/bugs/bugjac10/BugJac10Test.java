package org.jacorb.test.bugs.bugjac10;

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

import java.util.Properties;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.jacorb.test.common.ClientServerSetup;
import org.jacorb.test.common.ClientServerTestCase;

/**
 * @author Carol Jordon
 */
public class BugJac10Test extends ClientServerTestCase
{
    private TypeCodeTestServer server;

    public BugJac10Test (String name, ClientServerSetup setup)
    {
        super(name, setup);
    }

    public void setUp() throws Exception
    {
        server = TypeCodeTestServerHelper.narrow( setup.getServerObject() );
    }

    protected void tearDown() throws Exception
    {
        server = null;
    }

    public static Test suite()
    {
        final TestSuite suite = new TestSuite();
        suite.addTest(createTestSuite(1));
        suite.addTest(createTestSuite(2));

        return suite;
    }

    private static Test createTestSuite(int config)
    {
        final TestSuite suite = new TestSuite( "Client/server TypeCode tests" );

        Properties props = new Properties();

        /* Always turn off cacheTypecodes*/
        props.setProperty ("jacorb.cacheTypecodes", "off");

        if (config == 1)
        {
            props.setProperty ("jacorb.compactTypecodes", "on");
        }
        else if (config == 2)
        {
            props.setProperty ("jacorb.compactTypecodes", "off");
        }
        else
        {
            Assert.fail();
        }

        ClientServerSetup setup =
            new ClientServerSetup
               ( suite,
                 "org.jacorb.test.bugs.bugjac10.TypeCodeTestImpl",
                 props,
                 props);


        if( config == 1 )
        {
            suite.addTest
                ( new BugJac10Test( "test_compact_tc_on", setup ) );
        }
        else if( config == 2 )
        {
            suite.addTest
                ( new BugJac10Test( "test_compact_tc_off", setup ) );
        }

        return setup;
    }

    /**
     * <code>test_compact_tc_on</code>
     */
    public void test_compact_tc_on()
    {
        final org.omg.CORBA.TypeCode argin = C_exceptHelper.type();
        final org.omg.CORBA.TypeCodeHolder argout = new org.omg.CORBA.TypeCodeHolder();
        final org.omg.CORBA.TypeCodeHolder arginout = new org.omg.CORBA.TypeCodeHolder();
        arginout.value = C_exceptHelper.type();

        final org.omg.CORBA.TypeCode result = server.respond(true, argin, argout, arginout);

        assertTrue(C_exceptHelper.type().get_compact_typecode().equal(result));

        assertTrue(C_exceptHelper.type().get_compact_typecode().equal(argout.value));

        assertTrue(C_exceptHelper.type().get_compact_typecode().equal(arginout.value));
    }

    /**
     * <code>test_compact_tc_off</code>
     */
    public void test_compact_tc_off()
    {
        final org.omg.CORBA.TypeCode argin = C_exceptHelper.type();
        final org.omg.CORBA.TypeCodeHolder argout = new org.omg.CORBA.TypeCodeHolder();
        final org.omg.CORBA.TypeCodeHolder arginout = new org.omg.CORBA.TypeCodeHolder();
        arginout.value = C_exceptHelper.type();

        final org.omg.CORBA.TypeCode result = server.respond(false, argin, argout, arginout);

        assertTrue(C_exceptHelper.type().equal(result));

        assertTrue(C_exceptHelper.type().equal(argout.value));

        assertTrue(C_exceptHelper.type().equal(arginout.value));
    }
}
