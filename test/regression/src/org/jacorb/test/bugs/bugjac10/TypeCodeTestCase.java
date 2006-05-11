package org.jacorb.test.bugs.bugjac10;

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

import java.util.Properties;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.jacorb.test.common.ClientServerSetup;
import org.jacorb.test.common.ClientServerTestCase;

/**
 * @author Carol Jordon
 * @version $Id$
 */
public class TypeCodeTestCase extends ClientServerTestCase
{
    private TypeCodeTest server;

    public TypeCodeTestCase (String name, ClientServerSetup setup)
    {
        super(name, setup);
    }

    public void setUp() throws Exception
    {
        server = TypeCodeTestHelper.narrow( setup.getServerObject() );
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
            props.setProperty ("jacorb.compactTypecodes", "2");
        }
        else if (config == 2)
        {
            props.setProperty ("jacorb.compactTypecodes", "0");
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
                ( new TypeCodeTestCase( "test_compact_tc_on", setup ) );
        }
        else if( config == 2 )
        {
            suite.addTest
                ( new TypeCodeTestCase( "test_compact_tc_off", setup ) );
        }

        return setup;
    }

    /**
     * <code>test_compact_tc_on</code>
     */
    public void test_compact_tc_on() 
    {
        org.omg.CORBA.TypeCode argin;
        org.omg.CORBA.TypeCodeHolder argout;
        argout = new org.omg.CORBA.TypeCodeHolder();
        org.omg.CORBA.TypeCodeHolder arginout;
        arginout = new org.omg.CORBA.TypeCodeHolder();
        org.omg.CORBA.TypeCode _ret = null;

        argin = C_exceptHelper.type ();
        arginout.value = C_exceptHelper.type ();

        _ret = server.respond (true, argin, argout, arginout);

        if (!(C_exceptHelper.type ().get_compact_typecode ().equal(_ret)))
        {
            fail ("_ret value error in test_compact_tc_on");
        }

        if (!(C_exceptHelper.type ().get_compact_typecode ().equal
              (argout.value)))
        {
            fail ("argout value error in test_compact_tc_on");
        }

        if (!(C_exceptHelper.type ().get_compact_typecode ().equal
              (arginout.value)))
        {
            fail ("arginout value error in test_compact_tc_on");
        }
    }

    /**
     * <code>test_compact_tc_off</code>
     */
    public void test_compact_tc_off()
    {
        org.omg.CORBA.TypeCode argin;
        org.omg.CORBA.TypeCodeHolder argout;
        argout = new org.omg.CORBA.TypeCodeHolder();
        org.omg.CORBA.TypeCodeHolder arginout;
        arginout = new org.omg.CORBA.TypeCodeHolder();
        org.omg.CORBA.TypeCode _ret = null;

        argin = C_exceptHelper.type ();
        arginout.value = C_exceptHelper.type ();

        _ret = server.respond (false, argin, argout, arginout);
     
        if (!(C_exceptHelper.type ().equal(_ret)))
        {
            fail ("_ret value error in test_compact_tc_off");
        }

        if (!(C_exceptHelper.type ().equal
              (argout.value)))
        {
            fail ("argout value error in test_compact_tc_off");
        }

        if (!(C_exceptHelper.type ().equal
              (arginout.value)))
        {
            fail ("arginout value error in test_compact_tc_off");
        }
    }
}
