package org.jacorb.test.bugs.bugjac788Compat;

import org.omg.PortableServer.POA;

/**
 * Generated from IDL interface "HelloInterface".
 *
 * @author JacORB IDL compiler V 2.3.1, 27-May-2009
 * @version generated at 23-Mar-2012 12:48:37
 */

public class HelloInterfacePOATie
	extends HelloInterfacePOA
{
	private HelloInterfaceOperations _delegate;

	private POA _poa;
	public HelloInterfacePOATie(HelloInterfaceOperations delegate)
	{
		_delegate = delegate;
	}
	public HelloInterfacePOATie(HelloInterfaceOperations delegate, POA poa)
	{
		_delegate = delegate;
		_poa = poa;
	}
	public org.jacorb.test.bugs.bugjac788Compat.HelloInterface _this()
	{
		return org.jacorb.test.bugs.bugjac788Compat.HelloInterfaceHelper.narrow(_this_object());
	}
	public org.jacorb.test.bugs.bugjac788Compat.HelloInterface _this(org.omg.CORBA.ORB orb)
	{
		return org.jacorb.test.bugs.bugjac788Compat.HelloInterfaceHelper.narrow(_this_object(orb));
	}
	public HelloInterfaceOperations _delegate()
	{
		return _delegate;
	}
	public void _delegate(HelloInterfaceOperations delegate)
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
	public void hello()
	{
_delegate.hello();
	}

	public void send_TRANSIENT_exception()
	{
_delegate.send_TRANSIENT_exception();
	}

}
