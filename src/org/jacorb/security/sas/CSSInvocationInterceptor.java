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
import org.omg.CORBA.*;

import org.jacorb.util.*;
import org.jacorb.orb.portableInterceptor.ClientRequestInfoImpl;
import org.jacorb.orb.*;
import org.jacorb.orb.connection.ClientConnection;

import org.omg.IOP.*;
import org.omg.CSI.*;
import org.omg.CSIIOP.*;
import org.omg.ATLAS.*;

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
    private static final String DEFAULT_NAME = "CSSInvocationInterceptor";
    private static final int SecurityAttributeService = 15;
    private static GSSCredential myCredential;

    private Codec codec = null;
    private String name = null;

    private Hashtable atlasCache = new Hashtable();

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

    public static void setMyCredential(GSSCredential cred) {
        myCredential = cred;
    }

    public void send_request(org.omg.PortableInterceptor.ClientRequestInfo ri) throws org.omg.PortableInterceptor.ForwardRequest
    {
        org.omg.CORBA.ORB orb = ((ClientRequestInfoImpl) ri).orb;

        // see if target requires protected requests by looking into the IOR
        CompoundSecMechList csmList = null;
        try
        {
            TaggedComponent tc = ri.get_effective_component(TAG_CSI_SEC_MECH_LIST.value);
            CDRInputStream is = new CDRInputStream( (org.omg.CORBA.ORB)null, tc.component_data);
            is.openEncapsulatedArray();
            csmList = CompoundSecMechListHelper.read( is );
        }
        catch (Exception e)
        {
            Debug.output(2, "Did not find tagged component TAG_CSI_SEC_MECH_LIST");
        }
        if (csmList == null) return;

        // generate the context token
        byte[] contextToken = null;
        try
        {
            GSSManager gssManager = CSSInitializer.gssManager;
            Oid myMechOid = myCredential.getMechs()[0];
            GSSName myPeer = gssManager.createName("".getBytes(), GSSName.NT_ANONYMOUS, myMechOid);
            GSSContext myContext = gssManager.createContext(myPeer, myMechOid, myCredential, GSSContext.DEFAULT_LIFETIME);
            contextToken = new byte[0];
            while (!myContext.isEstablished()) contextToken = myContext.initSecContext(contextToken, 0, contextToken.length);
        }
        catch (Exception e)
        {
            Debug.output(1, "SAS Could not generate context token: " + e);
            throw new org.omg.CORBA.NO_PERMISSION("SAS Could not generate context token: " + e, MinorCodes.SAS_CSS_FAILURE, CompletionStatus.COMPLETED_NO);
        }

        // ask connection for client_context_id
        ClientConnection connection = ((ClientRequestInfoImpl) ri).connection;
        long client_context_id = connection.cacheSASContext(contextToken);
        if (client_context_id < 0) Debug.output(1, "New SAS Context: " + (-client_context_id));

        // get ATLAS tokens
        AuthorizationElement[] authorizationList = getATLASTokens(orb, csmList);
System.out.println("Authorized list size = " + authorizationList.length);

        // establish the security context
        try
        {
            Any msg = null;
            if (client_context_id <= 0)
            {
                IdentityToken identityToken = new IdentityToken();
                identityToken.absent(true);
                msg = makeEstablishContext(orb, -client_context_id, authorizationList, identityToken, contextToken);
            }
            else
            {
                msg = makeMessageInContext(orb, client_context_id, false);
            }
            ri.add_request_service_context(new ServiceContext(SecurityAttributeService, codec.encode( msg ) ), true);
        }
        catch (Exception e)
        {
            Debug.output(1, "Could not set security service context: " + e);
            e.printStackTrace();
        }
    }

    public void send_poll(org.omg.PortableInterceptor.ClientRequestInfo ri)
    {
        //System.out.println("send_poll");
    }

    public void receive_reply(org.omg.PortableInterceptor.ClientRequestInfo ri)
    {
        //System.out.println("receive_reply");

        // get SAS message
        SASContextBody contextBody = null;
        ServiceContext ctx = null;
        try
        {
            ctx = ri.get_request_service_context(SecurityAttributeService);
        }
        catch (Exception e)
        {
        }
        if (ctx == null) return;
        try
        {
            Any msg = codec.decode( ctx.context_data );
            contextBody = SASContextBodyHelper.extract(msg);
        }
        catch (Exception e)
        {
            Debug.output(1, "Could not parse SAS reply: " + e);
        }
        ClientConnection connection = ((ClientRequestInfoImpl) ri).connection;

        // process CompleteEstablishContext message
        if (contextBody.discriminator() == MTCompleteEstablishContext.value) {
            CompleteEstablishContext reply = contextBody.complete_msg();

            // if not stateful, remove from connection
            if (reply.client_context_id > 0 && !reply.context_stateful) connection.purgeSASContext(reply.client_context_id);
        }

        // process ContextError message
        if (contextBody.discriminator() == MTContextError.value) {
            ContextError reply = contextBody.error_msg();

            // if stateful, remove from connection
            if (reply.client_context_id > 0) connection.purgeSASContext(reply.client_context_id);
        }
    }

    public void receive_exception(org.omg.PortableInterceptor.ClientRequestInfo ri)
        throws org.omg.PortableInterceptor.ForwardRequest
    {
        //System.out.println("receive_exception");

        // get SAS message
        SASContextBody contextBody = null;
        ServiceContext ctx = null;
        try
        {
            ctx = ri.get_request_service_context(SecurityAttributeService);
        }
        catch (Exception e)
        {
        }
        if (ctx == null) return;
        try
        {
            Any msg = codec.decode( ctx.context_data );
            contextBody = SASContextBodyHelper.extract(msg);
        }
        catch (Exception e)
        {
            Debug.output(1, "Could not parse SAS reply: " + e);
        }
        ClientConnection connection = ((ClientRequestInfoImpl) ri).connection;

        // process CompleteEstablishContext message
        if (contextBody.discriminator() == MTCompleteEstablishContext.value) {
            CompleteEstablishContext reply = contextBody.complete_msg();

            // if not stateful, remove from connection
            if (reply.client_context_id > 0 && !reply.context_stateful) connection.purgeSASContext(reply.client_context_id);
        }

        // process ContextError message
        if (contextBody.discriminator() == MTContextError.value) {
            ContextError reply = contextBody.error_msg();

            // if stateful, remove from connection
            if (reply.client_context_id > 0) connection.purgeSASContext(reply.client_context_id);
        }
    }

    public void receive_other(org.omg.PortableInterceptor.ClientRequestInfo ri)
        throws org.omg.PortableInterceptor.ForwardRequest
    {
        //System.out.println("receive_other");
    }

    private Any makeEstablishContext(org.omg.CORBA.ORB orb, long client_context_id, AuthorizationElement[] authorization_token, IdentityToken identity_token, byte[] client_authentication_token)
    {
        EstablishContext msg = new EstablishContext();
        msg.client_context_id = client_context_id;
        msg.client_authentication_token = client_authentication_token;
        msg.identity_token = identity_token;
        msg.authorization_token = authorization_token;
        SASContextBody contextBody = new SASContextBody();
        contextBody.establish_msg(msg);
        Any any = orb.create_any();
        SASContextBodyHelper.insert( any, contextBody );
        return any;
    }

    private Any makeMessageInContext(org.omg.CORBA.ORB orb, long client_context_id, boolean discard_context)
    {
        MessageInContext msg = new MessageInContext();
        msg.client_context_id = client_context_id;
        msg.discard_context = discard_context;
        SASContextBody contextBody = new SASContextBody();
        contextBody.in_context_msg(msg);
        Any any = orb.create_any();
        SASContextBodyHelper.insert( any, contextBody );
        return any;
    }

    private AuthorizationElement[] getATLASTokens(org.omg.CORBA.ORB orb, CompoundSecMechList csmList) throws org.omg.CORBA.NO_PERMISSION
    {
        // find the ATLAS profile in the IOR
        ATLASProfile atlasProfile = null;
        try
        {
            //Any any = orb.create_any();
            //any = codec.decode(tc.component_data);
            //CompoundSecMechList compoundSecMechList = CompoundSecMechListHelper.extract(any);
            //CDRInputStream is = new CDRInputStream( orb, tc.component_data);
            //is.openEncapsulatedArray();
            //CompoundSecMechList compoundSecMechList = CompoundSecMechListHelper.read( is );
            ServiceConfiguration authorities[] = csmList.mechanism_list[0].sas_context_mech.privilege_authorities;
            for (int i = 0; i < authorities.length; i++)
            {
                if (authorities[i].syntax != SCS_ATLAS.value) continue;
                Any any = codec.decode(authorities[i].name);
                atlasProfile = ATLASProfileHelper.extract(any);
            }
        }
        catch (Exception e)
        {
            Debug.output(1, "Error parsing ATLAS from IOR: " + e);
            throw new org.omg.CORBA.NO_PERMISSION();
        }
        if (atlasProfile == null) return new AuthorizationElement[0];
        String cacheID = new String(atlasProfile.the_cache_id);
        String locator = atlasProfile.the_locator.the_url();

        // see if the tokens are in the ATLAS cache
        synchronized (atlasCache)
        {
            if (atlasCache.containsKey(cacheID))
            {
System.out.println("3 found cached tokens");
                return ((AuthTokenData)atlasCache.get(cacheID)).auth_token;
            }
        }

        // contact the ATLAS server and get the credentials
        AuthTokenDispenser dispenser = null;
        try {
            org.omg.CORBA.Object obj = orb.string_to_object(locator);
            dispenser = AuthTokenDispenserHelper.narrow(obj);
        }
        catch (Exception e)
        {
            Debug.output(1, "Could not find ATLAS server " + locator + ": " + e);
            throw new org.omg.CORBA.NO_PERMISSION();
        }
        if (dispenser == null)
        {
            Debug.output(1, "Could not find ATLAS server " + locator);
            throw new org.omg.CORBA.NO_PERMISSION();
        }

        AuthTokenData data = null;
        try
        {
            data = dispenser.get_my_authorization_token();
        }
        catch (Exception e)
        {
            Debug.output(1, "error getting ATLAS tokens from server " + locator + ": " + e);
            throw new org.omg.CORBA.NO_PERMISSION();
        }
        synchronized (atlasCache)
        {
            atlasCache.put(cacheID, data);
        }
        return data.auth_token;
    }

}







