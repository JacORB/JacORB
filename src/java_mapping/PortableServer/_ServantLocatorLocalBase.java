package org.omg.PortableServer;

/**
 * Abstract base class for implenentations of local interface ServantLocator
 * @author JacORB IDL compiler.
 */

public abstract class _ServantLocatorLocalBase
	extends org.omg.CORBA.LocalObject
	implements ServantLocator
{
	private String[] _type_ids = {"IDL:omg.org/PortableServer/ServantLocator:1.0","IDL:omg.org/CORBA/Object:1.0","IDL:omg.org/PortableServer/ServantManager:1.0"};
	public String[] _ids()	{
		return(String[])_type_ids.clone();
	}
}
