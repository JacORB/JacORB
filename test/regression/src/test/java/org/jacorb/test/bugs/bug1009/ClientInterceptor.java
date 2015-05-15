package org.jacorb.test.bugs.bug1009;

import org.omg.PortableInterceptor.ClientRequestInfo;
import org.omg.PortableInterceptor.ClientRequestInterceptor;

class ClientInterceptor extends org.omg.CORBA.LocalObject implements
        ClientRequestInterceptor
{
    public static int count;

    public ClientInterceptor()
    {
        count = 0;
    }

    public String name()
    {
        return "ipc.ClientInterceptor";
    }

    public void destroy()
    {
    }

    public void send_request(ClientRequestInfo ri)
    {
    }

    public void send_poll(ClientRequestInfo ri)
    {
    }

    public void receive_reply(ClientRequestInfo ri)
    {
    }

    public void receive_other(ClientRequestInfo ri)
    {
    }

    public void receive_exception(ClientRequestInfo ri)
            throws org.omg.PortableInterceptor.ForwardRequest
    {
        System.out.println("### receive_exception " + count);
        if (!Bug1009Test.ready)
        {
            return;
        }

        System.out.println("receive_exception " + ri.received_exception_id());
        System.out.println("receive_exception forward " + ri.forward_reference());
        if (ri.forward_reference() != null)
        {
            System.out.println("receive_exception forward " + ri.forward_reference());
        }

        if (count > 0)
        {
            System.out
                    .println("receive_exception " + ri.received_exception_id() + " count = " + count + " just return");
            return;
        }
        ++count;

        System.out.println(
                "receive_exception " + ri.received_exception_id() + " count = " + count + " throw ForwardRequest");
        throw new org.omg.PortableInterceptor.ForwardRequest(Bug1009Test.object);
    }
}
