package org.omg.CORBA;

/**
 * A Holder class for IDL's out/inout argument passing modes 
 *
 */

public class StringHolder 
	implements org.omg.CORBA.portable.Streamable
{
	public java.lang.String value;

	public StringHolder(){}

	public StringHolder(java.lang.String o)
	{
		value = o;
	}

	public TypeCode _type()
	{
		return ORB.init().get_primitive_tc(TCKind.tk_string);
	}

	public void _read(org.omg.CORBA.portable.InputStream in)
	{
		value = in.read_string();
	}

	public void _write(org.omg.CORBA.portable.OutputStream out)
	{
		out.write_string(value);
	}
}


