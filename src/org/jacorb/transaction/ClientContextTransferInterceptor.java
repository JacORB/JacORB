package org.jacorb.transaction;

import org.omg.PortableInterceptor.*;
import org.omg.IOP_N.Codec;
import org.omg.IOP.ServiceContext;
import org.omg.IOP.TransactionService;

/**
 * This interceptor adds a service context with
 * the transactions propagation context to the
 * outgoing message.
 *
 * @author Nicolas Noffke
 * @version $Id$
 */

public class ClientContextTransferInterceptor
    extends org.jacorb.orb.LocalityConstrainedObject 
    implements ClientRequestInterceptor
{

    private int slot_id = -1;
    private Codec codec = null;

    public ClientContextTransferInterceptor(int slot_id, Codec codec) 
    {
        this.slot_id = slot_id;
        this.codec = codec;
    }

    // implementation of org.omg.PortableInterceptor.InterceptorOperations interface

    public String name() 
    {
        return "ClientContextTransferInterceptor";
    }

    /**
     * Add the propagation context to the outgoing message
     */

    public void send_request( ClientRequestInfo ri ) 
        throws ForwardRequest
    {
        try
        {
            org.omg.CORBA.Any any = ri.get_slot(slot_id);
      
            if (! (any.type().kind().value() == org.omg.CORBA.TCKind._tk_null))
            {
                ServiceContext ctx = new ServiceContext(TransactionService.value,
                                                        codec.encode(any));

                ri.add_request_service_context(ctx, false);
                org.jacorb.util.Debug.output(2, "Set Transaction Context");
            }
        }
        catch (Exception e)
        {
            org.jacorb.util.Debug.output(2, e);
        }
    }

    public void send_poll(ClientRequestInfo ri){
    }

    public void receive_reply(ClientRequestInfo ri){
    }

    public void receive_exception(ClientRequestInfo ri) 
        throws ForwardRequest{
    }

    public void receive_other(ClientRequestInfo ri) 
        throws ForwardRequest{
    }
} // ClientContextTransferInterceptor
