package org.jacorb.test.bugs.bugjac788Compat;

/**
 * Generated from IDL interface "HelloInterface".
 *
 * @author JacORB IDL compiler V 2.3.1, 27-May-2009
 * @version generated at 23-Mar-2012 12:48:37
 */

public final class HelloInterfaceHolder	implements org.omg.CORBA.portable.Streamable{
	 public HelloInterface value;
	public HelloInterfaceHolder()
	{
	}
	public HelloInterfaceHolder (final HelloInterface initial)
	{
		value = initial;
	}
	public org.omg.CORBA.TypeCode _type()
	{
		return HelloInterfaceHelper.type();
	}
	public void _read (final org.omg.CORBA.portable.InputStream in)
	{
		value = HelloInterfaceHelper.read (in);
	}
	public void _write (final org.omg.CORBA.portable.OutputStream _out)
	{
		HelloInterfaceHelper.write (_out,value);
	}
}
