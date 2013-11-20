package org.jacorb.test.bugs.bug960;

import org.jacorb.orb.ORB;
import org.omg.CORBA.Any;
import org.omg.CORBA.INTERNAL;
import org.omg.CORBA.LocalObject;
import org.omg.CORBA.NO_PERMISSION;
import org.omg.IOP.Codec;
import org.omg.IOP.ServiceContext;
import org.omg.IOP.CodecPackage.InvalidTypeForEncoding;
import org.omg.PortableInterceptor.ForwardRequest;
import org.omg.PortableInterceptor.ServerRequestInfo;
import org.omg.PortableInterceptor.ServerRequestInterceptor;

final public class ServerRequestInterceptorImpl extends LocalObject implements
        ServerRequestInterceptor
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

    public void receive_request(ServerRequestInfo ri) throws ForwardRequest
    {
        Any any = orb.create_any();
        any.insert_string("some infos");
        ServiceContext requestServiceContext;
        try
        {
            requestServiceContext = new ServiceContext(1234, codec.encode(any));
        }
        catch (InvalidTypeForEncoding e)
        {
            String message = "Unexpected error while encoding";
            throw new INTERNAL(message);
        }
        ri.add_reply_service_context(requestServiceContext, true);
        throw new NO_PERMISSION("my test forces NO_PERMISSION");
    }

    public void receive_request_service_contexts(ServerRequestInfo ri)
            throws ForwardRequest
    {

    }

    public void send_exception(ServerRequestInfo ri) throws ForwardRequest
    {

    }

    public void send_other(ServerRequestInfo ri) throws ForwardRequest
    {

    }

    public void send_reply(ServerRequestInfo ri)
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
