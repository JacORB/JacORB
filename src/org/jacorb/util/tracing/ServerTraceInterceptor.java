package org.jacorb.util.tracing;

import java.io.*;

import org.omg.IOP_N.Codec;
import org.omg.CORBA.*;
import org.omg.CORBA.portable.*;
import org.omg.PortableInterceptor.*;

import org.jacorb.orb.LocalityConstrainedObject;

public class ServerTraceInterceptor 
    extends LocalityConstrainedObject 
    implements ServerRequestInterceptor
{


    private int slot_id;
    private PrintStream logStream = null;
    private Codec codec = null;

    public ServerTraceInterceptor( int slot_id, Codec codec )
    {
        this(slot_id, codec, System.out);
    }

    public ServerTraceInterceptor( int slot_id, Codec codec, 
                                   PrintStream logStream )
    {
        this.slot_id = slot_id;
        this.codec = codec;
        this.logStream = logStream;        
    }

    public String name() 
    {
        return "ServerTraceInterceptor";
    }

    public void receive_request_service_contexts( ServerRequestInfo ri ) 
        throws org.omg.PortableInterceptor.ForwardRequest
    {
        System.out.println("SI in operation <" + ri.operation() + ">");
        try
        {
            System.out.println("Request for op " + ri.operation());
            
            org.omg.IOP.ServiceContext ctx = 
                ri.get_request_service_context( TracingContextID.value );
            
            ri.set_slot(slot_id, codec.decode(ctx.context_data));

            ri.add_reply_service_context( ctx, true );
        }
        catch (org.omg.CORBA.BAD_PARAM bp)
        {
            //ignore, SC not present

            System.out.println("ServerRequestInterceptor: " + bp);
 
        }
        catch( Exception e )
        {
            System.err.println("No service context in operation <" + ri.operation() + ">");
            e.printStackTrace();
        }
    }

    public void receive_request( ServerRequestInfo ri ) 
        throws org.omg.PortableInterceptor.ForwardRequest
    {
    }

    public void send_reply(org.omg.PortableInterceptor.ServerRequestInfo ri)
    {

    }

    public void send_exception(org.omg.PortableInterceptor.ServerRequestInfo ri) 
        throws org.omg.PortableInterceptor.ForwardRequest
    {

    }

    public void send_other(org.omg.PortableInterceptor.ServerRequestInfo ri) 
        throws org.omg.PortableInterceptor.ForwardRequest
    {

    }


}






