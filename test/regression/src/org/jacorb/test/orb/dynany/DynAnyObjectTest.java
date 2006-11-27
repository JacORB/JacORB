package org.jacorb.test.orb.dynany;

import junit.framework.TestCase;

import org.omg.CORBA.ORB;
import org.omg.CORBA.TypeCode;
import org.omg.DynamicAny.DynAny;

/**
 * DynAnyObjectTest.java
 *
 * DynAny tests for embededded object type.
 */

public class DynAnyObjectTest extends DynAnyXXXTestCase
{
   private static final String nameService =
      "IOR:000000000000002B49444C3A6F6D672E6F72672F436F734E616D696E672F4E616"
      + "D696E67436F6E746578744578743A312E30000000000002000000000000007400010"
      + "2000000000E3231332E34382E39312E31353700836C0000001F5374616E646172644"
      + "E532F4E616D655365727665722D504F412F5F726F6F7400000000020000000000000"
      + "008000000004A414300000000010000001C000000000001000100000001050100010"
      + "00101090000000105010001000000010000002C00000000000000010000000100000"
      + "01C00000000000100010000000105010001000101090000000105010001";


   public void testInsertDynAnyObject() throws Exception
   {
       DynAny dyn_any = null;

       org.omg.CORBA.Object  obj = orb.string_to_object (nameService);

       TypeCode tc = org.omg.CosNaming.NamingContextExtHelper.type();

       dyn_any =
           (DynAny)
           factory.create_dyn_any_from_type_code (tc);

       dyn_any.insert_reference (obj);

       dyn_any.to_any();

       dyn_any.destroy();
   }
}
