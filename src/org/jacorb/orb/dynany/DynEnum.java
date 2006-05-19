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

import org.omg.DynamicAny.DynAnyPackage.*;
import org.omg.DynamicAny.*;
import org.apache.avalon.framework.logger.Logger;
import org.jacorb.orb.*;

/**
 * CORBA DynEnum
 *
 * @author Gerald Brose
 * @version $Id$
 */

public final class DynEnum
   extends DynAny
   implements org.omg.DynamicAny.DynEnum
{
   private int enumValue;
   private int max;
   private String [] member_names;

   DynEnum( org.omg.DynamicAny.DynAnyFactory dynFactory,
            org.omg.CORBA.TypeCode type,
            org.omg.CORBA.ORB orb,
            Logger logger)
     throws TypeMismatch
  {
       super(dynFactory, orb, logger);

       org.omg.CORBA.TypeCode _type = TypeCode.originalType( type );

       if( _type.kind().value() != org.omg.CORBA.TCKind._tk_enum )
       {
           throw new TypeMismatch();
       }

       typeCode = _type;
       pos = -1;
       enumValue = 0;

       try
       {
           member_names = new String[ type().member_count()];
           max = member_names.length;
           for( int i = 0; i < member_names.length; i++ )
           {
               member_names[i] = type().member_name(i);
           }
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


   /**
    * Overrides  from_any() in DynAny
    */

   public void from_any( org.omg.CORBA.Any value )
      throws InvalidValue, TypeMismatch
   {
      checkDestroyed ();
      if( ! value.type().equivalent( type()) )
      {
         throw new TypeMismatch();
      }

      typeCode = TypeCode.originalType( value.type() );

      try
      {
         enumValue = value.create_input_stream().read_long();
         member_names = new String[ type().member_count()];
         max = member_names.length;
         for( int i = 0; i < member_names.length; i++ )
         {
            member_names[i] = type().member_name(i);
         }
      }
      catch( org.omg.CORBA.TypeCodePackage.Bounds e )
      {
          throw unexpectedException(e);
      }
      catch( org.omg.CORBA.TypeCodePackage.BadKind e )
      {
          throw unexpectedException(e);
      }
   }

   /**
    * Overrides  equal() in DynAny
    */

   public boolean equal( org.omg.DynamicAny.DynAny dyn_any )
   {
      checkDestroyed ();
      if( !type().equal( dyn_any.type()))
      {
         return false;
      }

      return DynEnumHelper.narrow( dyn_any).get_as_ulong() == get_as_ulong();
   }


   public org.omg.CORBA.Any to_any()
   {
      checkDestroyed ();
      CDROutputStream out = new CDROutputStream();
      out.write_long( enumValue );

      org.jacorb.orb.Any out_any =
         (org.jacorb.orb.Any)orb.create_any();
      out_any.type(type());
      out_any.read_value( new CDRInputStream(orb, out.getBufferCopy()), type());
      return out_any;
   }

   public java.lang.String get_as_string()
   {
      checkDestroyed ();
      return member_names[ enumValue ];
   }

   public void set_as_string( java.lang.String arg )
      throws InvalidValue
   {
      checkDestroyed ();
      int i = 0;
      while( i < member_names.length && !(arg.equals(member_names[i])) )
      {
         i++;
      }

      if( i < member_names.length )
      {
         set_as_ulong(i);
      }
      else
      {
         throw new InvalidValue();
      }
   }

   public int get_as_ulong()
   {
      checkDestroyed ();
      return enumValue;
   }

   public void set_as_ulong(int arg)
      throws InvalidValue
   {
      checkDestroyed ();
      if( arg < 0 || arg > max )
      {
         throw new InvalidValue();
      }

      enumValue = arg;
   }
}
