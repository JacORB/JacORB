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

public class ProxyClientInitializer
    extends org.omg.CORBA.LocalObject
    implements ORBInitializer
{
    public ProxyClientInitializer ()
    {
    }

    /**
     * This method registers the client proxy interceptor.
     */

    public void post_init (ORBInitInfo info)
    {
        try
        {
            info.add_client_request_interceptor (new ProxyClientForwardInterceptor (info));
        }
        catch (org.omg.PortableInterceptor.ORBInitInfoPackage.DuplicateName ex)
        {
            Debug.output (1, "Duplicate client interceptor name");
        }
    }

    public void pre_init (ORBInitInfo info)
    {
    }
}
