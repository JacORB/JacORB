package org.omg.CORBA.ORBPackage;


public final class InvalidNameHelper
{
	private static org.omg.CORBA.TypeCode _type = org.omg.CORBA.ORB.init().create_exception_tc( org.omg.CORBA.ORBPackage.InvalidNameHelper.id(),"InvalidName",new org.omg.CORBA.StructMember[0]);
	public static void insert (final org.omg.CORBA.Any any, final org.omg.CORBA.ORBPackage.InvalidName s)
	{
		any.type(type());
		write( any.create_output_stream(),s);
	}
	public static org.omg.CORBA.ORBPackage.InvalidName extract (final org.omg.CORBA.Any any)
	{
		return read(any.create_input_stream());
	}
	public static org.omg.CORBA.TypeCode type()
	{
		return _type;
	}
	public static String id()
	{
		return "IDL:omg.org/CORBA/ORB/InvalidName:1.0";
	}
	public static org.omg.CORBA.ORBPackage.InvalidName read (final org.omg.CORBA.portable.InputStream in)
	{
		org.omg.CORBA.ORBPackage.InvalidName result = new org.omg.CORBA.ORBPackage.InvalidName();
		if(!in.read_string().equals(id())) throw new org.omg.CORBA.MARSHAL("wrong id");
		return result;
	}
	public static void write (final org.omg.CORBA.portable.OutputStream out, final org.omg.CORBA.ORBPackage.InvalidName s)
	{
		out.write_string(id());
	}
}
