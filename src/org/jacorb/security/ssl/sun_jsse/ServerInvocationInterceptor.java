package org.jacorb.security.ssl.sun_jsse;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 2000-2004 Nicolas Noffke, Gerald Brose.
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

import java.io.*;
import java.util.*;
import java.security.cert.*;

import org.apache.avalon.framework.configuration.*;
import org.apache.avalon.framework.logger.Logger;

import org.omg.SecurityReplaceable.*;
import org.omg.Security.*;
import org.omg.SecurityLevel2.ReceivedCredentials;

import org.omg.PortableInterceptor.*;
import org.omg.CORBA.ORBPackage.*;
import org.omg.CORBA.Any;

import org.jacorb.orb.portableInterceptor.ServerRequestInfoImpl;
import org.jacorb.security.level2.*;
import org.jacorb.orb.dsi.ServerRequest;
import org.jacorb.orb.iiop.*;
import org.jacorb.orb.giop.*;


import javax.net.ssl.SSLSocket;

/**
 *
 * 
 * @author Nicolas Noffke
 * $Id$
 */

public class ServerInvocationInterceptor
    extends org.omg.CORBA.LocalObject 
    implements ServerRequestInterceptor, Configurable
{
    public static final String DEFAULT_NAME = "ServerInvocationInterceptor";

    private String name = null;

    private org.jacorb.security.level2.CurrentImpl current = null;
    private SecAttributeManager attrib_mgr = null;
    private AttributeType type = null; 

    private HashMap sessionCredentials = new HashMap();

    private Logger logger;
    private short serverSupportedOptions = 0;
    private short serverRequiredOptions = 0;

    public ServerInvocationInterceptor(org.omg.SecurityLevel2.Current current, 
                                       org.jacorb.orb.ORB orb)
        throws ConfigurationException
    {
        this.current = (CurrentImpl) current;
        this.name = DEFAULT_NAME;
        attrib_mgr = SecAttributeManager.getInstance();

        type = 
            new AttributeType( new ExtensibleFamily( (short)0, (short)1 ), AccessId.value );
        configure(orb.getConfiguration());
    }


    public void configure(Configuration configuration)
        throws ConfigurationException
    {
        logger = 
            ((org.jacorb.config.Configuration)configuration).getNamedLogger("jacorb.security.ssl.interceptor");

        serverSupportedOptions = 
            Short.parseShort(
                configuration.getAttribute("jacorb.security.ssl.server.supported_options","20"),
                16); // 16 is the base as we take the string value as hex!

        serverRequiredOptions = 
            Short.parseShort(
                configuration.getAttribute("jacorb.security.ssl.server.required_options","0"),
                16);
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
        GIOPConnection connection = request.getConnection();
        
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
            
        ServerIIOPConnection transport =
            (ServerIIOPConnection)connection.getTransport();
        
        SSLSocket sslSocket = (SSLSocket)transport.getSocket();

        javax.net.ssl.SSLSession session = sslSocket.getSession();

        if (sessionCredentials.containsKey(session))
        {
            ReceivedCredentialsImpl sessionRcvCredentials = 
                (ReceivedCredentialsImpl)sessionCredentials.get(session);
            current.set_received_credentials(sessionRcvCredentials);
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
        
        KeyAndCert kac = null;
        
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
            
            kac = new KeyAndCert( null, newCerts );
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

        if( kac.chain == null )
        {
            if (logger.isInfoEnabled())
                logger.info("Client sent no certificate chain!" );
            
            return;
        }
                
        SecAttribute [] atts = 
            new SecAttribute[]{attrib_mgr.createAttribute(kac, type)} ;
        
        current.set_received_credentials( new ReceivedCredentialsImpl( atts ) );
    }

    public void send_reply( ServerRequestInfo ri )
    {
        removeAttribute();
        current.remove_received_credentials();
    }

    public void send_exception( ServerRequestInfo ri )
        throws ForwardRequest
    {
        removeAttribute();
        current.remove_received_credentials();
    }

    public void send_other( ServerRequestInfo ri )
        throws ForwardRequest
    {
        removeAttribute();
        current.remove_received_credentials();
    }

    private void removeAttribute()
    {
        ReceivedCredentials creds = current.received_credentials();

        if (creds == null)
        {
            return;
        }

        SecAttribute[] attributes = creds.get_attributes(
            new AttributeType[]{ type } );

        if (attributes.length != 0)
        {
            attrib_mgr.removeAttribute(attributes[0]);
        }
    }
}
