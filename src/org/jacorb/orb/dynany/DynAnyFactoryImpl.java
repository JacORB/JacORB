package org.jacorb.orb.dynany;

/*
 *        JacORB  - a free Java ORB
 *
 *   Copyright (C) 1997-99  Gerald Brose.
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

/**
 * @author Gerald Brose, FU Berlin
 * @version $Id$
 * 
 */

public class DynAnyFactoryImpl
    extends org.omg.DynamicAny.DynAnyFactoryPOA
{
    public DynAnyFactoryImpl(org.jacorb.orb.ORB orb)
    {
	_this_object( orb );
    }

    public org.omg.DynamicAny.DynAny create_dyn_any(org.omg.CORBA.Any value)
	throws org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCode
    {
	try
	{
	    switch( value.type().kind().value() )
	    {
	    case org.omg.CORBA.TCKind._tk_except:
  	    case org.omg.CORBA.TCKind._tk_struct:
		org.omg.DynamicAny.DynStructPOATie dst = 
  		    new org.omg.DynamicAny.DynStructPOATie(new DynStruct((org.jacorb.orb.ORB)_orb(), _this(), (org.jacorb.orb.Any)value));
		dst._this_object(_orb());
  		return dst._this();
	    case org.omg.CORBA.TCKind._tk_enum:
		org.omg.DynamicAny.DynEnumPOATie det = 
		    new org.omg.DynamicAny.DynEnumPOATie(new DynEnum((org.jacorb.orb.ORB)_orb(),_this(),  (org.jacorb.orb.Any)value));
		det._this_object(_orb());
		return det._this();
	    case org.omg.CORBA.TCKind._tk_array:
		org.omg.DynamicAny.DynArrayPOATie dyn_array_tie = 
		    new org.omg.DynamicAny.DynArrayPOATie(new DynArray((org.jacorb.orb.ORB)_orb(),_this() , (org.jacorb.orb.Any)value));
		dyn_array_tie._this_object(_orb());
		return dyn_array_tie._this();
	    case org.omg.CORBA.TCKind._tk_sequence:
		org.omg.DynamicAny.DynSequencePOATie dsqt = 
		    new org.omg.DynamicAny.DynSequencePOATie(
                        new DynSequence((org.jacorb.orb.ORB)_orb(), 
                                        _this(), 
                                        (org.jacorb.orb.Any)value));
		dsqt._this_object(_orb());
		return dsqt._this();
	    case org.omg.CORBA.TCKind._tk_union:
		org.omg.DynamicAny.DynUnionPOATie dyn_union_tie = 
		    new org.omg.DynamicAny.DynUnionPOATie(
                        new DynUnion( (org.jacorb.orb.ORB)_orb(),
                                      _this(), 
                                      (org.jacorb.orb.Any)value));
		dyn_union_tie._this_object(_orb());
		return dyn_union_tie._this();
	    default:
		org.omg.DynamicAny.DynAnyPOATie dat = 
		    new org.omg.DynamicAny.DynAnyPOATie(new DynAny((org.jacorb.orb.ORB)_orb(),_this() , (org.jacorb.orb.Any)value));
		dat._this_object(_orb());
		return dat._this();
	    }
	}
	catch( org.omg.DynamicAny.DynAnyPackage.InvalidValue iv )
	{
	    iv.printStackTrace();
	}
	catch( org.omg.DynamicAny.DynAnyPackage.TypeMismatch itc )
	{
	    itc.printStackTrace();
	    throw new org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCode();
	}
	return null;
    }

    public org.omg.DynamicAny.DynAny create_dyn_any_from_type_code(org.omg.CORBA.TypeCode type) 
	throws org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCode
    {     
	try
	{
	    switch( type.kind().value() )
	    {
	    case TCKind._tk_enum : 
		return new DynEnum((org.jacorb.orb.ORB)_orb(), _this(), type).copy();
	    case TCKind._tk_struct : 
		return new DynStruct((org.jacorb.orb.ORB)_orb(),_this() , type).copy();
	    case TCKind._tk_sequence : 
		return new DynSequence((org.jacorb.orb.ORB)_orb(),_this() , type).copy();
	    case TCKind._tk_union : 
		return new DynUnion((org.jacorb.orb.ORB)_orb(),_this() , type).copy();
	    case TCKind._tk_array : 
		return new DynArray((org.jacorb.orb.ORB)_orb(),_this() , type).copy();
	    default:
		return new DynAny((org.jacorb.orb.ORB)_orb(),_this() , type).copy();
	    }
	}
	catch( org.omg.DynamicAny.DynAnyPackage.InvalidValue iv )
	{
	    iv.printStackTrace();
	}
	catch( org.omg.DynamicAny.DynAnyPackage.TypeMismatch itc )
	{
	    org.jacorb.util.Debug.output(3, itc);
	    throw new org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCode();
	}
	return null;
    }
}


