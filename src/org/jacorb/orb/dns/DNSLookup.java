/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2002 Gerald Brose
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
package org.jacorb.orb.dns;

import java.net.InetAddress;
/**
 * DNSLookup.java
 *
 *
 * Created: Thu Apr  5 10:45:02 2002
 *
 * @author Nicolas Noffke
 * @version $Id$
 */

public class DNSLookup  
{
    private static DNSLookupDelegate delegate = null;

    static
    {
        createDelegate();
    }

    private static void createDelegate()
    {
        try
        {
            Class c = 
                Class.forName( "org.jacorb.orb.dns.DNSLookupDelegateImpl" );
            
            delegate = (DNSLookupDelegate) c.newInstance();
        }
        catch( Exception e )
        {
            //ignore
        }
    }

    public static String inverseLookup( String ip )
    {
        if( delegate != null )
        {
            return delegate.inverseLookup( ip );
        }
        else
        {
            return null;
        }
    }

    public static String inverseLookup( InetAddress addr )
    {
        if( delegate != null )
        {
            return delegate.inverseLookup( addr );
        }
        else
        {
            return null;
        }
    }
            
} // DNSLookup
