package org.omg.PortableServer;

/** stream-based skeleton class */

public abstract class ServantLocatorPOA
    extends org.omg.PortableServer.Servant
    implements org.omg.CORBA.portable.InvokeHandler, org.omg.PortableServer.ServantLocatorOperations
{
    public ServantLocator _this()
    {
	return ServantLocatorHelper.narrow(_this_object());
    }	

    public ServantLocator _this(org.omg.CORBA.ORB orb)
    {
	return ServantLocatorHelper.narrow(_this_object(orb));
    }	

    private String[] ids = {"IDL:omg.org/PortableServer/ServantLocator:1.0",
			    "IDL:omg.org/PortableServer/ServantManager:1.0"};

    public String[] _all_interfaces(org.omg.PortableServer.POA poa, byte[] obj_id)
    {
	return ids;
    }

    public org.omg.CORBA.portable.OutputStream _invoke(String method, 
						       org.omg.CORBA.portable.InputStream input, 
						       org.omg.CORBA.portable.ResponseHandler handler)
	throws org.omg.CORBA.SystemException
    {	
	throw new org.omg.CORBA.MARSHAL();	
    }


}


