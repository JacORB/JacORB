/*
 *        Written for JacORB - a free Java ORB
 *
 *   Copyright (C) 2002 - Gerald Brose
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

package org.jacorb.security.ssl.iaik;

import iaik.security.ssl.*;
import java.security.cert.X509Certificate;

/**
 * ServerChainVerifier.java
 *
 *
 * Created: Mon Oct  1 16:03:14 2002
 *
 * @author Nicolas Noffke
 * @version $Id$
 */

public class ServerChainVerifier 
    extends ChainVerifier 
{
    private boolean require_client_auth = false;

    public ServerChainVerifier( boolean require_client_auth )
    {
        super();
        
        this.require_client_auth = require_client_auth;
    }

    public ServerChainVerifier( int k )
    {
        super( k );
    }
    
    public boolean verifyChain( X509Certificate[] chain,
                                SSLTransport transport )
    {
        if( require_client_auth && (chain == null) )
        {
            return false;
        }
        
        return super.verifyChain( chain, transport );
    }
}// ServerChainVerifier


