package org.omg.CORBA;

/**
 *
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
	for( ; s.charAt( digits ) != '.'; digits++ );

	for( int i = digits + 1; i < s.length() ; scale++ );		
		
	return ORB.init().create_fixed_tc(digits,scale);
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


