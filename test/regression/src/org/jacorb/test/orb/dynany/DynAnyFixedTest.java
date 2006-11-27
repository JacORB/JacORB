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

import java.lang.reflect.Method;
import java.math.BigDecimal;

import junit.framework.Test;

/**
 * DynAnyFixedTest.java
 *
 * DynAny tests for fixed types.
 *
 */

public class DynAnyFixedTest extends DynAnyXXXTestCase
{
   /**
    * Tests creating a DynAny object from an Any object using the
    * DynAnyFactory object.
    */
   public void testFactoryCreateFromAny ()
   {
      BigDecimal fixedVal;
      org.omg.CORBA.Any any = null;
      org.omg.CORBA.TypeCode tc = null;

      tc = orb.create_fixed_tc ((short) 2, (short) 1);
      fixedVal = new BigDecimal ("1.0");
      any = orb.create_any ();
      any.insert_fixed (fixedVal, tc);

      createDynAnyFromAny (any);
   }


   /**
    * Tests creating a DynAny object from a TypeCode object using the
    * DynAnyFactory object.
    */
   public void testFactoryCreateFromTypeCode ()
   {
      org.omg.CORBA.TypeCode tc = null;

      tc = orb.create_fixed_tc ((short) 2, (short) 1);

      createDynAnyFromTypeCode (tc);
   }


   /**
    * Test comparing DynAny values.
    */
   public void testCompareDynAny ()
   {
      String msg;
      BigDecimal fixedVal;
      org.omg.CORBA.Any any = null;
      org.omg.CORBA.TypeCode tc = null;
      org.omg.DynamicAny.DynFixed dynAny = null;
      org.omg.DynamicAny.DynFixed dynAny2 = null;

      tc = orb.create_fixed_tc ((short) 2, (short) 1);
      fixedVal = new BigDecimal ("1.0");
      any = orb.create_any ();
      any.insert_fixed (fixedVal, tc);
      dynAny = createDynAnyFromAny (any);
      dynAny2 = createDynAnyFromAny (any);

      msg = "Comparing two equal DynAny values using DynAny::equal failed";
      assertTrue (msg, dynAny.equal (dynAny2));
   }


   /**
    * Test accessing a value in a DynFixed object.
    */
   public void testAccessFixedValue ()
   {
      String msg;
      Boolean setVal;
      BigDecimal fixedVal;
      BigDecimal fixedVal2;
      org.omg.CORBA.TypeCode tc = null;
      org.omg.DynamicAny.DynFixed dynAny = null;

      // use reflection to avoid ORB portability issues
      Class fixedClass = null;
      Method method = null;
      Class [] paramTypes = new Class [1];
      Object [] params = new Object [1];

      fixedVal = new BigDecimal ("1.0");
      fixedVal2 = null;
      tc = orb.create_fixed_tc ((short) 2, (short) 1);
      dynAny = createDynAnyFromTypeCode (tc);

      fixedClass = dynAny.getClass ();
      paramTypes [0] = String.class;
      params [0] = fixedVal.toString ();

      // get method to invoke via reflection
      try
      {
         method = fixedClass.getDeclaredMethod ("set_value", paramTypes);
      }
      catch (Throwable ex)
      {
         fail ("Unexpected error trying to obtain method to invoke: " + ex);
      }

      msg = "Failed to set value of DynFixed object with ";
      msg += "DynFixed::set_value operation when truncation was not required";
      setVal = null;
      try
      {
         setVal = (Boolean) method.invoke (dynAny, params);
      }
      catch (Throwable ex)
      {
         fail (msg + ": " + ex);
      }
      // verify return type if not void
      if (setVal != null)
      {
         assertTrue (msg, setVal.booleanValue ());
      }

      fixedVal = new BigDecimal ("1.01");
      params [0] = fixedVal.toString ();

      msg = "Failed to set value of DynFixed object with ";
      msg += "DynFixed::set_value operation when truncation was required";
      setVal = null;
      try
      {
         setVal = (Boolean) method.invoke (dynAny, params);
      }
      catch (Throwable ex)
      {
         fail (msg + ": " + ex);
      }
      // verify return type if not void
      if (setVal != null)
      {
         assertTrue (msg, !setVal.booleanValue ());
      }

      try
      {
         fixedVal2 = new BigDecimal (dynAny.get_value ());
      }
      catch (Throwable ex)
      {
         msg = "Failed to get value of DynFixed object with ";
         msg += "DynFixed::get_value operation";
         fail (msg + ": " + ex);
      }
      msg = "Value inserted into DynAny object is not equal to value ";
      msg += "extracted from same DynAny object";
      assertTrue (msg, fixedVal2.toString ().equals ("1.0"));
   }


   /**
    * Test that a TypeMismatch exception is raised if the fixed value is
    * invalid.
    */
   public void testAccessTypeMismatchEx ()
   {
      String msg;
      String badFixedVal;
      org.omg.CORBA.TypeCode tc = null;
      org.omg.DynamicAny.DynFixed dynAny = null;

      badFixedVal = "j.0";
      tc = orb.create_fixed_tc ((short) 2, (short) 1);
      dynAny = createDynAnyFromTypeCode (tc);

      msg = "TypeMismatch exception not thrown by DynFixed::set_value ";
      msg += "operation when a DynFixed object is set to an invalid value";
      try
      {
         dynAny.set_value (badFixedVal);

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
    * Test that an InvalidValue exception is raised if there are too many
    * digits in the fixed value.
    */
   public void testAccessInvalidValueEx ()
   {
      String msg;
      BigDecimal fixedVal;
      org.omg.CORBA.TypeCode tc = null;
      org.omg.DynamicAny.DynFixed dynAny = null;

      fixedVal = new BigDecimal ("10.01");
      tc = orb.create_fixed_tc ((short) 2, (short) 1);
      dynAny = createDynAnyFromTypeCode (tc);

      msg = "InvalidValue exception not thrown by DynFixed::set_value ";
      msg += "operation when there are too many digits in the fixed value";
      try
      {
         dynAny.set_value (fixedVal.toString ());

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
      org.omg.DynamicAny.DynFixed dynAny = null;

      tc = orb.create_fixed_tc ((short) 2, (short) 1);
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
      BigDecimal fixedVal;
      org.omg.CORBA.Any any = null;
      org.omg.CORBA.TypeCode tc = null;
      org.omg.DynamicAny.DynFixed dynAny = null;
      org.omg.DynamicAny.DynFixed dynAny2 = null;

      tc = orb.create_fixed_tc ((short) 2, (short) 1);
      dynAny = createDynAnyFromTypeCode (tc);

      fixedVal = new BigDecimal ("1.0");
      any = orb.create_any ();
      any.insert_fixed (fixedVal, tc);
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
      BigDecimal fixedVal;
      org.omg.CORBA.Any any = null;
      org.omg.CORBA.TypeCode tc = null;
      org.omg.DynamicAny.DynFixed dynAny = null;
      org.omg.DynamicAny.DynFixed dynAny2 = null;

      tc = orb.create_fixed_tc ((short) 2, (short) 1);
      dynAny = createDynAnyFromTypeCode (tc);

      fixedVal = new BigDecimal ("1.0");
      any = orb.create_any ();
      any.insert_fixed (fixedVal, tc);
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
      org.omg.DynamicAny.DynFixed dynAny = null;

      any = orb.create_any ();
      any.insert_string ("Hello");

      tc = orb.create_fixed_tc ((short) 2, (short) 1);
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
   public void testGenerateAnyFromDynAny () throws Exception
   {
	   final org.omg.CORBA.TypeCode tc = orb.create_fixed_tc ((short) 2, (short) 1);

      BigDecimal fixedVal;
      org.omg.CORBA.Any any;
      org.omg.DynamicAny.DynFixed dynAny;
      org.omg.DynamicAny.DynFixed dynAny2;

      fixedVal = new BigDecimal ("1.0");
      dynAny = createDynAnyFromTypeCode (tc);
      assertEquals(2, dynAny.type().fixed_digits());
      assertEquals(1, dynAny.type().fixed_scale());

      any = dynAny.to_any ();
      assertEquals(2, any.type().fixed_digits());
      assertEquals(1, any.type().fixed_scale());

      dynAny2 = createDynAnyFromAny (any);
      assertEquals(2, dynAny2.type().fixed_digits());
      assertEquals(1, dynAny2.type().fixed_scale());

      String msg = "The DynAny::to_any operation failed to create an Any ";
      msg += "object with the same value as the DynAny object";
      assertTrue (msg, dynAny.equal (dynAny2));
   }


   /**
    * Test destroying a DynAny object.
    */
   public void testDestroyDynAny ()
   {
      String msg;
      BigDecimal fixedVal;
      org.omg.CORBA.Any any = null;
      org.omg.CORBA.TypeCode tc = null;
      org.omg.DynamicAny.DynFixed dynAny = null;

      tc = orb.create_fixed_tc ((short) 2, (short) 1);
      fixedVal = new BigDecimal ("1.0");
      any = orb.create_any ();
      any.insert_fixed (fixedVal, tc);
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
      BigDecimal fixedVal;
      org.omg.CORBA.TypeCode tc = null;
      org.omg.DynamicAny.DynFixed dynAny = null;
      org.omg.DynamicAny.DynFixed dynAny2 = null;

      fixedVal = new BigDecimal ("1.0");
      tc = orb.create_fixed_tc ((short) 2, (short) 1);
      dynAny = createDynAnyFromTypeCode (tc);

      try
      {
         dynAny.set_value (fixedVal.toString ());
      }
      catch (Throwable ex)
      {
         fail ("Failed to insert value into DynAny object: " + ex);
      }
      dynAny2 = (org.omg.DynamicAny.DynFixed) dynAny.copy ();

      msg = "The DynAny object created with the DynAny::copy operation ";
      msg += "is not equal to the DynAny object it was copied from";
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
      org.omg.DynamicAny.DynFixed dynAny = null;

      tc = orb.create_fixed_tc ((short) 2, (short) 1);
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
         dynAny = (org.omg.DynamicAny.DynFixed) dynAny.current_component ();

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
    * Create a DynAny object from an Any object.
    */
   private org.omg.DynamicAny.DynFixed createDynAnyFromAny
      (org.omg.CORBA.Any any)
   {
      String msg;
      org.omg.DynamicAny.DynFixed dynAny = null;

      try
      {
         dynAny = (org.omg.DynamicAny.DynFixed) factory.create_dyn_any (any);
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
   private org.omg.DynamicAny.DynFixed createDynAnyFromTypeCode
      (org.omg.CORBA.TypeCode tc)
   {
      String msg;
      org.omg.DynamicAny.DynFixed dynAny = null;

      try
      {
         dynAny = (org.omg.DynamicAny.DynFixed)
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
