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

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Properties;

import org.jacorb.test.orb.rmi.Boo;
import org.jacorb.test.orb.rmi.Foo;
import org.jacorb.test.orb.rmi.NegativeArgumentException;
import org.jacorb.test.orb.rmi.Outer.StaticInner;

import javax.rmi.PortableRemoteObject;

public class RMITestImpl
        extends PortableRemoteObject
        implements RMITestInterface
{

    public RMITestImpl()
        throws java.rmi.RemoteException
    {
        super();
    }

    public String getString()
        throws java.rmi.RemoteException
    {
            return RMITestUtil.STRING;
    }

    public String testPrimitiveTypes(boolean flag, char c, byte b,
                                     short s, int i, long l, float f, double d)
        throws java.rmi.RemoteException
    {
        return RMITestUtil.primitiveTypesToString(flag, c, b, s, i, l, f, d);
    }

    public String testString(String s)
        throws java.rmi.RemoteException
    {
        return RMITestUtil.echo(s);
    }

    public RMITestInterface testRMITestInterface(String s, RMITestInterface t)
        throws java.rmi.RemoteException
    {
        return t;
    }

    public java.rmi.Remote testRemote(String s, java.rmi.Remote t)
        throws java.rmi.RemoteException
    {
        return t;
    }

    public Foo testSerializable(Foo foo)
        throws java.rmi.RemoteException
    {
        return RMITestUtil.echoFoo(foo);
    }

    public int[] testIntArray(int[] a)
        throws java.rmi.RemoteException
    {
        for (int i = 0; i < a.length; i++)
        {
            a[i]++;
        }
        return a;
    }

    public Foo[] testValueArray(Foo[] a)
        throws java.rmi.RemoteException
    {
        for (int i = 0; i < a.length; i++)
        {
            a[i] = RMITestUtil.echoFoo(a[i]);
        }
        return a;
    }

    public String testException(int i)
        throws NegativeArgumentException, java.rmi.RemoteException
    {
        if (i >= 0)
            return "#" + i;
        else
            throw new NegativeArgumentException(i);
    }

    public Object fooValueToObject(Foo foo)
        throws java.rmi.RemoteException
    {
        return RMITestUtil.echoFoo(foo);
    }

    public Object booValueToObject(Boo boo)
        throws java.rmi.RemoteException
    {
        return RMITestUtil.echoBoo(boo);
    }

    public java.util.Vector valueArrayToVector(Foo[] a)
        throws java.rmi.RemoteException
    {
        java.util.Vector v = new java.util.Vector();

        for (int i = 0; i < a.length; i++)
        {
            v.add(RMITestUtil.echoFoo(a[i]));
        }
        return v;
    }

    public Foo[] vectorToValueArray(java.util.Vector v)
        throws java.rmi.RemoteException
    {
        Foo a[] = new Foo[v.size()];

        for (int i = 0; i < a.length; i++)
        {
            a[i] = RMITestUtil.echoFoo((Foo)v.elementAt(i));
        }
        return a;
    }

    public Object getException()
        throws java.rmi.RemoteException
    {
        Object obj = null;
        try
        {
            NegativeArgumentException e = new NegativeArgumentException(-7777);
            throw e;
        }
        catch (NegativeArgumentException e)
        {
            obj = e;
        }
        return obj;
    }

    public Object getZooValue()
        throws java.rmi.RemoteException
    {
        return new Zoo("outer_zoo",
                       "returned by getZooValue",
                       new Zoo("inner_zoo", "inner"));
    }

    public Object[] testReferenceSharingWithinArray(Object[] a)
        throws java.rmi.RemoteException
    {
        int n = a.length;
        Object[] b = new Object[2 * n];
        for (int i = 0; i < n; i++)
            b[i + n] = b[i] = a[i];
        return b;
    }

    public java.util.Collection testReferenceSharingWithinCollection(
            java.util.Collection cin) throws java.rmi.RemoteException
    {
        java.util.Collection cout = new java.util.ArrayList(cin);
        java.util.Iterator i = cin.iterator();
        while (i.hasNext())
        {
            cout.add(i.next());
        }
        return cout;
    }

    public java.util.Vector getVectorWithObjectArrayAsElement()
            throws java.rmi.RemoteException
    {
        java.util.Vector vector = new java.util.Vector();
        Object[] innerArray = new Object[3];
        innerArray[0] = new Integer(1);
        innerArray[1] = new Integer(2);
        innerArray[2] = "Third Element";
        vector.add(innerArray);
        return vector;
    }

    public java.util.Vector getVectorWithVectorAsElement()
            throws java.rmi.RemoteException
    {
        java.util.Vector vector = new java.util.Vector();
        java.util.Vector innerVector = new java.util.Vector();
        innerVector.add(new Integer(1));
        innerVector.add(new Integer(2));
        innerVector.add("Third Element");
        vector.add(innerVector);
        return vector;
    }

    public java.util.Vector getVectorWithHashtableAsElement()
            throws java.rmi.RemoteException
    {
        java.util.Vector vector = new java.util.Vector();
        java.util.Hashtable innerHash = new java.util.Hashtable();
        innerHash.put(new Integer(0), new Integer(1));
        innerHash.put(new Integer(1), new Integer(2));
        innerHash.put(new Integer(2), "Third Element");
        vector.add(innerHash);
        return vector;
    }

    public Outer outerToOuter(Outer outer) throws RemoteException
    {
       return outer;
    }

    public StaticInner staticInnerToStaticInner(StaticInner staticInner) throws RemoteException
    {
        return staticInner;
    }

    public int sizeOfCollection(Collection c) throws RemoteException
    {
        return c.size();
    }

    public Serializable transmitSerializable(Serializable s) throws RemoteException
    {
        return s;
    }

	public Properties transmitProperties(Properties p) throws RemoteException {
		return p;
	}
}
