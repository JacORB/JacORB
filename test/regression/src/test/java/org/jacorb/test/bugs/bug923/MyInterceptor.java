package org.jacorb.test.bugs.bug923;

import org.omg.PortableInterceptor.ClientRequestInfo;
import org.omg.PortableInterceptor.ClientRequestInterceptor;
import org.omg.PortableInterceptor.ForwardRequest;
import org.omg.PortableInterceptor.ServerRequestInfo;
import org.omg.PortableInterceptor.ServerRequestInterceptor;

public class MyInterceptor
    extends org.omg.CORBA.LocalObject
    implements ClientRequestInterceptor, ServerRequestInterceptor
{
    public String name()
    {
        return "MyInterceptor";
    }

    public void destroy()
    {
    }

    public void send_request(ClientRequestInfo ri)
        throws ForwardRequest
    {
    }

    public void send_poll(ClientRequestInfo ri)
    {
    }

    public void receive_reply(ClientRequestInfo ri)
    {
    }

    public void receive_exception(ClientRequestInfo ri) throws ForwardRequest
    {
    }

    public void receive_other(ClientRequestInfo ri) throws ForwardRequest
    {
    }

    public void receive_request_service_contexts(ServerRequestInfo ri)
    {
    }

    public void receive_request(ServerRequestInfo ri)
    {
    }

    public void send_reply(ServerRequestInfo ri)
    {
    }

    public void send_exception(ServerRequestInfo ri)
    {
    }


    public void send_other(ServerRequestInfo ri)
    {
    }

} // MyInterceptor
