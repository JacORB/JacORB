package org.jacorb.util.tracing;

import org.omg.CosNaming.*;
import org.omg.PortableInterceptor.*;
import org.omg.PortableInterceptor.ORBInitInfoPackage.*;
import org.omg.IOP.*;

import org.omg.CORBA.LocalObject;

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






