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

import org.omg.DynamicAny.DynAnyPackage.*;
import org.omg.DynamicAny.*;
import org.jacorb.orb.*;

/**
 * CORBA DynStruct
 *
 * @author (c) Gerald Brose, FU Berlin 1999
 * $Id$
 */

public final class DynStruct
    extends DynAny
    implements org.omg.DynamicAny.DynStruct
{
    private org.omg.DynamicAny.NameValuePair[] members;

    /** only set if this represents an exception */
    private String exception_msg;

    DynStruct( org.omg.DynamicAny.DynAnyFactory dynFactory, 
               org.omg.CORBA.TypeCode tc)
        throws InvalidValue, TypeMismatch
    {
        org.jacorb.orb.TypeCode _type = 
            ((org.jacorb.orb.TypeCode)tc).originalType();

        if( _type.kind().value() != org.omg.CORBA.TCKind._tk_except && 
            _type.kind().value() != org.omg.CORBA.TCKind._tk_struct )
            throw new TypeMismatch();

        this.orb = org.omg.CORBA.ORB.init();
        this.dynFactory = dynFactory;

        type = _type;

        try
        {
            /* initialize position for all except empty exceptions */	
            if( !isEmptyEx () )
            {
                pos = 0;
            }
            if( _type.kind().value() == org.omg.CORBA.TCKind._tk_except )
                exception_msg = type.id();

            limit = type.member_count();
            members = new NameValuePair[limit];
            for( int i = 0 ; i < limit; i++ )
            {		
                org.jacorb.orb.TypeCode _tc = 
                    ((org.jacorb.orb.TypeCode)type.member_type(i)).originalType();
                members[i] =
                    new NameValuePair(
                                      type.member_name(i),
                                      dynFactory.create_dyn_any_from_type_code( _tc ).to_any());

            }
        }
        catch( org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCode itc )
        {
            itc.printStackTrace();
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
        checkDestroyed ();

        if( !value.type().equivalent( type() ))
            throw new org.omg.DynamicAny.DynAnyPackage.TypeMismatch();

        type = ((org.jacorb.orb.TypeCode)value.type()).originalType();

        try
        {      
            limit = type().member_count();
            members = new NameValuePair[limit];
            org.omg.CORBA.portable.InputStream is = 
                value.create_input_stream();

            if( type.kind().value() == org.omg.CORBA.TCKind._tk_except )
            {
                exception_msg = is.read_string();
            }

            for( int i = 0 ; i < limit; i++ )
            {
                try
                {
                    Any a = (org.jacorb.orb.Any)orb.create_any();
                    a.read_value(is, 
                                 ((org.jacorb.orb.TypeCode)type.member_type(i)).originalType());

                    members[i] = new NameValuePair( type().member_name(i), a);
                }
                catch( org.omg.CORBA.TypeCodePackage.Bounds b )
                {
                    b.printStackTrace();
                }   	
            }	
        }
        catch( org.omg.CORBA.TypeCodePackage.BadKind bk )
        {
            // should not happen anymore
            bk.printStackTrace();
        }
         
        super.from_any( value );
    }


    public org.omg.CORBA.Any to_any() 
    {
        checkDestroyed ();
        org.jacorb.orb.Any out_any = 
            (org.jacorb.orb.Any)orb.create_any();
        out_any.type( type());

        CDROutputStream os = new CDROutputStream();

        if( type().kind().value() == org.omg.CORBA.TCKind._tk_except )
        {
            os.write_string( exception_msg );
        }

        for( int i = 0; i < members.length; i++)
        {
            os.write_value( members[i].value.type(), 
                            (CDRInputStream)members[i].value.create_input_stream());
        }

        CDRInputStream is = new CDRInputStream(orb, os.getBufferCopy());
        out_any.read_value( is, type());
        return out_any;
    }

    /**
     * @overrides  equal() in DynAny
     */

    public boolean equal( org.omg.DynamicAny.DynAny dyn_any )
    {
        checkDestroyed ();
        if( !type().equal( dyn_any.type())  )
            return false;

        org.omg.DynamicAny.DynStruct other =  DynStructHelper.narrow( dyn_any );

        NameValuePair[]  elements = get_members();
        NameValuePair[]  other_elements = other.get_members();

        for( int i = 0; i < elements.length; i++ )
        {
            if( !(elements[i].value.equal( other_elements[i].value ))) 
                return false;
        }

        return true;
    }


    /* DynStruct specials */

    public java.lang.String current_member_name()
        throws TypeMismatch, InvalidValue
    {
        checkDestroyed ();

        if (isEmptyEx ())
        {
            throw new TypeMismatch ();
        }
        if (pos == -1)
        {
            throw new InvalidValue ();
        }
        return members[pos].id;	
    }


    public org.omg.CORBA.TCKind current_member_kind()
        throws TypeMismatch, InvalidValue
    {
        checkDestroyed ();

        if (isEmptyEx ())
        {
            throw new TypeMismatch ();
        }
        if (pos == -1)
        {
            throw new InvalidValue ();
        }      
        return members[pos].value.type().kind();
    }


    public NameValuePair[] get_members()
    {
        checkDestroyed ();
        return members;
    }


    public void set_members( NameValuePair[] nvp )
        throws InvalidValue, TypeMismatch
    {
        checkDestroyed ();
        if( nvp.length != limit )
            throw new org.omg.DynamicAny.DynAnyPackage.InvalidValue();
 
        for( int i = 0; i < limit; i++ )
        {
            if( ! nvp[i].value.type().equivalent( members[i].value.type() ))
            {
                throw new org.omg.DynamicAny.DynAnyPackage.TypeMismatch();
            }

            if(! (nvp[i].id.equals("") || nvp[i].id.equals( members[i].id )))
                throw new org.omg.DynamicAny.DynAnyPackage.TypeMismatch();
        }
        members = nvp;	
    }


    public org.omg.DynamicAny.NameDynAnyPair[] get_members_as_dyn_any()
    {
        checkDestroyed ();
        NameDynAnyPair[] result = new NameDynAnyPair[limit];
        try
        {
            for( int i = 0; i < limit; i++ )
            {
                result[i] = new NameDynAnyPair( members[i].id, 
                                                dynFactory.create_dyn_any( members[i].value )); 
            }	
        }
        catch( org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCode itc )
        {
            itc.printStackTrace();
        }
        return result ;
    }


    public void set_members_as_dyn_any(org.omg.DynamicAny.NameDynAnyPair[] nvp) 
        throws TypeMismatch, InvalidValue
    {
        checkDestroyed ();
        if( nvp.length != limit )
            throw new org.omg.DynamicAny.DynAnyPackage.InvalidValue();

        for( int i = 0; i < limit; i++ )
        {
            if(! nvp[i].value.type().equivalent( members[i].value.type() ))
                throw new org.omg.DynamicAny.DynAnyPackage.TypeMismatch();
	    
            if(! (nvp[i].id.equals("") || nvp[i].id.equals( members[i].id )))
                throw new org.omg.DynamicAny.DynAnyPackage.TypeMismatch();
	    
        }
        members = new NameValuePair[nvp.length];
        for( int i = 0; i < limit; i++ )
        {
            members[i] = new NameValuePair( nvp[i].id, nvp[i].value.to_any() );
        }	
    }

    public void destroy()
    {
        super.destroy();
        members = null;
    }

    /**
     * returns the DynAny's internal any representation, 
     * @overwrites getRepresentation() in DynAny
     */

    protected org.omg.CORBA.Any getRepresentation()
    {
        return members[pos].value;
    }

    /* iteration interface */

    public org.omg.DynamicAny.DynAny current_component() 
        throws TypeMismatch
    {	
        checkDestroyed ();
        try
        {
            /*  special case for empty exceptions */
            if( isEmptyEx () )
            {
                throw new TypeMismatch ();
            }

            if( pos == -1 )
            {
                return null;
            }
            return dynFactory.create_dyn_any( members[pos].value );
        }
        catch( org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCode itc )
        {
            itc.printStackTrace();
        }
        return null;
    }

    private boolean isEmptyEx ()
    {
        try
        {
            return (type.kind().value() == org.omg.CORBA.TCKind._tk_except && 
                    type.member_count() == 0);
        }
        catch( org.omg.CORBA.TypeCodePackage.BadKind bk )
        {
            bk.printStackTrace();
        }
        return false;
    }
   
}
