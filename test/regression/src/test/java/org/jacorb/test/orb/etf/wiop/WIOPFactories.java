package org.jacorb.test.orb.etf.wiop;

/*
 *        JacORB  - a free Java ORB
 *
 *   Copyright (C) 1997-2014 Gerald Brose / The JacORB Team.
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

import java.io.IOException;
import java.net.Socket;
import java.util.Iterator;
import org.jacorb.config.Configurable;
import org.jacorb.config.Configuration;
import org.jacorb.config.ConfigurationException;
import org.jacorb.orb.etf.ListenEndpoint;
import org.jacorb.orb.etf.ListenEndpoint.Protocol;
import org.jacorb.orb.iiop.ClientIIOPConnection;
import org.jacorb.orb.iiop.IIOPListener;
import org.jacorb.orb.iiop.IIOPProfile;
import org.omg.ETF.Connection;
import org.omg.ETF.Listener;
import org.omg.ETF.Profile;
import org.omg.ETF._FactoriesLocalBase;
import org.omg.IOP.TaggedComponentSeqHolder;
import org.omg.IOP.TaggedProfileHolder;
import org.omg.RTCORBA.ProtocolProperties;

/**
 * WIOP is a wrapper around an IIOP transport.  To the ORB, it looks like
 * a wholly different transport, but the actual implementation just
 * delegates everything to the standard IIOP classes.
 *
 * @author Andre Spiegel spiegel@gnu.org
 */
@SuppressWarnings("serial")
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
        this.configuration = configuration;
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
            @Override
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
            Iterator<ListenEndpoint> it = orb.getTransportManager().getListenEndpoints(Protocol.IIOP).iterator();
            delegate.setListenEndpoint(it.next());
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
