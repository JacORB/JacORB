package org.jacorb.orb.dns;

/*
 *        JacORB  - a free Java ORB
 *
 *   Copyright (C) 1997-2004 Gerald Brose.
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

import java.net.InetAddress;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;

public class DNSLookup  
    implements Configurable
{
    private  DNSLookupDelegate delegate = null;
    private  boolean enabled = false;

    public void configure(Configuration configuration)
        throws ConfigurationException
    {        
        enabled = configuration.getAttribute("jacorb.dns.enable","off").equals("on");
        if (enabled)
        {
            delegate = 
                new JdkDelegateImpl(((org.jacorb.config.Configuration)configuration).getNamedLogger("org.jacorb.dns"));
        }
    }

    /**
     * Returns true if this is a post-1.4 DNS implementation.
     */
    private static boolean jdk_DNS_Usable()
    {
        try
        {
            java.lang.reflect.Method m = java.net.InetAddress.class.getMethod
            (
                "getCanonicalHostName",
                new Class[] {}
            );
            return m != null;
        }
        catch (NoSuchMethodException ex)
        {
            return false;
        }
    }

    public String inverseLookup(String ip)
    {
        return ((delegate == null) ? null : delegate.inverseLookup(ip));
    }

    public String inverseLookup(InetAddress addr)
    {
        return ((delegate == null) ? null : delegate.inverseLookup(addr));
    }
            
} // DNSLookup
