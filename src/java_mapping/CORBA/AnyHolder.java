package org.omg.CORBA;

/**
 * A Holder class for IDL's out/inout argument passing modes 
 *
 */

public class AnyHolder 
	implements org.omg.CORBA.portable.Streamable
{
	public Any value;

	public AnyHolder(){}

	public AnyHolder(Any o)
	{
		value = o;
	}

	public TypeCode _type()
	{
		return ORB.init().get_primitive_tc(TCKind.tk_any);
	}

	public void _read(org.omg.CORBA.portable.InputStream in)
	{
		value = in.read_any();
	}

	public void _write(org.omg.CORBA.portable.OutputStream out)
	{
		out.write_any(value);
	}

}


