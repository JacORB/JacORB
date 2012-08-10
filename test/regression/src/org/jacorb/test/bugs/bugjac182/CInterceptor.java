package org.jacorb.test.bugs.bugjac182;

import org.jacorb.orb.CDROutputStream;
import org.jacorb.orb.ORB;
import org.omg.CORBA.portable.ObjectImpl;
import org.omg.IOP.ServiceContext;
import org.omg.PortableInterceptor.ClientRequestInfo;
import org.omg.PortableInterceptor.ClientRequestInterceptor;
import org.omg.PortableInterceptor.ForwardRequest;


/**
 * <code>CInterceptor</code> is a client request interceptor to check that the
 * object being transmitted is local or not. It then passes that information on
 * via a service context.
 *
 * @author Nick Cross
 */
public class CInterceptor
    extends org.omg.CORBA.LocalObject
    implements ClientRequestInterceptor
{
    private final ORB orb;

    public CInterceptor(ORB orb)
    {
        this.orb = orb;
    }

    /**
     * <code>send_request</code>
     *
     * @param ri a <code>ClientRequestInfo</code> value
     * @exception ForwardRequest if an error occurs
     */
    public void send_request( ClientRequestInfo ri )
        throws ForwardRequest
    {
        byte []result = null;

        ObjectImpl o = (ObjectImpl)ri.effective_target();

        // This part is proprietary code to marshal the service context data
        final CDROutputStream os = (CDROutputStream) orb.create_output_stream();
        if (o._is_local())
        {
            os.write_boolean (true);
        }
        else
        {
            os.write_boolean (false);
        }
        result = os.getBufferCopy();
        os.close();
        // End

        ri.add_request_service_context
            (new ServiceContext (BugJac182Test.svcID, result), true);
    }


    /**
     * <code>name</code> (default impl).
     *
     * @return a <code>String</code> value
     */
    public String name()
    {
        return "CInterceptor";
    }

    /**
     * <code>destroy</code> (default impl).
     */
    public void destroy()
    {
    }

    /**
     * <code>send_poll</code> (default impl).
     *
     * @param ri a <code>ClientRequestInfo</code> value
     */
    public void send_poll( ClientRequestInfo ri )
    {
    }

    /**
     * <code>receive_reply</code> (default impl).
     *
     * @param ri a <code>ClientRequestInfo</code> value
     */
    public void receive_reply( ClientRequestInfo ri )
    {
    }

    /**
     * <code>receive_exception</code> (default impl).
     *
     * @param ri a <code>ClientRequestInfo</code> value
     * @exception ForwardRequest if an error occurs
     */
    public void receive_exception( ClientRequestInfo ri )
        throws ForwardRequest
    {
    }

    /**
     * <code>receive_other</code> (default impl).
     *
     * @param ri a <code>ClientRequestInfo</code> value
     * @exception ForwardRequest if an error occurs
     */
    public void receive_other( ClientRequestInfo ri )
        throws ForwardRequest
    {
    }
}
