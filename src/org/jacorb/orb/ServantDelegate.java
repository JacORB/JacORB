package org.jacorb.orb;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2003  Gerald Brose.
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

import org.omg.CORBA.ORB;
import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.CORBA.*;

import org.omg.PortableServer.*;
import org.omg.PortableServer.POAPackage.*;
import org.omg.PortableServer.CurrentPackage.NoContext;

import org.jacorb.util.Debug;

/**
 * JacORB-specific implementation of PortableServer.Servant
 *
 * $Id$
 */

public class ServantDelegate
    implements org.omg.PortableServer.portable.Delegate
{
    private transient ORB orb = null;
    private transient Repository ir = null;
    private transient org.omg.PortableServer.Current _current = null;
    private transient POA poa = null;

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
            return poa.servant_to_reference(self);
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
        if (_current == null)
        {
            _getPOACurrent();
        }
        try
        {
            if( _current.get_servant() != self )
            {
                throw new org.omg.CORBA.OBJ_ADAPTER();
            }

            return _current.get_POA();
        }
        catch(NoContext e)
        {
            throw new org.omg.CORBA.OBJ_ADAPTER(e.toString());
        }
        catch(NoSuchMethodError nsme)
        {
            // We most likely get this if the Sun JDK definition of Current is getting picked up rather than ours.
            // It has (at present - SDK 1.4.2) no get_servant() method.
            // Give the user a hint as to how this can be fixed.
            org.jacorb.util.Debug.output(1, "ERROR: NoSuchMethodError - re-run specifying jacorb.jar "
                                             + "with -Xbootclasspath/p: option to avoid use of (incorrect) SDK implementation class.");
            throw nsme;
        }
    }

    final public byte[] object_id(org.omg.PortableServer.Servant self)
    {
        check();
        if (_current == null)
        {
            _getPOACurrent();
        }
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
        org.jacorb.util.Debug.output(2,"ServantDelegate: non_existent: return false");
        return false;
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
        org.jacorb.util.Debug.output( 3, "ServantDelegate: is a " +
                                      repid + " ?");

        String [] intf = self._all_interfaces(null, null);

        for( int i = 0; i < intf.length; i++)
        {
            org.jacorb.util.Debug.output( 4, "ServantDelegate: is a compares with " + intf[i] );

            if( intf[i].equals(repid))
            {
                org.jacorb.util.Debug.output( 4, "ServantDelegate: ! is a " +
                                              intf[i] + "!");
                return true;
            }
        }
        return repid.equals("IDL:omg.org/CORBA/Object:1.0");
    }


    /**
     * _get_policy
     */

    public org.omg.CORBA.Policy _get_policy(org.omg.CORBA.Object self,
                                            int policy_type)
    {
        return null;
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

        if(  method.equals("_get_policy"))
        {
            _out = handler.createReply();
            _out.write_Object(_get_policy(_input.read_Object() , _input.read_long()  ) );
        }
        else if( method.equals("_is_a"))
        {
            _out = handler.createReply();
            _out.write_boolean(self._is_a(_input.read_string() ));
        }
        else if( method.equals("_interface"))
        {
            _out = handler.createReply();
            _out.write_Object(self._get_interface() );
        }
        else if( method.equals("_non_existent"))
        {
            _out = handler.createReply();
            _out.write_boolean(self._non_existent() );
        }
        else
            throw new BAD_PARAM("Unknown operation: " + method );

        return _out;
    }


}
