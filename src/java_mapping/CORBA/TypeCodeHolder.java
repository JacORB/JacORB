package org.omg.CORBA;

/**
 * CORBA V2.3 - 1.3 July 1998 (merged version)
 * last modified: 02/03/99 RT
 */
public class TypeCodeHolder implements org.omg.CORBA.portable.Streamable {

	public TypeCode value;

	public TypeCodeHolder(){}
	public TypeCodeHolder(TypeCode o)
	{
		value = o;
	}

	public TypeCode _type()
	{
		return ORB.init().get_primitive_tc(TCKind.tk_TypeCode);
	}

	public void _read(org.omg.CORBA.portable.InputStream in)
	{
		value = in.read_TypeCode();
	}

	public void _write(org.omg.CORBA.portable.OutputStream out)
	{
		out.write_TypeCode(value);
	}
}


