package org.omg.CORBA;

/** 
 * CORBA V2.3 - 1.3 July 1998 (merged version)
 *
 * @deprecated Deprecated by CORBA 2.2
 *
 * last modified: 02/03/99 RT
 */
public class PrincipalHolder implements org.omg.CORBA.portable.Streamable {

	public Principal value;

	public PrincipalHolder(){}
	public PrincipalHolder(Principal o)
	{
		value = o;
	}
	public TypeCode _type()
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


