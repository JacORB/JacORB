package org.jacorb.security.sas;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 2000-2002 Nicolas Noffke, Gerald Brose.
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
import org.ietf.jgss.*;

import org.omg.PortableInterceptor.*;
import org.omg.CORBA.ORBPackage.*;
import org.omg.CORBA.Any;

import org.jacorb.util.*;
import org.jacorb.orb.portableInterceptor.ServerRequestInfoImpl;
import org.omg.IOP.*;
import org.omg.GIOP.*;
import org.omg.CSI.*;
import org.jacorb.orb.connection.*;
import org.jacorb.orb.dsi.ServerRequest;

/**
 * This is the SAS Target Security Service (TSS) Interceptor
 *
 * @author David Robison
 * @version $Id$
 */

public class TSSInvocationInterceptor
    extends org.omg.CORBA.LocalObject
    implements ServerRequestInterceptor
{
    private static final String DEFAULT_NAME = "TSSInvocationInterceptor";
    private static final int SecurityAttributeService = 15;

    private static GSSCredential myCredential = null;

    private String name = null;
    private org.jacorb.orb.ORB orb = null;
    private Codec codec = null;
    private int sourceNameSlotID = -1;
    private int sasReplySlotID = -1;


    public TSSInvocationInterceptor(org.jacorb.orb.ORB orb, Codec codec, int sourceNameSlotID, int sasReplySlotID)
    {
        this.orb = orb;
        this.codec = codec;
        this.sourceNameSlotID = sourceNameSlotID;
        this.sasReplySlotID = sasReplySlotID;
        name = DEFAULT_NAME;
    }

    public String name()
    {
        return name;
    }

    public void destroy()
    {
    }

    public static void setMyCredential(GSSCredential cred) {
        myCredential = cred;
    }

    public void receive_request( ServerRequestInfo ri )
        throws ForwardRequest
    {
        System.out.println("receive_request");
    }


    public void receive_request_service_contexts( ServerRequestInfo ri )
        throws ForwardRequest
    {
        System.out.println("receive_request_service_contexts");
        org.omg.CSI.SASContextBody contextBody = null;
        try
        {
            // parse service context
            GSSManager gssManager = TSSInitializer.gssManager;
            ServiceContext ctx = ri.get_request_service_context(SecurityAttributeService);
            Any ctx_any = codec.decode( ctx.context_data );
            contextBody = org.omg.CSI.SASContextBodyHelper.extract(ctx_any);

            // get GSS Context and credentials
            Oid myMechOid = myCredential.getMechs()[0];
            GSSContext context = gssManager.createContext(myCredential);
            context.acceptSecContext(contextBody.establish_msg().client_authentication_token, 0, contextBody.establish_msg().client_authentication_token.length);
            GSSName sourceName = context.getSrcName();

            Any source_any = orb.create_any();
            source_any.insert_string(sourceName.toString());
            ri.set_slot( sourceNameSlotID, source_any);
            ri.set_slot( sasReplySlotID, makeCompleteEstablishContext(contextBody.establish_msg().client_context_id, false));
        }
        catch (GSSException e)
        {
            Debug.output(1, "Error parsing service context: " + e+": "+e.getMajorString()+": "+e.getMinorString());
            try { ri.set_slot( sasReplySlotID, makeContextError(contextBody.establish_msg().client_context_id, e.getMajor(), e.getMinor(), contextBody.establish_msg().client_authentication_token)); } catch (Exception ee) {}
            throw new org.omg.CORBA.NO_PERMISSION("Error parsing service context");
        }
        catch (Exception e)
        {
            Debug.output(1, "Error parsing service context: " + e);
            try { ri.set_slot( sasReplySlotID, makeContextError(contextBody.establish_msg().client_context_id, 1, 1, contextBody.establish_msg().client_authentication_token)); } catch (Exception ee) {}
            throw new org.omg.CORBA.NO_PERMISSION("Error parsing service context");
        }
    }

    public void send_reply( ServerRequestInfo ri )
    {
        System.out.println("send_reply");
        try
        {
            ri.add_reply_service_context(new ServiceContext(SecurityAttributeService, codec.encode( ri.get_slot(sasReplySlotID) ) ), true);
        }
        catch (Exception e)
        {
            Debug.output(1, "Error setting reply service context:" + e);
        }
    }

    public void send_exception( ServerRequestInfo ri )
        throws ForwardRequest
    {
        System.out.println("send_exception");
        try
        {
            ri.add_reply_service_context(new ServiceContext(SecurityAttributeService, codec.encode( ri.get_slot(sasReplySlotID) ) ), true);
        }
        catch (Exception e)
        {
            Debug.output(1, "Error setting reply service context:" + e);
        }
    }

    public void send_other( ServerRequestInfo ri )
        throws ForwardRequest
    {
        System.out.println("send_other");
    }

    private Any makeCompleteEstablishContext(long client_context_id, boolean context_stateful) {
        CompleteEstablishContext msg = new CompleteEstablishContext();
        msg.client_context_id = client_context_id;
        msg.context_stateful = context_stateful;
        msg.final_context_token = new byte[0];
        Any any = orb.create_any();
        CompleteEstablishContextHelper.insert(any, msg);
        return any;
    }

    private Any makeContextError(long client_context_id, int major_status, int minor_status, byte[] error_token) {
        ContextError msg = new ContextError();
        msg.client_context_id = client_context_id;
        msg.error_token = error_token;
        msg.major_status = major_status;
        msg.minor_status = minor_status;
        Any any = orb.create_any();
        ContextErrorHelper.insert(any, msg);
        return any;
    }
}
