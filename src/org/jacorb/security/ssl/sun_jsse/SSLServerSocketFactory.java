package org.jacorb.security.ssl.sun_jsse;


/**
 * @author Nicolas Noffke
 * $Id$
 */
import org.jacorb.util.*;
import org.jacorb.security.util.*;
import org.jacorb.security.level2.*;

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
        Security.addProvider( new com.sun.net.ssl.internal.ssl.Provider() );

	factory = createServerSocketFactory();

	if( factory == null )
	{
	    Debug.output( 1, "ERROR: Unable to create ServerSocketFactory!" );
	}

	if( (Environment.getIntProperty( "jacorb.security.ssl.server.required_options", 16 ) & 0x40) != 0 )
	{
	    //required: establish trust in client
	    //--> force other side to authenticate
	    mutual_auth = true;
	}

	change_roles = 
            Environment.isPropertyOn( "jacorb.security.change_ssl_roles" );
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
            if((Environment.getIntProperty( "jacorb.security.ssl.server.required_options", 16 ) & 0x40) != 0 ) 
            {     
		tmf = TrustManagerFactory.getInstance( "SunX509" );
	    
		if( Environment.isPropertyOn( "jacorb.security.jsse.trustees_from_ks" ))
		{
		    tmf.init( key_store );
		}
		else
		{
		    tmf.init( null );
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
