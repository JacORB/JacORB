package org.omg.CORBA;

/**
 * CORBA V2.3 - 1.3 July 1998 (merged version)
 * last modified: 02/03/99 RT
 */
public abstract class Environment
{
	public abstract void exception( java.lang.Exception e);
	public abstract java.lang.Exception exception();
	public abstract void clear();
}


