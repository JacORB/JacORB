/*
 *        Written for JacORB - a free Java ORB
 *
 *   Copyright (C) 2002 Gerald Brose
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


import org.jacorb.util.*;

import java.net.*;
import java.io.*;
import javax.net.ssl.*;
import javax.net.*;
import java.security.*;

import com.sun.net.ssl.*;

public class SSLSocketFactory 
    implements org.jacorb.orb.factory.SocketFactory 
{    
    private SocketFactory factory = null;
    private boolean change_roles = false;
    
    public SSLSocketFactory( org.jacorb.orb.ORB orb ) 
    {
	factory = createSocketFactory();

	if( factory == null )
	{
	    Debug.output( 1, "ERROR: Unable to create ServerSocketFactory!" );
	}
	
	change_roles = Environment.changeSSLRoles();
    }

    public Socket createSocket( String host, 
                                int port )
	throws IOException, UnknownHostException
    {       
	SSLSocket s = (SSLSocket) factory.createSocket( host, port );
	
	if( change_roles )
	{
	    s.setUseClientMode( false );
	}
	
	return s;
    }

    public boolean isSSL ( java.net.Socket s )
    { 
        return ( s instanceof SSLSocket); 
    }

    private SocketFactory createSocketFactory() 
    {
	try 
	{
            java.security.Security.addProvider( new com.sun.net.ssl.internal.ssl.Provider() );

	    KeyManagerFactory kmf = null;
	    KeyStore key_store = null;

	    //only add own credentials, if establish trust in client
            //is supported
            if(( (byte) Environment.supportedBySSL() & 0x40) != 0 ) 
            {        
		String keystore_location = 
                    Environment.getProperty( "jacorb.security.keystore" );
                if( keystore_location == null )
                {
                    Debug.output( 1, "ERROR: No keystore location specified" );
                    Debug.output( 1, "Please check property \"jacorb.security.keystore\"");
                    
                    return null;
                }
                
		String keystore_password = 
		    Environment.getProperty( "jacorb.security.keystore_password" );
                if( keystore_password == null )
                {
                    Debug.output( 1, "ERROR: No keystore password specified" );
                    Debug.output( 1, "Please check property \"jacorb.security.keystore_password\"");
                    
                    return null;
                }

		kmf = KeyManagerFactory.getInstance( "SunX509" );

                key_store = 
                    KeyStoreUtil.getKeyStore( keystore_location,
                                              keystore_password.toCharArray() );

                kmf.init( key_store, keystore_password.toCharArray() );
	    }

            TrustManagerFactory tmf = 
		TrustManagerFactory.getInstance( "SunX509" );

	    if( key_store != null &&
		"on".equals( Environment.getProperty( "jacorb.security.jsse.trustees_from_ks",
						      "off" )))
	    {
		tmf.init( key_store );
	    }
	    else
	    {
		String truststore_location = 
                    Environment.getProperty( "jacorb.security.jsse.truststore" );
                if( truststore_location == null )
                {
                    Debug.output( 1, "ERROR: No truststore location specified" );
                    Debug.output( 1, "Please check property \"jacorb.security.jsse.truststore\"");
                    
                    return null;
                }
                
		String truststore_password = 
		    Environment.getProperty( "jacorb.security.jsse.truststore_password" );
                if( truststore_password == null )
                {
                    Debug.output( 1, "ERROR: No truststore password specified" );
                    Debug.output( 1, "Please check property \"jacorb.security.jsse.truststore_password\"");
                    
                    return null;
                }

                KeyStore trust_store = 
                    KeyStoreUtil.getKeyStore( truststore_location,
                                              truststore_password.toCharArray() );
                
		tmf.init( trust_store );
	    }

            SSLContext ctx = SSLContext.getInstance( "TLS" );
            ctx.init( (kmf == null)? null : kmf.getKeyManagers(), 
		      tmf.getTrustManagers(), 
		      null );

            return ctx.getSocketFactory();
	} 
	catch( Exception e ) 
	{
	    Debug.output( 1, e );
	}

	return null;
    }
}


