package org.jacorb.orb.dynany;

/*
 *        JacORB  - a free Java ORB
 *
 *   Copyright (C) 1997-2001  Gerald Brose.
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

import java.util.*;
import org.omg.DynamicAny.*;
import org.omg.CORBA.TCKind;
import org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCode;

/**
 * @author Gerald Brose, FU Berlin
 * @version $Id$
 * 
 */

public class DynAnyFactoryImpl
    extends org.jacorb.orb.LocalityConstrainedObject
    implements org.omg.DynamicAny.DynAnyFactory
{
    org.omg.CORBA.ORB orb;

    public DynAnyFactoryImpl( org.omg.CORBA.ORB orb )
    {
        this.orb = orb;
        //	_this_object( orb );
    }

    public org.omg.DynamicAny.DynAny create_dyn_any( org.omg.CORBA.Any value )
	throws InconsistentTypeCode
    {
	try
	{
            org.omg.DynamicAny.DynAny dynAny =
                create_dyn_any_from_type_code( value.type() );
            dynAny.from_any( value );
            return dynAny; 
	}
	catch( org.omg.DynamicAny.DynAnyPackage.InvalidValue iv )
	{
	    iv.printStackTrace();
	}
	catch( org.omg.DynamicAny.DynAnyPackage.TypeMismatch itc )
	{
	    itc.printStackTrace();
	}
        throw new InconsistentTypeCode();
    }


    public org.omg.DynamicAny.DynAny create_dyn_any_from_type_code( org.omg.CORBA.TypeCode type ) 
	throws InconsistentTypeCode
    {     
        type = ((org.jacorb.orb.TypeCode)type).originalType();

        try
        {
            switch( type.kind().value() )
            {
                case org.omg.CORBA.TCKind._tk_null:
                case org.omg.CORBA.TCKind._tk_void:
                case org.omg.CORBA.TCKind._tk_short:
                case org.omg.CORBA.TCKind._tk_long:
                case org.omg.CORBA.TCKind._tk_ushort:
                case org.omg.CORBA.TCKind._tk_ulong:
                case org.omg.CORBA.TCKind._tk_float:
                case org.omg.CORBA.TCKind._tk_double:
                case org.omg.CORBA.TCKind._tk_boolean:
                case org.omg.CORBA.TCKind._tk_char:
                case org.omg.CORBA.TCKind._tk_octet:
                case org.omg.CORBA.TCKind._tk_any:
                case org.omg.CORBA.TCKind._tk_TypeCode:
                case org.omg.CORBA.TCKind._tk_objref:
                case org.omg.CORBA.TCKind._tk_string:
                case org.omg.CORBA.TCKind._tk_longlong:
                case org.omg.CORBA.TCKind._tk_ulonglong:
                case org.omg.CORBA.TCKind._tk_wchar:
                case org.omg.CORBA.TCKind._tk_wstring:
                {
                    return new DynAny( this , type ) ;
                }
                case org.omg.CORBA.TCKind._tk_except:
                case org.omg.CORBA.TCKind._tk_struct:
                {
                    return new DynStruct( this , type ) ;                    
                }
                case org.omg.CORBA.TCKind._tk_enum:
                {
                    return new DynEnum( this , type ) ;
                }
                case org.omg.CORBA.TCKind._tk_array:
                {
                    return new DynArray( this , type ) ;
                }
                case org.omg.CORBA.TCKind._tk_sequence:
                {
                    return new DynSequence( this , type ) ;
                }
                case org.omg.CORBA.TCKind._tk_union:
                {
                    return new DynUnion( this , type ) ;
                }
                default:
                    throw new InconsistentTypeCode();
            }
        }
        catch( org.omg.DynamicAny.DynAnyPackage.InvalidValue iv )
        {
            iv.printStackTrace();
        }
        catch( org.omg.DynamicAny.DynAnyPackage.TypeMismatch itc )
        {
            org.jacorb.util.Debug.output(3, itc);
            throw new InconsistentTypeCode();
        }
	return null;
    }
}





