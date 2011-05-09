package org.jacorb.orb;

import org.jacorb.orb.giop.ReplyInputStream;
import org.omg.CORBA.Object;
import org.omg.CORBA.SystemException;
import org.omg.CORBA.portable.ApplicationException;
import org.omg.CORBA.portable.RemarshalException;

public class NullClientInterceptorHandler implements ClientInterceptorHandler
{
    private final static ClientInterceptorHandler instance = new NullClientInterceptorHandler();

    private NullClientInterceptorHandler()
    {
        super();
    }

    public void handle_location_forward(ReplyInputStream reply,
            Object forward_reference) throws RemarshalException
    {

    }

    public void handle_receive_exception(SystemException exception)
                                                                   throws RemarshalException
    {
    }

    public void handle_receive_exception(SystemException exception,
            ReplyInputStream reply) throws RemarshalException
    {
    }

    public void handle_receive_exception(ApplicationException exception,
            ReplyInputStream reply) throws RemarshalException
    {
    }

    public void handle_receive_other(short reply_status)
                                                        throws RemarshalException
    {
    }

    public void handle_receive_reply(ReplyInputStream reply)
                                                            throws RemarshalException
    {
    }

    public void handle_send_request() throws RemarshalException
    {
    }

    public static ClientInterceptorHandler getInstance()
    {
        return instance;
    }
}
