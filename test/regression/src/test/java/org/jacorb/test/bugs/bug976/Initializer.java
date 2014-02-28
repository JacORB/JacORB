package org.jacorb.test.bugs.bug976;

import org.jacorb.orb.portableInterceptor.ORBInitInfoImpl;
import org.omg.CORBA.INITIALIZE;
import org.omg.CORBA.LocalObject;
import org.omg.PortableInterceptor.ORBInitInfo;
import org.omg.PortableInterceptor.ORBInitializer;
import org.omg.PortableInterceptor.ORBInitInfoPackage.DuplicateName;

public final class Initializer extends LocalObject implements ORBInitializer
{

    private int slot;

    public void pre_init(ORBInitInfo info)
    {
        slot = info.allocate_slot_id();
    }

    public void post_init(ORBInitInfo info)
    {
        ORBInitInfoImpl infoImpl = (ORBInitInfoImpl) info;
        try
        {
            info.add_client_request_interceptor(new ClientRequestInterceptorImpl(
                    "ClientRequestInterceptor", infoImpl.getORB(), slot));
            info.add_server_request_interceptor(new ServerRequestInterceptorImpl(
                    "ServerRequestInterceptor", infoImpl.getORB()));
        }
        catch (DuplicateName e)
        {
            String message = "Unexpected error registering interceptors";
            throw new INITIALIZE(message);
        }
    }
}
