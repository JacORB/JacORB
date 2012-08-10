/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2012 Gerald Brose / The JacORB Team.
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

import org.jacorb.config.Configurable;
import org.jacorb.config.Configuration;
import org.jacorb.config.ConfigurationException;
import org.omg.ETF.Connection;
import org.omg.ETF.Listener;
import org.omg.ETF.Profile;
import org.omg.IOP.TaggedComponentSeqHolder;
import org.omg.IOP.TaggedProfileHolder;
import org.omg.RTCORBA.ProtocolProperties;

/**
 * @author Andre Spiegel
 */
public abstract class FactoriesBase
    extends org.omg.ETF._FactoriesLocalBase
    implements Configurable
{
    protected org.jacorb.config.Configuration configuration;


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
       Connection result = create_connection_internal ();
       configureResult (result);
       return result;
    }

    /**
     * Sub-classes should return the correct type of connection
     */
    protected abstract Connection create_connection_internal ();

    /**
    * ETF defined operation to create a connection.
    */
    public Listener create_listener(ProtocolProperties props,
                                    int stacksize,
                                    short base_priority)
    {
       return create_listener_internal ();
    }

    /**
     * Sub-classes should return the correct type of listener
     */
    protected abstract Listener create_listener_internal();


    public abstract Profile demarshal_profile(TaggedProfileHolder tagged_profile,
                                              TaggedComponentSeqHolder components);

    // Although not part of the ETF IDL for a Factory object, this is the best
    // place to add a new method for creating protocol address instances
    public ProtocolAddressBase create_protocol_address(String addr)
    {
        final ProtocolAddressBase address = create_address_internal();
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


    /**
     * Sub-classes should return the correct type of address
     */
    protected abstract ProtocolAddressBase create_address_internal ();


    public int match_tag(String address)
    {
        return -1;
    }

    /**
     * Sub-classes should implement corbaloc decoding
     */
    public abstract Profile decode_corbaloc (String corbaloc);

    /**
     * If the object is configurable call configure on it.
     * @param o
     */
    protected void configureResult (Object o)
    {
       if (o instanceof Configurable)
       {
          try
          {
             ((Configurable)o).configure(configuration);
          }
          catch( ConfigurationException e )
          {
             throw new org.omg.CORBA.INTERNAL("ConfigurationException: " + e.toString());
          }
       }
    }
}
