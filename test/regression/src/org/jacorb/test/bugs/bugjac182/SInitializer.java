package org.jacorb.test.bugs.bugjac182;

import org.jacorb.orb.portableInterceptor.ORBInitInfoImpl;
import org.omg.PortableInterceptor.ORBInitInfo;
import org.omg.PortableInterceptor.ORBInitInfoPackage.DuplicateName;
import org.omg.PortableInterceptor.ORBInitializer;

/**
 * <code>SInitializer</code> is basic initializer to register the interceptor.
 *
 * @author Nick Cross
 * @version $Id$
 */
public class SInitializer
    extends org.omg.CORBA.LocalObject
    implements ORBInitializer
{
    public static int slotID = -1;

    /**
     * This method registers the interceptors.
     * @param info an <code>ORBInitInfo</code> value
     */
    public void post_init( ORBInitInfo info )
    {
        try
        {
            slotID = info.allocate_slot_id();

            info.add_server_request_interceptor(new SInterceptor(((ORBInitInfoImpl)info).getORB()));
        }
        catch (DuplicateName e)
        {
            e.printStackTrace();
        }
    }

    /**
     * <code>pre_init</code> does nothing..
     *
     * @param info an <code>ORBInitInfo</code> value
     */
    public void pre_init(ORBInitInfo info)
    {
    }
}