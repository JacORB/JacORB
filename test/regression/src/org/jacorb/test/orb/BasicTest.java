package org.jacorb.test.orb;

/*
 *        JacORB  - a free Java ORB
 *
 *   Copyright (C) 1997-2005  Gerald Brose.
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

import junit.framework.Test;
import junit.framework.TestSuite;

import org.jacorb.test.BasicServer;
import org.jacorb.test.BasicServerHelper;
import org.jacorb.test.common.ClientServerSetup;
import org.jacorb.test.common.ClientServerTestCase;
import org.jacorb.test.common.JacORBTestSuite;
import org.omg.CORBA.BooleanHolder;
import org.omg.CORBA.ByteHolder;
import org.omg.CORBA.DoubleHolder;
import org.omg.CORBA.FloatHolder;
import org.omg.CORBA.IntHolder;
import org.omg.CORBA.LongHolder;
import org.omg.CORBA.ShortHolder;

public class BasicTest extends ClientServerTestCase
{
    private BasicServer server;

    public BasicTest(String name, ClientServerSetup setup)
    {
        super(name, setup);
    }

    public void setUp() throws Exception
    {
        server = BasicServerHelper.narrow( setup.getServerObject() );
    }

    protected void tearDown() throws Exception
    {
        server = null;
    }

    public static Test suite()
    {
        TestSuite suite = new JacORBTestSuite("Basic client/server tests",
                                              BasicTest.class);
        ClientServerSetup setup =
            new ClientServerSetup( suite,
                                   "org.jacorb.test.orb.BasicServerImpl" );

        suite.addTest( new BasicTest( "test_ping", setup ));

        // short tests
        suite.addTest( new BasicTest( "test_pass_in_short", setup ) );
        suite.addTest( new BasicTest( "test_pass_out_short", setup ) );
        suite.addTest( new BasicTest( "test_pass_inout_short", setup ) );
        suite.addTest( new BasicTest( "test_return_short", setup ) );
        suite.addTest( new BasicTest( "test_bounce_short", setup ) );
        suite.addTest( new BasicTest( "test_min_short", setup ) );
        suite.addTest( new BasicTest( "test_max_short", setup ) );
        suite.addTest( new BasicTest( "test_zero_short", setup ) );

        // unsigned short tests
        suite.addTest( new BasicTest( "test_pass_in_unsigned_short", setup ) );
        suite.addTest( new BasicTest( "test_pass_out_unsigned_short", setup ) );
        suite.addTest( new BasicTest( "test_pass_inout_unsigned_short", setup ) );
        suite.addTest( new BasicTest( "test_return_unsigned_short", setup ) );
        suite.addTest( new BasicTest( "test_bounce_unsigned_short", setup ) );
        suite.addTest( new BasicTest( "test_max_unsigned_short", setup ) );
        suite.addTest( new BasicTest( "test_zero_unsigned_short", setup ) );

        // long tests
        suite.addTest( new BasicTest( "test_pass_in_long", setup ) );
        suite.addTest( new BasicTest( "test_pass_out_long", setup ) );
        suite.addTest( new BasicTest( "test_pass_inout_long", setup ) );
        suite.addTest( new BasicTest( "test_return_long", setup ) );
        suite.addTest( new BasicTest( "test_bounce_long", setup ) );
        suite.addTest( new BasicTest( "test_min_long", setup ) );
        suite.addTest( new BasicTest( "test_max_long", setup ) );
        suite.addTest( new BasicTest( "test_zero_long", setup ) );

        // unsigned long tests
        suite.addTest( new BasicTest( "test_pass_in_unsigned_long", setup ) );
        suite.addTest( new BasicTest( "test_pass_out_unsigned_long", setup ) );
        suite.addTest( new BasicTest( "test_pass_inout_unsigned_long", setup ) );
        suite.addTest( new BasicTest( "test_return_unsigned_long", setup ) );
        suite.addTest( new BasicTest( "test_bounce_unsigned_long", setup ) );
        suite.addTest( new BasicTest( "test_max_unsigned_long", setup ) );
        suite.addTest( new BasicTest( "test_zero_unsigned_long", setup ) );

        // long long tests
        suite.addTest( new BasicTest( "test_pass_in_long_long", setup ) );
        suite.addTest( new BasicTest( "test_pass_out_long_long", setup ) );
        suite.addTest( new BasicTest( "test_pass_inout_long_long", setup ) );
        suite.addTest( new BasicTest( "test_return_long_long", setup ) );
        suite.addTest( new BasicTest( "test_bounce_long_long", setup ) );
        suite.addTest( new BasicTest( "test_min_long_long", setup ) );
        suite.addTest( new BasicTest( "test_max_long_long", setup ) );
        suite.addTest( new BasicTest( "test_zero_long_long", setup ) );

        // unsigned long long tests
        suite.addTest( new BasicTest( "test_pass_in_unsigned_long_long", setup ) );
        suite.addTest( new BasicTest( "test_pass_out_unsigned_long_long", setup ) );
        suite.addTest( new BasicTest( "test_pass_inout_unsigned_long_long", setup ) );
        suite.addTest( new BasicTest( "test_return_unsigned_long_long", setup ) );
        suite.addTest( new BasicTest( "test_bounce_unsigned_long_long", setup ) );
        suite.addTest( new BasicTest( "test_max_unsigned_long_long", setup ) );
        suite.addTest( new BasicTest( "test_zero_unsigned_long_long", setup ) );

        // boolean tests
        suite.addTest( new BasicTest( "test_pass_in_boolean", setup ) );
        suite.addTest( new BasicTest( "test_pass_out_boolean", setup ) );
        suite.addTest( new BasicTest( "test_pass_inout_boolean", setup ) );
        suite.addTest( new BasicTest( "test_return_boolean", setup ) );
        suite.addTest( new BasicTest( "test_bounce_boolean", setup ) );

        // octet tests
        suite.addTest( new BasicTest( "test_pass_in_octet", setup ) );
        suite.addTest( new BasicTest( "test_pass_out_octet", setup ) );
        suite.addTest( new BasicTest( "test_pass_inout_octet", setup ) );
        suite.addTest( new BasicTest( "test_return_octet", setup ) );
        suite.addTest( new BasicTest( "test_bounce_octet", setup ) );

        // float tests
        suite.addTest( new BasicTest( "test_pass_in_float", setup ) );
        suite.addTest( new BasicTest( "test_pass_out_float", setup ) );
        suite.addTest( new BasicTest( "test_pass_inout_float", setup ) );
        suite.addTest( new BasicTest( "test_return_float", setup ) );
        suite.addTest( new BasicTest( "test_bounce_float", setup ) );

        // double tests
        suite.addTest( new BasicTest( "test_pass_in_double", setup ) );
        suite.addTest( new BasicTest( "test_pass_out_double", setup ) );
        suite.addTest( new BasicTest( "test_pass_inout_double", setup ) );
        suite.addTest( new BasicTest( "test_return_double", setup ) );
        suite.addTest( new BasicTest( "test_bounce_double", setup ) );

        return setup;
    }

    public void test_ping()
    {
        server.ping();
    }

    // short parameters

    public void test_pass_in_short()
    {
        server.pass_in_short( ( short ) 14 );
    }

    public void test_pass_out_short()
    {
        ShortHolder x = new ShortHolder();
        server.pass_out_short( x );
        assertEquals( 82, x.value );
    }

    public void test_pass_inout_short()
    {
        ShortHolder x = new ShortHolder( ( short ) -4 );
        server.pass_inout_short( x );
        assertEquals( ( short ) -3, x.value );
    }

    public void test_return_short()
    {
        short result = server.return_short();
        assertEquals( ( short ) -4, result );
    }

    public void test_bounce_short()
    {
        short result = server.bounce_short( ( short ) 14 );
        assertEquals( ( short ) 14, result );
    }

    public void test_min_short()
    {
        short result = server.bounce_short( ( short ) 0x8000 );
        assertEquals( ( short ) 0x8000, result );
    }

    public void test_max_short()
    {
        short result = server.bounce_short( ( short ) 0xffff );
        assertEquals( ( short ) 0xffff, result );
    }

    public void test_zero_short()
    {
        short result = server.bounce_short( ( short ) 0 );
        assertEquals( ( short ) 0, result );
    }

    // unsigned short parameters

    public void test_pass_in_unsigned_short()
    {
        server.pass_in_unsigned_short( ( short ) 14 );
    }

    public void test_pass_out_unsigned_short()
    {
        ShortHolder x = new ShortHolder();
        server.pass_out_unsigned_short( x );
        assertEquals( 79, x.value );
    }

    public void test_pass_inout_unsigned_short()
    {
        ShortHolder x = new ShortHolder( ( short ) 88 );
        server.pass_inout_unsigned_short( x );
        assertEquals( 89, x.value );
    }

    public void test_return_unsigned_short()
    {
        short result = server.return_unsigned_short();
        assertEquals( 87, result );
    }

    public void test_bounce_unsigned_short()
    {
        short result = server.bounce_unsigned_short( ( short ) 14 );
        assertEquals( 14, result );
    }

    public void test_max_unsigned_short()
    {
        short result = server.bounce_unsigned_short( ( short ) 0xffff );
        assertEquals( ( short ) 0xffff, result );
    }

    public void test_zero_unsigned_short()
    {
        short result = server.bounce_unsigned_short( ( short ) 0 );
        assertEquals( ( short ) 0, result );
    }

    // long parameters

    public void test_pass_in_long()
    {
        server.pass_in_long( 14 );
    }

    public void test_pass_out_long()
    {
        IntHolder x = new IntHolder();
        server.pass_out_long( x );
        assertEquals( 83, x.value );
    }

    public void test_pass_inout_long()
    {
        IntHolder x = new IntHolder( -4 );
        server.pass_inout_long( x );
        assertEquals( -3, x.value );
    }

    public void test_return_long()
    {
        int result = server.return_long();
        assertEquals( -17, result );
    }

    public void test_bounce_long()
    {
        int result = server.bounce_long( 14 );
        assertEquals( 14, result );
    }

    public void test_min_long()
    {
        int result = server.bounce_long( 0x8000000 );
        assertEquals( 0x8000000, result );
    }

    public void test_max_long()
    {
        int result = server.bounce_long( 0xffffffff );
        assertEquals( 0xffffffff, result );
    }

    public void test_zero_long()
    {
        int result = server.bounce_long( 0 );
        assertEquals( 0, result );
    }

    // unsigned long parameters

    public void test_pass_in_unsigned_long()
    {
        server.pass_in_unsigned_long( 76542 );
    }

    public void test_pass_out_unsigned_long()
    {
        IntHolder x = new IntHolder();
        server.pass_out_unsigned_long( x );
        assertEquals( 80, x.value );
    }

    public void test_pass_inout_unsigned_long()
    {
        IntHolder x = new IntHolder( 5 );
        server.pass_inout_unsigned_long( x );
        assertEquals( 6, x.value );
    }

    public void test_return_unsigned_long()
    {
        int result = server.return_unsigned_long();
        assertEquals( 43, result );
    }

    public void test_bounce_unsigned_long()
    {
        int result = server.bounce_unsigned_long( 123456 );
        assertEquals( 123456, result );
    }

    public void test_max_unsigned_long()
    {
        int result = server.bounce_unsigned_long( 0xffffffff );
        assertEquals( 0xffffffff, result );
    }

    public void test_zero_unsigned_long()
    {
        int result = server.bounce_unsigned_long( 0 );
        assertEquals( 0, result );
    }

    // long long parameters

    public void test_pass_in_long_long()
    {
        server.pass_in_long_long( 14L );
    }

    public void test_pass_out_long_long()
    {
        LongHolder x = new LongHolder();
        server.pass_out_long_long( x );
        assertEquals( 84L, x.value );
    }

    public void test_pass_inout_long_long()
    {
        LongHolder x = new LongHolder( -12345678889L );
        server.pass_inout_long_long( x );
        assertEquals( -12345678888L, x.value );
    }

    public void test_return_long_long()
    {
        long result = server.return_long_long();
        assertEquals( 0xffeeddccbbaa0099L, result );
    }

    public void test_bounce_long_long()
    {
        long result = server.bounce_long_long( 14 );
        assertEquals( 14L, result );
    }

    public void test_min_long_long()
    {
        long result = server.bounce_long_long( 0x8000000000000000L );
        assertEquals( 0x8000000000000000L, result );
    }

    public void test_max_long_long()
    {
        long result = server.bounce_long_long( 0xffffffffffffffffL );
        assertEquals( 0xffffffffffffffffL, result );
    }

    public void test_zero_long_long()
    {
        long result = server.bounce_long_long( 0L );
        assertEquals( 0L, result );
    }

    // unsigned long long parameters

    public void test_pass_in_unsigned_long_long()
    {
        server.pass_in_unsigned_long_long( 14L );
    }

    public void test_pass_out_unsigned_long_long()
    {
        LongHolder x = new LongHolder();
        server.pass_out_unsigned_long_long( x );
        assertEquals( 81L, x.value );
    }

    public void test_pass_inout_unsigned_long_long()
    {
        LongHolder x = new LongHolder( 9876543210L );
        server.pass_inout_unsigned_long_long( x );
        assertEquals( 9876543211L, x.value );
    }

    public void test_return_unsigned_long_long()
    {
        long result = server.return_unsigned_long_long();
        assertEquals( 0xffeeddccbbaa0088L, result );
    }

    public void test_bounce_unsigned_long_long()
    {
        long result = server.bounce_unsigned_long_long( 14L );
        assertEquals( 14L, result );
    }

    public void test_max_unsigned_long_long()
    {
        long result = server.bounce_unsigned_long_long( 0xffffffffffffffffL );
        assertEquals( 0xffffffffffffffffL, result );
    }

    public void test_zero_unsigned_long_long()
    {
        long result = server.bounce_unsigned_long_long( 0L );
        assertEquals( 0L, result );
    }

    // boolean parameters

    public void test_pass_in_boolean()
    {
        server.pass_in_boolean ( false );
    }

    public void test_pass_out_boolean()
    {
        BooleanHolder x = new BooleanHolder( false );
        server.pass_out_boolean( x );
        assertEquals( true, x.value );
    }

    public void test_pass_inout_boolean()
    {
        BooleanHolder x = new BooleanHolder( true );
        server.pass_inout_boolean( x );
        assertEquals( false, x.value );
    }

    public void test_return_boolean()
    {
        boolean result = server.return_boolean();
        assertEquals( true, result );
    }

    public void test_bounce_boolean()
    {
        boolean result = server.bounce_boolean( false );
        assertEquals( false, result );
        result = server.bounce_boolean( true );
        assertEquals( true, result );
    }

    // octet parameters

    public void test_pass_in_octet()
    {
        server.pass_in_octet ( (byte)127 );
    }

    public void test_pass_out_octet()
    {
        ByteHolder x = new ByteHolder( (byte) -1 );
        server.pass_out_octet( x );
        assertEquals( (byte)23, x.value );
    }

    public void test_pass_inout_octet()
    {
        ByteHolder x = new ByteHolder( (byte) -1 );
        server.pass_inout_octet( x );
        assertEquals( (byte)0, x.value );
    }

    public void test_return_octet()
    {
        byte result = server.return_octet();
        assertEquals( (byte)0xf0, result );
    }

    public void test_bounce_octet()
    {
        byte result = server.bounce_octet( (byte)0xff );
        assertEquals( (byte)0xff, result );
    }

    // float parameters

    public void test_pass_in_float()
    {
        server.pass_in_float ( 1.234F );
    }

    public void test_pass_out_float()
    {
        FloatHolder x = new FloatHolder( 1.0F );
        server.pass_out_float( x );
        assertEquals( 0.005F, x.value, 0 );
    }

    public void test_pass_inout_float()
    {
        FloatHolder x = new FloatHolder( -23.4F );
        server.pass_inout_float( x );
        assertEquals( -22.4F, x.value, 0 );
    }

    public void test_return_float()
    {
        float result = server.return_float();
        assertEquals( 1.5E-1F, result, 0 );
    }

    public void test_bounce_float()
    {
        float result = server.bounce_float( 0.0F );
        assertEquals( 0.0F, result, 0 );
        result = server.bounce_float( -1234.56F );
        assertEquals( -1234.56F, result, 0 );
        result = server.bounce_float( Float.MIN_VALUE );
        assertEquals( Float.MIN_VALUE, result, 0 );
        result = server.bounce_float( Float.MAX_VALUE );
        assertEquals( Float.MAX_VALUE, result, 0 );
        result = server.bounce_float( Float.NaN );
        assertTrue( Float.isNaN( result ) );
        result = server.bounce_float( Float.NEGATIVE_INFINITY );
        assertEquals( Float.NEGATIVE_INFINITY, result, 0 );
        result = server.bounce_float( Float.POSITIVE_INFINITY );
        assertEquals( Float.POSITIVE_INFINITY, result, 0 );
    }

    // double parameters

    public void test_pass_in_double()
    {
        server.pass_in_double ( 1.234 );
    }

    public void test_pass_out_double()
    {
        DoubleHolder x = new DoubleHolder( 1.0 );
        server.pass_out_double( x );
        assertEquals( 1234E12, x.value, 0 );
    }

    public void test_pass_inout_double()
    {
        DoubleHolder x = new DoubleHolder( -23.4 );
        server.pass_inout_double( x );
        assertEquals( -22.4, x.value, 0 );
    }

    public void test_return_double()
    {
        double result = server.return_double();
        assertEquals( 1E-100, result, 0 );
    }

    public void test_bounce_double()
    {
        double result = server.bounce_double( 0.0 );
        assertEquals( 0.0F, result, 0 );
        result = server.bounce_double( -1234.56789 );
        assertEquals( -1234.56789, result, 0 );
        result = server.bounce_double( Double.MIN_VALUE );
        assertEquals( Double.MIN_VALUE, result, 0 );
        result = server.bounce_double( Double.MAX_VALUE );
        assertEquals( Double.MAX_VALUE, result, 0 );
        result = server.bounce_double( Double.NaN );
        assertTrue( Double.isNaN( result ) );
        result = server.bounce_double( Double.NEGATIVE_INFINITY );
        assertEquals( Double.NEGATIVE_INFINITY, result, 0 );
        result = server.bounce_double( Double.POSITIVE_INFINITY );
        assertEquals( Double.POSITIVE_INFINITY, result, 0 );
    }

}
