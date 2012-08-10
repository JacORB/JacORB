package org.jacorb.security.ssl.sun_jsse;

/*
 *        JacORB - a free Java ORB
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

import java.io.ByteArrayInputStream;
import java.security.cert.CertificateFactory;
import java.util.HashMap;
import javax.net.ssl.SSLSocket;
import org.jacorb.config.Configurable;
import org.jacorb.config.Configuration;
import org.jacorb.config.ConfigurationException;
import org.jacorb.orb.dsi.ServerRequest;
import org.jacorb.orb.giop.GIOPConnection;
import org.jacorb.orb.iiop.ServerIIOPConnection;
import org.jacorb.orb.portableInterceptor.ServerRequestInfoImpl;
import org.omg.PortableInterceptor.ForwardRequest;
import org.omg.PortableInterceptor.ServerRequestInfo;
import org.omg.PortableInterceptor.ServerRequestInterceptor;
import org.omg.Security.AccessId;
import org.omg.Security.AttributeType;
import org.omg.Security.ExtensibleFamily;
import org.slf4j.Logger;

/**
 * @author Nicolas Noffke
 */

public class ServerInvocationInterceptor
    extends org.omg.CORBA.LocalObject
    implements ServerRequestInterceptor, Configurable
{
    public static final String DEFAULT_NAME = "ServerInvocationInterceptor";

    private String name = null;

    private AttributeType type = null;

    private HashMap sessionCredentials = new HashMap();

    private Logger logger;
    private int serverSupportedOptions = 0;
    private int serverRequiredOptions = 0;

    public ServerInvocationInterceptor(org.omg.SecurityLevel2.Current current,
                                       org.jacorb.orb.ORB orb)
        throws ConfigurationException
    {
        this.name = DEFAULT_NAME;

        type =
            new AttributeType( new ExtensibleFamily( (short)0, (short)1 ), AccessId.value );
        configure(orb.getConfiguration());
    }


    public void configure(Configuration configuration)
        throws ConfigurationException
    {
        logger = configuration.getLogger("jacorb.security.ssl.interceptor");

        serverSupportedOptions = configuration.getAttributeAsInteger("jacorb.security.ssl.server.supported_options", 0x20, 16); // 16 is the base as we take the string value as hex!

        serverRequiredOptions = configuration.getAttributeAsInteger("jacorb.security.ssl.server.required_options", 0, 16);
    }


    public String name()
    {
        return name;
    }

    public void destroy()
    {
    }

    public void receive_request( ServerRequestInfo ri )
        throws ForwardRequest
    {
    }


    public void receive_request_service_contexts( ServerRequestInfo ri )
        throws ForwardRequest
    {
        ServerRequest request = ((ServerRequestInfoImpl) ri).request;
        GIOPConnection connection = null;

        /**
         * If this is a loopback request there may be no request. Handling
         * of local server objects with interceptors is now done locally
         * rather than via the remote mechanism.
         */
        if (request != null)
        {
           connection = request.getConnection();
        }

        // lookup for context
        if (connection == null)
        {
            if (logger.isErrorEnabled())
                logger.error("target has no connection!");
            return;
        }

        if( !connection.isSSL() )
        {
            return;
        }

        if (! (connection.getTransport() instanceof ServerIIOPConnection))
        {
            return;
        }

        ServerIIOPConnection transport =
            (ServerIIOPConnection)connection.getTransport();

        SSLSocket sslSocket = (SSLSocket)transport.getSocket();

        javax.net.ssl.SSLSession session = sslSocket.getSession();

        if (sessionCredentials.containsKey(session))
        {
            if (logger.isDebugEnabled())
                logger.info("Reusing SSL session credentials." );
            return;
        }

        CertificateFactory certificateFactory = null;

        try
        {
            certificateFactory = CertificateFactory.getInstance("X.509");
        }
        catch( Exception e )
        {
            if (logger.isWarnEnabled())
            {
                logger.warn(e.getMessage());
            }
        }

        try
        {
            javax.security.cert.X509Certificate[] certs =
                sslSocket.getSession().getPeerCertificateChain();

            int size = certs.length;
            java.security.cert.X509Certificate[] newCerts =
                new java.security.cert.X509Certificate[size];

            for( int i = size - 1; 0 <= i; i-- )
            {
                newCerts[i] = (java.security.cert.X509Certificate)
                    certificateFactory.generateCertificate( new ByteArrayInputStream( certs[i].getEncoded()));
            }

        }
        catch( Exception e )
        {
            if (logger.isWarnEnabled())
                logger.warn("Exception " + e.getMessage() +
                            " in ServerInvocationInterceptor");

            if ( (serverRequiredOptions & 0x40) != 0)
            {
                throw new org.omg.CORBA.NO_PERMISSION("Establish trust in client required, but failed");
            }
            return;
        }
    }

    public void send_reply( ServerRequestInfo ri )
    {
        removeAttribute();
    }

    public void send_exception( ServerRequestInfo ri )
        throws ForwardRequest
    {
        removeAttribute();
    }

    public void send_other( ServerRequestInfo ri )
        throws ForwardRequest
    {
        removeAttribute();
    }

    private void removeAttribute()
    {

    }
}
