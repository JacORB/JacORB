package org.jacorb.security.ssl.iaik;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 2000  Benvenuti André.
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
import org.omg.SecurityReplaceable.*;
import org.omg.Security.*;

import org.omg.PortableInterceptor.*;
import org.omg.CORBA.ORBPackage.*;
import org.omg.CORBA.Any;

import org.jacorb.util.*;
import org.jacorb.orb.portableInterceptor.ServerRequestInfoImpl;
import org.jacorb.security.level2.*;
import org.jacorb.orb.dsi.ServerRequest;
import org.jacorb.orb.connection.*;

import iaik.security.ssl.SSLSocket;

/**
 *
 * 
 * @author Nicolas Noffke
 * $Id$
 */

public class ServerInvocationInterceptor
    extends org.jacorb.orb.LocalityConstrainedObject 
    implements ServerRequestInterceptor
{
    public static final String DEFAULT_NAME = "ServerInvocationInterceptor";

    private String name = null;

    private org.jacorb.security.level2.CurrentImpl current = null;
    private SecAttributeManager attrib_mgr = null;
    private AttributeType type = null; 
    
    public ServerInvocationInterceptor(org.omg.SecurityLevel2.Current current)
    {
        this( current, DEFAULT_NAME );
    }

    public ServerInvocationInterceptor( org.omg.SecurityLevel2.Current current,
                                        String name )
    {
        this.current = (CurrentImpl) current;
        this.name = name;

        attrib_mgr = SecAttributeManager.getInstance();

        type = new AttributeType
            ( new ExtensibleFamily( (short) 0,
                                    (short) 1 ),
              AccessId.value );   
    }

    public String name()
    {
        return name;
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
            Debug.output( 3, "target has no connection!");
            return;
        }
        
        if( !connection.isSSL() )
        {
            return;
        }
            
        Server_TCP_IP_Transport transport =
            (Server_TCP_IP_Transport) connection.getTransport();
        
        SSLSocket sslSocket = (SSLSocket) transport.getSocket();
        
        KeyAndCert kac = new KeyAndCert( null, 
                                         sslSocket.getPeerCertificateChain() );
        
        if( kac.chain == null )
        {
            Debug.output( 2, "Client sent no certificate chain!" );
            
            return;
        }
        
        SecAttribute [] atts = new SecAttribute[] {
            attrib_mgr.createAttribute( kac, type ) } ;
        
        current.set_received_credentials( new ReceivedCredentialsImpl( atts ) );
    }

    public void send_reply( ServerRequestInfo ri )
    {
        current.remove_received_credentials();
    }

    public void send_exception( ServerRequestInfo ri )
        throws ForwardRequest
    {
        current.remove_received_credentials();
    }

    public void send_other( ServerRequestInfo ri )
        throws ForwardRequest
    {
        current.remove_received_credentials();
    }
}







