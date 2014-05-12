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
      String s = value.toString();
      short digits =0;
      short scale = 0;

       if ( s.startsWith("-") )
       {
           s = s.substring(1);
       }

       if ( ! s.startsWith("0.") )
       {
          for( int i = 0; i < s.length(); i++ )
          {
             if ( s.charAt(i) == '.' )
             {
                break;
             }
             digits++;
          }
       }

       int decimal = s.indexOf('.');
       if ( decimal != -1 )
       {
          s = s.substring( decimal + 1 );
          for( int i = 0; i < s.length(); i++ )
          {
             digits++;
             scale++;
          }
       }
      return ORB.init().create_fixed_tc( digits, scale );
   }


   /**
    * @deprecated use another method to read the fixed e.g.{@link org.omg.CORBA.portable.InputStream#read_fixed(short, short)}
    */
   public void _read(org.omg.CORBA.portable.InputStream in)
   {
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
