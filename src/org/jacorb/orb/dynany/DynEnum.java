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
 * CORBA DynEnum
 *
 * @author (c) Gerald Brose, FU Berlin 1999
 * $Id$
 */

public final class DynEnum
   extends DynAny
   implements org.omg.DynamicAny.DynEnum
{
   private int enum_value;
   private int max;
   private String [] member_names;

   DynEnum( org.omg.DynamicAny.DynAnyFactory dynFactory,
            org.omg.CORBA.TypeCode tc)
      throws InvalidValue, TypeMismatch
   {
      org.jacorb.orb.TypeCode _type = 
         ((org.jacorb.orb.TypeCode)tc).originalType();

      if( _type.kind().value() != org.omg.CORBA.TCKind._tk_enum )
         throw new TypeMismatch();

      type = _type;

      this.orb = org.omg.CORBA.ORB.init();
      this.dynFactory = dynFactory;
      pos = -1;
      enum_value = 0;

      try
      {	  
         member_names = new String[ type().member_count()];
         max = member_names.length;
         for( int i = 0; i < member_names.length; i++ )
            member_names[i] = type().member_name(i);
      }
      catch( org.omg.CORBA.TypeCodePackage.BadKind bk )
      {	    
         // should not happen
         bk.printStackTrace();
      }
      catch( org.omg.CORBA.TypeCodePackage.Bounds b )
      {	    
         // should not happen
         b.printStackTrace();
      }    
   }


   /**
    * @overrides  from_any() in DynAny
    */

   public void from_any( org.omg.CORBA.Any value ) 
      throws InvalidValue, TypeMismatch
   {
      checkDestroyed ();
      if( ! value.type().equivalent( type()) )
         throw new TypeMismatch();

      type = ((org.jacorb.orb.TypeCode)value.type()).originalType();

      try
      {	    
         enum_value = value.create_input_stream().read_long();
         member_names = new String[ type().member_count()];
         max = member_names.length;
         for( int i = 0; i < member_names.length; i++ )
         {
            member_names[i] = type().member_name(i);
         }
      }
      catch( org.omg.CORBA.TypeCodePackage.Bounds b )
      {	    
         // should not happen
         b.printStackTrace();
      }
      catch( org.omg.CORBA.TypeCodePackage.BadKind bk )
      {
         // should not happen anymore
         bk.printStackTrace();
      }
   }

   /**
    * @overrides  equal() in DynAny
    */

   public boolean equal( org.omg.DynamicAny.DynAny dyn_any )
   {
      checkDestroyed ();
      if( !type().equal( dyn_any.type()))
         return false;

      return DynEnumHelper.narrow( dyn_any).get_as_ulong() == get_as_ulong();
   }


   public org.omg.CORBA.Any to_any() 
   {
      checkDestroyed ();
      CDROutputStream os = new CDROutputStream();
      os.write_long( enum_value );

      org.jacorb.orb.Any out_any = 
         (org.jacorb.orb.Any)org.omg.CORBA.ORB.init().create_any();
      out_any.type(type());	
      out_any.read_value( new CDRInputStream(orb, os.getBufferCopy()), type());
      return out_any;
   }

   public java.lang.String get_as_string()
   {
      checkDestroyed ();
      return member_names[ enum_value ];
   }
	
   public void set_as_string( java.lang.String arg )
      throws InvalidValue
   {
      checkDestroyed ();
      int i = 0;
      while( i < member_names.length && !(arg.equals(member_names[i])) )
         i++;

      if( i < member_names.length )
         set_as_ulong(i);
      else
         throw new InvalidValue();
   }

   public int get_as_ulong()
   {
      checkDestroyed ();
      return enum_value;
   }
    
   public void set_as_ulong(int arg)
      throws InvalidValue
   {
      checkDestroyed ();
      if( arg < 0 || arg > max )
         throw new InvalidValue();

      enum_value = arg;
   }


}
