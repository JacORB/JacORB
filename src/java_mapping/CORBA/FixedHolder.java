package org.omg.CORBA;

/**
 * Holder class for fixed point types.
 *
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
      short digits = 0;
      short scale = 0;

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

   public void _read(org.omg.CORBA.portable.InputStream in)
   {
      value = in.read_fixed();
   }

   public void _write(org.omg.CORBA.portable.OutputStream out)
   {
      out.write_fixed(value);
   }

}
