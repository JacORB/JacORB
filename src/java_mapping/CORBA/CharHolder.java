package org.omg.CORBA;

/**
 * A Holder class for IDL's out/inout argument passing modes 
 *
 */

public class CharHolder 
	implements org.omg.CORBA.portable.Streamable
{
	public char value;

	public CharHolder(){}

	public CharHolder(char o)
	{
		value = o;
	}

	public TypeCode _type()
	{
		return ORB.init().get_primitive_tc(TCKind.tk_char);
	}

	public void _read(org.omg.CORBA.portable.InputStream in)
	{
		value = in.read_char();
	}

	public void _write(org.omg.CORBA.portable.OutputStream out)
	{
		out.write_char(value);
	}

}


