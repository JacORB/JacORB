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

 //DRR//
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
import org.jacorb.orb.connection.*;
import org.jacorb.orb.dsi.ServerRequest;

import javax.net.ssl.SSLSocket;

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
    public static final String DEFAULT_NAME = "TSSInvocationInterceptor";
    public static final int SecurityAttributeService = 15;
    private static GSSManager gssManager = null;

    private String name = null;
    private org.jacorb.orb.ORB orb = null;
    private Codec codec = null;
    private int slotID = -1;


    public TSSInvocationInterceptor(org.jacorb.orb.ORB orb, Codec codec, int slotID)
    {
        this.orb = orb;
        this.codec = codec;
        this.slotID = slotID;
        name = DEFAULT_NAME;
    }

    public String name()
    {
        return name;
    }

    public void destroy()
    {
    }

    public static void setGSSManager(GSSManager manager) {
        gssManager = manager;
    }

    public void receive_request( ServerRequestInfo ri )
        throws ForwardRequest
    {
        //System.out.println("receive_request");
    }


    public void receive_request_service_contexts( ServerRequestInfo ri )
        throws ForwardRequest
    {
        //System.out.println("receive_request_service_contexts");
        try
        {
            ServiceContext ctx = ri.get_request_service_context(SecurityAttributeService);
            Any ctx_any = codec.decode( ctx.context_data );
            ri.set_slot( slotID, ctx_any);
            org.omg.CSI.SASContextBody contextBody = org.omg.CSI.SASContextBodyHelper.extract(ctx_any);
            //org.omg.CORBA.portable..OutputStream outStream = new java.io.OutputStream();
            //byte[] b = new byte[contextBody.establish_msg().client_authentication_token.length-2];
            //System.arraycopy(contextBody.establish_msg().client_authentication_token, 2, b, 0, b.length);
            //GSSContext c = gssManager.createContext(contextBody.establish_msg().client_authentication_token);
            //System.out.println("HEHE");
            //GSSContext c1 = gssManager.createContext(contextBody.establish_msg().client_authentication_token);
            //outStream.write(contextBody.establish_msg().client_authentication_token);
            //c.acceptSecContext(contextBody.establish_msg().client_authentication_token, 0, contextBody.establish_msg().client_authentication_token.length);
            //InputStream inStream = new ByteArrayInputStream(contextBody.establish_msg().client_authentication_token);
            //org.omg.CORBA.portable.InputStream inStream = outStream.create_input_stream();
            //Oid mechOid2 = new Oid(inStream);

            Oid mechOid = new org.ietf.jgss.Oid(org.jacorb.util.Environment.getProperty("jacorb.security.sas.mechanism.1.oid"));
            GSSCredential cred = gssManager.createCredential(gssManager.createName("".getBytes(), null, mechOid), GSSCredential.DEFAULT_LIFETIME, mechOid, GSSCredential.ACCEPT_ONLY);
            GSSName peerName = gssManager.createName("".getBytes(), null, mechOid);
            GSSContext context = gssManager.createContext(peerName, mechOid, cred, GSSContext.DEFAULT_LIFETIME);
            byte[] b = context.acceptSecContext(contextBody.establish_msg().client_authentication_token, 0, contextBody.establish_msg().client_authentication_token.length);
for (int i=0;i<b.length;i++) System.out.println((int)b[i]+" ");System.out.println();
        }
        catch (GSSException e)
        {
            Debug.output(1, "Error parsing service context: " + e+": "+e.getMajorString()+": "+e.getMinorString());
            e.printStackTrace();
        }
        catch (Exception e)
        {
            Debug.output(1, "Error parsing service context: " + e);
            e.printStackTrace();
        }
    }

    public void send_reply( ServerRequestInfo ri )
    {
        //System.out.println("send_reply");
    }

    public void send_exception( ServerRequestInfo ri )
        throws ForwardRequest
    {
        //System.out.println("send_exception");
    }

    public void send_other( ServerRequestInfo ri )
        throws ForwardRequest
    {
        //System.out.println("send_other");
    }
}







