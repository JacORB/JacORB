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

import org.omg.PortableInterceptor.*;
import org.omg.CORBA.ORBPackage.*;
import org.omg.CORBA.Any;

import org.jacorb.util.*;
import org.jacorb.orb.portableInterceptor.ServerRequestInfoImpl;
import org.omg.IOP.*;

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

    private String name = null;
    private Codec codec = null;
    private int slotID = -1;


    public TSSInvocationInterceptor(Codec codec, int slotID)
    {
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
        }
        catch (Exception e)
        {
            Debug.output(1, "Error parsing service context: " + e);
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







