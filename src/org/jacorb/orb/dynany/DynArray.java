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

import org.omg.DynamicAny.NameValuePair;
import org.omg.DynamicAny.DynAnyPackage.*;
import org.omg.DynamicAny.NameDynAnyPair;
import org.jacorb.orb.*;
import java.util.Vector;

/**
 * CORBA DynArray
 *
 * @author (c) Gerald Brose, FU Berlin 1999
 * $Id$
 */

public final class DynArray
    extends DynAny
    implements org.omg.DynamicAny.DynArrayOperations
{
    private org.omg.CORBA.TypeCode elementType;
    private org.omg.CORBA.Any[] members;

    DynArray(org.jacorb.orb.ORB orb,
             org.omg.DynamicAny.DynAnyFactory dynFactory,
             org.jacorb.orb.Any any)
	throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch, InvalidValue
    {
	super(orb,dynFactory,any);
    }

    DynArray(org.jacorb.orb.ORB orb,
             org.omg.DynamicAny.DynAnyFactory dynFactory,
             org.omg.CORBA.TypeCode tc)
	throws InvalidValue, TypeMismatch
    {
	if( tc.kind() != org.omg.CORBA.TCKind.tk_array )
	    throw new TypeMismatch();	
	try
	{
	    type = tc;
	    this.orb = orb;
	    this.dynFactory = dynFactory;
	    elementType = tc.content_type();
	    limit = tc.length();
	    members = new Any[limit];
	    try
	    {
		for( int i = limit; i-- > 0;)
		{
		    members[i] = dynFactory.create_dyn_any_from_type_code( elementType ).to_any();
		}
	    }
	    catch( org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCode itc )
	    {
		// should never happen
		itc.printStackTrace();
	    }
	}
	catch( org.omg.CORBA.TypeCodePackage.BadKind bk )
	{
	    bk.printStackTrace();
	}
    }

    public void from_any(org.omg.CORBA.Any value) 
	throws InvalidValue, TypeMismatch
    {
	if( ! type.equal( value.type() ))
	    throw new org.omg.DynamicAny.DynAnyPackage.TypeMismatch();

	type = value.type();

	try
	{	    
	    limit = type().length();
	    elementType = type().content_type();
	    if( limit > 0 )
		pos = 0;

	    org.omg.CORBA.portable.InputStream is = value.create_input_stream();
	    members = new org.omg.CORBA.Any[limit];

	    for( int i = 0 ; i < limit; i++ )
	    {
		members[i] = org.omg.CORBA.ORB.init().create_any();
		members[i].read_value(is, elementType);	
	    }	
	}
	catch( org.omg.CORBA.TypeCodePackage.BadKind bk )
	{
	    // should not happen anymore
	    bk.printStackTrace();
	}
    }

    public org.omg.CORBA.Any to_any() 
    {
	org.jacorb.orb.Any out_any = 
            (org.jacorb.orb.Any)org.omg.CORBA.ORB.init().create_any();
	out_any.type( type());

	CDROutputStream os = new CDROutputStream();

	for( int i = 0; i < limit; i++)
	{
	    os.write_value( elementType, 
                            (CDRInputStream)members[i].create_input_stream());
	}

	CDRInputStream is = new CDRInputStream(orb, os.getBufferCopy());
	out_any.read_value( is, type());
	return out_any;
    }

    public org.omg.CORBA.Any[] get_elements()
    {
	return members;
    }

    public void set_elements(org.omg.CORBA.Any[] value) 
	throws TypeMismatch, InvalidValue
    {
	if( value.length != limit )
	    throw new InvalidValue();

	for( int i = value.length; i-- > 0 ;)
	    if( value[i].type().kind() != elementType.kind() )
		throw new TypeMismatch();

	/** ok now */
	members = value;
    }

    public org.omg.DynamicAny.DynAny[] get_elements_as_dyn_any()
    {
	org.omg.DynamicAny.DynAny[] result = new org.omg.DynamicAny.DynAny[ members.length ];
	try
	{
	    for( int i = members.length; i-- > 0; )
		result[i] = dynFactory.create_dyn_any( members[i]);
	    return result;
	}
	catch( org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCode itc )
	{
	    // should never happen
	    itc.printStackTrace();
	}
	return null;
    }

    public void set_elements_as_dyn_any(org.omg.DynamicAny.DynAny[] value) 
	throws TypeMismatch, InvalidValue
    {
	org.omg.CORBA.Any [] any_seq = new org.omg.CORBA.Any[value.length];
	for( int i = value.length; i-- > 0; )
	    any_seq[i] = value[i].to_any();

	set_elements( any_seq );
    }

    public void destroy()
    {
	super.destroy();
	members = null;
	elementType = null;
    }


    /* iteration interface */

    public org.omg.DynamicAny.DynAny current_component()
    {	
	if( pos == -1 )
	    return null;
	try
	{
	    return dynFactory.create_dyn_any( members[pos] );
	}
	catch( org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCode itc )
	{
	    // should never happen
	    itc.printStackTrace();
	}
	return null;
    }
   

}








