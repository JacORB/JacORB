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
import java.util.Collections;
import java.util.Properties;
import javax.rmi.PortableRemoteObject;
import junit.framework.Test;
import junit.framework.TestSuite;
import junit.framework.Assert;
import org.jacorb.test.common.ClientServerSetup;
import org.jacorb.test.common.ClientServerTestCase;
import org.jacorb.test.orb.rmi.Boo;
import org.jacorb.test.orb.rmi.Foo;
import org.jacorb.test.orb.rmi.NegativeArgumentException;
import org.jacorb.test.orb.rmi.Outer.StaticInner;

public class RMITest extends ClientServerTestCase
{
    private RMITestInterface server;

    public RMITest(String name, ClientServerSetup setup)
    {
        super(name, setup);
    }

    public void setUp() throws Exception
    {
        server = (RMITestInterface)javax.rmi.PortableRemoteObject.narrow(
                                                    setup.getServerObject(),
                                                    RMITestInterface.class);
    }

    public static Test suite()
    {
        TestSuite suite = new TestSuite( "RMI/IIOP tests" );

        Properties client_props = new Properties();
        client_props.setProperty("jacorb.interop.strict_check_on_tc_creation", "off");
        client_props.setProperty("jacorb.interop.chunk_custom_rmi_valuetypes", "off");

        Properties server_props = new Properties();

        server_props.setProperty("org.omg.CORBA.ORBClass", "org.jacorb.orb.ORB");
        server_props.setProperty("org.omg.CORBA.ORBSingletonClass", "org.jacorb.orb.ORBSingleton");

        client_props.setProperty("org.omg.CORBA.ORBClass", "org.jacorb.orb.ORB");
        client_props.setProperty("org.omg.CORBA.ORBSingletonClass", "org.jacorb.orb.ORBSingleton");


        server_props.setProperty("jacorb.interop.strict_check_on_tc_creation", "off");
        server_props.setProperty("jacorb.interop.chunk_custom_rmi_valuetypes", "off");

        ClientServerSetup setup =
            new ClientServerSetup( suite,
                                   "org.jacorb.test.orb.rmi.RMITestServant",
                                   client_props,
                                   server_props);

        suite.addTest( new RMITest( "test_getString", setup ));
        suite.addTest( new RMITest( "test_primitiveTypes", setup ));
        suite.addTest( new RMITest( "test_String", setup ));
        suite.addTest( new RMITest( "test_RMITestInterface", setup ));
        suite.addTest( new RMITest( "test_Remote", setup ));
        suite.addTest( new RMITest( "test_Serializable", setup ));
        suite.addTest( new RMITest( "test_intArray", setup ));
        suite.addTest( new RMITest( "test_valueArray", setup ));
        suite.addTest( new RMITest( "test_exception", setup ));
        suite.addTest( new RMITest( "test_FooValueToObject", setup ));
        suite.addTest( new RMITest( "test_BooValueToObject", setup ));
        suite.addTest( new RMITest( "test_valueArrayToVector", setup ));
        suite.addTest( new RMITest( "test_vectorToValueArray", setup ));
        suite.addTest( new RMITest( "test_getException", setup ));
        suite.addTest( new RMITest( "test_getZooValue", setup ));
        suite.addTest( new RMITest( "test_referenceSharingWithinArray",
                                     setup ));
        suite.addTest( new RMITest( "test_referenceSharingWithinCollection",
                                     setup ));
        suite.addTest( new RMITest( "test_getVectorWithObjectArrayAsElement",
                                     setup ));
        suite.addTest( new RMITest( "test_getVectorWithVectorAsElement",
                                     setup ));
        suite.addTest( new RMITest( "test_getVectorWithHashtableAsElement",
                                     setup ));

        suite.addTest( new RMITest("testPassStaticInnerClass", setup));
        suite.addTest( new RMITest("testPassInnerClass", setup));
        suite.addTest( new RMITest("testPassCollection", setup));

        return setup;
    }

    public void test_getString()
    {
        try
        {
            //System.out.println("getString" + " ---------------------------");
            String s = server.getString();
            //System.out.println(s);
            assertEquals(RMITestUtil.STRING, s);
        }
        catch (java.rmi.RemoteException re)
        {
            throw new RuntimeException(re.toString());
        }
    }

    public void test_primitiveTypes()
    {
        try
        {
            String s;
            //System.out.println("testPrimitiveTypes" + " ------------------");
            s = server.testPrimitiveTypes(false,
                                          'A',
                                          Byte.MIN_VALUE,
                                          Short.MIN_VALUE,
                                          Integer.MIN_VALUE,
                                          Long.MIN_VALUE,
                                          Float.MIN_VALUE,
                                          Double.MIN_VALUE);
            //System.out.println(s);
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
            //System.out.println(s);
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
        catch (java.rmi.RemoteException re)
        {
            throw new RuntimeException(re.toString());
        }
    }

    public void test_String()
    {
        try
        {
            //System.out.println("testString" + " --------------------------");
            String original = "0123456789";
            String echoedBack = server.testString("0123456789");
            //System.out.println(echoedBack);
            assertEquals(RMITestUtil.echo(original), echoedBack);
        }
        catch (java.rmi.RemoteException re)
        {
            throw new RuntimeException(re.toString());
        }
    }

    public void test_RMITestInterface()
    {
        try
        {
            //System.out.println("testRMITestInterface" + " ----------------");
            RMITestInterface t =
                server.testRMITestInterface("the quick brown fox", server);
            String s = t.getString();
            //System.out.println(s);
            assertEquals(RMITestUtil.STRING, s);
        }
        catch (java.rmi.RemoteException re)
        {
            throw new RuntimeException(re.toString());
        }
    }

    public void test_Remote()
    {
        try
        {
            //System.out.println("testRemote" + " --------------------------");
            Remote r = server.testRemote("jumps over the lazy dog", server);
            RMITestInterface t =
                (RMITestInterface)PortableRemoteObject.narrow(r,
                                                       RMITestInterface.class);
            String s = t.getString();
            //System.out.println(s);
            assertEquals(RMITestUtil.STRING, s);
        }
        catch (java.rmi.RemoteException re)
        {
            throw new RuntimeException(re.toString());
        }
    }

    public void test_Serializable()
    {
        try
        {
            //System.out.println("testSerializable" + " --------------------");
            Foo original = new Foo(7, "foo test");
            Foo echoedBack = server.testSerializable(original);
            //System.out.println(echoedBack.toString());
            assertEquals(RMITestUtil.echoFoo(original), echoedBack);
        }
        catch (java.rmi.RemoteException re)
        {
            throw new RuntimeException(re.toString());
        }
    }

    public void test_intArray()
    {
        try
        {
            //System.out.println("testIntArray" + " ------------------------");
            int[] original= new int[10];
            for (int i = 0; i < original.length; i++)
            {
                original[i] = 100 + i;
            }
            int[] echoedBack = server.testIntArray(original);
            assertEquals(original.length, echoedBack.length);
            for (int i = 0; i < echoedBack.length; i++)
            {
                //System.out.print(""+ a[i] + " ");
                assertEquals(original[i] + 1, echoedBack[i]);
            }
            //System.out.println();
        }
        catch (java.rmi.RemoteException re)
        {
            throw new RuntimeException(re.toString());
        }
    }

    public void test_valueArray()
    {
        try
        {
            //System.out.println("testValueArray" + " ----------------------");
            Foo[] original = new Foo[4];
            for (int i = 0; i < original.length; i++)
            {
                original[i] = new Foo(100 + i, "foo array test");
            }
            Foo[] echoedBack = server.testValueArray(original);
            assertEquals(original.length, echoedBack.length);
            for (int i = 0; i < echoedBack.length; i++)
            {
                //System.out.println(echoedBack[i].toString());
                assertEquals(RMITestUtil.echoFoo(original[i]), echoedBack[i]);
            }
        }
        catch (java.rmi.RemoteException re)
        {
            throw new RuntimeException(re.toString());
        }
    }

    public void test_exception()
    {
        try
        {
            //System.out.println("testException" + " -----------------------");

            //System.out.println(server.testException(0));
            assertEquals("#0", server.testException(0));
            //System.out.println(server.testException(1));
            assertEquals("#1", server.testException(1));
            //System.out.println(server.testException(2));
            assertEquals("#2", server.testException(2));
            try
            {
                server.testException(-2);
                fail("NegativeArgumentException expected but not thrown.");
            }
            catch (NegativeArgumentException na)
            {
                //System.out.println("Expected exception:\n" + na.toString());
                assertEquals(-2, na.getNegativeArgument());
            }
            try
            {
                server.testException(-1);
                fail("NegativeArgumentException expected but not thrown.");
            }
            catch (NegativeArgumentException na)
            {
                //System.out.println("Expected exception:\n" + na.toString());
                assertEquals(-1, na.getNegativeArgument());
            }
            //System.out.println(server.testException(0));
            assertEquals("#0", server.testException(0));
        }
        catch (NegativeArgumentException na)
        {
            throw new RuntimeException(na.toString());
        }
        catch (java.rmi.RemoteException re)
        {
            throw new RuntimeException(re.toString());
        }
    }

    public void test_FooValueToObject()
    {
        try
        {
            //System.out.println("fooValueToObject" + " --------------------");
            Foo original = new Foo(9999, "foo test");
            java.lang.Object echoedBack = server.fooValueToObject(original);
            //System.out.println(echoedBack.toString());
            assertEquals(RMITestUtil.echoFoo(original), echoedBack);
        }
        catch (java.rmi.RemoteException re)
        {
            throw new RuntimeException(re.toString());
        }
    }

    public void test_BooValueToObject()
    {
        try
        {
            //System.out.println("booValueToObject" + " --------------------");
            Boo original = new Boo("t1", "boo test");
            java.lang.Object echoedBack = server.booValueToObject(original);
            //System.out.println(echoedBack.toString());
            assertEquals(RMITestUtil.echoBoo(original), echoedBack);
        }
        catch (java.rmi.RemoteException re)
        {
            throw new RuntimeException(re.toString());
        }
    }

    public void test_valueArrayToVector()
    {
        try
        {
            //System.out.println("valueArrayToVector" + " ------------------");
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
                //System.out.println(echoedBack[i].toString());
                assertEquals(RMITestUtil.echoFoo(original[i]), echoedBack[i]);
            }
        }
        catch (java.rmi.RemoteException re)
        {
            throw new RuntimeException(re.toString());
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

    public void test_getException()
    {
        try
        {
            //System.out.println("getException" + " ------------------------");
            java.lang.Object obj = server.getException();
            //System.out.println(obj.toString());
            NegativeArgumentException na = (NegativeArgumentException)obj;
            assertEquals(-7777, na.getNegativeArgument());
        }
        catch (java.rmi.RemoteException re)
        {
            throw new RuntimeException(re.toString());
        }
    }

    public void test_getZooValue()
    {
        try
        {
            //System.out.println("getZooValue" + " -------------------------");
            java.lang.Object obj = server.getZooValue();
            //System.out.println(obj.toString());
            Assert.assertEquals(new Zoo("outer_zoo!",
                                 "returned by getZooValue",
                                 new Zoo("inner_zoo!", "inner")),
                         obj);
        }
        catch (java.rmi.RemoteException re)
        {
            throw new RuntimeException(re.toString());
        }
    }

    public void test_referenceSharingWithinArray()
    {
        try
        {
            //System.out.println("testRefSharingWithinArray" + " -----------");
            int n = 100;
            Object[] original = new Object[n];
            for (int i = 0; i < n; i++)
            {
                //original[i] = new Foo(100 + i, "foo array test");
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
        catch (java.rmi.RemoteException re)
        {
            throw new RuntimeException(re.toString());
        }
    }

    public void test_referenceSharingWithinCollection()
    {
        try
        {
            //System.out.println("testRefSharingWithinCollection" + " ------");
            java.util.Collection original = new java.util.ArrayList();
            int n = 10;
            for (int i = 0; i < n; i++)
            {
                original.add(new Foo(100 + i, "foo collection test"));
                //original.add(new Boo("t" + i, "boo collection test"));
            }
            java.util.Collection echoedBack =
                    server.testReferenceSharingWithinCollection(original);
            assertEquals(2 * n, echoedBack.size());

            java.util.ArrayList originalList = (java.util.ArrayList)original;
            java.util.ArrayList echoedList = (java.util.ArrayList)echoedBack;


            for (int i = 0; i < n; i++)
            {
                //System.out.println(echoedList.get(i).toString());
                assertEquals(originalList.get(i), echoedList.get(i));
                assertEquals(originalList.get(i), echoedList.get(i + n));
                assertSame(echoedList.get(i), echoedList.get(i + n));
            }
        }
        catch (java.rmi.RemoteException re)
        {
            throw new RuntimeException(re.toString());
        }
    }

    public void test_getVectorWithObjectArrayAsElement()
    {
        try
        {
            //System.out.println("getVectorWithObjectArrayAsElement" + " ---");
            java.util.Vector vector =
                server.getVectorWithObjectArrayAsElement();
            //System.out.println(vector.toString());
            assertTrue(vector.size() == 1);
            Object[] inner = (Object[]) vector.get(0);
            assertEquals(new Integer(1), inner[0]);
            assertEquals(new Integer(2), inner[1]);
            assertEquals("Third Element", inner[2]);
        }
        catch (java.rmi.RemoteException re)
        {
            throw new RuntimeException(re.toString());
        }
    }

    public void test_getVectorWithVectorAsElement()
    {
        try
        {
            //System.out.println("getVectorWithVectorAsElement" + " --------");
            java.util.Vector vector =
                server.getVectorWithVectorAsElement();
            //System.out.println(vector.toString());
            assertTrue(vector.size() == 1);
            java.util.Vector inner = (java.util.Vector) vector.get(0);
            assertEquals(new Integer(1), inner.get(0));
            assertEquals(new Integer(2), inner.get(1));
            assertEquals("Third Element", inner.get(2));
        }
        catch (java.rmi.RemoteException re)
        {
            throw new RuntimeException(re.toString());
        }
    }

    public void test_getVectorWithHashtableAsElement()
    {
        try
        {
            //System.out.println("getVectorWithHashtableAsElement" + " -----");
            java.util.Vector vector =
                server.getVectorWithHashtableAsElement();
            //System.out.println(vector.toString());
            assertTrue(vector.size() == 1);
            java.util.Hashtable inner = (java.util.Hashtable) vector.get(0);
            assertEquals(new Integer(1), inner.get(new Integer(0)));
            assertEquals(new Integer(2), inner.get(new Integer(1)));
            assertEquals("Third Element", inner.get(new Integer(2)));
        }
        catch (java.rmi.RemoteException re)
        {
            throw new RuntimeException(re.toString());
        }
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
}
