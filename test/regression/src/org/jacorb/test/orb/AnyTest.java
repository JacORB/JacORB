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

import java.math.BigDecimal;
import java.util.Properties;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.jacorb.orb.CDROutputStream;
import org.jacorb.test.common.ClientServerSetup;
import org.jacorb.test.common.ClientServerTestCase;
import org.jacorb.test.common.TestUtils;
import org.jacorb.test.orb.RecursiveUnionStructPackage.RecursiveUnionStructUnion;
import org.omg.CORBA.Any;
import org.omg.CORBA.AnyHolder;
import org.omg.CORBA.BooleanHolder;
import org.omg.CORBA.ByteHolder;
import org.omg.CORBA.CharHolder;
import org.omg.CORBA.DoubleHolder;
import org.omg.CORBA.FixedHolder;
import org.omg.CORBA.FloatHolder;
import org.omg.CORBA.IntHolder;
import org.omg.CORBA.LongHolder;
import org.omg.CORBA.ShortHolder;
import org.omg.CORBA.StringHolder;
import org.omg.CORBA.TypeCode;
import org.omg.CORBA.TypeCodeHolder;
import org.omg.CORBA.TCKind;
import org.omg.CORBA.portable.*;

public class AnyTest extends ClientServerTestCase
{
    private AnyServer server;

    public AnyTest(String name, ClientServerSetup setup)
    {
        super(name, setup);
    }

    public void setUp() throws Exception
    {
        server = AnyServerHelper.narrow( setup.getServerObject() );
    }

    protected void tearDown() throws Exception
    {
        server = null;
    }

    public static Test suite()
    {
        Properties props = new Properties();
        props.put("jacorb.compactTypecodes", "0");

        TestSuite suite = new TestSuite("Client/server any tests");
        ClientServerSetup setup = new ClientServerSetup
            ( suite, AnyServerImpl.class.getName(), props, props);

        TestUtils.addToSuite(suite, setup, AnyTest.class);

        // in the PrismTech version of this test there are
        // some environments where we don't want
        // to run all tests.
        // for now all tests are run. please
        // leave in the commented out section so that
        // the information is not lost which tests
        // should be excluded eventually.

//        suite.addTest(new AnyTest("test_empty", setup));
//        suite.addTest(new AnyTest("test_short", setup));
//        suite.addTest(new AnyTest("test_short_streamable", setup));
//        suite.addTest(new AnyTest("test_ushort", setup));
//        suite.addTest(new AnyTest("test_long", setup));
//        suite.addTest(new AnyTest("test_long_streamable", setup));
//        suite.addTest(new AnyTest("test_ulong", setup));
//        suite.addTest(new AnyTest("test_longlong", setup));
//        suite.addTest(new AnyTest("test_ulonglong", setup));
//        suite.addTest(new AnyTest("test_float", setup));
//        suite.addTest(new AnyTest("test_float_streamable", setup));
//        suite.addTest(new AnyTest("test_double", setup));
//        suite.addTest(new AnyTest("test_double_streamable", setup));
//        suite.addTest(new AnyTest("test_char", setup));
//        suite.addTest(new AnyTest("test_char_streamable", setup));
//        suite.addTest(new AnyTest("test_wchar", setup));
//        suite.addTest(new AnyTest("test_octet", setup));
//        suite.addTest(new AnyTest("test_octet_streamable", setup));
//        suite.addTest(new AnyTest("test_any", setup));
//        suite.addTest(new AnyTest("test_any_streamable", setup));
//        suite.addTest(new AnyTest("test_string", setup));
//        suite.addTest(new AnyTest("test_string_streamable", setup));
//        suite.addTest(new AnyTest("test_wstring", setup));
//
//        if ( ! TestUtils.isCVM())
//        {
//            suite.addTest(new AnyTest("test_fixed", setup));
//            suite.addTest(new AnyTest("test_fixed_streamable", setup));
//        }
//
//        suite.addTest(new AnyTest("test_object", setup));
//        suite.addTest(new AnyTest("test_object_streamable", setup));
//        suite.addTest(new AnyTest("test_object2", setup));
//        suite.addTest(new AnyTest("test_object_null", setup));
//        suite.addTest(new AnyTest("test_TypeCode", setup));
//
//        if ( ! TestUtils.isCDC())
//        {
//            suite.addTest(new AnyTest("test_value_box_string", setup));
//            //suite.addTest(new AnyTest("test_value_box_string_streamable", setup));
//        }
//
//        if ( ! TestUtils.isCVM())
//        {
//            suite.addTest(new AnyTest("test_value_box_string2", setup));
//        }
//
//        suite.addTest(new AnyTest("test_value_null", setup));
//
//        if ( ! TestUtils.isCVM())
//        {
//            suite.addTest(new AnyTest("test_value_box_string_helper", setup));
//            suite.addTest(new AnyTest("test_value_box_string_helper2", setup));
//            suite.addTest(new AnyTest("test_value_box_string_helper3", setup));
//            suite.addTest(new AnyTest("test_value_box_long", setup));
//            suite.addTest(new AnyTest("test_value_box_long2", setup));
//            //suite.addTest(new AnyTest("test_value_box_long_streamable", setup));
//            suite.addTest(new AnyTest("test_value_box_long_helper", setup));
//            suite.addTest(new AnyTest("test_value_box_long_helper2", setup));
//            suite.addTest(new AnyTest("test_value_box_long_helper3", setup));
//            //[Nico-possibly invalid]
//            //suite.addTest(new AnyTest("test_valuetype", setup));
//            suite.addTest(new AnyTest("test_valuetype2", setup));
//            //suite.addTest(new AnyTest("test_valuetype_streamable", setup));
//            suite.addTest(new AnyTest("test_valuetype_helper", setup));
//            suite.addTest(new AnyTest("test_valuetype_helper2", setup));
//            suite.addTest(new AnyTest("test_valuetype_helper3", setup));
//        }
//
//        suite.addTest(new AnyTest("test_recursive_struct", setup));
//        suite.addTest(new AnyTest("test_recursive_struct_streamable", setup));
//        suite.addTest(new AnyTest("test_repeated_struct", setup));
//        suite.addTest(new AnyTest("test_repeated_struct_streamable", setup));
//        suite.addTest(new AnyTest("test_recursive_union", setup));
//        suite.addTest(new AnyTest("test_recursive_union_streamable", setup));
//        suite.addTest(new AnyTest("test_recursive_struct_union", setup));
//        suite.addTest(new AnyTest("test_recursive_struct_union_streamable", setup));
//        suite.addTest(new AnyTest("test_alias", setup));
//        suite.addTest(new AnyTest("test_alias2", setup));
//        suite.addTest(new AnyTest("test_alias3", setup));
//        suite.addTest(new AnyTest("test_alias4", setup));
//        suite.addTest(new AnyTest("test_equal", setup));
//        suite.addTest(new AnyTest("test_equal2", setup));
//        suite.addTest(new AnyTest("test_short_disc_union", setup));
//        suite.addTest(new AnyTest("test_short_disc_union_streamable", setup));
//        suite.addTest(new AnyTest("test_short_disc_union_manual", setup));
//        suite.addTest(new AnyTest("test_long_disc_union", setup));
//        suite.addTest(new AnyTest("test_long_disc_union_streamable", setup));
//        suite.addTest(new AnyTest("test_long_disc_union_manual", setup));
//        suite.addTest(new AnyTest("test_ushort_disc_union", setup));
//        suite.addTest(new AnyTest("test_ushort_disc_union_streamable", setup));
//        suite.addTest(new AnyTest("test_ushort_disc_union_manual", setup));
//        suite.addTest(new AnyTest("test_ulong_disc_union", setup));
//        suite.addTest(new AnyTest("test_ulong_disc_union_streamable", setup));
//        suite.addTest(new AnyTest("test_ulong_disc_union_manual", setup));
//        suite.addTest(new AnyTest("test_boolean_disc_union", setup));
//        suite.addTest(new AnyTest("test_boolean_disc_union_streamable", setup));
//        suite.addTest(new AnyTest("test_boolean_disc_union_manual", setup));
//        suite.addTest(new AnyTest("test_char_disc_union", setup));
//        suite.addTest(new AnyTest("test_char_disc_union_streamable", setup));
//        suite.addTest(new AnyTest("test_char_disc_union_manual", setup));
//        suite.addTest(new AnyTest("test_enum_disc_union", setup));
//        suite.addTest(new AnyTest("test_enum_disc_union_streamable", setup));
//        suite.addTest(new AnyTest("test_enum_disc_union_manual", setup));

        return setup;
    }

    public void test_empty()
        throws Exception
    {
        Any outAny = setup.getClientOrb().create_any();
        Any inAny = server.bounce_any(outAny);
        assertTrue(outAny.equal(inAny));
    }

    public void test_short()
        throws Exception
    {
        short testValue = (short) 4711;
        Any outAny = setup.getClientOrb().create_any();
        outAny.insert_short(testValue);
        assertEquals(testValue, outAny.extract_short());

        Any inAny = server.bounce_any(outAny);

        assertEquals(testValue, inAny.extract_short());
        assertTrue(outAny.equal(inAny));
    }

    public void test_short_streamable()
        throws Exception
    {
        short testValue = (short) 4711;
        Any outAny = setup.getClientOrb().create_any();
        outAny.insert_Streamable(new ShortHolder(testValue));
        assertEquals(testValue, outAny.extract_short());

        Any inAny = server.bounce_any(outAny);

        assertEquals(testValue, inAny.extract_short());
        assertTrue(outAny.equal(inAny));
    }

    public void test_short_stream()
    {
        short testValue = (short) 4711;
        Any any = setup.getClientOrb().create_any();
        any.type (setup.getClientOrb().get_primitive_tc(TCKind.tk_short));
        any.create_output_stream().write_short (testValue);
        // don't bounce, because we want to extract from the
        // output stream we just created
        short outValue = any.extract_short();
        assertEquals (testValue, outValue);
    }

    public void test_ushort()
        throws Exception
    {
        short testValue = (short) 4711;
        Any outAny = setup.getClientOrb().create_any();
        outAny.insert_ushort(testValue);
        assertEquals(testValue, outAny.extract_ushort());

        Any inAny = server.bounce_any(outAny);

        assertEquals(testValue, inAny.extract_ushort());
        assertTrue(outAny.equal(inAny));
    }

    public void test_ushort_stream()
    {
        short testValue = (short) 4711;
        Any any = setup.getClientOrb().create_any();
        any.type (setup.getClientOrb().get_primitive_tc(TCKind.tk_ushort));
        any.create_output_stream().write_ushort (testValue);
        // don't bounce, because we want to extract from the
        // output stream we just created
        short outValue = any.extract_ushort();
        assertEquals (testValue, outValue);
    }

    public void test_long()
        throws Exception
    {
        int testValue = 4711;
        Any outAny = setup.getClientOrb().create_any();
        outAny.insert_long(testValue);
        assertEquals(testValue, outAny.extract_long());

        Any inAny = server.bounce_any(outAny);

        assertEquals(testValue, inAny.extract_long());
        assertTrue(outAny.equal(inAny));
    }

    public void test_long_streamable()
        throws Exception
    {
        int testValue = 4711;
        Any outAny = setup.getClientOrb().create_any();
        outAny.insert_Streamable(new IntHolder(testValue));
        assertEquals(testValue, outAny.extract_long());

        Any inAny = server.bounce_any(outAny);

        assertEquals(testValue, inAny.extract_long());
        assertTrue(outAny.equal(inAny));
    }

    public void test_long_stream()
    {
        int testValue = 4711;
        Any any = setup.getClientOrb().create_any();
        any.type (setup.getClientOrb().get_primitive_tc(TCKind.tk_long));
        any.create_output_stream().write_long (testValue);
        // don't bounce, because we want to extract from the
        // output stream we just created
        int outValue = any.extract_long();
        assertEquals (testValue, outValue);
    }

    public void test_ulong()
        throws Exception
    {
        int testValue = 4711;
        Any outAny = setup.getClientOrb().create_any();
        outAny.insert_ulong(testValue);
        assertEquals(testValue, outAny.extract_ulong());

        Any inAny = server.bounce_any(outAny);

        assertEquals(testValue, inAny.extract_ulong());
        assertTrue(outAny.equal(inAny));
    }

    public void test_ulong_stream()
    {
        int testValue = 4711;
        Any any = setup.getClientOrb().create_any();
        any.type (setup.getClientOrb().get_primitive_tc(TCKind.tk_ulong));
        any.create_output_stream().write_ulong (testValue);
        // don't bounce, because we want to extract from the
        // output stream we just created
        int outValue = any.extract_ulong();
        assertEquals (testValue, outValue);
    }

    public void test_longlong()
        throws Exception
    {
        long testValue = 4711L;
        Any outAny = setup.getClientOrb().create_any();
        outAny.insert_longlong(testValue);
        assertEquals(testValue, outAny.extract_longlong());

        Any inAny = server.bounce_any(outAny);

        assertEquals(testValue, inAny.extract_longlong());
        assertTrue(outAny.equal(inAny));
    }

    public void test_longlong_streamable()
        throws Exception
    {
        long testValue = 4711L;
        Any outAny = setup.getClientOrb().create_any();
        outAny.insert_Streamable(new LongHolder(testValue));
        assertEquals(testValue, outAny.extract_longlong());

        Any inAny = server.bounce_any(outAny);

        assertEquals(testValue, inAny.extract_longlong());
        assertTrue(outAny.equal(inAny));
    }

    public void test_longlong_stream()
    {
        long testValue = 4711L;
        Any any = setup.getClientOrb().create_any();
        any.type (setup.getClientOrb().get_primitive_tc(TCKind.tk_longlong));
        any.create_output_stream().write_longlong (testValue);
        // don't bounce, because we want to extract from the
        // output stream we just created
        long outValue = any.extract_longlong();
        assertEquals (testValue, outValue);
    }

    public void test_ulonglong()
        throws Exception
    {
        long testValue = 4711L;
        Any outAny = setup.getClientOrb().create_any();
        outAny.insert_ulonglong(testValue);
        assertEquals(testValue, outAny.extract_ulonglong());

        Any inAny = server.bounce_any(outAny);

        assertEquals(testValue, inAny.extract_ulonglong());
        assertTrue(outAny.equal(inAny));
    }

    public void test_ulonglong_stream()
    {
        long testValue = 4711L;
        Any any = setup.getClientOrb().create_any();
        any.type (setup.getClientOrb().get_primitive_tc(TCKind.tk_ulonglong));
        any.create_output_stream().write_ulonglong (testValue);
        // don't bounce, because we want to extract from the
        // output stream we just created
        long outValue = any.extract_ulonglong();
        assertEquals (testValue, outValue);
    }

    public void test_float()
        throws Exception
    {
        float testValue = (float) 4711.0;
        Any outAny = setup.getClientOrb().create_any();
        outAny.insert_float(testValue);
        assertEquals(testValue, outAny.extract_float(), 0.0);

        Any inAny = server.bounce_any(outAny);

        assertEquals(testValue, inAny.extract_float(), 0.0);
        assertTrue(outAny.equal(inAny));
    }

    public void test_float_streamable()
        throws Exception
    {
        float testValue = (float) 4711.0;
        Any outAny = setup.getClientOrb().create_any();
        outAny.insert_Streamable(new FloatHolder(testValue));
        assertEquals(testValue, outAny.extract_float(), 0.0);

        Any inAny = server.bounce_any(outAny);

        assertEquals(testValue, inAny.extract_float(), 0.0);
        assertTrue(outAny.equal(inAny));
    }

    public void test_float_stream()
    {
        float testValue = 47.11F;
        Any any = setup.getClientOrb().create_any();
        any.type (setup.getClientOrb().get_primitive_tc(TCKind.tk_float));
        any.create_output_stream().write_float (testValue);
        // don't bounce, because we want to extract from the
        // output stream we just created
        float outValue = any.extract_float();
        assertEquals (testValue, outValue, 0.0);
    }

    public void test_double()
        throws Exception
    {
        double testValue = 4711.0;
        Any outAny = setup.getClientOrb().create_any();
        outAny.insert_double(testValue);
        assertEquals(testValue, outAny.extract_double(), 0.0);

        Any inAny = server.bounce_any(outAny);

        assertEquals(testValue, inAny.extract_double(), 0.0);
        assertTrue(outAny.equal(inAny));
    }

    public void test_double_streamable()
        throws Exception
    {
        double testValue = 4711.0;
        Any outAny = setup.getClientOrb().create_any();
        outAny.insert_Streamable(new DoubleHolder(testValue));
        assertEquals(testValue, outAny.extract_double(), 0.0);

        Any inAny = server.bounce_any(outAny);

        assertEquals(testValue, inAny.extract_double(), 0.0);
        assertTrue(outAny.equal(inAny));
    }

    public void test_double_stream()
    {
        double testValue = 47.11;
        Any any = setup.getClientOrb().create_any();
        any.type (setup.getClientOrb().get_primitive_tc(TCKind.tk_double));
        any.create_output_stream().write_double (testValue);
        // don't bounce, because we want to extract from the
        // output stream we just created
        double outValue = any.extract_double();
        assertEquals (testValue, outValue, 0.0);
    }

    public void test_boolean()
        throws Exception
    {
        boolean testValue = true;
        Any outAny = setup.getClientOrb().create_any();
        outAny.insert_boolean(testValue);
        assertEquals(testValue, outAny.extract_boolean());

        Any inAny = server.bounce_any(outAny);

        assertEquals(testValue, inAny.extract_boolean());
        assertTrue(outAny.equal(inAny));
    }

    public void test_boolean_streamable()
        throws Exception
    {
        boolean testValue = false;
        Any outAny = setup.getClientOrb().create_any();
        outAny.insert_Streamable(new BooleanHolder(testValue));
        assertEquals(testValue, outAny.extract_boolean());

        Any inAny = server.bounce_any(outAny);

        assertEquals(testValue, inAny.extract_boolean());
        assertTrue(outAny.equal(inAny));
    }

    public void test_boolean_stream()
    {
        boolean testValue = false;
        Any any = setup.getClientOrb().create_any();
        any.type (setup.getClientOrb().get_primitive_tc(TCKind.tk_boolean));
        any.create_output_stream().write_boolean (testValue);
        // don't bounce, because we want to extract from the
        // output stream we just created
        boolean outValue = any.extract_boolean();
        assertEquals (testValue, outValue);
    }

    public void test_char()
        throws Exception
    {
        char testValue = 'c';
        Any outAny = setup.getClientOrb().create_any();
        outAny.insert_char(testValue);
        assertEquals(testValue, outAny.extract_char());

        Any inAny = server.bounce_any(outAny);

        assertEquals(testValue, inAny.extract_char());
        assertTrue(outAny.equal(inAny));
    }

    public void test_char_streamable()
        throws Exception
    {
        char testValue = 'c';
        Any outAny = setup.getClientOrb().create_any();
        outAny.insert_Streamable(new CharHolder(testValue));
        assertEquals(testValue, outAny.extract_char());

        Any inAny = server.bounce_any(outAny);

        assertEquals(testValue, inAny.extract_char());
        assertTrue(outAny.equal(inAny));
    }

    public void test_char_stream()
    {
        char testValue = 'x';
        Any any = setup.getClientOrb().create_any();
        any.type (setup.getClientOrb().get_primitive_tc(TCKind.tk_char));
        any.create_output_stream().write_char (testValue);
        // don't bounce, because we want to extract from the
        // output stream we just created
        char outValue = any.extract_char();
        assertEquals (testValue, outValue);
    }

    public void test_wchar()
        throws Exception
    {
        char testValue = 'c';
        Any outAny = setup.getClientOrb().create_any();
        outAny.insert_wchar(testValue);
        assertEquals(testValue, outAny.extract_wchar());

        Any inAny = server.bounce_any(outAny);

        assertEquals(testValue, inAny.extract_wchar());
        assertTrue(outAny.equal(inAny));
    }

    public void test_wchar_stream()
    {
        char testValue = 'x';
        Any any = setup.getClientOrb().create_any();
        any.type (setup.getClientOrb().get_primitive_tc(TCKind.tk_wchar));
        any.create_output_stream().write_wchar (testValue);
        // don't bounce, because we want to extract from the
        // output stream we just created
        char outValue = any.extract_wchar();
        assertEquals (testValue, outValue);
    }

    public void test_octet()
        throws Exception
    {
        byte testValue = (byte) 0xFF;

        Any outAny = setup.getClientOrb().create_any();
        outAny.insert_octet(testValue);
        assertEquals(testValue, outAny.extract_octet());

        Any inAny = server.bounce_any(outAny);

        assertEquals(testValue, inAny.extract_octet());
        assertTrue(outAny.equal(inAny));
    }

    public void test_octet_streamable()
        throws Exception
    {
        byte testValue = (byte) 0xFF;

        Any outAny = setup.getClientOrb().create_any();
        outAny.insert_Streamable(new ByteHolder(testValue));
        assertEquals(testValue, outAny.extract_octet());

        Any inAny = server.bounce_any(outAny);

        assertEquals(testValue, inAny.extract_octet());
        assertTrue(outAny.equal(inAny));
    }

    public void test_octet_stream()
    {
        byte testValue = (byte)47;
        Any any = setup.getClientOrb().create_any();
        any.type (setup.getClientOrb().get_primitive_tc(TCKind.tk_octet));
        any.create_output_stream().write_octet (testValue);
        // don't bounce, because we want to extract from the
        // output stream we just created
        byte outValue = any.extract_octet();
        assertEquals (testValue, outValue);
    }

    public void test_any()
        throws Exception
    {
        Any testValue = setup.getClientOrb().create_any();
        testValue.insert_string("foo");

        Any outAny = setup.getClientOrb().create_any();
        outAny.insert_any(testValue);
        assertEquals(testValue, outAny.extract_any());

        Any inAny = server.bounce_any(outAny);

        assertEquals(testValue, inAny.extract_any());
        assertTrue(outAny.equal(inAny));
    }

    public void test_any_streamable()
        throws Exception
    {
        Any testValue = setup.getClientOrb().create_any();
        testValue.insert_string("foo");

        Any outAny = setup.getClientOrb().create_any();
        outAny.insert_Streamable(new AnyHolder(testValue));
        assertEquals(testValue, outAny.extract_any());

        Any inAny = server.bounce_any(outAny);

        assertEquals(testValue, inAny.extract_any());
        assertTrue(outAny.equal(inAny));
    }

    public void test_any_stream()
    {
        Any testValue = setup.getClientOrb().create_any();
        testValue.insert_wstring("hello world");

        Any any = setup.getClientOrb().create_any();
        any.type (setup.getClientOrb().get_primitive_tc(TCKind.tk_any));
        any.create_output_stream().write_any (testValue);
        // don't bounce, because we want to extract from the
        // output stream we just created
        Any outValue = any.extract_any();
        assertTrue (outValue.equal(testValue));
    }

    public void test_string()
        throws Exception
    {
        String testValue = "foo";
        Any outAny = setup.getClientOrb().create_any();
        outAny.insert_string(testValue);
        assertEquals(testValue, outAny.extract_string());

        Any inAny = server.bounce_any(outAny);

        assertEquals(testValue, inAny.extract_string());
        assertTrue(outAny.equal(inAny));
    }

    public void test_string_streamable()
        throws Exception
    {
        String testValue = "foo";

        Any outAny = setup.getClientOrb().create_any();
        outAny.insert_Streamable(new StringHolder(testValue));
        assertEquals(testValue, outAny.extract_string());

        Any inAny = server.bounce_any(outAny);

        assertEquals(testValue, inAny.extract_string());
        assertTrue(outAny.equal(inAny));
    }

    public void test_string_stream()
    {
        String testValue = "hello world";
        Any any = setup.getClientOrb().create_any();
        any.type (setup.getClientOrb().get_primitive_tc(TCKind.tk_string));
        any.create_output_stream().write_string (testValue);
        // don't bounce, because we want to extract from the
        // output stream we just created
        String outValue = any.extract_string();
        assertEquals (testValue, outValue);
    }

    public void test_wstring()
        throws Exception
    {
        String testValue = "foo";

        Any outAny = setup.getClientOrb().create_any();
        outAny.insert_wstring(testValue);
        assertEquals(testValue, outAny.extract_wstring());

        Any inAny = server.bounce_any(outAny);

        assertEquals(testValue, inAny.extract_wstring());
        assertTrue(outAny.equal(inAny));
    }

    public void test_wstring_stream()
    {
        String testValue = "hello world";
        Any any = setup.getClientOrb().create_any();
        any.type (setup.getClientOrb().get_primitive_tc(TCKind.tk_wstring));
        any.create_output_stream().write_wstring (testValue);
        // don't bounce, because we want to extract from the
        // output stream we just created
        String outValue = any.extract_wstring();
        assertEquals (testValue, outValue);
    }

    /**
     * if this test fails others might fail too.
     */
    public void testCorrectClassOnBootclasspath() throws Exception
    {
        TypeCode typeCode = new FixedHolder(new BigDecimal("471.1"))._type();

        String message = "probably using org.omg.CORBA.* from JDK, not JacORB"
                       + " (is JacORB on the bootclasspath?)";
        assertEquals (message, 4, typeCode.fixed_digits());
        assertEquals (message, 1, typeCode.fixed_scale());
    }

    /**
     * @see #testCorrectClassOnBootclasspath()
     */
    public void test_fixed1()
        throws Exception
    {
        BigDecimal testValue = new BigDecimal("471.1");

        Any outAny = setup.getClientOrb().create_any();
        outAny.insert_fixed(testValue,
                            setup.getClientOrb().create_fixed_tc(
                                (short)4,(short)1));
        assertEquals(testValue, outAny.extract_fixed());

        Any inAny = server.bounce_any(outAny);

        assertEquals(testValue, inAny.extract_fixed());
        assertTrue(outAny.equal(inAny));
    }

    /**
     * @see #testCorrectClassOnBootclasspath()
     */
    public void test_fixed_streamable()
        throws Exception
    {
        BigDecimal testValue = new BigDecimal("471.1");

        Any outAny = setup.getClientOrb().create_any();
        outAny.insert_Streamable(new FixedHolder(testValue));
        assertEquals(testValue, outAny.extract_fixed());

        Any inAny = server.bounce_any(outAny);

        assertEquals(testValue, inAny.extract_fixed());
        assertTrue(outAny.equal(inAny));
    }

    public void test_fixed_stream1()
    {
        BigDecimal testValue = new BigDecimal("471.1");
        Any any = setup.getClientOrb().create_any();
        any.type (setup.getClientOrb().create_fixed_tc((short)4,(short)1));
        any.create_output_stream().write_fixed (testValue);
        // don't bounce, because we want to extract from the
        // output stream we just created
        assertEquals (testValue, any.extract_fixed());
    }

    public void test_fixed_stream2()
    {
        BigDecimal testValue = new BigDecimal("471.1");
        Any any = setup.getClientOrb().create_any();
        any.type (setup.getClientOrb().create_fixed_tc((short)4,(short)1));
        ((CDROutputStream)any.create_output_stream()).write_fixed (testValue, (short)4, (short)1);
        // don't bounce, because we want to extract from the
        // output stream we just created
        assertEquals (testValue, any.extract_fixed());
    }

    public void test_object()
        throws Exception
    {
        Any outAny = setup.getClientOrb().create_any();
        outAny.insert_Object(server);
        assertEquals(server, outAny.extract_Object());

        Any inAny = server.bounce_any(outAny);

        //can't readily test equality of object references
        assertTrue(outAny.equal(inAny));
    }

    public void test_object_streamable()
        throws Exception
    {
        Any outAny = setup.getClientOrb().create_any();
        outAny.insert_Streamable(new AnyServerHolder(server));
        assertEquals(server, outAny.extract_Object());

        Any inAny = server.bounce_any(outAny);

        //can't readily test equality of object references
        assertTrue(outAny.equal(inAny));
    }

    public void test_object2()
        throws Exception
    {
        Any outAny = setup.getClientOrb().create_any();
        outAny.insert_Object(server, AnyServerHelper.type());
        assertEquals(server, outAny.extract_Object());

        Any inAny = server.bounce_any(outAny);

        //can't readily test equality of object references
        assertTrue(outAny.equal(inAny));
    }

    public void test_extract_objref()
    {
        Any outAny = setup.getClientOrb().create_any();
        outAny.insert_Object(server, AnyServerHelper.type());
        assertEquals(server, ((org.jacorb.orb.Any)outAny).extract_objref());

        Any inAny = server.bounce_any(outAny);

        //can't readily test equality of object references
        assertTrue(outAny.equal(inAny));
    }

    public void test_object_null()
        throws Exception
    {
        Any outAny = setup.getClientOrb().create_any();
        outAny.insert_Object(null);
        assertNull(outAny.extract_Object());

        Any inAny = server.bounce_any(outAny);

        assertTrue(outAny.equal(inAny));
        assertNull(inAny.extract_Object());
    }

    public void test_object_stream()
    {
        Any any = setup.getClientOrb().create_any();
        any.type (setup.getClientOrb().create_interface_tc
        (
            "IDL:org/jacorb/test/orb/AnyServer:1.0",
            "AnyServer"
        ));
        any.create_output_stream().write_Object (server);
        // don't bounce, because we want to extract from the
        // output stream we just created
        org.omg.CORBA.Object outValue = any.extract_Object();
        assertTrue (outValue._is_a("IDL:org/jacorb/test/orb/AnyServer:1.0"));
    }

    public void test_TypeCode()
        throws Exception
    {
        TypeCode testValue = AnyServerHelper.type();

        Any outAny = setup.getClientOrb().create_any();
        outAny.insert_TypeCode(testValue);
        assertEquals(testValue, outAny.extract_TypeCode());

        Any inAny = server.bounce_any(outAny);

        assertTrue(testValue.equal(inAny.extract_TypeCode()));
        assertTrue(outAny.equal(inAny));
    }

    public void test_TypeCode_streamable()
        throws Exception
    {
        TypeCode testValue = AnyServerHelper.type();

        Any outAny = setup.getClientOrb().create_any();
        outAny.insert_Streamable(new TypeCodeHolder(testValue));
        assertEquals(testValue, outAny.extract_TypeCode());

        Any inAny = server.bounce_any(outAny);

        assertTrue(testValue.equal(inAny.extract_TypeCode()));
        assertTrue(outAny.equal(inAny));
    }

    public void test_principal()
    {
        Any any = setup.getClientOrb().create_any();
        try
        {
            any.insert_Principal(null);
            fail ("should have thrown NO_IMPLEMENT");
        }
        catch (org.omg.CORBA.NO_IMPLEMENT ex)
        {
            // ok
        }

        try
        {
            any.extract_Principal();
            fail ("should have thrown NO_IMPLEMENT");
        }
        catch (org.omg.CORBA.NO_IMPLEMENT ex)
        {
            // ok
        }
    }

    public void test_value_box_string()
        throws Exception
    {
        String testValue = "foo";

        Any outAny = setup.getClientOrb().create_any();
        outAny.insert_Value(testValue);
        assertEquals(testValue, outAny.extract_Value());

        Any inAny = server.bounce_any(outAny);
        assertEquals(testValue, inAny.extract_Value());
        assertTrue(outAny.equal(inAny));
    }

    // use any.insert_value with explicit typecode
    public void test_value_box_string2()
        throws Exception
    {
        String testValue = "foo";

        Any outAny = setup.getClientOrb().create_any();
        outAny.insert_Value(testValue,
                            MyBoxedStringHelper.type());
        assertEquals(testValue, outAny.extract_Value());

        Any inAny = server.bounce_any(outAny);

        assertEquals(testValue, inAny.extract_Value());
        assertTrue(outAny.equal(inAny));
    }

    public void _test_value_box_string_streamable()
        throws Exception
    {
        String testValue = "foo";

        Any outAny = setup.getClientOrb().create_any();
        outAny.insert_Streamable(new MyBoxedStringHolder(testValue));
        assertEquals(testValue, outAny.extract_Value());

        Any inAny = server.bounce_any(outAny);
        assertEquals(testValue, inAny.extract_Value());
        assertTrue(outAny.equal(inAny));
    }

    public void test_value_null()
        throws Exception
    {
        String testValue = null;

        Any outAny = setup.getClientOrb().create_any();
        outAny.insert_Value(testValue);
        assertEquals(testValue, outAny.extract_Value());

        Any inAny = server.bounce_any(outAny);

        assertEquals(testValue, inAny.extract_Value());
        assertTrue(outAny.equal(inAny));
    }

    //insert with helper, extract manually
    public void test_value_box_string_helper()
        throws Exception
    {
        String testValue = "foo";

        Any outAny = setup.getClientOrb().create_any();
        MyBoxedStringHelper.insert(outAny, testValue);
        assertEquals(testValue, outAny.extract_Value());

        Any inAny = server.bounce_any(outAny);
        assertEquals(testValue, inAny.extract_Value());
        assertTrue(outAny.equal(inAny));
    }

    //insert manually, extract with helper
    public void test_value_box_string_helper2()
        throws Exception
    {
        String testValue = "foo";

        Any outAny = setup.getClientOrb().create_any();
        outAny.insert_Value(testValue,
                            MyBoxedStringHelper.type());
        assertEquals(testValue, outAny.extract_Value());

        Any inAny = server.bounce_any(outAny);
        assertEquals(testValue, MyBoxedStringHelper.extract(inAny));
        assertTrue(outAny.equal(inAny));
    }

    //insert and extract with helper
    public void test_value_box_string_helper3()
        throws Exception
    {
        String testValue = "foo";

        Any outAny = setup.getClientOrb().create_any();
        MyBoxedStringHelper.insert(outAny, testValue);

        Any inAny = server.bounce_any(outAny);
        assertEquals(testValue, MyBoxedStringHelper.extract(inAny));
        assertTrue(outAny.equal(inAny));
    }

    public void test_value_box_long()
        throws Exception
    {
        MyBoxedLong testValue = new MyBoxedLong(4711);

        Any outAny = setup.getClientOrb().create_any();
        outAny.insert_Value(testValue);
        assertEquals(testValue.value,
                     ((MyBoxedLong) outAny.extract_Value()).value);

        Any inAny = server.bounce_any(outAny);
        assertEquals(testValue.value,
                     ((MyBoxedLong) inAny.extract_Value()).value);
        assertTrue(outAny.equal(inAny));
    }

    // use any.insert_value with explicit typecode
    public void test_value_box_long2()
        throws Exception
    {
        MyBoxedLong testValue = new MyBoxedLong(4711);

        Any outAny = setup.getClientOrb().create_any();
        outAny.insert_Value(testValue,
                            MyBoxedLongHelper.type());
        assertEquals(testValue.value,
                     ((MyBoxedLong) outAny.extract_Value()).value);

        Any inAny = server.bounce_any(outAny);

        assertEquals(testValue.value,
                     ((MyBoxedLong) inAny.extract_Value()).value);
        assertTrue(outAny.equal(inAny));
    }

    public void _test_value_box_long_streamable()
        throws Exception
    {
        MyBoxedLong testValue = new MyBoxedLong(4711);

        Any outAny = setup.getClientOrb().create_any();
        outAny.insert_Streamable(new MyBoxedLongHolder(testValue));
        assertEquals(testValue.value,
                     ((MyBoxedLong) outAny.extract_Value()).value);

        Any inAny = server.bounce_any(outAny);
        assertEquals(testValue.value,
                     ((MyBoxedLong) inAny.extract_Value()).value);
        assertTrue(outAny.equal(inAny));
    }

    //insert with helper, extract manually
    public void test_value_box_long_helper()
        throws Exception
    {
        MyBoxedLong testValue = new MyBoxedLong(4711);

        Any outAny = setup.getClientOrb().create_any();
        MyBoxedLongHelper.insert(outAny, testValue);
        assertEquals(testValue.value,
                     ((MyBoxedLong) outAny.extract_Value()).value);

        Any inAny = server.bounce_any(outAny);
        assertEquals(testValue.value,
                     ((MyBoxedLong) inAny.extract_Value()).value);
        assertTrue(outAny.equal(inAny));
    }

    //insert manually, extract with helper
    public void test_value_box_long_helper2()
        throws Exception
    {
        MyBoxedLong testValue = new MyBoxedLong(4711);

        Any outAny = setup.getClientOrb().create_any();
        outAny.insert_Value(testValue,
                            MyBoxedLongHelper.type());

        Any inAny = server.bounce_any(outAny);
        assertEquals(testValue.value, MyBoxedLongHelper.extract(inAny).value);
        assertTrue(outAny.equal(inAny));
    }

    //insert and extract with helper
    public void test_value_box_long_helper3()
        throws Exception
    {
        MyBoxedLong testValue = new MyBoxedLong(4711);

        Any outAny = setup.getClientOrb().create_any();
        MyBoxedLongHelper.insert(outAny, testValue);

        Any inAny = server.bounce_any(outAny);
        assertEquals(testValue.value, MyBoxedLongHelper.extract(inAny).value);
        assertTrue(outAny.equal(inAny));
    }

    //probably invalid
    public void _test_valuetype()
        throws Exception
    {
        MyValueType testValue = new MyValueTypeImpl(4711);

        Any outAny = setup.getClientOrb().create_any();
        outAny.insert_Value(testValue);

        Any inAny = server.bounce_any(outAny);
        assertEquals(testValue, inAny.extract_Value());
        assertTrue(outAny.equal(inAny));
    }

    // use any.insert_value with explicit typecode
    public void test_valuetype2()
        throws Exception
    {
        MyValueType testValue = new MyValueTypeImpl(4711);
        Any outAny = setup.getClientOrb().create_any();
        outAny.insert_Value(testValue,
                            MyValueTypeHelper.type());
        assertEquals(testValue, outAny.extract_Value());

        Any inAny = server.bounce_any(outAny);

        assertEquals(testValue, inAny.extract_Value());
        assertTrue(outAny.equal(inAny));
    }

    public void _test_valuetype_streamable()
        throws Exception
    {
        MyValueType testValue = new MyValueTypeImpl(4711);
        Any outAny = setup.getClientOrb().create_any();
        outAny.insert_Streamable(new MyValueTypeHolder(testValue));
        assertEquals(testValue, outAny.extract_Value());

        Any inAny = server.bounce_any(outAny);

        assertEquals(testValue, inAny.extract_Value());
        assertTrue(outAny.equal(inAny));
    }

    //insert with helper, extract manually
    public void test_valuetype_helper()
        throws Exception
    {
        MyValueType testValue = new MyValueTypeImpl(4711);

        Any outAny = setup.getClientOrb().create_any();
        MyValueTypeHelper.insert(outAny, testValue);
        assertEquals(testValue, outAny.extract_Value());

        Any inAny = server.bounce_any(outAny);
        assertEquals(testValue, inAny.extract_Value());
        assertTrue(outAny.equal(inAny));
    }

    //insert manually, extract with helper
    public void test_valuetype_helper2()
        throws Exception
    {
        MyValueType testValue = new MyValueTypeImpl(4711);

        Any outAny = setup.getClientOrb().create_any();
        outAny.insert_Value(testValue,
                            MyValueTypeHelper.type());
        assertEquals(testValue, outAny.extract_Value());

        Any inAny = server.bounce_any(outAny);
        assertEquals(testValue, MyValueTypeHelper.extract(inAny));
        assertTrue(outAny.equal(inAny));
    }

    //insert and extract with helper
    public void test_valuetype_helper3()
        throws Exception
    {
        MyValueType testValue = new MyValueTypeImpl(4711);

        Any outAny = setup.getClientOrb().create_any();
        MyValueTypeHelper.insert(outAny, testValue);

        Any inAny = server.bounce_any(outAny);
        assertEquals(testValue, MyValueTypeHelper.extract(inAny));
        assertTrue(outAny.equal(inAny));
    }

    public void _test_valuetype_stream()
    {
        MyValueType testValue = new MyValueTypeImpl(4711);

        Any any = setup.getClientOrb().create_any();
        any.type (MyValueTypeHelper.type());

        MyValueTypeHelper.write(any.create_output_stream(),
                                testValue);

        // don't bounce, because we want to extract from the
        // output stream we just created
        java.io.Serializable outValue = any.extract_Value();
        assertEquals (testValue, outValue);
    }

    public void test_recursive_struct()
        throws Exception
    {
        Recursive testValue =
            new Recursive(
                new Recursive[]{
                    new Recursive(new Recursive[0]),
                    new Recursive(new Recursive[0])});

        Any outAny = setup.getClientOrb().create_any();
        RecursiveHelper.insert(outAny, testValue);

        Any inAny = server.bounce_any(outAny);
        assertTrue(outAny.equal(inAny));
    }

    public void test_recursive_struct_streamable()
        throws Exception
    {
        Recursive testValue =
            new Recursive(
                new Recursive[]{
                    new Recursive(new Recursive[0]),
                    new Recursive(new Recursive[0])});

        Any outAny = setup.getClientOrb().create_any();
        outAny.insert_Streamable(new RecursiveHolder(testValue));

        Any inAny = server.bounce_any(outAny);
        assertTrue(outAny.equal(inAny));
    }

    public void test_repeated_struct()
        throws Exception
    {
        Recursive r =
            new Recursive(
                new Recursive[]{
                    new Recursive(new Recursive[0]),
                    new Recursive(new Recursive[0])});
        Repeated testValue = new Repeated(r, r, r);

        Any outAny = setup.getClientOrb().create_any();
        RepeatedHelper.insert(outAny, testValue);


        Any inAny = server.bounce_any(outAny);
        assertTrue(outAny.equal(inAny));
    }

    public void test_repeated_struct_streamable()
        throws Exception
    {
        Recursive r =
            new Recursive(
                new Recursive[]{
                    new Recursive(new Recursive[0]),
                    new Recursive(new Recursive[0])});
        Repeated testValue = new Repeated(r, r, r);

        Any outAny = setup.getClientOrb().create_any();
        outAny.insert_Streamable(new RepeatedHolder(testValue));

        Any inAny = server.bounce_any(outAny);
        assertTrue(outAny.equal(inAny));
    }

    public void test_recursive_union()
        throws Exception
    {
        RecursiveUnion testValue = new RecursiveUnion();
        RecursiveUnion b = new RecursiveUnion();
        b.b(new RecursiveUnion[0]);
        testValue.b(new RecursiveUnion[]{b, b});

        Any outAny = setup.getClientOrb().create_any();
        RecursiveUnionHelper.insert(outAny, testValue);

        Any inAny = server.bounce_any(outAny);
        assertTrue(outAny.equal(inAny));
    }

    public void test_recursive_union_streamable()
        throws Exception
    {
        RecursiveUnion testValue = new RecursiveUnion();
        RecursiveUnion b = new RecursiveUnion();
        b.b(new RecursiveUnion[0]);
        testValue.b(new RecursiveUnion[]{b, b});

        Any outAny = setup.getClientOrb().create_any();
        outAny.insert_Streamable(new RecursiveUnionHolder(testValue));

        Any inAny = server.bounce_any(outAny);
        assertTrue(outAny.equal(inAny));
    }

    public void test_recursive_struct_union()
        throws Exception
    {
        RecursiveUnionStructUnion s = new RecursiveUnionStructUnion();
        s.s("foo");
        RecursiveUnionStruct r = new RecursiveUnionStruct(s);

        RecursiveUnionStructUnion b = new RecursiveUnionStructUnion();
        b.b(new RecursiveUnionStruct[]{r, r});
        RecursiveUnionStruct testValue = new RecursiveUnionStruct(b);

        Any outAny = setup.getClientOrb().create_any();
        RecursiveUnionStructHelper.insert(outAny, testValue);

        Any inAny = server.bounce_any(outAny);
        assertTrue(outAny.equal(inAny));
    }

    public void test_recursive_struct_union_streamable()
        throws Exception
    {
        RecursiveUnionStructUnion s = new RecursiveUnionStructUnion();
        s.s("foo");
        RecursiveUnionStruct r = new RecursiveUnionStruct(s);

        RecursiveUnionStructUnion b = new RecursiveUnionStructUnion();
        b.b(new RecursiveUnionStruct[]{r, r});
        RecursiveUnionStruct testValue = new RecursiveUnionStruct(b);

        Any outAny = setup.getClientOrb().create_any();
        outAny.insert_Streamable(new RecursiveUnionStructHolder(testValue));

        Any inAny = server.bounce_any(outAny);
        assertTrue(outAny.equal(inAny));
    }

    //insert via helper, extract manually
    public void test_alias()
        throws Exception
    {
        String testValue = "foo";

        Any outAny = setup.getClientOrb().create_any();
        MyStringAliasHelper.insert(outAny, testValue);

        Any inAny = server.bounce_any(outAny);
        assertEquals(testValue, inAny.extract_string());
        assertTrue(outAny.equal(inAny));
    }

    //compare two anys, one inserted manually, one inserted using helper
    public void test_alias2()
        throws Exception
    {
        String testValue = "foo";

        Any outAny = setup.getClientOrb().create_any();
        MyStringAliasHelper.insert(outAny, testValue);

        Any inAny = setup.getClientOrb().create_any();
        inAny.insert_string(testValue);

        assertTrue(outAny.equal(inAny));
    }

    //insert manually, extract using helper
    public void test_alias3()
        throws Exception
    {
        String testValue = "foo";

        Any outAny = setup.getClientOrb().create_any();
        outAny.insert_string(testValue);

        Any inAny = server.bounce_any(outAny);
        assertEquals(testValue, MyStringAliasHelper.extract(inAny));
        assertTrue(outAny.equal(inAny));
    }

    //insert using helper, extract using helper
    public void test_alias4()
        throws Exception
    {
        String testValue = "foo";

        Any outAny = setup.getClientOrb().create_any();
        MyStringAliasHelper.insert(outAny, testValue);

        Any inAny = server.bounce_any(outAny);
        assertEquals(testValue, MyStringAliasHelper.extract(inAny));
        assertTrue(outAny.equal(inAny));
    }

    public void test_short_sequence() throws Exception
    {
        short[] testValue = new short[] { 44 };

        Any outAny = setup.getClientOrb().create_any();
        MyShortSequenceHelper.insert(outAny, testValue);

        Any inAny = server.bounce_any(outAny);
        assertEquals(testValue[0], MyShortSequenceHelper.extract(inAny)[0]);
        assertTrue(outAny.equal(inAny));
    }

    public void test_ushort_sequence() throws Exception
    {
        short[] testValue = new short[] { 44 };

        Any outAny = setup.getClientOrb().create_any();
        MyUShortSequenceHelper.insert(outAny, testValue);

        Any inAny = server.bounce_any(outAny);
        assertEquals(testValue[0], MyUShortSequenceHelper.extract(inAny)[0]);
        assertTrue(outAny.equal(inAny));
    }

    public void test_long_sequence() throws Exception
    {
        int[] testValue = new int[] { 44 };

        Any outAny = setup.getClientOrb().create_any();
        MyLongSequenceHelper.insert(outAny, testValue);

        Any inAny = server.bounce_any(outAny);
        assertEquals(testValue[0], MyLongSequenceHelper.extract(inAny)[0]);
        assertTrue(outAny.equal(inAny));
    }

    public void test_ulong_sequence() throws Exception
    {
        int[] testValue = new int[] { 44 };

        Any outAny = setup.getClientOrb().create_any();
        MyULongSequenceHelper.insert(outAny, testValue);

        Any inAny = server.bounce_any(outAny);
        assertEquals(testValue[0], MyULongSequenceHelper.extract(inAny)[0]);
        assertTrue(outAny.equal(inAny));
    }

    public void test_float_sequence() throws Exception
    {
        float[] testValue = new float[] { 44.0F };

        Any outAny = setup.getClientOrb().create_any();
        MyFloatSequenceHelper.insert(outAny, testValue);

        Any inAny = server.bounce_any(outAny);
        assertEquals(testValue[0], MyFloatSequenceHelper.extract(inAny)[0], 0.0);
        assertTrue(outAny.equal(inAny));
    }

    public void test_double_sequence() throws Exception
    {
        double[] testValue = new double[] { 44 };

        Any outAny = setup.getClientOrb().create_any();
        MyDoubleSequenceHelper.insert(outAny, testValue);

        Any inAny = server.bounce_any(outAny);
        assertEquals(testValue[0], MyDoubleSequenceHelper.extract(inAny)[0], 0.0);
        assertTrue(outAny.equal(inAny));
    }

    public void test_char_sequence() throws Exception
    {
        char[] testValue = new char[] { 'a' };

        Any outAny = setup.getClientOrb().create_any();
        MyCharSequenceHelper.insert(outAny, testValue);

        Any inAny = server.bounce_any(outAny);
        assertEquals(testValue[0], MyCharSequenceHelper.extract(inAny)[0]);
        assertTrue(outAny.equal(inAny));
    }

    public void test_octet_sequence() throws Exception
    {
        byte[] testValue = new byte[] { 44 };

        Any outAny = setup.getClientOrb().create_any();
        MyOctetSequenceHelper.insert(outAny, testValue);

        Any inAny = server.bounce_any(outAny);
        assertEquals(testValue[0], MyOctetSequenceHelper.extract(inAny)[0]);
        assertTrue(outAny.equal(inAny));
    }

    public void test_longlong_sequence() throws Exception
    {
        long[] testValue = new long[] { 44 };

        Any outAny = setup.getClientOrb().create_any();
        MyLongLongSequenceHelper.insert(outAny, testValue);

        Any inAny = server.bounce_any(outAny);
        assertEquals(testValue[0], MyLongLongSequenceHelper.extract(inAny)[0]);
        assertTrue(outAny.equal(inAny));
    }

    public void test_ulonglong_sequence() throws Exception
    {
        long[] testValue = new long[] { 44 };

        Any outAny = setup.getClientOrb().create_any();
        MyULongLongSequenceHelper.insert(outAny, testValue);

        Any inAny = server.bounce_any(outAny);
        assertEquals(testValue[0], MyULongLongSequenceHelper.extract(inAny)[0]);
        assertTrue(outAny.equal(inAny));
    }

    public void test_wchar_sequence() throws Exception
    {
        char[] testValue = new char[] { 'a' };

        Any outAny = setup.getClientOrb().create_any();
        MyWCharSequenceHelper.insert(outAny, testValue);

        Any inAny = server.bounce_any(outAny);
        assertEquals(testValue[0], MyWCharSequenceHelper.extract(inAny)[0]);
        assertTrue(outAny.equal(inAny));
    }

    public void test_wstring_sequence() throws Exception
    {
        String[] testValue = new String[] { "442" };

        Any outAny = setup.getClientOrb().create_any();
        MyWStringSequenceHelper.insert(outAny, testValue);

        Any inAny = server.bounce_any(outAny);
        assertEquals(testValue[0], MyWStringSequenceHelper.extract(inAny)[0]);
        assertTrue(outAny.equal(inAny));
    }

    public void test_any_sequence() throws Exception
    {
        Any contentAny = setup.getClientOrb().create_any();
        contentAny.insert_boolean(true);
        Any[] testValue = new Any[] { contentAny };

        Any outAny = setup.getClientOrb().create_any();
        MyAnySequenceHelper.insert(outAny, testValue);

        Any inAny = server.bounce_any(outAny);
        assertEquals(testValue[0], MyAnySequenceHelper.extract(inAny)[0]);
        assertTrue(outAny.equal(inAny));
    }

    //same typecode, divverent value
    public void test_equal()
        throws Exception
    {
        Any outAny = setup.getClientOrb().create_any();
        outAny.insert_string("foo");

        Any inAny = setup.getClientOrb().create_any();
        inAny.insert_string("bar");

        assertFalse(outAny.equal(inAny));
    }

    //different typecode
    public void test_equal2()
        throws Exception
    {
        Any outAny = setup.getClientOrb().create_any();
        outAny.insert_wstring("foo");

        Any inAny = setup.getClientOrb().create_any();
        inAny.insert_string("bar");

        assertFalse(outAny.equal(inAny));
    }

    public void test_short_disc_union()
        throws Exception
    {
        ShortDiscUnion testValue = new ShortDiscUnion();
        testValue.s("foo");

        Any outAny = setup.getClientOrb().create_any();
        ShortDiscUnionHelper.insert(outAny, testValue);

        Any inAny = server.bounce_any(outAny);
        assertTrue(outAny.equal(inAny));
    }

    public void test_short_disc_union_streamable()
        throws Exception
    {
        ShortDiscUnion testValue = new ShortDiscUnion();
        testValue.s("foo");

        Any outAny = setup.getClientOrb().create_any();
        outAny.insert_Streamable(new ShortDiscUnionHolder(testValue));

        Any inAny = server.bounce_any(outAny);
        assertTrue(outAny.equal(inAny));
    }

    //test manual stream insert, for when helpers use streamables
    public void test_short_disc_union_manual()
        throws Exception
    {
        ShortDiscUnion testValue = new ShortDiscUnion();
        testValue.s("foo");

        Any outAny = setup.getClientOrb().create_any();
        outAny.type(ShortDiscUnionHelper.type());
        ShortDiscUnionHelper.write(outAny.create_output_stream(), testValue);

        Any inAny = server.bounce_any(outAny);
        assertTrue(outAny.equal(inAny));
    }

    public void test_long_disc_union()
        throws Exception
    {
        LongDiscUnion testValue = new LongDiscUnion();
        testValue.s("foo");

        Any outAny = setup.getClientOrb().create_any();
        LongDiscUnionHelper.insert(outAny, testValue);

        Any inAny = server.bounce_any(outAny);
        assertTrue(outAny.equal(inAny));
    }

    public void test_long_disc_union_streamable()
        throws Exception
    {
        LongDiscUnion testValue = new LongDiscUnion();
        testValue.s("foo");

        Any outAny = setup.getClientOrb().create_any();
        outAny.insert_Streamable(new LongDiscUnionHolder(testValue));

        Any inAny = server.bounce_any(outAny);
        assertTrue(outAny.equal(inAny));
    }

    //test manual stream insert, for when helpers use streamables
    public void test_long_disc_union_manual()
        throws Exception
    {
        LongDiscUnion testValue = new LongDiscUnion();
        testValue.s("foo");

        Any outAny = setup.getClientOrb().create_any();
        outAny.type(LongDiscUnionHelper.type());
        LongDiscUnionHelper.write(outAny.create_output_stream(), testValue);

        Any inAny = server.bounce_any(outAny);
        assertTrue(outAny.equal(inAny));
    }

    public void test_longlong_disc_union() throws Exception
    {
        LongLongDiscUnion testValue = new LongLongDiscUnion();
        testValue.s("foo");

        Any outAny = setup.getClientOrb().create_any();
        LongLongDiscUnionHelper.insert(outAny, testValue);

        Any inAny = server.bounce_any(outAny);
        assertTrue(outAny.equal(inAny));
    }

    public void test_longlong_disc_union_streamable() throws Exception
    {
        LongLongDiscUnion testValue = new LongLongDiscUnion();
        testValue.s("foo");

        Any outAny = setup.getClientOrb().create_any();
        outAny.insert_Streamable(new LongLongDiscUnionHolder(testValue));

        Any inAny = server.bounce_any(outAny);
        assertTrue(outAny.equal(inAny));
    }

    //test manual stream insert, for when helpers use streamables
    public void test_longlong_disc_union_manual() throws Exception
    {
        LongDiscUnion testValue = new LongDiscUnion();
        testValue.s("foo");

        Any outAny = setup.getClientOrb().create_any();
        outAny.type(LongDiscUnionHelper.type());
        LongDiscUnionHelper.write(outAny.create_output_stream(), testValue);

        Any inAny = server.bounce_any(outAny);
        assertTrue(outAny.equal(inAny));
    }

    public void test_ushort_disc_union()
        throws Exception
    {
        UShortDiscUnion testValue = new UShortDiscUnion();
        testValue.s("foo");

        Any outAny = setup.getClientOrb().create_any();
        UShortDiscUnionHelper.insert(outAny, testValue);

        Any inAny = server.bounce_any(outAny);
        assertTrue(outAny.equal(inAny));
    }

    public void test_ushort_disc_union_streamable()
        throws Exception
    {
        UShortDiscUnion testValue = new UShortDiscUnion();
        testValue.s("foo");

        Any outAny = setup.getClientOrb().create_any();
        outAny.insert_Streamable(new UShortDiscUnionHolder(testValue));

        Any inAny = server.bounce_any(outAny);
        assertTrue(outAny.equal(inAny));
    }

    //test manual stream insert, for when helpers use streamables
    public void test_ushort_disc_union_manual()
        throws Exception
    {
        UShortDiscUnion testValue = new UShortDiscUnion();
        testValue.s("foo");

        Any outAny = setup.getClientOrb().create_any();
        outAny.type(UShortDiscUnionHelper.type());
        UShortDiscUnionHelper.write(outAny.create_output_stream(), testValue);

        Any inAny = server.bounce_any(outAny);
        assertTrue(outAny.equal(inAny));
    }

    public void test_ulong_disc_union()
        throws Exception
    {
        ULongDiscUnion testValue = new ULongDiscUnion();
        testValue.s("foo");

        Any outAny = setup.getClientOrb().create_any();
        ULongDiscUnionHelper.insert(outAny, testValue);

        Any inAny = server.bounce_any(outAny);
        assertTrue(outAny.equal(inAny));
    }

    public void test_ulong_disc_union_streamable()
        throws Exception
    {
        ULongDiscUnion testValue = new ULongDiscUnion();
        testValue.s("foo");

        Any outAny = setup.getClientOrb().create_any();
        outAny.insert_Streamable(new ULongDiscUnionHolder(testValue));

        Any inAny = server.bounce_any(outAny);
        assertTrue(outAny.equal(inAny));
    }

    //test manual stream insert, for when helpers use streamables
    public void test_ulong_disc_union_manual()
        throws Exception
    {
        ULongDiscUnion testValue = new ULongDiscUnion();
        testValue.s("foo");

        Any outAny = setup.getClientOrb().create_any();
        outAny.type(ULongDiscUnionHelper.type());
        ULongDiscUnionHelper.write(outAny.create_output_stream(), testValue);

        Any inAny = server.bounce_any(outAny);
        assertTrue(outAny.equal(inAny));
    }

    public void test_ulonglong_disc_union() throws Exception
    {
        ULongLongDiscUnion testValue = new ULongLongDiscUnion();
        testValue.s("foo");

        Any outAny = setup.getClientOrb().create_any();
        ULongLongDiscUnionHelper.insert(outAny, testValue);

        Any inAny = server.bounce_any(outAny);
        assertTrue(outAny.equal(inAny));
    }

    public void test_ulonglong_disc_union_streamable() throws Exception
    {
        ULongLongDiscUnion testValue = new ULongLongDiscUnion();
        testValue.s("foo");

        Any outAny = setup.getClientOrb().create_any();
        outAny.insert_Streamable(new ULongLongDiscUnionHolder(testValue));

        Any inAny = server.bounce_any(outAny);
        assertTrue(outAny.equal(inAny));
    }

    //test manual stream insert, for when helpers use streamables
    public void test_ulonglong_disc_union_manual() throws Exception
    {
        ULongLongDiscUnion testValue = new ULongLongDiscUnion();
        testValue.s("foo");

        Any outAny = setup.getClientOrb().create_any();
        outAny.type(ULongLongDiscUnionHelper.type());
        ULongLongDiscUnionHelper.write(outAny.create_output_stream(), testValue);

        Any inAny = server.bounce_any(outAny);
        assertTrue(outAny.equal(inAny));
    }

    public void test_boolean_disc_union()
        throws Exception
    {
        BooleanDiscUnion testValue = new BooleanDiscUnion();
        testValue.s("foo");

        Any outAny = setup.getClientOrb().create_any();
        BooleanDiscUnionHelper.insert(outAny, testValue);

        Any inAny = server.bounce_any(outAny);
        assertTrue(outAny.equal(inAny));
    }

    public void test_boolean_disc_union_streamable()
        throws Exception
    {
        BooleanDiscUnion testValue = new BooleanDiscUnion();
        testValue.s("foo");

        Any outAny = setup.getClientOrb().create_any();
        outAny.insert_Streamable(new BooleanDiscUnionHolder(testValue));

        Any inAny = server.bounce_any(outAny);
        assertTrue(outAny.equal(inAny));
    }

    //test manual stream insert, for when helpers use streamables
    public void test_boolean_disc_union_manual()
        throws Exception
    {
        BooleanDiscUnion testValue = new BooleanDiscUnion();
        testValue.s("foo");

        Any outAny = setup.getClientOrb().create_any();
        outAny.type(BooleanDiscUnionHelper.type());
        BooleanDiscUnionHelper.write(outAny.create_output_stream(), testValue);

        Any inAny = server.bounce_any(outAny);
        assertTrue(outAny.equal(inAny));
    }

    public void test_char_disc_union()
        throws Exception
    {
        CharDiscUnion testValue = new CharDiscUnion();
        testValue.s("foo");

        Any outAny = setup.getClientOrb().create_any();
        CharDiscUnionHelper.insert(outAny, testValue);

        Any inAny = server.bounce_any(outAny);
        assertTrue(outAny.equal(inAny));
    }

    public void test_char_disc_union_streamable()
        throws Exception
    {
        CharDiscUnion testValue = new CharDiscUnion();
        testValue.s("foo");

        Any outAny = setup.getClientOrb().create_any();
        outAny.insert_Streamable(new CharDiscUnionHolder(testValue));

        Any inAny = server.bounce_any(outAny);
        assertTrue(outAny.equal(inAny));
    }

    //test manual stream insert, for when helpers use streamables
    public void test_char_disc_union_manual()
        throws Exception
    {
        CharDiscUnion testValue = new CharDiscUnion();
        testValue.s("foo");

        Any outAny = setup.getClientOrb().create_any();
        outAny.type(CharDiscUnionHelper.type());
        CharDiscUnionHelper.write(outAny.create_output_stream(), testValue);

        Any inAny = server.bounce_any(outAny);
        assertTrue(outAny.equal(inAny));
    }

    public void test_enum_disc_union()
        throws Exception
    {
        EnumDiscUnion testValue = new EnumDiscUnion();
        testValue.s("foo");

        Any outAny = setup.getClientOrb().create_any();
        EnumDiscUnionHelper.insert(outAny, testValue);

        Any inAny = server.bounce_any(outAny);
        assertTrue(outAny.equal(inAny));
    }

    public void test_enum_disc_union_streamable()
        throws Exception
    {
        EnumDiscUnion testValue = new EnumDiscUnion();
        testValue.s("foo");

        Any outAny = setup.getClientOrb().create_any();
        outAny.insert_Streamable(new EnumDiscUnionHolder(testValue));

        Any inAny = server.bounce_any(outAny);
        assertTrue(outAny.equal(inAny));
    }

    //test manual stream insert, for when helpers use streamables
    public void test_enum_disc_union_manual()
        throws Exception
    {
        EnumDiscUnion testValue = new EnumDiscUnion();
        testValue.s("foo");

        Any outAny = setup.getClientOrb().create_any();
        outAny.type(EnumDiscUnionHelper.type());
        EnumDiscUnionHelper.write(outAny.create_output_stream(), testValue);

        Any inAny = server.bounce_any(outAny);
        assertTrue(outAny.equal(inAny));
    }

    public void test_extract_streamable()
    {
        String testValue = "hello world";

        Any outAny = setup.getClientOrb().create_any();
        outAny.insert_Streamable(new StringHolder(testValue));
        Streamable s = outAny.extract_Streamable();
        assertEquals (testValue, ((StringHolder)s).value);

        Any inAny = server.bounce_any(outAny);
        assertTrue (outAny.equal(inAny));
    }

    public void test_extract_streamable_null()
    {
        Any any = setup.getClientOrb().create_any();
        try
        {
            any.extract_Streamable();
            fail ("should have thrown BAD_OPERATION");
        }
        catch (org.omg.CORBA.BAD_OPERATION ex)
        {
            // ok
        }
        catch (Exception ex)
        {
            fail ("should have thrown BAD_OPERATION");
        }
    }

    public void test_to_string()
    {
        Any any = setup.getClientOrb().create_any();
        assertEquals ("null", any.toString());

        any.insert_string("hello world");
        assertEquals ("hello world", any.toString());
    }

    public void test_not_equal()
    {
        Any any = setup.getClientOrb().create_any();
        any.insert_string("hello world");
        assertFalse (any.equals ("hello world"));
    }

    public void test_not_equal_to_null()
    {
        Any any = setup.getClientOrb().create_any();
        try
        {
            any.equal (null);
            fail ("should have raised BAD_PARAM");
        }
        catch (org.omg.CORBA.BAD_PARAM ex)
        {
            // ok, this seems to be required by the spec
        }
        catch (Exception ex)
        {
            fail ("should have raised BAD_PARAM");
        }
    }

    public void test_shallow_copy()
    {
        Any any1 = setup.getClientOrb().create_any();
        any1.insert_string("foobar");
        Any any2 = setup.getClientOrb().create_any();
        ((org.jacorb.orb.Any)any2).insert_object(any1.type(),
                                                 any1.extract_string());
        assertTrue (any1.extract_string() == any2.extract_string());
    }

    public void testEquals() throws Exception
    {
        Any any1 = setup.getClientOrb().create_any();
        Any any2 = setup.getClientOrb().create_any();

        any1.insert_Object(null);
        any2.insert_Object(null);

        assertEquals(any1, any2);
        assertEquals(any2, any1);

        any1.insert_Object(server);
        any2.insert_Object(server);

        assertEquals(any1, any2);
        assertEquals(any2, any1);

        any1.insert_Object(null, AnyServerHelper.type());
        assertFalse(any1.equals(any2));
        assertFalse(any2.equals(any1));
    }
}
