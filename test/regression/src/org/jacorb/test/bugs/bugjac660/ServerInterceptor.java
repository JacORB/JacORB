package org.jacorb.test.bugs.bugjac660;

import org.omg.CORBA.INTERNAL;
import org.omg.IOP.Codec;
import org.omg.IOP.ServiceContext;
import org.omg.PortableInterceptor.ForwardRequest;
import org.omg.PortableInterceptor.ServerRequestInfo;
import org.omg.PortableInterceptor.ServerRequestInterceptor;

public class ServerInterceptor
    extends org.omg.CORBA.LocalObject
    implements ServerRequestInterceptor
{
    private int slot_id = -1;
    private Codec codec = null;
    private org.omg.CORBA.ORB orb = null;

    public ServerInterceptor(int slot_id, Codec codec)
    {
        this.slot_id = slot_id;
        this.codec = codec;
        orb = org.omg.CORBA.ORB.init();
    }

    // implementation of org.omg.PortableInterceptor.InterceptorOperations interface
    public String name()
    {
        return "ServerInterceptor";
    }

    public void destroy()
    {
        System.out.println("[" + Thread.currentThread() + "] ServerInterceptor: destroy()");
    }

    public void receive_request_service_contexts(ServerRequestInfo ri)
        throws ForwardRequest
    {
        System.out.println("[" + Thread.currentThread() + "] ServerInterceptor: receive_request_service_contexts()");

        try
        {
            ServiceContext ctx =
                ri.get_request_service_context (4711);

            ri.set_slot( slot_id, codec.decode( ctx.context_data ));

            System.out.println("[" + Thread.currentThread()
                               + "] ServerInterceptor: receive_request_service_contexts() - set_slot() to "
                               + codec.decode( ctx.context_data ));
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new INTERNAL (e.getMessage());
        }
    }

    public void receive_request(ServerRequestInfo ri)
        throws ForwardRequest{
        System.out.println("[" + Thread.currentThread() + "] ServerInterceptor: receive_request()");
    }

    public void send_reply(ServerRequestInfo ri){
        System.out.println("[" + Thread.currentThread() + "] ServerInterceptor: send_reply()");

        try
        {
            org.omg.CORBA.Any any = orb.create_any();

            any.insert_string( "This is a test BBB" );
            ri.set_slot( slot_id, any);
            System.out.println("[" + Thread.currentThread()
                               + "] ServerInterceptor : send_reply() - Set_slot() to " + any);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void send_exception(ServerRequestInfo ri)
        throws ForwardRequest{
        System.out.println("[" + Thread.currentThread() + "] ServerInterceptor: send_exception()");
    }

    public void send_other(ServerRequestInfo ri)
        throws ForwardRequest{
        System.out.println("[" + Thread.currentThread() + "] ServerInterceptor: send_other()");
    }

}// ServerInterceptor
