package org.omg.CORBA;

/**
 * A Holder class for IDL's out/inout argument passing modes 
 *
 */


public class FloatHolder
	implements org.omg.CORBA.portable.Streamable
{
	public float value;

	public FloatHolder(){}

	public FloatHolder(float o)
	{
		value = o;
	}

	public TypeCode _type()
	{
		return ORB.init().get_primitive_tc(TCKind.tk_float);
	}

	public void _read(org.omg.CORBA.portable.InputStream in)
	{
		value = in.read_float();
	}

	public void _write(org.omg.CORBA.portable.OutputStream out)
	{
		out.write_float(value);
	}

}


