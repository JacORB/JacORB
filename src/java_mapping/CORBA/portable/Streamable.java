package org.omg.CORBA.portable; 

/**
 * CORBA V2.3 - 1.3 July 1998 (merged version)
 * last modified: 02/03/99 RT
 */
public interface Streamable {   
	void _read(org.omg.CORBA.portable.InputStream in);
	void _write(org.omg.CORBA.portable.OutputStream out);
	org.omg.CORBA.TypeCode _type(); 
}


