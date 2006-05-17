package org.jacorb.ir;

import org.omg.CORBA.INTERNAL;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2004 Gerald Brose.
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

public class FixedDef
    extends IDLType
    implements org.omg.CORBA.FixedDefOperations
{
    private short digits;
    private short scale;

    public FixedDef(org.omg.CORBA.TypeCode tc)
    {
        def_kind = org.omg.CORBA.DefinitionKind.dk_Fixed;
        type = tc;
        try
        {
            digits = tc.fixed_digits();
            scale = tc.fixed_scale();
        }
        catch( Exception e )
        {
            throw new INTERNAL(e.getMessage());
        }
    }

    public short digits()
    {
        return digits;
    }

    public void digits(short arg)
    {
        digits = arg;
    }

    public short scale()
    {
        return scale;
    }

    public void scale(short arg)
    {
        scale = arg;
    }

    public void define()
    {
    }

    public void destroy()
    {
    }
}








