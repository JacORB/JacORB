package org.omg.CORBA;

/**
 * orbos/98-03-10
 */
abstract public class NVList  
{
	abstract public int count();
	abstract public org.omg.CORBA.NamedValue add( int flags);
	abstract public org.omg.CORBA.NamedValue add_item( java.lang.String name, int flags );
	abstract public org.omg.CORBA.NamedValue add_value( java.lang.String name, org.omg.CORBA.Any value, int flags );
	abstract public org.omg.CORBA.NamedValue item( int index ) throws org.omg.CORBA.Bounds;
	abstract public void remove( int index ) throws  org.omg.CORBA.Bounds; 
}


