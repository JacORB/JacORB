package org.jacorb.orb.iiop;

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

import org.jacorb.config.ConfigurationException;
import org.jacorb.orb.etf.ProfileBase;
import org.jacorb.orb.etf.ProtocolAddressBase;
import org.omg.ETF.Connection;
import org.omg.ETF.Listener;
import org.omg.ETF.Profile;
import org.omg.IOP.TaggedComponentSeqHolder;
import org.omg.IOP.TaggedProfileHolder;

/**
 * @author Andre Spiegel
 */
public class IIOPFactories
    extends org.jacorb.orb.etf.FactoriesBase
{
    /**
     * Demarshall and return the correct type of profile
     */
    public ProfileBase demarshal_profile (TaggedProfileHolder tagged_profile,
                                              TaggedComponentSeqHolder components)
    {
        final ProfileBase profile = new IIOPProfile ();

        configureResult (profile);

        profile.demarshal(tagged_profile, components);

       return profile;
    }

    /**
     * Return the correct type of address
     */
    protected ProtocolAddressBase create_address_internal ()
    {
       return new IIOPAddress();
    }


    /**
     * Return the correct type of connection
     */
    protected Connection create_connection_internal ()
    {
       return new ClientIIOPConnection();
    }

    /**
     * Return the correct type of listener
     */
    protected Listener create_listener_internal ()
    {
       IIOPListener result = new IIOPListener();
       configureResult (result);
       return result;
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
            "iiop".equals (token) ||
            "ssliop".equals (token))
        {
            IIOPProfile result = new IIOPProfile(corbaloc);
            try
            {
                result.configure(configuration);
            }
            catch(ConfigurationException e)
            {
                throw new org.omg.CORBA.INTERNAL("ConfigurationException: " + e);
            }

            return result;
        }
        return null;
    }

    public int match_tag(String address)
    {
        if (address == null)
        {
            return -1;
        }
        int colon = address.indexOf (':');
        String token = address.substring (0,colon).toLowerCase();
        if ("iiop".equals (token) || "ssliop".equals (token))
        {
            return colon+1;
        }
        return -1;
    }
}
