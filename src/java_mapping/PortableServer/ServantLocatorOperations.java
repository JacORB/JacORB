package org.omg.PortableServer;

/** manually edited to get cookie types right */


public interface ServantLocatorOperations
	extends org.omg.PortableServer.ServantManagerOperations
{
	public void postinvoke(byte[] oid, org.omg.PortableServer.POA adapter, java.lang.String operation, java.lang.Object the_cookie, org.omg.PortableServer.Servant the_servant);
	public org.omg.PortableServer.Servant preinvoke(byte[] oid, org.omg.PortableServer.POA adapter, java.lang.String operation, org.omg.PortableServer.ServantLocatorPackage.CookieHolder the_cookie) throws org.omg.PortableServer.ForwardRequest;
}

