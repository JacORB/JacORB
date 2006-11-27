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

import org.jacorb.test.NonEmptyException;
import org.jacorb.test.NonEmptyExceptionHelper;
import org.omg.CORBA.TCKind;
import org.omg.DynamicAny.NameDynAnyPair;
import org.omg.DynamicAny.NameValuePair;

/**
 * DynAnyNonEmptyExTest.java
 *
 * DynAny tests for (non-empty) exception types.
 *
 */

public class DynAnyNonEmptyExTest extends DynAnyXXXTestCase
{
   private static final String ID = "IDL:test:1.0";
   private static final String NAME = "MyNonEmptyException";


   public static Test suite ()
   {
      return new TestSuite (DynAnyNonEmptyExTest.class, "Non-empty Exception DynStruct Tests");
   }


   /**
    * Tests creating a DynAny object from an Any object using the
    * DynAnyFactory object.
    */
   public void testFactoryCreateFromAny ()
   {
      NonEmptyException type = null;
      org.omg.CORBA.Any any = null;

      type = new NonEmptyException (1, "Hello");
      any = orb.create_any ();
      NonEmptyExceptionHelper.insert (any, type);

      createDynAnyFromAny (any);
   }


   /**
    * Tests creating a DynAny object from a TypeCode object using the
    * DynAnyFactory object.
    */
   public void testFactoryCreateFromTypeCode ()
   {
      org.omg.CORBA.TypeCode tc = null;

      tc = orb.create_exception_tc (ID, NAME, getExceptionMembers ());
      createDynAnyFromTypeCode (tc);
   }


   /**
    * Tests creating a DynAny object from a TypeCode object generated from
    * IDL using the DynAnyFactory object.
    */
   public void testFactoryCreateFromIDLTypeCode ()
   {
      org.omg.CORBA.TypeCode tc = null;

      tc = NonEmptyExceptionHelper.type ();
      createDynAnyFromTypeCode (tc);
   }


   /**
    * Test comparing DynAny values.
    */
   public void testCompareDynAny ()
   {
      String msg;
      NonEmptyException type;
      org.omg.CORBA.Any any = null;
      org.omg.DynamicAny.DynStruct dynAny = null;
      org.omg.DynamicAny.DynStruct dynAny2 = null;

      type = new NonEmptyException (1, "Hello");
      any = orb.create_any ();
      NonEmptyExceptionHelper.insert (any, type);
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
      org.omg.DynamicAny.DynAny compSeek = null;
      org.omg.DynamicAny.DynAny compRewind = null;

      tc = orb.create_exception_tc (ID, NAME, getExceptionMembers ());
      dynAny = createDynAnyFromTypeCode (tc);

      // test the component count
      msg = "The number of components returned from the ";
      msg += "DynAny::component_count method is incorrect";
      try
      {
         compCount = dynAny.component_count ();
      }
      catch (Throwable ex)
      {
         // should not be needed, but it prevents compiler errors
         fail ("Unexpected error raised by DynAny::component_count operation");
      }
      assertEquals (msg, 2, compCount);  // specific to IDL

      // seek an invalid position
      msg = "The DynAny::seek operation indicates a valid current position ";
      msg += "when the current position should be invalid";
      seek = dynAny.seek (-1);
      assertTrue (msg, !seek);

      // seek the first position
      msg = "The DynAny::seek operation indicates an invalid current position ";
      msg += "when the current position should be valid";
      seek = dynAny.seek (0);
      assertTrue (msg, seek);

      // extract a value from the current position
      try
      {
         compSeek = dynAny.current_component ();
      }
      catch (Throwable ex)
      {
         msg = "Failed to get the current component using the ";
         msg += "DynAny::current_component operation after calling the ";
         msg += "DynAny::seek operation";
         fail (msg);
      }

      // seek the next position
      msg = "The DynAny::next operation indicates an invalid current position ";
      msg += "when the current position should be valid";
      seek = dynAny.next ();
      assertTrue (msg, seek);

      // return to the first position
      dynAny.rewind ();

      // extract a value from the current position
      try
      {
         compRewind = dynAny.current_component ();
      }
      catch (Throwable ex)
      {
         msg = "Failed to get the current component using the ";
         msg += "DynAny::current_component operation after calling the ";
         msg += "DynAny::rewind operation";
         fail (msg);
      }
      msg = "The component at DynAny::seek(0) is not equal to the ";
      msg += "component at DynAny::rewind";
      assertTrue (msg, compSeek.equal (compRewind));
   }


   /**
    * Test accessing the names and types of members in a DynStruct object.
    */
   public void testAccessStructMembers ()
   {
      String msg;
      String memberName = null;
      TCKind memberKind = null;
      org.omg.CORBA.TypeCode tc = null;
      org.omg.DynamicAny.DynStruct dynAny = null;

      tc = NonEmptyExceptionHelper.type ();
      dynAny = createDynAnyFromTypeCode (tc);

      // test getting the name of the current member
      msg = "Failed to get the correct name of the first member using ";
      msg += "DynStruct::current_member_name operation";
      try
      {
         memberName = dynAny.current_member_name ();

         assertEquals (msg, "field1", memberName); // specific to IDL
      }
      catch (AssertionFailedError ex)
      {
         throw ex;
      }
      catch (Throwable ex)
      {
         fail (msg + ": " + ex);
      }

      // test getting the kind of the current member
      msg = "Failed to get the correct kind of the first member using ";
      msg += "DynStruct::current_member_kind operation";
      try
      {
         memberKind = dynAny.current_member_kind ();

         // specific to IDL
         assertEquals (msg, TCKind._tk_long, memberKind.value ());
      }
      catch (AssertionFailedError ex)
      {
         throw ex;
      }
      catch (Throwable ex)
      {
         fail (msg + ": " + ex);
      }

      // move to the next position
      dynAny.next ();

      // test getting the name of the current member
      msg = "Failed to get the correct name of the second member using ";
      msg += "DynStruct::current_member_name operation";
      try
      {
         memberName = dynAny.current_member_name ();

         assertEquals (msg, "field2", memberName); // specific to IDL
      }
      catch (AssertionFailedError ex)
      {
         throw ex;
      }
      catch (Throwable ex)
      {
         fail (msg + ": " + ex);
      }

      // test getting the kind of the current member
      msg = "Failed to get the correct kind of the second member using ";
      msg += "DynStruct::current_member_kind operation";
      try
      {
         memberKind = dynAny.current_member_kind ();

         // specific to IDL
         assertEquals (msg, TCKind._tk_string, memberKind.value ());
      }
      catch (AssertionFailedError ex)
      {
         throw ex;
      }
      catch (Throwable ex)
      {
         fail (msg + ": " + ex);
      }

      // move to an invalid position
      dynAny.seek (-1);

      // test getting the name of the current member
      msg = "Failed to throw an InvalidValue exception when calling ";
      msg += "DynStruct::current_member_name operation at an invalid position";
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
         if (!(ex instanceof org.omg.DynamicAny.DynAnyPackage.InvalidValue))
         {
            fail (msg + ": " + ex);
         }
      }

      // test getting the kind of the current member
      msg = "Failed to throw an InvalidValue exception when calling ";
      msg += "DynStruct::current_member_kind operation at an invalid position";
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
         if (!(ex instanceof org.omg.DynamicAny.DynAnyPackage.InvalidValue))
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
      final String name1 = "field1"; // specific to IDL
      final String name2 = "field2"; // specific to IDL
      String msg;
      org.omg.CORBA.Any any = null;
      NonEmptyException type = null;
      int oldInt = 1;
      String oldStr = "Hello";
      int newInt = 2;
      String newStr = "GoodBye";
      org.omg.DynamicAny.DynStruct dynAny = null;
      org.omg.DynamicAny.NameValuePair [] pairs = null;

      type = new NonEmptyException (oldInt, oldStr);
      any = orb.create_any ();
      NonEmptyExceptionHelper.insert (any, type);
      dynAny = createDynAnyFromAny (any);

      // test extracting the name/value pairs as Anys
      pairs = dynAny.get_members ();
      msg = "Old name associated with first name/value pair is incorrect";
      assertEquals (msg, name1, pairs [0].id);
      msg = "Old value associated with first name/value pair is incorrect";
      assertEquals (msg, oldInt, pairs [0].value.extract_long ());
      msg = "Old name associated with second name/value pair is incorrect";
      assertEquals (msg, name2, pairs [1].id);
      msg = "Old value associated with second name/value pair is incorrect";
      assertEquals (msg, oldStr, pairs [1].value.extract_string ());

      pairs = new NameValuePair [2]; // specific to IDL
      any = orb.create_any ();
      any.insert_long (newInt);
      pairs [0] = new NameValuePair (name1, any);
      any = orb.create_any ();
      any.insert_string (newStr);
      pairs [1] = new NameValuePair (name2, any);

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

      // test extracting the name/value pairs as Anys
      pairs = dynAny.get_members ();
      msg = "New name associated with first name/value pair is incorrect";
      assertEquals (msg, name1, pairs [0].id);
      msg = "New value associated with first name/value pair is incorrect ";
      assertEquals (msg, newInt, pairs [0].value.extract_long ());
      msg = "New name associated with second name/value pair is incorrect";
      assertEquals (msg, name2, pairs [1].id);
      msg = "New value associated with second name/value pair is incorrect";
      assertEquals (msg, newStr, pairs [1].value.extract_string ());
   }


   /**
    * Test accessing the member Name/Value DynAny pairs in a DynStruct object.
    */
   public void testAccessStructDynAnyPairs ()
   {
      final String name1 = "field1"; // specific to IDL
      final String name2 = "field2"; // specific to IDL
      String msg;
      org.omg.CORBA.Any any = null;
      NonEmptyException type = null;
      int oldInt = 1;
      String oldStr = "Hello";
      int newInt = 2;
      String newStr = "GoodBye";
      org.omg.DynamicAny.DynStruct dynAny = null;
      org.omg.DynamicAny.DynAny dynAny1 = null;
      org.omg.DynamicAny.DynAny dynAny2 = null;
      org.omg.DynamicAny.NameDynAnyPair [] pairs = null;

      type = new NonEmptyException (oldInt, oldStr);
      any = orb.create_any ();
      NonEmptyExceptionHelper.insert (any, type);
      dynAny = createDynAnyFromAny (any);

      // test extracting the name/value pairs as DynAnys
      pairs = dynAny.get_members_as_dyn_any ();
      msg = "Old name associated with first name/value pair is incorrect";
      assertEquals (msg, name1, pairs [0].id);
      msg = "Old value associated with first name/value pair is incorrect";
      assertEquals (msg, oldInt, pairs [0].value.to_any ().extract_long ());
      msg = "Old name associated with second name/value pair is incorrect";
      assertEquals (msg, name2, pairs [1].id);
      msg = "Old value associated with second name/value pair is incorrect";
      assertEquals (msg, oldStr, pairs [1].value.to_any ().extract_string ());

      any = orb.create_any ();
      any.insert_long (newInt);
      try
      {
         dynAny1 = factory.create_dyn_any (any);
      }
      catch (Throwable ex)
      {
         msg = "Factory failed to create DynAny from Any for first member ";
         msg += "using DynAny::create_dyn_any operation: " + ex;
         fail (msg);
      }

      any = orb.create_any ();
      any.insert_string (newStr);
      try
      {
         dynAny2 = factory.create_dyn_any (any);
      }
      catch (Throwable ex)
      {
         msg = "Factory failed to create DynAny from Any for second member ";
         msg += "using DynAny::create_dyn_any operation: " + ex;
         fail (msg);
      }

      pairs = new NameDynAnyPair [2]; // specific to IDL
      pairs [0] = new NameDynAnyPair (name1, dynAny1);
      pairs [1] = new NameDynAnyPair (name2, dynAny2);

      msg = "Failed to set DynStruct members using ";
      msg += "DynStruct::set_members_as_dyn_any operation";
      try
      {
         dynAny.set_members_as_dyn_any (pairs);
      }
      catch (Throwable ex)
      {
         fail (msg + ": " + ex);
      }

      // test extracting the name/value pairs as DynAnys
      pairs = dynAny.get_members_as_dyn_any ();
      msg = "New name associated with first name/value pair is incorrect";
      assertEquals (msg, name1, pairs [0].id);
      msg = "New value associated with first name/value pair is incorrect ";
      assertEquals (msg, newInt, pairs [0].value.to_any ().extract_long ());
      msg = "New name associated with second name/value pair is incorrect";
      assertEquals (msg, name2, pairs [1].id);
      msg = "New value associated with second name/value pair is incorrect";
      assertEquals (msg, newStr, pairs [1].value.to_any ().extract_string ());
   }


   /**
    * Test the exceptions raised while accessing the member Name/Value
    * DynAny pairs in a DynStruct object.
    */
   public void testAccessStructPairsEx ()
   {
      final String name1 = "field1";
      final String name2 = "field2";
      String msg;
      org.omg.CORBA.Any any = null;
      org.omg.CORBA.TypeCode tc = null;
      org.omg.DynamicAny.DynStruct dynAny = null;
      org.omg.DynamicAny.NameValuePair [] pairs = null;

      tc = orb.create_exception_tc (ID, NAME, getExceptionMembers ());
      dynAny = createDynAnyFromTypeCode (tc);

      // test inserting a member with an incorrect type
      pairs = new NameValuePair [2];
      any = orb.create_any ();
      any.insert_char ('a');
      pairs [0] = new NameValuePair (name1, any);
      any = orb.create_any ();
      any.insert_string ("WrongType");
      pairs [0] = new NameValuePair (name2, any);

      msg = "Failed to raise a TypeMismatch exception when passing in a ";
      msg += "name/value pair containing an incorrect member value";
      try
      {
         dynAny.set_members (pairs);

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

      // test inserting a member with an incorrect name
      pairs = new NameValuePair [2];
      any = orb.create_any ();
      any.insert_char ('a');
      pairs [0] = new NameValuePair (name1, any);
      any = orb.create_any ();
      any.insert_longlong ((long) 1);
      pairs [1] = new NameValuePair ("WrongName", any);

      msg = "Failed to raise a TypeMismatch exception when passing in a ";
      msg += "name/value pair containing an incorrect member name";
      try
      {
         dynAny.set_members (pairs);

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

      // test inserting a sequence with the wrong number of members
      pairs = new NameValuePair [3];
      any = orb.create_any ();
      any.insert_char ('a');
      pairs [0] = new NameValuePair (name1, any);
      any = orb.create_any ();
      any.insert_longlong ((long) 1);
      pairs [1] = new NameValuePair (name2, any);
      any = orb.create_any ();
      any.insert_longlong ((long) 1);
      pairs [2] = new NameValuePair (name2, any);

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

      tc = orb.create_exception_tc (ID, NAME, getExceptionMembers ());
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
      NonEmptyException type;
      org.omg.CORBA.Any any = null;
      org.omg.CORBA.TypeCode tc = null;
      org.omg.DynamicAny.DynStruct dynAny = null;
      org.omg.DynamicAny.DynStruct dynAny2 = null;

      tc = NonEmptyExceptionHelper.type ();
      dynAny = createDynAnyFromTypeCode (tc);

      type = new NonEmptyException (1, "Hello");
      any = orb.create_any ();
      NonEmptyExceptionHelper.insert (any, type);
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
      NonEmptyException type;
      org.omg.CORBA.Any any = null;
      org.omg.CORBA.TypeCode tc = null;
      org.omg.DynamicAny.DynStruct dynAny = null;
      org.omg.DynamicAny.DynStruct dynAny2 = null;

      tc = NonEmptyExceptionHelper.type ();
      dynAny = createDynAnyFromTypeCode (tc);

      type = new NonEmptyException (1, "Hello");
      any = orb.create_any ();
      NonEmptyExceptionHelper.insert (any, type);
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

      tc = orb.create_exception_tc (ID, NAME, getExceptionMembers ());
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

      tc = NonEmptyExceptionHelper.type ();
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
      NonEmptyException type;
      org.omg.CORBA.Any any = null;
      org.omg.DynamicAny.DynStruct dynAny = null;

      type = new NonEmptyException (1, "Hello");
      any = orb.create_any ();
      NonEmptyExceptionHelper.insert (any, type);
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
    * Test destroying a component of a DynAny object.
    */
   public void testDestroyComponent ()
   {
      String msg;
      NonEmptyException type;
      org.omg.CORBA.Any any = null;
      org.omg.DynamicAny.DynStruct dynAny = null;
      org.omg.DynamicAny.DynAny comp = null;

      type = new NonEmptyException (1, "Hello");
      any = orb.create_any ();
      NonEmptyExceptionHelper.insert (any, type);
      dynAny = createDynAnyFromAny (any);

      try
      {
         comp = dynAny.current_component ();
      }
      catch (Throwable ex)
      {
         msg = "Failed to get the current component of the DynAny using the ";
         msg += "DynAny::current_component operation before calling the ";
         msg += "DynAny::destroy operation";
         fail (msg + ": " + ex);
      }

      comp.destroy ();
      try
      {
         comp = dynAny.current_component ();
      }
      catch (Throwable ex)
      {
         msg = "Failed to get the current component of the DynAny using the ";
         msg += "DynAny::current_component operation after calling the ";
         msg += "DynAny::destroy operation";
         fail (msg + ": " + ex);
      }

      try
      {
         comp.type ();
      }
      catch (org.omg.CORBA.OBJECT_NOT_EXIST ex)
      {
         msg = "Calling destroy on a component resulted in destroying the ";
         msg += "component object";
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

      tc = NonEmptyExceptionHelper.type ();
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


   /**
    * Create a sequence of fields for use in creating an exception TypeCode.
    */
   private org.omg.CORBA.StructMember [] getExceptionMembers ()
   {
      final org.omg.CORBA.StructMember [] members =
         new org.omg.CORBA.StructMember [2];

      members [0] = new org.omg.CORBA.StructMember
      (
         "field1",
         orb.get_primitive_tc (TCKind.tk_char),
         null
      );

      members [1] = new org.omg.CORBA.StructMember
      (
         "field2",
         orb.get_primitive_tc (TCKind.tk_longlong),
         null
      );

      return members;
   }

}
