package org.jacorb.test.bugs.bug387;

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
import static org.junit.Assert.assertTrue;
import org.jacorb.test.harness.ClientServerSetup;
import org.jacorb.test.harness.ClientServerTestCase;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests marshaling of value box instances within structs within anys.
 *
 * @author <a href="mailto:spiegel@gnu.org">Andre Spiegel</a>
 */
public class Bug387Test extends ClientServerTestCase
{
    private TestInterface server = null;

    @Before
    public void setUp()
    {
        server = TestInterfaceHelper.narrow(setup.getServerObject());
    }

    @BeforeClass
    public static void beforeClassSetUp() throws Exception
    {
        setup = new ClientServerSetup("org.jacorb.test.bugs.bug387.TestInterfaceImpl");
    }

    @Test
    public void test_return_value()
    {
        org.omg.CORBA.Any a = server.test_return_value();
        TestStruct t = TestStructHelper.extract(a);
        assertEquals("STRINGTEST", t.name);
    }

    @Test
    public void test_return_null()
    {
        org.omg.CORBA.Any a = server.test_return_null();
        TestStruct t = TestStructHelper.extract(a);
        assertEquals(null, t.name);
    }

    @Test
    public void test_pass_value()
    {
        org.omg.CORBA.Any a = setup.getClientOrb().create_any();
        TestStruct t = new TestStruct("STRINGTEST", null, 1);
        TestStructHelper.insert(a, t);
        boolean result = server.test_pass_value(a, "STRINGTEST");
        assertTrue(result);
    }

    @Test
    public void test_pass_null()
    {
        org.omg.CORBA.Any a = setup.getClientOrb().create_any();
        TestStruct t = new TestStruct(null, null, 1);
        TestStructHelper.insert(a, t);
        boolean result = server.test_pass_null(a);
        assertTrue(result);
    }

    @Test
    public void test_pass_unshared()
    {
        org.omg.CORBA.Any a = setup.getClientOrb().create_any();
        String s1 = "hello";
        String s2 = new String(s1);
        TestStruct t = new TestStruct(s1, s2, 1);
        TestStructHelper.insert(a, t);
        boolean result = server.test_pass_shared(a);
        assertFalse(result);
    }

    @Test
    public void test_pass_shared()
    {
        org.omg.CORBA.Any a = setup.getClientOrb().create_any();
        String s1 = "hello";
        String s2 = s1;
        TestStruct t = new TestStruct(s1, s2, 1);
        TestStructHelper.insert(a, t);
        boolean result = server.test_pass_shared(a);
        assertTrue(result);
    }
}
