
/*
 *        Written for JacORB - a free Java ORB
 *
 *   Copyright (C) 2000 Gerald Brose
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

package org.jacorb.security.jsse;

/**
 * @author Nicolas Noffke
 * $Id$
 */

import org.jacorb.security.level2.*;

import org.jacorb.util.*;

import java.net.*;
import java.io.*;
import javax.net.ssl.*;
import java.security.*;

import com.sun.net.ssl.*;

public class SSLSocketFactory 
    implements org.jacorb.orb.factory.SocketFactory 
{    
    private boolean isRoleChange; // rt

    private CurrentImpl securityCurrent = null;
    
    public SSLSocketFactory( org.jacorb.orb.ORB orb ) 
    {
	isRoleChange = Environment.changeSSLRoles();

        try
        {
            securityCurrent = (CurrentImpl)
                orb.resolve_initial_references("SecurityCurrent");
        }
        catch ( Exception e )
        {
            Debug.output( 2, e );
        }        
    }


    public Socket createSocket( String host, 
                                int port )
	throws IOException, UnknownHostException
    {       
        try
        {
            KeyAndCert[] kac = securityCurrent.getSSLCredentials();

            TrustManagerFactory tmf = TrustManagerFactory.getInstance( "SunX509" );

            SSLContext ctx = SSLContext.getInstance( "TLS" );
            ctx.init( new KeyManager[]{ new X509KeyManagerImpl( kac ) },
                      tmf.getTrustManagers(), 
                      null );
            
            javax.net.ssl.SSLSocketFactory factory = ctx.getSocketFactory();
            
            SSLSocket sock = (SSLSocket) factory.createSocket( host, port );

            // rt: switch to server mode
            if (isRoleChange) 
            {
                org.jacorb.util.Debug.output(1, "SSLSocket switch to server mode...");
                sock.setUseClientMode( false );
            }
		
            return sock;

        }
        catch( Exception e )
        {
            Debug.output( 1, e );
        }

        return null;
    }

    public boolean isSSL ( java.net.Socket s )
    { 
        return ( s instanceof SSLSocket); 
    }
}
