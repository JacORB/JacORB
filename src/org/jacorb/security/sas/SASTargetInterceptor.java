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

import java.util.Hashtable;

import org.jacorb.orb.MinorCodes;
import org.jacorb.orb.giop.GIOPConnection;
import org.jacorb.orb.portableInterceptor.ServerRequestInfoImpl;
import org.jacorb.util.Debug;
import org.jacorb.util.Environment;
import org.omg.CORBA.Any;
import org.omg.CORBA.BAD_PARAM;
import org.omg.CORBA.CompletionStatus;
import org.omg.CSI.CompleteEstablishContext;
import org.omg.CSI.ContextError;
import org.omg.CSI.EstablishContext;
import org.omg.CSI.MTEstablishContext;
import org.omg.CSI.MTMessageInContext;
import org.omg.CSI.MessageInContext;
import org.omg.CSI.SASContextBody;
import org.omg.CSI.SASContextBodyHelper;
import org.omg.IOP.Codec;
import org.omg.IOP.ENCODING_CDR_ENCAPS;
import org.omg.IOP.Encoding;
import org.omg.IOP.ServiceContext;
import org.omg.IOP.CodecFactoryPackage.UnknownEncoding;
import org.omg.PortableInterceptor.ForwardRequest;
import org.omg.PortableInterceptor.ORBInitInfo;
import org.omg.PortableInterceptor.ServerRequestInfo;
import org.omg.PortableInterceptor.ServerRequestInterceptor;

/**
 * This is the SAS Target Security Service (TSS) Interceptor
 *
 * @author David Robison
 * @version $Id$
 */

public class SASTargetInterceptor
    extends org.omg.CORBA.LocalObject
    implements ServerRequestInterceptor
{
    private static final String name = "SASTargetInterceptor";
    protected org.jacorb.orb.ORB orb = null;
    protected Codec codec = null;

    protected int sasReplySlotID = -1;
    protected int clientUserNameSlotID = -1;
    protected int sasContextsCubby = -1;

	protected static boolean useSAS = true;
    protected static boolean useStateful = true;
	protected static short targetRequires = (short)0;
	protected static short targetSupports = (short)0;
    protected static boolean useSsl = false;

    protected ISASContextValidator contextValidator = null;

    public SASTargetInterceptor(ORBInitInfo info) throws UnknownEncoding
    {
        sasReplySlotID = info.allocate_slot_id();
        sasContextsCubby = org.jacorb.orb.giop.GIOPConnection.allocate_cubby_id();
        Encoding encoding = new Encoding(ENCODING_CDR_ENCAPS.value, (byte) 1, (byte) 0);
        codec = info.codec_factory().create_codec(encoding);
        orb = ((org.jacorb.orb.portableInterceptor.ORBInitInfoImpl) info).getORB ();
		useSAS = Environment.isPropertyOn("jacorb.security.support_sas");
        useStateful = Boolean.valueOf(org.jacorb.util.Environment.getProperty("jacorb.security.sas.stateful", "true")).booleanValue();

        // see what transport modes are required
        String targetRequiresNames = Environment.getProperty( "jacorb.security.sas.tss.target_requires", "" );
        java.util.StringTokenizer requiredNameTokens = new java.util.StringTokenizer(targetRequiresNames, ":;, ");
        while (requiredNameTokens.hasMoreTokens())
        {
          String token = requiredNameTokens.nextToken();
          if (token.equals("Integrity"))                   targetRequires |= org.omg.CSIIOP.Integrity.value;
          else if (token.equals("Confidentiality"))        targetRequires |= org.omg.CSIIOP.Confidentiality.value;
          else if (token.equals("EstablishTrustInTarget")) targetRequires |= org.omg.CSIIOP.EstablishTrustInTarget.value;
          else if (token.equals("EstablishTrustInClient")) targetRequires |= org.omg.CSIIOP.EstablishTrustInClient.value;
          else if (token.equals("IdentityAssertion"))      targetRequires |= org.omg.CSIIOP.IdentityAssertion.value;
          else if (token.equals("DelegationByClient"))     targetRequires |= org.omg.CSIIOP.DelegationByClient.value;
          else org.jacorb.util.Debug.output("Unknown SAS Association Taken: " + token);
        }

		// see what transport modes are supported
		String targetSupportsNames = Environment.getProperty( "jacorb.security.sas.tss.target_supports", "" );
		java.util.StringTokenizer supportedNameTokens = new java.util.StringTokenizer(targetSupportsNames, ":;, ");
		while (supportedNameTokens.hasMoreTokens())
		{
		  String token = supportedNameTokens.nextToken();
		  if (token.equals("Integrity"))                   targetSupports |= org.omg.CSIIOP.Integrity.value;
		  else if (token.equals("Confidentiality"))        targetSupports |= org.omg.CSIIOP.Confidentiality.value;
		  else if (token.equals("EstablishTrustInTarget")) targetSupports |= org.omg.CSIIOP.EstablishTrustInTarget.value;
		  else if (token.equals("EstablishTrustInClient")) targetSupports |= org.omg.CSIIOP.EstablishTrustInClient.value;
		  else if (token.equals("IdentityAssertion"))      targetSupports |= org.omg.CSIIOP.IdentityAssertion.value;
		  else if (token.equals("DelegationByClient"))     targetSupports |= org.omg.CSIIOP.DelegationByClient.value;
		  else org.jacorb.util.Debug.output("Unknown SAS Association Taken: " + token);
		}

		useSsl = Boolean.getBoolean(org.jacorb.util.Environment.getProperty( "jacorb.security.sas.tss.requires_sas", "false" ));

        String validatorClass = org.jacorb.util.Environment.getProperty("jacorb.security.sas.tss.context_validator");
        if (validatorClass != null) {
            try {
              Class c = Class.forName(validatorClass);
              contextValidator = (ISASContextValidator)c.newInstance();
            } catch (Exception e) {
              Debug.output("Could not instantiate class " + validatorClass + ": " + e);
            }
        }
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
    }

    public void receive_request_service_contexts( ServerRequestInfo ri )
        throws ForwardRequest
    {
    	if (!useSAS) return;
        if (ri.operation().equals("_is_a")) return;
        if (ri.operation().equals("_non_existent")) return;
        GIOPConnection connection = ((ServerRequestInfoImpl) ri).request.getConnection();

        // verify SSL requirements
        if (useSsl && !connection.isSSL())
        {
            Debug.output("SSL required for operation " + ri.operation());
            throw new org.omg.CORBA.NO_PERMISSION("SSL Required!", MinorCodes.SAS_TSS_FAILURE, CompletionStatus.COMPLETED_NO);
        }

        // parse service context
        SASContextBody contextBody = null;
        long client_context_id = 0;
        byte[] contextToken = null;
        try
        {
            ServiceContext ctx = ri.get_request_service_context(SASTargetInitializer.SecurityAttributeService);
            Any ctx_any = codec.decode( ctx.context_data );
            contextBody = SASContextBodyHelper.extract(ctx_any);
        }
		catch (BAD_PARAM e)
		{
			Debug.output("Could not parse service context for operation " + ri.operation());
		}
		catch (Exception e)
		{
			Debug.output("Could not parse service context for operation " + ri.operation() + ": " + e);
		}
        if (contextBody == null && (targetRequires & org.omg.CSIIOP.EstablishTrustInClient.value) != 0) {
			Debug.output("Did not parse service context for operation " + ri.operation());
			throw new org.omg.CORBA.NO_PERMISSION("No SAS service context found", MinorCodes.SAS_TSS_FAILURE, CompletionStatus.COMPLETED_NO);
        }
        if (contextBody == null) return;

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
                Debug.output("Could not parse service MessageInContext " + ri.operation() + ": " + e);
                throw new org.omg.CORBA.NO_PERMISSION("SAS Error parsing MessageInContext: " + e, MinorCodes.SAS_TSS_FAILURE, CompletionStatus.COMPLETED_NO);
            }
            if (contextToken == null)
            {
                Debug.output("Could not parse service MessageInContext " + ri.operation() + ": " + msg.client_context_id);
                throw new org.omg.CORBA.NO_PERMISSION("SAS Error parsing MessageInContext", MinorCodes.SAS_TSS_FAILURE, CompletionStatus.COMPLETED_NO);
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

                // verify context
                //Oid myMechOid = myCredential.getMechs()[0];
                //GSSContext context = gssManager.createContext(myCredential);
                //context.acceptSecContext(msg.client_authentication_token, 0, msg.client_authentication_token.length);
                //GSSName sourceName = context.getSrcName();
                //contextToken = sourceName.toString().getBytes();
                //JBuffer cssBuffer = new JBuffer();
                //cssBuffer.Allocate(msg.client_authentication_token.length, JBuffer.bo_lib);
                //cssBuffer.SetContents(msg.client_authentication_token, msg.client_authentication_token.length);
//byte[] b = (byte[])cssBuffer.GetBufferForRecv().clone();
//for (int i=0;i<b.length;i++) System.out.print(Integer.toHexString((int)b[i])+" ");System.out.println();
                //int ok = tssContext.Authenticate ( cssBuffer, tssBuffer );
//System.out.println("OK="+ok);
                //if (ok != JServerContext.as_ok) throw new org.omg.CORBA.NO_PERMISSION("SAS Error validating context", MinorCodes.SAS_TSS_FAILURE, CompletionStatus.COMPLETED_NO);
                if (contextValidator != null && !contextValidator.validate(ri, contextToken)) throw new org.omg.CORBA.NO_PERMISSION("SAS Error validating context", MinorCodes.SAS_TSS_FAILURE, CompletionStatus.COMPLETED_NO);
                if (contextValidator != null) principalName = contextValidator.getPrincipalName();
            }
            catch (org.omg.CORBA.NO_PERMISSION e)
            {
                Debug.output("Err " + ri.operation() + ": " + e);
                throw e;
            }
            catch (Exception e)
            {
                Debug.output("Could not parse service EstablishContext " + ri.operation() + ": " + e);
                throw new org.omg.CORBA.NO_PERMISSION("SAS Error parsing EstablishContext: " + e, MinorCodes.SAS_TSS_FAILURE, CompletionStatus.COMPLETED_NO);
            }
            if (contextToken == null)
            {
                Debug.output("Could not parse service EstablishContext " + ri.operation() + ": " + msg.client_context_id);
                throw new org.omg.CORBA.NO_PERMISSION("SAS Error parsing EstablishContext", MinorCodes.SAS_TSS_FAILURE, CompletionStatus.COMPLETED_NO);
            }

            // cache context
            if (useStateful) cacheSASContext(connection, msg.client_context_id, contextToken, principalName);
        }

        // set slots
        try
        {
            //Any source_any = orb.create_any();
            //source_any.insert_string(new String(contextToken));
            //Any msg_any = orb.create_any();
            //EstablishContextHelper.insert(msg_any, getSASContextMsg(connection, client_context_id));
            //ri.set_slot( sourceNameSlotID, source_any);
            //ri.set_slot( contextMsgSlotID, msg_any);
            ri.set_slot( sasReplySlotID, makeCompleteEstablishContext(client_context_id));
            Any nameAny = orb.create_any();
            nameAny.insert_string(getSASContextPrincipalName(connection, client_context_id));
            ri.set_slot( SASTargetInitializer.sasPrincipalNamePIC, nameAny);
        }
        catch (Exception e)
        {
            Debug.output("Error insert service context into slots " + ri.operation() + ": " + e);
            try { ri.set_slot( sasReplySlotID, makeContextError(client_context_id, 1, 1, contextToken)); } catch (Exception ee) {}
            throw new org.omg.CORBA.NO_PERMISSION("SAS Error insert service context into slots: " + e, MinorCodes.SAS_TSS_FAILURE, CompletionStatus.COMPLETED_NO);
        }
    }

    public void send_reply( ServerRequestInfo ri )
    {
		if (!useSAS) return;
        Any slot_any = null;
        try {
            slot_any = ri.get_slot(sasReplySlotID);
        }
        catch (Exception e)
        {
            Debug.output("No SAS reply found " + ri.operation() + ": ");
        }
        if (slot_any == null) return;

        try
        {
            ri.add_reply_service_context(new ServiceContext(SASTargetInitializer.SecurityAttributeService, codec.encode( slot_any ) ), true);
        }
        catch (Exception e)
        {
            Debug.output("Error setting reply service context " + ri.operation() + ": " + e);
            throw new org.omg.CORBA.NO_PERMISSION("SAS Error setting reply service contex: " + e, MinorCodes.SAS_TSS_FAILURE, CompletionStatus.COMPLETED_MAYBE);
        }
    }

    public void send_exception( ServerRequestInfo ri )
        throws ForwardRequest
    {
		if (!useSAS) return;
        try
        {
            ri.add_reply_service_context(new ServiceContext(SASTargetInitializer.SecurityAttributeService, codec.encode( ri.get_slot(sasReplySlotID) ) ), true);
        }
        catch (Exception e)
        {
            Debug.output("Error setting reply service context:" + e);
            throw new org.omg.CORBA.NO_PERMISSION("SAS Error setting reply service context: " + e, MinorCodes.SAS_TSS_FAILURE, CompletionStatus.COMPLETED_MAYBE);
        }
    }

    public void send_other( ServerRequestInfo ri )
        throws ForwardRequest
    {
    }

    protected Any makeCompleteEstablishContext(long client_context_id) {
        CompleteEstablishContext msg = new CompleteEstablishContext();
        msg.client_context_id = client_context_id;
        msg.context_stateful = useStateful;
        msg.final_context_token = new byte[0];
        SASContextBody contextBody = new SASContextBody();
        contextBody.complete_msg(msg);
        Any any = orb.create_any();
        SASContextBodyHelper.insert( any, contextBody );
        return any;
    }

    protected Any makeContextError(long client_context_id, int major_status, int minor_status, byte[] error_token) {
        ContextError msg = new ContextError();
        msg.client_context_id = client_context_id;
        msg.error_token = error_token;
        msg.major_status = major_status;
        msg.minor_status = minor_status;
        SASContextBody contextBody = new SASContextBody();
        contextBody.error_msg(msg);
        Any any = orb.create_any();
        SASContextBodyHelper.insert( any, contextBody );
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

    public void cacheSASContext(GIOPConnection connection, long client_context_id, byte[] client_authentication_token, String principalName)
    {
        synchronized ( connection )
        {
            Hashtable sasContexts = (Hashtable) connection.get_cubby(sasContextsCubby);
            if (sasContexts == null) {
                sasContexts = new Hashtable();
                connection.set_cubby(sasContextsCubby, sasContexts);
            }
            sasContexts.put(new Long(client_context_id), new CachedContext(client_authentication_token, principalName));
        }
    }

    public void purgeSASContext(GIOPConnection connection, long client_context_id)
    {
        synchronized ( connection )
        {
            Hashtable sasContexts = (Hashtable) connection.get_cubby(sasContextsCubby);
            if (sasContexts == null) {
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
            if (sasContexts == null) {
                sasContexts = new Hashtable();
                connection.set_cubby(sasContextsCubby, sasContexts);
            }
            if (!sasContexts.containsKey(key)) return null;
            return ((CachedContext)sasContexts.get(key)).client_authentication_token;
        }
    }

    public String getSASContextPrincipalName(GIOPConnection connection, long client_context_id)
    {
        Long key = new Long(client_context_id);
        synchronized ( connection )
        {
            Hashtable sasContexts = (Hashtable) connection.get_cubby(sasContextsCubby);
            if (sasContexts == null) {
                sasContexts = new Hashtable();
                connection.set_cubby(sasContextsCubby, sasContexts);
            }
            if (!sasContexts.containsKey(key)) return null;
            return ((CachedContext)sasContexts.get(key)).principalName;
        }
    }
}
