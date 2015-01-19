package org.jacorb.test.bugs.bug940;

import org.jacorb.orb.portableInterceptor.ORBInitInfoImpl;
import org.omg.CORBA.INITIALIZE;
import org.omg.CORBA.LocalObject;
import org.omg.PortableInterceptor.ORBInitInfo;
import org.omg.PortableInterceptor.ORBInitializer;
import org.omg.PortableInterceptor.ORBInitInfoPackage.DuplicateName;

public final class Initializer extends LocalObject implements ORBInitializer
{
    static ClientRequestInterceptorImpl ci;

    @Override
    public void pre_init(ORBInitInfo info)
    {
        ORBInitInfoImpl infoImpl = (ORBInitInfoImpl) info;
        try
        {
            ci = new ClientRequestInterceptorImpl("ClientRequestInterceptor", infoImpl.getORB());

            info.add_client_request_interceptor( ci );
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
