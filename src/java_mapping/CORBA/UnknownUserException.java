package org.omg.CORBA;

/**
 * CORBA V2.3 - 1.3 July 1998 (merged version)
 * last modified: 02/03/99 RT
 */
final public class UnknownUserException extends org.omg.CORBA.UserException {
   
	public org.omg.CORBA.Any except;

	public UnknownUserException() {
		super();
	}   

	public UnknownUserException(org.omg.CORBA.Any except) 
	{     
		super();
		this.except = except;
	} 
} 


