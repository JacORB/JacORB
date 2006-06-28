/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2004 Gerald Brose
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

package org.jacorb.orb;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.jacorb.ir.RepositoryID;
import org.jacorb.util.ObjectUtil;
import org.omg.CORBA.portable.ApplicationException;

/**
 * This class provides a method for inserting an arbirtary
 * application exception into an any.
 *
 * @author Nicolas Noffke
 * @version $Id$
 */
public class ApplicationExceptionHelper
{
    /**
     * Private constructor to prevent instantiation
     */
    private ApplicationExceptionHelper()
    {
        // utility class
    }

    /**
     * This method tries to insert the given ApplicationException into the
     * given any by deriving the helper name from object id. <br>
     * All exceptions are propagated upward to be handled there.
     */

    public static void insert(org.omg.CORBA.Any any, ApplicationException  exception)
        throws
            ClassNotFoundException,
            NoSuchMethodException,
            IllegalAccessException,
            InvocationTargetException
    {
        java.lang.Object userEx;

        // Get exception and helper names

        String name = RepositoryID.className(exception.getId(), null);
        String helperName = name + "Helper";

        // Get various required classes

        Class exClass = ObjectUtil.classForName(name);
        Class helperClass = ObjectUtil.classForName(helperName);
        Class anyClass = org.omg.CORBA.Any.class;
        Class isClass = org.omg.CORBA.portable.InputStream.class;

        // Get various required methods

        Method readMeth  =
            helperClass.getMethod("read", new Class[] { isClass });
        Method insertMeth =
            helperClass.getMethod("insert", new Class[] { anyClass, exClass });

        // Do equivalent of:
        //
        // userEx = UserExHelper.read (s.getInputStream ());
        // UserExHelper.insert (any, userEx);
        //

        userEx =
            readMeth.invoke(null, new java.lang.Object[] { exception.getInputStream () });
        insertMeth.invoke(null, new java.lang.Object[] {any, userEx});
    }
}
