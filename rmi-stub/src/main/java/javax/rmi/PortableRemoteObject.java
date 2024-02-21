/*
 *        JacORB - a free Java ORB
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

package javax.rmi;

import java.rmi.NoSuchObjectException;
import java.rmi.Remote;
import java.rmi.RemoteException;

import org.omg.CORBA.INITIALIZE;

/**
 * Mininal RMI stub for JacORB
 */
public class PortableRemoteObject
{
    /**
     * Mininal RMI stub for JacORB
     */
    protected PortableRemoteObject() throws RemoteException
    {
    }

    /**
     * Mininal RMI stub for JacORB
     */
    public static void exportObject(Remote obj) throws RemoteException
    {
    }

    /**
     * Mininal RMI stub for JacORB
     */
    public static Remote toStub(Remote obj) throws NoSuchObjectException
    {
        return null;
    }

    /**
     * Mininal RMI stub for JacORB
     */
    public static void unexportObject(Remote obj) throws NoSuchObjectException
    {
    }

    /**
     * Mininal RMI stub for JacORB
     */
    public static java.lang.Object narrow(java.lang.Object narrowFrom, java.lang.Class<?> narrowTo)
            throws ClassCastException
    {
        return null;
    }

    /**
     * Mininal RMI stub for JacORB
     */
    public static void connect(Remote target, Remote source) throws RemoteException
    {
    }
}
