package org.jacorb.test.bugs.bug384;

/**
 *	Generated from IDL definition of interface "TestObject"
 *	@author JacORB IDL compiler 
 */


public abstract class TestObjectPOA
	extends org.omg.PortableServer.Servant
	implements org.omg.CORBA.portable.InvokeHandler, org.jacorb.test.bugs.bug384.TestObjectOperations
{
	static private final java.util.Hashtable m_opsHash = new java.util.Hashtable();
	static
	{
		m_opsHash.put ( "ping", new java.lang.Integer(0));
	}
	private String[] ids = {"IDL:org/jacorb/test/bugs/bug384/TestObject:1.0"};
	public org.jacorb.test.bugs.bug384.TestObject _this()
	{
		return org.jacorb.test.bugs.bug384.TestObjectHelper.narrow(_this_object());
	}
	public org.jacorb.test.bugs.bug384.TestObject _this(org.omg.CORBA.ORB orb)
	{
		return org.jacorb.test.bugs.bug384.TestObjectHelper.narrow(_this_object(orb));
	}
	public org.omg.CORBA.portable.OutputStream _invoke(String method, org.omg.CORBA.portable.InputStream _input, org.omg.CORBA.portable.ResponseHandler handler)
		throws org.omg.CORBA.SystemException
	{
		org.omg.CORBA.portable.OutputStream _out = null;
		// do something
		// quick lookup of operation
		java.lang.Integer opsIndex = (java.lang.Integer)m_opsHash.get ( method );
		if ( null == opsIndex )
			throw new org.omg.CORBA.BAD_OPERATION(method + " not found");
		switch ( opsIndex.intValue() )
		{
			case 0: // ping
			{
				_out = handler.createReply();
				ping();
				break;
			}
		}
		return _out;
	}

	public String[] _all_interfaces(org.omg.PortableServer.POA poa, byte[] obj_id)
	{
		return ids;
	}
}
