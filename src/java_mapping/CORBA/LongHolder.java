package org.omg.CORBA;

/**
 * A Holder class for IDL's out/inout argument passing modes 
 *
 */

final public class LongHolder
	implements org.omg.CORBA.portable.Streamable
{
	public long value;

	public LongHolder(){}

	public LongHolder(long o)
	{
		value = o;
	}

	public TypeCode _type()
	{
		return ORB.init().get_primitive_tc(TCKind.tk_longlong);
	}

	public void _read(org.omg.CORBA.portable.InputStream in)
	{
		value = in.read_longlong();
	}

	public void _write(org.omg.CORBA.portable.OutputStream out)
	{
		out.write_longlong(value);
	}
}


