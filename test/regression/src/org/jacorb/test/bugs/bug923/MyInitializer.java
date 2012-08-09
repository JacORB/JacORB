package org.jacorb.test.bugs.bug923;

import org.omg.PortableInterceptor.ORBInitInfo;
import org.omg.PortableInterceptor.ORBInitializer;

/**
 * This class registers the MyInterceptor
 * with the ORB.
 *
 * @author Nicolas Noffke
 * @version
 */

public class MyInitializer
    extends org.omg.CORBA.LocalObject
    implements ORBInitializer
{

    public MyInitializer() {
    }

    public void post_init(ORBInitInfo info)
    {
        System.out.println("-- post_init, orb_id=" + info.orb_id() + "--");
        try
        {
            MyInterceptor interceptor = new MyInterceptor();
            info.add_client_request_interceptor(interceptor);
            info.add_server_request_interceptor(interceptor);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void pre_init(ORBInitInfo info) {
        System.out.println("-- pre_init, orb_id=" + info.orb_id() + "--");
    }
} // MyInitializer
