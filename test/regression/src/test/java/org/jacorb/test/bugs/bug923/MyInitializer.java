package org.jacorb.test.bugs.bug923;

import org.omg.PortableInterceptor.ORBInitInfo;
import org.omg.PortableInterceptor.ORBInitializer;
import org.omg.PortableInterceptor.ORBInitInfoPackage.DuplicateName;
import org.omg.CORBA.INTERNAL;

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
        try
        {
            MyInterceptor interceptor = new MyInterceptor();
            info.add_client_request_interceptor(interceptor);
            info.add_server_request_interceptor(interceptor);
        }
        catch (DuplicateName e)
        {
            throw new INTERNAL ("Caught " + e);
        }
    }

    public void pre_init(ORBInitInfo info)
    {
    }
} // MyInitializer
