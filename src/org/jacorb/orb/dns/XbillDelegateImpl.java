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

import org.xbill.DNS.*;
import org.jacorb.util.Debug;
import java.net.InetAddress;

public class XbillDelegateImpl
    implements DNSLookupDelegate
{
    
    private String _inverseLookup( String addr )
    {
        try
        {
            Record[] r = dns.getRecords( addr, Type.PTR );
            
            if( r != null )
            {
                for( int i = 0; i < r.length; i++ )
                {
                    if( r[i] instanceof PTRRecord )
                    {
                        PTRRecord ptr_r = (PTRRecord) r[i];
                        
                        if( ptr_r.getTarget() != null )
                        { 
                            String name = ptr_r.getTarget().toString();

                            name = name.trim();

                            if( name.endsWith( "." ))
                            {
                                name = name.substring( 0, name.length() - 1 );
                            }
                            
                            Debug.output( 2, "Resolved " + addr + 
                                          " to " + name );
                            
                            return name;
                        }
                    }
                }
            }
        }
        catch( Exception e )
        {
            Debug.output( 1, e );
        }

        Debug.output( 2, "Unable to resolve " + addr + 
                      " via DNS");

        return null;
    }
                    
    public String inverseLookup( String ip )
    {
        return _inverseLookup (dns.inaddrString (ip));
    }

    public String inverseLookup( InetAddress addr )
    {
        return _inverseLookup (dns.inaddrString (addr));
    }

} // DNSLookupDelegateImpl
