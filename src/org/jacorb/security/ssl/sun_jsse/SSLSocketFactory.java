package org.jacorb.security.ssl.sun_jsse;

/*
 *       JacORB - a free Java ORB
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

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyStore;
import java.util.StringTokenizer;

import javax.net.SocketFactory;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.jacorb.orb.ORB;
import org.jacorb.orb.factory.AbstractSocketFactory;
import org.jacorb.orb.listener.SSLHandshakeListener;
import org.jacorb.orb.listener.SSLSessionListener;

/**
 * a SocketFactory implementation that allows
 * to create sockets that support SSL.
 *
 * @author Nicolas Noffke
 * $Id$
 */
public class SSLSocketFactory
    extends AbstractSocketFactory
{
    private SocketFactory factory = null;
    private String[] cipher_suites = null;
    private String[] enabledProtocols = null;
    private TrustManager trustManager = null;

    private boolean trusteesFromKS = false;
    private short clientSupportedOptions = 0;
    private String keystore_location = null;
    private String keystore_passphrase = null;
    private final SSLSessionListener sslListener;
    private SSLRandom sslRandom;

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
            configuration.getAttribute("jacorb.security.keystore", "UNSET");

        keystore_passphrase =
            configuration.getAttribute("jacorb.security.keystore_password", "UNSET");

        clientSupportedOptions =
            Short.parseShort(
                configuration.getAttribute("jacorb.security.ssl.client.supported_options", "0"),
                16);
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

        if (JSSEUtil.isJDK14() && configuration.getAttribute("jacorb.security.ssl.client.protocols", null) != null)
        {
            enabledProtocols = (String[]) ((org.jacorb.config.Configuration)configuration).getAttributeList
                                            ("jacorb.security.ssl.client.protocols").toArray();
            if (logger.isDebugEnabled())
            {
                logger.debug("Setting user specified client enabled protocols : " +
                             configuration.getAttribute("jacorb.security.ssl.client.protocols", ""));
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
        String cipher_suite_list =
            configuration.getAttribute("jacorb.security.ssl.server.cipher_suites", null );

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

                // This will fill the array in reverse order but that doesn't
                // matter
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
        SSLSocket socket = (SSLSocket)factory.createSocket( host, port );

        initSSLSocket(socket);

        return socket;
    }

    public Socket createSocket(String host, int port, int timeout) throws IOException
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
        if( cipher_suites != null )
        {
            socket.setEnabledCipherSuites( cipher_suites );
        }

        if (enabledProtocols != null)
        {
            JSSEUtil.setEnabledProtocols(socket, enabledProtocols);
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
        JSSEUtil.registerSecurityProvider();

        KeyManagerFactory kmf = null;
        KeyStore key_store = null;

        if( trusteesFromKS || ( clientSupportedOptions & 0x40) != 0 )
        {
            key_store =
                KeyStoreUtil.getKeyStore( keystore_location,
                                          keystore_passphrase.toCharArray() );
            //only add own credentials, if establish trust in
            //client is supported
            if( ( clientSupportedOptions & 0x40) != 0 )
            {
                kmf = KeyManagerFactory.getInstance( "SunX509" );
                kmf.init( key_store, keystore_passphrase.toCharArray() );
            }
        }

        TrustManagerFactory tmf =
            TrustManagerFactory.getInstance( "SunX509" );

        if( key_store != null && trusteesFromKS )
        {
            //take trusted certificates from keystore
            if (logger.isInfoEnabled())
            {
                logger.info("Loading certs from keystore " + key_store.getType() );
            }
            tmf.init( key_store );
        }
        else
        {
            tmf.init( (KeyStore) null );
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
                logger.debug("Setting user specified client TrustManger : " + trustManager.getClass().getName());
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
