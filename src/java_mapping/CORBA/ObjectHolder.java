package org.omg.CORBA;

/**
 * A Holder class for IDL's out/inout argument passing modes 
 *
 */

public class ObjectHolder 
	implements org.omg.CORBA.portable.Streamable
{
	public org.omg.CORBA.Object value;

	public ObjectHolder(){}
	public ObjectHolder(org.omg.CORBA.Object o)
	{
		value = o;
	}

	public TypeCode _type()
	{
		return ORB.init().get_primitive_tc(TCKind.tk_objref);
	}

	public void _read(org.omg.CORBA.portable.InputStream in)
	{
		value = in.read_Object();
	}

	public void _write(org.omg.CORBA.portable.OutputStream out)
	{
		out.write_Object(value);
	}

}


