package org.omg.CORBA;

/**
 * CORBA V2.3 - 1.3 July 1998 (merged version)
 * last modified: 02/03/99 RT
 */

abstract public class UserException 
	extends java.lang.Exception 
	implements org.omg.CORBA.portable.IDLEntity 
{

	public UserException() 
	{
		super();
	}

	public UserException(java.lang.String value) 
	{
		super(value);
	}
}


