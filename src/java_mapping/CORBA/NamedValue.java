package org.omg.CORBA;

/**
 * orbos/98-03-10
 */
abstract public class NamedValue
{
	abstract public java.lang.String name();
	abstract public org.omg.CORBA.Any value();
	abstract public int flags();
}


