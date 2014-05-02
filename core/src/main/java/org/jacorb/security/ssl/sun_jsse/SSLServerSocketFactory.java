/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2012 Gerald Brose / The JacORB Team.
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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.cert.CRL;
import java.security.cert.CertPathParameters;
import java.security.cert.CertStore;
import java.security.cert.CertStoreParameters;
import java.security.cert.CertificateFactory;
import java.security.cert.CollectionCertStoreParameters;
import java.security.cert.PKIXBuilderParameters;
import java.security.cert.X509CertSelector;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import javax.net.ServerSocketFactory;
import javax.net.ssl.CertPathTrustManagerParameters;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.ManagerFactoryParameters;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import org.jacorb.config.Configurable;
import org.jacorb.config.Configuration;
import org.jacorb.config.ConfigurationException;

/**
 * @author Nicolas Noffke
 */

public class SSLServerSocketFactory
    extends SSLRandom
    implements org.jacorb.orb.factory.ServerSocketFactory, Configurable
{
    private ServerSocketFactory factory = null;
    private boolean require_mutual_auth = false;
    private boolean request_mutual_auth = false;
    private boolean trusteesFromKS = false;
    private String[] cipher_suites = null;
    private String[] enabledProtocols = null;
    private TrustManager trustManager = null;
    private int serverSupportedOptions = 0;
    private int serverRequiredOptions = 0;
    private String keystore_location = null;
    private String keystore_passphrase = null;
    private String keystore_type = null;
    private String keyManagerAlgorithm = null;
    private String trustManagerAlgorithm = null;
    
    private String keystore_provider = null; // e.g. SunPKCS11-NSS
    private String truststore_type = null; 
    private String truststore_location = null;
    private String truststore_passphrase = null;
    private String truststore_provider = null;
    private boolean support_crl = false;    // CRL support on/off
    private String crl_file = null;         // absolute path to the CRL file

    public void configure(Configuration configuration)
        throws ConfigurationException
    {
        super.configure(configuration);

        final org.jacorb.config.Configuration config = (org.jacorb.config.Configuration) configuration;

        trusteesFromKS =
            configuration.getAttributeAsBoolean("jacorb.security.jsse.trustees_from_ks", false);


        serverSupportedOptions = configuration.getAttributeAsInteger("jacorb.security.ssl.server.supported_options", 0x20, 16); // 16 is the base as we take the string value as hex!

        serverRequiredOptions = configuration.getAttributeAsInteger("jacorb.security.ssl.server.required_options", 0, 16);

        if( (serverSupportedOptions & 0x40) != 0 )
        {
            // would prefer to establish trust in client.  If client can
            // support authentication, it will, otherwise we will continue
            if (logger.isInfoEnabled())
                logger.info("Will create SSL sockets that request client authentication" );

            request_mutual_auth = true;
        }

        if( (serverRequiredOptions & 0x40) != 0 )
        {
            //required: establish trust in client
            //--> force other side to authenticate
            require_mutual_auth = true;
            request_mutual_auth = false;
            if (logger.isInfoEnabled())
                logger.info("Will create SSL sockets that require client authentication" );
        }

        keystore_location =
            configuration.getAttribute("jacorb.security.keystore");

        keystore_passphrase =
            configuration.getAttribute("jacorb.security.keystore_password");

        keystore_type=
           configuration.getAttribute("jacorb.security.keystore_type", "JKS");

        keyManagerAlgorithm =
           configuration.getAttribute("jacorb.security.jsse.server.key_manager_algorithm","SunX509");

        trustManagerAlgorithm =
           configuration.getAttribute("jacorb.security.jsse.server.trust_manager_algorithm","SunX509");
        
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

        try
        {
            trustManager = (TrustManager) ((org.jacorb.config.Configuration)configuration).getAttributeAsObject
                                               ("jacorb.security.ssl.server.trust_manager");
        }
        catch (ConfigurationException ce)
        {
            if (logger.isErrorEnabled())
            {
                logger.error("TrustManager object creation failed. Please check value of property "
                             + "'jacorb.security.ssl.server.trust_manager'. Current value: "
                             + configuration.getAttribute("jacorb.security.ssl.server.trust_manager", ""), ce);
            }
        }

        enabledProtocols = configuration.getAttributeAsStringsArray ("jacorb.security.ssl.server.protocols");
        if (enabledProtocols != null && logger.isDebugEnabled())
        {
            logger.debug("Setting user specified server enabled protocols : " +
                         configuration.getAttribute("jacorb.security.ssl.server.protocols", ""));
        }

        try
        {
            factory = createServerSocketFactory();
        }
        catch(Exception e)
        {
            logger.warn("Unable to create ServerSocketFactory : {}", e.getMessage (), e);

            throw new ConfigurationException("Unable to create ServerSocketFactory!", e);
        }

        // Andrew T. Finnell
        // We need to obtain all the cipher suites to use from the
        // properties file.
        cipher_suites = config.getAttributeAsStringsArray ("jacorb.security.ssl.server.cipher_suites");
    }

    public ServerSocket createServerSocket( int port )
        throws IOException
    {
        SSLServerSocket s = (SSLServerSocket)
            factory.createServerSocket( port );

        if (request_mutual_auth)
        {
           s.setWantClientAuth (request_mutual_auth);
        }
        else if (require_mutual_auth)
        {
            s.setNeedClientAuth( require_mutual_auth );
        }

        // Andrew T. Finnell / Change made for e-Security Inc. 2002
        // We need a way to enable the cipher suites that we would
        // like to use. We should obtain these from the properties file.
        if (cipher_suites != null)
        {
            s.setEnabledCipherSuites ( cipher_suites );
        }

        if (enabledProtocols != null)
        {
           s.setEnabledProtocols (enabledProtocols);
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
           s.setWantClientAuth (request_mutual_auth);
        }
        else if (require_mutual_auth)
        {
            s.setNeedClientAuth( require_mutual_auth );
        }

        // Andrew T. Finnell / Change made for e-Security Inc. 2002
        // We need a way to enable the cipher suites that we would
        // like to use. We should obtain these from the properties file.
        if (cipher_suites != null)
        {
            s.setEnabledCipherSuites (cipher_suites);
        }

        if (enabledProtocols != null)
        {
           s.setEnabledProtocols (enabledProtocols);
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
           s.setWantClientAuth (request_mutual_auth);
        }
        else if (require_mutual_auth)
        {
            s.setNeedClientAuth( require_mutual_auth );
        }

        // Andrew T. Finnell / Change made for e-Security Inc. 2002
        // We need a way to enable the cipher suites that we would
        // like to use. We should obtain these from the properties file.
        if (cipher_suites != null)
        {
            s.setEnabledCipherSuites (cipher_suites);
        }

        if (enabledProtocols != null)
        {
           s.setEnabledProtocols (enabledProtocols);
        }

        return s;
    }

    public boolean isSSL( java.net.ServerSocket s )
    {
        return (s instanceof SSLServerSocket);
    }

    private ServerSocketFactory createServerSocketFactory()
        throws IOException, java.security.GeneralSecurityException
    {
        KeyStore key_store =
            KeyStoreUtil.getKeyStore( keystore_location,
                                      keystore_passphrase.toCharArray(),
                                      keystore_type, 
                                      keystore_provider);

        KeyManagerFactory kmf = KeyManagerFactory.getInstance(keyManagerAlgorithm);

        // Bugzilla #883: PKCS 11 and CRL support for SSL
        // Password for "WINDOWS-MY" store type doesn't need to be set
        if ("WINDOWS-MY".equalsIgnoreCase (keystore_type))
        {
            kmf.init( key_store, null );
            
        }
        else
        {
            kmf.init( key_store, keystore_passphrase.toCharArray() );
        }
        
        TrustManagerFactory tmf = null;
        KeyStore trust_store = null;
        //only add trusted certs, if establish trust in client
        //is required
        if(( serverRequiredOptions & 0x40) != 0 ||
           ( serverSupportedOptions & 0x40) != 0)
        {
            tmf = TrustManagerFactory.getInstance (trustManagerAlgorithm);

            if( trusteesFromKS )
            {
                trust_store = key_store;
                
            }
            else
            {
                if ( "PKCS11".equalsIgnoreCase(truststore_type) ) 
                {
                    trust_store = KeyStore.getInstance (truststore_type, truststore_provider);
                    trust_store.load (null, truststore_passphrase.toCharArray());
                }
                else if ( "WINDOWS-ROOT".equalsIgnoreCase(truststore_type) )
                {
                    trust_store = KeyStore.getInstance("WINDOWS-ROOT");
                    trust_store.load (null, null);
                }
                else if (truststore_location != null && truststore_passphrase != null)
                {
                    trust_store = KeyStoreUtil.getKeyStore(truststore_location, 
                                                           truststore_passphrase.toCharArray(),
                                                           truststore_type);
                }
                logger.debug ("SSLServerSocketFactory: loaded trust store: " 
                              + ((trust_store != null) ? trust_store.getProvider() : "default (null)"));
              
            }
            
            if( trust_store != null && support_crl )
            {
                CertPathParameters pkixParams =  null;
                
                //create the selector to filter the trusted CA from others.
                X509CertSelector x509CertSelector = new X509CertSelector();
                x509CertSelector.setCertificateValid( new Date() );

                PKIXBuilderParameters xparams = new PKIXBuilderParameters( trust_store, x509CertSelector );
                Collection<? extends CRL> crls = getCRLs();

                CertStoreParameters cparam = new CollectionCertStoreParameters( crls );
                CertStore cs = CertStore.getInstance( "Collection", cparam );

                //Specify that revocation checking is to be enabled
                xparams.setRevocationEnabled( true );
                
                //add the certificate store
                xparams.addCertStore( cs );
                pkixParams = xparams;

                //Wrap them as trust manager parameters
                ManagerFactoryParameters trustParams = new CertPathTrustManagerParameters( pkixParams );

                //Pass parameters to factory to be passed to CertPath implementation
                tmf.init( trustParams );
            }
            else 
            {
                tmf.init( trust_store );
            }
        }

        TrustManager[] trustManagers;

        if (trustManager == null)
        {
            trustManagers = (tmf == null) ? null : tmf.getTrustManagers();
        }
        else
        {
            trustManagers = new TrustManager[] { trustManager };
            if (logger.isDebugEnabled())
            {
                logger.debug("Setting user specified server TrustManger : " + trustManager.getClass().toString());
            }
        }

        SSLContext ctx = SSLContext.getInstance( "TLS" );
        ctx.init( kmf.getKeyManagers(),
                  trustManagers,
                  getSecureRandom());

        return ctx.getServerSocketFactory();
    }
    
    private Collection<? extends CRL> getCRLs () throws IOException, GeneralSecurityException
    {
        logger.debug( "SSLServerSocketFactory: Loading the CRLs from file: " + crl_file );
        File crlFile = new File( crl_file );
        Collection <? extends CRL> crls = null;
        InputStream is = null;
        try
        {
            CertificateFactory cf = CertificateFactory.getInstance( "X.509" );
            is = new FileInputStream( crlFile );
            crls = cf.generateCRLs( is );

            if (logger.isDebugEnabled ())
            {
                logger.debug ("SSLServerSocketFactory: Found CLRs:");
                Iterator <? extends CRL> it = crls.iterator ();
                while (it.hasNext ())
                {
                    logger.debug (it.next().toString ());
                }
            }
                
        }
        catch ( IOException ex )
        {
            logger.error ("SSLServerSocketFactory: CLRs loading failed: ", ex);
            throw ex;
        }
        catch (GeneralSecurityException gse)
        {
            logger.error ("SSLServerSocketFactory: CLRs security error: ", gse);
            throw gse;
        }
        finally
        {
            if (is != null)
            {
                try
                {
                    is.close ();
                }
                catch (Exception ex) { /* ignored */ }
            }
        }
        return crls;
   }
}
