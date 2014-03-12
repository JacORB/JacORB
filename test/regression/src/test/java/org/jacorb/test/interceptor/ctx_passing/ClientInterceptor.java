package org.jacorb.test.interceptor.ctx_passing;

import org.omg.IOP.Codec;
import org.omg.IOP.ServiceContext;
import org.omg.PortableInterceptor.ClientRequestInfo;
import org.omg.PortableInterceptor.ClientRequestInterceptor;
import org.omg.PortableInterceptor.ForwardRequest;
import org.omg.CORBA.INTERNAL;

/**
 * ClientInterceptor.java
 *
 *
 * Created: Fri Oct 26 11:04:19 2001
 *
 * @author Nicolas Noffke
 */

public class ClientInterceptor extends org.omg.CORBA.LocalObject implements ClientRequestInterceptor
{
    private int slot_id = -1;
    private Codec codec = null;

    public ClientInterceptor(int slot_id, Codec codec)
    {
        this.slot_id = slot_id;
        this.codec = codec;
    }

    public String name()
    {
        return "ClientInterceptor";
    }

    public void destroy()
    {
    }

    public void send_request( ClientRequestInfo ri )
    throws ForwardRequest
    {
        try
        {
            org.omg.CORBA.Any any = ri.get_slot( slot_id );

            if( any.type().kind().value() != org.omg.CORBA.TCKind._tk_null )
            {
                ServiceContext ctx =
                    new ServiceContext(4711, codec.encode( any ));

                ri.add_request_service_context( ctx, false );
            }
        }
        catch (Exception e)
        {
            throw new INTERNAL ("Caught " + e);
        }
    }

    public void send_poll(ClientRequestInfo ri)
    {
    }

    public void receive_reply(ClientRequestInfo ri)
    {
    }

    public void receive_exception(ClientRequestInfo ri)
    throws ForwardRequest
    {
    }

    public void receive_other(ClientRequestInfo ri)
    throws ForwardRequest
    {
    }
}// ClientInterceptor
