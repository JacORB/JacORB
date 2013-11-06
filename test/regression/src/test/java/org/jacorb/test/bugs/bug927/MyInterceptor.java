package org.jacorb.test.bugs.bug927;

import org.omg.CORBA.Any;
import org.omg.IOP.Codec;
import org.omg.IOP.ServiceContext;
import org.omg.PortableInterceptor.ClientRequestInfo;
import org.omg.PortableInterceptor.ClientRequestInterceptor;
import org.omg.PortableInterceptor.ForwardRequest;
import org.omg.PortableInterceptor.ServerRequestInfo;
import org.omg.PortableInterceptor.ServerRequestInterceptor;

public class MyInterceptor
    extends org.omg.CORBA.LocalObject
    implements ClientRequestInterceptor, ServerRequestInterceptor
{
    public static final int SERVICE_ID = 100003;
    private int slot_id;
    private Codec codec;

    public MyInterceptor(int slot_id, Codec codec)
    {
        this.slot_id = slot_id;
        this.codec = codec;
    }

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
        try
        {
            org.omg.CORBA.Any any = ri.get_slot( slot_id );

            if( any.type().kind().value() == org.omg.CORBA.TCKind._tk_null ) {
                System.out.println("tid="+Thread.currentThread().getName()+","+"ClientInterceptor.send_request, slot is empty");
            } else {
                System.out.println("tid="+Thread.currentThread().getName()+","+"ClientInterceptor.send_request, adding ServiceContext");

                ServiceContext ctx =
                    new ServiceContext(SERVICE_ID, codec.encode( any ));

                ri.add_request_service_context( ctx, false );
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
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
        ServiceContext ctx;
        try {
           ctx = ri.get_request_service_context(SERVICE_ID);
        } catch (org.omg.CORBA.BAD_PARAM e) {
           System.out.println("tid="+Thread.currentThread().getName()+","+"**Service context was not specified");
           return;
        }

        if (null == ctx) {
           System.out.println("tid="+Thread.currentThread().getName()+","+"**Service context is null");
           return;
        }

        try
        {
            Any slotDataAsAny = codec.decode( ctx.context_data );

            // Get the slot data as a string
            String slotDataAsStr;
            if (null == (slotDataAsStr = slotDataAsAny.extract_string())) {
               System.out.println("slotDataAsStr=<null>");
            } else {
               System.out.println("slotDataAsStr=" + slotDataAsStr);
            }

            slotDataAsStr += ":receive_request_service_contexts";

            slotDataAsAny.insert_string(slotDataAsStr);

            ri.set_slot( slot_id, slotDataAsAny);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void receive_request(ServerRequestInfo ri)
    {
System.out.println("tid="+Thread.currentThread().getName()+","+"receive_request " + ri.operation());
       addStringToSlotId("receive_request:"+ri.operation(), ri);
    }

    public void send_reply(ServerRequestInfo ri)
    {
System.out.println("tid="+Thread.currentThread().getName()+","+"send_reply " + ri.operation());
       addStringToSlotId("send_reply", ri);
    }

    public void send_exception(ServerRequestInfo ri)
    {
System.out.println("tid="+Thread.currentThread().getName()+","+"send_exception " + ri.operation());
       addStringToSlotId("send_exception", ri);
    }


    public void send_other(ServerRequestInfo ri)
    {
System.out.println("tid="+Thread.currentThread().getName()+","+"send_other " + ri.operation());
       addStringToSlotId("send_other", ri);
    }

   private void addStringToSlotId(String methodName, ServerRequestInfo ri)
   {
        try
        {
            Any slotDataAsAny = ri.get_slot(slot_id);

            // Get the slot data as a string
            String s = null;
            String slotDataAsStr = "<no_slot_data>";
            if( slotDataAsAny.type().kind().value() != org.omg.CORBA.TCKind._tk_null
                && null != (s = slotDataAsAny.extract_string())) {
               slotDataAsStr = s;
            }

            slotDataAsStr += ":" + methodName;

            slotDataAsAny.insert_string(slotDataAsStr);

            ri.set_slot( slot_id, slotDataAsAny);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
   }

} // MyInterceptor
