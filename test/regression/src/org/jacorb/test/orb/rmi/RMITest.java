package org.jacorb.test.orb.rmi;

/*
 *        JacORB  - a free Java ORB
 *
 *   Copyright (C) 1997-2003  Gerald Brose.
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

import java.rmi.Remote;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import javax.rmi.PortableRemoteObject;

import org.jacorb.test.common.ClientServerSetup;
import org.jacorb.test.common.ClientServerTestCase;
import org.jacorb.test.orb.rmi.Outer.StaticInner;

/**
 * Abstract testclass for RMITests. subclasses are responsible for
 * choosing which ORB should run on client and server.
 *
 * @see SunJacORBRMITest
 * @see SunSunRMITest
 * @see JacORBJacORBRMITest
 * @see JacORBSunRMITest
 *
 * @version $Id$
 */
public abstract class RMITest extends ClientServerTestCase
{
    private RMITestInterface server;

    public RMITest(String name, ClientServerSetup setup)
    {
        super(name, setup);
    }

    public final void setUp() throws Exception
    {
        server = (RMITestInterface)javax.rmi.PortableRemoteObject.narrow(
                                                    setup.getServerObject(),
                                                    RMITestInterface.class);
    }

    public void test_getString() throws Exception
    {
        String s = server.getString();
        assertEquals(RMITestUtil.STRING, s);
    }

    public void test_primitiveTypes() throws Exception
    {
        String s;

        s = server.testPrimitiveTypes(false,
                'A',
                Byte.MIN_VALUE,
                Short.MIN_VALUE,
                Integer.MIN_VALUE,
                Long.MIN_VALUE,
                Float.MIN_VALUE,
                Double.MIN_VALUE);

        assertEquals(RMITestUtil.primitiveTypesToString(false,
                'A',
                Byte.MIN_VALUE,
                Short.MIN_VALUE,
                Integer.MIN_VALUE,
                Long.MIN_VALUE,
                Float.MIN_VALUE,
                Double.MIN_VALUE),
                s);

        s = server.testPrimitiveTypes(true,
                'Z',
                Byte.MAX_VALUE,
                Short.MAX_VALUE,
                Integer.MAX_VALUE,
                Long.MAX_VALUE,
                Float.MAX_VALUE,
                Double.MAX_VALUE);

        assertEquals(RMITestUtil.primitiveTypesToString(true,
                'Z',
                Byte.MAX_VALUE,
                Short.MAX_VALUE,
                Integer.MAX_VALUE,
                Long.MAX_VALUE,
                Float.MAX_VALUE,
                Double.MAX_VALUE),
                s);
    }

    public void test_String() throws Exception
    {
        String original = "0123456789";
        String echoedBack = server.testString("0123456789");
        assertEquals(RMITestUtil.echo(original), echoedBack);
    }

    public void test_RMITestInterface() throws Exception
    {
        RMITestInterface t = server.testRMITestInterface("the quick brown fox", server);
        String s = t.getString();
        assertEquals(RMITestUtil.STRING, s);
    }

    public void test_Remote() throws Exception
    {
        Remote r = server.testRemote("jumps over the lazy dog", server);
        RMITestInterface t =
            (RMITestInterface)PortableRemoteObject.narrow(r,
                    RMITestInterface.class);
        String s = t.getString();
        assertEquals(RMITestUtil.STRING, s);
    }

    public void test_Serializable() throws Exception
    {
        Foo original = new Foo(7, "foo test");
        Foo echoedBack = server.testSerializable(original);
        assertEquals(RMITestUtil.echoFoo(original), echoedBack);
    }

    public void test_intArray() throws Exception
    {
        int[] original= new int[10];
        for (int i = 0; i < original.length; i++)
        {
            original[i] = 100 + i;
        }
        int[] echoedBack = server.testIntArray(original);
        assertEquals(original.length, echoedBack.length);
        for (int i = 0; i < echoedBack.length; i++)
        {
            assertEquals(original[i] + 1, echoedBack[i]);
        }
    }

    public void test_valueArray() throws Exception
    {
        Foo[] original = new Foo[4];
        for (int i = 0; i < original.length; i++)
        {
            original[i] = new Foo(100 + i, "foo array test");
        }
        Foo[] echoedBack = server.testValueArray(original);
        assertEquals(original.length, echoedBack.length);
        for (int i = 0; i < echoedBack.length; i++)
        {
            assertEquals(RMITestUtil.echoFoo(original[i]), echoedBack[i]);
        }
    }

    public void test_exception() throws Exception
    {
        assertEquals("#0", server.testException(0));
        assertEquals("#1", server.testException(1));
        assertEquals("#2", server.testException(2));

        try
        {
            server.testException(-2);
            fail("NegativeArgumentException expected but not thrown.");
        }
        catch (NegativeArgumentException na)
        {
            assertEquals(-2, na.getNegativeArgument());
        }

        try
        {
            server.testException(-1);
            fail("NegativeArgumentException expected but not thrown.");
        }
        catch (NegativeArgumentException na)
        {
            assertEquals(-1, na.getNegativeArgument());
        }

        assertEquals("#0", server.testException(0));
    }

    public void test_FooValueToObject() throws Exception
    {
        Foo original = new Foo(9999, "foo test");
        java.lang.Object echoedBack = server.fooValueToObject(original);
        assertEquals(RMITestUtil.echoFoo(original), echoedBack);
    }

    public void test_BooValueToObject() throws Exception
    {
        Boo original = new Boo("t1", "boo test");
        java.lang.Object echoedBack = server.booValueToObject(original);
        assertEquals(RMITestUtil.echoBoo(original), echoedBack);
    }

    public void test_valueArrayToVector() throws Exception
    {
        Foo[] original = new Foo[4];
        for (int i = 0; i < original.length; i++)
        {
            original[i] = new Foo(100 + i, "foo vector test");
        }
        java.util.Vector v = server.valueArrayToVector(original);
        java.lang.Object[] echoedBack = v.toArray();
        assertEquals(original.length, echoedBack.length);
        for (int i = 0; i < echoedBack.length; i++)
        {
            assertEquals(RMITestUtil.echoFoo(original[i]), echoedBack[i]);
        }
    }

    public void test_vectorToValueArray() throws Exception
    {
        Foo[] original = new Foo[4];
        for (int i = 0; i < original.length; i++)
        {
            original[i] = new Foo(100 + i, "foo vector test");
        }

        java.util.Vector v = server.valueArrayToVector(original);
        Foo[] echoedBack = server.vectorToValueArray(v);
        assertEquals(original.length, echoedBack.length);
        for (int i = 0; i < echoedBack.length; i++)
        {
            assertEquals(
                    RMITestUtil.echoFoo(RMITestUtil.echoFoo(original[i])),
                    echoedBack[i]);
        }
    }

    public void test_getException() throws Exception
    {
        java.lang.Object obj = server.getException();
        NegativeArgumentException na = (NegativeArgumentException)obj;
        assertEquals(-7777, na.getNegativeArgument());
    }

    public void test_getZooValue() throws Exception
    {
        java.lang.Object obj = server.getZooValue();
        assertEquals(new Zoo("outer_zoo!",
                "returned by getZooValue",
                new Zoo("inner_zoo!", "inner")),
                obj);
    }

    public void test_referenceSharingWithinArray() throws Exception
    {
        int n = 100;
        Object[] original = new Object[n];
        for (int i = 0; i < n; i++)
        {
            original[i] = new Boo("t" + i, "boo array test");
        }
        Object[] echoedBack =
            server.testReferenceSharingWithinArray(original);
        assertEquals(2 * n, echoedBack.length);

        for (int i = 0; i < n; i++)
        {
            assertEquals(original[i], echoedBack[i]);
            assertEquals(original[i], echoedBack[i + n]);
            assertSame(echoedBack[i], echoedBack[i + n]);
        }
    }

    public void test_referenceSharingWithinCollection() throws Exception
    {
        java.util.Collection original = new java.util.ArrayList();
        int n = 10;
        for (int i = 0; i < n; i++)
        {
            original.add(new Foo(100 + i, "foo collection test"));
        }
        java.util.Collection echoedBack =
            server.testReferenceSharingWithinCollection(original);
        assertEquals(2 * n, echoedBack.size());

        java.util.ArrayList originalList = (java.util.ArrayList)original;
        java.util.ArrayList echoedList = (java.util.ArrayList)echoedBack;


        for (int i = 0; i < n; i++)
        {
            assertEquals(originalList.get(i), echoedList.get(i));
            assertEquals(originalList.get(i), echoedList.get(i + n));
            assertSame(echoedList.get(i), echoedList.get(i + n));
        }
    }

    public void test_getVectorWithObjectArrayAsElement() throws Exception
    {
        java.util.Vector vector =
            server.getVectorWithObjectArrayAsElement();
        assertTrue(vector.size() == 1);
        Object[] inner = (Object[]) vector.get(0);
        assertEquals(new Integer(1), inner[0]);
        assertEquals(new Integer(2), inner[1]);
        assertEquals("Third Element", inner[2]);
    }

    public void test_getVectorWithVectorAsElement() throws Exception
    {
        java.util.Vector vector =
            server.getVectorWithVectorAsElement();
        assertTrue(vector.size() == 1);
        java.util.Vector inner = (java.util.Vector) vector.get(0);
        assertEquals(new Integer(1), inner.get(0));
        assertEquals(new Integer(2), inner.get(1));
        assertEquals("Third Element", inner.get(2));
    }

    public void test_getVectorWithHashtableAsElement() throws Exception
    {
        java.util.Vector vector =
            server.getVectorWithHashtableAsElement();
        assertTrue(vector.size() == 1);
        java.util.Hashtable inner = (java.util.Hashtable) vector.get(0);
        assertEquals(new Integer(1), inner.get(new Integer(0)));
        assertEquals(new Integer(2), inner.get(new Integer(1)));
        assertEquals("Third Element", inner.get(new Integer(2)));
    }

    public void testPassStaticInnerClass() throws Exception
    {
        StaticInner expect = new StaticInner("staticInner");
        StaticInner result = server.staticInnerToStaticInner(expect);
        assertEquals(expect, result);
    }

    public void testPassInnerClass() throws Exception
    {
        Outer expect = new Outer("outer");
        Outer result = server.outerToOuter(expect);
        assertEquals(expect, result);
    }

    public void testPassCollection() throws Exception
    {
        assertEquals(0, server.sizeOfCollection(Collections.EMPTY_LIST));
    }

    public void testPassSerializable0() throws Exception
    {
        Date date = new Date();
        ArrayList list = new ArrayList();
        ObjectParam param = new ObjectParam(date.toString());
        list.add(param);

        ArrayList result = (ArrayList) server.transmitSerializable(list);
        assertEquals(param.payload, ((ObjectParam)result.get(0)).payload);
    }

    public void testPassSerializable1() throws Exception
    {
        Date date = new Date();
        ArrayList list = new ArrayList();
        StringParam param = new StringParam(date.toString());
        list.add(param);

        ArrayList result = (ArrayList) server.transmitSerializable(list);
        assertEquals(param.payload, ((StringParam)result.get(0)).payload);
    }
}
