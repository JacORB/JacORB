package org.omg.PortableServer;
public class CurrentHelper
	implements org.omg.CORBA.portable.Helper
{
	public CurrentHelper()
	{
	}
	public static void insert(org.omg.CORBA.Any any, org.omg.PortableServer.Current s)
	{
	    any.insert_Streamable( (new org.omg.PortableServer.CurrentHolder(s)));
	}
	public static org.omg.PortableServer.Current extract(org.omg.CORBA.Any any)
	{
	    org.omg.PortableServer.CurrentHolder h = (org.omg.PortableServer.CurrentHolder)any.extract_Streamable();
	    return h.value;
	}
	public static org.omg.CORBA.TypeCode type()
	{
		return org.omg.CORBA.ORB.init().create_interface_tc( "IDL:omg.org/PortableServer/Current:1.0", "Current");
	}
	public static String id()
	{
		return "pseudo object";
	}
	public static Current read(org.omg.CORBA.portable.InputStream in)
	{
	    throw new org.omg.CORBA.MARSHAL();
	}
	public static void write(org.omg.CORBA.portable.OutputStream out, org.omg.PortableServer.Current s)
	{
	    throw new org.omg.CORBA.MARSHAL();
	}
	public static org.omg.PortableServer.Current narrow(org.omg.CORBA.Object obj)
	{
	    return (org.omg.PortableServer.Current)obj;
	}
	public void write_Object(org.omg.CORBA.portable.OutputStream out, java.lang.Object obj)
	{
	    throw new org.omg.CORBA.MARSHAL();

	}
	public java.lang.Object read_Object(org.omg.CORBA.portable.InputStream in)
	{
	    throw new org.omg.CORBA.MARSHAL();

	}
	public String get_id()
	{
	    throw new org.omg.CORBA.MARSHAL();
	}
	public org.omg.CORBA.TypeCode get_type()
	{
	    throw new org.omg.CORBA.MARSHAL();
	}
}


