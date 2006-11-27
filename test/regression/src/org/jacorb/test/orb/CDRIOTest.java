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

import junit.framework.*;
import org.omg.CORBA.ORB;
import org.omg.CORBA.portable.InputStream;

import java.io.IOException;

import org.jacorb.test.common.*;

/**
 * Test CDRInputStream and CDROutputStream.
 *
 * @jacorb-client-since 2.2
 */
public class CDRIOTest extends JacORBTestCase
{
    private ORB orb;
    private org.omg.CORBA.portable.OutputStream os;

    /**
     * <code>ORBInitTest</code> constructor - for JUnit.
     *
     * @param name a <code>String</code> value
     */
    public CDRIOTest (String name)
    {
        super (name);
    }

    protected void setUp() throws Exception
    {
        super.setUp();
        orb = org.omg.CORBA.ORB.init( (String[])null, null );
        os = orb.create_output_stream();
    }

    protected void tearDown() throws Exception
    {
        os.close();
        os = null;
        orb.shutdown(true);
        orb = null;
        super.tearDown();
    }

    /**
     * <code>suite</code> lists the tests for Junit to run.
     *
     * @return a <code>Test</code> value
     */
    public static Test suite ()
    {
        TestSuite suite = new JacORBTestSuite("CDRIO Test",
                                              CDRIOTest.class);

        suite.addTest(new CDRIOTest("testLong"));
        suite.addTest(new CDRIOTest("testString"));
        suite.addTest(new CDRIOTest("testWString"));
        suite.addTest(new CDRIOTest("testBoolean"));
        suite.addTest(new CDRIOTest("testShort"));
        suite.addTest(new CDRIOTest("testReadString"));
        suite.addTest(new CDRIOTest("testRead"));
        suite.addTest(new CDRIOTest("testMultiple"));

        return suite;
    }

    public void testLong () throws IOException
    {
        int pre = 123;
        os.write_long(pre);
        InputStream is = os.create_input_stream();
        assertTrue(is.available() > 0);
        int post = is.read_long();
        assertEquals(pre, post);
    }

    public void testString() throws IOException
    {
        String pre = "abc";
        os.write_string(pre);
        InputStream is = os.create_input_stream();
        assertTrue(is.available() > 0);
        String post = is.read_string();
        assertEquals(pre, post);
    }

    public void testWString() throws IOException
    {
        String pre = "abc";
        os.write_wstring(pre);
        InputStream is = os.create_input_stream();
        assertTrue(is.available() > 0);
        String post = is.read_wstring();
        assertEquals(pre, post);
    }

    public void testBoolean() throws IOException
    {
        boolean pre = true;
        os.write_boolean(pre);
        InputStream is = os.create_input_stream();
        assertTrue(is.available() > 0);
        boolean post = is.read_boolean();
        assertEquals(pre, post);
    }

    public void testShort() throws IOException
    {
        short pre = 234;
        os.write_short(pre);
        InputStream is = os.create_input_stream();
        assertTrue(is.available() > 0);
        short post = is.read_short();
        assertEquals(pre, post);
    }

    public void testReadString() throws IOException
    {
        os.write_string("abcde");
        InputStream is = os.create_input_stream();

        assertTrue(is.available() > 0);
        byte[] buf = new byte[is.available()];
        int readAmount = is.read(buf);
        assertTrue(readAmount > 0);

        os = orb.create_output_stream();
        os.write_octet_array(buf, 0, buf.length);
        InputStream is2 = os.create_input_stream();
        assertEquals("abcde", is2.read_string());
    }

    public void testRead() throws IOException
    {
        os.write_string("thing-one");
        os.write_long(123);
        os.write_string("thing-two");

        InputStream is = os.create_input_stream();
        assertTrue(is.available() > 0);
        assertEquals("thing-one", is.read_string());
        assertEquals(123, is.read_long());

        assertTrue(is.available() > 0);
        byte[] buf = new byte[is.available()];
        int readAmount = is.read(buf);
        assertTrue(readAmount > 0);

        os = orb.create_output_stream();
        os.write_octet_array(buf, 0, buf.length);
        InputStream is2 = os.create_input_stream();
        assertEquals("thing-two", is2.read_string());
    }

    public void testMultiple() throws IOException
    {
        os.write_boolean(true);
        os.write_char('a');
        os.write_double(1.234);
        os.write_float(2.345f);
        os.write_long(987);
        os.write_longlong(876);
        os.write_short((short)13);
        os.write_string("hi");
        os.write_ulong(765);
        os.write_ulonglong(567);
        os.write_ushort((short)23);
        os.write_wchar('b');
        os.write_wstring("bye");

        InputStream is = os.create_input_stream();
        assertTrue(is.available() > 0);

        assertEquals(is.read_boolean(), true);
        assertEquals(is.read_char(), 'a');
        assertEquals(is.read_double(), 1.234, 0.001);
        assertEquals(is.read_float(), 2.345f, 0.001f);
        assertEquals(is.read_long(), 987);
        assertEquals(is.read_longlong(), 876);
        assertEquals(is.read_short(), (short)13);
        assertEquals(is.read_string(), "hi");
        assertEquals(is.read_ulong(), 765);
        assertEquals(is.read_ulonglong(), 567);
        assertEquals(is.read_ushort(), (short)23);
        assertEquals(is.read_wchar(), 'b');
        assertEquals(is.read_wstring(), "bye");
    }

}
