package org.jacorb.security.jsse;


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
import java.security.*;

public class SSLServerSocketFactory 
    implements org.jacorb.orb.factory.SSLServerSocketFactory
{
    private javax.net.ssl.SSLServerSocketFactory factory = null;

    public SSLServerSocketFactory(org.jacorb.orb.ORB orb)
    {
        try
        {
            CurrentImpl securityCurrent = (CurrentImpl)
                orb.resolve_initial_references("SecurityCurrent");

            TrustManagerFactory tmf = TrustManagerFactory.getInstance( "SunX509" );

            KeyAndCert[] kac = securityCurrent.getSSLCredentials();


            SSLContext ctx = SSLContext.getInstance( "TLS" );
            ctx.init( new KeyManager[]{ new X509KeyManagerImpl( kac ) },
                      tmf.getTrustManagers(), 
                      null );
            
            factory = ctx.getServerSocketFactory();
    
        }
        catch ( Exception e )
        {
            Debug.output( 2, e );
        } 
    }
           
    public ServerSocket createServerSocket( int port )
        throws IOException
    {
        return factory.createServerSocket( port );
    }


    public ServerSocket createServerSocket(int port,int backlog) 
        throws IOException
    {
        return factory.createServerSocket( port, backlog );
    }

    public ServerSocket createServerSocket (int port,
                                            int backlog,
                                            InetAddress ifAddress)
        throws IOException    
    {
        return factory.createServerSocket( port, backlog, ifAddress );
    }

    public boolean isSSL (java.net.ServerSocket s)
    { 
        return (s instanceof SSLServerSocket); 
    }

    public void switchToClientMode( java.net.Socket socket )
    {
        // rt: switch to client mode
        if( Environment.changeSSLRoles())
        {	
            ((SSLSocket) socket).setUseClientMode( true );
        }
    }
}






