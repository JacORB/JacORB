package org.jacorb.security.sas;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 2000-2003 Nicolas Noffke, Gerald Brose.
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

import java.net.URLDecoder;
import java.util.Hashtable;

import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.configuration.*;

import org.jacorb.orb.CDRInputStream;
import org.jacorb.orb.MinorCodes;
import org.jacorb.orb.giop.ClientConnection;
import org.jacorb.orb.portableInterceptor.ClientRequestInfoImpl;

import org.omg.ATLAS.*;
import org.omg.CORBA.Any;
import org.omg.CORBA.BAD_PARAM;
import org.omg.CORBA.CompletionStatus;
import org.omg.CSI.*;
import org.omg.CSIIOP.CompoundSecMechList;
import org.omg.CSIIOP.CompoundSecMechListHelper;
import org.omg.CSIIOP.ServiceConfiguration;
import org.omg.CSIIOP.TAG_CSI_SEC_MECH_LIST;
import org.omg.IOP.Codec;
import org.omg.IOP.ENCODING_CDR_ENCAPS;
import org.omg.IOP.Encoding;
import org.omg.IOP.ServiceContext;
import org.omg.IOP.TaggedComponent;
import org.omg.IOP.CodecFactoryPackage.UnknownEncoding;
import org.omg.PortableInterceptor.ClientRequestInterceptor;
import org.omg.PortableInterceptor.ORBInitInfo;

/**
 * This is the SAS Client Security Service (CSS) Interceptor
 *
 * @author David Robison
 * @version $Id$
 */

public class SASClientInterceptor
    extends org.omg.CORBA.LocalObject
    implements ClientRequestInterceptor, Configurable
{
    protected static final int SecurityAttributeService = 15;
    protected final String DEFAULT_NAME = "SASClientInterceptor";
    protected Codec codec = null;
    protected String name = null;

    private  Logger logger = null;

    protected byte[] contextToken = new byte[0];
    protected boolean useStateful = true;
    protected Hashtable atlasCache = new Hashtable();

    protected ISASContext sasContext = null;

    public SASClientInterceptor(ORBInitInfo info) 
        throws UnknownEncoding, ConfigurationException
    {
        name = DEFAULT_NAME;
        Encoding encoding = new Encoding(ENCODING_CDR_ENCAPS.value, (byte) 1, (byte) 0);
        codec = info.codec_factory().create_codec(encoding);

        org.jacorb.orb.ORB orb = 
            ((org.jacorb.orb.portableInterceptor.ORBInitInfoImpl)info).getORB ();
        configure( orb.getConfiguration());
    }

    public void configure(Configuration configuration)
        throws ConfigurationException
    {
        logger = 
            ((org.jacorb.config.Configuration)configuration).getNamedLogger("jacorb.security.sas.CSS");

        useStateful = 
            configuration.getAttribute("jacorb.security.sas.stateful","true").equals("true");

        String contextClass = null;
        try
        {
            configuration.getAttribute("jacorb.security.sas.contextClass");
            try 
            {
                Class c = 
                    org.jacorb.util.ObjectUtil.classForName(contextClass);
                sasContext = (ISASContext)c.newInstance();
            }
            catch (Exception e) 
            {
                if (logger.isErrorEnabled())
                    logger.error("Could not instantiate class " + contextClass + ": " + e);
            }
        }
        catch(ConfigurationException ce) 
        {
            if (logger.isDebugEnabled())
                logger.debug("ConfigurationException", ce);
        }

        if (sasContext == null) 
        {
            if (logger.isErrorEnabled())
                logger.error("Could not load SAS context class: "+contextClass);
        } 
        else 
        {
            sasContext.initClient();
        }
    }

    public void setContextToken(byte[] contextToken) {
        this.contextToken = contextToken;
    }

    public String name()
    {
        return name;
    }

    public void destroy()
    {
    }

    public void send_request(org.omg.PortableInterceptor.ClientRequestInfo ri)
        throws org.omg.PortableInterceptor.ForwardRequest
    {
        //if (ri.operation().equals("_is_a")) return;
        //if (ri.operation().equals("_non_existent")) return;
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
        catch (BAD_PARAM e)
        {
            if (logger.isDebugEnabled())
                logger.debug("Did not find tagged component TAG_CSI_SEC_MECH_LIST: "+
                             ri.operation());
        }
        catch (Exception e)
        {
            if (logger.isWarnEnabled())
                logger.warn("Did not find tagged component TAG_CSI_SEC_MECH_LIST: "+e);
        }
        if (csmList == null) 
            return;

        if (csmList.mechanism_list[0].as_context_mech.target_supports == 0 &&
            csmList.mechanism_list[0].as_context_mech.target_requires == 0 &&
            csmList.mechanism_list[0].sas_context_mech.target_supports == 0 &&
            csmList.mechanism_list[0].sas_context_mech.target_requires == 0)
            return;

        // ask connection for client_context_id
        ClientConnection connection = ((ClientRequestInfoImpl) ri).connection;

        long client_context_id = 0;
        if (useStateful) 
            client_context_id = connection.cacheSASContext("css".getBytes());
        if (client_context_id < 0) 
        {
            if (logger.isInfoEnabled())
                logger.info("New SAS Context: " + (-client_context_id));
        }

        // get ATLAS tokens
        AuthorizationElement[] authorizationList = getATLASTokens(orb, csmList);

        // establish the security context
        try
        {
            Any msg = null;
            if (client_context_id <= 0)
            {
                IdentityToken identityToken = new IdentityToken();
                identityToken.absent(true);
                contextToken = sasContext.createClientContext(ri, csmList);
                msg = makeEstablishContext(orb, 
                                           -client_context_id, 
                                           authorizationList, 
                                           identityToken, 
                                           contextToken);
            }
            else
            {
                msg = makeMessageInContext(orb, client_context_id, false);
            }
            ri.add_request_service_context(new ServiceContext(SecurityAttributeService, codec.encode_value( msg ) ), true);
        }
        catch (Exception e)
        {
            if (logger.isWarnEnabled())
                logger.warn("Could not set security service context: " + e);
            throw new org.omg.CORBA.NO_PERMISSION("SAS Could not set security service context: " + e, 
                                                  MinorCodes.SAS_CSS_FAILURE,
                                                  CompletionStatus.COMPLETED_NO);
        }
    }

    public void send_poll(org.omg.PortableInterceptor.ClientRequestInfo ri)
    {
    }

    public void receive_reply(org.omg.PortableInterceptor.ClientRequestInfo ri)
    {
        // get SAS message
        SASContextBody contextBody = null;
        ServiceContext ctx = null;
        try
        {
            ctx = ri.get_reply_service_context(SecurityAttributeService);
        }
        catch (BAD_PARAM e)
        {
            if (logger.isDebugEnabled())
                logger.debug("No SAS security context found: "+ri.operation());
        }
        catch (Exception e)
        {
            if (logger.isWarnEnabled())
                logger.warn("No SAS security context found: "+e);
        }
        if (ctx == null || ctx.context_data.length <= 1) return;

        try
        {
            Any msg = codec.decode_value( ctx.context_data, SASContextBodyHelper.type() );
            contextBody = SASContextBodyHelper.extract(msg);
        }
        catch (Exception e)
        {
            if (logger.isWarnEnabled())
                logger.warn("Could not parse SAS reply: " + e);e.printStackTrace();
            throw new org.omg.CORBA.NO_PERMISSION("SAS Could not parse SAS reply: " + e, 
                                                  MinorCodes.SAS_CSS_FAILURE, 
                                                  CompletionStatus.COMPLETED_MAYBE);
        }
        ClientConnection connection = ((ClientRequestInfoImpl) ri).connection;

        // process CompleteEstablishContext message
        if (contextBody.discriminator() == MTCompleteEstablishContext.value)
        {
            CompleteEstablishContext reply = contextBody.complete_msg();

            // if not stateful, remove from connection
            if (reply.client_context_id > 0 && !reply.context_stateful) 
                connection.purgeSASContext(reply.client_context_id);
        }

        // process ContextError message
        if (contextBody.discriminator() == MTContextError.value) 
        {
            ContextError reply = contextBody.error_msg();

            // if stateful, remove from connection
            if (reply.client_context_id > 0) 
                connection.purgeSASContext(reply.client_context_id);
        }
    }

    public void receive_exception(org.omg.PortableInterceptor.ClientRequestInfo ri)
        throws org.omg.PortableInterceptor.ForwardRequest
    {
        // get SAS message
        SASContextBody contextBody = null;
        ServiceContext ctx = null;
        try
        {
            ctx = ri.get_reply_service_context(SecurityAttributeService);
        }
        catch (BAD_PARAM e)
        {
            if (logger.isDebugEnabled())
                logger.debug("No SAS security context found (exception): "+ri.operation());
        }
        catch (Exception e)
        {
            if (logger.isWarnEnabled())
                logger.warn("No SAS security context found (exception): "+e);
        }
        if (ctx == null) return;

        try
        {
            Any msg = codec.decode_value( ctx.context_data, SASContextBodyHelper.type() );
            contextBody = SASContextBodyHelper.extract(msg);
        }
        catch (Exception e)
        {
            if (logger.isWarnEnabled())
                logger.warn("Could not parse SAS reply: " + e);
            throw new org.omg.CORBA.NO_PERMISSION("SAS Could not parse SAS reply: " + e, 
                                                  MinorCodes.SAS_CSS_FAILURE, 
                                                  CompletionStatus.COMPLETED_MAYBE);
        }
        ClientConnection connection = ((ClientRequestInfoImpl) ri).connection;

        // process CompleteEstablishContext message
        if (contextBody.discriminator() == MTCompleteEstablishContext.value) 
        {
            CompleteEstablishContext reply = contextBody.complete_msg();

            // if not stateful, remove from connection
            if (reply.client_context_id > 0 && !reply.context_stateful) 
                connection.purgeSASContext(reply.client_context_id);
        }

        // process ContextError message
        if (contextBody.discriminator() == MTContextError.value) 
        {
            ContextError reply = contextBody.error_msg();

            // if stateful, remove from connection
            if (reply.client_context_id > 0) 
                connection.purgeSASContext(reply.client_context_id);
        }
    }

    public void receive_other(org.omg.PortableInterceptor.ClientRequestInfo ri)
        throws org.omg.PortableInterceptor.ForwardRequest
    {
    }

    protected Any makeEstablishContext(org.omg.CORBA.ORB orb, 
                                       long client_context_id, 
                                       AuthorizationElement[] authorization_token, 
                                       IdentityToken identity_token, 
                                       byte[] client_authentication_token)
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

    protected Any makeMessageInContext(org.omg.CORBA.ORB orb, 
                                       long client_context_id, 
                                       boolean discard_context)
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

    protected AuthorizationElement[] getATLASTokens(org.omg.CORBA.ORB orb, 
                                                    CompoundSecMechList csmList) 
        throws org.omg.CORBA.NO_PERMISSION
    {
        // find the ATLAS profile in the IOR
        ATLASProfile atlasProfile = null;
        try
        {
            ServiceConfiguration authorities[] = 
                csmList.mechanism_list[0].sas_context_mech.privilege_authorities;
            for (int i = 0; i < authorities.length; i++)
            {
                if (authorities[i].syntax != SCS_ATLAS.value)
                    continue;
                Any any = codec.decode(authorities[i].name);
                atlasProfile = ATLASProfileHelper.extract(any);
            }
        }
        catch (Exception e)
        {
            if (logger.isWarnEnabled())
                logger.warn("Error parsing ATLAS from IOR: " + e);
            throw new org.omg.CORBA.NO_PERMISSION("SAS Error parsing ATLAS from IOR: " + e, 
                                                  MinorCodes.SAS_ATLAS_FAILURE,
                                                  CompletionStatus.COMPLETED_NO);
        }
        if (atlasProfile == null) 
            return new AuthorizationElement[0];

        String cacheID = new String(atlasProfile.the_cache_id);
        String locator = atlasProfile.the_locator.the_url();
        if (locator != null) 
            locator = URLDecoder.decode(locator);

        // see if the tokens are in the ATLAS cache
        synchronized (atlasCache)
        {
            if (atlasCache.containsKey(cacheID))
            {
                return ((AuthTokenData)atlasCache.get(cacheID)).auth_token;
            }
        }

        // contact the ATLAS server and get the credentials
        AuthTokenDispenser dispenser = null;
        try 
        {
            org.omg.CORBA.Object obj = orb.string_to_object(locator);
            dispenser = AuthTokenDispenserHelper.narrow(obj);
        }
        catch (Exception e)
        {
            logger.warn("Could not find ATLAS server " + locator + ": " + e);
            throw new org.omg.CORBA.NO_PERMISSION("SAS Could not find ATLAS server " + locator + ": " + e, MinorCodes.SAS_ATLAS_FAILURE, CompletionStatus.COMPLETED_NO);
        }
        if (dispenser == null)
        {
            if (logger.isWarnEnabled())
                logger.warn("SAS found null ATLAS server " + locator);
            throw new org.omg.CORBA.NO_PERMISSION("SAS found null ATLAS server "+locator, 
                                                  MinorCodes.SAS_ATLAS_FAILURE, 
                                                  CompletionStatus.COMPLETED_NO);
        }

        AuthTokenData data = null;
        try
        {
            data = dispenser.get_my_authorization_token();
        }
        catch (Exception e)
        {
            if (logger.isWarnEnabled())
                logger.warn("Error getting ATLAS tokens from server " + 
                            locator + ": " + e);
            throw new org.omg.CORBA.NO_PERMISSION("SAS Error getting ATLAS tokens from server: " + e, 
                                                  MinorCodes.SAS_ATLAS_FAILURE, 
                                                  CompletionStatus.COMPLETED_NO);
        }
        synchronized (atlasCache)
        {
            atlasCache.put(cacheID, data);
        }
        return data.auth_token;
    }

}
