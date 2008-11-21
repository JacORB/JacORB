package org.omg.CORBA;


import org.omg.CORBA.MARSHAL;
import org.omg.CORBA.TypeCode;
import org.omg.CORBA.TypeCodePackage.BadKind;


/**
 * Holder class for fixed point types.
 */
public class FixedHolder
   implements org.omg.CORBA.portable.Streamable
{
   public java.math.BigDecimal value;

   public FixedHolder(){}

   public FixedHolder(java.math.BigDecimal o)
   {
      value = o;
   }

   public TypeCode _type()
   {
      short digits = (short)value.precision();
      short scale = (short)value.scale();

      return ORB.init().create_fixed_tc( digits, scale );
   }

   /**
    * @deprecated use another method to read the fixed e.g.{@link org.omg.CORBA.portable.InputStream#read_fixed(short, short)}
    */
   public void _read(org.omg.CORBA.portable.InputStream in)
   {
      value = in.read_fixed();
       // unable to read
       // because when the fixed is read off the stream, the
       // BigDecimal can't be reconstructed to the original digits and scale without explicitly
       // passing in the digits and scale (Java-to-IDL: 1.21.4.1).
       // application and/or ORB code should use other means to read the fixed.

       throw new NO_IMPLEMENT();
   }

   public void _write(org.omg.CORBA.portable.OutputStream out)
   {
       if (value == null)
       {
           throw new MARSHAL("value may not be null");
       }

       TypeCode typeCode = _type();
       try
       {
           out.write_fixed(value, typeCode.fixed_digits(), typeCode.fixed_scale());
       }
       catch (BadKind e)
       {
           throw new RuntimeException("should never happen", e);
       }
   }
}
