package org.omg.PortableServer;

/** stream-based skeleton class */

import org.omg.PortableServer.POA;
public class ServantActivatorPOATie
    extends ServantActivatorPOA
{
    private ServantActivatorOperations _delegate;
    private POA _poa;

    public ServantActivatorPOATie(ServantActivatorOperations delegate)
    {
	_delegate = delegate;
    }

    public ServantActivatorPOATie(ServantActivatorOperations delegate, POA poa)
    {
	_delegate = delegate;
	_poa = poa;
    }

    public ServantActivator _this()
    {
	return ServantActivatorHelper.narrow(_this_object());
    }	

    public ServantActivatorOperations _delegate()
    {
	return _delegate;
    }
    public void _delegate(ServantActivatorOperations delegate)
    {
	_delegate = delegate;
    }

    public void etherealize(byte[] oid, org.omg.PortableServer.POA adapter, 
			    org.omg.PortableServer.Servant serv, 
			    boolean cleanup_in_progress, 
			    boolean remaining_activations)
    {
	_delegate.etherealize(oid,adapter,serv,cleanup_in_progress,remaining_activations);
    }

    public org.omg.PortableServer.Servant incarnate(byte[] oid, 
						    org.omg.PortableServer.POA adapter) 
	throws org.omg.PortableServer.ForwardRequest
    {
	return _delegate.incarnate(oid,adapter);
    }
}


