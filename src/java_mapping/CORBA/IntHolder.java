package org.omg.CORBA;

/**
 * A Holder class for IDL's out/inout argument passing modes 
 *
 */

final public class IntHolder
	implements org.omg.CORBA.portable.Streamable
{
	public int value;

	public IntHolder(){}

	public IntHolder(int o)
	{
		value = o;
	}

	public TypeCode _type()
	{
		return ORB.init().get_primitive_tc(TCKind.tk_long);
	}

	public void _read(org.omg.CORBA.portable.InputStream in)
	{
		value = in.read_long();
	}

	public void _write(org.omg.CORBA.portable.OutputStream out)
	{
		out.write_long(value);
	}

}


