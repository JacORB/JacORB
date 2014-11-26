package org.jacorb.ir;

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

import org.omg.CORBA.INTERNAL;
import org.omg.CORBA.INTF_REPOS;

public class WstringDef
    extends StringDef
    implements org.omg.CORBA.WstringDefOperations
{
    public WstringDef(org.omg.CORBA.TypeCode tc)
    {
        if (tc.kind () != org.omg.CORBA.TCKind.tk_wstring)
        {
            throw new INTF_REPOS ("Precondition volation: TypeCode must be of kind wstring, but is " +
                                        tc.kind().value());
        }
        def_kind = org.omg.CORBA.DefinitionKind.dk_Wstring;
        type = tc;
        try
        {
            bound( tc.length());
        }
        catch( Exception e )
        {
            throw new INTERNAL(e.toString());
        }
    }

}
