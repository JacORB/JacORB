package org.jacorb.security.sas;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 2000-2004 Nicolas Noffke, Gerald Brose.
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

import java.util.Hashtable;

import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.configuration.*;

import org.jacorb.orb.MinorCodes;
import org.jacorb.orb.giop.GIOPConnection;
import org.jacorb.orb.portableInterceptor.ServerRequestInfoImpl;
import org.jacorb.sasPolicy.*;

import org.omg.CORBA.Any;
import org.omg.CORBA.BAD_PARAM;
import org.omg.CORBA.CompletionStatus;
import org.omg.CORBA.portable.ObjectImpl;
import org.omg.CSI.*;
import org.omg.IOP.*;
import org.omg.IOP.CodecFactoryPackage.UnknownEncoding;
import org.omg.PortableInterceptor.*;

/**
 * This is the SAS Target Security Service (TSS) Interceptor
 *
 * @author David Robison
 * @version $Id$
 */

public class SASTargetInterceptor
    extends org.omg.CORBA.LocalObject
    implements ServerRequestInterceptor, Configurable
{
    private static final String name = "SASTargetInterceptor";

    private  Logger logger = null;

    protected org.jacorb.orb.ORB orb = null;
    protected Codec codec = null;

    protected int sasReplySlotID = -1;
    protected int clientUserNameSlotID = -1;
    protected int sasContextsCubby = -1;

    protected  boolean useSsl = false;
    protected ISASContext sasContext = null;


    public SASTargetInterceptor(ORBInitInfo info) 
        throws UnknownEncoding, ConfigurationException
    {
        sasReplySlotID = info.allocate_slot_id();
        sasContextsCubby = org.jacorb.orb.giop.GIOPConnection.allocate_cubby_id();
        Encoding encoding = 
            new Encoding(ENCODING_CDR_ENCAPS.value, (byte) 1, (byte) 0);
        codec = 
            info.codec_factory().create_codec(encoding);

        orb = ((org.jacorb.orb.portableInterceptor.ORBInitInfoImpl)info).getORB ();
        configure( orb.getConfiguration());
    }

    public void configure(Configuration configuration)
        throws ConfigurationException
    {
        logger = 
            ((org.jacorb.config.Configuration)configuration).getNamedLogger("jacorb.security.sas.TSS");

        useSsl = 
            configuration.getAttribute("jacorb.security.sas.tss.requires_sas","false").equals("true");

        String contextClass = null;
        try
        {
            configuration.getAttribute("jacorb.security.sas.contextClass");
            Class c = 
                org.jacorb.util.ObjectUtil.classForName(contextClass);
            sasContext = (ISASContext)c.newInstance();
        }
        catch(ConfigurationException ce) 
        {
            if (logger.isDebugEnabled())
                logger.debug("ConfigurationException", ce);
        }
        catch (Exception e) 
        {
            if (logger.isErrorEnabled())
                logger.error("Could not instantiate class " + contextClass + ": " + e);
        }

        if (sasContext == null) 
        {
            if (logger.isErrorEnabled())
                logger.error("Could not load SAS context class: "+ contextClass);
        }
        else 
        {
            sasContext.initTarget();
        }
    }

    public String name()
    {
        return name;
    }

    public void destroy()
    {
    }

    public void receive_request_service_contexts( ServerRequestInfo ri )
        throws ForwardRequest
    {
        if (logger.isDebugEnabled())
            logger.debug("receive_request_service_contexts for " + ri.operation());

        //if (ri.operation().equals("_is_a")) return;
        //if (ri.operation().equals("_non_existent")) return;
        if (sasContext == null) 
            return;

        GIOPConnection connection = 
            ((ServerRequestInfoImpl) ri).request.getConnection();

        // verify SSL requirements
        if (useSsl && !connection.isSSL())
        {
            if (logger.isErrorEnabled())
                logger.error("SSL required for operation " + ri.operation());
            throw new org.omg.CORBA.NO_PERMISSION("SSL Required!", 
                                                  MinorCodes.SAS_TSS_FAILURE, 
                                                  CompletionStatus.COMPLETED_NO);
        }

        // parse service context
        SASContextBody contextBody = null;
        long client_context_id = 0;
        byte[] contextToken = null;
        try
        {
            ServiceContext ctx = 
                ri.get_request_service_context(SASInitializer.SecurityAttributeService);
            Any ctx_any = 
                codec.decode_value( ctx.context_data, SASContextBodyHelper.type() );
            contextBody = SASContextBodyHelper.extract(ctx_any);
        }
        catch (BAD_PARAM e)
        {
        }
        catch (Exception e)
        {
            if (logger.isWarnEnabled())
                logger.warn("Could not parse service context: ", e);
            throw new org.omg.CORBA.NO_PERMISSION("Could not parse service context: " + 
                                                  e, 
                                                  MinorCodes.SAS_TSS_FAILURE, 
                                                  CompletionStatus.COMPLETED_NO);
        }
        if (contextBody == null) 
            return;

        // process MessageInContext
        if (contextBody.discriminator() == MTMessageInContext.value)
        {
            MessageInContext msg = null;
            try
            {
                msg = contextBody.in_context_msg();
                client_context_id = msg.client_context_id;
                contextToken = getSASContext(connection, msg.client_context_id);
            }
            catch (Exception e)
            {
                if (logger.isErrorEnabled())
                    logger.error("Could not parse service MessageInContext " + 
                             ri.operation() + ": " + e);
                throw new org.omg.CORBA.NO_PERMISSION("SAS Error parsing MessageInContext: " +
                                                      e, 
                                                      MinorCodes.SAS_TSS_FAILURE, 
                                                      CompletionStatus.COMPLETED_NO);
            }
            if (contextToken == null)
            {
                if (logger.isErrorEnabled())
                    logger.error("Could not parse service MessageInContext " + 
                                 ri.operation() + ": " + msg.client_context_id);

                throw new org.omg.CORBA.NO_PERMISSION("SAS Error parsing MessageInContext", 
                                                      MinorCodes.SAS_TSS_FAILURE, 
                                                      CompletionStatus.COMPLETED_NO);
            }
        }

        // process EstablishContext
        String principalName = null;
        if (contextBody.discriminator() == MTEstablishContext.value)
        {
            EstablishContext msg = null;
            try
            {
                msg = contextBody.establish_msg();
                client_context_id = msg.client_context_id;
                contextToken = msg.client_authentication_token;

                if (!sasContext.validateContext(ri, contextToken)) {
                    logger.info("Could not validate context EstablishContext " + ri.operation());
                    throw new org.omg.CORBA.NO_PERMISSION("SAS Error validating context", 
                                                          MinorCodes.SAS_TSS_FAILURE, 
                                                          CompletionStatus.COMPLETED_NO);
                }
                principalName = sasContext.getValidatedPrincipal();
            }
            catch (org.omg.CORBA.NO_PERMISSION e)
            {
                if (logger.isErrorEnabled())
                    logger.error("Err " + ri.operation() + ": " + e);
                makeContextError(ri, client_context_id, 1, 1, contextToken);
                throw e;
            }
            catch (Exception e)
            {
                if (logger.isErrorEnabled())
                    logger.error("Could not parse service EstablishContext " + 
                                 ri.operation() + ": " + e);
                makeContextError(ri, client_context_id, 1, 1, contextToken);
                throw new org.omg.CORBA.NO_PERMISSION("SAS Error parsing EstablishContext: " +
                                                      e, MinorCodes.SAS_TSS_FAILURE, 
                                                      CompletionStatus.COMPLETED_NO);
            }
            if (contextToken == null)
            {
                if (logger.isErrorEnabled())
                    logger.error("Could not parse service EstablishContext " +
                                 ri.operation() + ": " + msg.client_context_id);
                makeContextError(ri, client_context_id, 1, 1, contextToken);
                throw new org.omg.CORBA.NO_PERMISSION("SAS Error parsing EstablishContext", 
                                                      MinorCodes.SAS_TSS_FAILURE, 
                                                      CompletionStatus.COMPLETED_NO);
            }
        }

        // set slots
        try
        {
            Any nameAny = orb.create_any();
            if (principalName == null) 
                principalName = 
                    getSASContextPrincipalName(connection, client_context_id);

            nameAny.insert_string(principalName);
            ri.set_slot( SASInitializer.sasPrincipalNamePIC, nameAny);
        }
        catch (Exception e)
        {
            if (logger.isErrorEnabled())
                logger.error("Error inserting service context into slots for " + 
                             ri.operation() + ": " + e);
            makeContextError(ri, client_context_id, 1, 1, contextToken);
            throw new org.omg.CORBA.NO_PERMISSION("SAS Error insert service context into slots: " + e, 
                                                  MinorCodes.SAS_TSS_FAILURE, 
                                                  CompletionStatus.COMPLETED_NO);
        }
    }

    public void receive_request( ServerRequestInfo ri )
        throws ForwardRequest
    {
        if (logger.isDebugEnabled())
            logger.debug("receive_request for "+ri.operation());

        //if (ri.operation().equals("_is_a")) return;
        //if (ri.operation().equals("_non_existent")) return;
        if (sasContext == null) 
            return;
        GIOPConnection connection = 
            ((ServerRequestInfoImpl) ri).request.getConnection();

        // check policy
        SASPolicyValues sasValues = null;
        try 
        {
            ObjectImpl oi = 
                (ObjectImpl)((ServerRequestInfoImpl) ri).target();
            org.jacorb.orb.Delegate d = 
                (org.jacorb.orb.Delegate)oi._get_delegate();
            SASPolicy policy = 
                (SASPolicy)d.getPOA().getPolicy(SAS_POLICY_TYPE.value);
            //SASPolicy policy = (SASPolicy)ri.get_server_policy(SAS_POLICY_TYPE.value);
            if (policy != null) 
                sasValues = policy.value();
        } 
        catch (BAD_PARAM e) 
        {
            if (logger.isDebugEnabled())
                logger.debug("No SAS Policy for "+ri.operation());
        } 
        catch (Exception e) 
        {
            if (logger.isWarnEnabled())
                logger.warn("Error fetching SAS policy for "+
                            ri.operation()+": "+e);
            throw new org.omg.CORBA.NO_PERMISSION("Error fetching SAS policy: "+e, 
                                                  MinorCodes.SAS_TSS_FAILURE, 
                                                  CompletionStatus.COMPLETED_NO);
        }
        if (sasValues == null) 
            return;
        if (sasValues.targetRequires == 0 && sasValues.targetSupports == 0)
            return;

        ATLASPolicyValues atlasValues = null;
        try 
        {
            ObjectImpl oi = (ObjectImpl)((ServerRequestInfoImpl) ri).target();
            org.jacorb.orb.Delegate d = (org.jacorb.orb.Delegate)oi._get_delegate();
            ATLASPolicy policy = (ATLASPolicy)d.getPOA().getPolicy(ATLAS_POLICY_TYPE.value);
            //SASPolicy policy = (SASPolicy)ri.get_server_policy(SAS_POLICY_TYPE.value);
            if (policy != null) 
                atlasValues = policy.value();
        } 
        catch (BAD_PARAM e) 
        {
            if (logger.isDebugEnabled())
                logger.debug("No ATLAS Policy for "+ri.operation());
        } 
        catch (Exception e) 
        {
            if (logger.isWarnEnabled())
                logger.warn("Error fetching ATLAS policy for "+
                            ri.operation()+": "+e);
            throw new org.omg.CORBA.NO_PERMISSION("Error fetching ATLAS policy: "+e, 
                                                  MinorCodes.SAS_TSS_FAILURE, 
                                                  CompletionStatus.COMPLETED_NO);
        }

        // parse service context
        SASContextBody contextBody = null;
        long client_context_id = 0;
        byte[] contextToken = null;
        try
        {
            ServiceContext ctx = 
                ri.get_request_service_context(SASInitializer.SecurityAttributeService);
            Any ctx_any =
                codec.decode_value( ctx.context_data, SASContextBodyHelper.type() );
            contextBody =
                SASContextBodyHelper.extract(ctx_any);
        }
        catch (BAD_PARAM e)
        {
            if (logger.isDebugEnabled())
                logger.debug("Could not parse service context for operation " + 
                         ri.operation());
        }
        catch (Exception e)
        {
            if (logger.isWarnEnabled())
                logger.warn("Could not parse service context for operation " + 
                        ri.operation() + ": " + e);
        }

        if (contextBody == null && 
           (sasValues.targetRequires & org.omg.CSIIOP.EstablishTrustInClient.value) != 0 &&
           !ri.operation().equals("_non_existent") &&
           !ri.operation().equals("_is_a")) 
        {
            if (logger.isErrorEnabled())
                logger.error("Did not parse service context for operation " + 
                         ri.operation());
            throw new org.omg.CORBA.NO_PERMISSION("No SAS service context found", 
                                                  MinorCodes.SAS_TSS_FAILURE, 
                                                  CompletionStatus.COMPLETED_NO);
        }
        if (contextBody == null) 
        {
            if (logger.isDebugEnabled())
                logger.debug("No context found, but not required");
            return;
        }

        // process MessageInContext
        if (contextBody.discriminator() == MTMessageInContext.value)
        {
            MessageInContext msg = null;
            try
            {
                msg = contextBody.in_context_msg();
                client_context_id = msg.client_context_id;
                contextToken = getSASContext(connection, msg.client_context_id);
            }
            catch (Exception e)
            {
                if (logger.isErrorEnabled())
                    logger.error("Could not parse service MessageInContext " + 
                                 ri.operation() + ": " + e);
                makeContextError(ri, client_context_id, 1, 1, contextToken);
                throw new org.omg.CORBA.NO_PERMISSION("SAS Error parsing MessageInContext: " + e, 
                                                      MinorCodes.SAS_TSS_FAILURE, 
                                                      CompletionStatus.COMPLETED_NO);
            }
            if (contextToken == null)
            {
                if (logger.isErrorEnabled())
                    logger.error("Could not parse service MessageInContext " +
                                 ri.operation() + ": " + msg.client_context_id);
                makeContextError(ri, client_context_id, 1, 1, contextToken);
                throw new org.omg.CORBA.NO_PERMISSION("SAS Error parsing MessageInContext", 
                                                      MinorCodes.SAS_TSS_FAILURE, 
                                                      CompletionStatus.COMPLETED_NO);
            }
        }

        // process EstablishContext
        if (contextBody.discriminator() == MTEstablishContext.value)
        {
            EstablishContext msg = null;
            String principalName = null;
            try
            {
                msg = contextBody.establish_msg();
                client_context_id = msg.client_context_id;
                contextToken = msg.client_authentication_token;

                //if (!sasContext.validateContext(ri, contextToken)) throw new org.omg.CORBA.NO_PERMISSION("SAS Error validating context", MinorCodes.SAS_TSS_FAILURE, CompletionStatus.COMPLETED_NO);
                principalName = sasContext.getValidatedPrincipal();
            }
            catch (org.omg.CORBA.NO_PERMISSION e)
            {
                if (logger.isErrorEnabled())
                    logger.error("Err " + ri.operation() + ": " + e);
                makeContextError(ri, client_context_id, 1, 1, contextToken);
                throw e;
            }
            catch (Exception e)
            {
                if (logger.isErrorEnabled())
                    logger.error("Could not parse service EstablishContext " + 
                                 ri.operation() + ": " + e);
                makeContextError(ri, client_context_id, 1, 1, contextToken);
                throw new org.omg.CORBA.NO_PERMISSION("SAS Error parsing EstablishContext: " + e, 
                                                      MinorCodes.SAS_TSS_FAILURE, 
                                                      CompletionStatus.COMPLETED_NO);
            }
            if (contextToken == null)
            {
                if (logger.isErrorEnabled())
                    logger.error("Could not parse service EstablishContext " + 
                                 ri.operation() + ": " + msg.client_context_id);
                makeContextError(ri, client_context_id, 1, 1, contextToken);

                throw new org.omg.CORBA.NO_PERMISSION("SAS Error parsing EstablishContext", 
                                                      MinorCodes.SAS_TSS_FAILURE, 
                                                      CompletionStatus.COMPLETED_NO);
            }

            // cache context
            if (sasValues.stateful) 
                cacheSASContext(connection, msg.client_context_id, 
                                contextToken, principalName);
        }

        // set slots
        try
        {
            makeCompleteEstablishContext(ri, client_context_id, sasValues);
            //Any nameAny = orb.create_any();
            //nameAny.insert_string(getSASContextPrincipalName(connection, client_context_id));
            //ri.set_slot( SASInitializer.sasPrincipalNamePIC, nameAny);
        }
        catch (Exception e)
        {
            if (logger.isErrorEnabled())
                logger.error("Error inserting service context into slots for " + 
                             ri.operation() + ": " + e);
            makeContextError(ri, client_context_id, 1, 1, contextToken);
            throw new org.omg.CORBA.NO_PERMISSION("SAS Error insert service context into slots: " + e, MinorCodes.SAS_TSS_FAILURE, CompletionStatus.COMPLETED_NO);
        }
    }

    public void send_reply( ServerRequestInfo ri )
    {
        if (logger.isDebugEnabled())
            logger.debug("send_reply for "+ri.operation());

        //if (!useSAS) return;
        Any slot_any = null;
        try 
        {
            slot_any = ri.get_slot(sasReplySlotID);
        }
        catch (BAD_PARAM e)
        {
            if (logger.isDebugEnabled())
                logger.debug("No SAS reply found " + ri.operation() + ": ");
        }
        catch (Exception e)
        {
            if (logger.isWarnEnabled())
                logger.warn("No SAS reply found " + ri.operation() + ": ");
        }
        if (slot_any == null) return;

        try
        {
            ri.add_reply_service_context(
                  new ServiceContext(SASInitializer.SecurityAttributeService, 
                                     codec.encode_value( slot_any ) ), 
                  true);
        }
        catch (Exception e)
        {
            if (logger.isErrorEnabled())
                logger.error("Error setting reply service context " + 
                             ri.operation() + ": " + e);
            throw new org.omg.CORBA.NO_PERMISSION("SAS Error setting reply service contex: " + e, 
                                                  MinorCodes.SAS_TSS_FAILURE, 
                                                  CompletionStatus.COMPLETED_MAYBE);
        }
    }

    public void send_exception( ServerRequestInfo ri )
        throws ForwardRequest
    {
        if (logger.isDebugEnabled())
            logger.debug("send_exception for "+ri.operation());

        //if (!useSAS) return;
        Any slot_any = null;
        try 
        {
            slot_any = ri.get_slot(sasReplySlotID);
        }
        catch (BAD_PARAM e)
        {
            if (logger.isDebugEnabled())
                logger.debug("No SAS reply found " + ri.operation() + ": ");
        }
        catch (Exception e)
        {
            if (logger.isWarnEnabled())
                logger.warn("No SAS reply found " + ri.operation() + ": ");
        }
        if (slot_any == null)
            return;

        try
        {
            ri.add_reply_service_context(
                 new ServiceContext(
                        SASInitializer.SecurityAttributeService, 
                        codec.encode_value( slot_any ) 
                        ),
                 true);
        }
        catch (Exception e)
        {
            if (logger.isErrorEnabled())
                logger.error("Error setting reply service context:" + e);
            throw new org.omg.CORBA.NO_PERMISSION("SAS Error setting reply service context: " + e, 
                                                  MinorCodes.SAS_TSS_FAILURE, 
                                                  CompletionStatus.COMPLETED_MAYBE);
        }
    }

    public void send_other( ServerRequestInfo ri )
        throws ForwardRequest
    {
        if (logger.isDebugEnabled())
            logger.debug("send_other for "+ri.operation());
    }

    protected Any makeCompleteEstablishContext(ServerRequestInfo ri, long client_context_id, SASPolicyValues sasValues) {
        CompleteEstablishContext msg = new CompleteEstablishContext();
        msg.client_context_id = client_context_id;
        msg.context_stateful = sasValues.stateful;
        msg.final_context_token = new byte[0];
        SASContextBody contextBody = new SASContextBody();
        contextBody.complete_msg(msg);
        Any any = orb.create_any();
        SASContextBodyHelper.insert( any, contextBody );
        if (ri != null)
        {
            try
            {
                ri.add_reply_service_context(new ServiceContext(SASInitializer.SecurityAttributeService, codec.encode_value( any ) ), true);
            }
            catch (Exception e)
            {
                logger.error("Error setting reply service context:" + e);
                throw new org.omg.CORBA.NO_PERMISSION("SAS Error setting reply service context: " + e, MinorCodes.SAS_TSS_FAILURE, CompletionStatus.COMPLETED_MAYBE);
            }
        }
        return any;
    }

    protected Any makeContextError(ServerRequestInfo ri, long client_context_id, int major_status, int minor_status, byte[] error_token) {
        ContextError msg = new ContextError();
        msg.client_context_id = client_context_id;
        msg.error_token = error_token;
        msg.major_status = major_status;
        msg.minor_status = minor_status;
        SASContextBody contextBody = new SASContextBody();
        contextBody.error_msg(msg);
        Any any = orb.create_any();
        SASContextBodyHelper.insert( any, contextBody );
        if (ri != null)
        {
            try
            {
                ri.add_reply_service_context(new ServiceContext(SASInitializer.SecurityAttributeService, codec.encode_value( any ) ), true);
            }
            catch (Exception e)
            {
                logger.error("Error setting reply service context:" + e);
                throw new org.omg.CORBA.NO_PERMISSION("SAS Error setting reply service context: " + e, MinorCodes.SAS_TSS_FAILURE, CompletionStatus.COMPLETED_MAYBE);
            }
        }
        return any;
    }

    // manage cached contexts

    class CachedContext
    {
        public byte[] client_authentication_token;
        public String principalName;
        CachedContext(byte[] client_authentication_token, String principalName)
        {
            this.client_authentication_token = client_authentication_token;
            this.principalName = principalName;
        }
    }

    public void cacheSASContext(GIOPConnection connection, 
                                long client_context_id, 
                                byte[] client_authentication_token, 
                                String principalName)
    {
        synchronized ( connection )
        {
            Hashtable sasContexts = 
                (Hashtable) connection.get_cubby(sasContextsCubby);
            if (sasContexts == null) 
            {
                sasContexts = new Hashtable();
                connection.set_cubby(sasContextsCubby, sasContexts);
            }
            sasContexts.put(new Long(client_context_id), 
                            new CachedContext(client_authentication_token, principalName));
        }
    }

    public void purgeSASContext(GIOPConnection connection, long client_context_id)
    {
        synchronized ( connection )
        {
            Hashtable sasContexts = (Hashtable) connection.get_cubby(sasContextsCubby);
            if (sasContexts == null) 
            {
                sasContexts = new Hashtable();
                connection.set_cubby(sasContextsCubby, sasContexts);
            }
            sasContexts.remove(new Long(client_context_id));
        }
    }

    public byte[] getSASContext(GIOPConnection connection, long client_context_id)
    {
        Long key = new Long(client_context_id);
        synchronized ( connection )
        {
            Hashtable sasContexts = (Hashtable) connection.get_cubby(sasContextsCubby);
            if (sasContexts == null) 
            {
                sasContexts = new Hashtable();
                connection.set_cubby(sasContextsCubby, sasContexts);
            }
            if (!sasContexts.containsKey(key)) 
                return null;
            return ((CachedContext)sasContexts.get(key)).client_authentication_token;
        }
    }

    public String getSASContextPrincipalName(GIOPConnection connection, 
                                             long client_context_id)
    {
        Long key = new Long(client_context_id);
        synchronized ( connection )
        {
            Hashtable sasContexts = 
                (Hashtable) connection.get_cubby(sasContextsCubby);
            if (sasContexts == null) 
            {
                sasContexts = new Hashtable();
                connection.set_cubby(sasContextsCubby, sasContexts);
            }
            if (!sasContexts.containsKey(key)) 
                return null;
            return ((CachedContext)sasContexts.get(key)).principalName;
        }
    }
}
