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

import org.omg.CORBA.*;
import org.omg.DynamicAny.*;
import org.omg.DynamicAny.DynAnyPackage.*;

/**
 * CORBA DynAny
 *
 * @author (c) Gerald Brose, FU Berlin 1999
 * $Id$
 *
 */


public class DynAny
   extends org.omg.CORBA.LocalObject
   implements org.omg.DynamicAny.DynAny
{
   protected org.omg.DynamicAny.DynAnyFactory dynFactory;
   protected org.omg.CORBA.TypeCode type;
   protected int pos = -1;
   protected int limit = 0;
   protected org.omg.CORBA.ORB orb;

   /* our representation of a primitive type any is the any itself */
   private org.omg.CORBA.Any anyRepresentation;


   protected DynAny()
   {}

   DynAny( org.omg.DynamicAny.DynAnyFactory dynFactory,  
           org.omg.CORBA.TypeCode _type)
      throws TypeMismatch
   {
      this.orb = org.omg.CORBA.ORB.init();
      this.dynFactory = dynFactory;
      type = ((org.jacorb.orb.TypeCode)_type).originalType();
      anyRepresentation = defaultValue( type );
   }

   public org.omg.CORBA.TypeCode type()
   {
      checkDestroyed ();
      return type;       
   }

   public void assign(org.omg.DynamicAny.DynAny dyn_any) 
      throws TypeMismatch
   {
      checkDestroyed ();
      if( dyn_any.type().equivalent( this.type()))
      {
         try
         {
            from_any( dyn_any.to_any() );
         }
         catch( InvalidValue iv )
         {
            // should not happen
         }
      }
      else
         throw new TypeMismatch();
   }

   public boolean equal( org.omg.DynamicAny.DynAny dyn_any )
   {
      checkDestroyed ();
      org.jacorb.util.Debug.myAssert( anyRepresentation != null, "anyRepresentation not initialized");
      return dyn_any.to_any().equal( anyRepresentation );
   }

   public void from_any(org.omg.CORBA.Any value) 
      throws InvalidValue, TypeMismatch
   {
      checkDestroyed ();
      if( ! value.type().equivalent( type()) )
         throw new TypeMismatch();

      type = ((org.jacorb.orb.TypeCode)value.type()).originalType();

      try
      {
         anyRepresentation = (org.jacorb.orb.Any)orb.create_any();
         anyRepresentation.read_value( value.create_input_stream(), type());
      }
      catch( Exception e)
      {
         e.printStackTrace();
         throw new InvalidValue();
      }
   }
    
   public org.omg.CORBA.Any to_any() 
   {
      checkDestroyed ();
      org.jacorb.orb.Any out_any = (org.jacorb.orb.Any)orb.create_any();
      out_any.type( type());
      out_any.read_value( anyRepresentation.create_input_stream(), type());
      return out_any;
   }

   public void destroy()
   {
      checkDestroyed ();
      anyRepresentation = null;
      type = null;
   }    

   public org.omg.DynamicAny.DynAny copy()
   {
      checkDestroyed ();
      try
      {
         return dynFactory.create_dyn_any( to_any() );
      } 
      catch( org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCode tm )
      {
         tm.printStackTrace();
      }       
      return null;
   }

   /**
    * returns the DynAny's internal any representation, 
    * overwritten in subclasses that represent constructed
    * types and need to traverse structures.
    */

   protected org.omg.CORBA.Any getRepresentation()
   {
      return anyRepresentation;
   }


   public void insert_boolean( boolean value ) 
      throws TypeMismatch
   {
      checkDestroyed ();
      org.omg.CORBA.Any any = getRepresentation();
      if( any.type().kind() != org.omg.CORBA.TCKind.tk_boolean)
         throw new TypeMismatch ();
      any.insert_boolean(value);
   }


   public void insert_octet( byte value ) 
      throws TypeMismatch
   {
      checkDestroyed ();
      org.omg.CORBA.Any any = getRepresentation();
      if( any.type().kind() != org.omg.CORBA.TCKind.tk_octet)
         throw new TypeMismatch ();
      any.insert_octet(value);
   }


   public void insert_char(char value) 
      throws TypeMismatch
   {
      checkDestroyed ();
      org.omg.CORBA.Any any = getRepresentation();
      if( any.type().kind() != org.omg.CORBA.TCKind.tk_char)
         throw new TypeMismatch ();
      any.insert_char(value);
   }


   public void insert_short(short value) 
      throws TypeMismatch
   {
      checkDestroyed ();
      org.omg.CORBA.Any any = getRepresentation();
      if( any.type().kind() != org.omg.CORBA.TCKind.tk_short)
         throw new TypeMismatch ();
      any.insert_short(value);
   }


   public void insert_ushort(short value) 
      throws TypeMismatch
   {
      checkDestroyed ();
      org.omg.CORBA.Any any = getRepresentation();
      if( any.type().kind() != org.omg.CORBA.TCKind.tk_ushort)
         throw new TypeMismatch ();
      any.insert_ushort(value);
   }


   public void insert_long(int value) 
      throws TypeMismatch
   {
      checkDestroyed ();
      org.omg.CORBA.Any any = getRepresentation();
      if( any.type().kind() != org.omg.CORBA.TCKind.tk_long)
         throw new TypeMismatch ();
      any.insert_long(value);
   }


   public void insert_ulong(int value) 
      throws TypeMismatch
   {
      checkDestroyed ();
      org.omg.CORBA.Any any = getRepresentation();
      if(  any.type().kind() != org.omg.CORBA.TCKind.tk_ulong)
         throw new TypeMismatch ();
      any.insert_ulong(value);
   }


   public void insert_float(float value) 
      throws TypeMismatch
   {
      checkDestroyed ();
      org.omg.CORBA.Any any = getRepresentation();
      if(  any.type().kind() != org.omg.CORBA.TCKind.tk_float)
         throw new TypeMismatch ();
      any.insert_float(value);
   }


   public void insert_double(double value) 
      throws TypeMismatch
   {
      checkDestroyed ();
      org.omg.CORBA.Any any = getRepresentation();
      if( any.type().kind() != org.omg.CORBA.TCKind.tk_double)
         throw new TypeMismatch ();
      any.insert_double(value);
   }


   public void insert_string(java.lang.String value) 
      throws TypeMismatch
   {
      checkDestroyed ();
      org.omg.CORBA.Any any = getRepresentation();
      if( any.type().kind() != org.omg.CORBA.TCKind.tk_string)
         throw new TypeMismatch ();
      any.insert_string(value);
   }


   public void insert_reference(org.omg.CORBA.Object value) 
      throws TypeMismatch
   {
      checkDestroyed ();
      org.omg.CORBA.Any any = getRepresentation();
      if( any.type().kind() != org.omg.CORBA.TCKind.tk_objref)
         throw new TypeMismatch ();
      any.insert_Object(value);
   }


   public void insert_typecode(org.omg.CORBA.TypeCode value) 
      throws TypeMismatch
   {
      checkDestroyed ();
      org.omg.CORBA.Any any = getRepresentation();
      if(  any.type().kind() != org.omg.CORBA.TCKind.tk_TypeCode)
         throw new TypeMismatch ();
      any.insert_TypeCode(value);
   }

   public void insert_longlong(long value) 
      throws TypeMismatch
   {
      checkDestroyed ();
      org.omg.CORBA.Any any = getRepresentation();
      if(  any.type().kind() != org.omg.CORBA.TCKind.tk_longlong)
         throw new TypeMismatch ();
      any.insert_longlong(value);
   }

   public void insert_ulonglong(long value) 
      throws TypeMismatch
   {
      checkDestroyed ();
      org.omg.CORBA.Any any = getRepresentation();
      if(  any.type().kind() != org.omg.CORBA.TCKind.tk_ulonglong)
         throw new TypeMismatch ();
      any.insert_ulonglong(value);
   }

   public void insert_wchar(char value) 
      throws TypeMismatch
   {
      checkDestroyed ();
      org.omg.CORBA.Any any = getRepresentation();
      if( any.type().kind() != org.omg.CORBA.TCKind.tk_wchar)
         throw new TypeMismatch ();
      any.insert_wchar(value);
   }

   public void insert_wstring(java.lang.String value) 
      throws TypeMismatch
   {
      checkDestroyed ();
      org.omg.CORBA.Any any = getRepresentation();
      if( any.type().kind() != org.omg.CORBA.TCKind.tk_wstring)
         throw new TypeMismatch ();
      any.insert_wstring(value);
   }

   public void insert_any(org.omg.CORBA.Any value) 
      throws TypeMismatch
   {
      checkDestroyed ();
      org.omg.CORBA.Any any = getRepresentation();
      if(  any.type().kind() != org.omg.CORBA.TCKind.tk_any)
         throw new TypeMismatch ();
      any.insert_any(value);
   }
    

   public void insert_dyn_any(org.omg.DynamicAny.DynAny value) 
      throws TypeMismatch
   {
      checkDestroyed ();
      insert_any (value.to_any ());
   }


   public boolean get_boolean() throws TypeMismatch
   {
      checkDestroyed ();
      org.omg.CORBA.Any any = getRepresentation();
      try
      {           
         return any.extract_boolean();
      }
      catch( org.omg.CORBA.BAD_OPERATION b )
      {
         throw new TypeMismatch();
      }
   }

   public byte get_octet() throws TypeMismatch
   {
      checkDestroyed ();
      org.omg.CORBA.Any any = getRepresentation();
      try
      {
         return any.extract_octet();
      }
      catch( org.omg.CORBA.BAD_OPERATION b )
      {
         throw new TypeMismatch();
      }
   }

   public char get_char() throws TypeMismatch
   {
      checkDestroyed ();
      org.omg.CORBA.Any any = getRepresentation();
      try
      {
         return any.extract_char();
      }
      catch( org.omg.CORBA.BAD_OPERATION b )
      {
         throw new TypeMismatch();
      }
   }

   public short get_short() throws TypeMismatch
   {
      checkDestroyed ();
      org.omg.CORBA.Any any = getRepresentation();
      try
      {
         return any.extract_short();
      }
      catch( org.omg.CORBA.BAD_OPERATION b )
      {
         throw new TypeMismatch();
      }
   }

   public short get_ushort() throws TypeMismatch
   {
      checkDestroyed ();
      org.omg.CORBA.Any any = getRepresentation();
      try
      {
         return any.extract_ushort();
      }
      catch( org.omg.CORBA.BAD_OPERATION b )
      {
         throw new TypeMismatch();
      }
   }

   public int get_long() throws TypeMismatch
   {
      checkDestroyed ();
      org.omg.CORBA.Any any = getRepresentation();
      try
      {
         return any.extract_long();
      }
      catch( org.omg.CORBA.BAD_OPERATION b )
      {
         throw new TypeMismatch();
      }
   }

   public int get_ulong() throws TypeMismatch
   {
      checkDestroyed ();
      org.omg.CORBA.Any any = getRepresentation();
      try
      {
         return any.extract_ulong();
      }
      catch( org.omg.CORBA.BAD_OPERATION b )
      {
         throw new TypeMismatch();
      }
   }
   public float get_float() throws TypeMismatch
   {
      checkDestroyed ();
      org.omg.CORBA.Any any = getRepresentation();
      try
      {
         return any.extract_float();
      }
      catch( org.omg.CORBA.BAD_OPERATION b )
      {
         throw new TypeMismatch();
      }
   }

   public double get_double() throws TypeMismatch
   {
      checkDestroyed ();
      org.omg.CORBA.Any any = getRepresentation();
      try
      {
         return any.extract_double();
      }
      catch( org.omg.CORBA.BAD_OPERATION b )
      {
         throw new TypeMismatch();
      }
   }

   public java.lang.String get_string() throws TypeMismatch
   {
      checkDestroyed ();
      org.omg.CORBA.Any any = getRepresentation();
      try
      {
         return any.extract_string();
      }
      catch( org.omg.CORBA.BAD_OPERATION b )
      {
         throw new TypeMismatch();
      }
   }

   public org.omg.CORBA.Object get_reference() throws TypeMismatch
   {
      checkDestroyed ();
      org.omg.CORBA.Any any = getRepresentation();
      try
      {   
         return any.extract_Object();
      }
      catch( org.omg.CORBA.BAD_OPERATION b )
      {
         throw new TypeMismatch();
      }
   }

   public org.omg.CORBA.TypeCode get_typecode() throws TypeMismatch
   {
      checkDestroyed ();
      org.omg.CORBA.Any any = getRepresentation();
      try
      {
         return any.extract_TypeCode();
      }
      catch( org.omg.CORBA.BAD_OPERATION b )
      {
         throw new TypeMismatch();
      }
   }

   public long get_longlong() throws TypeMismatch
   {
      checkDestroyed ();
      org.omg.CORBA.Any any = getRepresentation();
      try
      {
         return any.extract_longlong();
      }
      catch( org.omg.CORBA.BAD_OPERATION b )
      {
         throw new TypeMismatch();
      }
   }

   public long get_ulonglong() throws TypeMismatch
   {
      checkDestroyed ();
      org.omg.CORBA.Any any = getRepresentation();
      try
      {
         return any.extract_ulonglong();
      }
      catch( org.omg.CORBA.BAD_OPERATION b )
      {
         throw new TypeMismatch();
      }
   }

   public char get_wchar() throws TypeMismatch
   {
      checkDestroyed ();
      org.omg.CORBA.Any any = getRepresentation();
      try
      {
         return any.extract_wchar();
      }
      catch( org.omg.CORBA.BAD_OPERATION b )
      {
         throw new TypeMismatch();
      }
   }

   public java.lang.String get_wstring() throws TypeMismatch
   {
      checkDestroyed ();
      org.omg.CORBA.Any any = getRepresentation();
      try
      {
         return any.extract_wstring();
      }
      catch( org.omg.CORBA.BAD_OPERATION b )
      {
         throw new TypeMismatch();
      }
   }

   public org.omg.CORBA.Any get_any() throws TypeMismatch
   {
      checkDestroyed ();
      org.omg.CORBA.Any any = getRepresentation();
      try
      {
         return any.extract_any();
      }
      catch( org.omg.CORBA.BAD_OPERATION b )
      {
         throw new TypeMismatch();
      }
   }

   public org.omg.DynamicAny.DynAny get_dyn_any() throws TypeMismatch
   {
      checkDestroyed ();
      try
      {
         return dynFactory.create_dyn_any( get_any () );
      } 
      catch( org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCode tm )
      {
         tm.printStackTrace();
      }       
      return null;
   }

   public int component_count()
   {
      checkDestroyed ();
      return limit;
   }

   public org.omg.DynamicAny.DynAny current_component()
      throws TypeMismatch
   {
      checkDestroyed ();
      throw new TypeMismatch();
   }

   public boolean next()
   {
      checkDestroyed ();
      if( pos < limit-1 )
      {
         pos++;
         return true;
      }
      pos = -1;
      return false;
   }

   public boolean seek(int index)    
   {
      checkDestroyed ();
      if( index < 0 )
      {
         pos = -1;
         return false;
      }
      if( index < limit )
      {
         pos = index;
         return true;
      }
      pos = -1;

      return false;
   }

   public void rewind()
   {
      checkDestroyed ();
      seek(0);
   }


   protected void checkDestroyed ()
   {
      if (anyRepresentation == null && type == null)
      {
         throw new OBJECT_NOT_EXIST ();
      }
   }

   private org.omg.CORBA.Any defaultValue(org.omg.CORBA.TypeCode tc)
      throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch
   {
      org.omg.CORBA.Any _any = orb.create_any();
      _any.type( tc );
      switch( tc.kind().value() )
      {
      case TCKind._tk_boolean : 
         _any.insert_boolean(false);
         break;
      case TCKind._tk_short: 
         _any.insert_short( (short)0 );
         break;
      case TCKind._tk_ushort:  
         _any.insert_ushort( (short)0 );
         break;
      case TCKind._tk_long:  
         _any.insert_long( 0 );
         break;
      case TCKind._tk_double:  
         _any.insert_double( 0 );
         break;
      case TCKind._tk_ulong:  
         _any.insert_long( 0 );
         break;
      case TCKind._tk_longlong:  
         _any.insert_longlong(0);
         break;
      case TCKind._tk_ulonglong: 
         _any.insert_ulonglong(0);
         break;
      case TCKind._tk_float: 
         _any.insert_float(0);
         break;
      case TCKind._tk_char: 
         _any.insert_char((char)0);
         break;
      case TCKind._tk_wchar: 
         _any.insert_wchar((char)0);
         break;
      case TCKind._tk_octet: 
         _any.insert_octet((byte)0);
         break;
      case TCKind._tk_string: 
         _any.insert_string("");
         break;
      case TCKind._tk_wstring: 
         _any.insert_wstring("");
         break;
      case TCKind._tk_TypeCode: 
         _any.insert_TypeCode( orb.get_primitive_tc( TCKind.tk_null ) );
         break;
      case TCKind._tk_any:
         org.jacorb.orb.Any a = (org.jacorb.orb.Any)orb.create_any();
         a.type( orb.get_primitive_tc( TCKind.tk_null ));
         _any.insert_any(a);
         break;
      default: 
         throw new TypeMismatch();
      }
      return _any;
   }

}
