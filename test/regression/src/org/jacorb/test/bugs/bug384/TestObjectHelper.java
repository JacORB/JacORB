package org.jacorb.test.bugs.bug384;


/**
 *	Generated from IDL definition of interface "TestObject"
 *	@author JacORB IDL compiler 
 */

public final class TestObjectHelper
{
	public static void insert (final org.omg.CORBA.Any any, final org.jacorb.test.bugs.bug384.TestObject s)
	{
			any.insert_Object( s );
	}
	public static org.jacorb.test.bugs.bug384.TestObject extract (final org.omg.CORBA.Any any)
	{
		return narrow (any.extract_Object ());
	}
	public static org.omg.CORBA.TypeCode type ()
	{
		return org.omg.CORBA.ORB.init().create_interface_tc( "IDL:org/jacorb/test/bugs/bug384/TestObject:1.0", "TestObject");
	}
	public static String id()
	{
		return "IDL:org/jacorb/test/bugs/bug384/TestObject:1.0";
	}
	public static TestObject read (final org.omg.CORBA.portable.InputStream in)
	{
		return narrow (in.read_Object ());
	}
	public static void write (final org.omg.CORBA.portable.OutputStream _out, final org.jacorb.test.bugs.bug384.TestObject s)
	{
		_out.write_Object(s);
	}
	public static org.jacorb.test.bugs.bug384.TestObject narrow (final java.lang.Object obj)
	{
		if( obj instanceof org.jacorb.test.bugs.bug384.TestObject)
		{
			return (org.jacorb.test.bugs.bug384.TestObject)obj;
		}
		else if( obj instanceof org.omg.CORBA.Object )
		{
			return narrow((org.omg.CORBA.Object)obj);
		}
		throw new org.omg.CORBA.BAD_PARAM("Failed to narrow in helper");
	}
	public static org.jacorb.test.bugs.bug384.TestObject narrow (final org.omg.CORBA.Object obj)
	{
		if( obj == null )
			return null;
		try
		{
			return (org.jacorb.test.bugs.bug384.TestObject)obj;
		}
		catch( ClassCastException c )
		{
			if( obj._is_a("IDL:org/jacorb/test/bugs/bug384/TestObject:1.0"))
			{
				org.jacorb.test.bugs.bug384._TestObjectStub stub;
				stub = new org.jacorb.test.bugs.bug384._TestObjectStub();
				stub._set_delegate(((org.omg.CORBA.portable.ObjectImpl)obj)._get_delegate());
				return stub;
			}
		}
		throw new org.omg.CORBA.BAD_PARAM("Narrow failed");
	}
	public static org.jacorb.test.bugs.bug384.TestObject unchecked_narrow (final org.omg.CORBA.Object obj)
	{
		if( obj == null )
			return null;
		try
		{
			return (org.jacorb.test.bugs.bug384.TestObject)obj;
		}
		catch( ClassCastException c )
		{
				org.jacorb.test.bugs.bug384._TestObjectStub stub;
				stub = new org.jacorb.test.bugs.bug384._TestObjectStub();
				stub._set_delegate(((org.omg.CORBA.portable.ObjectImpl)obj)._get_delegate());
				return stub;
		}
	}
}
