package org.jacorb.test.bugs.bug960;

import org.omg.CORBA.Any;
import org.omg.CORBA.INTERNAL;
import org.omg.CORBA.LocalObject;
import org.omg.IOP.Codec;
import org.omg.IOP.ServiceContext;
import org.omg.IOP.CodecPackage.FormatMismatch;
import org.omg.PortableInterceptor.ClientRequestInfo;
import org.omg.PortableInterceptor.ClientRequestInterceptor;
import org.omg.PortableInterceptor.ForwardRequest;
import org.jacorb.test.harness.TestUtils;

final public class ClientRequestInterceptorImpl extends LocalObject implements
        ClientRequestInterceptor
{

    private String name;
    private Codec codec;

    public ClientRequestInterceptorImpl(String string, Codec codec)
    {
        this.name = string;
        this.codec = codec;
    }

    public void receive_exception(ClientRequestInfo ri) throws ForwardRequest
    {
        ServiceContext context = ri.get_reply_service_context(1234);
        byte[] context_data = context.context_data;
        Any any;
        try
        {
            any = codec.decode(context_data);
            TestUtils.getLogger().debug(any.extract_string());
        }
        catch (FormatMismatch e)
        {
            String message = "Unexpected error of FormatMismatch";
            throw new INTERNAL(message);
        }
    }

    public void receive_other(ClientRequestInfo ri) throws ForwardRequest
    {

    }

    public void receive_reply(ClientRequestInfo ri)
    {
    }

    public void send_poll(ClientRequestInfo ri)
    {

    }

    public void send_request(ClientRequestInfo ri) throws ForwardRequest
    {

    }

    public void destroy()
    {

    }

    public String name()
    {
        return this.name;
    }

}
