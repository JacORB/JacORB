package org.omg.PortableServer;

/** stream-based skeleton class */

import org.omg.PortableServer.POA;

public class ServantLocatorPOATie
    extends ServantLocatorPOA
{
    private ServantLocatorOperations _delegate;

    private POA _poa;
    public ServantLocatorPOATie(ServantLocatorOperations delegate)
    {
	_delegate = delegate;
    }
    public ServantLocatorPOATie(ServantLocatorOperations delegate, POA poa)
    {
	_delegate = delegate;
	_poa = poa;
    }
    public ServantLocator _this()
    {
	return ServantLocatorHelper.narrow(_this_object());
    }	

    public ServantLocatorOperations _delegate()
    {
	return _delegate;
    }
    public void _delegate(ServantLocatorOperations delegate)
    {
	_delegate = delegate;
    }
    public void postinvoke(byte[] oid, 
			   org.omg.PortableServer.POA adapter, 
			   java.lang.String operation, 
			   java.lang.Object the_cookie, 
			   org.omg.PortableServer.Servant the_servant)
	throws org.omg.PortableServer.ForwardRequest
    {
	_delegate.postinvoke(oid,adapter,operation,the_cookie,the_servant);
    }

    public org.omg.PortableServer.Servant preinvoke(byte[] oid, 
						    org.omg.PortableServer.POA adapter, 
						    java.lang.String operation, 
						    org.omg.PortableServer.ServantLocatorPackage.CookieHolder the_cookie) 
	throws org.omg.PortableServer.ForwardRequest
    {
	return _delegate.preinvoke(oid,adapter,operation,the_cookie);
    }
}


