package org.jacorb.orb.dynany;

/*
 *        JacORB  - a free Java ORB
 *
 *   Copyright (C) 1997-2000  Gerald Brose.
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
import org.omg.CORBA.TCKind;

/**
 * CORBA DynUnion
 *
 * @author (c) Gerald Brose, FU Berlin 1999
 * $Id$
 *
 */


public final class DynUnion
    extends DynAny
    implements org.omg.DynamicAny.DynUnionOperations
{
    private org.omg.DynamicAny.NameDynAnyPair[] members;
    private org.omg.CORBA.Any discriminator;
    private org.omg.DynamicAny.DynAny member;
    private String member_name;
    private int member_index;
    
    DynUnion( org.jacorb.orb.ORB orb,
              org.omg.DynamicAny.DynAnyFactory dynFactory,
              org.jacorb.orb.Any any 
              )
	throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch, InvalidValue
    {
	super(orb,dynFactory,any);
	limit = 2;
    }

    DynUnion( org.jacorb.orb.ORB orb,
              org.omg.DynamicAny.DynAnyFactory dynFactory,
              org.omg.CORBA.TypeCode tc
              )
	throws InvalidValue, TypeMismatch
    {
	if( tc.kind() != org.omg.CORBA.TCKind.tk_union )
	    throw new TypeMismatch();

	type = tc;

	this.orb = orb;
	this.dynFactory = dynFactory;

	limit = 2;

	try
	{
	    discriminator = type.member_label(0);
	    select_member();
	}
	catch( org.omg.CORBA.TypeCodePackage.BadKind bk )
	{
	    bk.printStackTrace();
	}
	catch( org.omg.CORBA.TypeCodePackage.Bounds b )
	{
	    b.printStackTrace();
	}   	
    }


    public void from_any(org.omg.CORBA.Any value) 
	throws InvalidValue, TypeMismatch
    {
	if( ! type().equal( value.type() ))
	    throw new org.omg.DynamicAny.DynAnyPackage.TypeMismatch();

	try
	{
	    limit = 2;
	    org.omg.CORBA.portable.InputStream is = value.create_input_stream();

	    discriminator = org.omg.CORBA.ORB.init().create_any();
	    discriminator.type( type().discriminator_type());
	    discriminator.read_value(is, type().discriminator_type());

	    int members = type().member_count();
	    org.omg.CORBA.Any member_any = null;
	    for( int i = 0; i < members; i++ )
	    {
		if( type().member_label(i).equals( discriminator ))
		{
		    member_any = org.omg.CORBA.ORB.init().create_any();
		    member_any.read_value( is, type().member_type(i));
		    member_name = type().member_name(i);
		    member_index = i;
		    break;		   
		}
	    }
	    if( member_any == null )
	    {
		int def_idx = type().default_index();
		if( def_idx != -1 )
		{
		    member_any = org.omg.CORBA.ORB.init().create_any();
		    member_any.read_value( is, type().member_type(def_idx));
		    member_name = type().member_name(def_idx);
		    member_index = def_idx;
		}
	    }
	    if( member_any != null )
	    {
		try
		{
		    member =  dynFactory.create_dyn_any( member_any );
		}
		catch( org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCode itc )
		{
		    // should neever happen
		    itc.printStackTrace();
		}		
	    }
	}
	catch( org.omg.CORBA.TypeCodePackage.Bounds b )
	{
	    b.printStackTrace();
	}   	
	catch( org.omg.CORBA.TypeCodePackage.BadKind bk )
	{
	    // should not happen anymore
	    bk.printStackTrace();
	}
    }


    public org.omg.CORBA.Any to_any() 
    {
      	CDROutputStream os = new CDROutputStream();

	os.write_value( discriminator.type(), 
                        (CDRInputStream)discriminator.create_input_stream() );

	os.write_value( member.type(), 
                        (CDRInputStream) member.to_any().create_input_stream());

	jacorb.orb.Any out_any = 
            (org.jacorb.orb.Any)org.omg.CORBA.ORB.init().create_any();

	out_any.type( type() );
	out_any.read_value( new CDRInputStream( orb, os.getBufferCopy()), type());
	return out_any;
    }

    public org.omg.DynamicAny.DynAny get_discriminator()
    {
	try
	{
	    return dynFactory.create_dyn_any( discriminator );
	}
	catch( org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCode itc )
	{
	    // should neever happen
	    itc.printStackTrace();
	}
	return null;
    }

    public void set_discriminator(org.omg.DynamicAny.DynAny d) 
	throws TypeMismatch
    {
	if( !d.type().equal( discriminator.type()))
	{
	    System.err.println("expected tc kind " + discriminator.type().kind().value()
				   + ", got " + d.type().kind().value() );	  
	    throw new TypeMismatch();
	}

	discriminator = d.to_any();

	/* check if the new discriminator is consistent with the 
	   currently active member. If not, select a new one */

	try
	{
	    if( ! type().member_label(member_index).equals( discriminator ))
		select_member();
	}
	catch( org.omg.CORBA.TypeCodePackage.Bounds b )
	{
	    b.printStackTrace();
	}   	
	catch( org.omg.CORBA.TypeCodePackage.BadKind bk )
	{
	    bk.printStackTrace();
	}	
    }

    /**
     * updates the private instance variables member, member_name and member_index
     * according to the current discriminator value
     */

    private void select_member()
    {
        member = null;
	try
	{
	    int members = type().member_count();
            
            /* search through all members and compare their label with the discriminator */

	    for( int i = 0; i < members; i++ )
	    {
		if( type().member_label(i).equals( discriminator ))
		{
		    try
		    {
			member = 
                            dynFactory.create_dyn_any_from_type_code( type().member_type(i));
		    }
		    catch( org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCode itc )
		    {
			itc.printStackTrace();
		    }
		    member_name = type().member_name(i);
		    member_index = i;
		    break;
		}
	    }

            /* none found, use default, if there is one */

	    if( member == null )
	    {
		int def_idx = type().default_index();
		if( def_idx != -1 )
		{
		    try
		    {
			member = 
                            dynFactory.create_dyn_any_from_type_code(type().member_type(def_idx));
		    }
		    catch( org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCode itc )
		    {
			itc.printStackTrace();
		    }  
		    member_name = type().member_name(def_idx);
		    member_index = def_idx;
		}
	    }
	}
	catch( org.omg.CORBA.TypeCodePackage.Bounds b )
	{
	    b.printStackTrace();
	}   	
	catch( org.omg.CORBA.TypeCodePackage.BadKind bk )
	{
	    // should not happen anymore
	    bk.printStackTrace();
	}	
    }


    public void set_to_default_member() 
	throws TypeMismatch
    {
	try
	{
	    int def_idx = type().default_index();
	    if( def_idx == -1 )
		throw new TypeMismatch();

	    pos = 0;

	    discriminator = type().member_label(def_idx);
	    select_member();
	}	
	catch( org.omg.CORBA.TypeCodePackage.Bounds b )
	{
	    b.printStackTrace();
	}   	
	catch( org.omg.CORBA.TypeCodePackage.BadKind bk )
	{
	    // should not happen anymore
	    bk.printStackTrace();
	}	
    }

    public void set_to_no_active_member() 
	throws TypeMismatch
    {
	/* if there is a default index, we do have active members */
	try
	{
	    if( type().default_index() != -1 )
		throw new TypeMismatch();

	    int members = type().member_count();
	    Any dis_any = (org.jacorb.orb.Any)org.omg.CORBA.ORB.init().create_any();
	    /* find a discriminator value that is not an explicit case label */
	    switch( type().kind().value() )
	    {
	    case TCKind._tk_boolean:
		{
		    boolean found_true = false;
		    boolean found_false = false;
		    for( int i = 0; i < members; i++ )
		    {
			found_true = ( type().member_label(i).extract_boolean() == true );
			found_false = ( type().member_label(i).extract_boolean() == false );
		    }
		    if( !found_true )
		    {
			dis_any.insert_boolean( true );
		    }
		    else if( !found_false )
		    {
			dis_any.insert_boolean( true );
		    }
		    else
			throw new TypeMismatch();
		    break;
		}
	    default:
		throw new TypeMismatch();
	    }
	}
	catch( org.omg.CORBA.TypeCodePackage.Bounds b )
	{
	    b.printStackTrace();
	}   	
	catch( org.omg.CORBA.TypeCodePackage.BadKind bk )
	{
	    // should not happen anymore
	    bk.printStackTrace();
	}	
    }


    public boolean has_no_active_member()
    {
	/* if there is a default index, we do have active members */

	try
	{
	    if( type().default_index() != -1 )
		return false;
	}
	catch( org.omg.CORBA.TypeCodePackage.BadKind bk )
	{
	    // should not happen anymore
	    bk.printStackTrace();
	}	
	return( member != null );
    }

    public org.omg.CORBA.TCKind discriminator_kind()
    {
	return discriminator.type().kind();
    }

    public org.omg.DynamicAny.DynAny member() 
	throws InvalidValue
    {
	if( member == null )
	    throw new InvalidValue();

	return  member;
    }

    public org.omg.CORBA.TCKind member_kind() 
	throws InvalidValue
    {
	if( member == null )
	    throw new InvalidValue();
	return member.type().kind();
    }

    public java.lang.String member_name() 
	throws InvalidValue
    {
	if( member == null )
	    throw new InvalidValue();
	return member_name;
    }

    public void destroy()
    {
	super.destroy();
	members = null;
	member_index = -1;
    }

    /* iteration interface */

    public org.omg.DynamicAny.DynAny current_component()
    {	
	if( pos == -1 )
	    return null;
	if( pos == 0 )
	    return get_discriminator();
	else
	{
	    try
	    {
		return member();
	    }
	    catch( org.omg.DynamicAny.DynAnyPackage.InvalidValue iv )
	    {
		iv.printStackTrace();
	    }
	    return null;
	}
    }
   


}


