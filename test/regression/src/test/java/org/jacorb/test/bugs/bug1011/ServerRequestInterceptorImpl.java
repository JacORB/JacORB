package org.jacorb.test.bugs.bug1011;

import org.jacorb.orb.ORB;
import org.omg.CORBA.*;
import org.omg.IOP.Codec;
import org.omg.IOP.ServiceContext;
import org.omg.PortableInterceptor.ForwardRequest;
import org.omg.PortableInterceptor.ServerRequestInfo;
import org.omg.PortableInterceptor.ServerRequestInterceptor;

final public class ServerRequestInterceptorImpl extends LocalObject implements ServerRequestInterceptor
{

    private String name;
    private ORB orb;
    private Codec codec;

    public ServerRequestInterceptorImpl(String string, ORB orb, Codec codec)
    {
        this.name = string;
        this.orb = orb;
        this.codec = codec;
    }

    @Override public void receive_request(ServerRequestInfo ri) throws ForwardRequest
    {
        byte[] encodedCredential = null;
        ServiceContext requestServiceContext = ri.get_request_service_context(CredentialContextId.value);
        encodedCredential = requestServiceContext.context_data;
        Any any;
        try
        {
            any = codec.decode_value(encodedCredential, CredentialHelper.type());
        }
        catch (Exception e)
        {
            String message = "Unexpected error decoding credential";
            throw new INTERNAL(message);
        }
        Credential credential = CredentialHelper.extract(any);
        if (!CORRECT_SECRET.value.equals(credential.secret))
        {
            throw new NO_PERMISSION(0, CompletionStatus.COMPLETED_NO);
        }
    }

    @Override public void receive_request_service_contexts(ServerRequestInfo ri) throws ForwardRequest
    {

    }

    @Override public void send_exception(ServerRequestInfo ri) throws ForwardRequest
    {

    }

    @Override public void send_other(ServerRequestInfo ri) throws ForwardRequest
    {

    }

    @Override public void send_reply(ServerRequestInfo ri)
    {

    }

    @Override public void destroy()
    {

    }

    @Override public String name()
    {
        return this.name;
    }
}
