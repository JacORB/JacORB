package org.jacorb.proxy;

import org.omg.PortableInterceptor.*;
import org.omg.CosNaming.*;
import org.jacorb.orb.*;
import org.jacorb.util.Debug;
import org.omg.IOP.*;

/**
 * This class registers the ClientForwardInterceptor
 * with the ORB.
 *
 * @author Nicolas Noffke, Sebastian Müller
 * @version
 */

public class ProxyServerInitializer
    extends org.omg.CORBA.LocalObject
    implements ORBInitializer
{
    public static int slot_id = -1;

    public ProxyServerInitializer () 
    {
    }

    /**
     * This method registers the server proxy interceptor.
     */

    public void post_init (ORBInitInfo info)
    {
        Encoding encoding;
        Codec codec;

        try
        {
            encoding = new Encoding (ENCODING_CDR_ENCAPS.value, (byte) 1, (byte) 0);
            codec = info.codec_factory().create_codec (encoding);
            slot_id = info.allocate_slot_id ();

            info.add_server_request_interceptor
                (new ProxyServerForwardInterceptor (info, codec, slot_id));
        }
        catch (org.omg.PortableInterceptor.ORBInitInfoPackage.DuplicateName ex)
        {
            Debug.output (1, "Duplicate server interceptor name");
        }
        catch (Exception ex)
        {
            Debug.output (1, ex);
        }
    }

    public void pre_init (ORBInitInfo info)
    {
    }
}
