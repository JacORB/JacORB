package org.omg.CORBA;

/**
 * A Holder class for IDL's out/inout argument passing modes 
 *
 */

public class BooleanHolder 
	implements org.omg.CORBA.portable.Streamable
{
	public boolean value;

	public BooleanHolder(){}

	public BooleanHolder(boolean o)
	{
		value = o;
	}

	public TypeCode _type()
	{
		return ORB.init().get_primitive_tc(TCKind.tk_boolean);
	}

	public void _read(org.omg.CORBA.portable.InputStream in)
	{
		value = in.read_boolean();
	}

	public void _write(org.omg.CORBA.portable.OutputStream out)
	{
		out.write_boolean(value);
	}

}


