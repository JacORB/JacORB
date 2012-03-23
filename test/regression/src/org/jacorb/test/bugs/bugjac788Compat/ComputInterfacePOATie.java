package org.jacorb.test.bugs.bugjac788Compat;

import org.omg.PortableServer.POA;

/**
 * Generated from IDL interface "ComputInterface".
 *
 * @author JacORB IDL compiler V 2.3.1, 27-May-2009
 * @version generated at 23-Mar-2012 12:48:37
 */

public class ComputInterfacePOATie
	extends ComputInterfacePOA
{
	private ComputInterfaceOperations _delegate;

	private POA _poa;
	public ComputInterfacePOATie(ComputInterfaceOperations delegate)
	{
		_delegate = delegate;
	}
	public ComputInterfacePOATie(ComputInterfaceOperations delegate, POA poa)
	{
		_delegate = delegate;
		_poa = poa;
	}
	public org.jacorb.test.bugs.bugjac788Compat.ComputInterface _this()
	{
		return org.jacorb.test.bugs.bugjac788Compat.ComputInterfaceHelper.narrow(_this_object());
	}
	public org.jacorb.test.bugs.bugjac788Compat.ComputInterface _this(org.omg.CORBA.ORB orb)
	{
		return org.jacorb.test.bugs.bugjac788Compat.ComputInterfaceHelper.narrow(_this_object(orb));
	}
	public ComputInterfaceOperations _delegate()
	{
		return _delegate;
	}
	public void _delegate(ComputInterfaceOperations delegate)
	{
		_delegate = delegate;
	}
	public POA _default_POA()
	{
		if (_poa != null)
		{
			return _poa;
		}
		return super._default_POA();
	}
	public int get_result(int time_ms)
	{
		return _delegate.get_result(time_ms);
	}

}
