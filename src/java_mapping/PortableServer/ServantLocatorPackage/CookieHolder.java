package org.omg.PortableServer.ServantLocatorPackage;

/**
 * CORBA V2.3 - 1.3 July 1998 (merged version)
 * last modified: 02/03/99 RT
 */
public class CookieHolder implements org.omg.CORBA.portable.Streamable {

	public java.lang.Object value;

	public CookieHolder(){}
	public CookieHolder(java.lang.Object o)
	{
		value = o;
	}

	public org.omg.CORBA.TypeCode _type()
	{
	    throw new org.omg.CORBA.NO_IMPLEMENT();
	}

	public void _read(org.omg.CORBA.portable.InputStream in)
	{
	    throw new org.omg.CORBA.NO_IMPLEMENT();	
	}

	public void _write(org.omg.CORBA.portable.OutputStream out)
	{
	    throw new org.omg.CORBA.NO_IMPLEMENT();
	}

}


