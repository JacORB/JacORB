package org.jacorb.orb.dns;

import org.xbill.DNS.*;

import org.jacorb.util.Debug;

import java.net.InetAddress;

/**
 * DNSLookupDelegateImpl.java
 *
 *
 * Created: Thu Apr  5 10:54:29 2001
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
        return _inverseLookup( dns.inaddrString( ip ));
    }

    public String inverseLookup( InetAddress addr )
    {
        return _inverseLookup( dns.inaddrString( addr ));
    }

} // DNSLookupDelegateImpl
