
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
import javax.net.*;
import java.security.*;

import com.sun.net.ssl.*;

public class SSLSocketFactory 
    implements org.jacorb.orb.factory.SocketFactory 
{    
    private SocketFactory factory = null;
    
    public SSLSocketFactory( org.jacorb.orb.ORB orb ) 
    {
	factory = createSocketFactory();

	if( factory == null )
	{
	    Debug.output( 1, "ERROR: Unable to create ServerSocketFactory!" );
	}
    }

    public Socket createSocket( String host, 
                                int port )
	throws IOException, UnknownHostException
    {       
	return factory.createSocket( host, port );    
    }

    public boolean isSSL ( java.net.Socket s )
    { 
        return ( s instanceof SSLSocket); 
    }

    private SocketFactory createSocketFactory() 
    {
	try 
	{
	    // set up key manager to do server authentication
	    KeyManagerFactory kmf = KeyManagerFactory.getInstance( "SunX509" );
	    KeyStore key_store = KeyStore.getInstance( "JKS" );

            String keystore_location = Environment.keyStore();
            if( keystore_location == null ) 
            {
                System.out.print( "Please enter key store file name: " );
                keystore_location = 
                    (new BufferedReader(new InputStreamReader(System.in))).readLine();
            }

            String keystore_passphrase = 
                Environment.getProperty( "jacorb.security.keystore_password" );
            if( keystore_passphrase == null ) 
            {
                System.out.print( "Please enter store pass phrase: " );
                keystore_passphrase= 
                    (new BufferedReader(new InputStreamReader(System.in))).readLine();
            }

            String alias = Environment.defaultUser();
            if( alias == null ) 
            {
                System.out.print( "Please enter alias  name: " );
                alias = 
                    (new BufferedReader(new InputStreamReader(System.in))).readLine();
            }

            String password = Environment.defaultPassword();
            if ( password == null ) 
            {
                System.out.print( "Please enter password: " );
                password = 
                    (new BufferedReader(new InputStreamReader(System.in))).readLine();
            }

            key_store.load( new FileInputStream( keystore_location ),
                            keystore_passphrase.toCharArray() );

            kmf.init( key_store, keystore_passphrase.toCharArray() );

            TrustManagerFactory tmf = 
		TrustManagerFactory.getInstance( "SunX509" );
	    tmf.init( key_store );

            SSLContext ctx = SSLContext.getInstance( "TLS" );
            ctx.init( kmf.getKeyManagers(), 
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
