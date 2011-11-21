package org.jacorb.test.orb;

/*
 *        JacORB  - a free Java ORB
 *
 *   Copyright (C) 1997-2011 Gerald Brose / The JacORB Team.
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
import org.jacorb.test.common.ClientServerSetup;
import org.jacorb.test.common.JacORBTestSuite;

public class NIOBasicTest extends BasicTest
{
    public NIOBasicTest(String name, ClientServerSetup setup)
    {
        super(name, setup);
    }

    public static Test suite()
    {
        TestSuite suite = new JacORBTestSuite("Basic client/server tests",
                                              NIOBasicTest.class);

        Properties client_props = new Properties();
        client_props.setProperty ("jacorb.connection.nonblocking", "true");

        ClientServerSetup setup =
            new ClientServerSetup( suite,
                                   "org.jacorb.test.orb.BasicServerImpl", client_props, null );

        if ( ! setup.isSSLEnabled() )
        {
            suite.addTest( new NIOBasicTest( "test_ping", setup ));

            // short tests
            suite.addTest( new NIOBasicTest( "test_pass_in_short", setup ) );
            suite.addTest( new NIOBasicTest( "test_pass_out_short", setup ) );
            suite.addTest( new NIOBasicTest( "test_pass_inout_short", setup ) );
            suite.addTest( new NIOBasicTest( "test_return_short", setup ) );
            suite.addTest( new NIOBasicTest( "test_bounce_short", setup ) );
            suite.addTest( new NIOBasicTest( "test_min_short", setup ) );
            suite.addTest( new NIOBasicTest( "test_max_short", setup ) );
            suite.addTest( new NIOBasicTest( "test_zero_short", setup ) );

            // unsigned short tests
            suite.addTest( new NIOBasicTest( "test_pass_in_unsigned_short", setup ) );
            suite.addTest( new NIOBasicTest( "test_pass_out_unsigned_short", setup ) );
            suite.addTest( new NIOBasicTest( "test_pass_inout_unsigned_short", setup ) );
            suite.addTest( new NIOBasicTest( "test_return_unsigned_short", setup ) );
            suite.addTest( new NIOBasicTest( "test_bounce_unsigned_short", setup ) );
            suite.addTest( new NIOBasicTest( "test_max_unsigned_short", setup ) );
            suite.addTest( new NIOBasicTest( "test_zero_unsigned_short", setup ) );

            // long tests
            suite.addTest( new NIOBasicTest( "test_pass_in_long", setup ) );
            suite.addTest( new NIOBasicTest( "test_pass_out_long", setup ) );
            suite.addTest( new NIOBasicTest( "test_pass_inout_long", setup ) );
            suite.addTest( new NIOBasicTest( "test_return_long", setup ) );
            suite.addTest( new NIOBasicTest( "test_bounce_long", setup ) );
            suite.addTest( new NIOBasicTest( "test_min_long", setup ) );
            suite.addTest( new NIOBasicTest( "test_max_long", setup ) );
            suite.addTest( new NIOBasicTest( "test_zero_long", setup ) );

            // unsigned long tests
            suite.addTest( new NIOBasicTest( "test_pass_in_unsigned_long", setup ) );
            suite.addTest( new NIOBasicTest( "test_pass_out_unsigned_long", setup ) );
            suite.addTest( new NIOBasicTest( "test_pass_inout_unsigned_long", setup ) );
            suite.addTest( new NIOBasicTest( "test_return_unsigned_long", setup ) );
            suite.addTest( new NIOBasicTest( "test_bounce_unsigned_long", setup ) );
            suite.addTest( new NIOBasicTest( "test_max_unsigned_long", setup ) );
            suite.addTest( new NIOBasicTest( "test_zero_unsigned_long", setup ) );

            // long long tests
            suite.addTest( new NIOBasicTest( "test_pass_in_long_long", setup ) );
            suite.addTest( new NIOBasicTest( "test_pass_out_long_long", setup ) );
            suite.addTest( new NIOBasicTest( "test_pass_inout_long_long", setup ) );
            suite.addTest( new NIOBasicTest( "test_return_long_long", setup ) );
            suite.addTest( new NIOBasicTest( "test_bounce_long_long", setup ) );
            suite.addTest( new NIOBasicTest( "test_min_long_long", setup ) );
            suite.addTest( new NIOBasicTest( "test_max_long_long", setup ) );
            suite.addTest( new NIOBasicTest( "test_zero_long_long", setup ) );

            // unsigned long long tests
            suite.addTest( new NIOBasicTest( "test_pass_in_unsigned_long_long", setup ) );
            suite.addTest( new NIOBasicTest( "test_pass_out_unsigned_long_long", setup ) );
            suite.addTest( new NIOBasicTest( "test_pass_inout_unsigned_long_long", setup ) );
            suite.addTest( new NIOBasicTest( "test_return_unsigned_long_long", setup ) );
            suite.addTest( new NIOBasicTest( "test_bounce_unsigned_long_long", setup ) );
            suite.addTest( new NIOBasicTest( "test_max_unsigned_long_long", setup ) );
            suite.addTest( new NIOBasicTest( "test_zero_unsigned_long_long", setup ) );

            // boolean tests
            suite.addTest( new NIOBasicTest( "test_pass_in_boolean", setup ) );
            suite.addTest( new NIOBasicTest( "test_pass_out_boolean", setup ) );
            suite.addTest( new NIOBasicTest( "test_pass_inout_boolean", setup ) );
            suite.addTest( new NIOBasicTest( "test_return_boolean", setup ) );
            suite.addTest( new NIOBasicTest( "test_bounce_boolean", setup ) );

            // octet tests
            suite.addTest( new NIOBasicTest( "test_pass_in_octet", setup ) );
            suite.addTest( new NIOBasicTest( "test_pass_out_octet", setup ) );
            suite.addTest( new NIOBasicTest( "test_pass_inout_octet", setup ) );
            suite.addTest( new NIOBasicTest( "test_return_octet", setup ) );
            suite.addTest( new NIOBasicTest( "test_bounce_octet", setup ) );

            // float tests
            suite.addTest( new NIOBasicTest( "test_pass_in_float", setup ) );
            suite.addTest( new NIOBasicTest( "test_pass_out_float", setup ) );
            suite.addTest( new NIOBasicTest( "test_pass_inout_float", setup ) );
            suite.addTest( new NIOBasicTest( "test_return_float", setup ) );
            suite.addTest( new NIOBasicTest( "test_bounce_float", setup ) );

            // double tests
            suite.addTest( new NIOBasicTest( "test_pass_in_double", setup ) );
            suite.addTest( new NIOBasicTest( "test_pass_out_double", setup ) );
            suite.addTest( new NIOBasicTest( "test_pass_inout_double", setup ) );
            suite.addTest( new NIOBasicTest( "test_return_double", setup ) );
            suite.addTest( new NIOBasicTest( "test_bounce_double", setup ) );
        }

        return setup;
    }
}
