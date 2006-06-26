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
package org.jacorb.orb.etf;

import org.apache.avalon.framework.configuration.*;

import org.omg.ETF.*;
import org.omg.IOP.*;
import org.omg.RTCORBA.ProtocolProperties;

/**
 * @author Andre Spiegel
 * @version $Id$
 */
public abstract class FactoriesBase
    extends org.omg.ETF._FactoriesLocalBase
    implements Configurable
{
    protected org.jacorb.config.Configuration configuration;

    protected static Class connectionClz;

    protected static Class listenerClz;

    protected static Class profileClz;

    protected static Class addressClz;

    public void configure(Configuration configuration)
        throws ConfigurationException
    {
        if (configuration == null)
        {
            throw new IllegalArgumentException();
        }
        this.configuration = (org.jacorb.config.Configuration)configuration;
    }

    /**
    * ETF defined operation to create a connection.
    */
    public Connection create_connection(ProtocolProperties props)
    {
        return (Connection) newInstance(connectionClz, "ETF::Connection");
    }

    /**
    * ETF defined operation to create a connection.
    */
    public Listener create_listener(ProtocolProperties props,
                                    int stacksize,
                                    short base_priority)
    {
        return (Listener) newInstance(listenerClz, "ETF::Listener");
    }

    public Profile demarshal_profile(TaggedProfileHolder tagged_profile,
                                     TaggedComponentSeqHolder components)
    {
        final ProfileBase profile = (ProfileBase)newInstance(profileClz, "ETF::Profile");
        profile.demarshal(tagged_profile, components);
        return profile;
    }

    // Although not part of the ETF IDL for a Factory object, this is the best
    // place to add a new method for creating protocol address instances
    public ProtocolAddressBase create_protocol_address(String addr)
    {
        final ProtocolAddressBase address = (ProtocolAddressBase)newInstance(addressClz, "ETF::ProtocolAddressBase");
        final int address_start = this.match_tag(addr);
        if (address_start >= 0)
        {
            // general form is "prot://address"
            if (!address.fromString(addr.substring(address_start + 2)))
            {
                throw new org.omg.CORBA.INTERNAL("Invalid protocol address string: " + address);
            }
        }
        return address;
    }

    public int match_tag(String address)
    {
        return -1;
    }

    public abstract Profile decode_corbaloc (String corbaloc);

    private Object newInstance(final Class clazz, final String description)
    {
        Object connection = null;
        try
        {
            connection = clazz.newInstance();
        }
        catch (Exception e)
        {
            throw new org.omg.CORBA.INTERNAL("Cannot instantiate " + description + " class: " + e.toString());
        }

        if (connection instanceof Configurable)
        {
            try
            {
                ((Configurable)connection).configure(configuration);
            }
            catch( ConfigurationException e )
            {
                throw new org.omg.CORBA.INTERNAL("ConfigurationException: " + e.toString());
            }
        }
        return connection;
    }
}
