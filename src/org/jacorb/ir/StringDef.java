package org.jacorb.ir;

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

import org.omg.CORBA.INTF_REPOS;

public class StringDef
    extends IDLType
    implements org.omg.CORBA.StringDefOperations
{
    private int bound;

    /** for subclasses only */
    protected StringDef(){}

    public StringDef(org.omg.CORBA.TypeCode tc)
    {
        if (tc.kind () != org.omg.CORBA.TCKind.tk_string)
        {
            throw new INTF_REPOS ("Precondition volation: TypeCode must be of kind string");
        }

        def_kind = org.omg.CORBA.DefinitionKind.dk_String;
        type = tc;
        try
        {
            bound( tc.length());
        }
        catch( Exception e )
        {
            e.printStackTrace(); // should not happen
        }
    }

    public int bound()
    {
        return bound;
    }

    public void bound(int arg)
    {
        bound = arg;
    }

    public void define()
    {
    }

    public void destroy()
    {
    }

}
