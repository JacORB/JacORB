package org.omg.CORBA.portable;

/**
 * CORBA V2.3 - 1.3 July 1998 (merged version)
 * last modified: 02/03/99 RT
 */
public interface Helper {

	Object read_Object(InputStream istream);
	void write_Object(OutputStream ostream, Object obj);
	String get_id();
	org.omg.CORBA.TypeCode get_type();
}


