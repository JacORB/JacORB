/*
 *        Written for JacORB - a free Java ORB
 *
 *   Copyright (C) 2001 Gerald Brose
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

import com.sun.net.ssl.*;

import java.net.*;
import java.io.*;
import javax.net.ssl.*;
import javax.net.*;
import java.security.*;

public class SSLServerSocketFactory 
    implements org.jacorb.orb.factory.SSLServerSocketFactory
{
    private ServerSocketFactory factory = null;
    private boolean mutual_auth = false;
    private boolean change_roles = false;

    public SSLServerSocketFactory( org.jacorb.orb.ORB orb )
    {
	factory = createServerSocketFactory();

	if( factory == null )
	{
	    Debug.output( 1, "ERROR: Unable to create ServerSocketFactory!" );
	}

	if( (Environment.requiredBySSL() & 0x40) != 0 )
	{
	    //required: establish trust in client
	    //--> force other side to authenticate
	    mutual_auth = true;
	}

	change_roles = Environment.changeSSLRoles();
    }
           
    public ServerSocket createServerSocket( int port )
        throws IOException
    {
	SSLServerSocket s = (SSLServerSocket) 
	    factory.createServerSocket( port );
	s.setNeedClientAuth( mutual_auth );

	return s;
    }


    public ServerSocket createServerSocket( int port, int backlog ) 
        throws IOException
    {
	SSLServerSocket s = (SSLServerSocket) 
	    factory.createServerSocket( port, backlog );

	s.setNeedClientAuth( mutual_auth );

	return s;
    }

    public ServerSocket createServerSocket (int port,
                                            int backlog,
                                            InetAddress ifAddress)
        throws IOException    
    {
	SSLServerSocket s = (SSLServerSocket) 
	    factory.createServerSocket( port, backlog, ifAddress );
	s.setNeedClientAuth( mutual_auth );

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
	try 
	{
            java.security.Security.addProvider( new com.sun.net.ssl.internal.ssl.Provider() );

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

            KeyStore key_store = 
                KeyStoreUtil.getKeyStore( keystore_location,
                                          keystore_password.toCharArray() );

	    KeyManagerFactory kmf = KeyManagerFactory.getInstance( "SunX509" );
            kmf.init( key_store, keystore_password.toCharArray() );

            TrustManagerFactory tmf = null;
	    
	    //only add trusted certs, if establish trust in client
            //is required
            if(( (byte) Environment.requiredBySSL() & 0x40) != 0 ) 
            {     
		tmf = TrustManagerFactory.getInstance( "SunX509" );
	    
		if( "on".equals( Environment.getProperty( "jacorb.security.jsse.trustees_from_ks",
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
            }
		
            SSLContext ctx = SSLContext.getInstance( "TLS" );
            ctx.init( kmf.getKeyManagers(), 
		      (tmf == null)? null : tmf.getTrustManagers(), 
		      null );

            return ctx.getServerSocketFactory();
	} 
	catch( Exception e ) 
	{
	    Debug.output( 1, e );
	}

	return null;
    }
}




