package org.jacorb.orb.dynany;

/*
 *        JacORB  - a free Java ORB
 *
 *   Copyright (C) 1997-2001  Gerald Brose, FU Berlin.
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
import org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCode;
import org.omg.DynamicAny.*;

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
    implements org.omg.DynamicAny.DynUnion
{
    private org.omg.DynamicAny.NameDynAnyPair[] members;
    private org.omg.CORBA.Any discriminator;
    private org.omg.DynamicAny.DynAny member;
    private String member_name;
    private int member_index;
    
    /** 
     * constructor from TypeCode
     */
    
    DynUnion( org.omg.DynamicAny.DynAnyFactory dynFactory,
              org.omg.CORBA.TypeCode tc )
	throws InvalidValue, TypeMismatch
    {
        org.jacorb.orb.TypeCode _type = 
            ((org.jacorb.orb.TypeCode)tc).originalType();

	if( _type.kind() != org.omg.CORBA.TCKind.tk_union )
	    throw new TypeMismatch();

	type = _type;

        this.orb = org.omg.CORBA.ORB.init();
	this.dynFactory = dynFactory;

	limit = 2;

	try
	{
            for( int i = 0; i < type.member_count(); i++ )
            {
                discriminator = type.member_label(i);
                if( discriminator.type().kind().value() != 
                    org.omg.CORBA.TCKind._tk_octet )
                {
                    break;
                }
            }
	    select_member();
            org.jacorb.util.Debug.output( 3, "DynUnion.ctor(), member == null? " + 
                                          ( member == null ));
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

    /**
     * @overrides  from_any() in DynAny
     */

    public void from_any( org.omg.CORBA.Any value ) 
	throws InvalidValue, TypeMismatch
    {
	if( ! type().equivalent( value.type() ))
	    throw new org.omg.DynamicAny.DynAnyPackage.TypeMismatch();

	try
	{
            type = ((org.jacorb.orb.TypeCode)value.type()).originalType();
            super.from_any( value );

	    limit = 2;
	    org.omg.CORBA.portable.InputStream is = 
                value.create_input_stream();

	    discriminator = org.omg.CORBA.ORB.init().create_any();
	    discriminator.type( type().discriminator_type());
            
	    discriminator.read_value(is, type().discriminator_type());

	    int member_count = type().member_count();
            members = new org.omg.DynamicAny.NameDynAnyPair[member_count];
	    org.omg.CORBA.Any member_any = null;
	    for( int i = 0; i < member_count; i++ )
	    {
                try
                {
                    members[i] = 
                        new org.omg.DynamicAny.NameDynAnyPair( 
                               type().member_name(i),
                               dynFactory.create_dyn_any_from_type_code( type().member_type(i) ));	
                }
		catch( InconsistentTypeCode itc )
		{
		    // should never happen
		    itc.printStackTrace();
		}	
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
		    member = dynFactory.create_dyn_any( member_any );
		}
		catch( InconsistentTypeCode itc )
		{
		    // should never happen
		    itc.printStackTrace();
		}		
	    }

            org.jacorb.util.Debug.output( 3, "DynUnion.from_any(), member == null? " + 
                                          ( member == null ));
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

    /** 
     * @return an Any that holds a copy of this union
     */

    public org.omg.CORBA.Any to_any() 
    {
      	CDROutputStream os = new CDROutputStream();

	os.write_value( discriminator.type(), 
                        (CDRInputStream)discriminator.create_input_stream() );

	os.write_value( member.type(), 
                        (CDRInputStream) member.to_any().create_input_stream());

	org.jacorb.orb.Any out_any = 
            (org.jacorb.orb.Any)org.omg.CORBA.ORB.init().create_any();

	out_any.type( type() );
	out_any.read_value( new CDRInputStream( orb, os.getBufferCopy()), type());
	return out_any;
    }



    /**
     * @overrides  equal() in DynAny
     */

    public boolean equal( org.omg.DynamicAny.DynAny dyn_any )
    {
        if( !type().equal( dyn_any.type())  )
            return false;

        org.omg.DynamicAny.DynUnion other =  DynUnionHelper.narrow( dyn_any );

        if( ! get_discriminator().equal( other.get_discriminator()))
            return false;

        if( !has_no_active_member() ) 
        {
            // other must have the same here because we know the 
            // discriminators are equal

            try
            {
                if( ! member.equal( other.member()))
                    return false;
            }
            catch( Exception e )
            {
                // should not happen
                e.printStackTrace();
            }
        }
        return true;
    }



    /** 
     * @return the current discriminator value
     */

    public org.omg.DynamicAny.DynAny get_discriminator()
    {
	try
	{
	    return dynFactory.create_dyn_any( discriminator );
	}
	catch( InconsistentTypeCode itc )
	{
	    // should never happen
	    itc.printStackTrace();
	}
	return null;
    }

    /** 
     * sets the discriminator to d  
     * @throws  TypeMismatch if  the TypeCode of the d parameter 
     * is not equivalent to  the TypeCode of the union's discriminator
     */

    public void set_discriminator( org.omg.DynamicAny.DynAny d ) 
	throws TypeMismatch
    {
	if( ! d.type().equivalent( discriminator.type()))
	{
	    System.err.println("expected tc kind " + 
                               discriminator.type().kind().value() + 
			       ", got " + d.type().kind().value() );	  
	    throw new TypeMismatch();
	}

	discriminator = d.to_any();

	/* check if the new discriminator is consistent with the 
	   currently active member. If not, select a new one */

	try
	{
	    if( ! type().member_label( member_index ).equals( discriminator ) )
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
     * updates the private  instance variables member, member_name and
     * member_index according to the current discriminator value 
     */

    private void select_member()
    {
        member = null;
	try
	{
	    int members = type().member_count();
            
            /* search through all members and compare their label with
               the discriminator */

	    for( int i = 0; i < members; i++ )
	    {
		if( type().member_label(i).equals( discriminator ))
		{
		    try
		    {
			member = 
                            dynFactory.create_dyn_any_from_type_code( 
                                   type().member_type(i));
		    }
		    catch( InconsistentTypeCode itc )
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
		    catch( InconsistentTypeCode itc )
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

    /**
     * sets the discriminator to  a value that is consistent with the
     * value  of the  default case  of a union;  it sets  the current
     * position to  zero  and causes  component_count  to return  2.
     *
     * @throws TypeMismatch if the union  does not have an explicit 
     * default case.
     */

    public void set_to_default_member() 
	throws TypeMismatch
    {
	try
	{
	    int def_idx = type().default_index();
	    if( def_idx == -1 )
		throw new TypeMismatch();

	    pos = 0;

	    discriminator = type().member_label( def_idx );
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


    /**
     * sets the  discriminator to a value that  does not correspond to
     * any of the  union's case labels; it sets the  current position 
     * to zero and     causes     component_count     to    return 1. 
     *
     * @throws TypeMismatch if the union  has an explicit default 
     * case or uses the entire range of discriminator values for 
     * explicit case labels.  
     */

    public void set_to_no_active_member() 
	throws TypeMismatch
    {
	try
	{
            /* if there is a default index, we do have active members */
            if( type().default_index() != -1 )
		throw new TypeMismatch();

	    /* find a discriminator value that is not an explicit case label */
	    switch( discriminator.type().kind().value() )
	    {
	    case TCKind._tk_boolean:
		{
		    boolean found_true = false;
		    boolean found_false = false;
		    for( int i = 0; i < type().member_count(); i++ )
		    {
			found_true |= 
                            ( type().member_label(i).extract_boolean() == true );
			found_false |= 
                            ( type().member_label(i).extract_boolean() == false );
		    }
		    if( !found_true )
		    {
			discriminator.insert_boolean( true );
		    }
		    else if( !found_false )
		    {
			discriminator.insert_boolean( true );
		    }
		    else
			throw new TypeMismatch();
		    break;
		}
	    case TCKind._tk_char:
            {
                // does this union use all char values
                if( type().member_count() == 256 )
                    throw new TypeMismatch();

                for( short s = 0; s < 256; s++)
                {
                    char c = (char)s;
                }
            }
	    case TCKind._tk_long:
            {	
                // does this union use all long values
                if( type().member_count() == 2147483647) // -2^31,  max long
                    throw new TypeMismatch();
            }
	    case TCKind._tk_enum:
            {
                // does this union use all enum labels available?
                if( type().member_count() == discriminator.type().member_count())
                    throw new TypeMismatch();

                org.omg.DynamicAny.DynEnum enum = null;
                try
                {         
                    enum = DynEnumHelper.narrow(
                              dynFactory.create_dyn_any_from_type_code(discriminator.type()));
                }
                catch( InconsistentTypeCode it )
                {
                    it.printStackTrace();
                }   	

                for( int i = 0; i < discriminator.type().member_count(); i++ )
                {    
                    try
                    {
                        enum.set_as_string( discriminator.type().member_name(i) );
                    }
                    catch( InvalidValue iv )
                    {
                        // should not happen anymore
                        iv.printStackTrace();
                    }

                    boolean found = false; // is the value used as a label?
                    
                    for( int j = 0; j < type().member_count() && !found ; j++ )
                    {
                        if( enum.equals( type().member_label(i))  )
                        {
                            found = true;
                        }
                    }                   
                    if( !found )
                    {
                        // the enum value is not found among the union's label
                        discriminator = enum.to_any();
                        return;
                    }                    
                }
                // no unused value found, should not happen
                throw new TypeMismatch();                
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

    /** 
     * @returns true, if the union  has no active member (that is, the
     * union's  value consists solely of  its discriminator because
     * the discriminator has a value that is not listed as an explicit
     *  case label).  Calling this  operation on  a union  that  has a
     * default case  returns false. Calling this operation  on a union
     * that uses the entire range of discriminator values for explicit
     * case labels returns false.  */

    public boolean has_no_active_member()
    {
	try
	{
	    if( type().default_index() != -1 )
            {
		return false;
            }

            // check whether  the union  uses the  complete  range of
            // discriminator values

            if( discriminator.type().member_count() == members.length )
            {
                return false;
            }
            else
            {                    
                org.omg.CORBA.TypeCode tc = type();
                
                for( int i = 0; i < type.member_count(); i++ )
                {
                    if( discriminator.equal( type.member_label(i)  ))
                        return false;
                }
                return true;
            }
	}
	catch( org.omg.CORBA.TypeCodePackage.Bounds b )
	{
	    // should not happen anymore
	    b.printStackTrace();
	}	
	catch( org.omg.CORBA.TypeCodePackage.BadKind bk )
	{
	    // should not happen anymore
	    bk.printStackTrace();
	}	
	return( member == null );
    }

    /** 
     * @returns the TCKind value of the discriminator's TypeCode.
     */

    public org.omg.CORBA.TCKind discriminator_kind()
    {
	return discriminator.type().kind();
    }

    /** 
     * @returns the currently active member. 
     * @throws InvalidValue if the union has no active  member
     */

    public org.omg.DynamicAny.DynAny member() 
	throws InvalidValue
    {
	if( has_no_active_member() )
	    throw new InvalidValue();

	return  member;
    }

    /** 
     * @returns the TypeCode kind of the currently active member. 
     * @throws InvalidValue if the union has no active  member
     */

    public org.omg.CORBA.TCKind member_kind() 
	throws InvalidValue
    {
	if( has_no_active_member() )
	    throw new InvalidValue();

	return member.type().kind();
    }

    /** 
     * @returns the name of the currently active member. 
     * @throws InvalidValue if the union has no active member
     */

    public String member_name() 
	throws InvalidValue
    {
	if( has_no_active_member() )
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






