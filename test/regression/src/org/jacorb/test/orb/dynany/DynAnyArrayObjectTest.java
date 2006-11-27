package org.jacorb.test.orb.dynany;

import org.omg.CORBA.Any;
import org.omg.CORBA.TypeCode;
import org.omg.DynamicAny.DynAny;
import org.omg.DynamicAny.DynArray;

/**
 * DynAnyArrayObjectTest.java
 *
 * DynAny tests for arrays of object types.
 *
 */

public class DynAnyArrayObjectTest extends DynAnyXXXTestCase
{
   private static String nameService =
      "IOR:000000000000002B49444C3A6F6D672E6F72672F436F734E616D696E672F4E616"
      + "D696E67436F6E746578744578743A312E30000000000002000000000000007400010"
      + "2000000000E3231332E34382E39312E31353700836C0000001F5374616E646172644"
      + "E532F4E616D655365727665722D504F412F5F726F6F7400000000020000000000000"
      + "008000000004A414300000000010000001C000000000001000100000001050100010"
      + "00101090000000105010001000000010000002C00000000000000010000000100000"
      + "01C00000000000100010000000105010001000101090000000105010001";


   public void testInsertAnyObject() throws Exception
   {
       DynArray dyn_array = null;

       org.omg.CORBA.Object  obj = orb.string_to_object (nameService);

       TypeCode tc = org.omg.CosNaming.NamingContextExtHelper.type();

       TypeCode array_tc =
           orb.create_array_tc (3, tc);

       dyn_array =
           (DynArray) factory.create_dyn_any_from_type_code (array_tc);


       Any [] object_array = new Any[3];
       object_array[0] = orb.create_any();
       object_array[1] = orb.create_any();
       object_array[2] = orb.create_any();
       object_array[0].insert_Object (obj);
       object_array[1].insert_Object (obj);
       object_array[2].insert_Object (obj);

       dyn_array.set_elements (object_array);

       dyn_array.to_any();

       dyn_array.destroy();
   }

   public void testInsertDynAnyObject() throws Exception
   {
       DynArray dyn_array;

       TypeCode tc = org.omg.CosNaming.NamingContextExtHelper.type();

       TypeCode array_tc =
           orb.create_array_tc (3, tc);

       dyn_array =
           (DynArray) factory.create_dyn_any_from_type_code (array_tc);


       DynAny [] object_array = new DynAny[3];

       object_array[0] = factory.create_dyn_any_from_type_code (tc);
       object_array[1] = factory.create_dyn_any_from_type_code (tc);
       object_array[2] = factory.create_dyn_any_from_type_code (tc);

       dyn_array.set_elements_as_dyn_any (object_array);

       dyn_array.to_any();

       dyn_array.destroy();
   }
}
