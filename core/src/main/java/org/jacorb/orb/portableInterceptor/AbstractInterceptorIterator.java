/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2012 Gerald Brose / The JacORB Team.
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

package org.jacorb.orb.portableInterceptor;

import org.omg.CORBA.UserException;
import org.omg.PortableInterceptor.Interceptor;

/**
 * @author Gerald Brose
 */

public abstract class AbstractInterceptorIterator
{
    protected final Interceptor[] interceptors;
    protected int index = 0;
    protected int increment = 1;

    public AbstractInterceptorIterator(Interceptor[] interceptors)
    {
        this.interceptors = interceptors;
    }

    /**
     * Tests, if there are more elements available.
     */
    protected boolean hasMoreElements()
    {
        return ( index >= 0 ) && ( index < interceptors.length );
    }

    /**
     * Returns the next element in the enumeration
     */

    protected Interceptor nextElement()
    {
        Interceptor result = interceptors[index];
        index += increment;
        return result;
    }

    protected void iterate()
        throws UserException
    {
        while( hasMoreElements() )
        {
            invoke( nextElement() );
        }
    }

    abstract protected void invoke(Interceptor interceptor)
        throws UserException;
}
