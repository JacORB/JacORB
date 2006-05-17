package org.jacorb.orb.dynany;

/*
 *        JacORB  - a free Java ORB
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

import org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCode;

import org.apache.avalon.framework.logger.Logger;
import org.jacorb.orb.TypeCode;

/**
 * @author Gerald Brose, FU Berlin
 * @version $Id$
 */

public class DynAnyFactoryImpl
    extends org.omg.CORBA.LocalObject
    implements org.omg.DynamicAny.DynAnyFactory
{
    private final org.jacorb.orb.ORB orb;
    private final Logger logger;

    public DynAnyFactoryImpl( org.jacorb.orb.ORB orb )
    {
        super();
        this.orb = orb;
        logger = orb.getConfiguration().getNamedLogger("jacorb.orb");
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
           logger.error("unable to create DynAny", iv);
       }
       catch( org.omg.DynamicAny.DynAnyPackage.TypeMismatch itc )
       {
           logger.error("unable to create DynAny", itc);
       }
       throw new InconsistentTypeCode();
    }


    public org.omg.DynamicAny.DynAny create_dyn_any_from_type_code( org.omg.CORBA.TypeCode typeCode )
    throws InconsistentTypeCode
    {
        final org.omg.CORBA.TypeCode _type = TypeCode.originalType( typeCode );

        try
        {
            switch( _type.kind().value() )
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
                   return new DynAny( this , _type, orb, logger) ;
                }
                case org.omg.CORBA.TCKind._tk_fixed:
                {
                    return new DynFixed( this , _type, orb, logger) ;
                }
                case org.omg.CORBA.TCKind._tk_except:
                case org.omg.CORBA.TCKind._tk_struct:
                {
                    return new DynStruct( this , _type, orb, logger) ;
                }
                case org.omg.CORBA.TCKind._tk_enum:
                {
                    return new DynEnum( this , _type, orb, logger) ;
                }
                case org.omg.CORBA.TCKind._tk_array:
                {
                    return new DynArray( this , _type, orb, logger) ;
                }
                case org.omg.CORBA.TCKind._tk_sequence:
                {
                    return new DynSequence( this , _type, orb, logger ) ;
                }
                case org.omg.CORBA.TCKind._tk_union:
                {
                    return new DynUnion( this , _type, orb, logger ) ;
                }
                case org.omg.CORBA.TCKind._tk_value:
                {
                    throw new org.omg.CORBA.NO_IMPLEMENT
                        ("DynValue is not yet implemented in Jacorb");
                }
                default:
                    throw new InconsistentTypeCode();
            }
        }
        catch( org.omg.DynamicAny.DynAnyPackage.InvalidValue iv )
        {
            logger.error("unable to create DynAny from TypeCode", iv);
        }
        catch( org.omg.DynamicAny.DynAnyPackage.TypeMismatch itc )
        {
            throw new InconsistentTypeCode();
        }
        return null;
    }
}





