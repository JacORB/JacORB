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

package org.jacorb.ir;

import org.omg.CORBA.INTF_REPOS;
import org.omg.CORBA.PrimitiveKind;
import org.omg.CORBA.TCKind;

public final class PrimitiveDef
    extends IRObject
    implements org.omg.CORBA.PrimitiveDefOperations
{
    protected org.omg.CORBA.PrimitiveKind kind;
    protected org.omg.CORBA.TypeCode type;

    public PrimitiveDef( org.omg.CORBA.TypeCode tc )
    {
        type = tc;
        def_kind = org.omg.CORBA.DefinitionKind.dk_Primitive;
        int _kind = tc.kind().value();
        switch ( _kind )
        {
        case TCKind._tk_null:
            {
                kind = PrimitiveKind.pk_null;
                break;
            }
        case TCKind._tk_void:
            {
                kind = PrimitiveKind.pk_void;
                break;
            }
        case TCKind._tk_short:
            {
                kind = PrimitiveKind.pk_short;
                break;
            }
        case TCKind._tk_long:
            {
                kind = PrimitiveKind.pk_long;
                break;
            }
        case TCKind._tk_ushort:
            {
                kind = PrimitiveKind.pk_ushort;
                break;
            }
        case TCKind._tk_ulong:
            {
                kind = PrimitiveKind.pk_ulong;
                break;
            }
        case TCKind._tk_float:
            {
                kind = PrimitiveKind.pk_float;
                break;
            }
        case TCKind._tk_double:
            {
                kind = PrimitiveKind.pk_double;
                break;
            }
        case TCKind._tk_boolean:
            {
                kind = PrimitiveKind.pk_boolean;
                break;
            }
        case TCKind._tk_char:
            {
                kind = PrimitiveKind.pk_char;
                break;
            }
        case TCKind._tk_longlong:
            {
                kind = PrimitiveKind.pk_longlong;
                break;
            }
        case TCKind._tk_ulonglong:
            {
                kind = PrimitiveKind.pk_ulonglong;
                break;
            }
        case TCKind._tk_longdouble:
            {
                kind = PrimitiveKind.pk_longdouble;
                break;
            }
        case TCKind._tk_wchar:
            {
                kind = PrimitiveKind.pk_wchar;
                break;
            }
        case TCKind._tk_wstring:
            {
                kind = PrimitiveKind.pk_wstring;
                break;
            }
        case TCKind._tk_octet:
            {
                kind = PrimitiveKind.pk_octet;
                break;
            }
        case TCKind._tk_any:
            {
                kind = PrimitiveKind.pk_any;
                break;
            }
        case TCKind._tk_TypeCode:
            {
                kind = PrimitiveKind.pk_TypeCode;
                break;
            }
        case TCKind._tk_Principal:
            {
                kind = PrimitiveKind.pk_Principal;
                break;
            }
        case TCKind._tk_string:
            {
                kind = PrimitiveKind.pk_string;
                break;
            }
        case TCKind._tk_objref:
            {
                kind = PrimitiveKind.pk_objref;
                break;
            }
        default:
            {
                throw new INTF_REPOS ("Unrecognized kind: " + kind );
            }
        }
    }

    public PrimitiveDef( int _kind )
    {
        def_kind = org.omg.CORBA.DefinitionKind.dk_Primitive;
        kind =  PrimitiveKind.from_int(_kind);
        switch ( _kind )
        {
        case org.omg.CORBA.PrimitiveKind._pk_null:
            {
                type = org.omg.CORBA.ORB.init().get_primitive_tc( org.omg.CORBA.TCKind.tk_null );
                break;
            }
        case org.omg.CORBA.PrimitiveKind._pk_void:
            {
                type = org.omg.CORBA.ORB.init().get_primitive_tc( org.omg.CORBA.TCKind.tk_void );
                break;
            }
        case org.omg.CORBA.PrimitiveKind._pk_short:
            {
                type = org.omg.CORBA.ORB.init().get_primitive_tc( org.omg.CORBA.TCKind.tk_short );
                break;
            }
        case org.omg.CORBA.PrimitiveKind._pk_long:
            {
                type = org.omg.CORBA.ORB.init().get_primitive_tc( org.omg.CORBA.TCKind.tk_long );
                break;
            }
        case org.omg.CORBA.PrimitiveKind._pk_float:
            {
                type = org.omg.CORBA.ORB.init().get_primitive_tc( org.omg.CORBA.TCKind.tk_float );
                break;
            }
        case org.omg.CORBA.PrimitiveKind._pk_double:
            {
                type = org.omg.CORBA.ORB.init().get_primitive_tc( org.omg.CORBA.TCKind.tk_double );
                break;
            }
        case org.omg.CORBA.PrimitiveKind._pk_boolean:
            {
                type = org.omg.CORBA.ORB.init().get_primitive_tc( org.omg.CORBA.TCKind.tk_boolean );
                break;
            }
        case org.omg.CORBA.PrimitiveKind._pk_char:
            {
                type = org.omg.CORBA.ORB.init().get_primitive_tc( org.omg.CORBA.TCKind.tk_char );
                break;
            }
        case org.omg.CORBA.PrimitiveKind._pk_octet:
            {
                type = org.omg.CORBA.ORB.init().get_primitive_tc( org.omg.CORBA.TCKind.tk_octet );
                break;
            }
        case org.omg.CORBA.PrimitiveKind._pk_any:
            {
                type = org.omg.CORBA.ORB.init().get_primitive_tc( org.omg.CORBA.TCKind.tk_any );
                break;
            }
        case org.omg.CORBA.PrimitiveKind._pk_TypeCode:
            {
                type = org.omg.CORBA.ORB.init().get_primitive_tc( org.omg.CORBA.TCKind.tk_TypeCode );
                break;
            }
        case org.omg.CORBA.PrimitiveKind._pk_Principal:
            {
                type = org.omg.CORBA.ORB.init().get_primitive_tc( org.omg.CORBA.TCKind.tk_Principal );
                break;
            }
        case org.omg.CORBA.PrimitiveKind._pk_string:
            {
                type = org.omg.CORBA.ORB.init().get_primitive_tc( org.omg.CORBA.TCKind.tk_string );
                break;
            }
        case org.omg.CORBA.PrimitiveKind._pk_objref:
            {
                type = org.omg.CORBA.ORB.init().get_primitive_tc( org.omg.CORBA.TCKind.tk_objref );
                break;
            }
        default:
            {
                throw new INTF_REPOS ("Unrecognized kind: " + kind );
            }
        }
    }

    public org.omg.CORBA.TypeCode type()
    {
        return type;
    }

    public org.omg.CORBA.PrimitiveKind kind()
    {
        return kind;
    }

    public void destroy()
    {
    }

    void define()
    {
        // do nothing
    }
}










