package org.jacorb.test.orb.etf.wiop;

/*
 *        JacORB  - a free Java ORB
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

import java.io.*;
import java.net.*;

import org.omg.ETF.*;
import org.omg.IOP.*;
import org.omg.RTCORBA.ProtocolProperties;

import org.jacorb.orb.iiop.*;

/**
 * WIOP is wrapper around an IIOP transport.  To the ORB, it looks like
 * a wholly different transport, but the actual implementation just
 * delegates everything to the standard IIOP classes.
 * 
 * @author <a href="mailto:spiegel@gnu.org">Andre Spiegel</a>
 * @version $Id$
 */
public class WIOPFactories extends _FactoriesLocalBase
{
    public static boolean transportInUse = false;
    
    private final int tag = 7; 

    public Connection create_connection (ProtocolProperties props)
    {
        return new WIOPConnection (new ClientIIOPConnection(),
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
                    (new ServerIIOPConnection (socket, is_ssl), tag);
            }
        }; 
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

}
