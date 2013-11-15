package org.jacorb.test.bugs.bugjac788Compat;


/**
 * Generated from IDL interface "ComputInterface".
 *
 * @author JacORB IDL compiler V 2.3.1, 27-May-2009
 * @version generated at 23-Mar-2012 12:48:37
 */

public final class ComputInterfaceHelper
{
	public static void insert (final org.omg.CORBA.Any any, final org.jacorb.test.bugs.bugjac788Compat.ComputInterface s)
	{
			any.insert_Object(s);
	}
	public static org.jacorb.test.bugs.bugjac788Compat.ComputInterface extract(final org.omg.CORBA.Any any)
	{
		return narrow(any.extract_Object()) ;
	}
	public static org.omg.CORBA.TypeCode type()
	{
		return org.omg.CORBA.ORB.init().create_interface_tc("IDL:org/jacorb/test/bugs.bugjac788Compat/ComputInterface:1.0", "ComputInterface");
	}
	public static String id()
	{
		return "IDL:org/jacorb/test/bugs.bugjac788Compat/ComputInterface:1.0";
	}
	public static ComputInterface read(final org.omg.CORBA.portable.InputStream in)
	{
		return narrow(in.read_Object(org.jacorb.test.bugs.bugjac788Compat._ComputInterfaceStub.class));
	}
	public static void write(final org.omg.CORBA.portable.OutputStream _out, final org.jacorb.test.bugs.bugjac788Compat.ComputInterface s)
	{
		_out.write_Object(s);
	}
	public static org.jacorb.test.bugs.bugjac788Compat.ComputInterface narrow(final org.omg.CORBA.Object obj)
	{
		if (obj == null)
		{
			return null;
		}
		else if (obj instanceof org.jacorb.test.bugs.bugjac788Compat.ComputInterface)
		{
			return (org.jacorb.test.bugs.bugjac788Compat.ComputInterface)obj;
		}
		else if (obj._is_a("IDL:org/jacorb/test/bugs.bugjac788Compat/ComputInterface:1.0"))
		{
			org.jacorb.test.bugs.bugjac788Compat._ComputInterfaceStub stub;
			stub = new org.jacorb.test.bugs.bugjac788Compat._ComputInterfaceStub();
			stub._set_delegate(((org.omg.CORBA.portable.ObjectImpl)obj)._get_delegate());
			return stub;
		}
		else
		{
			throw new org.omg.CORBA.BAD_PARAM("Narrow failed");
		}
	}
	public static org.jacorb.test.bugs.bugjac788Compat.ComputInterface unchecked_narrow(final org.omg.CORBA.Object obj)
	{
		if (obj == null)
		{
			return null;
		}
		else if (obj instanceof org.jacorb.test.bugs.bugjac788Compat.ComputInterface)
		{
			return (org.jacorb.test.bugs.bugjac788Compat.ComputInterface)obj;
		}
		else
		{
			org.jacorb.test.bugs.bugjac788Compat._ComputInterfaceStub stub;
			stub = new org.jacorb.test.bugs.bugjac788Compat._ComputInterfaceStub();
			stub._set_delegate(((org.omg.CORBA.portable.ObjectImpl)obj)._get_delegate());
			return stub;
		}
	}
}
