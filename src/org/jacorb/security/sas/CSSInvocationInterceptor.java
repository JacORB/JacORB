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
import java.util.*;
import org.ietf.jgss.*;
import org.omg.Security.*;

import org.omg.PortableInterceptor.*;
import org.omg.CORBA.ORBPackage.*;
import org.omg.CORBA.Any;

import org.jacorb.util.*;
import org.jacorb.orb.portableInterceptor.ClientRequestInfoImpl;
import org.jacorb.orb.*;

import org.omg.IOP.*;
import org.omg.CSI.*;
import org.omg.CSIIOP.*;

/**
 * This is the SAS Client Security Service (CSS) Interceptor
 *
 * @author David Robison
 * @version $Id$
 */

public class CSSInvocationInterceptor
    extends org.omg.CORBA.LocalObject
    implements ClientRequestInterceptor
{
    public static final String DEFAULT_NAME = "CSSInvocationInterceptor";
    public static final int SecurityAttributeService = 15;
    private static byte[] contextToken;

    private Codec codec = null;
    private String name = null;

    public CSSInvocationInterceptor(Codec codec)
    {
        this.codec = codec;
        name = DEFAULT_NAME;
    }

    public String name()
    {
        return name;
    }

    public void destroy()
    {
    }

    public static void setInitialContext(byte[] token) {
        contextToken = token;
    }

    public void send_request(org.omg.PortableInterceptor.ClientRequestInfo ri) throws org.omg.PortableInterceptor.ForwardRequest
    {
        // see if target requires protected requests by looking into the IOR
        TaggedComponent tc = ri.get_effective_component(TAG_CSI_SEC_MECH_LIST.value);
        if (tc == null) return;

        // send the security context
        org.omg.CORBA.ORB orb = ((ClientRequestInfoImpl) ri).orb;
        try
        {
            IdentityToken identityToken = new IdentityToken();
            identityToken.absent(true);
            EstablishContext establishContext = new EstablishContext(0, new AuthorizationElement[0], identityToken, contextToken);
            org.omg.CSI.SASContextBody contextBody = new org.omg.CSI.SASContextBody();
            contextBody.establish_msg(establishContext);
            Any ctx_any = orb.create_any();
            SASContextBodyHelper.insert( ctx_any, contextBody );
            ri.add_request_service_context(new ServiceContext(SecurityAttributeService, codec.encode( ctx_any ) ), true);
        }
        catch (Exception e)
        {
            Debug.output(1, "Could not set security service context: " + e);
        }
    }

    public void send_poll(org.omg.PortableInterceptor.ClientRequestInfo ri)
    {
        //System.out.println("send_poll");
    }

    public void receive_reply(org.omg.PortableInterceptor.ClientRequestInfo ri)
    {
        //System.out.println("receive_reply");
    }

    public void receive_exception(org.omg.PortableInterceptor.ClientRequestInfo ri)
        throws org.omg.PortableInterceptor.ForwardRequest
    {
        //System.out.println("receive_exception");
    }

    public void receive_other(org.omg.PortableInterceptor.ClientRequestInfo ri)
        throws org.omg.PortableInterceptor.ForwardRequest
    {
        //System.out.println("receive_other");
    }

}







