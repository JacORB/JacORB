package org.omg.CORBA;

/**
 * A Holder class for IDL's out/inout argument passing modes 
 *
 */

public class DoubleHolder 
	implements org.omg.CORBA.portable.Streamable
{
	public double value;

	public DoubleHolder(){}
	public DoubleHolder(double o)
	{
		value = o;
	}
	public TypeCode _type()
	{
		return ORB.init().get_primitive_tc(TCKind.tk_double);
	}

	public void _read(org.omg.CORBA.portable.InputStream in)
	{
		value = in.read_double();
	}

	public void _write(org.omg.CORBA.portable.OutputStream out)
	{
		out.write_double(value);
	}
}


