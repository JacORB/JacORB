package org.jacorb.test.bugs.bug384;

/**
 *	Generated from IDL definition of interface "TestObject"
 *	@author JacORB IDL compiler 
 */

public final class TestObjectHolder	implements org.omg.CORBA.portable.Streamable{
	 public TestObject value;
	public TestObjectHolder ()
	{
	}
	public TestObjectHolder (final TestObject initial)
	{
		value = initial;
	}
	public org.omg.CORBA.TypeCode _type ()
	{
		return TestObjectHelper.type ();
	}
	public void _read (final org.omg.CORBA.portable.InputStream in)
	{
		value = TestObjectHelper.read (in);
	}
	public void _write (final org.omg.CORBA.portable.OutputStream _out)
	{
		TestObjectHelper.write (_out,value);
	}
}
