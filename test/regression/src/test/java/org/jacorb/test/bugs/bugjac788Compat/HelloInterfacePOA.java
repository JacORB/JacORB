package org.jacorb.test.bugs.bugjac788Compat;


/**
 * Generated from IDL interface "HelloInterface".
 *
 * @author JacORB IDL compiler V 2.3.1, 27-May-2009
 * @version generated at 23-Mar-2012 12:48:37
 */

@SuppressWarnings({"unchecked","rawtypes"})
public abstract class HelloInterfacePOA
	extends org.omg.PortableServer.Servant
	implements org.omg.CORBA.portable.InvokeHandler, org.jacorb.test.bugs.bugjac788Compat.HelloInterfaceOperations
{
    static private final java.util.Hashtable m_opsHash = new java.util.Hashtable();
	static
	{
		m_opsHash.put ( "hello", new java.lang.Integer(0));
		m_opsHash.put ( "send_TRANSIENT_exception", new java.lang.Integer(1));
	}
	private String[] ids = {"IDL:org/jacorb/test/bugs.bugjac788Compat/HelloInterface:1.0"};
	public org.jacorb.test.bugs.bugjac788Compat.HelloInterface _this()
	{
		return org.jacorb.test.bugs.bugjac788Compat.HelloInterfaceHelper.narrow(_this_object());
	}
	public org.jacorb.test.bugs.bugjac788Compat.HelloInterface _this(org.omg.CORBA.ORB orb)
	{
		return org.jacorb.test.bugs.bugjac788Compat.HelloInterfaceHelper.narrow(_this_object(orb));
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
			case 0: // hello
			{
				_out = handler.createReply();
				hello();
				break;
			}
			case 1: // send_TRANSIENT_exception
			{
				_out = handler.createReply();
				send_TRANSIENT_exception();
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
