package org.omg.CORBA.ORBPackage;


public final class ObjectIdHelper
{
	private static org.omg.CORBA.TypeCode _type = org.omg.CORBA.ORB.init().create_alias_tc( org.omg.CORBA.ORBPackage.ObjectIdHelper.id(), "ObjectId",org.omg.CORBA.ORB.init().create_string_tc(0) );
	public static void insert (org.omg.CORBA.Any any, java.lang.String s)
	{
		any.type (type ());
		write (any.create_output_stream (), s);
	}
	public static java.lang.String extract (final org.omg.CORBA.Any any)
	{
		return read (any.create_input_stream ());
	}
	public static org.omg.CORBA.TypeCode type ()
	{
		return _type;
	}
	public static String id()
	{
		return "IDL:omg.org/CORBA/ORB/ObjectId:1.0";
	}
	public static java.lang.String read (final org.omg.CORBA.portable.InputStream _in)
	{
		java.lang.String _result;
		_result=_in.read_string();
		return _result;
	}
	public static void write (final org.omg.CORBA.portable.OutputStream _out, java.lang.String _s)
	{
		_out.write_string(_s);
	}
}
