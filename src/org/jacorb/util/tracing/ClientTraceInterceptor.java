package org.jacorb.util.tracing;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2004 Gerald Brose
 *
 *   This library is free software; you can redistribute it and/or
 *   modify it under the terms of the GNU Library General Public
 *   License as published by the Free Software Foundation; either
 *   version 2 of the License, or (at your option) any later version.
 *
 *   This library is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *   Library General Public License for more details.
 *
 *   You should have received a copy of the GNU Library General Public
 *   License along with this library; if not, write to the Free
 *   Software Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *
 */

import org.jacorb.orb.portableInterceptor.ClientRequestInfoImpl;
import org.jacorb.orb.portableInterceptor.RecursionAwareCI;
import org.omg.CORBA.Any;
import org.omg.CORBA.TCKind;
import org.omg.IOP.Codec;
import org.omg.IOP.ServiceContext;
import org.omg.PortableInterceptor.ClientRequestInfo;
import org.omg.PortableInterceptor.ForwardRequest;

public class ClientTraceInterceptor
    extends RecursionAwareCI
{
    private TracingService tracer;
    private int myTraceId;
    private Codec codec;

    private Request current_request = null;

    private int slot_id;
    private Timer timer;

    public ClientTraceInterceptor(Codec codec, int slot_id,
                                  TracingService tracer)
    {
        super(true);

        this.tracer = tracer;
        this.codec = codec;
        this.slot_id = slot_id;

        myTraceId = tracer.get_id();
        timer = new Timer();

        System.out.println(" ********************************** ");
        System.out.println("          My id: " + myTraceId);
        System.out.println(" ********************************** ");
    }

    // implementation InterceptorOperations interface
    public String name()
    {
        return "ClientTraceInterceptor";
    }

    public void destroy()
    {
    }

    /**
     * Add the propagation context to the outgoing message
     */

    public void do_send_request( ClientRequestInfo ri )
        throws ForwardRequest
    {
        try
        {
            // only for requests which return
            if( ri.response_expected() )
            {
                System.out.println("request: call to op " + ri.operation());


                current_request = new Request( myTraceId,
                                               ri.effective_target().
                                               hashCode() << 32 |
                                               ((myTraceId & 0xffff) << 16) |
                                               (ri.request_id() & 0xffff));

                Any any = ri.get_slot(slot_id);
                if (any.type().kind().value() != TCKind._tk_null)
                {
                    /*
                       we are not the initiatiator of the call:
                       extract the existing request
                    */

                    Request origin = RequestHelper.extract( any );

                    tracer.registerSubTrace( origin,
                                             current_request );
                }

                /* insert the context data into an any and then encode
                   it into the context */
                Any ctx_any = ((ClientRequestInfoImpl) ri).orb.create_any();
                RequestHelper.insert( ctx_any, current_request );
                ServiceContext context =
                    new ServiceContext ( TracingContextID.value,
                                         codec.encode( ctx_any ) );

                timer.start( ri.request_id(), ri.target() );

                ri.add_request_service_context( context, true );
            }
        }
        catch( Exception e)
    {
            e.printStackTrace();
        }
    }

    public void do_send_poll(ClientRequestInfo ri)
    {
    }

    public void do_receive_reply(ClientRequestInfo ri)
    {
        try
        {

            System.out.println("reply: return from op " + ri.operation());

            long t = timer.stop( ri.request_id(), ri.target());

            tracer.logTraceAtPoint( current_request,
                                    ri.operation(),
                                    t,
                                    0);

            TraceData trace = tracer.getTrace( current_request );

            System.out.println("-- Trace for request " +
                               ri.operation() +
                               " (rid:" + ri.request_id() +
                               ") -- ");

            printTrace( trace, "" );
        }
        catch( Exception e)
    {
            e.printStackTrace();
        }
    }

    public void do_receive_exception(ClientRequestInfo ri)
        throws ForwardRequest
    {
    }

    public void do_receive_other(ClientRequestInfo ri)
        throws ForwardRequest
    {
    }

    private void printTrace( TraceData trace ,
                             String prefix )
    {
        System.out.println(prefix + " Request originator: " +
                           trace.tracer_id);

        System.out.println(prefix + " Operation: " +
                           trace.operation);

        System.out.println(prefix + " Time: "
                           + trace.client_time + " msecs" );

        for( int i = 0; i < trace.subtrace.length; i++ )
        {
            System.out.println(prefix + "\tsubtrace " + i + " >>>");

            printTrace( trace.subtrace[i],
                        prefix + '\t' );

            System.out.println(prefix + "\t<<< subtrace " + i);
        }
    }
}
