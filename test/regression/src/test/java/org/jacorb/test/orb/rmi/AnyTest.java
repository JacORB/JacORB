package org.jacorb.test.orb.rmi;

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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.io.File;
import java.math.BigDecimal;
import java.util.Properties;
import org.apache.commons.io.FileUtils;
import org.jacorb.orb.CDROutputStream;
import org.jacorb.test.MyUserException;
import org.jacorb.test.MyUserExceptionHelper;
import org.jacorb.test.harness.ClientServerSetup;
import org.jacorb.test.harness.ClientServerTestCase;
import org.jacorb.test.orb.RecursiveUnionStructPackage.RecursiveUnionStructUnion;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.omg.CORBA.Any;
import org.omg.CORBA.AnyHolder;
import org.omg.CORBA.BAD_PARAM;
import org.omg.CORBA.BAD_PARAMHelper;
import org.omg.CORBA.BooleanHolder;
import org.omg.CORBA.ByteHolder;
import org.omg.CORBA.CharHolder;
import org.omg.CORBA.DoubleHolder;
import org.omg.CORBA.FixedHolder;
import org.omg.CORBA.FloatHolder;
import org.omg.CORBA.IntHolder;
import org.omg.CORBA.LongHolder;
import org.omg.CORBA.NO_IMPLEMENT;
import org.omg.CORBA.ShortHolder;
import org.omg.CORBA.StringHolder;
import org.omg.CORBA.TCKind;
import org.omg.CORBA.TypeCode;
import org.omg.CORBA.TypeCodeHolder;
import org.omg.CORBA.portable.InputStream;
import org.omg.CORBA.portable.OutputStream;
import org.omg.CORBA.portable.Streamable;
import org.jacorb.test.orb.*;

public class AnyTest extends ClientServerTestCase
{
    private AnyServer server;

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Before
    public void setUp() throws Exception
    {
        server = AnyServerHelper.narrow( setup.getServerObject() );
    }

    @BeforeClass
    public static void beforeClassSetUp() throws Exception
    {

        Properties props = new Properties();
        props.put("jacorb.compactTypecodes", "off");

        setup = new ClientServerSetup( AnyServerImpl.class.getName(), props, props);
    }

    @Test
    public void testRMI_value_box_string()
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
    @Test
    public void testRMI_value_box_string2()
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

    //insert with helper, extract manually
    @Test
    public void testRMI_value_box_string_helper()
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
    @Test
    public void testRMI_value_box_string_helper2()
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
    @Test
    public void testRMI_value_box_string_helper3()
        throws Exception
    {
        String testValue = "foo";

        Any outAny = setup.getClientOrb().create_any();
        MyBoxedStringHelper.insert(outAny, testValue);

        Any inAny = server.bounce_any(outAny);
        assertEquals(testValue, MyBoxedStringHelper.extract(inAny));
        assertTrue(outAny.equal(inAny));
    }

    @Test
    public void testRMI_value_box_long()
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
    @Test
    public void testRMI_value_box_long2()
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

    //insert with helper, extract manually
    @Test
    public void testRMI_value_box_long_helper()
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
    @Test
    public void testRMI_value_box_long_helper2()
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
    @Test
    public void testRMI_value_box_long_helper3()
        throws Exception
    {
        MyBoxedLong testValue = new MyBoxedLong(4711);

        Any outAny = setup.getClientOrb().create_any();
        MyBoxedLongHelper.insert(outAny, testValue);

        Any inAny = server.bounce_any(outAny);
        assertEquals(testValue.value, MyBoxedLongHelper.extract(inAny).value);
        assertTrue(outAny.equal(inAny));
    }

    // use any.insert_value with explicit typecode
    @Test
    public void testRMI_valuetype2()
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

    //insert with helper, extract manually
    @Test
    public void testRMI_valuetype_helper()
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
    @Test
    public void testRMI_valuetype_helper2()
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
    @Test
    public void testRMI_valuetype_helper3()
        throws Exception
    {
        MyValueType testValue = new MyValueTypeImpl(4711);

        Any outAny = setup.getClientOrb().create_any();
        MyValueTypeHelper.insert(outAny, testValue);

        Any inAny = server.bounce_any(outAny);
        assertEquals(testValue, MyValueTypeHelper.extract(inAny));
        assertTrue(outAny.equal(inAny));
    }

}
