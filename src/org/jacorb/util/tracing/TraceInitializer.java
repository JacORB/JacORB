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

import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.IOP.Codec;
import org.omg.IOP.ENCODING_CDR_ENCAPS;
import org.omg.IOP.Encoding;
import org.omg.PortableInterceptor.ORBInitInfo;
import org.omg.PortableInterceptor.ORBInitializer;

/**
 * This class registers the trace interceptors
 */
public class TraceInitializer
    extends org.omg.CORBA.LocalObject
    implements ORBInitializer
{
    public TraceInitializer()
    {
    }

    /**
     * This method  registers the interceptor.
     */
    public void post_init(ORBInitInfo info)
    {
        try
        {
            int slot_id = info.allocate_slot_id();

            NamingContextExt nc =
                NamingContextExtHelper.narrow(
                     info.resolve_initial_references("NameService"));

            TracingService tracer =
                TracingServiceHelper.narrow(
                    nc.resolve( nc.to_name("tracing.service")));


            if( tracer == null )
            {
                System.err.println("No Tracing Service, cannot register tracer interceptor!");
                return;
            }

            Encoding encoding = new Encoding(ENCODING_CDR_ENCAPS.value,
                                             (byte) 1, (byte) 0);
            Codec codec = info.codec_factory().create_codec(encoding);

            ClientTraceInterceptor interceptor =
                new ClientTraceInterceptor(codec, slot_id , tracer);
            info.add_client_request_interceptor(interceptor);

            info.add_server_request_interceptor( new ServerTraceInterceptor( slot_id,
                                                                             codec));

        }
        catch (Exception  e)
        {
            e.printStackTrace();
        }
    }

    public void pre_init(ORBInitInfo info)
    {
    }

}
