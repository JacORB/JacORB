package org.omg.PortableServer;

/** stream-based skeleton class */

public abstract class ServantActivatorPOA
	extends org.omg.PortableServer.Servant
	implements org.omg.CORBA.portable.InvokeHandler, 
    org.omg.PortableServer.ServantActivatorOperations
{

	private String[] ids = {"IDL:omg.org/PortableServer/ServantActivator:1.0",
				"IDL:omg.org/PortableServer/ServantManager:1.0"};

	public org.omg.PortableServer.ServantActivator _this()
	{
		return org.omg.PortableServer.ServantActivatorHelper.narrow(_this_object());
	}

	public org.omg.PortableServer.ServantActivator _this(org.omg.CORBA.ORB orb)
	{
		return org.omg.PortableServer.ServantActivatorHelper.narrow(_this_object(orb));
	}

	public org.omg.CORBA.portable.OutputStream _invoke(String method, 
							   org.omg.CORBA.portable.InputStream _input, 
							   org.omg.CORBA.portable.ResponseHandler handler)
		throws org.omg.CORBA.SystemException
	{       
	    throw new org.omg.CORBA.MARSHAL();	
	}

	public String[] _all_interfaces(org.omg.PortableServer.POA poa, byte[] obj_id)
	{
		return ids;
	}
}
