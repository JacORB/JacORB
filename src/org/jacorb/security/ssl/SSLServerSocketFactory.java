package org.jacorb.security.ssl;

/*
 *        Written for JacORB - a free Java ORB
 *
 *   Copyright (C) 1999  André Benvenuti.
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

/* 
 * We follow the design of socket factories in package javax.net 
 * and javax.net.ssl. Because this package don't the JDK yet we 
 * don't extend its classes, but we are fully compatible.
 *
 * The basic idea is to setup policies related to the sockets being 
 * constructed, in the factory: no special configuration is done in 
 * the code which asks for the sockets.
 * 
 * We will not define an abstract SSLSocketFactory but implement one 
 * using the iSaSiLk packages.
 * The sockets returned to the application have to be subclasses of 
 * java.net.Socket,
 * so we can return sockets of class iaik.security.ssl.Socket.
 * By now we wont directly expose security relevant APIs, but will
 *  probably as we implement Security Objects. 
 * Which factory classes is used will de decide in org.jacorb.util.Environment 
 * as this is specific to the environment configuration.
 * So the getDefault method could return null if no SSL support at all, 
 * or a factory that encapsulates a particular implementation and take 
 * care of initialising and pass specific parameters. 
 * bnv: just to be sure...
 */

import org.jacorb.util.*;
import org.jacorb.security.util.*;
import org.jacorb.security.level2.*;

import iaik.security.ssl.*;

public class SSLServerSocketFactory 
    implements org.jacorb.orb.factory.SSLServerSocketFactory
{
    private SSLServerContext defaultContext;
    private CipherSuite[] cs;

    public SSLServerSocketFactory(jacorb.orb.ORB orb)
    {
        cs = SSLSetup.getCipherSuites();

        String [] trusteeFileNames = 
            Environment.getPropertyValueList("jacorb.security.trustees");

        defaultContext = new SSLServerContext();

        if( ( (byte)Environment.supportedBySSL() & 0x20) != 0  ) 
            // 32 = Establish trust in target
        {
            try
            {
                CurrentImpl securityCurrent = 
                    (CurrentImpl) orb.resolve_initial_references("SecurityCurrent");
                
                org.jacorb.security.level2.KeyAndCert[] kac = 
                    securityCurrent.getSSLCredentials();
                

                if( Environment.changeSSLRoles())
                {	
                    for( int i = 0; i < kac.length; i++ )
                    { 
                        defaultContext.addClientCredentials( kac[i].chain, 
                                                             kac[i].key );
                    }                   
                    
                    ((DefaultSessionManager) 
                     defaultContext.getSessionManager()).setResumePeriod( 0 );
                    
                }
                else
                {
                    for( int i = 0; i < kac.length; i++ )
                    { 
                        defaultContext.addServerCredentials( kac[i].chain, 
                                                             kac[i].key );
                    }                   
                }                    
            }
            catch( org.omg.CORBA.ORBPackage.InvalidName in )
            {}
        }

        for( int i = 0; i < trusteeFileNames.length; i++ )
        {
            defaultContext.addTrustedCertificate( CertUtils.readCertificate( trusteeFileNames[i] ));
        }

        //defaultContext.setDebugStream( System.out );
    }
           
    /**
     * Returns a server socket which uses all network interfaces on 
     * the host, and is bound to the specified port.
     * Parameters:
     *     port - the port to listen to
     * Throws:
     *     java.io.IOException - for networking errors
     */

    public java.net.ServerSocket createServerSocket (int port)
        throws java.io.IOException
    {
        if (defaultContext == null) 
            throw new java.io.IOException("Cannot support SSL, no default SSL context found!");

        return new SSLServerSocket(port, defaultContext);
    }

    /** Returns a server socket which uses all network interfaces 
     * on the host, is bound to a the specified port, and uses the 
     * specified connection backlog. The socket is configured with 
     * the socket options (such as accept timeout) given to this factory.
     * Parameters:
     *     port - the port to listen to
     *     backlog - how many connections are queued
     * Throws:
     *     java.io.IOException - for networking errors
     */

    public java.net.ServerSocket createServerSocket(int port,int backlog) 
        throws java.io.IOException
    {
        if ( defaultContext == null ) 
            throw new java.io.IOException("Cannot support SSL, no default SSL context found!");
    
        return new SSLServerSocket(port, backlog, defaultContext);
    }

    /** 
     * Returns a server socket which uses only the specified network 
     * interface on the local host, is bound to a the specified port, 
     * and uses the specified connection backlog. The socket is 
     * configured with the socket options (such as accept timeout) 
     * given to this factory.
     * Parameters:
     *     port - the port to listen to
     *     backlog - how many connections are queued
     *     ifAddress - the network interface address to use
     * Throws:
     *     java.io.IOException - for networking errors
     */

    public java.net.ServerSocket createServerSocket (int port,
                                                     int backlog,
                                                     java.net.InetAddress ifAddress)
        throws java.io.IOException    
    {
        if (defaultContext == null)
            throw new java.io.IOException("Cannot support SSL, no default SSL context found!");
        return new SSLServerSocket (port, backlog, ifAddress, defaultContext);
    }

    /**
     * Returns the list of cipher suites which are enabled by 
     * default. Unless a different list is enabled, handshaking 
     * on an SSL connection will use one of these cipher suites.
     * The minimum quality of service for these defaults requires
     * confidentiality protection and server authentication.
     * Returns:
     *    array of the cipher suites enabled by default
     * See Also: 
     */

    public java.lang.String[] getDefaultCipherSuites()
    {
        java.lang.String lst[] = new java.lang.String[cs.length];
        for (int i = 0; i < lst.length; i++)
            lst [i] = cs[i].toString();
        return lst;
    }

    /**
     * Returns the names of the cipher suites which could be 
     * enabled for use on an SSL connection.
     * Normally, only a subset of these will actually be enabled 
     * by default, since this list may include
     * cipher suites which do not meet quality of service requirements for those defaults.
     * Such cipher suites are useful in specialized applications.
     * Returns:
     *     an array of cipher suite names
     */

    public java.lang.String[] getSupportedCipherSuites()
    {
        CipherSuite [] suites = CipherSuite.getDefault ();
        java.lang.String lst [] = new java.lang.String[ suites.length ];
        for ( int i = 0; i < lst.length; i++ )
            lst [ i ] = suites[ i ].toString ();
        return lst;
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
            try
            {
                ((SSLSocket) socket).setUseClientMode( true );
            }
            catch( java.io.IOException iox )
            {
                Debug.output( Debug.SECURITY | Debug.IMPORTANT, iox );
            }
        }
    }
}
