package org.omg.CORBA.ORBPackage;
public class InvalidNameHelper
{
	private static org.omg.CORBA.TypeCode _type = org.omg.CORBA.ORB.init().create_struct_tc(org.omg.CORBA.ORBPackage.InvalidNameHelper.id(),"InvalidName",new org.omg.CORBA.StructMember[0]);
	public InvalidNameHelper ()
	{
	}
	public static void insert(org.omg.CORBA.Any any, org.omg.CORBA.ORBPackage.InvalidName s)
	{
		any.type(type());
		write( any.create_output_stream(),s);
	}
	public static org.omg.CORBA.ORBPackage.InvalidName extract(org.omg.CORBA.Any any)
	{
		return read(any.create_input_stream());
	}
	public static org.omg.CORBA.TypeCode type()
	{
		return _type;
	}
	public String get_id()
	{
		return id();
	}
	public org.omg.CORBA.TypeCode get_type()
	{
		return type();
	}
	public void write_Object(org.omg.CORBA.portable.OutputStream out, java.lang.Object obj)
	{
		 throw new RuntimeException(" not implemented");
	}
	public java.lang.Object read_Object(org.omg.CORBA.portable.InputStream in)
	{
		 throw new RuntimeException(" not implemented");
	}
	public static String id()
	{
		return "IDL:org/omg/CORBA/ORB/InvalidName:1.0";
	}
	public static org.omg.CORBA.ORBPackage.InvalidName read(org.omg.CORBA.portable.InputStream in)
	{
		org.omg.CORBA.ORBPackage.InvalidName result = new org.omg.CORBA.ORBPackage.InvalidName();
		if(!in.read_string().equals(id())) throw new org.omg.CORBA.MARSHAL("wrong id");
		return result;
	}
	public static void write(org.omg.CORBA.portable.OutputStream out, org.omg.CORBA.ORBPackage.InvalidName s)
	{
		out.write_string(id());
	}
}
