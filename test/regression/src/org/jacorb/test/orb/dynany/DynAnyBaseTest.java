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

import junit.framework.*;
import junit.extensions.TestSetup;
import org.jacorb.test.common.ORBSetup;

/**
 * DynAnyBaseTest.java
 *
 * DynAny tests for basic types.
 *
 */

public class DynAnyBaseTest extends TestCase
{
   private static org.omg.DynamicAny.DynAnyFactory factory = null;
   private static org.omg.CORBA.ORB orb = null;


   public DynAnyBaseTest (String name)
   {
      super (name);
   }


   public static Test suite ()
   {
      TestSuite suite = new TestSuite ("DynAny Base Tests");
      Setup setup = new Setup (suite);
      ORBSetup osetup = new ORBSetup (setup);

      suite.addTest (new DynAnyBaseTest ("testFactoryCreateFromAny"));
      suite.addTest (new DynAnyBaseTest ("testFactoryCreateFromTypeCode"));
      suite.addTest (new DynAnyBaseTest ("testFactoryInconsistentTypeCodeEx"));
      suite.addTest (new DynAnyBaseTest ("testDynAnyLocalityConstraint"));
      suite.addTest (new DynAnyBaseTest ("testCompareDynAny"));
      suite.addTest (new DynAnyBaseTest ("testAccessBasicValue"));
      suite.addTest (new DynAnyBaseTest ("testAccessTypeMismatchEx"));
      suite.addTest (new DynAnyBaseTest ("testDynAnyTypeCode"));
      suite.addTest (new DynAnyBaseTest ("testInitDynAnyFromDynAny"));
      suite.addTest (new DynAnyBaseTest ("testInitFromDynAnyTypeMismatchEx"));
      suite.addTest (new DynAnyBaseTest ("testInitDynAnyFromAny"));
      suite.addTest (new DynAnyBaseTest ("testInitFromAnyTypeMismatchEx"));
      suite.addTest (new DynAnyBaseTest ("testGenerateAnyFromDynAny"));
      suite.addTest (new DynAnyBaseTest ("testDestroyDynAny"));
      suite.addTest (new DynAnyBaseTest ("testCopyDynAny"));
      suite.addTest (new DynAnyBaseTest ("testIterateDynAny"));
      
      return osetup;
   }


   /**
    * Tests creating a DynAny object from an Any object using the
    * DynAnyFactory object.
    */
   public void testFactoryCreateFromAny ()
   {
      org.omg.CORBA.Any any = null;

      any = orb.create_any ();
      any.insert_long (700);

      // TODO: Test using all possible TypeCodes
      createDynAnyFromAny (any);
   }


   /**
    * Tests creating a DynAny object from a TypeCode object using the
    * DynAnyFactory object.
    */
   public void testFactoryCreateFromTypeCode ()
   {
      org.omg.CORBA.TypeCode tc = null;

      tc = orb.get_primitive_tc (org.omg.CORBA.TCKind.tk_long);

      // TODO: Test using all possible TypeCodes
      createDynAnyFromTypeCode (tc);
   }


   /**
    * Test that an InconsistentTypeCode exception is raised if an invalid
    * TypeCode is used to create a DynAny object.
    */
   public void testFactoryInconsistentTypeCodeEx ()
   {
      String msg;
      org.omg.CORBA.Any any = null;
      org.omg.CORBA.TypeCode tc = null;
      org.omg.DynamicAny.DynAny dynAny = null;

      any = orb.create_any ();
      any.type (orb.get_primitive_tc (org.omg.CORBA.TCKind.tk_Principal));

      // Principal TypeCode
      msg = "Failed to throw InconsistentTypeCode exception when ";
      msg += "creating DynAny from Any with TypeCode tk_Principal";
      try
      {
         dynAny = factory.create_dyn_any (any);         
         
         fail (msg);
      }
      catch (org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCode ex)
      {
         // success
      }
   }


   /**
    * Tests locality constraints on DynAny objects.
    */
   public void testDynAnyLocalityConstraint ()
   {
      String msg;
      org.omg.CORBA.TypeCode tc = null;
      org.omg.DynamicAny.DynAny dynAny = null;

      tc = orb.get_primitive_tc (org.omg.CORBA.TCKind.tk_long);
      dynAny = createDynAnyFromTypeCode (tc);

      msg = "MARSHAL system exception not raised by ORB::object_to_string ";
      msg += "operation";
      try
      {
         orb.object_to_string (dynAny);
         
         fail (msg);
      }
      catch (org.omg.CORBA.MARSHAL ex)
      {
         // success
      }
   }


   /**
    * Test comparing DynAny values.
    */
   public void testCompareDynAny ()
   {
      String msg;
      org.omg.CORBA.Any any = null;
      org.omg.DynamicAny.DynAny dynAny = null;
      org.omg.DynamicAny.DynAny dynAny2 = null;
      
      any = orb.create_any ();
      any.insert_long (700);
      dynAny = createDynAnyFromAny (any);
      dynAny2 = createDynAnyFromAny (any);
      
      msg = "Comparing two equal DynAny values using DynAny::equal failed";
      assertTrue (msg, dynAny.equal (dynAny2));
   }


   /**
    * Test accessing a value of some basic type in a DynAny object.
    */
   public void testAccessBasicValue ()
   {
      String msg;
      int intVal;
      int intVal2;
      org.omg.CORBA.TypeCode tc = null;
      org.omg.DynamicAny.DynAny dynAny = null;

      intVal = 700;
      intVal2 = 0;
      tc = orb.get_primitive_tc (org.omg.CORBA.TCKind.tk_long);
      dynAny = createDynAnyFromTypeCode (tc);
      
      try
      {
         dynAny.insert_long (intVal);
      }
      catch (Throwable ex)
      {
         fail ("Failed to insert value into DynAny object: " + ex);
      }

      try
      {
         intVal2 = dynAny.get_long ();
      }
      catch (Throwable ex)
      {
         fail ("Failed to extract value from DynAny object: " + ex);
      }

      msg = "Value inserted into DynAny object is not equal to value ";
      msg += "extracted from same DynAny object";
      assertEquals (msg, intVal, intVal2);
   }


   /**
    * Test that a TypeMismatch exception is raised if there is a type
    * mismatch between the TypeCode of a DynAny object and the accessor
    * methods used to access its value.
    */
   public void testAccessTypeMismatchEx ()
   {
      String msg;
      org.omg.CORBA.TypeCode tc = null;
      org.omg.DynamicAny.DynAny dynAny = null;

      tc = orb.get_primitive_tc (org.omg.CORBA.TCKind.tk_string);
      dynAny = createDynAnyFromTypeCode (tc);

      msg = "TypeMismatch exception not thrown by insert operation when ";
      msg += "insert operation is not valid for TypeCode of DynAny";
      try
      {
         dynAny.insert_long (700);

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

      msg = "TypeMismatch exception not thrown by get operation when ";
      msg += "get operation is not valid for TypeCode of DynAny";
      try
      {
         dynAny.get_long ();
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
    * Test obtaining the TypeCode associated with a DynAny object.
    */
   public void testDynAnyTypeCode ()
   {
      String msg;
      org.omg.CORBA.TypeCode tc = null;
      org.omg.DynamicAny.DynAny dynAny = null;

      tc = orb.get_primitive_tc (org.omg.CORBA.TCKind.tk_long);
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
      org.omg.CORBA.Any any = null;
      org.omg.CORBA.TypeCode tc = null;
      org.omg.DynamicAny.DynAny dynAny = null;
      org.omg.DynamicAny.DynAny dynAny2 = null;      

      tc = orb.get_primitive_tc (org.omg.CORBA.TCKind.tk_long);
      dynAny = createDynAnyFromTypeCode (tc);

      any = orb.create_any ();
      any.insert_long (700);
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
    * Test that a TypeMismatch exception is raised if there is a type
    * mismatch between the two DynAny types in an assignment operation.
    */
   public void testInitFromDynAnyTypeMismatchEx ()
   {
      String msg;
      org.omg.CORBA.Any any = null;
      org.omg.CORBA.TypeCode tc = null;
      org.omg.DynamicAny.DynAny dynAny = null;
      org.omg.DynamicAny.DynAny dynAny2 = null;      

      any = orb.create_any ();
      any.insert_long (700);
      dynAny = createDynAnyFromAny (any);

      tc = orb.get_primitive_tc (org.omg.CORBA.TCKind.tk_string);
      dynAny2 = createDynAnyFromTypeCode (tc);

      msg = "TypeMismatch exception not thrown by DynAny::assign ";
      msg += "operation when DynAny operands have different types";
      try
      {
         dynAny.assign (dynAny2);
         
         fail (msg);
      }
      catch (org.omg.DynamicAny.DynAnyPackage.TypeMismatch ex)
      {
         // success
      }
   }


   /**
    * Test initializing a DynAny object from an Any value.
    */
   public void testInitDynAnyFromAny ()
   {
      String msg;
      org.omg.CORBA.Any any = null;
      org.omg.CORBA.TypeCode tc = null;
      org.omg.DynamicAny.DynAny dynAny = null;
      org.omg.DynamicAny.DynAny dynAny2 = null;
      
      tc = orb.get_primitive_tc (org.omg.CORBA.TCKind.tk_long);
      dynAny = createDynAnyFromTypeCode (tc);

      any = orb.create_any ();
      any.insert_long (700);
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
      org.omg.DynamicAny.DynAny dynAny = null;
      
      any = orb.create_any ();
      any.insert_long (700);

      tc = orb.get_primitive_tc (org.omg.CORBA.TCKind.tk_string);
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
      org.omg.DynamicAny.DynAny dynAny = null;
      org.omg.DynamicAny.DynAny dynAny2 = null;
      
      tc = orb.get_primitive_tc (org.omg.CORBA.TCKind.tk_long);
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
      org.omg.CORBA.Any any = null;
      org.omg.DynamicAny.DynAny dynAny = null;
      
      any = orb.create_any ();
      any.insert_long (700);
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
      org.omg.DynamicAny.DynAny dynAny = null;
      org.omg.DynamicAny.DynAny dynAny2 = null;
      
      tc = orb.get_primitive_tc (org.omg.CORBA.TCKind.tk_long);
      dynAny = createDynAnyFromTypeCode (tc);

      try
      {
         dynAny.insert_long (700);
      }
      catch (Throwable ex)
      {
         fail ("Failed to insert value into DynAny object: " + ex);
      }
      dynAny2 = dynAny.copy ();

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
      org.omg.DynamicAny.DynAny dynAny = null;
      
      tc = orb.get_primitive_tc (org.omg.CORBA.TCKind.tk_long);
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
         dynAny = dynAny.current_component ();

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


   private static class Setup extends TestSetup
   {
      public Setup (Test test)
      {
         super (test);
      }

      protected void setUp ()
      {
         org.omg.CORBA.Object obj = null;

         orb = ORBSetup.getORB ();
         try
         {
            obj = orb.resolve_initial_references ("DynAnyFactory");
         }
         catch (org.omg.CORBA.ORBPackage.InvalidName ex)
         {
            fail ("Failed to resolve DynAnyFactory: " + ex);
         }
         try
         {
            factory = org.omg.DynamicAny.DynAnyFactoryHelper.narrow (obj);
         }
         catch (Throwable ex)
         {
            fail ("Failed to narrow to DynAnyFactory: " + ex);
         }
      }

      protected void tearDown ()
      {
      }
   }


   /**
    * Create a DynAny object from an Any object.
    */   
   private static org.omg.DynamicAny.DynAny createDynAnyFromAny
      (org.omg.CORBA.Any any)
   {      
      String msg;
      org.omg.DynamicAny.DynAny dynAny = null;

      try
      {
         dynAny = factory.create_dyn_any (any);
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
   private static org.omg.DynamicAny.DynAny createDynAnyFromTypeCode
      (org.omg.CORBA.TypeCode tc)
   {
      String msg;      
      org.omg.DynamicAny.DynAny dynAny = null;

      try
      {
         dynAny = factory.create_dyn_any_from_type_code (tc);
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
