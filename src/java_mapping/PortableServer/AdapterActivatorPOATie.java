package org.omg.PortableServer;

/** stream-based skeleton class */

import org.omg.PortableServer.POA;
public class AdapterActivatorPOATie
	extends AdapterActivatorPOA
{
	private AdapterActivatorOperations _delegate;

	private POA _poa;
	public AdapterActivatorPOATie(AdapterActivatorOperations delegate)
	{
		_delegate = delegate;
	}
	public AdapterActivatorPOATie(AdapterActivatorOperations delegate, POA poa)
	{
		_delegate = delegate;
		_poa = poa;
	}
	public org.omg.PortableServer.AdapterActivator _this()
	{
		return org.omg.PortableServer.AdapterActivatorHelper.narrow(_this_object());
	}
	public org.omg.PortableServer.AdapterActivator _this(org.omg.CORBA.ORB orb)
	{
		return org.omg.PortableServer.AdapterActivatorHelper.narrow(_this_object(orb));
	}
	public AdapterActivatorOperations _delegate()
	{
		return _delegate;
	}
	public void _delegate(AdapterActivatorOperations delegate)
	{
		_delegate = delegate;
	}
	public boolean unknown_adapter(org.omg.PortableServer.POA parent, java.lang.String name)
	{
		return _delegate.unknown_adapter(parent,name);
	}

}
