package org.omg.CORBA.ORBPackage;


public final class ObjectIdListHolder
	implements org.omg.CORBA.portable.Streamable
{
	public java.lang.String[] value;

	public ObjectIdListHolder ()
	{
	}
	public ObjectIdListHolder (final java.lang.String[] initial)
	{
		value = initial;
	}
	public org.omg.CORBA.TypeCode _type ()
	{
		return ObjectIdListHelper.type ();
	}
	public void _read (final org.omg.CORBA.portable.InputStream in)
	{
		value = ObjectIdListHelper.read (in);
	}
	public void _write (final org.omg.CORBA.portable.OutputStream out)
	{
		ObjectIdListHelper.write (out,value);
	}
}
