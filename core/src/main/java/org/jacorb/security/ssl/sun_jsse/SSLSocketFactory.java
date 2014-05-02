package org.jacorb.security.ssl.sun_jsse;

/*
 *       JacORB - a free Java ORB
 *
 *   Copyright (C) 2000-2012 Gerald Brose / The JacORB Team.
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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyStore;
import java.security.cert.CertStore;
import java.security.cert.CertStoreParameters;
import java.security.cert.CertificateFactory;
import java.security.cert.CollectionCertStoreParameters;
import java.security.cert.PKIXBuilderParameters;
import java.security.cert.X509CRL;
import java.security.cert.X509CertSelector;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.net.SocketFactory;
import javax.net.ssl.CertPathTrustManagerParameters;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.ManagerFactoryParameters;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import org.jacorb.config.Configuration;
import org.jacorb.config.ConfigurationException;
import org.jacorb.orb.ORB;
import org.jacorb.orb.factory.AbstractSocketFactory;
import org.jacorb.orb.listener.SSLHandshakeListener;
import org.jacorb.orb.listener.SSLSessionListener;

/**
 * a SocketFactory implementation that allows
 * to create sockets that support SSL.
 *
 * @author Nicolas Noffke
 */
public class SSLSocketFactory
    extends AbstractSocketFactory
{
    private SocketFactory factory = null;
    private String[] cipher_suites = null;
    private String[] enabledProtocols = null;
    private TrustManager trustManager = null;

    private boolean trusteesFromKS = false;
    private int clientSupportedOptions = 0;
    private String keystore_location = null;
    private String keystore_passphrase = null;
    private final SSLSessionListener sslListener;
    private SSLRandom sslRandom;
    private String keystore_type = null;
    private String keyManagerAlgorithm = null;
    private String trustManagerAlgorithm = null;
    
    // Bugzilla#883: added the PKCS11 and CRL support
    private String keystore_provider = null;        // e.g.: SunPKCS11-NSS
    private String truststore_type = null;
    private String truststore_location = null;
    private String truststore_passphrase = null;
    private String truststore_provider = null;
    
    private boolean support_crl = false;    // CRL support on/off
    private String crl_file = null;         // absolute path to the CRL file
    
    public SSLSocketFactory(ORB orb)
    {
         sslListener = orb.getTransportManager().getSocketFactoryManager().getSSLListener();
    }

    public void configure(Configuration configuration)
        throws ConfigurationException
    {
        super.configure(configuration);

        sslRandom = new SSLRandom();
        sslRandom.configure(configuration);

        trusteesFromKS =
            configuration.getAttributeAsBoolean("jacorb.security.jsse.trustees_from_ks", false);

        keystore_location =
            configuration.getAttribute("jacorb.security.keystore");

        keystore_passphrase =
            configuration.getAttribute("jacorb.security.keystore_password");

        keystore_type=
           configuration.getAttribute("jacorb.security.keystore_type", "JKS");
        
        keystore_provider =
            configuration.getAttribute("jacorb.security.keystore_provider", null);

        truststore_type =
            configuration.getAttribute("jacorb.security.truststore_type", null);

        truststore_location =
            configuration.getAttribute("jacorb.security.truststore", null);

        truststore_passphrase =
            configuration.getAttribute("jacorb.security.truststore_password", null);

        truststore_provider =
            configuration.getAttribute("jacorb.security.truststore_provider", null);

        crl_file =
            configuration.getAttribute("jacorb.security.crl_file", null);

        support_crl =
            configuration.getAttributeAsBoolean("jacorb.security.support_crl", false);

        keyManagerAlgorithm =
           configuration.getAttribute("jacorb.security.jsse.client.key_manager_algorithm","SunX509");

        trustManagerAlgorithm =
           configuration.getAttribute("jacorb.security.jsse.client.trust_manager_algorithm","SunX509");

        clientSupportedOptions = configuration.getAttributeAsInteger("jacorb.security.ssl.client.supported_options", 0, 16);
        
        try
        {
            trustManager = (TrustManager) ((org.jacorb.config.Configuration)configuration).getAttributeAsObject
                                            ("jacorb.security.ssl.client.trust_manager");
        }
        catch (ConfigurationException e)
        {
            if (logger.isErrorEnabled())
            {
                logger.error("TrustManager object creation failed. Please check value of property "
                             + "'jacorb.security.ssl.client.trust_manager'. Current value: "
                             + configuration.getAttribute("jacorb.security.ssl.client.trust_manager", ""), e);
            }
        }

        if (configuration.getAttribute("jacorb.security.ssl.client.protocols", null) != null)
        {
            enabledProtocols = 
                configuration.getAttributeAsStringsArray ("jacorb.security.ssl.server.protocols");

            if (logger.isDebugEnabled())
            {
                logger.debug("Setting user specified client enabled protocols : " 
                             + configuration.getAttribute("jacorb.security.ssl.client.protocols", ""));
            }
        }

        try
        {
            factory = createSocketFactory();
        }
        catch( Exception e )
        {
            logger.error("Unable to create SSLSocketFactory!", e);
            throw new ConfigurationException("Unable to create SSLSocketFactory!", e);
        }

        // Andrew T. Finnell / Change made for e-Security Inc. 2002
        // We need to obtain all the cipher suites to use from the
        // properties file.
        cipher_suites = 
            configuration.getAttributeAsStringsArray ("jacorb.security.ssl.client.cipher_suites");
    }

    public Socket createSocket( String host,
                                int port )
        throws IOException, UnknownHostException
    {
        SSLSocket socket = (SSLSocket)factory.createSocket( host, port );

        initSSLSocket(socket);

        return socket;
    }

    protected Socket doCreateSocket(String host, int port, int timeout) throws IOException
    {
        SSLSocket socket = (SSLSocket)factory.createSocket();
        socket.connect(new InetSocketAddress(host, port), timeout);

        initSSLSocket(socket);

        return socket;
    }

    /**
     * common ssl socket initialization
     * @param socket
     */
    private void initSSLSocket(SSLSocket socket)
    {
        // Andrew T. Finnell
        // We need a way to enable the cipher suites that we would like to use
        // We should obtain these from the properties file
        if ( cipher_suites != null )
        {
            socket.setEnabledCipherSuites( cipher_suites );
        }

        if (enabledProtocols != null)
        {
           socket.setEnabledProtocols (enabledProtocols);
        }

        socket.addHandshakeCompletedListener(new SSLHandshakeListener(logger, sslListener));
    }

    public boolean isSSL( java.net.Socket socket )
    {
        return (socket instanceof SSLSocket);
    }

    private SocketFactory createSocketFactory()
        throws IOException, java.security.GeneralSecurityException
    {
        KeyManagerFactory kmf = null;
        KeyStore key_store = null;

        if( trusteesFromKS || ( clientSupportedOptions & 0x40) != 0 )
        {
            key_store =
                KeyStoreUtil.getKeyStore( keystore_location,
                                          keystore_passphrase.toCharArray(),
                                          keystore_type, 
                                          keystore_provider );

            //only add own credentials, if establish trust in
            //client is supported
            if( ( clientSupportedOptions & 0x40) != 0 )
            {
                kmf = KeyManagerFactory.getInstance( keyManagerAlgorithm );
                
                // Bugzilla #883: PKCS 11 and CRL support for SSL
                // Password for "WINDOWS-MY" store type doesn't need to be set
                if ( "WINDOWS-MY".equalsIgnoreCase( keystore_type ) )
                {
                    kmf.init( key_store, null );
                }
                else
                {
                    kmf.init( key_store, keystore_passphrase.toCharArray() );
                }
            }
        }

        TrustManagerFactory tmf =
            TrustManagerFactory.getInstance( trustManagerAlgorithm );

        KeyStore trust_store = null;

        if ( trusteesFromKS )
        {
            trust_store = key_store;
        }
        else
        {
            if ( "PKCS11".equalsIgnoreCase( truststore_type ) ) 
            {
                trust_store = KeyStore.getInstance( truststore_type, truststore_provider );
                trust_store.load( null, truststore_passphrase.toCharArray() );
            }
            else if ( "WINDOWS-ROOT".equalsIgnoreCase( truststore_type ) )
            {
                trust_store = KeyStore.getInstance( "WINDOWS-ROOT" );
                trust_store.load( null, null );
            }
            else if (truststore_location != null && truststore_passphrase != null)
            {
                trust_store = KeyStoreUtil.getKeyStore( truststore_location, 
                                                        truststore_passphrase.toCharArray(),
                                                        truststore_type, 
                                                        truststore_provider);
            }
        }
        
        if (trust_store != null)
        {
            //take trusted certificates from keystore
            if (logger.isInfoEnabled())
            {
                logger.info("Loading trusted certs from keystore " + trust_store.getType() );
            }
            
        }
            
        if ( trust_store != null && support_crl )
        {
            PKIXBuilderParameters pkixParams =  null;
            
            //create the selector to filter the trusted CA from others.
            X509CertSelector x509CertSelector = new X509CertSelector();
            x509CertSelector.setCertificateValid( new Date() );

            pkixParams = new PKIXBuilderParameters( trust_store, x509CertSelector );

            //the CRL file
            InputStream crlFileStream = new FileInputStream( crl_file );
            
            //the usage of the certificate
            boolean[] keyUsage = {true};
            x509CertSelector.setKeyUsage( keyUsage );
            
            CertificateFactory crlf = CertificateFactory.getInstance( "X.509" );
            X509CRL x509crl = (X509CRL) crlf.generateCRL( crlFileStream );

            List list = new ArrayList();
            list.add( x509crl );
            CertStoreParameters cparam = new CollectionCertStoreParameters( list );
            CertStore cs = CertStore.getInstance( "Collection", cparam );
            
            //Specify that revocation checking is to be enabled
            pkixParams.setRevocationEnabled( true );
            
            //add the certificate store
            pkixParams.addCertStore( cs );
            
            //Wrap them as trust manager parameters
            ManagerFactoryParameters trustParams = new CertPathTrustManagerParameters( pkixParams );

            //Pass parameters to factory to be passed to CertPath implementation
            tmf.init( trustParams );
        }
        else
        {
            tmf.init( trust_store );
        }
  
        TrustManager[] trustManagers;

        if (trustManager == null)
        {
            trustManagers = tmf.getTrustManagers();
        }
        else
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("Setting user specified TrustManager for the client: " + trustManager.getClass().getName());
            }
            trustManagers = new TrustManager[] { trustManager };
        }

        SSLContext ctx = SSLContext.getInstance( "TLS" );

        ctx.init( (kmf == null)? null : kmf.getKeyManagers(),
                  trustManagers,
                  sslRandom.getSecureRandom() );

        return ctx.getSocketFactory();
    }
}
