/*
 *        Written for JacORB - a free Java ORB
 *
 *   Copyright (C) 2000-2003 Gerald Brose
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

package org.jacorb.security.ssl.sun_jsse;

/**
 * @author Nicolas Noffke
 * $Id$
 */

import org.jacorb.security.level2.*;

import org.jacorb.util.*;

import java.net.*;
import java.io.*;
import java.security.*;
import java.util.*;

import org.apache.avalon.framework.logger.Logger;

import javax.net.ssl.*;
import javax.net.*;


//uncomment this line if you want to compile with the separately
//available jsse1.0.2
//import com.sun.net.ssl.*;

public class SSLSocketFactory 
    implements org.jacorb.orb.factory.SocketFactory 
{    
    private SocketFactory factory = null;
    private boolean change_roles = false;
    private String[] cipher_suites = null;
    private Logger logger;
    
    public SSLSocketFactory( org.jacorb.orb.ORB orb ) 
    {
        logger = Debug.getNamedLogger("jacorb.security.jsse");

	factory = createSocketFactory();

	if( factory == null )
	{
	    if (logger.isErrorEnabled())
                logger.error("Unable to create SSLSocketFactory!" );
	}
	
	change_roles = 
            Environment.isPropertyOn( "jacorb.security.change_ssl_roles" );

        // Andrew T. Finnell / Change made for e-Security Inc. 2002
        // We need to obtain all the cipher suites to use from the 
        // properties file.
	String cipher_suite_list = 
            Environment.getProperty("jacorb.security.ssl.client.cipher_suites" );
	
	if ( cipher_suite_list != null )
	{
            StringTokenizer tokenizer =
                new StringTokenizer( cipher_suite_list, "," );
        
            // Get the number of ciphers in the list
            int tokens = tokenizer.countTokens();
            
            if ( tokens > 0 )
            {
                // Create an array of strings to store the ciphers
                cipher_suites = new String[tokens];
                
                // This will fill the array in reverse order but that doesn't matter
                while( tokenizer.hasMoreElements() )
                {
                    cipher_suites[--tokens] = tokenizer.nextToken();
                }
            }
	}
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
	
	// Andrew T. Finnell
	// We need a way to enable the cipher suites that we would like to use
        // We should obtain these from the properties file
	if( cipher_suites != null )
        {
            s.setEnabledCipherSuites( cipher_suites );
        }

	return s;
    }

    public boolean isSSL ( java.net.Socket s )
    { 
        return ( s instanceof SSLSocket); 
    }

    private SocketFactory createSocketFactory() 
    {
        //uncomment this line if you want to compile with the separately
        //available jsse1.0.2
        //Security.addProvider( new com.sun.net.ssl.internal.ssl.Provider() );

	try 
	{
	    KeyManagerFactory kmf = null;
	    KeyStore key_store = null;

            if( Environment.isPropertyOn( "jacorb.security.jsse.trustees_from_ks" ) ||
                ((Environment.getIntProperty( "jacorb.security.ssl.client.supported_options", 16 ) & 0x40) != 0 ))
            {
                String keystore_location = 
                    Environment.getProperty( "jacorb.security.keystore" );
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
            
                key_store = 
                    KeyStoreUtil.getKeyStore( keystore_location,
                                              keystore_passphrase.toCharArray() );
                //only add own credentials, if establish trust in
                //client is supported
                if((Environment.getIntProperty( "jacorb.security.ssl.client.supported_options", 16 ) & 0x40) != 0 ) 
                {        
                    kmf = KeyManagerFactory.getInstance( "SunX509" );
                    kmf.init( key_store, keystore_passphrase.toCharArray() );
                }
            }

            TrustManagerFactory tmf = 
		TrustManagerFactory.getInstance( "SunX509" );

	    if( key_store != null &&
		Environment.isPropertyOn( "jacorb.security.jsse.trustees_from_ks" ))
	    {
                //take trusted certificates from keystore
                if (logger.isInfoEnabled())
                    logger.info("Loading certs from keystore " + key_store );
		tmf.init( key_store );
	    }
	    else
	    {
		tmf.init( (KeyStore) null );
	    }

            SSLContext ctx = SSLContext.getInstance( "TLS" );
            ctx.init( (kmf == null)? null : kmf.getKeyManagers(), 
		      tmf.getTrustManagers(), 
		      null );

            return ctx.getSocketFactory();
	} 
	catch( Exception e ) 
	{
            if (logger.isWarnEnabled())
                logger.warn("Exception " + e.getMessage() + " in SSLSocketFactory");
	}

	return null;
    }
}


