package org.jacorb.orb.iiop;

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

import org.apache.avalon.framework.configuration.*;

import org.omg.IOP.*;
import org.omg.ETF.*;
import org.omg.RTCORBA.ProtocolProperties;

/**
 * @author Andre Spiegel
 * @version $Id$
 */
public class IIOPFactories 
    extends org.omg.ETF._FactoriesLocalBase
    implements Configurable
{
    org.jacorb.config.Configuration configuration;

    public void configure(Configuration configuration)
        throws ConfigurationException
    {
        this.configuration = (org.jacorb.config.Configuration)configuration;
    }


    public Connection create_connection(ProtocolProperties props)
    {
        return new ClientIIOPConnection();
    }

    /**
     * Creates and configures a new IIOP listener
     */

    public Listener create_listener(ProtocolProperties props,
                                    int stacksize,
                                    short base_priority)
    {
        IIOPListener result = new IIOPListener(configuration.getORB());
        try
        {
            result.configure(configuration);
        }
        catch( ConfigurationException ce )
        {
            throw new org.omg.CORBA.INTERNAL("ConfigurationException: " + ce.getMessage());
        }
        return result;
    }

    public Profile demarshal_profile(TaggedProfileHolder tagged_profile,
                                      TaggedComponentSeqHolder components)
    {
        if (tagged_profile.value.tag != TAG_INTERNET_IOP.value)
        {
            throw new org.omg.CORBA.BAD_PARAM 
                ("wrong profile for IIOP transport, tag: " 
                 + tagged_profile.value.tag);
        }
        else
        {
            IIOPProfile result = 
                new IIOPProfile(tagged_profile.value.profile_data);
            try
            {
                result.configure(configuration);
            }
            catch(ConfigurationException e)
            {
                throw new org.omg.CORBA.INTERNAL("ConfigurationException: " + e.getMessage());
            }

            components.value = result.getComponents().asArray();
            return result;
        }
    }

    public int profile_tag()
    {
        return org.omg.IOP.TAG_INTERNET_IOP.value;
    }

    public Profile decode_corbaloc (String corbaloc)
    {
        int colon = corbaloc.indexOf (':');
        String token = corbaloc.substring (0,colon).toLowerCase();
        if (token.length() == 0 ||
            token.equals ("iiop") ||
            token.equals ("ssliop"))
        {
            IIOPProfile result = new IIOPProfile(corbaloc);
            try
            {
                result.configure(configuration);
            }
            catch(ConfigurationException e)
            {
                throw new org.omg.CORBA.INTERNAL("ConfigurationException: " + e.getMessage());
            }

            return result;
        }
        else
            return null;
    }

}
