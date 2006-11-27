package org.jacorb.test.orb.dynany;

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

import junit.framework.AssertionFailedError;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.jacorb.test.EmptyException;
import org.jacorb.test.EmptyExceptionHelper;
import org.omg.CORBA.StructMember;
import org.omg.CORBA.TCKind;
import org.omg.DynamicAny.NameDynAnyPair;
import org.omg.DynamicAny.NameValuePair;

/**
 * DynAnyEmptyExTest.java
 *
 * DynAny tests for (empty) exception types.
 *
 */

public class DynAnyEmptyExTest extends DynAnyXXXTestCase
{
   private static final String ID = "IDL:test:1.0";
   private static final String NAME = "MyEmptyException";
   private static final StructMember [] MEMBERS = new StructMember [0];


   public static Test suite ()
   {
      return new TestSuite (DynAnyEmptyExTest.class, "Empty Exception DynStruct Tests");
   }


   /**
    * Tests creating a DynAny object from an Any object using the
    * DynAnyFactory object.
    */
   public void testFactoryCreateFromAny ()
   {
      EmptyException type = null;
      org.omg.CORBA.Any any = null;

      type = new EmptyException ();
      any = orb.create_any ();
      EmptyExceptionHelper.insert (any, type);

      createDynAnyFromAny (any);
   }


   /**
    * Tests creating a DynAny object from a TypeCode object using the
    * DynAnyFactory object.
    */
   public void testFactoryCreateFromTypeCode ()
   {
      org.omg.CORBA.TypeCode tc = null;

      tc = orb.create_exception_tc (ID, NAME, MEMBERS);
      createDynAnyFromTypeCode (tc);
   }


   /**
    * Tests creating a DynAny object from a TypeCode object generated from
    * IDL using the DynAnyFactory object.
    */
   public void testFactoryCreateFromIDLTypeCode ()
   {
      org.omg.CORBA.TypeCode tc = null;

      tc = EmptyExceptionHelper.type ();
      createDynAnyFromTypeCode (tc);
   }


   /**
    * Test comparing DynAny values.
    */
   public void testCompareDynAny ()
   {
      String msg;
      EmptyException type;
      org.omg.CORBA.Any any = null;
      org.omg.DynamicAny.DynStruct dynAny = null;
      org.omg.DynamicAny.DynStruct dynAny2 = null;

      type = new EmptyException ();
      any = orb.create_any ();
      EmptyExceptionHelper.insert (any, type);
      dynAny = createDynAnyFromAny (any);
      dynAny2 = createDynAnyFromAny (any);

      msg = "Comparing two equal DynAny values using DynAny::equal failed";
      assertTrue (msg, dynAny.equal (dynAny2));
   }


   /**
    * Test iterating through components of a DynAny.
    */
   public void testIterateDynAny ()
   {
      String msg;
      int compCount = -1;
      boolean seek;
      org.omg.CORBA.TypeCode tc = null;
      org.omg.DynamicAny.DynStruct dynAny = null;

      tc = EmptyExceptionHelper.type ();
      dynAny = createDynAnyFromTypeCode (tc);

      // test the component count
      try
      {
         compCount = dynAny.component_count ();
      }
      catch (Throwable ex)
      {
         // should not be needed, but it prevents compiler errors
         fail ("Unexpected error raised by DynAny::component_count operation");
      }
      msg = "The number of components returned from the ";
      msg += "DynAny::component_count operation is incorrect";
      assertEquals (msg, 0, compCount);

      // test if there is a first component
      msg = "The DynAny::seek operation indicates that a valid component ";
      msg += "exists but the DynAny should have no components";
      seek = dynAny.seek (0);
      assertTrue (msg, !seek);

      // test getting the current component
      try
      {
         dynAny.current_component ();

         msg = "A TypeMismatch exception was not raised by the ";
         msg += "DynAny::current_component operation when trying to access ";
         msg += "the current component of a DynAny with no components";
         fail (msg);
      }
      catch (org.omg.DynamicAny.DynAnyPackage.TypeMismatch ex)
      {
         // success
      }
   }


   /**
    * Test accessing the names and types of members in a DynStruct object.
    */
   public void testAccessStructMembers ()
   {
      String msg;
      String memberName = null;
      TCKind memberKind = null;
      org.omg.CORBA.Any any = null;
      EmptyException exception = null;
      org.omg.DynamicAny.DynStruct dynAny = null;

      exception = new EmptyException ();
      any = orb.create_any ();
      EmptyExceptionHelper.insert (any, exception);
      dynAny = createDynAnyFromAny (any);

      // test getting the name of the current member
      msg = "Failed to throw a TypeMismatch exception when calling ";
      msg += "DynStruct::current_member_name operation on an empty exception";
      try
      {
         memberName = dynAny.current_member_name ();

         fail (msg);
      }
      catch (AssertionFailedError ex)
      {
         throw ex;
      }
      catch (Throwable ex)
      {
         if (!(ex instanceof org.omg.DynamicAny.DynAnyPackage.TypeMismatch))
         {
            fail (msg + ": " + ex);
         }
      }

      // test getting the kind of the current member
      msg = "Failed to throw a TypeMismatch exception when calling ";
      msg += "DynStruct::current_member_kind operation on an empty exception";
      try
      {
         memberKind = dynAny.current_member_kind ();

         fail (msg);
      }
      catch (AssertionFailedError ex)
      {
         throw ex;
      }
      catch (Throwable ex)
      {
         if (!(ex instanceof org.omg.DynamicAny.DynAnyPackage.TypeMismatch))
         {
            fail (msg + ": " + ex);
         }
      }
   }


   /**
    * Test accessing the member Name/Value pairs in a DynStruct object.
    */
   public void testAccessStructNameValuePairs ()
   {
      String msg;
      org.omg.CORBA.Any any = null;
      EmptyException type = null;
      org.omg.DynamicAny.DynStruct dynAny = null;
      org.omg.DynamicAny.NameValuePair [] pairs = null;

      type = new EmptyException ();
      any = orb.create_any ();
      EmptyExceptionHelper.insert (any, type);
      dynAny = createDynAnyFromAny (any);

      // test extracting the name/value pairs as Anys
      pairs = dynAny.get_members ();
      msg = "Returned number of members from DynStruct::get_members operation ";
      msg += "is non-zero";
      assertEquals (msg, 0, pairs.length);

      pairs = new NameValuePair [0];
      try
      {
         dynAny.set_members (pairs);
      }
      catch (Throwable ex)
      {
         msg = "Failed to set DynStruct members using DynStruct::set_members ";
         msg += "operation";
         fail (msg + ": " + ex);
      }
   }


   /**
    * Test accessing the member Name/Value DynAny pairs in a DynStruct object.
    */
   public void testAccessStructDynAnyPairs ()
   {
      String msg;
      org.omg.CORBA.Any any = null;
      EmptyException type = null;
      org.omg.DynamicAny.DynStruct dynAny = null;
      org.omg.DynamicAny.NameDynAnyPair [] pairs = null;

      type = new EmptyException ();
      any = orb.create_any ();
      EmptyExceptionHelper.insert (any, type);
      dynAny = createDynAnyFromAny (any);

      // test extracting the name/value pairs as Anys
      pairs = dynAny.get_members_as_dyn_any ();
      msg = "Returned number of members from DynStruct::get_members operation ";
      msg += "is non-zero";
      assertEquals (msg, 0, pairs.length);

      pairs = new NameDynAnyPair [0];
      try
      {
         dynAny.set_members_as_dyn_any (pairs);
      }
      catch (Throwable ex)
      {
         msg = "Failed to set DynStruct members using DynStruct::set_members ";
         msg += "operation";
         fail (msg + ": " + ex);
      }
   }


   /**
    * Test the exceptions raised while accessing the member Name/Value
    * DynAny pairs in a DynStruct object.
    */
   public void testAccessStructPairsEx ()
   {
      String msg;
      org.omg.CORBA.Any any = null;
      org.omg.CORBA.TypeCode tc = null;
      org.omg.DynamicAny.DynStruct dynAny = null;
      org.omg.DynamicAny.NameValuePair [] pairs = null;

      tc = orb.create_exception_tc (ID, NAME, MEMBERS);
      dynAny = createDynAnyFromTypeCode (tc);

      // test inserting a sequence with the wrong number of members
      pairs = new NameValuePair [1];
      any = orb.create_any ();
      any.insert_char ('a');
      pairs [0] = new NameValuePair ("name", any);

      msg = "Failed to raise an InvalidValue exception when passing in a ";
      msg += "name/value sequence containing too many members";
      try
      {
         dynAny.set_members (pairs);

         fail (msg);
      }
      catch (org.omg.DynamicAny.DynAnyPackage.InvalidValue ex)
      {
         // success
      }
      catch (org.omg.DynamicAny.DynAnyPackage.TypeMismatch ex)
      {
         fail (msg + ": " + ex);
      }
   }


   /**
    * Test obtaining the TypeCode associated with a DynAny object.
    */
   public void testDynAnyTypeCode ()
   {
      String msg;
      org.omg.CORBA.TypeCode tc = null;
      org.omg.DynamicAny.DynStruct dynAny = null;

      tc = orb.create_exception_tc (ID, NAME, MEMBERS);
      dynAny = createDynAnyFromTypeCode (tc);

      msg = "Incorrect TypeCode retrieved from DynAny::type operation";
      assertTrue (msg, dynAny.type ().equal (tc));
   }


   /**
    * Test initializing a DynAny object from another DynAny object.
    */
   public void testInitDynAnyFromDynAny ()
   {
      String msg;
      EmptyException type;
      org.omg.CORBA.Any any = null;
      org.omg.CORBA.TypeCode tc = null;
      org.omg.DynamicAny.DynStruct dynAny = null;
      org.omg.DynamicAny.DynStruct dynAny2 = null;

      tc = EmptyExceptionHelper.type ();
      dynAny = createDynAnyFromTypeCode (tc);

      type = new EmptyException ();
      any = orb.create_any ();
      EmptyExceptionHelper.insert (any, type);
      dynAny2 = createDynAnyFromAny (any);

      msg = "Failed to initialize a DynAny object from another DynAny ";
      msg += "object using the DynAny::assign operation";
      try
      {
         dynAny.assign (dynAny2);
      }
      catch (Throwable ex)
      {
         fail (msg + ": " + ex);
      }
      assertTrue (msg, dynAny.equal (dynAny2));
   }


   /**
    * Test initializing a DynAny object from an Any value.
    */
   public void testInitDynAnyFromAny ()
   {
      String msg;
      EmptyException type;
      org.omg.CORBA.Any any = null;
      org.omg.CORBA.TypeCode tc = null;
      org.omg.DynamicAny.DynStruct dynAny = null;
      org.omg.DynamicAny.DynStruct dynAny2 = null;

      tc = EmptyExceptionHelper.type ();
      dynAny = createDynAnyFromTypeCode (tc);

      type = new EmptyException ();
      any = orb.create_any ();
      EmptyExceptionHelper.insert (any, type);
      dynAny2 = createDynAnyFromAny (any);

      msg = "Failed to initialize a DynAny object from an Any object ";
      msg += "using the DynAny::from_any operation";
      try
      {
         dynAny.from_any (any);
      }
      catch (Throwable ex)
      {
         fail (msg + ": " + ex);
      }
      assertTrue (msg, dynAny.equal (dynAny2));
   }


   /**
    * Test that a TypeMismatch exception is raised if there is a type
    * mismatch between the DynAny and Any types in an assignment.
    */
   public void testInitFromAnyTypeMismatchEx ()
   {
      String msg;
      org.omg.CORBA.Any any = null;
      org.omg.CORBA.TypeCode tc = null;
      org.omg.DynamicAny.DynStruct dynAny = null;

      any = orb.create_any ();
      any.insert_string ("Hello");

      tc = orb.create_exception_tc (ID, NAME, MEMBERS);
      dynAny = createDynAnyFromTypeCode (tc);

      msg = "TypeMismatch exception not thrown by DynAny::from_any ";
      msg += "operation when DynAny and Any operands have different types";
      try
      {
         dynAny.from_any (any);

         fail (msg);
      }
      catch (org.omg.DynamicAny.DynAnyPackage.TypeMismatch ex)
      {
         // success
      }
      catch (org.omg.DynamicAny.DynAnyPackage.InvalidValue ex)
      {
         fail (msg + ": " + ex);
      }
   }


   /**
    * Test generating an Any value from a DynAny object.
    */
   public void testGenerateAnyFromDynAny ()
   {
      String msg;
      org.omg.CORBA.Any any = null;
      org.omg.CORBA.TypeCode tc = null;
      org.omg.DynamicAny.DynStruct dynAny = null;
      org.omg.DynamicAny.DynStruct dynAny2 = null;

      tc = EmptyExceptionHelper.type ();
      dynAny = createDynAnyFromTypeCode (tc);

      any = orb.create_any ();
      any = dynAny.to_any ();
      dynAny2 = createDynAnyFromAny (any);

      msg = "The DynAny::to_any operation failed to create an Any ";
      msg += "object with the same value as the DynAny object";
      assertTrue (msg, dynAny.equal (dynAny2));
   }


   /**
    * Test destroying a DynAny object.
    */
   public void testDestroyDynAny ()
   {
      String msg;
      EmptyException type;
      org.omg.CORBA.Any any = null;
      org.omg.DynamicAny.DynStruct dynAny = null;

      type = new EmptyException ();
      any = orb.create_any ();
      EmptyExceptionHelper.insert (any, type);
      dynAny = createDynAnyFromAny (any);
      dynAny.destroy ();

      try
      {
         dynAny.type ();

         msg = "Failed to destroy DynAny using DynAny::destroy operation - ";
         msg += "calling DynAny::type operation on a destroyed DynAny object ";
         msg += "did not raise OBJECT_NOT_EXIST exception";
         fail (msg);
      }
      catch (org.omg.CORBA.OBJECT_NOT_EXIST ex)
      {
         // success
      }

      msg = "Failed to destroy DynAny using DynAny::destroy operation - ";
      msg += "calling DynAny::current_component operation on a destroyed ";
      msg += "DynAny object did not raise OBJECT_NOT_EXIST exception";
      try
      {
         dynAny.current_component ();

         fail (msg);
      }
      catch (org.omg.CORBA.OBJECT_NOT_EXIST ex)
      {
         // success
      }
      catch (org.omg.DynamicAny.DynAnyPackage.TypeMismatch ex)
      {
         fail (msg + ": " + ex);
      }
   }


   /**
    * Test creating a copy of a DynAny object.
    */
   public void testCopyDynAny ()
   {
      String msg;
      org.omg.CORBA.TypeCode tc = null;
      org.omg.DynamicAny.DynStruct dynAny = null;
      org.omg.DynamicAny.DynStruct dynAny2 = null;

      tc = EmptyExceptionHelper.type ();
      dynAny = createDynAnyFromTypeCode (tc);
      dynAny2 = (org.omg.DynamicAny.DynStruct) dynAny.copy ();

      msg = "The DynAny object created with the DynAny::copy operation ";
      msg += "is not equal to the DynAny object it was copied from";
      assertTrue (msg, dynAny.equal (dynAny2));
   }

   /**
    * Create a DynAny object from an Any object.
    */
   private org.omg.DynamicAny.DynStruct createDynAnyFromAny
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


   /**
    * Create a DynAny object from a TypeCode object.
    */
   private org.omg.DynamicAny.DynStruct createDynAnyFromTypeCode
      (org.omg.CORBA.TypeCode tc)
   {
      String msg;
      org.omg.DynamicAny.DynStruct dynAny = null;

      try
      {
         dynAny = (org.omg.DynamicAny.DynStruct)
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

}
