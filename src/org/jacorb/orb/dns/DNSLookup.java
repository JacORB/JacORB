package org.jacorb.orb.dns;

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

import java.net.InetAddress;
import org.jacorb.util.Environment;

public class DNSLookup  
{
    private static DNSLookupDelegate delegate = null;
    private static boolean enabled = false;

    static
    {
        enabled = Environment.isPropertyOn ("jacorb.dns.enable");
        if (enabled)
        {
            createDelegate();
        }
    }

    private static void createDelegate()
    {
        if (jdk_DNS_Usable())
        {
            delegate = new JdkDelegateImpl();
        }
        else
        {
            try
            {
                Class c;

                // Ensure that both the delegate implementation
                // and the DNS support classes are available

                c = Environment.classForName ("org.xbill.DNS.dns");
                c = Environment.classForName ("org.jacorb.orb.dns.XbillDelegateImpl");
            
                delegate = (DNSLookupDelegate) c.newInstance ();
            }
            catch (Exception e)
            {
                //ignore
            }
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

    public static String inverseLookup (String ip)
    {
        return ((delegate == null) ? null : delegate.inverseLookup (ip));
    }

    public static String inverseLookup (InetAddress addr)
    {
        return ((delegate == null) ? null : delegate.inverseLookup (addr));
    }
            
} // DNSLookup
