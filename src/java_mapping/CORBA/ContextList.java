package org.omg.CORBA;

/**
 * orbos/98-03-10
 */
abstract public class ContextList
{
	abstract public int count();
	abstract public void add( java.lang.String ctx );
	abstract public java.lang.String item( int index ) throws org.omg.CORBA.Bounds;
	abstract public void remove( int index ) throws org.omg.CORBA.Bounds; 
}


