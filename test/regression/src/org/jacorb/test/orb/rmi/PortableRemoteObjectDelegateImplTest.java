/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2006 Gerald Brose
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
 *
 */

package org.jacorb.test.orb.rmi;

import javax.rmi.PortableRemoteObject;
import javax.rmi.CORBA.Util;

import junit.framework.TestCase;

import org.jacorb.orb.rmi.PortableRemoteObjectDelegateImpl;
import org.omg.CORBA.ORB;
import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;
import org.omg.PortableServer.POAManagerPackage.AdapterInactive;

public class PortableRemoteObjectDelegateImplTest extends TestCase
{
    private static ORB orb;
    private static Exception initException;

    static
    {
        orb = ORB.init(new String[0], null);
        System.setProperty("javax.rmi.CORBA.PortableRemoteObjectClass", PortableRemoteObjectDelegateImpl.class.getName());
        PortableRemoteObjectDelegateImpl.setORB(orb);

        try
        {
            POA poa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
            poa.the_POAManager().activate();
        }
        catch (InvalidName e)
        {
            initException = e;
        }
        catch (AdapterInactive e)
        {
            initException = e;
        }
    }

    protected void setUp() throws Exception
    {
        assertNull(initException);
    }

    public void testExport() throws Exception
    {
        RMITestImpl servant = new RMITestImpl();

        assertNotNull(Util.getTie(servant));
    }

    public void testUnExport() throws Exception
    {
        RMITestImpl servant = new RMITestImpl();

        assertNotNull(Util.getTie(servant));

        PortableRemoteObject.unexportObject(servant);

        assertNull(Util.getTie(servant));
    }

    public void testToStub() throws Exception
    {
        RMITestImpl servant = new RMITestImpl();

        RMITestInterface remote = (RMITestInterface) PortableRemoteObject.toStub(servant);

        String string = remote.testString("hello");

        assertEquals("hello (echoed back)", string);
    }
}
