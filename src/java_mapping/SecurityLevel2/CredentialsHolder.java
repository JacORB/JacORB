package org.omg.SecurityLevel2;

final public class CredentialsHolder
	implements org.omg.CORBA.portable.Streamable
{
	public Credentials value;

	public CredentialsHolder(){}

	public CredentialsHolder(Credentials c)
	{
		value = c;
	}

	public org.omg.CORBA.TypeCode _type()
	{
	    throw new org.omg.CORBA.MARSHAL();
	}

	public void _read(org.omg.CORBA.portable.InputStream in)
	{
	    throw new org.omg.CORBA.MARSHAL();

	}

	public void _write(org.omg.CORBA.portable.OutputStream out)
	{
	    throw new org.omg.CORBA.MARSHAL();
	}

}


