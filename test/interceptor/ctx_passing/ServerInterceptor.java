package test.interceptor.ctx_passing;

import org.omg.PortableInterceptor.*;
import org.omg.IOP.Codec;
import org.omg.IOP.ServiceContext;

/**
 * ServerInterceptor.java
 *
 *
 * Created: Fri Oct 26 11:17:02 2001
 *
 * @author Nicolas Noffke
 * @version $Id$
 */

public class ServerInterceptor 
    extends org.jacorb.orb.LocalityConstrainedObject 
    implements ServerRequestInterceptor
{
    private int slot_id = -1;
    private Codec codec = null;

    public ServerInterceptor(int slot_id, Codec codec)
    {
        this.slot_id = slot_id;
        this.codec = codec;       
    }

    // implementation of org.omg.PortableInterceptor.InterceptorOperations interface
    public String name() 
    {
        return "ServerInterceptor";
    }

    public void destroy()
    {
    }

    public void receive_request_service_contexts(ServerRequestInfo ri) 
        throws ForwardRequest
    {
        System.out.println("ServerInterceptor: receive_request_service_contexts()");
        
        try
        {
            ServiceContext ctx = 
                ri.get_request_service_context( 4711 );

            ri.set_slot( slot_id, codec.decode( ctx.context_data ));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void receive_request(ServerRequestInfo ri)
        throws ForwardRequest{
    }

    public void send_reply(ServerRequestInfo ri){
    }

    public void send_exception(ServerRequestInfo ri)
        throws ForwardRequest{
    }

    public void send_other(ServerRequestInfo ri) 
        throws ForwardRequest{
    }

}// ServerInterceptor


