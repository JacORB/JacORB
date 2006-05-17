package org.jacorb.orb.dynany;

/*
 *        JacORB  - a free Java ORB
 *
 *   Copyright (C) 1997-2004  Gerald Brose.
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

import org.omg.DynamicAny.DynAnyPackage.*;
import org.omg.DynamicAny.*;

/**
 * CORBA DynValue
 *
 * @author Nick Cross
 * @version $Id$
 */

public final class DynValue
    extends DynAny
    implements org.omg.DynamicAny.DynValue
{
    private static final String DYN_VALUE_NYI =
        ("DynValue is not yet implemented in Jacorb");

    private DynValue() {
        super(null, null, null);
    }

    public java.lang.String current_member_name()
        throws TypeMismatch, InvalidValue
    {
        throw new org.omg.CORBA.NO_IMPLEMENT
            (DYN_VALUE_NYI);
    }

    public org.omg.CORBA.TCKind current_member_kind()
        throws TypeMismatch, InvalidValue
    {
        throw new org.omg.CORBA.NO_IMPLEMENT
            (DYN_VALUE_NYI);
    }

    public NameValuePair[] get_members()
    {
        throw new org.omg.CORBA.NO_IMPLEMENT
            (DYN_VALUE_NYI);
    }

    public void set_members( NameValuePair[] nvp )
        throws InvalidValue, TypeMismatch
    {
        throw new org.omg.CORBA.NO_IMPLEMENT
            (DYN_VALUE_NYI);
    }

    public org.omg.DynamicAny.NameDynAnyPair[] get_members_as_dyn_any()
    {
        throw new org.omg.CORBA.NO_IMPLEMENT
            (DYN_VALUE_NYI);
    }

    public void set_members_as_dyn_any(org.omg.DynamicAny.NameDynAnyPair[] nvp)
        throws TypeMismatch, InvalidValue
    {
        throw new org.omg.CORBA.NO_IMPLEMENT
            (DYN_VALUE_NYI);
    }
}
