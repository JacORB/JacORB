package org.omg.PortableServer;
public class ServantLocatorHelper
{
	public ServantLocatorHelper()
	{
	}
	public static void insert(org.omg.CORBA.Any any, org.omg.PortableServer.ServantLocator s)
	{
		any.insert_Object(s);
	}
	public static org.omg.PortableServer.ServantLocator extract(org.omg.CORBA.Any any)
	{
		return narrow(any.extract_Object());
	}
	public static org.omg.CORBA.TypeCode type()
	{
		return org.omg.CORBA.ORB.init().create_interface_tc( "IDL:omg.org/PortableServer/ServantLocator:1.0", "ServantLocator");
	}
	public static String id()
	{
		return "IDL:omg.org/PortableServer/ServantLocator:1.0";
	}
	public static ServantLocator read(org.omg.CORBA.portable.InputStream in)
	{
		return narrow( in.read_Object());
	}
	public static void write(org.omg.CORBA.portable.OutputStream _out, org.omg.PortableServer.ServantLocator s)
	{
		_out.write_Object(s);
	}
	public static org.omg.PortableServer.ServantLocator narrow(org.omg.CORBA.Object obj)
	{
		try
		{
			return (org.omg.PortableServer.ServantLocator)obj;
		}
		catch( ClassCastException c )
		{
			if( obj._is_a("IDL:omg.org/PortableServer/ServantLocator:1.0"))
			{
				org.omg.PortableServer._ServantLocatorStub stub;
				stub = new org.omg.PortableServer._ServantLocatorStub();
				stub._set_delegate(((org.omg.CORBA.portable.ObjectImpl)obj)._get_delegate());
				return stub;
			}
		}
		return null;
	}
	public void write_Object(org.omg.CORBA.portable.OutputStream _out, java.lang.Object obj)
	{
		 throw new RuntimeException(" not implemented");
	}
	public java.lang.Object read_Object(org.omg.CORBA.portable.InputStream in)
	{
		 throw new RuntimeException(" not implemented");
	}
	public String get_id()
	{
		return id();
	}
	public org.omg.CORBA.TypeCode get_type()
	{
		return type();
	}
}
