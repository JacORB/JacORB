package org.jacorb.orb.diop;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2003 Gerald Brose
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

import org.jacorb.orb.iiop.IIOPProfile;
import org.omg.CORBA.INTERNAL;
import org.omg.ETF.Connection;
import org.omg.ETF.Listener;
import org.omg.ETF.Profile;
import org.omg.IOP.TaggedComponentSeqHolder;
import org.omg.IOP.TaggedProfileHolder;
import org.omg.RTCORBA.ProtocolProperties;

/**
 * DIOP Factory
 *
 * Currently this is only used to allow DIOP IOR's to be decoded. Delegates
 * to IIOPProfile.
 *
 * @author Nick Cross
 * @version $Id$
 */
public class DIOPFactories extends org.omg.ETF._FactoriesLocalBase
{
    /**
     * <code>TAG_DIOP_UDP</code> is a constant for DIOP IORs. This is a TAO
     * protocol for GIOP/UDP.
     */
    public static final int TAG_DIOP_UDP = 0x54414f04;


    /**
     * <code>factory</code> is a static instance cache - as this is currently not
     * cached within TransportManager::factoriesList
     */
    private static DIOPFactories factory;


    /**
     * <code>getDIOPFactory</code> returns the cached instance.
     */
    public static DIOPFactories getDIOPFactory()
    {
        if (factory == null)
        {
            factory = new DIOPFactories();
        }
        return factory;
    }


    public Connection create_connection (ProtocolProperties props)
    {
        throw new INTERNAL ("DIOP Connection not implemented");
    }

    public Listener create_listener (ProtocolProperties props,
                                     int stacksize,
                                     short base_priority)
    {
        throw new INTERNAL ("DIOP Listener not implemented");
    }


    public Profile demarshal_profile (TaggedProfileHolder tagged_profile,
                                      TaggedComponentSeqHolder components)
    {
        if (tagged_profile.value.tag != TAG_DIOP_UDP)
        {
            throw new org.omg.CORBA.BAD_PARAM
                ("wrong profile for DIOP transport, tag: "
                 + tagged_profile.value.tag);
        }
        
        IIOPProfile result = new IIOPProfile(tagged_profile.value.profile_data);
        components.value = result.getComponents().asArray();
        return result;
    }

    public int profile_tag()
    {
        return TAG_DIOP_UDP;
    }

    public Profile decode_corbaloc (String corbaloc)
    {
        int colon = corbaloc.indexOf (':');
        String token = corbaloc.substring (0,colon).toLowerCase();
        if (token.length() == 0 ||
            token.equals ("diop"))
        {
            return new IIOPProfile(corbaloc);
        }
        
        return null;
    }
}
