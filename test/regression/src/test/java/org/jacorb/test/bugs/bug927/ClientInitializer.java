package org.jacorb.test.bugs.bug927;

import org.omg.IOP.Codec;
import org.omg.IOP.ENCODING_CDR_ENCAPS;
import org.omg.IOP.Encoding;
import org.omg.PortableInterceptor.ORBInitInfo;
import org.omg.PortableInterceptor.ORBInitializer;
import org.omg.CORBA.INTERNAL;

/**
 * ClientInitializer.java
 *
 *
 * Created: Fri Oct 26 10:58:29 2001
 *
 * @author Nicolas Noffke
 */

public class ClientInitializer extends org.omg.CORBA.LocalObject implements ORBInitializer
{
    public static int slot_id = -1;

    public ClientInitializer()
    {
    }

    public void pre_init(ORBInitInfo info)
    {
    }

    public void post_init(ORBInitInfo info)
    {
        try
        {
            slot_id = info.allocate_slot_id();

            Encoding encoding = new Encoding(ENCODING_CDR_ENCAPS.value,
                                             (byte) 1, (byte) 0);
            Codec codec = info.codec_factory().create_codec(encoding);

            info.add_client_request_interceptor( new ClientInterceptor( slot_id, codec ));
        }
        catch( Exception e )
        {
            throw new INTERNAL ("Caught " + e);
        }
    }
}// ClientInitializer
