package org.jacorb.test.bugs.bug384;


/**
 *	Generated from IDL definition of interface "TestObject"
 *	@author JacORB IDL compiler 
 */

public class _TestObjectStub
	extends org.omg.CORBA.portable.ObjectImpl
	implements org.jacorb.test.bugs.bug384.TestObject
{
	private String[] ids = {"IDL:org/jacorb/test/bugs/bug384/TestObject:1.0"};
	public String[] _ids()
	{
		return ids;
	}

	public final static java.lang.Class _opsClass = org.jacorb.test.bugs.bug384.TestObjectOperations.class;
	public void ping()
	{
		while(true)
		{
		if(! this._is_local())
		{
			org.omg.CORBA.portable.InputStream _is = null;
			try
			{
				org.omg.CORBA.portable.OutputStream _os = _request( "ping", true);
				_is = _invoke(_os);
				return;
			}
			catch( org.omg.CORBA.portable.RemarshalException _rx ){}
			catch( org.omg.CORBA.portable.ApplicationException _ax )
			{
				String _id = _ax.getId();
				throw new RuntimeException("Unexpected exception " + _id );
			}
			finally
			{
				this._releaseReply(_is);
			}
		}
		else
		{
			org.omg.CORBA.portable.ServantObject _so = _servant_preinvoke( "ping", _opsClass );
			if( _so == null )
				throw new org.omg.CORBA.UNKNOWN("local invocations not supported!");
			TestObjectOperations _localServant = (TestObjectOperations)_so.servant;
			try
			{
			_localServant.ping();
			}
			finally
			{
				_servant_postinvoke(_so);
			}
			return;
		}

		}

	}

}
