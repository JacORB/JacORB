package org.jacorb.test.bugs.bugjac660;

import org.omg.CORBA.INTERNAL;
import org.omg.IOP.Codec;
import org.omg.IOP.ServiceContext;
import org.omg.PortableInterceptor.ForwardRequest;
import org.omg.PortableInterceptor.ServerRequestInfo;
import org.omg.PortableInterceptor.ServerRequestInterceptor;
import org.slf4j.Logger;
import org.jacorb.orb.ORB;

public class ServerInterceptor
    extends org.omg.CORBA.LocalObject
    implements ServerRequestInterceptor
{
    private int slot_id = -1;
    private Codec codec = null;
    private ORB orb = null;
    private Logger logger;

    public ServerInterceptor(ORB orb, int slot_id, Codec codec)
    {
        this.slot_id = slot_id;
        this.codec = codec;
        this.orb = orb;
        logger = orb.getConfiguration ().getLogger("org.jacorb.test");
    }

    // implementation of org.omg.PortableInterceptor.InterceptorOperations interface
    public String name()
    {
        return "ServerInterceptor";
    }

    public void destroy()
    {
        logger.debug("[" + Thread.currentThread() + "] ServerInterceptor: destroy()");
    }

    public void receive_request_service_contexts(ServerRequestInfo ri)
        throws ForwardRequest
    {
        logger.debug("[" + Thread.currentThread() + "] ServerInterceptor: receive_request_service_contexts()");

        try
        {
            ServiceContext ctx =
                ri.get_request_service_context (4711);

            ri.set_slot( slot_id, codec.decode( ctx.context_data ));

            logger.debug("[" + Thread.currentThread()
                               + "] ServerInterceptor: receive_request_service_contexts() - set_slot() to "
                               + codec.decode( ctx.context_data ));
        }
        catch (Exception e)
        {
            throw new INTERNAL (e.getMessage());
        }
    }

    public void receive_request(ServerRequestInfo ri)
        throws ForwardRequest{
        logger.debug("[" + Thread.currentThread() + "] ServerInterceptor: receive_request()");
    }

    public void send_reply(ServerRequestInfo ri){
        logger.debug("[" + Thread.currentThread() + "] ServerInterceptor: send_reply()");

        try
        {
            org.omg.CORBA.Any any = orb.create_any();

            any.insert_string( "This is a test BBB" );
            ri.set_slot( slot_id, any);
            logger.debug("[" + Thread.currentThread()
                               + "] ServerInterceptor : send_reply() - Set_slot() to " + any);
        }
        catch (Exception e)
        {
            throw new INTERNAL (e.getMessage());
        }
    }

    public void send_exception(ServerRequestInfo ri)
        throws ForwardRequest{
        logger.debug("[" + Thread.currentThread() + "] ServerInterceptor: send_exception()");
    }

    public void send_other(ServerRequestInfo ri)
        throws ForwardRequest{
        logger.debug("[" + Thread.currentThread() + "] ServerInterceptor: send_other()");
    }

}// ServerInterceptor
