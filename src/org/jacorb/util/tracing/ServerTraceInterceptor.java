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

import java.io.PrintStream;
import org.omg.IOP.Codec;
import org.omg.PortableInterceptor.ServerRequestInfo;
import org.omg.PortableInterceptor.ServerRequestInterceptor;

public class ServerTraceInterceptor
    extends org.omg.CORBA.LocalObject
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

    public void destroy()
    {
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
