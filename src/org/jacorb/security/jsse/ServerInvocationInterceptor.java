package org.jacorb.security.jsse;

import java.io.*;
import javax.net.ssl.*;

import org.omg.Security.*;
import org.omg.SecurityLevel2.*;
import org.omg.PortableInterceptor.*;

import org.jacorb.util.*;
import org.jacorb.security.level2.*;
import org.jacorb.orb.portableInterceptor.*;
import org.jacorb.orb.Connection;
import org.jacorb.orb.LocalityConstrainedObject;
import org.jacorb.orb.dsi.ServerRequest;

/**
 * @author Nicolas Noffke
 * $Id$
 */

public class ServerInvocationInterceptor
    extends LocalityConstrainedObject 
    implements ServerRequestInterceptor
{
    private CurrentImpl current = null;
    private SecAttributeManager attrib_mgr;
    private AttributeType type; 

    public ServerInvocationInterceptor( org.omg.SecurityLevel2.Current current )
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
        org.jacorb.util.Debug.output( 3, "receive_request_service_contexts!");
    }


    /**
     * @throws CORBA::NO_PERMISSION, if security policy violated
     */

    public void receive_request_service_contexts( ServerRequestInfo ri )
        throws ForwardRequest
    {
        org.jacorb.util.Debug.output( 3, "receive_request!");

        ServerRequest request = ((ServerRequestInfoImpl) ri).request;

        Connection connection = request.getConnection();


        if (connection == null)
        {
            org.jacorb.util.Debug.output( 3, "target has no connection!");
            return;
        }

        if( !connection.isSSL() )
        {
            return;
        }

        SSLSocket sslSocket =  (SSLSocket) connection.getSocket();
        try
        {
        SecAttribute [] atts = new SecAttribute[] {
            attrib_mgr.createAttribute( sslSocket.getSession().getPeerCertificateChain(),
                                        type ) } ;

            current.set_received_credentials( new ReceivedCredentialsImpl( atts ) );
        }
        catch( SSLPeerUnverifiedException e )
        {
            Debug.output( 2, e );
        }
    }

    public void send_reply( ServerRequestInfo ri )
    {
        org.jacorb.util.Debug.output( 3, "send_reply!");
    }

    public void send_exception( ServerRequestInfo ri )
        throws ForwardRequest
    {
        org.jacorb.util.Debug.output( 3, "send_exception!");
    }

    public void send_other( ServerRequestInfo ri )
        throws ForwardRequest
    {
        org.jacorb.util.Debug.output( 3, "send_other!");
    }

    public void receive_other(ClientRequestInfo ri) 
        throws ForwardRequest
    {
        org.jacorb.util.Debug.output( 3, "receive_other!");
    }
}






