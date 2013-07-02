package org.jacorb.test.bugs.bug495;

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

import junit.framework.Test;
import junit.framework.TestSuite;
import org.jacorb.test.ArrayTypeHelper;
import org.jacorb.test.Bound;
import org.jacorb.test.StructType;
import org.jacorb.test.StructTypeHelper;
import org.jacorb.test.orb.dynany.DynAnyXXXTestCase;
import org.omg.CORBA.StringSeqHelper;
import org.omg.CORBA.TypeCode;


public class Bug495Test extends DynAnyXXXTestCase
{
   public static Test suite ()
   {
      return new TestSuite (Bug495Test.class, "Bug495 Tests");
   }

   /**
    * Test current_component returns the same object.
    */
   public void testCurrentComponentSequenceDynAny () throws Exception
   {
      TypeCode tc = null;
      org.omg.DynamicAny.DynSequence dynAny = null;

      tc = StringSeqHelper.type();
      dynAny = createDynAnyFromTypeCode (tc);

      dynAny.set_length(1);
      dynAny.current_component().insert_string("hello");
      if ( ! dynAny.current_component().get_string().equals("hello"))
      {
          fail ("Current_component should return the same object");
      }
   }


   /**
    * Test current_component returns the same object.
    */
   public void testCurrentComponentArrayDynAny () throws Exception
   {
      org.omg.DynamicAny.DynArray dynAny = null;

      org.omg.CORBA.Any any = orb.create_any ();
      ArrayTypeHelper.insert (any, getIntArray() );
      dynAny = createArrayDynAnyFromAny (any);

      dynAny.current_component().insert_long(100);
      if ( dynAny.current_component().get_long() != 100)
      {
          fail ("Current_component should return the same object");
      }
   }


   /**
    * Test current_component returns the same object.
    */
   public void testCurrentComponentStructDynAny () throws Exception
   {
      StructType type = null;
      org.omg.CORBA.Any any = null;
      org.omg.DynamicAny.DynStruct dynAny = null;

      type = new StructType (1, "Hello");
      any = orb.create_any ();
      StructTypeHelper.insert (any, type);
      dynAny = createStructDynAnyFromAny (any);

      dynAny.current_component().insert_long(100);
      if ( dynAny.current_component().get_long() != 100)
      {
          fail ("Current_component should return the same object");
      }
   }


   /**
    * Create a DynAny object from a TypeCode object.
    */
   private org.omg.DynamicAny.DynSequence createDynAnyFromTypeCode
      (org.omg.CORBA.TypeCode tc)
   {
      String msg;
      org.omg.DynamicAny.DynSequence dynAny = null;

      try
      {
         dynAny = (org.omg.DynamicAny.DynSequence)
            factory.create_dyn_any_from_type_code (tc);
      }
      catch (Throwable ex)
      {
         msg = "Factory failed to create DynAny from TypeCode using ";
         msg += "DynAny::create_dyn_any_from_type_code operation: " + ex;
         fail (msg);
      }
      return dynAny;
   }


   /**
    * Create a DynAny object from an Any object.
    */
   private org.omg.DynamicAny.DynStruct createStructDynAnyFromAny
      (org.omg.CORBA.Any any)
   {
      String msg;
      org.omg.DynamicAny.DynStruct dynAny = null;

      try
      {
         dynAny = (org.omg.DynamicAny.DynStruct) factory.create_dyn_any (any);
      }
      catch (Throwable ex)
      {
         msg = "Factory failed to create DynAny from Any using ";
         msg += "DynAny::create_dyn_any operation: " + ex;
         fail (msg);
      }
      return dynAny;
   }

    private final org.omg.DynamicAny.DynArray createArrayDynAnyFromAny
      (org.omg.CORBA.Any any)
   {
      String msg;
      org.omg.DynamicAny.DynArray dynAny = null;

      try
      {
         dynAny = (org.omg.DynamicAny.DynArray) factory.create_dyn_any (any);
      }
      catch (Throwable ex)
      {
         msg = "Factory failed to create DynAny from Any using ";
         msg += "DynAny::create_dyn_any operation: " + ex;
         fail (msg);
      }
      return dynAny;
   }

   /**
    * Create an array of integers of fixed length.
    */
   private static int [] getIntArray ()
   {
      int [] type = new int [Bound.value];
      for (int i = 0; i < Bound.value; i++)
      {
         type [i] = i;
      }
      return type;
   }
}
