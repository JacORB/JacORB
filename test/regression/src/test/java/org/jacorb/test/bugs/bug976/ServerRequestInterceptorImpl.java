package org.jacorb.test.bugs.bug976;

import org.jacorb.orb.ORB;
import org.omg.CORBA.INTERNAL;
import org.omg.CORBA.LocalObject;
import org.omg.CORBA.NO_PERMISSION;
import org.omg.PortableInterceptor.ForwardRequest;
import org.omg.PortableInterceptor.ServerRequestInfo;
import org.omg.PortableInterceptor.ServerRequestInterceptor;

final public class ServerRequestInterceptorImpl extends LocalObject implements
        ServerRequestInterceptor
{
    private String name;

    public ServerRequestInterceptorImpl(String string, ORB orb)
    {
        this.name = string;
    }

    public void receive_request(ServerRequestInfo ri) throws ForwardRequest
    {
        String methodName;

        // Just in case someone changes the name of the test make it fail.
        try
        {
            methodName = (Bug976Test.class.getDeclaredMethod("testSlotReceiveException",
                    (Class<?>[]) null).getName());
        }
        catch (SecurityException e)
        {
            throw new INTERNAL("Problem retrieving method name" + e);
        }
        catch (NoSuchMethodException e)
        {
            throw new INTERNAL("Problem retrieving method name" + e);
        }
        if (Bug976Test.testName.equals(methodName))
        {
            // throw exception to force calling client's interceptor
            // receive_exception
            throw new NO_PERMISSION("my test forces NO_PERMISSION");
        }
    }

    public void receive_request_service_contexts(ServerRequestInfo ri)
            throws ForwardRequest
    {

    }

    public void send_exception(ServerRequestInfo ri) throws ForwardRequest
    {

    }

    public void send_other(ServerRequestInfo ri) throws ForwardRequest
    {

    }

    public void send_reply(ServerRequestInfo ri)
    {

    }

    public void destroy()
    {

    }

    public String name()
    {
        return this.name;
    }
}
