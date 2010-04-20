package org.omg.CORBA;

import org.omg.CORBA.TCKind;

/**
 * <code>ValueBaseHelper</code> has been implemented as the
 * org.omg.CORBA.ValueBaseHelper provided by OMG does not implement any
 * of the methods and exists only to allow the org,omg packages to be
 * compiled.  The ORB Vendor must supply an implementation of the class.
 * see ofj/idl/omg-03-01-02/org/omg/CORBA/ValueBaseHelper.java
 */
public final class ValueBaseHelper
{
   private final static String  id = "IDL:omg.org/CORBA/ValueBase:1.0";
   private static org.omg.CORBA.TypeCode typeCode;

   public static void insert (org.omg.CORBA.Any any, java.io.Serializable value)
   {
      final org.omg.CORBA.portable.OutputStream out = any.create_output_stream();

      try
      {
         any.type(type ());
         write(out, value);
         any.read_value (out.create_input_stream(), type());
      }
      finally
      {
         try
         {
            out.close();
         }
         catch(java.io.IOException e)
         {
         }
      }
   }

   public static java.io.Serializable extract (org.omg.CORBA.Any any)
   {
      return read (any.create_input_stream ());
   }

   synchronized public static org.omg.CORBA.TypeCode type ()
   {
      if (typeCode == null)
      {
         typeCode = org.omg.CORBA.ORB.init().get_primitive_tc (TCKind.tk_value);
      }
      return typeCode;
   }

   public static String id ()
   {
      return id;
   }

   public static java.io.Serializable read (org.omg.CORBA.portable.InputStream in)
   {
      return ((org.omg.CORBA_2_3.portable.InputStream)in).read_value ();
   }

   public static void write (org.omg.CORBA.portable.OutputStream out, java.io.Serializable value)
   {
      ((org.omg.CORBA_2_3.portable.OutputStream)out).write_value (value);
   }
}
