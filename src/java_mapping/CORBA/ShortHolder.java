package org.omg.CORBA;

/**
 * A Holder class for IDL's out/inout argument passing modes 
 *
 */

final public class ShortHolder
	implements org.omg.CORBA.portable.Streamable
{
	public short value;

	public ShortHolder(){}

	public ShortHolder(short o)
	{
		value = o;
	}

	public TypeCode _type()
	{
		return ORB.init().get_primitive_tc(TCKind.tk_short);
	}

	public void _read(org.omg.CORBA.portable.InputStream in)
	{
		value = in.read_short();
	}

	public void _write(org.omg.CORBA.portable.OutputStream out)
	{
		out.write_short(value);
	}

}


