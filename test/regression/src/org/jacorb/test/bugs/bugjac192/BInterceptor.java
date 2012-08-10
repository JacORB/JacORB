package org.jacorb.test.bugs.bugjac192;

import org.jacorb.orb.CDROutputStream;
import org.omg.CORBA.ORB;
import org.omg.IOP.ServiceContext;
import org.omg.PortableInterceptor.ClientRequestInfo;
import org.omg.PortableInterceptor.ClientRequestInterceptor;
import org.omg.PortableInterceptor.ForwardRequest;

/**
 * <code>BInterceptor</code> is a client request interceptor to help test
 * service context propagation after a ForwardRequest. It adds the service
 * context.
 *
 * @author Nick Cross
 */
public class BInterceptor
    extends org.omg.CORBA.LocalObject
    implements ClientRequestInterceptor
{
    private final ORB orb;

    public BInterceptor(ORB orb)
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

        // This part is proprietary code to marshal the service context data
        final CDROutputStream os = (CDROutputStream) orb.create_output_stream();

        os.write_boolean (true);
        result = os.getBufferCopy();
        os.close();
        // End

        ri.add_request_service_context
            (new ServiceContext (BugJac192Test.svcID, result), true);
    }


    /**
     * <code>name</code> (default impl).
     *
     * @return a <code>String</code> value
     */
    public String name()
    {
        return "BInterceptor";
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
