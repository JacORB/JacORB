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

import junit.framework.Test;
import junit.framework.TestSuite;

import org.omg.CORBA.*;
import org.omg.DynamicAny.DynAny;
import org.omg.DynamicAny.DynAnyPackage.TypeMismatch;

/**
 * DynAnyBaseTest.java
 *
 * DynAny tests for basic types.
 *
 */

public class DynAnyBaseTest extends DynAnyXXXTestCase
{
   private static final char EURO_SIGN = '\u20AC';

   public static Test suite ()
   {
       return new TestSuite (DynAnyBaseTest.class, "DynAny Base Tests");
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
   public void testAccessBasicValue_boolean () throws Exception
   {
      TypeCode tc     = orb.get_primitive_tc (org.omg.CORBA.TCKind.tk_boolean);
      DynAny   dynAny = createDynAnyFromTypeCode (tc);

      String msg = "Value inserted into DynAny object is not equal to value ";
      msg += "extracted from same DynAny object";

      dynAny.insert_boolean (true);
      assertEquals (msg, true, dynAny.get_boolean());
   }

   /**
    * Test inserting a basic value into a DynAny that has a different typecode.
    */
   public void testInsertMismatch_boolean () throws Exception
   {
      TypeCode tc     = orb.get_primitive_tc (org.omg.CORBA.TCKind.tk_long);
      DynAny   dynAny = createDynAnyFromTypeCode (tc);

      try
      {
          dynAny.insert_boolean (true);
          fail ("should have thrown TypeMismatch");
      }
      catch (TypeMismatch ex)
      {
          // ok
      }
      catch (Exception ex)
      {
          fail ("should have thrown TypeMismatch, but was: " + ex);
      }
   }

   /**
    * Test retrieving a basic value from a DynAny that has a different typecode.
    */
   public void testRetrieveMismatch_boolean () throws Exception
   {
      TypeCode tc     = orb.get_primitive_tc (org.omg.CORBA.TCKind.tk_long);
      DynAny   dynAny = createDynAnyFromTypeCode (tc);
      dynAny.insert_long(700);

      try
      {
          dynAny.get_boolean();
          fail ("should have thrown TypeMismatch");
      }
      catch (TypeMismatch ex)
      {
          // ok
      }
      catch (Exception ex)
      {
          fail ("should have thrown TypeMismatch, but was:  " + ex);
      }
   }

   /**
    * Test accessing a value of some basic type in a DynAny object.
    */
   public void testAccessBasicValue_short () throws Exception
   {
      TypeCode tc     = orb.get_primitive_tc (org.omg.CORBA.TCKind.tk_short);
      DynAny   dynAny = createDynAnyFromTypeCode (tc);

      String msg = "Value inserted into DynAny object is not equal to value ";
      msg += "extracted from same DynAny object";

      dynAny.insert_short ((short)700);
      assertEquals (msg, (short)700, dynAny.get_short());
   }

   /**
    * Test inserting a basic value into a DynAny that has a different typecode.
    */
   public void testInsertMismatch_short () throws Exception
   {
      TypeCode tc     = orb.get_primitive_tc (org.omg.CORBA.TCKind.tk_long);
      DynAny   dynAny = createDynAnyFromTypeCode (tc);

      try
      {
          dynAny.insert_short ((short)128);
          fail ("should have thrown TypeMismatch");
      }
      catch (TypeMismatch ex)
      {
          // ok
      }
      catch (Exception ex)
      {
          fail ("should have thrown TypeMismatch, but was: " + ex);
      }
   }

   /**
    * Test retrieving a basic value from a DynAny that has a different typecode.
    */
   public void testRetrieveMismatch_short () throws Exception
   {
      TypeCode tc     = orb.get_primitive_tc (org.omg.CORBA.TCKind.tk_long);
      DynAny   dynAny = createDynAnyFromTypeCode (tc);
      dynAny.insert_long(700);

      try
      {
          dynAny.get_short();
          fail ("should have thrown TypeMismatch");
      }
      catch (TypeMismatch ex)
      {
          // ok
      }
      catch (Exception ex)
      {
          fail ("should have thrown TypeMismatch, but was:  " + ex);
      }
   }

   /**
    * Test accessing a value of some basic type in a DynAny object.
    */
   public void testAccessBasicValue_ushort () throws Exception
   {
      TypeCode tc     = orb.get_primitive_tc (org.omg.CORBA.TCKind.tk_ushort);
      DynAny   dynAny = createDynAnyFromTypeCode (tc);

      String msg = "Value inserted into DynAny object is not equal to value ";
      msg += "extracted from same DynAny object";

      dynAny.insert_ushort ((short)700);
      assertEquals (msg, (short)700, dynAny.get_ushort());
   }

      /**
    * Test inserting a basic value into a DynAny that has a different typecode.
    */
   public void testInsertMismatch_ushort () throws Exception
   {
      TypeCode tc     = orb.get_primitive_tc (org.omg.CORBA.TCKind.tk_long);
      DynAny   dynAny = createDynAnyFromTypeCode (tc);

      try
      {
          dynAny.insert_ushort ((short)700);
          fail ("should have thrown TypeMismatch");
      }
      catch (TypeMismatch ex)
      {
          // ok
      }
      catch (Exception ex)
      {
          fail ("should have thrown TypeMismatch, but was: " + ex);
      }
   }

   /**
    * Test retrieving a basic value from a DynAny that has a different typecode.
    */
   public void testRetrieveMismatch_ushort () throws Exception
   {
      TypeCode tc     = orb.get_primitive_tc (org.omg.CORBA.TCKind.tk_long);
      DynAny   dynAny = createDynAnyFromTypeCode (tc);
      dynAny.insert_long(700);

      try
      {
          dynAny.get_ushort();
          fail ("should have thrown TypeMismatch");
      }
      catch (TypeMismatch ex)
      {
          // ok
      }
      catch (Exception ex)
      {
          fail ("should have thrown TypeMismatch, but was:  " + ex);
      }
   }

   /**
    * Test accessing a value of some basic type in a DynAny object.
    */
   public void testAccessBasicValue_long () throws Exception
   {
      TypeCode tc     = orb.get_primitive_tc (org.omg.CORBA.TCKind.tk_long);
      DynAny   dynAny = createDynAnyFromTypeCode (tc);

      String msg = "Value inserted into DynAny object is not equal to value ";
      msg += "extracted from same DynAny object";

      dynAny.insert_long (700);
      assertEquals (msg, 700, dynAny.get_long());
   }

   /**
    * Test inserting a basic value into a DynAny that has a different typecode.
    */
   public void testInsertMismatch_long () throws Exception
   {
      TypeCode tc     = orb.get_primitive_tc (org.omg.CORBA.TCKind.tk_boolean);
      DynAny   dynAny = createDynAnyFromTypeCode (tc);

      try
      {
          dynAny.insert_long (700);
          fail ("should have thrown TypeMismatch");
      }
      catch (TypeMismatch ex)
      {
          // ok
      }
      catch (Exception ex)
      {
          fail ("should have thrown TypeMismatch, but was: " + ex);
      }
   }

   /**
    * Test retrieving a basic value from a DynAny that has a different typecode.
    */
   public void testRetrieveMismatch_long () throws Exception
   {
      TypeCode tc     = orb.get_primitive_tc (org.omg.CORBA.TCKind.tk_boolean);
      DynAny   dynAny = createDynAnyFromTypeCode (tc);
      dynAny.insert_boolean(false);

      try
      {
          dynAny.get_long();
          fail ("should have thrown TypeMismatch");
      }
      catch (TypeMismatch ex)
      {
          // ok
      }
      catch (Exception ex)
      {
          fail ("should have thrown TypeMismatch, but was:  " + ex);
      }
   }

   /**
    * Test accessing a value of some basic type in a DynAny object.
    */
   public void testAccessBasicValue_ulong () throws Exception
   {
      TypeCode tc     = orb.get_primitive_tc (org.omg.CORBA.TCKind.tk_ulong);
      DynAny   dynAny = createDynAnyFromTypeCode (tc);

      String msg = "Value inserted into DynAny object is not equal to value ";
      msg += "extracted from same DynAny object";

      dynAny.insert_ulong (700);
      assertEquals (msg, 700, dynAny.get_ulong());
   }

      /**
    * Test inserting a basic value into a DynAny that has a different typecode.
    */
   public void testInsertMismatch_ulong () throws Exception
   {
      TypeCode tc     = orb.get_primitive_tc (org.omg.CORBA.TCKind.tk_boolean);
      DynAny   dynAny = createDynAnyFromTypeCode (tc);

      try
      {
          dynAny.insert_ulong (700);
          fail ("should have thrown TypeMismatch");
      }
      catch (TypeMismatch ex)
      {
          // ok
      }
      catch (Exception ex)
      {
          fail ("should have thrown TypeMismatch, but was: " + ex);
      }
   }

   /**
    * Test retrieving a basic value from a DynAny that has a different typecode.
    */
   public void testRetrieveMismatch_ulong () throws Exception
   {
      TypeCode tc     = orb.get_primitive_tc (org.omg.CORBA.TCKind.tk_boolean);
      DynAny   dynAny = createDynAnyFromTypeCode (tc);
      dynAny.insert_boolean(true);

      try
      {
          dynAny.get_ulong();
          fail ("should have thrown TypeMismatch");
      }
      catch (TypeMismatch ex)
      {
          // ok
      }
      catch (Exception ex)
      {
          fail ("should have thrown TypeMismatch, but was:  " + ex);
      }
   }

   /**
    * Test accessing a value of some basic type in a DynAny object.
    */
   public void testAccessBasicValue_longlong () throws Exception
   {
      TypeCode tc     = orb.get_primitive_tc (org.omg.CORBA.TCKind.tk_longlong);
      DynAny   dynAny = createDynAnyFromTypeCode (tc);

      String msg = "Value inserted into DynAny object is not equal to value ";
      msg += "extracted from same DynAny object";

      dynAny.insert_longlong (700700L);
      assertEquals (msg, 700700L, dynAny.get_longlong());
   }

      /**
    * Test inserting a basic value into a DynAny that has a different typecode.
    */
   public void testInsertMismatch_longlong () throws Exception
   {
      TypeCode tc     = orb.get_primitive_tc (org.omg.CORBA.TCKind.tk_boolean);
      DynAny   dynAny = createDynAnyFromTypeCode (tc);

      try
      {
          dynAny.insert_longlong (700700L);
          fail ("should have thrown TypeMismatch");
      }
      catch (TypeMismatch ex)
      {
          // ok
      }
      catch (Exception ex)
      {
          fail ("should have thrown TypeMismatch, but was: " + ex);
      }
   }

   /**
    * Test retrieving a basic value from a DynAny that has a different typecode.
    */
   public void testRetrieveMismatch_longlong () throws Exception
   {
      TypeCode tc     = orb.get_primitive_tc (org.omg.CORBA.TCKind.tk_long);
      DynAny   dynAny = createDynAnyFromTypeCode (tc);
      dynAny.insert_long(700);

      try
      {
          dynAny.get_longlong();
          fail ("should have thrown TypeMismatch");
      }
      catch (TypeMismatch ex)
      {
          // ok
      }
      catch (Exception ex)
      {
          fail ("should have thrown TypeMismatch, but was:  " + ex);
      }
   }

   /**
    * Test accessing a value of some basic type in a DynAny object.
    */
   public void testAccessBasicValue_ulonglong () throws Exception
   {
      TypeCode tc     = orb.get_primitive_tc (org.omg.CORBA.TCKind.tk_ulonglong);
      DynAny   dynAny = createDynAnyFromTypeCode (tc);

      String msg = "Value inserted into DynAny object is not equal to value ";
      msg += "extracted from same DynAny object";

      dynAny.insert_ulonglong (700700700L);
      assertEquals (msg, 700700700L, dynAny.get_ulonglong());
   }

      /**
    * Test inserting a basic value into a DynAny that has a different typecode.
    */
   public void testInsertMismatch_ulonglong () throws Exception
   {
      TypeCode tc     = orb.get_primitive_tc (org.omg.CORBA.TCKind.tk_boolean);
      DynAny   dynAny = createDynAnyFromTypeCode (tc);

      try
      {
          dynAny.insert_ulonglong (700700700L);
          fail ("should have thrown TypeMismatch");
      }
      catch (TypeMismatch ex)
      {
          // ok
      }
      catch (Exception ex)
      {
          fail ("should have thrown TypeMismatch, but was: " + ex);
      }
   }

   /**
    * Test retrieving a basic value from a DynAny that has a different typecode.
    */
   public void testRetrieveMismatch_ulonglong () throws Exception
   {
      TypeCode tc     = orb.get_primitive_tc (org.omg.CORBA.TCKind.tk_long);
      DynAny   dynAny = createDynAnyFromTypeCode (tc);
      dynAny.insert_long(700);

      try
      {
          dynAny.get_ulonglong();
          fail ("should have thrown TypeMismatch");
      }
      catch (TypeMismatch ex)
      {
          // ok
      }
      catch (Exception ex)
      {
          fail ("should have thrown TypeMismatch, but was:  " + ex);
      }
   }

   /**
    * Test accessing a value of some basic type in a DynAny object.
    */
   public void testAccessBasicValue_octet () throws Exception
   {
      TypeCode tc     = orb.get_primitive_tc (org.omg.CORBA.TCKind.tk_octet);
      DynAny   dynAny = createDynAnyFromTypeCode (tc);

      String msg = "Value inserted into DynAny object is not equal to value ";
      msg += "extracted from same DynAny object";

      dynAny.insert_octet ((byte)123);
      assertEquals (msg, (byte)123, dynAny.get_octet());
   }

      /**
    * Test inserting a basic value into a DynAny that has a different typecode.
    */
   public void testInsertMismatch_octet () throws Exception
   {
      TypeCode tc     = orb.get_primitive_tc (org.omg.CORBA.TCKind.tk_boolean);
      DynAny   dynAny = createDynAnyFromTypeCode (tc);

      try
      {
          dynAny.insert_octet ((byte)87);
          fail ("should have thrown TypeMismatch");
      }
      catch (TypeMismatch ex)
      {
          // ok
      }
      catch (Exception ex)
      {
          fail ("should have thrown TypeMismatch, but was: " + ex);
      }
   }

   /**
    * Test retrieving a basic value from a DynAny that has a different typecode.
    */
   public void testRetrieveMismatch_octet () throws Exception
   {
      TypeCode tc     = orb.get_primitive_tc (org.omg.CORBA.TCKind.tk_long);
      DynAny   dynAny = createDynAnyFromTypeCode (tc);
      dynAny.insert_long(700);

      try
      {
          dynAny.get_octet();
          fail ("should have thrown TypeMismatch");
      }
      catch (TypeMismatch ex)
      {
          // ok
      }
      catch (Exception ex)
      {
          fail ("should have thrown TypeMismatch, but was:  " + ex);
      }
   }

   /**
    * Test accessing a value of some basic type in a DynAny object.
    */
   public void testAccessBasicValue_float () throws Exception
   {
      TypeCode tc     = orb.get_primitive_tc (org.omg.CORBA.TCKind.tk_float);
      DynAny   dynAny = createDynAnyFromTypeCode (tc);

      String msg = "Value inserted into DynAny object is not equal to value ";
      msg += "extracted from same DynAny object";

      dynAny.insert_float (700.0F);
      assertEquals (msg, 700.0F, dynAny.get_float(), 0.0);
   }

      /**
    * Test inserting a basic value into a DynAny that has a different typecode.
    */
   public void testInsertMismatch_float () throws Exception
   {
      TypeCode tc     = orb.get_primitive_tc (org.omg.CORBA.TCKind.tk_boolean);
      DynAny   dynAny = createDynAnyFromTypeCode (tc);

      try
      {
          dynAny.insert_float (700.0F);
          fail ("should have thrown TypeMismatch");
      }
      catch (TypeMismatch ex)
      {
          // ok
      }
      catch (Exception ex)
      {
          fail ("should have thrown TypeMismatch, but was: " + ex);
      }
   }

   /**
    * Test retrieving a basic value from a DynAny that has a different typecode.
    */
   public void testRetrieveMismatch_float () throws Exception
   {
      TypeCode tc     = orb.get_primitive_tc (org.omg.CORBA.TCKind.tk_long);
      DynAny   dynAny = createDynAnyFromTypeCode (tc);
      dynAny.insert_long(700);

      try
      {
          dynAny.get_float();
          fail ("should have thrown TypeMismatch");
      }
      catch (TypeMismatch ex)
      {
          // ok
      }
      catch (Exception ex)
      {
          fail ("should have thrown TypeMismatch, but was:  " + ex);
      }
   }

   /**
    * Test accessing a value of some basic type in a DynAny object.
    */
   public void testAccessBasicValue_double () throws Exception
   {
      TypeCode tc     = orb.get_primitive_tc (org.omg.CORBA.TCKind.tk_double);
      DynAny   dynAny = createDynAnyFromTypeCode (tc);

      String msg = "Value inserted into DynAny object is not equal to value ";
      msg += "extracted from same DynAny object";

      dynAny.insert_double (700.0);
      assertEquals (msg, 700.0, dynAny.get_double(), 0.0);
   }

      /**
    * Test inserting a basic value into a DynAny that has a different typecode.
    */
   public void testInsertMismatch_double () throws Exception
   {
      TypeCode tc     = orb.get_primitive_tc (org.omg.CORBA.TCKind.tk_boolean);
      DynAny   dynAny = createDynAnyFromTypeCode (tc);

      try
      {
          dynAny.insert_double (700.0);
          fail ("should have thrown TypeMismatch");
      }
      catch (TypeMismatch ex)
      {
          // ok
      }
      catch (Exception ex)
      {
          fail ("should have thrown TypeMismatch, but was: " + ex);
      }
   }

   /**
    * Test retrieving a basic value from a DynAny that has a different typecode.
    */
   public void testRetrieveMismatch_double () throws Exception
   {
      TypeCode tc     = orb.get_primitive_tc (org.omg.CORBA.TCKind.tk_long);
      DynAny   dynAny = createDynAnyFromTypeCode (tc);
      dynAny.insert_long(700);

      try
      {
          dynAny.get_double();
          fail ("should have thrown TypeMismatch");
      }
      catch (TypeMismatch ex)
      {
          // ok
      }
      catch (Exception ex)
      {
          fail ("should have thrown TypeMismatch, but was:  " + ex);
      }
   }

   /**
    * Test accessing a value of some basic type in a DynAny object.
    */
   public void testAccessBasicValue_char () throws Exception
   {
      TypeCode tc     = orb.get_primitive_tc (org.omg.CORBA.TCKind.tk_char);
      DynAny   dynAny = createDynAnyFromTypeCode (tc);

      String msg = "Value inserted into DynAny object is not equal to value ";
      msg += "extracted from same DynAny object";

      dynAny.insert_char ('a');
      assertEquals (msg, 'a', dynAny.get_char());
   }

      /**
    * Test inserting a basic value into a DynAny that has a different typecode.
    */
   public void testInsertMismatch_char () throws Exception
   {
      TypeCode tc     = orb.get_primitive_tc (org.omg.CORBA.TCKind.tk_boolean);
      DynAny   dynAny = createDynAnyFromTypeCode (tc);

      try
      {
          dynAny.insert_char ('a');
          fail ("should have thrown TypeMismatch");
      }
      catch (TypeMismatch ex)
      {
          // ok
      }
      catch (Exception ex)
      {
          fail ("should have thrown TypeMismatch, but was: " + ex);
      }
   }

   /**
    * Test retrieving a basic value from a DynAny that has a different typecode.
    */
   public void testRetrieveMismatch_char () throws Exception
   {
      TypeCode tc     = orb.get_primitive_tc (org.omg.CORBA.TCKind.tk_long);
      DynAny   dynAny = createDynAnyFromTypeCode (tc);
      dynAny.insert_long(700);

      try
      {
          dynAny.get_char();
          fail ("should have thrown TypeMismatch");
      }
      catch (TypeMismatch ex)
      {
          // ok
      }
      catch (Exception ex)
      {
          fail ("should have thrown TypeMismatch, but was:  " + ex);
      }
   }

   /**
    * Test accessing a value of some basic type in a DynAny object.
    */
   public void testAccessBasicValue_wchar () throws Exception
   {
      TypeCode tc     = orb.get_primitive_tc (org.omg.CORBA.TCKind.tk_wchar);
      DynAny   dynAny = createDynAnyFromTypeCode (tc);

      String msg = "Value inserted into DynAny object is not equal to value ";
      msg += "extracted from same DynAny object";

      dynAny.insert_wchar (EURO_SIGN);
      assertEquals (msg, EURO_SIGN, dynAny.get_wchar());
   }

      /**
    * Test inserting a basic value into a DynAny that has a different typecode.
    */
   public void testInsertMismatch_wchar () throws Exception
   {
      TypeCode tc     = orb.get_primitive_tc (org.omg.CORBA.TCKind.tk_boolean);
      DynAny   dynAny = createDynAnyFromTypeCode (tc);

      try
      {
          dynAny.insert_wchar (EURO_SIGN);
          fail ("should have thrown TypeMismatch");
      }
      catch (TypeMismatch ex)
      {
          // ok
      }
      catch (Exception ex)
      {
          fail ("should have thrown TypeMismatch, but was: " + ex);
      }
   }

   /**
    * Test retrieving a basic value from a DynAny that has a different typecode.
    */
   public void testRetrieveMismatch_wchar () throws Exception
   {
      TypeCode tc     = orb.get_primitive_tc (org.omg.CORBA.TCKind.tk_long);
      DynAny   dynAny = createDynAnyFromTypeCode (tc);
      dynAny.insert_long(700);

      try
      {
          dynAny.get_wchar();
          fail ("should have thrown TypeMismatch");
      }
      catch (TypeMismatch ex)
      {
          // ok
      }
      catch (Exception ex)
      {
          fail ("should have thrown TypeMismatch, but was:  " + ex);
      }
   }

   /**
    * Test accessing a value of some basic type in a DynAny object.
    */
   public void testAccessBasicValue_string () throws Exception
   {
      TypeCode tc     = orb.get_primitive_tc (org.omg.CORBA.TCKind.tk_string);
      DynAny   dynAny = createDynAnyFromTypeCode (tc);

      String msg = "Value inserted into DynAny object is not equal to value ";
      msg += "extracted from same DynAny object";

      dynAny.insert_string ("foobar");
      assertEquals (msg, "foobar", dynAny.get_string());
   }

      /**
    * Test inserting a basic value into a DynAny that has a different typecode.
    */
   public void testInsertMismatch_string () throws Exception
   {
      TypeCode tc     = orb.get_primitive_tc (org.omg.CORBA.TCKind.tk_boolean);
      DynAny   dynAny = createDynAnyFromTypeCode (tc);

      try
      {
          dynAny.insert_string ("foobar");
          fail ("should have thrown TypeMismatch");
      }
      catch (TypeMismatch ex)
      {
          // ok
      }
      catch (Exception ex)
      {
          fail ("should have thrown TypeMismatch, but was: " + ex);
      }
   }

   /**
    * Test retrieving a basic value from a DynAny that has a different typecode.
    */
   public void testRetrieveMismatch_string () throws Exception
   {
      TypeCode tc     = orb.get_primitive_tc (org.omg.CORBA.TCKind.tk_long);
      DynAny   dynAny = createDynAnyFromTypeCode (tc);
      dynAny.insert_long(700);

      try
      {
          dynAny.get_string();
          fail ("should have thrown TypeMismatch");
      }
      catch (TypeMismatch ex)
      {
          // ok
      }
      catch (Exception ex)
      {
          fail ("should have thrown TypeMismatch, but was:  " + ex);
      }
   }

   /**
    * Test accessing a value of some basic type in a DynAny object.
    */
   public void testAccessBasicValue_wstring () throws Exception
   {
      TypeCode tc     = orb.get_primitive_tc (org.omg.CORBA.TCKind.tk_wstring);
      DynAny   dynAny = createDynAnyFromTypeCode (tc);

      String msg = "Value inserted into DynAny object is not equal to value ";
      msg += "extracted from same DynAny object";

      dynAny.insert_wstring ("foobar");
      assertEquals (msg, "foobar", dynAny.get_wstring());
   }

      /**
    * Test inserting a basic value into a DynAny that has a different typecode.
    */
   public void testInsertMismatch_wstring () throws Exception
   {
      TypeCode tc     = orb.get_primitive_tc (org.omg.CORBA.TCKind.tk_boolean);
      DynAny   dynAny = createDynAnyFromTypeCode (tc);

      try
      {
          dynAny.insert_wstring ("foobarx");
          fail ("should have thrown TypeMismatch");
      }
      catch (TypeMismatch ex)
      {
          // ok
      }
      catch (Exception ex)
      {
          fail ("should have thrown TypeMismatch, but was: " + ex);
      }
   }

   /**
    * Test retrieving a basic value from a DynAny that has a different typecode.
    */
   public void testRetrieveMismatch_wstring () throws Exception
   {
      TypeCode tc     = orb.get_primitive_tc (org.omg.CORBA.TCKind.tk_long);
      DynAny   dynAny = createDynAnyFromTypeCode (tc);
      dynAny.insert_long(700);

      try
      {
          dynAny.get_wstring();
          fail ("should have thrown TypeMismatch");
      }
      catch (TypeMismatch ex)
      {
          // ok
      }
      catch (Exception ex)
      {
          fail ("should have thrown TypeMismatch, but was:  " + ex);
      }
   }

   /**
    * Test accessing a value of some basic type in a DynAny object.
    */
   public void testAccessBasicValue_any () throws Exception
   {
      TypeCode tc     = orb.get_primitive_tc (org.omg.CORBA.TCKind.tk_any);
      DynAny   dynAny = createDynAnyFromTypeCode (tc);

      String msg = "Value inserted into DynAny object is not equal to value ";
      msg += "extracted from same DynAny object";

      Any inAny = orb.create_any();
      inAny.insert_long (777);

      dynAny.insert_any (inAny);
      Any outAny = dynAny.get_any();

      assertTrue (msg, inAny.equal(outAny));
      assertEquals (msg, inAny.extract_long(), outAny.extract_long());
   }

      /**
    * Test inserting a basic value into a DynAny that has a different typecode.
    */
   public void testInsertMismatch_any () throws Exception
   {
      TypeCode tc     = orb.get_primitive_tc (org.omg.CORBA.TCKind.tk_boolean);
      DynAny   dynAny = createDynAnyFromTypeCode (tc);
      Any inAny = orb.create_any();
      inAny.insert_long(700);

      try
      {
          dynAny.insert_any (inAny);
          fail ("should have thrown TypeMismatch");
      }
      catch (TypeMismatch ex)
      {
          // ok
      }
      catch (Exception ex)
      {
          fail ("should have thrown TypeMismatch, but was: " + ex);
      }
   }

   /**
    * Test retrieving a basic value from a DynAny that has a different typecode.
    */
   public void testRetrieveMismatch_any () throws Exception
   {
      TypeCode tc     = orb.get_primitive_tc (org.omg.CORBA.TCKind.tk_long);
      DynAny   dynAny = createDynAnyFromTypeCode (tc);
      dynAny.insert_long(700);

      try
      {
          dynAny.get_any();
          fail ("should have thrown TypeMismatch");
      }
      catch (TypeMismatch ex)
      {
          // ok
      }
      catch (Exception ex)
      {
          fail ("should have thrown TypeMismatch, but was:  " + ex);
      }
   }

   /**
    * Test accessing a value of some basic type in a DynAny object.
    */
   public void testAccessBasicValue_typecode () throws Exception
   {
      TypeCode tc     = orb.get_primitive_tc (org.omg.CORBA.TCKind.tk_TypeCode);
      DynAny   dynAny = createDynAnyFromTypeCode (tc);

      String msg = "Value inserted into DynAny object is not equal to value ";
      msg += "extracted from same DynAny object";

      TypeCode payload = orb.get_primitive_tc (org.omg.CORBA.TCKind.tk_long);
      dynAny.insert_typecode (payload);

      TypeCode offload = dynAny.get_typecode();
      assertTrue(msg, payload.equal(offload));
   }

      /**
    * Test inserting a basic value into a DynAny that has a different typecode.
    */
   public void testInsertMismatch_typecode () throws Exception
   {
      TypeCode tc     = orb.get_primitive_tc (org.omg.CORBA.TCKind.tk_boolean);
      DynAny   dynAny = createDynAnyFromTypeCode (tc);
      TypeCode payload = orb.get_primitive_tc (org.omg.CORBA.TCKind.tk_octet);

      try
      {
          dynAny.insert_typecode (payload);
          fail ("should have thrown TypeMismatch");
      }
      catch (TypeMismatch ex)
      {
          // ok
      }
      catch (Exception ex)
      {
          fail ("should have thrown TypeMismatch, but was: " + ex);
      }
   }

   /**
    * Test retrieving a basic value from a DynAny that has a different typecode.
    */
   public void testRetrieveMismatch_typecode () throws Exception
   {
      TypeCode tc     = orb.get_primitive_tc (org.omg.CORBA.TCKind.tk_long);
      DynAny   dynAny = createDynAnyFromTypeCode (tc);
      dynAny.insert_long(700);

      try
      {
          dynAny.get_typecode();
          fail ("should have thrown TypeMismatch");
      }
      catch (TypeMismatch ex)
      {
          // ok
      }
      catch (Exception ex)
      {
          fail ("should have thrown TypeMismatch, but was:  " + ex);
      }
   }

   /**
    * Test accessing a value of some basic type in a DynAny object.
    */
   public void testAccessBasicValue_dynany () throws Exception
   {
      TypeCode tc     = orb.get_primitive_tc (org.omg.CORBA.TCKind.tk_any);
      DynAny   dynAny = createDynAnyFromTypeCode (tc);

      String msg = "Value inserted into DynAny object is not equal to value ";
      msg += "extracted from same DynAny object";

      tc = orb.get_primitive_tc (org.omg.CORBA.TCKind.tk_long);
      DynAny payload = createDynAnyFromTypeCode (tc);
      payload.insert_long(787);

      dynAny.insert_dyn_any(payload);
      DynAny offload = dynAny.get_dyn_any();

      assertEquals (msg, 787, offload.get_long());
   }

      /**
    * Test inserting a basic value into a DynAny that has a different typecode.
    */
   public void testInsertMismatch_dynany () throws Exception
   {
      TypeCode tc     = orb.get_primitive_tc (org.omg.CORBA.TCKind.tk_boolean);
      DynAny   dynAny = createDynAnyFromTypeCode (tc);
      DynAny inAny = createDynAnyFromTypeCode(tc);
      inAny.insert_boolean (true);

      try
      {
          dynAny.insert_dyn_any (inAny);
          fail ("should have thrown TypeMismatch");
      }
      catch (TypeMismatch ex)
      {
          // ok
      }
      catch (Exception ex)
      {
          fail ("should have thrown TypeMismatch, but was: " + ex);
      }
   }

   /**
    * Test retrieving a basic value from a DynAny that has a different typecode.
    */
   public void testRetrieveMismatch_dynany () throws Exception
   {
      TypeCode tc     = orb.get_primitive_tc (org.omg.CORBA.TCKind.tk_long);
      DynAny   dynAny = createDynAnyFromTypeCode (tc);
      dynAny.insert_long(700);

      try
      {
          dynAny.get_dyn_any();
          fail ("should have thrown TypeMismatch");
      }
      catch (TypeMismatch ex)
      {
          // ok
      }
      catch (Exception ex)
      {
          fail ("should have thrown TypeMismatch, but was:  " + ex);
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


   /**
    * Create a DynAny object from an Any object.
    */
   private org.omg.DynamicAny.DynAny createDynAnyFromAny
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
   private org.omg.DynamicAny.DynAny createDynAnyFromTypeCode
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
