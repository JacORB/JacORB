package org.jacorb.test.bugs.bugjac562;

import org.omg.CORBA.LocalObject;
import org.omg.PortableInterceptor.ForwardRequest;
import org.omg.PortableInterceptor.ServerRequestInfo;
import org.omg.PortableInterceptor.ServerRequestInterceptor;

public class ServerInterceptor extends LocalObject implements
        ServerRequestInterceptor
{
    public void receive_request(ServerRequestInfo ri) throws ForwardRequest
    {
        throw new RuntimeException();
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
        return "MyName";
    }
}
