package org.jacorb.security.ssl.iaik;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 2000-2004 Gerald Brose
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

import org.jacorb.security.level2.*;
import org.jacorb.security.util.*;

import iaik.security.ssl.*;

import java.util.*;
import java.net.*;
import java.io.IOException;
import java.security.ProviderException;
import java.security.cert.X509Certificate;


/**
 * @author Andr'e Benvenuti, Gerald Brose.
 * @version $Id$
 *
 * We follow the design of socket factories in package javax.net
 * and javax.net.ssl.* Because this package doesn't exist in the JDK yet we
 * don't extend its classes, but we are fully compatible.
 *
 * The basic idea is to provide one extra constructor that sets up
 * a default SSL configuration that is implicitly used by all other
 * constructors. The default SSL credentials can also be changed by
 * calling setDefaultSSLContext().
 *
 * After creating a new SSL socket, this factory swaps roles for
 * the SSL handshake, i.e. any client socket created takes the
 * server role in the handshake, so the actual server need not
 * authenticate.
 */

public class SSLSocketFactory
    implements org.jacorb.orb.factory.SocketFactory, Configurable
{
    private String[] default_cs = null;
    private boolean isRoleChange; // rt
    private CurrentImpl securityCurrent = null;
    private org.jacorb.orb.ORB orb = null;
    private SSLContext default_context = null;
    private short clientRequirededOptions = 0;
    private short clientSupportedOptions = 0;
    private boolean iaikDebug = false;
    private List trusteeFileNames;
    private Logger logger;

    public SSLSocketFactory( org.jacorb.orb.ORB orb )
        throws ConfigurationException
    {
        this.orb = orb;
	CipherSuite[] cs = SSLSetup.getCipherSuites();
	default_cs = new String[ cs.length ];
	for ( int i = 0; i < cs.length; i++ )
        {
	    default_cs[ i ] = cs[ i ].toString();
        }
        configure( orb.getConfiguration());
    }


    public void configure(Configuration configuration)
        throws ConfigurationException
    {
        logger = 
            ((org.jacorb.config.Configuration)configuration).getNamedLogger("jacorb.security.jsse");

	isRoleChange =
            configuration.getAttributeAsBoolean("jacorb.security.change_ssl_roles",false);

        clientRequirededOptions = 
            Short.parseShort(
                configuration.getAttribute("jacorb.security.ssl.client.required_options","0"),
                16);

        clientSupportedOptions = 
            Short.parseShort(
                configuration.getAttribute("jacorb.security.ssl.client.supported_options","0"),
                16);

        trusteeFileNames =
            ((org.jacorb.config.Configuration)configuration).getAttributeList("jacorb.security.trustees");

        if( trusteeFileNames.isEmpty())
        {
            logger.warn("No trusted certificates specified. This will accept all peer certificate chains!");
        }
        iaikDebug = 
            configuration.getAttributeAsBoolean("jacorb.security.iaik_debug",false);
    }


    public Socket createSocket( String host,  int port )
	throws IOException, UnknownHostException
    {
        SSLSocket sock = null;
        try
        {
            sock = new SSLSocket( host, port, getDefaultContext() );
        }
        catch( java.security.GeneralSecurityException g)
        {
            if (logger.isWarnEnabled())
                logger.warn("GeneralSecurityException", g);
            throw new IOException(g.getMessage());
        }

        // rt: switch to server mode
        if( isRoleChange )
        {
            if (logger.isDebugEnabled())
                logger.debug("SSLSocket switch to server mode...");
	    sock.setUseClientMode( false );
	}

        return sock;
    }

    private org.jacorb.security.level2.KeyAndCert[] getSSLCredentials()
    {
        CurrentImpl securityCurrent = null;
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

    private SSLContext getDefaultContext()
	throws iaik.x509.X509ExtensionException, java.security.cert.CertificateException, 
	java.security.NoSuchAlgorithmException, java.security.InvalidKeyException,
        java.security.NoSuchProviderException, java.io.IOException
    {
        if( default_context != null )
        {
            return default_context;
        }

	if( isRoleChange )
        {
	    SSLServerContext ctx = new SSLServerContext();

            //the server always has to have certificates
            org.jacorb.security.level2.KeyAndCert[] kac = getSSLCredentials();

            for( int i = 0; i < kac.length; i++ )
            {
		ctx.addServerCredentials( (X509Certificate[]) kac[i].chain,
                                          kac[i].key );
	    }

            if( ( clientRequirededOptions & 0x20) != 0 )
            {
                //required: establish trust in target (the SSL client
                //in this case)--> force other side to authenticate
                ctx.setRequestClientCertificate( true );
                ctx.setChainVerifier( new ServerChainVerifier( true ));
	    }

            if (!trusteeFileNames.isEmpty())
            {
                for( Iterator iter = trusteeFileNames.iterator(); iter.hasNext(); )
                {
                    String fName = (String)iter.next();
                    ctx.addTrustedCertificate( CertUtils.readCertificate(fName));
                }
            }

            default_context = ctx;
	}
	else
        {
	    SSLClientContext ctx = new SSLClientContext();

            //only add own credentials, if establish trust in client
            //is supported
            if((clientSupportedOptions & 0x40) != 0 )
            {
                org.jacorb.security.level2.KeyAndCert[] kac =
                    getSSLCredentials();

                for( int i = 0; i < kac.length; i++ )
                {
                    ctx .addClientCredentials( (X509Certificate[]) kac[i].chain,
                                               kac[i].key );
                }
            }

	    //always adding trusted certificates, since in SSL, the
	    //server must always authenticate

            if (!trusteeFileNames.isEmpty())
            {
                for( Iterator iter = trusteeFileNames.iterator(); iter.hasNext(); )
                {
                    String fName = (String)iter.next();
                    ctx.addTrustedCertificate( CertUtils.readCertificate(fName));
                }
            }
            default_context = ctx;
	}

	if( iaikDebug )
        {
	    default_context.setDebugStream( System.out );
        }

        return default_context;
    }

    /**
     * Returns the list of cipher suites which are enabled by
     * default. Unless a different list is enabled, handshaking
     * on an SSL connection will use one of these cipher suites.
     * The minimum quality of service for these defaults requires
     * confidentiality protection and server authentication.
     *
     * @return array of the cipher suites enabled by default
     */

    public String[] getDefaultCipherSuites()
    {
        return default_cs;
    }

    /**
     * Returns the names of the cipher suites which could be enabled
     * for use on an SSL connection.
     * Normally, only a subset of these will actually be enabled by
     * default, since this list may include
     * cipher suites which do not meet quality of service
     * requirements for those defaults.
     * Such cipher suites are useful in specialized applications.
     *
     * @return an array of cipher suite names
     */
    public String[] getSupportedCipherSuites()
    {
	CipherSuite [] suites = CipherSuite.getDefault();
	java.lang.String lst [] = new String[ suites.length ];
	for ( int i = 0; i < lst.length; i++ )
	    lst [ i ] = suites[ i ].toString ();
	return lst;
    }

    public boolean isSSL ( Socket s )
    {
        return ( s instanceof SSLSocket);
    }
}
