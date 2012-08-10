package org.jacorb.orb;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2012 Gerald Brose / The JacORB Team.
 *
 *   This library is free software; you can redistribute it and/or
 *   modify it under the terms of the GNU Library General Public
 *   License as published by the Free Software Foundation; either
 *   version 2 of the License, or (at your option) any later version.
 *
 *   This library is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *   Library General Public License for more details.
 *
 *   You should have received a copy of the GNU Library General Public
 *   License along with this library; if not, write to the Free
 *   Software Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

import org.omg.CORBA.BAD_PARAM;
import org.omg.CORBA.NO_IMPLEMENT;
import org.omg.CORBA.ORB;
import org.omg.CORBA.Repository;
import org.omg.CORBA.RepositoryHelper;
import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;
import org.omg.PortableServer.Servant;
import org.omg.PortableServer.CurrentPackage.NoContext;
import org.omg.PortableServer.POAPackage.ServantNotActive;
import org.omg.PortableServer.POAPackage.WrongPolicy;

/**
 * JacORB-specific implementation of PortableServer.Servant
 *
 */
public class ServantDelegate
    implements org.omg.PortableServer.portable.Delegate
{
    private final ORB orb;
    private Repository ir = null;
    private org.omg.PortableServer.Current _current = null;
    private POA poa = null;

    ServantDelegate( org.jacorb.orb.ORB orb )
    {
        this.orb = orb;
    }

    /**
     * Must be checked for every invocation (cf. Lang. Mapping p. 1-89)
     */

    private final void check()
    {
        if (orb == null)
        {
            throw new org.omg.CORBA.BAD_INV_ORDER ("The Servant has not been associated with an ORB instance");
        }
    }

    final public org.omg.CORBA.Object this_object (org.omg.PortableServer.Servant self)
    {
        check();
        try
        {
            poa = poa(self);
        }
        catch (org.omg.CORBA.OBJ_ADAPTER e)
        {
            // Use servants default POA. Operation may be re-implemented
            // by servant implementation.

            poa = self._default_POA ();
        }

        if (poa == null)
        {
            throw new org.omg.CORBA.OBJ_ADAPTER("null value returned by  _default_POA() on Servant " + self);
        }

        try
        {
            /**
             * We need to return a duplicate of the object because when this method is called by
             * generated POA code _this() method the object returned may subsequently have it's
             * delegate set to null by the _this method.  As JacORB caches object references the
             * cached object would then have a null delegate. Any subsequent calls that read
             * the object from the cache and tried to narrow it would result in a BAD_OPERATION on
             * any attempt to retrieve the delegate.  This way the cached object always retains it's
             * delegate and it is the duplicated object that will have the delegate set to null.
             */
            return (poa.servant_to_reference(self))._duplicate();
        }
        catch(ServantNotActive e)
        {
            throw new org.omg.CORBA.OBJ_ADAPTER(e.toString());
        }
        catch(WrongPolicy e)
        {
            throw new org.omg.CORBA.OBJ_ADAPTER(e.toString());
        }
    }


    final public ORB orb(org.omg.PortableServer.Servant self)
    {
        check();
        return orb;
    }

    final public POA poa(org.omg.PortableServer.Servant self)
    {
        check();

        _getPOACurrent();

        try
        {
            // CORBA 2.4 added the get_servant() operation to the
            // PortableServer::Current interface. As of JDK 1.4.2,
            // however, the class org.omg.PortableServant.Current
            // in Sun's JDK does not have the method get_servant().
            // Instead of simply saying _current.get_servant(), below
            // we say ((org.jacorb.poa.Current)_current).get_servant().
            // The cast allows JacORB to run with the obsolete Sun class.
            if( ((org.jacorb.poa.Current)_current).get_servant() != self )
            {
                throw new org.omg.CORBA.OBJ_ADAPTER();
            }

            return _current.get_POA();
        }
        catch(NoContext e)
        {
            throw new org.omg.CORBA.OBJ_ADAPTER(e.toString());
        }
    }

    final public byte[] object_id(org.omg.PortableServer.Servant self)
    {
        check();

        _getPOACurrent();

        try
        {
            return _current.get_object_id();
        }
        catch(NoContext e)
        {
            throw new org.omg.CORBA.OBJ_ADAPTER(e.toString());
        }
    }

    private synchronized void _getPOACurrent()
    {
        if (_current == null)
        {
            try
            {
                _current = org.omg.PortableServer.CurrentHelper.narrow(orb.resolve_initial_references("POACurrent"));
            }
            catch (Exception e)
            {
                throw new org.omg.CORBA.INITIALIZE(e.toString());
            }
        }
    }

    public POA default_POA(org.omg.PortableServer.Servant self)
    {
        check();
        try
        {
            return POAHelper.narrow(orb(self).resolve_initial_references("RootPOA"));
        }
        catch(InvalidName e)
        {
            throw new org.omg.CORBA.INITIALIZE(e.toString());
        }
    }

    public boolean non_existent(org.omg.PortableServer.Servant self)
    {
        check();
        return false;
    }

    public org.omg.CORBA.Object get_component(org.omg.PortableServer.Servant self)
    {
        check();
        return null;
    }

    public org.omg.CORBA.Object get_interface_def( org.omg.PortableServer.Servant self)
    {
        check();
        if ( ir == null)
        {
            try
            {
                ir = RepositoryHelper.narrow(orb.resolve_initial_references("InterfaceRepository"));
            }
            catch (Exception e)
            {
                throw new org.omg.CORBA.INITIALIZE(e.toString());
            }
        }
        return ir.lookup_id( ((org.omg.CORBA.portable.ObjectImpl)self._this_object())._ids()[0] );
    }

    public org.omg.CORBA.InterfaceDef get_interface(org.omg.PortableServer.Servant self)
    {
        return org.omg.CORBA.InterfaceDefHelper.narrow( get_interface_def( self ));
    }

    public boolean is_a(org.omg.PortableServer.Servant self, String repid)
    {
        String [] intf = self._all_interfaces(poa(self), object_id(self));

        for( int i = 0; i < intf.length; i++)
        {
            if( intf[i].equals(repid))
            {
                return true;
            }
        }
        return "IDL:omg.org/CORBA/Object:1.0".equals (repid);
    }


    /**
     * _get_policy
     */

    public org.omg.CORBA.Policy _get_policy(org.omg.CORBA.Object self,
                                            int policy_type)
    {
        return poa != null ? ((org.jacorb.poa.POA)poa).getPolicy(policy_type) : null;
    }

    /**
     * _get_domain_managers
     */

    public org.omg.CORBA.DomainManager[] _get_domain_managers
        (org.omg.CORBA.Object self)
    {
        return null;
    }

    /**
     * Similar to invoke in InvokeHandler, which is ultimately implement by
     * skeletons. This method is used by the POA to handle operations that
     * are "special", i.e. not implemented by skeletons
     */

    public org.omg.CORBA.portable.OutputStream _invoke(org.omg.PortableServer.Servant self,
                                                       String method,
                                                       org.omg.CORBA.portable.InputStream _input,
                                                       org.omg.CORBA.portable.ResponseHandler handler)
        throws org.omg.CORBA.SystemException
    {
        org.omg.CORBA.portable.OutputStream _out = null;

        if ("_get_policy".equals (method))
        {
            _out = handler.createReply();
            _out.write_Object(_get_policy(_input.read_Object() , _input.read_long()  ) );
        }
        else if ("_is_a".equals (method))
        {
            _out = handler.createReply();
            _out.write_boolean(self._is_a(_input.read_string() ));
        }
        else if ("_interface".equals (method))
        {
            _out = handler.createReply();
            _out.write_Object(self._get_interface_def() );
        }
        else if ("_non_existent".equals (method))
        {
            _out = handler.createReply();
            _out.write_boolean(self._non_existent() );
        }
        else if ("_get_component".equals (method))
        {
            _out = handler.createReply();
            _out.write_Object(self._get_component() );
        }
        else
        {
            throw new BAD_PARAM("Unknown operation: " + method );
        }

        return _out;
    }

   public String repository_id (Servant self)
   {
      throw new NO_IMPLEMENT ("NYI");
   }
}
