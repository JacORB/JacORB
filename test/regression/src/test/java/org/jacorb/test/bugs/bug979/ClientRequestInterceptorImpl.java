package org.jacorb.test.bugs.bug979;

import org.omg.CORBA.Any;
import org.omg.CORBA.INTERNAL;
import org.omg.CORBA.LocalObject;
import org.omg.CORBA.NO_PERMISSIONHelper;
import org.omg.CORBA.ORB;
import org.omg.IOP.Codec;
import org.omg.IOP.ServiceContext;
import org.omg.IOP.CodecPackage.InvalidTypeForEncoding;
import org.omg.PortableInterceptor.ClientRequestInfo;
import org.omg.PortableInterceptor.ClientRequestInterceptor;
import org.omg.PortableInterceptor.ForwardRequest;

final public class ClientRequestInterceptorImpl extends LocalObject implements
        ClientRequestInterceptor
{

    private String name;
    private ORB orb;
    private Codec codec;

    private int counter = 1;

    public ClientRequestInterceptorImpl(String string, ORB orb, Codec codec)
    {
        this.name = string;
        this.orb = orb;
        this.codec = codec;
    }

    private String getSecret()
    {
        int i = counter++;
        if (i % 2 == 0)
        {
            return CORRECT_SECRET.value;
        }
        else
        {
            return "Any wrong secret";
        }
    }

    @Override
    public void send_request(ClientRequestInfo ri) throws ForwardRequest
    {
        Credential credential = new Credential(getSecret());
        Any anyCredential = orb.create_any();
        CredentialHelper.insert(anyCredential, credential);
        byte[] encodedCredential;
        try
        {
            encodedCredential = codec.encode_value(anyCredential);
        }
        catch (InvalidTypeForEncoding e)
        {
            String message = "Unexpected error encoding credential";
            throw new INTERNAL(message);
        }
        ServiceContext requestServiceContext = new ServiceContext(
                CredentialContextId.value, encodedCredential);
        ri.add_request_service_context(requestServiceContext, false);
    }

    @Override
    public void receive_exception(ClientRequestInfo ri) throws ForwardRequest
    {
        if (ri.received_exception_id().equals(NO_PERMISSIONHelper.id()))
        {
            throw new ForwardRequest(ri.target());
        }
    }

    @Override
    public void receive_other(ClientRequestInfo ri) throws ForwardRequest
    {

    }

    @Override
    public void receive_reply(ClientRequestInfo ri)
    {

    }

    @Override
    public void send_poll(ClientRequestInfo ri)
    {

    }

    @Override
    public void destroy()
    {

    }

    @Override
    public String name()
    {
        return this.name;
    }

}