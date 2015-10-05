package org.jacorb.test.bugs.bug1018;

import org.jacorb.orb.portableInterceptor.ORBInitInfoImpl;
import org.omg.CORBA.INITIALIZE;
import org.omg.CORBA.LocalObject;
import org.omg.PortableInterceptor.ORBInitInfo;
import org.omg.PortableInterceptor.ORBInitializer;
import org.omg.PortableInterceptor.ORBInitInfoPackage.DuplicateName;

public final class Initializer extends LocalObject implements ORBInitializer
{
    @Override
    public void pre_init(ORBInitInfo info)
    {
        ORBInitInfoImpl infoImpl = (ORBInitInfoImpl) info;
        int slotId = info.allocate_slot_id();
        try
        {
          info.add_server_request_interceptor(new ServerRequestInterceptorImpl(
                    "ServerRequestInterceptor", infoImpl.getORB(), slotId));
        }
        catch (DuplicateName e)
        {
            String message = "Unexpected error registering interceptors";
            throw new INITIALIZE(message);
        }
    }

    @Override
    public void post_init(ORBInitInfo info)
    {

    }
}
