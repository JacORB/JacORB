package org.jacorb.proxy;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2002  Gerald Brose.
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

import org.omg.PortableInterceptor.*;
import org.omg.CosNaming.*;
import org.omg.CORBA.Any;
import org.jacorb.util.Environment;
import org.omg.IOP.ServiceContext;
import org.omg.IOP.Codec;
import org.jacorb.orb.ContextID;

/**
 * This appligator server interceptor retrieves the original
 * target as a service context and makes it available to the
 * main proxy implementation class via a reqest slot entry.
 *
 * @author Nicolas Noffke, Sebastian Müller, Steve Osselton
 */

public class ProxyServerForwardInterceptor
    extends org.omg.CORBA.LocalObject
    implements ServerRequestInterceptor
{
    private org.jacorb.orb.ORB orb = null;
    private Codec codec = null;
    private org.jacorb.proxy.Proxy proxy = null;
    public static int slot = -1;

    public ProxyServerForwardInterceptor (ORBInitInfo info, Codec codec, int sl)
    {
        this.codec = codec;
        slot = sl;
        orb = ((org.jacorb.orb.portableInterceptor.ORBInitInfoImpl) info).getORB ();

        String url = Environment.getProperty ("jacorb.ProxyServerURL");
        org.omg.CORBA.Object obj = orb.string_to_object (url);
        proxy = org.jacorb.proxy.ProxyHelper.narrow (obj);
    }

    public void receive_request_service_contexts (ServerRequestInfo ri)
        throws ForwardRequest
    {
        ServiceContext context;
        org.omg.CORBA.Any any;
        
        try
        {
            context = ri.get_request_service_context (ContextID.SERVICE_PROXY_CONTEXT);
            any = codec.decode (context.context_data);
            ri.set_slot (slot, any);
        }
        catch (Exception e)
        {
            e.printStackTrace ();
        }
    }

    public String name ()
    {
        return "JacORB.ProxyServerForwardInterceptor";
    }

    public void destroy ()
    {
        orb = null;
        codec = null;
        proxy = null;
    }

    public void receive_request (ServerRequestInfo ri)
        throws ForwardRequest
    {
    }

    public void send_reply (ServerRequestInfo ri)
    {
    }

    public void send_exception (ServerRequestInfo ri)
        throws ForwardRequest
    {
    }

    public void send_other (ServerRequestInfo ri)
        throws ForwardRequest
    {
    }
}
