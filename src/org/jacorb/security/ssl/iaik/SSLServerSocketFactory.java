package org.jacorb.security.ssl.iaik;

/*
 *        Written for JacORB - a free Java ORB
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
 */


import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.configuration.*;

import org.jacorb.security.util.*;
import org.jacorb.security.level2.*;

import iaik.security.ssl.*;

import java.net.*;
import java.util.*;
import java.io.IOException;
import java.security.ProviderException;
import java.security.cert.X509Certificate;

public class SSLServerSocketFactory
    implements org.jacorb.orb.factory.SSLServerSocketFactory, Configurable
{
    private SSLServerContext defaultContext;
    private CipherSuite[] cs;
    private Logger logger;
    private short serverRequiredOptions = 0;
    private short serverSupportedOptions = 0;
    private boolean iaikDebug = false;
    private boolean isRoleChange; // rt
    private List trusteeFileNames;
    private org.jacorb.orb.ORB orb;

    public SSLServerSocketFactory( org.jacorb.orb.ORB orb )
        throws ConfigurationException
    {
        this.orb = orb;
        cs = SSLSetup.getCipherSuites();
        configure( orb.getConfiguration());
    }

    public void configure(Configuration configuration)
        throws ConfigurationException
    {
        logger = 
            ((org.jacorb.config.Configuration)configuration).getNamedLogger("jacorb.security.jsse");

	isRoleChange =
            configuration.getAttributeAsBoolean("jacorb.security.change_ssl_roles",false);

        serverRequiredOptions = 
            Short.parseShort(
                             configuration.getAttribute("jacorb.security.ssl.server.required_options","0"),
                             16);

        serverSupportedOptions = 
            Short.parseShort(
                             configuration.getAttribute("jacorb.security.ssl.server.aupported_options","0"),
                             16);

        defaultContext = new SSLServerContext();

        try
        {
            if( isRoleChange)
            {
                if(( serverSupportedOptions  & 0x20) != 0 )
                    //Establish trust in target supported means
                    //that we must have own certificates
                {
                    org.jacorb.security.level2.KeyAndCert[] kac =
                        getSSLCredentials( orb );

                    for( int i = 0; i < kac.length; i++ )
                    {
                        defaultContext.addClientCredentials( (X509Certificate[]) kac[i].chain,
                                                             kac[i].key );
                    }

                    //no session cache
                    ((DefaultSessionManager)defaultContext.getSessionManager()).setResumePeriod( 0 );
                }
            }
            else
            {
                //the SSL server always has to have own certificates
                org.jacorb.security.level2.KeyAndCert[] kac =
                    getSSLCredentials( orb );

                for( int i = 0; i < kac.length; i++ )
                {
                    defaultContext.addServerCredentials( (X509Certificate[]) kac[i].chain,
                                                         kac[i].key );
                }

                if(( serverRequiredOptions  & 0x40) != 0 )
                    //Establish trust in client requireded means
                    //that we must request the clients certificates
                {
                    defaultContext.setRequestClientCertificate( true );
                    defaultContext.setChainVerifier( new ServerChainVerifier( true ));

                    trusteeFileNames =
                        ((org.jacorb.config.Configuration)configuration).getAttributeList("jacorb.security.trustees");
                
                    if( trusteeFileNames.isEmpty())
                    {
                        logger.warn("No trusted certificates specified. This will accept all peer certificate chains!");
                    }
                    else
                    {
                        for( Iterator iter = trusteeFileNames.iterator(); iter.hasNext(); )
                        {
                            String fName = (String)iter.next();
                            defaultContext.addTrustedCertificate( CertUtils.readCertificate(fName));
                        }
                    }

                }
            }
        }
        catch( Exception g)
        {
            if (logger.isWarnEnabled())
                logger.warn("GeneralSecurityException", g);
            throw new ConfigurationException(g.getMessage());
        }
	if( iaikDebug )
        {
	    defaultContext.setDebugStream( System.out );
        }
    }

    private org.jacorb.security.level2.KeyAndCert[] getSSLCredentials( org.jacorb.orb.ORB orb )
    {
        CurrentImpl  securityCurrent = null;

        try
        {
            securityCurrent = 
                (CurrentImpl)orb.resolve_initial_references("SecurityCurrent");
        }
        catch ( org.omg.CORBA.ORBPackage.InvalidName in )
        {
            throw new ProviderException("Unable to obtain Security Current.");
        }

        return securityCurrent.getSSLCredentials();
    }

    /**
     * Returns a server socket which uses all network interfaces on
     * the host, and is bound to the specified port.
     * Parameters:
     *     port - the port to listen to
     * Throws:
     *     IOException - for networking errors
     */

    public ServerSocket createServerSocket (int port)
        throws IOException
    {
        if (defaultContext == null)
            throw new IOException("Cannot support SSL, no default SSL context found!");

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
     *     IOException - for networking errors
     */

    public ServerSocket createServerSocket(int port,int backlog)
        throws IOException
    {
        if ( defaultContext == null )
            throw new IOException("Cannot support SSL, no default SSL context found!");

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
     *     IOException - for networking errors
     */

    public ServerSocket createServerSocket (int port,
                                            int backlog,
                                            InetAddress ifAddress)
        throws IOException
    {
        if (defaultContext == null)
            throw new IOException("Cannot support SSL, no default SSL context found!");
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

    public String[] getDefaultCipherSuites()
    {
        String lst[] = new String[cs.length];
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

    public String[] getSupportedCipherSuites()
    {
        CipherSuite[] suites = CipherSuite.getDefault ();
        String lst[] = new String[ suites.length ];
        for( int i = 0; i < lst.length; i++ )
            lst [ i ] = suites[ i ].toString ();
        return lst;
    }

    public boolean isSSL( ServerSocket s )
    {
        return (s instanceof SSLServerSocket);
    }

    public void switchToClientMode( Socket socket )
    {
        // rt: switch to client mode
        if( isRoleChange )
        {
            try
            {
                if (logger.isDebugEnabled())
                    logger.debug("SSLSocket switch to server mode...");
                ((SSLSocket) socket).setUseClientMode( true );
            }
            catch( IOException iox )
            {
                if (logger.isWarnEnabled())
                    logger.warn("IOException", iox);
            }
        }
    }
}
