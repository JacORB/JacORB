package org.omg.PortableServer;

public class _ServantActivatorStub
	extends org.omg.CORBA.portable.ObjectImpl
	implements org.omg.PortableServer.ServantActivator
{
	private String[] ids = {"IDL:omg.org/PortableServer/ServantActivator:1.0","IDL:omg.org/PortableServer/ServantManager:1.0"};
	public String[] _ids()
	{
		return ids;
	}

	public final static java.lang.Class _opsClass = org.omg.PortableServer.ServantActivatorOperations.class;
	public org.omg.PortableServer.Servant incarnate(byte[] oid, org.omg.PortableServer.POA adapter) throws org.omg.PortableServer.ForwardRequest
	{
		while(true)
		{
			org.omg.CORBA.portable.ServantObject _so = _servant_preinvoke( "incarnate", _opsClass );
			if( _so == null )
				throw new org.omg.CORBA.UNKNOWN("local invocations not supported!");
			ServantActivatorOperations _localServant = (ServantActivatorOperations)_so.servant;
			org.omg.PortableServer.Servant _result;			try
			{
			_result = _localServant.incarnate(oid,adapter);
			}
			finally
			{
				_servant_postinvoke(_so);
			}
			return _result;
		}

	}

	public void etherealize(byte[] oid, org.omg.PortableServer.POA adapter, org.omg.PortableServer.Servant serv, boolean cleanup_in_progress, boolean remaining_activations)
	{
		while(true)
		{
			org.omg.CORBA.portable.ServantObject _so = _servant_preinvoke( "etherealize", _opsClass );
			if( _so == null )
				throw new org.omg.CORBA.UNKNOWN("local invocations not supported!");
			ServantActivatorOperations _localServant = (ServantActivatorOperations)_so.servant;
			try
			{
			_localServant.etherealize(oid,adapter,serv,cleanup_in_progress,remaining_activations);
			}
			finally
			{
				_servant_postinvoke(_so);
			}
			return;
		}

	}

}
