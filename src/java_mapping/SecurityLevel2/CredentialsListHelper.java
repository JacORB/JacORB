package org.omg.SecurityLevel2;
public class CredentialsListHelper
	implements org.omg.CORBA.portable.Helper
{
	private static org.omg.CORBA.TypeCode _type = org.omg.CORBA.ORB.init().create_sequence_tc(0, org.omg.CORBA.ORB.init().create_interface_tc( "IDL:omg.org/SecurityLevel2/Credentials:1.0", "Credentials"));
	public CredentialsListHelper ()
	{
	}
	public static void insert(org.omg.CORBA.Any any, org.omg.SecurityLevel2.Credentials[] s)
	{
		any.type(type());
		write( any.create_output_stream(),s);
	}
	public static org.omg.SecurityLevel2.Credentials[] extract(org.omg.CORBA.Any any)
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
		return "IDL:omg.org/SecurityLevel2/CredentialsList:1.0";
	}
	public static org.omg.SecurityLevel2.Credentials[] read(org.omg.CORBA.portable.InputStream in)
	{
	    throw new org.omg.CORBA.MARSHAL();
	}
	public static void write(org.omg.CORBA.portable.OutputStream out, org.omg.SecurityLevel2.Credentials[] s)
	{
	    throw new org.omg.CORBA.MARSHAL();
	}
}


