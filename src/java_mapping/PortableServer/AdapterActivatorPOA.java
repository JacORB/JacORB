package org.omg.PortableServer;

/** stream-based skeleton class */

public abstract class AdapterActivatorPOA
	extends org.omg.PortableServer.Servant
	implements org.omg.CORBA.portable.InvokeHandler, org.omg.PortableServer.AdapterActivatorOperations
{
	static private final java.util.Hashtable m_opsHash = new java.util.Hashtable();
	static
	{
		m_opsHash.put ( "unknown_adapter", new java.lang.Integer(0));
	}
	private String[] ids = {"IDL:omg.org/PortableServer/AdapterActivator:1.0"};
	public org.omg.PortableServer.AdapterActivator _this()
	{
		return org.omg.PortableServer.AdapterActivatorHelper.narrow(_this_object());
	}
	public org.omg.PortableServer.AdapterActivator _this(org.omg.CORBA.ORB orb)
	{
		return org.omg.PortableServer.AdapterActivatorHelper.narrow(_this_object(orb));
	}
	public org.omg.CORBA.portable.OutputStream _invoke(String method, org.omg.CORBA.portable.InputStream _input, org.omg.CORBA.portable.ResponseHandler handler)
		throws org.omg.CORBA.SystemException
	{
		org.omg.CORBA.portable.OutputStream _out = null;
		// do something
		org.omg.CORBA.portable.OutputStream out = null;
		// quick lookup of operation
		java.lang.Integer opsIndex = (java.lang.Integer)m_opsHash.get ( method );
		if ( null == opsIndex )
			throw new org.omg.CORBA.BAD_OPERATION(method + " not found");
		switch ( opsIndex.intValue() )
		{
			case 0: // unknown_adapter
			{
				org.omg.PortableServer.POA _arg0=org.omg.PortableServer.POAHelper.read(_input);
				java.lang.String _arg1=_input.read_string();
				_out = handler.createReply();
				_out.write_boolean(unknown_adapter(_arg0,_arg1));
				break;
			}
		}
		return out;
	}

	public String[] _all_interfaces(org.omg.PortableServer.POA poa, byte[] obj_id)
	{
		return ids;
	}
}
