package org.jacorb.test.orb;

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

import junit.framework.Test;
import junit.framework.TestSuite;
import org.jacorb.test.ArrayServer;
import org.jacorb.test.ArrayServerHelper;
import org.jacorb.test.any_sequenceHolder;
import org.jacorb.test.boolean_sequenceHolder;
import org.jacorb.test.char_sequenceHolder;
import org.jacorb.test.color_enum;
import org.jacorb.test.long_sequenceHolder;
import org.jacorb.test.common.ClientServerSetup;
import org.jacorb.test.common.ClientServerTestCase;
import org.jacorb.test.common.TestUtils;
import org.omg.CORBA.Any;

/**
 * Tests array/sequence parameters.  For the time being, only those types
 * of sequences are covered that are not already covered by other tests.
 * 
 * @author Andre Spiegel spiegel@gnu.org
 */
public class ArrayTest extends ClientServerTestCase
{
    private ArrayServer server;

    public ArrayTest(String name, ClientServerSetup setup)
    {
        super(name, setup);
    }

    public void setUp() throws Exception
    {
        server = ArrayServerHelper.narrow (setup.getServerObject());
    }

    public static Test suite()
    {
        TestSuite suite = new TestSuite( "Client/server array tests" );
        ClientServerSetup setup =
            new ClientServerSetup( suite,
                                   "org.jacorb.test.orb.ArrayServerImpl" );
        TestUtils.addToSuite(suite, setup, ArrayTest.class);
        return setup;
    }

    public void test_reduce_boolean_sequence()
    {
        boolean[] a = new boolean[] { true, false, true, true };
        boolean result = server.reduce_boolean_sequence(a);
        assertEquals (true, result);
    }
    
    public void test_reduce_empty_boolean_sequence()
    {
        boolean[] a = new boolean[] { };
        boolean result = server.reduce_boolean_sequence(a);
        assertEquals (false, result);
    }
    
    public void test_sum_octet_sequence()
    {
        byte[] a = new byte[] { 1, 2, 3, 4, 5 };
        int result = server.sum_octet_sequence (a);
        assertEquals (15, result);
    }

    public void test_sum_empty_octet_sequence()
    {
        byte[] a = new byte[] { };
        int result = server.sum_octet_sequence (a);
        assertEquals (0, result);
    }
    
    public void test_sum_short_sequence()
    {
        short[] a = new short[] { 1, 2, 3, 4, 5 };
        int result = server.sum_short_sequence (a);
        assertEquals (15, result);
    }

    public void test_sum_empty_short_sequence()
    {
        short[] a = new short[] { };
        int result = server.sum_short_sequence (a);
        assertEquals (0, result);
    }


    public void test_sum_ushort_sequence()
    {
        short[] a = new short[] { 1, 2, 3, 4, 5 };
        int result = server.sum_ushort_sequence (a);
        assertEquals (15, result);
    }

    public void test_sum_empty_ushort_sequence()
    {
        short[] a = new short[] { };
        int result = server.sum_ushort_sequence (a);
        assertEquals (0, result);
    }

    public void test_sum_long_sequence()
    {
        int[] a = new int[] { 1, 2, 3, 4, 5 };
        int result = server.sum_long_sequence (a);
        assertEquals (15, result);
    }

    public void test_sum_empty_long_sequence()
    {
        int[] a = new int[] { };
        int result = server.sum_long_sequence (a);
        assertEquals (0, result);
    }

    public void test_sum_ulong_sequence()
    {
        int[] a = new int[] { 1, 2, 3, 4, 5 };
        int result = server.sum_ulong_sequence (a);
        assertEquals (15, result);
    }

    public void test_sum_empty_ulong_sequence()
    {
        int[] a = new int[] { };
        int result = server.sum_ulong_sequence (a);
        assertEquals (0, result);
    }


    public void test_sum_ulonglong_sequence()
    {
        long[] a = new long[] { 1, 2, 3, 4, 5 };
        int result = server.sum_ulonglong_sequence (a);
        assertEquals (15, result);
    }

    public void test_sum_empty_ulonglong_sequence()
    {
        long[] a = new long[] { };
        int result = server.sum_ulonglong_sequence (a);
        assertEquals (0, result);
    }

    public void test_sum_float_sequence()
    {
        float[] a = new float[] { 1.0F, 2.0F, 3.0F, 4.0F, 5.0F };
        float result = server.sum_float_sequence (a);
        assertEquals (15.0F, result, 0.0001F);
    }

    public void test_sum_empty_float_sequence()
    {
        float[] a = new float[] { };
        float result = server.sum_float_sequence (a);
        assertEquals (0.0F, result, 0.0001F);
    }

    public void test_sum_double_sequence()
    {
        double[] a = new double[] { 1.0F, 2.0F, 3.0F, 4.0F, 5.0F };
        double result = server.sum_double_sequence (a);
        assertEquals (15.0, result, 0.0001);
    }

    public void test_sum_empty_double_sequence()
    {
        double[] a = new double[] { };
        double result = server.sum_double_sequence (a);
        assertEquals (0.0, result, 0.0001);
    }

    public void test_reduce_enum_sequence()
    {
        color_enum[] a = new color_enum[] 
        {
             color_enum.color_red,
             color_enum.color_green, 
             color_enum.color_blue
        };
        int result = server.reduce_enum_sequence (a);
        assertEquals (3, result);
    }

    public void test_reduce_empty_enum_sequence()
    {
        color_enum[] a = new color_enum[] { };
        int result = server.reduce_enum_sequence (a);
        assertEquals (0, result);
    }

    public void test_reduce_char_sequence()
    {
        char[] a = new char[] { 'a', 'b', 'c' };
        int result = server.reduce_char_sequence (a);
        assertEquals (3, result);
    }

    public void test_illegal_char_sequence()
    {
        char[] a = new char[] { 'a', CharTest.EURO_SIGN, 'c' };
        try
        {
                server.reduce_char_sequence (a);
                fail();
        }
        catch (org.omg.CORBA.DATA_CONVERSION ex)
        {
                // ok
        }
    }

    public void test_reduce_empty_char_sequence()
    {
        char[] a = new char[] { };
        int result = server.reduce_char_sequence (a);
        assertEquals (0, result);
    }

    public void test_return_illegal_char_sequence()
    {
        char_sequenceHolder c = new char_sequenceHolder();
        try
        {
            server.return_illegal_char_sequence(c);
            fail();
        }
        catch (org.omg.CORBA.DATA_CONVERSION ex)
        {
            // ok
        }
    }
    
    public void test_reduce_wchar_sequence()
    {
        char[] a = new char[] { 'a', CharTest.EURO_SIGN, 'c' };
        int result = server.reduce_wchar_sequence (a);
        assertEquals (3, result);
    }

    public void test_reduce_empty_wchar_sequence()
    {
        char[] a = new char[] { };
        int result = server.reduce_wchar_sequence (a);
        assertEquals (0, result);        
    }

    public void test_reduce_any_sequence()
    {
        Any a1 = setup.getClientOrb().create_any();
        a1.insert_long(1);
        Any a2 = setup.getClientOrb().create_any();
        a2.insert_long(2);
        Any a3 = setup.getClientOrb().create_any();
        a3.insert_long(3);
        Any[] a = new Any[] { a1, a2, a3 };
        int result = server.reduce_any_sequence(a);
        assertEquals (3, result);
    }

    public void test_reduce_empty_any_sequence()
    {
        Any[] a = new Any[] { };
        int result = server.reduce_any_sequence(a);
        assertEquals (0, result);
    }
    
    public void test_bounce_boolean_sequence()
    {
        boolean[] a = new boolean[] { true, false, true, true };
        boolean_sequenceHolder b = new boolean_sequenceHolder();
        server.bounce_boolean_sequence (a, b);
        assertTrue (java.util.Arrays.equals(a, b.value));
    }

    public void test_bounce_long_sequence()
    {
        int[] a = new int[] { 1, 2, 3, 4, 5 };
        long_sequenceHolder b = new long_sequenceHolder();
        server.bounce_long_sequence (a, b);
        assertTrue (java.util.Arrays.equals(a, b.value));
    }

    public void test_bounce_any_sequence()
    {
        Any a1 = setup.getClientOrb().create_any();
        a1.insert_long(1);
        Any a2 = setup.getClientOrb().create_any();
        a2.insert_long(2);
        Any a3 = setup.getClientOrb().create_any();
        a3.insert_long(3);
        Any[] a = new Any[] { a1, a2, a3 };
        any_sequenceHolder b = new any_sequenceHolder();
        server.bounce_any_sequence(a, b);
        assertEquals (3, b.value.length);
        for (int i=0; i<a.length; i++)
        {
            assertTrue (a[i].equal(b.value[i]));
        }
    }
    
}
