package org.jacorb.orb.dns;

import java.net.InetAddress;

/**
 * DNSLookupDelegate.java
 *
 *
 * Created: Thu Apr  5 10:54:29 2001
 *
 * @author Nicolas Noffke
 * @version $Id$
 */

public interface DNSLookupDelegate  
{                    
    public String inverseLookup( String ip );

    public String inverseLookup( InetAddress addr );

} // DNSLookupDelegate
