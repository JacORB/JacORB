package org.jacorb.test.bugs.bugjac660;

import org.omg.CORBA.INTERNAL;
import org.omg.IOP.Codec;
import org.omg.IOP.ENCODING_CDR_ENCAPS;
import org.omg.IOP.Encoding;
import org.omg.PortableInterceptor.ORBInitInfo;
import org.omg.PortableInterceptor.ORBInitializer;
import org.jacorb.orb.portableInterceptor.ORBInitInfoImpl;


public class Initializer
    extends org.omg.CORBA.LocalObject
    implements ORBInitializer
{
    public static int slot_id = -1;

    public Initializer ()
    {
    }

    public void pre_init (ORBInitInfo info)
    {
    }

    public void post_init (ORBInitInfo info)
    {
        try
        {
            slot_id = info.allocate_slot_id();

            Encoding encoding = new Encoding(ENCODING_CDR_ENCAPS.value,
                                             (byte) 1, (byte) 0);


            Codec codec = info.codec_factory().create_codec(encoding);

            info.add_server_request_interceptor( new ServerInterceptor( ((ORBInitInfoImpl)info).getORB(), slot_id, codec ));
            info.add_client_request_interceptor( new ClientInterceptor( ((ORBInitInfoImpl)info).getORB(), slot_id, codec ));
        }
        catch( Exception e )
        {
            throw new INTERNAL (e.getMessage());
        }
    }
}// ServerInitializer
