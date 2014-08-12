package org.jacorb.test.bugs.bug983;

import org.jacorb.orb.ORB;
import org.jacorb.test.bugs.bug979.CredentialContextId;
import org.jacorb.test.harness.TestUtils;
import org.omg.CORBA.BAD_PARAM;
import org.omg.CORBA.CompletionStatus;
import org.omg.CORBA.LocalObject;
import org.omg.CORBA.NO_PERMISSION;
import org.omg.PortableInterceptor.ForwardRequest;
import org.omg.PortableInterceptor.ServerRequestInfo;
import org.omg.PortableInterceptor.ServerRequestInterceptor;

final public class ServerRequestInterceptorImpl extends LocalObject implements
        ServerRequestInterceptor
{

    private String name;

    public ServerRequestInterceptorImpl(String string, ORB orb, int slot)
    {
        this.name = string;
    }

    @Override
    public void receive_request(ServerRequestInfo ri) throws ForwardRequest
    {
        TestUtils.getLogger().debug("receive request: " + ri.operation());

        try
        {

            ri.get_request_service_context(CredentialContextId.value);
        }
        catch (BAD_PARAM e)
        {
            throw new NO_PERMISSION(0, CompletionStatus.COMPLETED_NO);
        }
    }

    @Override
    public void receive_request_service_contexts(ServerRequestInfo ri)
            throws ForwardRequest
    {

    }

    @Override
    public void send_exception(ServerRequestInfo ri) throws ForwardRequest
    {
        TestUtils.getLogger().debug("send exception: " + ri.operation());
    }

    @Override
    public void send_other(ServerRequestInfo ri) throws ForwardRequest
    {

    }

    @Override
    public void send_reply(ServerRequestInfo ri)
    {
        TestUtils.getLogger().debug("send reply: " + ri.operation());
    }

    @Override
    public void destroy()
    {

    }

    @Override
    public String name()
    {
        return this.name;
    }
}
