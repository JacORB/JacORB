package org.jacorb.orb.portableInterceptor;

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

import org.omg.PortableInterceptor.*;
import org.omg.CORBA.UserException;
import org.jacorb.orb.standardInterceptors.CodeSetInfoInterceptor;

/**
 * IORInterceptorIterator.java
 *
 * Created: Mon Apr 17 09:53:33 2000
 *
 * @author Nicolas Noffke
 * $Id$
 */

public class IORInterceptorIterator
    extends AbstractInterceptorIterator
{
    private IORInfoImpl info = null;
    private int[] profile_tags = null;

    public IORInterceptorIterator(Interceptor[] interceptors)
    {
        super(interceptors);
    }

    public IORInterceptorIterator(Interceptor[] interceptors,
                                  int[] ptags )
    {
        super(interceptors);
        profile_tags = ptags;
    }

    public void iterate(IORInfoImpl info)
        throws UserException{

        this.info = info;

        iterate();
    }

    protected void invoke(Interceptor interceptor)
        throws UserException
    {
        try
        {
            if (interceptor instanceof CodeSetInfoInterceptor)
                ((CodeSetInfoInterceptor) interceptor).
                    establish_components(info, profile_tags);
            else
                ((IORInterceptor) interceptor).establish_components(info);
        }
        catch(Exception e)
        {
        }
    }
} // IORInterceptorIterator
