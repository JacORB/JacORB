package org.jacorb.test.bugs.bugjac660;

import org.omg.CORBA.INTERNAL;
import org.omg.IOP.Codec;
import org.omg.IOP.ServiceContext;
import org.omg.PortableInterceptor.ClientRequestInfo;
import org.omg.PortableInterceptor.ClientRequestInterceptor;
import org.omg.PortableInterceptor.ForwardRequest;


public class ClientInterceptor
    extends org.omg.CORBA.LocalObject
    implements ClientRequestInterceptor
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
        System.out.println("[" + Thread.currentThread() + "] ClientInterceptor: destroy()");
    }

    public void send_request( ClientRequestInfo ri )
        throws ForwardRequest
    {
        System.out.println("[" + Thread.currentThread() + "] ClientInterceptor: send_request()");

        try
        {
            org.omg.CORBA.Any any = ri.get_slot( slot_id );

            System.out.println("[" + Thread.currentThread()
                               + "] ClientInterceptor: send_request() - get_slot() = "
                               + any);

            if (any.type().kind().value() != org.omg.CORBA.TCKind._tk_null)
            {
                ServiceContext ctx =
                    new ServiceContext(4711, codec.encode( any ));

                ri.add_request_service_context( ctx, false );
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new INTERNAL (e.getMessage());
        }
    }

    public void send_poll(ClientRequestInfo ri)
    {
        System.out.println("[" + Thread.currentThread() + "] ClientInterceptor: send_poll()");
    }

    public void receive_reply(ClientRequestInfo ri)
    {
        System.out.println("[" + Thread.currentThread() + "] ClientInterceptor: receive_reply()");

        try
        {
            org.omg.CORBA.Any any = ri.get_slot( slot_id );

            if (any == null)
            {
                System.out.println ("Slot null");
                throw new INTERNAL ("Any slot was unexpectedly null");
            }

            String result = any.extract_string();

            if (! result.equals ("This is a test AAA"))
            {
                throw new Exception ("Did not receive correct message : got <"
                                     + result + "> and expected <This is a test AAA>");
            }

            System.out.println ("[" + Thread.currentThread()
                                + "] ClientInterceptor: receive_reply() - get_slot() = "
                                + any);
        }
        catch (Exception e)
        {
           e.printStackTrace();
           throw new INTERNAL (e.getMessage());
        }
    }

    public void receive_exception(ClientRequestInfo ri)
        throws ForwardRequest
    {
        System.out.println("[" + Thread.currentThread()
                           + "] ClientInterceptor: receive_exception()");
    }

    public void receive_other(ClientRequestInfo ri)
        throws ForwardRequest
    {
        System.out.println("[" + Thread.currentThread() + "] ClientInterceptor: receive_other()");
    }
}// ClientInterceptor
