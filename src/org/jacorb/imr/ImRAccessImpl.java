/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2004 Gerald Brose
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
 *
 */

package org.jacorb.imr;

import org.jacorb.imr.RegistrationPackage.DuplicatePOAName;
import org.jacorb.imr.RegistrationPackage.IllegalPOAName;
import org.omg.CORBA.INTERNAL;
import org.jacorb.orb.iiop.IIOPAddress;

/**
 * @author Nicolas Noffke
 * @version $Id$
 */
public class ImRAccessImpl
    implements org.jacorb.orb.ImRAccess
{
    private Registration reg = null;
    private ImRInfo info = null;

    /**
     * <code>ImRAccessImpl</code> private; use the static connect method.
     */
    private ImRAccessImpl () {
        // use the static connect method
    }

   /**
    * <code>connect</code> resolves the IMR and returns a new ImRAccessImpl.
    *
    * @param orb an <code>org.omg.CORBA.ORB</code> value
    * @return an <code>ImRAccessImpl</code> value
    */
    public static ImRAccessImpl connect(org.omg.CORBA.ORB orb)
    {
        final ImRAccessImpl result = new ImRAccessImpl();

        try
        {
            result.reg = RegistrationHelper.narrow( orb.resolve_initial_references("ImplementationRepository"));
        }
        catch( org.omg.CORBA.ORBPackage.InvalidName e)
        {
            throw new INTERNAL("unable to resolve ImplementationRepository: " + e.toString());
        }

        boolean non_exist = true;
        if (result.reg != null)
        {
            try
            {
                non_exist = result.reg._non_existent();
            }
            catch (org.omg.CORBA.SystemException e)
            {
                non_exist = true;
            }
        }

        if (non_exist)
        {
            throw new INTERNAL("Unable to resolve reference to ImR");
        }
        return result;
    }

    public org.jacorb.orb.etf.ProtocolAddressBase getImRAddress()
    {
        if( info == null )
        {
            info = reg.get_imr_info();
        }
        return new IIOPAddress (info.host, info.port);
    }

    public String getImRHost()
    {
        if( info == null )
        {
            info = reg.get_imr_info();
        }

        return info.host;
    }

    public int getImRPort()
    {
        if( info == null )
        {
            info = reg.get_imr_info();
        }

        return info.port;
    }

    public void registerPOA( String name,
                             String server,
                             org.jacorb.orb.etf.ProtocolAddressBase address)
        throws INTERNAL
    {
        if (address instanceof IIOPAddress)
        {
            registerPOA (name, server,
                         ((IIOPAddress)address).getHostname(),
                         ((IIOPAddress)address).getPort());
        }
        else
        {
            throw new INTERNAL("IMR only supports IIOP based POAs");
        }
    }

    public void registerPOA( String name,
                             String server,
                             String host,
                             int port)
        throws INTERNAL
    {
        try
        {
            reg.register_poa(name, server, host, port );
        }
        catch( DuplicatePOAName e )
        {
            throw new INTERNAL( "A server with the same combination of ImplName/POA-Name (" +
                                name  +
                                ") is already registered and listed as active at the imr: " + e.toString() );
        }
        catch( IllegalPOAName e )
        {
            throw new INTERNAL( "The ImR replied that the POA name >>" +
                                e.name + "<< is illegal: " + e.toString() );
        }
        catch( UnknownServerName e )
        {
            throw new INTERNAL( "The ImR replied that the server name >>" +
                                e.name + "<< is unknown: " + e.toString());
        }
    }

    public void setServerDown( String name )
        throws INTERNAL
    {
        try
        {
            reg.set_server_down( name );
        }
        catch(UnknownServerName e)
        {
            throw new INTERNAL( "The ImR replied that a server with name " +
                                name + " is unknown: " + e.toString() );
        }
    }
}
