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

package javax.rmi.CORBA;

import java.net.MalformedURLException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.RMIClassLoader;
import java.util.Properties;

import javax.rmi.CORBA.impl.ValueHandlerImpl;

import org.omg.CORBA.INITIALIZE;
import org.omg.CORBA.ORB;
import org.omg.CORBA.SystemException;
import org.omg.CORBA.portable.InputStream;
import org.omg.CORBA.portable.OutputStream;

/**
 * Mininal RMI stub for JacORB
 */
public class Util
{
    private static ValueHandlerImpl valueHandlerSingleton = new ValueHandlerImpl();

    private Util()
    {
    }

    /**
     * Mininal RMI stub for JacORB
     */
    public static void registerTarget(javax.rmi.CORBA.Tie tie, java.rmi.Remote target)
    {
    }

    /**
     * Mininal RMI stub for JacORB
     */
    public static void unexportObject(java.rmi.Remote target) throws java.rmi.NoSuchObjectException
    {
    }

    /**
     * Mininal RMI stub for JacORB
     */
    public static Tie getTie(Remote target)
    {
        return null;
    }

    /**
     * Mininal RMI stub for JacORB
     */
    public static ValueHandler createValueHandler()
    {
        return valueHandlerSingleton;
    }

    /**
     * Mininal RMI stub for JacORB
     */
    public static String getCodebase(java.lang.Class<?> clz)
    {
        return null;
    }

    /**
     * Mininal RMI stub for JacORB
     */
    public static Class<?> loadClass(String className, String remoteCodebase, ClassLoader loader)
            throws ClassNotFoundException
    {
        return null;
    }
}
