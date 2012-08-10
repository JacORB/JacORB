package org.jacorb.test.orb.etf.wiop;

/*
 *        JacORB  - a free Java ORB
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
 *   Software Foundation, 51 Franklin Street, Fifth Floor, Boston,
 *   MA 02110-1301, USA.
 */

import java.io.*;
import java.net.*;

import org.jacorb.config.*;

import org.omg.ETF.*;
import org.omg.IOP.*;
import org.omg.RTCORBA.ProtocolProperties;

import org.jacorb.orb.iiop.*;

/**
 * WIOP is a wrapper around an IIOP transport.  To the ORB, it looks like
 * a wholly different transport, but the actual implementation just
 * delegates everything to the standard IIOP classes.
 *
 * @author Andre Spiegel spiegel@gnu.org
 */
public class WIOPFactories
    extends _FactoriesLocalBase
    implements Configurable
{
    private static boolean transportInUse = false;
    private final int tag = 7;
    org.jacorb.config.Configuration configuration;
    org.jacorb.orb.ORB orb;

    public void configure(Configuration configuration)
        throws ConfigurationException
    {
        this.configuration = (org.jacorb.config.Configuration)configuration;
        this.orb = this.configuration.getORB();
    }

    public Connection create_connection (ProtocolProperties props)
    {
        ClientIIOPConnection delegate = new ClientIIOPConnection();
        try
        {
            delegate.configure(configuration);
        }
        catch( ConfigurationException ce )
        {
            throw new org.omg.CORBA.INTERNAL("ConfigurationException: " + ce.getMessage());
        }

        return new WIOPConnection (delegate,
                                   tag);
    }

    public Listener create_listener (ProtocolProperties props,
                                     int stacksize,
                                     short base_priority)
    {
        IIOPListener delegate = new IIOPListener()
        {
            protected Connection createServerConnection (Socket socket,
                                                         boolean is_ssl)
                throws IOException
            {
                return new WIOPConnection
                    (super.createServerConnection(socket, is_ssl), tag);
            }
        };
        try
        {
            delegate.configure(configuration);
        }
        catch( ConfigurationException ce )
        {
            throw new org.omg.CORBA.INTERNAL("ConfigurationException: " + ce.getMessage());
        }

        return new WIOPListener (delegate, tag);
    }

    public Profile demarshal_profile (TaggedProfileHolder tagged_profile,
                                      TaggedComponentSeqHolder components)
    {
        if (tagged_profile.value.tag != this.tag)
        {
            throw new org.omg.CORBA.BAD_PARAM
                ("wrong profile for WIOP transport, tag: "
                 + tagged_profile.value.tag);
        }
        else
        {
            IIOPProfile result
                = new IIOPProfile (tagged_profile.value.profile_data);

            try
            {
                result.configure (configuration);
            }
            catch( ConfigurationException e )
            {
                throw new org.omg.CORBA.INTERNAL("ConfigurationException: " + e.toString());
            }
            

            try
            {
                result.configure (configuration);
            }
            catch( ConfigurationException e )
            {
                throw new org.omg.CORBA.INTERNAL("ConfigurationException: " + e.toString());
            }
            
            components.value = result.getComponents().asArray();
            
            
            return new WIOPProfile (result, this.tag);
        }
    }

    public int profile_tag()
    {
        return tag;
    }

    public Profile decode_corbaloc(String corbaloc)
    {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public synchronized static void setTransportInUse(boolean transportInUse)
    {
        WIOPFactories.transportInUse = transportInUse;
    }

    public synchronized static boolean isTransportInUse()
    {
        return transportInUse;
    }
}
