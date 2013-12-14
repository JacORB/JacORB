package org.jacorb.test.bugs.bugjac788Compat;


/**
 * Generated from IDL interface "HelloInterface".
 *
 * @author JacORB IDL compiler V 2.3.1, 27-May-2009
 * @version generated at 23-Mar-2012 12:48:37
 */

public class _HelloInterfaceStub
	extends org.omg.CORBA.portable.ObjectImpl
	implements org.jacorb.test.bugs.bugjac788Compat.HelloInterface
{
	private String[] ids = {"IDL:org/jacorb/test/bugs.bugjac788Compat/HelloInterface:1.0"};
	public String[] _ids()
	{
		return ids;
	}

	@SuppressWarnings("rawtypes")
    public final static java.lang.Class _opsClass = org.jacorb.test.bugs.bugjac788Compat.HelloInterfaceOperations.class;
	public void hello()
	{
		while(true)
		{
		if(! this._is_local())
		{
			org.omg.CORBA.portable.InputStream _is = null;
			org.omg.CORBA.portable.OutputStream _os = null;
			try
			{
				_os = _request( "hello", true);
				_is = _invoke(_os);
				return;
			}
			catch( org.omg.CORBA.portable.RemarshalException _rx ){}
			catch( org.omg.CORBA.portable.ApplicationException _ax )
			{
				String _id = _ax.getId();
					try
					{
							_ax.getInputStream().close();
					}
					catch (java.io.IOException e)
					{
					throw new RuntimeException("Unexpected exception " + e.toString() );
					}
				throw new RuntimeException("Unexpected exception " + _id );
			}
			finally
			{
				if (_os != null)
				{
					try
					{
						_os.close();
					}
					catch (java.io.IOException e)
					{
					throw new RuntimeException("Unexpected exception " + e.toString() );
					}
				}
				this._releaseReply(_is);
			}
		}
		else
		{
			org.omg.CORBA.portable.ServantObject _so = _servant_preinvoke( "hello", _opsClass );
			if( _so == null )
				throw new org.omg.CORBA.UNKNOWN("local invocations not supported!");
			HelloInterfaceOperations _localServant = (HelloInterfaceOperations)_so.servant;
			try
			{
				_localServant.hello();
			}
			finally
			{
				_servant_postinvoke(_so);
			}
			return;
		}

		}

	}

	public void send_TRANSIENT_exception()
	{
		while(true)
		{
		if(! this._is_local())
		{
			org.omg.CORBA.portable.InputStream _is = null;
			org.omg.CORBA.portable.OutputStream _os = null;
			try
			{
				_os = _request( "send_TRANSIENT_exception", true);
				_is = _invoke(_os);
				return;
			}
			catch( org.omg.CORBA.portable.RemarshalException _rx ){}
			catch( org.omg.CORBA.portable.ApplicationException _ax )
			{
				String _id = _ax.getId();
					try
					{
							_ax.getInputStream().close();
					}
					catch (java.io.IOException e)
					{
					throw new RuntimeException("Unexpected exception " + e.toString() );
					}
				throw new RuntimeException("Unexpected exception " + _id );
			}
			finally
			{
				if (_os != null)
				{
					try
					{
						_os.close();
					}
					catch (java.io.IOException e)
					{
					throw new RuntimeException("Unexpected exception " + e.toString() );
					}
				}
				this._releaseReply(_is);
			}
		}
		else
		{
			org.omg.CORBA.portable.ServantObject _so = _servant_preinvoke( "send_TRANSIENT_exception", _opsClass );
			if( _so == null )
				throw new org.omg.CORBA.UNKNOWN("local invocations not supported!");
			HelloInterfaceOperations _localServant = (HelloInterfaceOperations)_so.servant;
			try
			{
				_localServant.send_TRANSIENT_exception();
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
