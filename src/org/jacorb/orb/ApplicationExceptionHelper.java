/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2002 Gerald Brose
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

import org.omg.CORBA.*;
import org.omg.CORBA.portable.*;
import java.lang.reflect.*;
import org.jacorb.ir.RepositoryID;

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
     * This method tries to insert the given ApplicationException into the
     * given any by deriving the helper name from object id. <br>
     * All exceptions are propagated upward to be handled there.
     */

    public static void insert (org.omg.CORBA.Any any, ApplicationException  s)
        throws ClassNotFoundException, 
        NoSuchMethodException, 
        IllegalAccessException,
        InvocationTargetException
    { 
        String name   = RepositoryID.className (s.getId(), "Helper");

        // if the class is not found, let exception propagate up
        Class _helper = Class.forName (name);

        //_helper must not be null from here on
        
        //get read method from helper and invoke it,
        //i.e. read the object from the stream
        Method _read = 
            _helper.getMethod( "read", 
                               new Class[]{ 
                                   Class.forName("org.omg.CORBA.portable.InputStream")
                               }
                               );    
        java.lang.Object _user_ex = 
            _read.invoke(null, new java.lang.Object[]{ s.getInputStream() } );
    
        //get insert method and insert exception into any
        Method _insert = 
            _helper.getMethod("insert", 
                              new Class[]{ Class.forName("org.omg.CORBA.Any"), 
                                           Class.forName( name ) }
                              ); 
        _insert.invoke( null, new java.lang.Object[]{any, _user_ex} );
    }
} // ApplicationExceptionHelper
