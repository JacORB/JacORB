package org.jacorb.security.ssl;

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
import org.jacorb.orb.connection.ServerConnection;

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
    private org.jacorb.security.level2.CurrentImpl current = null;
    private SecAttributeManager attrib_mgr;
    private AttributeType type; 

    public ServerInvocationInterceptor(org.omg.SecurityLevel2.Current current)
    {
        this.current = (CurrentImpl) current;

        attrib_mgr = SecAttributeManager.getInstance();

        type = new AttributeType
            ( new ExtensibleFamily( (short) 0,
                                    (short) 1 ),
              AccessId.value );   
    }

    public String name()
    {
        return "ServerInvocationInterceptor";
    }

    public void receive_request( ServerRequestInfo ri )
        throws ForwardRequest
    {
        Debug.output( 3, "receive_request_service_contexts!");
    }


    /**
     * @throws CORBA::NO_PERMISSION, if security policy violated
     */

    public void receive_request_service_contexts( ServerRequestInfo ri )
        throws ForwardRequest
    {
        Debug.output( 3, "receive_request!");

        ServerRequest request = ((ServerRequestInfoImpl) ri).request;

        ServerConnection connection = request.getConnection();

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

        SSLSocket sslSocket = (SSLSocket) connection.getSocket();
            


        KeyAndCert kac = new KeyAndCert( null, 
                                         sslSocket.getPeerCertificateChain() );

        SecAttribute [] atts = new SecAttribute[] {
            attrib_mgr.createAttribute( kac, type ) } ;

        current.set_received_credentials( new ReceivedCredentialsImpl( atts ) );
 
    }

    public void send_reply( ServerRequestInfo ri )
    {
        current.remove_received_credentials();

        Debug.output( 3, "send_reply!");
    }

    public void send_exception( ServerRequestInfo ri )
        throws ForwardRequest
    {
        current.remove_received_credentials();

        Debug.output( 3, "send_exception!");
    }

    public void send_other( ServerRequestInfo ri )
        throws ForwardRequest
    {
        current.remove_received_credentials();

        Debug.output( 3, "send_other!");
    }
}

