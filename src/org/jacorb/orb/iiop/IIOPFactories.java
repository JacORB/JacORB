package org.jacorb.orb.iiop;

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

import org.omg.IOP.*;
import org.omg.ETF.*;
import org.omg.RTCORBA.ProtocolProperties;

import org.jacorb.orb.InternetIOPProfile;

/**
 * @author Andre Spiegel
 * @version $Id$
 */
public class IIOPFactories extends org.omg.ETF._FactoriesLocalBase
{
    public Connection create_connection (ProtocolProperties props)
    {
        // TODO Auto-generated method stub
        return null;
    }

    public Listener create_listener (ProtocolProperties props,
                                     int stacksize,
                                     short base_priority)
    {
        return new IIOPListener();
    }

    public Profile demarshal_profile (TaggedProfileHolder tagged_profile,
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
            InternetIOPProfile result 
                = new InternetIOPProfile (tagged_profile.value.profile_data);
            components.value = result.getComponents().asArray();
            return result;
        }
    }

    public int profile_tag()
    {
        return org.omg.IOP.TAG_INTERNET_IOP.value;
    }

}
