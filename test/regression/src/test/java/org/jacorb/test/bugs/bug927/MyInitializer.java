package org.jacorb.test.bugs.bug927;

import org.omg.IOP.Codec;
import org.omg.IOP.ENCODING_CDR_ENCAPS;
import org.omg.IOP.Encoding;
import org.omg.PortableInterceptor.ORBInitInfo;
import org.omg.PortableInterceptor.ORBInitializer;

public class MyInitializer extends org.omg.CORBA.LocalObject implements ORBInitializer
{
    public static int slot_id = -1;

    public void pre_init(ORBInitInfo info)
    {
    }

    public void post_init(ORBInitInfo info)
    {
        System.out.println("tid="+Thread.currentThread().getName()+","+"MyInitializer.post_init");
        try
        {
            slot_id = info.allocate_slot_id();

            Encoding encoding = new Encoding(ENCODING_CDR_ENCAPS.value,
                                             (byte) 1, (byte) 0);
            Codec codec = info.codec_factory().create_codec(encoding);

            MyInterceptor interceptor = new MyInterceptor(slot_id, codec);
            info.add_client_request_interceptor(interceptor);
            info.add_server_request_interceptor(interceptor);
        }
        catch( Exception e )
        {
            e.printStackTrace();
        }
    }
}// MyInitializer
