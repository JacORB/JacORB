package org.jacorb.test.orb;

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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.jacorb.test.BasicServer;
import org.jacorb.test.BasicServerHelper;
import org.jacorb.test.harness.ClientServerSetup;
import org.jacorb.test.harness.ClientServerTestCase;
import org.jacorb.test.harness.TestUtils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.omg.CORBA.BooleanHolder;
import org.omg.CORBA.ByteHolder;
import org.omg.CORBA.DoubleHolder;
import org.omg.CORBA.FloatHolder;
import org.omg.CORBA.IntHolder;
import org.omg.CORBA.LongHolder;
import org.omg.CORBA.Policy;
import org.omg.CORBA.PolicyListHolder;
import org.omg.CORBA.ShortHolder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class BasicTest extends ClientServerTestCase
{
    private BasicServer server;

    @Before
    public void setUp() throws Exception
    {
        server = BasicServerHelper.narrow( setup.getServerObject() );
    }

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        setup = new ClientServerSetup ("org.jacorb.test.orb.BasicServerImpl" );
    }

    @Test
    public void test_ping()
    {
        server.ping();
    }

    // short parameters

    @Test
    public void test_pass_in_short()
    {
        server.pass_in_short( ( short ) 14 );
    }

    @Test
    public void test_pass_out_short()
    {
        ShortHolder x = new ShortHolder();
        server.pass_out_short( x );
        assertEquals( 82, x.value );
    }

    @Test
    public void test_pass_inout_short()
    {
        ShortHolder x = new ShortHolder( ( short ) -4 );
        server.pass_inout_short( x );
        assertEquals( ( short ) -3, x.value );
    }

    @Test
    public void test_return_short()
    {
        short result = server.return_short();
        assertEquals( ( short ) -4, result );
    }

    @Test
    public void test_bounce_short()
    {
        short result = server.bounce_short( ( short ) 14 );
        assertEquals( ( short ) 14, result );
    }

    @Test
    public void test_min_short()
    {
        short result = server.bounce_short( ( short ) 0x8000 );
        assertEquals( ( short ) 0x8000, result );
    }

    @Test
    public void test_max_short()
    {
        short result = server.bounce_short( ( short ) 0xffff );
        assertEquals( ( short ) 0xffff, result );
    }

    @Test
    public void test_zero_short()
    {
        short result = server.bounce_short( ( short ) 0 );
        assertEquals( ( short ) 0, result );
    }

    // unsigned short parameters

    @Test
    public void test_pass_in_unsigned_short()
    {
        server.pass_in_unsigned_short( ( short ) 14 );
    }

    @Test
    public void test_pass_out_unsigned_short()
    {
        ShortHolder x = new ShortHolder();
        server.pass_out_unsigned_short( x );
        assertEquals( 79, x.value );
    }

    @Test
    public void test_pass_inout_unsigned_short()
    {
        ShortHolder x = new ShortHolder( ( short ) 88 );
        server.pass_inout_unsigned_short( x );
        assertEquals( 89, x.value );
    }

    @Test
    public void test_return_unsigned_short()
    {
        short result = server.return_unsigned_short();
        assertEquals( 87, result );
    }

    @Test
    public void test_bounce_unsigned_short()
    {
        short result = server.bounce_unsigned_short( ( short ) 14 );
        assertEquals( 14, result );
    }

    @Test
    public void test_max_unsigned_short()
    {
        short result = server.bounce_unsigned_short( ( short ) 0xffff );
        assertEquals( ( short ) 0xffff, result );
    }

    @Test
    public void test_zero_unsigned_short()
    {
        short result = server.bounce_unsigned_short( ( short ) 0 );
        assertEquals( ( short ) 0, result );
    }

    // long parameters

    @Test
    public void test_pass_in_long()
    {
        server.pass_in_long( 14 );
    }

    @Test
    public void test_pass_out_long()
    {
        IntHolder x = new IntHolder();
        server.pass_out_long( x );
        assertEquals( 83, x.value );
    }

    @Test
    public void test_pass_inout_long()
    {
        IntHolder x = new IntHolder( -4 );
        server.pass_inout_long( x );
        assertEquals( -3, x.value );
    }

    @Test
    public void test_return_long()
    {
        int result = server.return_long();
        assertEquals( -17, result );
    }

    @Test
    public void test_bounce_long()
    {
        int result = server.bounce_long( 14 );
        assertEquals( 14, result );
    }

    @Test
    public void test_min_long()
    {
        int result = server.bounce_long( 0x8000000 );
        assertEquals( 0x8000000, result );
    }

    @Test
    public void test_max_long()
    {
        int result = server.bounce_long( 0xffffffff );
        assertEquals( 0xffffffff, result );
    }

    @Test
    public void test_zero_long()
    {
        int result = server.bounce_long( 0 );
        assertEquals( 0, result );
    }

    // unsigned long parameters

    @Test
    public void test_pass_in_unsigned_long()
    {
        server.pass_in_unsigned_long( 76542 );
    }

    @Test
    public void test_pass_out_unsigned_long()
    {
        IntHolder x = new IntHolder();
        server.pass_out_unsigned_long( x );
        assertEquals( 80, x.value );
    }

    @Test
    public void test_pass_inout_unsigned_long()
    {
        IntHolder x = new IntHolder( 5 );
        server.pass_inout_unsigned_long( x );
        assertEquals( 6, x.value );
    }

    @Test
    public void test_return_unsigned_long()
    {
        int result = server.return_unsigned_long();
        assertEquals( 43, result );
    }

    @Test
    public void test_bounce_unsigned_long()
    {
        int result = server.bounce_unsigned_long( 123456 );
        assertEquals( 123456, result );
    }

    @Test
    public void test_max_unsigned_long()
    {
        int result = server.bounce_unsigned_long( 0xffffffff );
        assertEquals( 0xffffffff, result );
    }

    @Test
    public void test_zero_unsigned_long()
    {
        int result = server.bounce_unsigned_long( 0 );
        assertEquals( 0, result );
    }

    // long long parameters

    @Test
    public void test_pass_in_long_long()
    {
        server.pass_in_long_long( 14L );
    }

    @Test
    public void test_pass_out_long_long()
    {
        LongHolder x = new LongHolder();
        server.pass_out_long_long( x );
        assertEquals( 84L, x.value );
    }

    @Test
    public void test_pass_inout_long_long()
    {
        LongHolder x = new LongHolder( -12345678889L );
        server.pass_inout_long_long( x );
        assertEquals( -12345678888L, x.value );
    }

    @Test
    public void test_return_long_long()
    {
        long result = server.return_long_long();
        assertEquals( 0xffeeddccbbaa0099L, result );
    }

    @Test
    public void test_bounce_long_long()
    {
        long result = server.bounce_long_long( 14 );
        assertEquals( 14L, result );
    }

    @Test
    public void test_min_long_long()
    {
        long result = server.bounce_long_long( 0x8000000000000000L );
        assertEquals( 0x8000000000000000L, result );
    }

    @Test
    public void test_max_long_long()
    {
        long result = server.bounce_long_long( 0xffffffffffffffffL );
        assertEquals( 0xffffffffffffffffL, result );
    }

    @Test
    public void test_zero_long_long()
    {
        long result = server.bounce_long_long( 0L );
        assertEquals( 0L, result );
    }

    // unsigned long long parameters

    @Test
    public void test_pass_in_unsigned_long_long()
    {
        server.pass_in_unsigned_long_long( 14L );
    }

    @Test
    public void test_pass_out_unsigned_long_long()
    {
        LongHolder x = new LongHolder();
        server.pass_out_unsigned_long_long( x );
        assertEquals( 81L, x.value );
    }

    @Test
    public void test_pass_inout_unsigned_long_long()
    {
        LongHolder x = new LongHolder( 9876543210L );
        server.pass_inout_unsigned_long_long( x );
        assertEquals( 9876543211L, x.value );
    }

    @Test
    public void test_return_unsigned_long_long()
    {
        long result = server.return_unsigned_long_long();
        assertEquals( 0xffeeddccbbaa0088L, result );
    }

    @Test
    public void test_bounce_unsigned_long_long()
    {
        long result = server.bounce_unsigned_long_long( 14L );
        assertEquals( 14L, result );
    }

    @Test
    public void test_max_unsigned_long_long()
    {
        long result = server.bounce_unsigned_long_long( 0xffffffffffffffffL );
        assertEquals( 0xffffffffffffffffL, result );
    }

    @Test
    public void test_zero_unsigned_long_long()
    {
        long result = server.bounce_unsigned_long_long( 0L );
        assertEquals( 0L, result );
    }

    // boolean parameters

    @Test
    public void test_pass_in_boolean()
    {
        server.pass_in_boolean ( false );
    }

    @Test
    public void test_pass_out_boolean()
    {
        BooleanHolder x = new BooleanHolder( false );
        server.pass_out_boolean( x );
        assertEquals( true, x.value );
    }

    @Test
    public void test_pass_inout_boolean()
    {
        BooleanHolder x = new BooleanHolder( true );
        server.pass_inout_boolean( x );
        assertEquals( false, x.value );
    }

    @Test
    public void test_return_boolean()
    {
        boolean result = server.return_boolean();
        assertEquals( true, result );
    }

    @Test
    public void test_bounce_boolean()
    {
        boolean result = server.bounce_boolean( false );
        assertEquals( false, result );
        result = server.bounce_boolean( true );
        assertEquals( true, result );
    }

    // octet parameters

    @Test
    public void test_pass_in_octet()
    {
        server.pass_in_octet ( (byte)127 );
    }

    @Test
    public void test_pass_out_octet()
    {
        ByteHolder x = new ByteHolder( (byte) -1 );
        server.pass_out_octet( x );
        assertEquals( (byte)23, x.value );
    }

    @Test
    public void test_pass_inout_octet()
    {
        ByteHolder x = new ByteHolder( (byte) -1 );
        server.pass_inout_octet( x );
        assertEquals( (byte)0, x.value );
    }

    @Test
    public void test_return_octet()
    {
        byte result = server.return_octet();
        assertEquals( (byte)0xf0, result );
    }

    @Test
    public void test_bounce_octet()
    {
        byte result = server.bounce_octet( (byte)0xff );
        assertEquals( (byte)0xff, result );
    }

    // float parameters

    @Test
    public void test_pass_in_float()
    {
        server.pass_in_float ( 1.234F );
    }

    @Test
    public void test_pass_out_float()
    {
        FloatHolder x = new FloatHolder( 1.0F );
        server.pass_out_float( x );
        assertEquals( 0.005F, x.value, 0 );
    }

    @Test
    public void test_pass_inout_float()
    {
        FloatHolder x = new FloatHolder( -23.4F );
        server.pass_inout_float( x );
        assertEquals( -22.4F, x.value, 0 );
    }

    @Test
    public void test_return_float()
    {
        float result = server.return_float();
        assertEquals( 1.5E-1F, result, 0 );
    }

    @Test
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

    @Test
    public void test_pass_in_double()
    {
        server.pass_in_double ( 1.234 );
    }

    @Test
    public void test_pass_out_double()
    {
        DoubleHolder x = new DoubleHolder( 1.0 );
        server.pass_out_double( x );
        assertEquals( 1234E12, x.value, 0 );
    }

    @Test
    public void test_pass_inout_double()
    {
        DoubleHolder x = new DoubleHolder( -23.4 );
        server.pass_inout_double( x );
        assertEquals( -22.4, x.value, 0 );
    }

    @Test
    public void test_return_double()
    {
        double result = server.return_double();
        assertEquals( 1E-100, result, 0 );
    }

    @Test
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

    @Test
    public void test_validate_connection()
	{
            try
            {
		PolicyListHolder h = new PolicyListHolder(new Policy[2]);

		boolean result = server._validate_connection(h);
                assertTrue(result);
            }
            catch (Exception e)
            {
                // not expected
                fail(e.getMessage());
            }
	}

    @Test
    public void testSimpleMultiThread() throws Exception
    {
        final int SIZE = 3;
        ExecutorService executor = Executors.newFixedThreadPool(SIZE);

        class SimpleRequestor implements Callable<Boolean>
        {
            BasicServer server;

            SimpleRequestor(BasicServer server)
            {
                this.server = server;
            }

            @Override
            public Boolean call() throws Exception
            {
                for (int i = 0; i < 100; ++i)
                {
                    TestUtils.getLogger().debug(this.toString() + ": iteration " + i);
                    server.pass_in_long(1000);
                    assertEquals(47, server.bounce_long(47));
                }
                return true;
            }
        }

        try
        {
            List<Future<Boolean>> list = new ArrayList<Future<Boolean>>();
            for (int i = 0; i < SIZE; i++)
            {
                list.add(executor.submit(new SimpleRequestor(server)));
            }
            for (Future<Boolean> b : list)
            {
                try
                {
                    b.get();
                }
                catch (ExecutionException e)
                {
                    fail (e.getCause().toString());
                }
            }
        }
        finally
        {
            executor.shutdown();
        }
    }
}
