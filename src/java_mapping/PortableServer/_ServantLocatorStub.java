package org.omg.PortableServer;

public class _ServantLocatorStub
	extends org.omg.CORBA.portable.ObjectImpl
	implements org.omg.PortableServer.ServantLocator
{
	private String[] ids = {"IDL:omg.org/PortableServer/ServantLocator:1.0","IDL:omg.org/PortableServer/ServantManager:1.0"};
	public String[] _ids()
	{
		return ids;
	}

	public final static java.lang.Class _opsClass = org.omg.PortableServer.ServantLocatorOperations.class;

	public org.omg.PortableServer.Servant preinvoke(byte[] oid, org.omg.PortableServer.POA adapter, 
							java.lang.String operation, 
							org.omg.PortableServer.ServantLocatorPackage.CookieHolder the_cookie) 
	    throws org.omg.PortableServer.ForwardRequest
	{
		while(true)
		{
			org.omg.CORBA.portable.ServantObject so = _servant_preinvoke( "preinvoke", _opsClass );
			if( so == null )
				throw new org.omg.CORBA.UNKNOWN("local invocations not supported!");
			ServantLocatorOperations localServant = (ServantLocatorOperations)so.servant;
			org.omg.PortableServer.Servant _result;			try
			{
			_result = localServant.preinvoke(oid,adapter,operation,the_cookie);
			}
			finally
			{
				_servant_postinvoke(so);
			}
			return _result;
		}

	}

    public void postinvoke(byte[] oid, 
			   org.omg.PortableServer.POA adapter, 
			   java.lang.String operation, 
			   java.lang.Object the_cookie, 
			   org.omg.PortableServer.Servant the_servant)
	    throws org.omg.PortableServer.ForwardRequest
    {
		while(true)
		{
			org.omg.CORBA.portable.ServantObject so = _servant_preinvoke( "postinvoke", _opsClass );
			if( so == null )
				throw new org.omg.CORBA.UNKNOWN("local invocations not supported!");
			ServantLocatorOperations localServant = (ServantLocatorOperations)so.servant;
			try
			{
			localServant.postinvoke(oid,adapter,operation,the_cookie,the_servant);
			}
			finally
			{
				_servant_postinvoke(so);
			}
			return;
		}

	}

}


