package org.jacorb.test.bugs.bug1009;

import org.omg.PortableInterceptor.ORBInitInfo;
import org.omg.PortableInterceptor.ORBInitializer;


public class ClientInitializer
    extends org.omg.CORBA.LocalObject
    implements ORBInitializer
{

    public ClientInitializer() {
    }

    public void post_init(ORBInitInfo info)
    {
        try
        {
            info.add_client_request_interceptor(new ClientInterceptor());
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void pre_init(ORBInitInfo info) {
    }
}