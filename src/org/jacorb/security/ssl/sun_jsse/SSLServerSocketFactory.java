/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2003 Gerald Brose
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
package org.jacorb.security.ssl.sun_jsse;

import org.apache.avalon.framework.logger.Logger;

import org.jacorb.util.*;

import org.apache.avalon.framework.logger.Logger;

//uncomment this line if you want to compile with the separately
//available jsse1.0.2
//import com.sun.net.ssl.*;

import java.net.*;
import java.io.*;
import java.security.*;
import java.util.*;

import javax.net.ssl.*;
import javax.net.*;

/**
 * @author Nicolas Noffke
 * $Id$
 */

public class SSLServerSocketFactory 
    implements org.jacorb.orb.factory.SSLServerSocketFactory
{
    private ServerSocketFactory factory = null;
    private boolean require_mutual_auth = false;
    private boolean request_mutual_auth = false;
    private boolean change_roles = false;
    private String[] cipher_suites = null;

    private Logger logger;

    public SSLServerSocketFactory( org.jacorb.orb.ORB orb )
    {
        logger = Debug.getNamedLogger("jacorb.security.jsse");

        //uncomment this line if you want to compile with the separately
        //available jsse1.0.2
        //Security.addProvider( new com.sun.net.ssl.internal.ssl.Provider() );

	factory = createServerSocketFactory();

	if( factory == null )
	{
            if (logger.isErrorEnabled())
                logger.error("Unable to create ServerSocketFactory!" );
	}

	if( (Environment.getIntProperty( "jacorb.security.ssl.server.supported_options", 16 ) & 0x40) != 0 )
	{
	    // would prefer to establish trust in client.  If client can support
	    // authentication, it will, otherwise we will continue
            if (logger.isInfoEnabled())
                logger.info("Will create SSL sockets that request client authentication" );

	    request_mutual_auth = true;
	}

	if( (Environment.getIntProperty( "jacorb.security.ssl.server.required_options", 16 ) & 0x40) != 0 )
	{
	    //required: establish trust in client
	    //--> force other side to authenticate
	    require_mutual_auth = true;
            request_mutual_auth = false;
            if (logger.isInfoEnabled())
                logger.info("Will create SSL sockets that require client authentication" );
	}

	change_roles = 
            Environment.isPropertyOn( "jacorb.security.change_ssl_roles" );

        // Andrew T. Finnell
        // We need to obtain all the cipher suites to use from the
        // properties file.
	String cipher_suite_list =
		Environment.getProperty("jacorb.security.ssl.server.cipher_suites" );
	
	if ( cipher_suite_list != null )
	{
            StringTokenizer tokenizer = 
                new StringTokenizer( cipher_suite_list, "," );
            
            // Get the number of ciphers in the list
            int tokens = tokenizer.countTokens();
            
            if ( tokens > 0 )
            {
                // Create an array of strings to store the ciphers
                cipher_suites = new String [tokens];
                
                // This will fill the array in reverse order but that
                // doesn't matter
                while( tokenizer.hasMoreElements() )
                {
                    cipher_suites[--tokens] = tokenizer.nextToken();
                }
            }
	}
    }
           
    public ServerSocket createServerSocket( int port )
        throws IOException
    {
	SSLServerSocket s = (SSLServerSocket) 
	    factory.createServerSocket( port );

        if (request_mutual_auth) 
        {
            s.setWantClientAuth( request_mutual_auth );
        } 
        else if (require_mutual_auth) 
        {
            s.setNeedClientAuth( require_mutual_auth );
        }

	// Andrew T. Finnell / Change made for e-Security Inc. 2002 
        // We need a way to enable the cipher suites that we would
        // like to use. We should obtain these from the properties file.
	if( cipher_suites != null )
        {
            s.setEnabledCipherSuites ( cipher_suites );	
        }

	return s;
    }


    public ServerSocket createServerSocket( int port, int backlog ) 
        throws IOException
    {
	SSLServerSocket s = (SSLServerSocket) 
	    factory.createServerSocket( port, backlog );

        if (request_mutual_auth) 
        {
            s.setWantClientAuth( request_mutual_auth );
        }
        else if (require_mutual_auth) 
        {
            s.setNeedClientAuth( require_mutual_auth );
        }

	// Andrew T. Finnell / Change made for e-Security Inc. 2002 
        // We need a way to enable the cipher suites that we would
        // like to use. We should obtain these from the properties file.
	if( cipher_suites != null )
        {
            s.setEnabledCipherSuites ( cipher_suites );	
        }

	return s;
    }

    public ServerSocket createServerSocket (int port,
                                            int backlog,
                                            InetAddress ifAddress)
        throws IOException    
    {
	SSLServerSocket s = (SSLServerSocket) 
	    factory.createServerSocket( port, backlog, ifAddress );

        if (request_mutual_auth) 
        {
            s.setWantClientAuth( request_mutual_auth );
        }
        else if (require_mutual_auth) 
        {
            s.setNeedClientAuth( require_mutual_auth );
        }

	// Andrew T. Finnell / Change made for e-Security Inc. 2002 
        // We need a way to enable the cipher suites that we would
        // like to use. We should obtain these from the properties file.
	if( cipher_suites != null )
        {
            s.setEnabledCipherSuites ( cipher_suites );	
        }

	return s;
    }

    public boolean isSSL( java.net.ServerSocket s )
    { 
        return (s instanceof SSLServerSocket); 
    }

    public void switchToClientMode( java.net.Socket socket )
    {
        if( change_roles )
        {	
            ((SSLSocket) socket).setUseClientMode( true );
        }
    }
    
    private ServerSocketFactory createServerSocketFactory() 
    {
        Security.addProvider( new com.sun.net.ssl.internal.ssl.Provider() );

	try 
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

	    KeyStore key_store = 
		KeyStoreUtil.getKeyStore( keystore_location,
					  keystore_passphrase.toCharArray() );

	    KeyManagerFactory kmf = KeyManagerFactory.getInstance( "SunX509" );
            kmf.init( key_store, keystore_passphrase.toCharArray() );

            TrustManagerFactory tmf = null;
	    
	    //only add trusted certs, if establish trust in client
            //is required
            if((Environment.getIntProperty( "jacorb.security.ssl.server.required_options", 16 ) & 0x40) != 0 ||
               (Environment.getIntProperty( "jacorb.security.ssl.server.supported_options", 16 ) & 0x40) != 0) 
            {     
		tmf = TrustManagerFactory.getInstance( "SunX509" );
	    
		if( Environment.isPropertyOn( "jacorb.security.jsse.trustees_from_ks" ))
		{
		    tmf.init( key_store );
		}
		else
		{
		    tmf.init( (KeyStore) null );
		}
	    }
		
            SSLContext ctx = SSLContext.getInstance( "TLS" );
            ctx.init( kmf.getKeyManagers(), 
		      (tmf == null)? null : tmf.getTrustManagers(), 
		      null );

            return ctx.getServerSocketFactory();
	} 
	catch( Exception e ) 
	{
            if (logger.isWarnEnabled())
                logger.warn("Exception " + e.getMessage() + " in SSLServerSocketFactory");
	}

	return null;
    }
}
