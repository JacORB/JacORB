package org.omg.CORBA;

/**
 * orbos/98-03-10
 */
abstract public class ExceptionList 
{
	abstract public int count();
	abstract public void add( org.omg.CORBA.TypeCode exc );
	abstract public org.omg.CORBA.TypeCode item( int index ) throws org.omg.CORBA.Bounds;
	abstract public void remove( int index ) throws org.omg.CORBA.Bounds; 
}


