package org.jacorb.test.bugs.bugjac788Compat;

/**
 * Generated from IDL interface "ComputInterface".
 *
 * @author JacORB IDL compiler V 2.3.1, 27-May-2009
 * @version generated at 23-Mar-2012 12:48:37
 */

public final class ComputInterfaceHolder	implements org.omg.CORBA.portable.Streamable{
	 public ComputInterface value;
	public ComputInterfaceHolder()
	{
	}
	public ComputInterfaceHolder (final ComputInterface initial)
	{
		value = initial;
	}
	public org.omg.CORBA.TypeCode _type()
	{
		return ComputInterfaceHelper.type();
	}
	public void _read (final org.omg.CORBA.portable.InputStream in)
	{
		value = ComputInterfaceHelper.read (in);
	}
	public void _write (final org.omg.CORBA.portable.OutputStream _out)
	{
		ComputInterfaceHelper.write (_out,value);
	}
}
