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

import org.xbill.DNS.*;

import org.jacorb.util.Debug;

import java.net.InetAddress;

/**
 * DNSLookupDelegateImpl.java
 *
 *
 * Created: Thu Apr  5 10:54:29 2002
 *
 * @author Nicolas Noffke
 * @version $Id$
 */

public class DNSLookupDelegateImpl
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
        try
        {
            return _inverseLookup( dns.inaddrString( ip ));
        }
        catch( NoClassDefFoundError e )
        {
            Debug.output( 1, "DNS lookup support is compiled, but the classes of the org.xbill.dns package are not present" );
            Debug.output( 2, e );
        }            
        
        return null;
    }

    public String inverseLookup( InetAddress addr )
    {
        try
        {
            return _inverseLookup( dns.inaddrString( addr ));
        }
        catch( NoClassDefFoundError e )
        {
            Debug.output( 1, "DNS lookup support is compiled, but the classes of the org.xbill.dns package are not present" );
            Debug.output( 2, e );
        }
        
        return null;
    }

} // DNSLookupDelegateImpl
