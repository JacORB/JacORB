package org.jacorb.test.orb.rmi;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Properties;

import org.jacorb.test.orb.rmi.Boo;
import org.jacorb.test.orb.rmi.Foo;
import org.jacorb.test.orb.rmi.NegativeArgumentException;

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

public interface RMITestInterface
        extends java.rmi.Remote
{

    public String getString()
            throws java.rmi.RemoteException;

    public String testPrimitiveTypes(boolean flag, char c, byte b,
                                     short s, int i, long l, float f, double d)
            throws java.rmi.RemoteException;

    public String testString(String s)
            throws java.rmi.RemoteException;

    public RMITestInterface testRMITestInterface(String s, RMITestInterface t)
            throws java.rmi.RemoteException;

    public java.rmi.Remote testRemote(String s, java.rmi.Remote t)
            throws java.rmi.RemoteException;

    public Foo testSerializable(Foo foo)
            throws java.rmi.RemoteException;

    public int[] testIntArray(int[] a)
            throws java.rmi.RemoteException;

    public Foo[] testValueArray(Foo[] a)
            throws java.rmi.RemoteException;

    public String testException(int i)
            throws NegativeArgumentException, java.rmi.RemoteException;

    public Object fooValueToObject(Foo foo)
            throws java.rmi.RemoteException;

    public Object booValueToObject(Boo boo)
            throws java.rmi.RemoteException;

    public java.util.Vector valueArrayToVector(Foo[] a)
            throws java.rmi.RemoteException;

    public Foo[] vectorToValueArray(java.util.Vector v)
            throws java.rmi.RemoteException;

    public Object getException()
            throws java.rmi.RemoteException;

    public Object getZooValue()
            throws java.rmi.RemoteException;

    public Object[] testReferenceSharingWithinArray(Object[] a)
            throws java.rmi.RemoteException;

    public java.util.Collection testReferenceSharingWithinCollection(
            java.util.Collection c) throws java.rmi.RemoteException;

    public java.util.Vector getVectorWithObjectArrayAsElement()
            throws java.rmi.RemoteException;

    public java.util.Vector getVectorWithVectorAsElement()
            throws java.rmi.RemoteException;

    public java.util.Vector getVectorWithHashtableAsElement()
            throws java.rmi.RemoteException;

    public Outer outerToOuter(Outer outer) throws java.rmi.RemoteException;

    public Outer.StaticInner staticInnerToStaticInner(Outer.StaticInner staticInner)
        throws java.rmi.RemoteException;

    public int sizeOfCollection(Collection c) throws java.rmi.RemoteException;

    Serializable transmitSerializable(Serializable s) throws RemoteException;
    
    Properties transmitProperties(Properties p) throws RemoteException;
}
