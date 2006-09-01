package org.jacorb.test.orb;

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

import junit.framework.*;

import org.jacorb.test.common.*;
import org.jacorb.test.*;

/**
 * Tests array/sequence parameters.  For the time being, only those types
 * of sequences are covered that are not already covered by other tests.
 * 
 * @author Andre Spiegel spiegel@gnu.org
 * @version $Id$
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

        suite.addTest (new ArrayTest ("test_sum_short_sequence", setup));
        suite.addTest (new ArrayTest ("test_sum_empty_short_sequence", setup));
        suite.addTest (new ArrayTest ("test_sum_ushort_sequence", setup));
        suite.addTest (new ArrayTest ("test_sum_empty_ushort_sequence", setup));
        suite.addTest (new ArrayTest ("test_sum_ulonglong_sequence", setup));
        suite.addTest (new ArrayTest ("test_sum_empty_ulonglong_sequence", setup));
        suite.addTest (new ArrayTest ("test_sum_float_sequence", setup));
        suite.addTest (new ArrayTest ("test_sum_empty_float_sequence", setup));
        suite.addTest (new ArrayTest ("test_sum_double_sequence", setup));
        suite.addTest (new ArrayTest ("test_sum_empty_double_sequence", setup));

        return setup;
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


}
