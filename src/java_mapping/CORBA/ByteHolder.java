package org.omg.CORBA;

/**
 * A Holder class for IDL's out/inout argument passing modes 
 *
*/

public class ByteHolder
	implements org.omg.CORBA.portable.Streamable
{
	public byte value;

	public ByteHolder(){}
	public ByteHolder(byte o)
	{
		value = o;
	}

	public TypeCode _type()
	{
		return ORB.init().get_primitive_tc(TCKind.tk_octet);
	}

	public void _read(org.omg.CORBA.portable.InputStream in)
	{
		value = in.read_octet();
	}

	public void _write(org.omg.CORBA.portable.OutputStream out)
	{
		out.write_octet(value);
	}
}


