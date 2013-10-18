package org.jacorb.test.bugs.bugjac788Compat;


/**
 * Generated from IDL interface "ComputInterface".
 *
 * @author JacORB IDL compiler V 2.3.1, 27-May-2009
 * @version generated at 23-Mar-2012 12:48:37
 */

public class _ComputInterfaceStub
	extends org.omg.CORBA.portable.ObjectImpl
	implements org.jacorb.test.bugs.bugjac788Compat.ComputInterface
{
	private String[] ids = {"IDL:org/jacorb/test/bugs.bugjac788Compat/ComputInterface:1.0"};
	public String[] _ids()
	{
		return ids;
	}

	@SuppressWarnings("rawtypes")
    public final static java.lang.Class _opsClass = org.jacorb.test.bugs.bugjac788Compat.ComputInterfaceOperations.class;
	public int get_result(int time_ms)
	{
		while(true)
		{
		if(! this._is_local())
		{
			org.omg.CORBA.portable.InputStream _is = null;
			org.omg.CORBA.portable.OutputStream _os = null;
			try
			{
				_os = _request( "get_result", true);
				_os.write_ulong(time_ms);
				_is = _invoke(_os);
				int _result = _is.read_long();
				return _result;
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
			org.omg.CORBA.portable.ServantObject _so = _servant_preinvoke( "get_result", _opsClass );
			if( _so == null )
				throw new org.omg.CORBA.UNKNOWN("local invocations not supported!");
			ComputInterfaceOperations _localServant = (ComputInterfaceOperations)_so.servant;
			int _result;
			try
			{
				_result = _localServant.get_result(time_ms);
			}
			finally
			{
				_servant_postinvoke(_so);
			}
			return _result;
		}

		}

	}

}
