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

import org.jacorb.orb.*;
import java.util.*;

/**
 * CORBA DynArray
 *
 * @author (c) Gerald Brose, FU Berlin 1999
 * $Id$
 */

public final class DynArray
    extends DynAny
    implements org.omg.DynamicAny.DynArray
{
    private org.omg.CORBA.TypeCode elementType;
    private org.omg.CORBA.Any[] members;

   DynArray( org.omg.DynamicAny.DynAnyFactory dynFactory,
             org.omg.CORBA.TypeCode tc)
      throws InvalidValue, TypeMismatch
   {
      org.jacorb.orb.TypeCode _type = 
         ((org.jacorb.orb.TypeCode)tc).originalType();
      
      if(  _type.kind() != org.omg.CORBA.TCKind.tk_array )
         throw new TypeMismatch();	

      try
      {
         type = _type;
         this.orb = org.omg.CORBA.ORB.init();
         this.dynFactory = dynFactory;
         elementType = ((org.jacorb.orb.TypeCode)type.content_type()).originalType();

         limit = type.length();
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
      checkDestroyed ();
      if( ! type.equivalent( value.type() ))
         throw new org.omg.DynamicAny.DynAnyPackage.TypeMismatch();

      type = ((org.jacorb.orb.TypeCode)value.type()).originalType();

      try
      {	    
         limit = type().length();
         elementType = ((org.jacorb.orb.TypeCode)type.content_type()).originalType();

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
      checkDestroyed ();
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

   /**
     * @overrides  equal() in DynAny
     */

   public boolean equal( org.omg.DynamicAny.DynAny dyn_any )
   {
      checkDestroyed ();
      if( !type().equal( dyn_any.type())  )
         return false;

      org.omg.DynamicAny.DynArray other =  DynArrayHelper.narrow( dyn_any );

      org.omg.CORBA.Any[] elements = get_elements();
      org.omg.CORBA.Any[] other_elements = other.get_elements();

      for( int i = 0; i < elements.length; i++ )
      {
         if( ! (elements[i].equal( other_elements[i] )))
            return false;
      }

      return true;
   }


   public org.omg.CORBA.Any[] get_elements()
   {
      checkDestroyed ();
      return members;
   }


   public void set_elements(org.omg.CORBA.Any[] value) 
      throws TypeMismatch, InvalidValue
   {
      checkDestroyed ();
      if( value.length != limit )
         throw new InvalidValue();

      for( int i = value.length; i-- > 0 ;)
      {
         TypeCode tc = 
            ((org.jacorb.orb.TypeCode)value[i].type()).originalType();

         if( tc.kind() != elementType.kind() )
            throw new TypeMismatch();
      }

      /** ok now */
      members = value;
   }

   public org.omg.DynamicAny.DynAny[] get_elements_as_dyn_any()
   {
      checkDestroyed ();
      org.omg.DynamicAny.DynAny[] result = 
         new org.omg.DynamicAny.DynAny[ members.length ];
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
      checkDestroyed ();
      org.omg.CORBA.Any [] any_seq = 
         new org.omg.CORBA.Any[value.length];
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

   /**
     * returns the DynAny's internal any representation, 
     * @overwrites getRepresentation() in DynAny
     */

   protected org.omg.CORBA.Any getRepresentation()
   {
      return members[pos];
   }

   /* iteration interface */

   public org.omg.DynamicAny.DynAny current_component()
   {	
      checkDestroyed ();
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
