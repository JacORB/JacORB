/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2004 Gerald Brose
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

import org.apache.avalon.framework.configuration.*;

import java.net.*;
import java.io.*;
import java.security.*;
import java.util.*;

// for use with JSSE 1.0.x
//import com.sun.net.ssl.TrustManager;
//import com.sun.net.ssl.KeyManagerFactory;
//import com.sun.net.ssl.TrustManagerFactory;
//import com.sun.net.ssl.SSLContext;
//import com.sun.net.ssl.*;
import javax.net.ssl.*;
import javax.net.*;

/**
 * @author Nicolas Noffke
 * $Id$
 */

public class SSLServerSocketFactory
    extends SSLRandom
    implements org.jacorb.orb.factory.SSLServerSocketFactory, Configurable
{
    private ServerSocketFactory factory = null;
    private boolean require_mutual_auth = false;
    private boolean request_mutual_auth = false;
    private boolean trusteesFromKS = false;
    private String[] cipher_suites = null;
    private String[] enabledProtocols = null;
    private TrustManager trustManager = null;
    private short serverSupportedOptions = 0;
    private short serverRequiredOptions = 0;
    private String keystore_location = null;
    private String keystore_passphrase = null;

    public void configure(Configuration configuration)
        throws ConfigurationException
    {
        super.configure(configuration);


        trusteesFromKS =
            configuration.getAttributeAsBoolean("jacorb.security.jsse.trustees_from_ks", false);


        serverSupportedOptions =
            Short.parseShort(
                configuration.getAttribute("jacorb.security.ssl.server.supported_options","20"),
                16); // 16 is the base as we take the string value as hex!

        serverRequiredOptions =
            Short.parseShort(
                configuration.getAttribute("jacorb.security.ssl.server.required_options","0"),
                16);

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
            configuration.getAttribute("jacorb.security.keystore","UNSET");

        keystore_passphrase =
            configuration.getAttribute("jacorb.security.keystore_password","UNSET" );

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

        if (JSSEUtil.isJDK14() && configuration.getAttribute("jacorb.security.ssl.server.protocols", null) != null)
        {
            enabledProtocols = (String[]) ((org.jacorb.config.Configuration)configuration).getAttributeList
                                            ("jacorb.security.ssl.server.protocols").toArray();
            if (logger.isDebugEnabled())
            {
                logger.debug("Setting user specified server enabled protocols : " +
                             configuration.getAttribute("jacorb.security.ssl.server.protocols", ""));
            }
        }

        JSSEUtil.registerSecurityProvider();

        try
        {
            factory = createServerSocketFactory();
        }
        catch( Exception e )
        {
            logger.warn("Unable to create ServerSocketFactory", e);

            throw new ConfigurationException("Unable to create ServerSocketFactory!", e);
        }


        // Andrew T. Finnell
        // We need to obtain all the cipher suites to use from the
        // properties file.
        String cipher_suite_list =
            configuration.getAttribute("jacorb.security.ssl.server.cipher_suites",null );

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

        if (JSSEUtil.wantClientAuth(request_mutual_auth, require_mutual_auth))
        {
            JSSEUtil.setWantClientAuth(s, request_mutual_auth);
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

        if (enabledProtocols != null)
        {
            JSSEUtil.setEnabledProtocols(s, enabledProtocols);
        }

        return s;
    }


    public ServerSocket createServerSocket( int port, int backlog )
        throws IOException
    {
        SSLServerSocket s = (SSLServerSocket)
            factory.createServerSocket( port, backlog );

        if (JSSEUtil.wantClientAuth(request_mutual_auth, require_mutual_auth))
        {
            JSSEUtil.setWantClientAuth(s, request_mutual_auth);
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

        if (enabledProtocols != null)
        {
            JSSEUtil.setEnabledProtocols(s, enabledProtocols);
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

        if (JSSEUtil.wantClientAuth(request_mutual_auth, require_mutual_auth))
        {
            JSSEUtil.setWantClientAuth(s, request_mutual_auth);
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

        if (enabledProtocols != null)
        {
            JSSEUtil.setEnabledProtocols(s, enabledProtocols);
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
                                      keystore_passphrase.toCharArray() );

        KeyManagerFactory kmf = KeyManagerFactory.getInstance( "SunX509" );
        kmf.init( key_store, keystore_passphrase.toCharArray() );
        TrustManagerFactory tmf = null;

        //only add trusted certs, if establish trust in client
        //is required
        if(( serverRequiredOptions & 0x40) != 0 ||
           ( serverSupportedOptions & 0x40) != 0)
        {
            tmf = TrustManagerFactory.getInstance( "SunX509" );

            if( trusteesFromKS )
            {
                tmf.init( key_store );
            }
            else
            {
                tmf.init( (KeyStore) null );
            }
        }

        TrustManager[] trustManagers;

        if (trustManager == null)
        {
            trustManagers = (tmf == null)? null : tmf.getTrustManagers();
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
}
