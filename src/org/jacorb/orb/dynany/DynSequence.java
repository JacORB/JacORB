package org.jacorb.orb.dynany;

/*
 *        JacORB  - a free Java ORB
 *
 *   Copyright (C) 1997-2003  Gerald Brose.
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

import org.omg.CORBA.INTERNAL;
import org.jacorb.orb.*;
import java.util.*;

/**
 * CORBA DynSequence
 *
 * @author (c) Gerald Brose, FU Berlin 1999
 * @version $Id$
 *
 */

public final class DynSequence
   extends DynAny
   implements org.omg.DynamicAny.DynSequence
{
   private Vector members;
   private int length;
   private org.omg.CORBA.TypeCode elementType;

   DynSequence( org.omg.DynamicAny.DynAnyFactory dynFactory,
                org.omg.CORBA.TypeCode tc )
      throws InvalidValue, TypeMismatch
   {
      org.omg.CORBA.TypeCode _type = TypeCode.originalType( tc );

      if( _type.kind() != org.omg.CORBA.TCKind.tk_sequence )
         throw new TypeMismatch();

      try
      {
         type = _type;

         this.orb = org.omg.CORBA.ORB.init();
         this.dynFactory = dynFactory;

         elementType = TypeCode.originalType( type().content_type() );
         limit = type.length();
         length = 0;
         members = new Vector();
      }
      catch( org.omg.CORBA.TypeCodePackage.BadKind bk )
      {
         bk.printStackTrace();
      }
      if (elementType == null)
      {
          throw new INTERNAL ("DynSequence.set_length, elementType null");
      }
   }

   public void from_any( org.omg.CORBA.Any value )
      throws InvalidValue, TypeMismatch
   {
      checkDestroyed ();
      if( ! type().equivalent( value.type() ))
      {
         throw new org.omg.DynamicAny.DynAnyPackage.TypeMismatch();
      }

      try
      {
         type = TypeCode.originalType( value.type() );
         super.from_any( value );

         limit = type().length();

         org.omg.CORBA.portable.InputStream is =
            value.create_input_stream();
         length = is.read_long();

         if( length > 0 )
            pos = 0;

         if( limit != 0 && length > limit )
            throw new InvalidValue();

         members = new Vector(length);
         elementType = TypeCode.originalType( type().content_type() );

         for( int i = 0 ; i < length; i++ )
         {
            Any a = (org.jacorb.orb.Any)orb.create_any();
            a.read_value( is, elementType );
            members.addElement( a );
         }
      }
      catch( org.omg.CORBA.TypeCodePackage.BadKind bk )
      {
         // should not happen anymore
         bk.printStackTrace();
      }
      if (elementType == null)
      {
          throw new INTERNAL ("DynSequence.set_length, elementType null");
      }
   }


   public org.omg.CORBA.Any to_any()
   {
      checkDestroyed ();
      org.jacorb.orb.Any out_any = (org.jacorb.orb.Any)orb.create_any();
      out_any.type( type());

      CDROutputStream os = new CDROutputStream();
      os.write_long( length );

      for( int i = 0; i < length; i++)
      {
         os.write_value( elementType,
                         (CDRInputStream)((Any)members.elementAt(i)).create_input_stream());
      }

      CDRInputStream is = new CDRInputStream( orb, os.getBufferCopy());
      out_any.read_value( is, type());
      return out_any;
   }

   /**
    * Overrides  equal() in DynAny
    */

   public boolean equal( org.omg.DynamicAny.DynAny dyn_any )
   {
      checkDestroyed ();
      if( !type().equal( dyn_any.type())  )
         return false;

      org.omg.DynamicAny.DynSequence other =  DynSequenceHelper.narrow( dyn_any );

      if( other.get_length() != get_length())
         return false;

      org.omg.CORBA.Any[] elements = get_elements();
      org.omg.CORBA.Any[] other_elements = other.get_elements();

      for( int i = 0; i < elements.length; i++ )
      {
         if( ! (elements[i].equal( other_elements[i] )))
            return false;
      }

      return true;
   }

   public int get_length()
   {
      checkDestroyed ();
      return length;
   }


   public void set_length(int len)
      throws InvalidValue
   {
      checkDestroyed ();
      if( limit > 0 && len > limit )
         throw new InvalidValue();

      if( len == 0 )
      {
         members = new Vector();
         pos = -1;
      }

      if (elementType == null)
      {
          throw new INTERNAL ("DynSequence.set_length, elementType null");
      }

      if( len > length )
      {
         try
         {
            for( int i = length; i < len; i++ )
            {
               // create correctly initialized anys
               members.addElement( dynFactory.create_dyn_any_from_type_code( elementType ).to_any() );
            }
         }
         catch( org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCode itc )
         {
            // should neever happen
            itc.printStackTrace();
         }

         if( pos == -1 )
            pos = len - length - 1;
      }
      else if( len < length )
      {
         members.setSize(len);

         if( pos > len )
            pos = -1;
      }
      length = len;
   }


   public org.omg.CORBA.Any[] get_elements()
   {
      checkDestroyed ();
      Any[] result = new Any[ members.size()];
      for( int i = members.size(); i-- > 0; )
         result[i] = (Any)members.elementAt(i);
      return result;
   }


   public void set_elements( org.omg.CORBA.Any[] value )
      throws TypeMismatch, InvalidValue
   {
      checkDestroyed ();
      if( limit > 0 && value.length > limit )
         throw new InvalidValue();

      for( int i = value.length; i-- > 0 ;)
      {
         org.omg.CORBA.TypeCode tc = TypeCode.originalType( value[i].type() );

         if( tc.kind() != elementType.kind() )
            throw new TypeMismatch();
      }

      /** ok now */
      length = value.length;

      members = new Vector();
      for( int i = 0; i < length; i++)
      {
         members.addElement( value[i] );
      }

      if( length > 0 )
         pos = 0;
      else
         pos = -1;

   }

   public org.omg.DynamicAny.DynAny[] get_elements_as_dyn_any()
   {
      checkDestroyed ();
      org.omg.DynamicAny.DynAny[] result =
         new org.omg.DynamicAny.DynAny[ members.size()];
      try
      {
         for( int i = members.size(); i-- > 0; )
            result[i] = dynFactory.create_dyn_any( (Any)members.elementAt(i));
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
      checkDestroyed ();
      org.omg.CORBA.Any [] any_seq = new org.omg.CORBA.Any[value.length];
      for( int i = value.length; i-- > 0; )
         any_seq[i] = value[i].to_any();

      set_elements( any_seq );
   }


   public void destroy()
   {
      super.destroy();
      members.removeAllElements();
      members = null;
      elementType = null;
   }


   /**
    * returns the DynAny's internal any representation,
    * overwrites
    */


   protected org.omg.CORBA.Any getRepresentation()
   {
      return (Any)members.elementAt(pos);
   }


   public boolean next()
   {
      checkDestroyed ();
      if( pos < length-1 )
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
      if( index < length )
      {
         pos = index;
         return true;
      }
      pos = -1;
      return false;
   }

   /* iteration interface */

   public org.omg.DynamicAny.DynAny current_component()
   {
      checkDestroyed ();
      if( pos == -1 )
      {
         return null;
      }
      try
      {
         return dynFactory.create_dyn_any( (Any)members.elementAt(pos) );
      }
      catch( org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCode itc )
      {
         // should never happen
         itc.printStackTrace();
      }
      return null;
   }

   public int component_count()
   {
      checkDestroyed ();
      return get_length();
   }

}
