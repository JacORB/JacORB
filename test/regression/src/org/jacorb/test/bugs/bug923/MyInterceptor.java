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
       System.out.println("tid="+Thread.currentThread().getName()+","+"send_request " + ri.operation());
    }

    public void send_poll(ClientRequestInfo ri)
    {
System.out.println("tid="+Thread.currentThread().getName()+","+"send_poll " + ri.operation());
    }

    public void receive_reply(ClientRequestInfo ri)
    {
System.out.println("tid="+Thread.currentThread().getName()+","+"receive_reply " + ri.operation());
    }

    public void receive_exception(ClientRequestInfo ri) throws ForwardRequest
    {
System.out.println("tid="+Thread.currentThread().getName()+","+"receive_exception " + ri.operation());
    }

    public void receive_other(ClientRequestInfo ri) throws ForwardRequest
    {
System.out.println("tid="+Thread.currentThread().getName()+","+"receive_other " + ri.operation());
    }

// ////////////////////////
// ServerRequestInterceptor
// ////////////////////////

    public void receive_request_service_contexts(ServerRequestInfo ri)
    {
        System.out.println("tid="+Thread.currentThread().getName()+","+"receive_request_service_contexts " + ri.operation());
    }

    public void receive_request(ServerRequestInfo ri)
    {
System.out.println("tid="+Thread.currentThread().getName()+","+"receive_request " + ri.operation());
    }

    public void send_reply(ServerRequestInfo ri)
    {
System.out.println("tid="+Thread.currentThread().getName()+","+"send_reply " + ri.operation());
    }

    public void send_exception(ServerRequestInfo ri)
    {
System.out.println("tid="+Thread.currentThread().getName()+","+"send_exception " + ri.operation());
    }


    public void send_other(ServerRequestInfo ri)
    {
System.out.println("tid="+Thread.currentThread().getName()+","+"send_other " + ri.operation());
    }

} // MyInterceptor
