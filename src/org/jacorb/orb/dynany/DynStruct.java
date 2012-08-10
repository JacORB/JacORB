package org.jacorb.orb.dynany;

/*
 *        JacORB  - a free Java ORB
 *
 *   Copyright (C) 1997-2012 Gerald Brose / The JacORB Team.
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

import org.slf4j.Logger;
import org.jacorb.orb.Any;
import org.jacorb.orb.CDRInputStream;
import org.jacorb.orb.CDROutputStream;
import org.jacorb.orb.TypeCode;
import org.omg.DynamicAny.DynStructHelper;
import org.omg.DynamicAny.NameDynAnyPair;
import org.omg.DynamicAny.NameValuePair;
import org.omg.DynamicAny.DynAnyPackage.InvalidValue;
import org.omg.DynamicAny.DynAnyPackage.TypeMismatch;

/**
 * CORBA DynStruct
 *
 * @author Gerald Brose
 */

public final class DynStruct
    extends DynAny
    implements org.omg.DynamicAny.DynStruct
{
    private org.omg.DynamicAny.NameValuePair[] members;

    /** only set if this represents an exception */
    private String exceptionMsg;

    DynStruct( org.omg.DynamicAny.DynAnyFactory dynFactory,
            org.omg.CORBA.TypeCode type,
            org.jacorb.orb.ORB orb,
            Logger logger)
            throws TypeMismatch
    {
        super(dynFactory, orb, logger);

        org.omg.CORBA.TypeCode _type = TypeCode.originalType( type );

        if( _type.kind().value() != org.omg.CORBA.TCKind._tk_except &&
                _type.kind().value() != org.omg.CORBA.TCKind._tk_struct )
        {
            throw new TypeMismatch();
        }

        typeCode = _type;

        try
        {
            /* initialize position for all except empty exceptions */
            if( !isEmptyEx () )
            {
                pos = 0;
            }
            if( _type.kind().value() == org.omg.CORBA.TCKind._tk_except )
            {
                exceptionMsg = typeCode.id();
            }

            limit = typeCode.member_count();
            members = new NameValuePair[limit];
            for( int i = 0 ; i < limit; i++ )
            {
                org.omg.CORBA.TypeCode _tc =
                    TypeCode.originalType( typeCode.member_type(i) );
                members[i] =
                    new NameValuePair(
                            typeCode.member_name(i),
                            dynFactory.create_dyn_any_from_type_code( _tc ).to_any());

            }
        }
        catch( org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCode e )
        {
            throw unexpectedException(e);
        }
        catch( org.omg.CORBA.TypeCodePackage.BadKind e )
        {
            throw unexpectedException(e);
        }
        catch( org.omg.CORBA.TypeCodePackage.Bounds e )
        {
            throw unexpectedException(e);
        }
    }

    public void from_any(org.omg.CORBA.Any value)
        throws InvalidValue, TypeMismatch
    {
        checkDestroyed ();

        if( !value.type().equivalent( type() ))
        {
            throw new org.omg.DynamicAny.DynAnyPackage.TypeMismatch();
        }

        typeCode = TypeCode.originalType( value.type() );

        try
        {
            limit = type().member_count();
            members = new NameValuePair[limit];
            org.omg.CORBA.portable.InputStream is =
                value.create_input_stream();

            if( typeCode.kind().value() == org.omg.CORBA.TCKind._tk_except )
            {
                exceptionMsg = is.read_string();
            }

            for( int i = 0 ; i < limit; i++ )
            {
                try
                {
                    Any any = (org.jacorb.orb.Any)orb.create_any();
                    any.read_value(is,
                                 TypeCode.originalType(typeCode.member_type(i)));

                    members[i] = new NameValuePair( type().member_name(i), any);
                }
                catch( org.omg.CORBA.TypeCodePackage.Bounds e )
                {
                    throw unexpectedException(e);
                }
            }
        }
        catch( org.omg.CORBA.TypeCodePackage.BadKind e )
        {
            throw unexpectedException(e);
        }

        super.from_any( value );
    }


    public org.omg.CORBA.Any to_any()
    {
        checkDestroyed ();
        org.omg.CORBA.Any out_any = orb.create_any();
        out_any.type( type());

        final CDROutputStream out = new CDROutputStream(orb);

        try
        {
            if( type().kind().value() == org.omg.CORBA.TCKind._tk_except )
            {
                out.write_string( exceptionMsg );
            }

            for( int i = 0; i < members.length; i++)
            {
                out.write_value( members[i].value.type(),
                        members[i].value.create_input_stream());
            }

            final CDRInputStream in = new CDRInputStream(orb, out.getBufferCopy());
            try
            {
                out_any.read_value( in, type());
                return out_any;
            }
            finally
            {
                in.close();
            }
        }
        finally
        {
            out.close();
        }
    }

    /**
     * Overrides  equal() in DynAny
     */

    public boolean equal( org.omg.DynamicAny.DynAny dyn_any )
    {
        checkDestroyed ();
        if( !type().equal( dyn_any.type())  )
        {
            return false;
        }

        org.omg.DynamicAny.DynStruct other =  DynStructHelper.narrow( dyn_any );

        NameValuePair[]  elements = get_members();
        NameValuePair[]  other_elements = other.get_members();

        for( int i = 0; i < elements.length; i++ )
        {
            if( !(elements[i].value.equal( other_elements[i].value )))
            {
                return false;
            }
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
        {
            throw new org.omg.DynamicAny.DynAnyPackage.InvalidValue();
        }

        for( int i = 0; i < limit; i++ )
        {
            if( ! nvp[i].value.type().equivalent( members[i].value.type() ))
            {
                throw new org.omg.DynamicAny.DynAnyPackage.TypeMismatch();
            }

            if(! (nvp[i].id.equals("") || nvp[i].id.equals( members[i].id )))
            {
                throw new org.omg.DynamicAny.DynAnyPackage.TypeMismatch();
            }
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

            return result;
        }
        catch( org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCode e )
        {
            throw unexpectedException(e);
        }
    }


    public void set_members_as_dyn_any(org.omg.DynamicAny.NameDynAnyPair[] nvp)
        throws TypeMismatch, InvalidValue
    {
        checkDestroyed ();
        if( nvp.length != limit )
        {
            throw new org.omg.DynamicAny.DynAnyPackage.InvalidValue();
        }

        for( int i = 0; i < limit; i++ )
        {
            if(! nvp[i].value.type().equivalent( members[i].value.type() ))
            {
                throw new org.omg.DynamicAny.DynAnyPackage.TypeMismatch();
            }

            if(! (nvp[i].id.equals("") || nvp[i].id.equals( members[i].id )))
            {
                throw new org.omg.DynamicAny.DynAnyPackage.TypeMismatch();
            }

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
     * returns the DynAny's internal any representation.
     * <p>
     * Overrides getRepresentation() in DynAny.
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
        catch( org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCode e )
        {
            throw unexpectedException(e);
        }
    }

    private boolean isEmptyEx ()
    {
        try
        {
            return (typeCode.kind().value() == org.omg.CORBA.TCKind._tk_except &&
                    typeCode.member_count() == 0);
        }
        catch( org.omg.CORBA.TypeCodePackage.BadKind e )
        {
            throw unexpectedException(e);
        }
    }
}
