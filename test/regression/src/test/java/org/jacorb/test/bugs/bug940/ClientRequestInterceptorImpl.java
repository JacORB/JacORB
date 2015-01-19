package org.jacorb.test.bugs.bug940;

import org.jacorb.test.EmptyException;
import org.jacorb.test.TimingServer;
import org.omg.CORBA.LocalObject;
import org.omg.CORBA.ORB;
import org.omg.PortableInterceptor.ClientRequestInfo;
import org.omg.PortableInterceptor.ClientRequestInterceptor;
import org.omg.PortableInterceptor.ForwardRequest;

import static org.jacorb.test.bugs.bug940.Bug940Test.setReplyEndTime;

final public class ClientRequestInterceptorImpl extends LocalObject implements
        ClientRequestInterceptor
{
    private String name;
    private TimingServer ts;
    private int timeout = 500;

    private boolean inInterceptor;

    public ClientRequestInterceptorImpl(String string, ORB orb)
    {
        this.name = string;
    }

    void setInInterceptor(boolean b)
    {
        inInterceptor = b;
    }

    void setTimingServer(TimingServer ts)
    {
        this.ts = ts;
    }

    void setTimingServer(TimingServer ts, int timeout)
    {
        this.timeout = timeout;
        this.ts = setReplyEndTime(ts, System.currentTimeMillis() + timeout);
    }

    @Override
    public void send_request(ClientRequestInfo ri) throws ForwardRequest
    {
        // Prevent infinite loop.
        if (inInterceptor)
        {
            return;
        }

        if (ri.operation().equals("server_time"))
        {
            inInterceptor = true;
            try
            {
                ts.ex_op('a', 0);
            }
            catch (EmptyException e)
            {
                e.printStackTrace();
            }

        }
        else if (!ri.operation().equals("ex_op"))
        {
            inInterceptor = true;
            ts.operation(5, timeout / 2);
        }
    }

    @Override
    public void receive_exception(ClientRequestInfo ri) throws ForwardRequest
    {
    }

    @Override
    public void receive_other(ClientRequestInfo ri) throws ForwardRequest
    {

    }

    @Override
    public void receive_reply(ClientRequestInfo ri)
    {

    }

    @Override
    public void send_poll(ClientRequestInfo ri)
    {

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