package org.omg.CORBA;

/**
 * CORBA V2.3 - 1.3 July 1998 (merged version)
 *
 * incomplete!!!
 *
 * last modified: 02/03/99 RT
 */
final public class UnknownUserExceptionHolder implements org.omg.CORBA.portable.Streamable {

	public org.omg.CORBA.UnknownUserException value;

	public UnknownUserExceptionHolder ()
	{
	}
	public UnknownUserExceptionHolder (org.omg.CORBA.UnknownUserException initial)
	{
		value = initial;
	}

	/** 
	 * incomplete
	 */
	public void _read(org.omg.CORBA.portable.InputStream in)
	{
		value = new UnknownUserException();
	}

	/** 
	 * incomplete
	 */
	public org.omg.CORBA.TypeCode _type()
	{
		return org.omg.CORBA.ORB.init().create_struct_tc("IDL:UnknownUserException:1.0", "UnknownUserException",null);
	}

	/** 
	 * incomplete
	 */
	public void _write(org.omg.CORBA.portable.OutputStream out)
	{
	}
}


