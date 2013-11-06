package org.jacorb.test.bugs.bugrtj634;

import org.omg.CORBA.LocalObject;
import org.omg.PortableInterceptor.ClientRequestInfo;
import org.omg.PortableInterceptor.ClientRequestInterceptor;
import org.omg.PortableInterceptor.ForwardRequest;


/**
 * <code>CInterceptor</code> is a client request interceptor to check that the
 * object being transmitted is local or not. It then passes that information on
 * via a service context.
 *
 * @author <a href="mailto:rnc@prismtechnologies.com">Nick Cross</a>
 * @version 1.0
 */
public class CInterceptor extends LocalObject implements ClientRequestInterceptor
{
   private int counter = 0;

   /**
     * <code>send_request</code>
     *
     * @param ri a <code>ClientRequestInfo</code> value
     * @exception ForwardRequest if an error occurs
     */
    public void send_request( ClientRequestInfo ri )
        throws ForwardRequest
    {
       // Initial operation calls this once on outgoing and then hits SInterceptor
       // that throws a ForwardRequest to Object2 via
       // receive_request_service_contexts. This causes a RemarshalException and
       // therefore this will be called again. Counter will be 2 then...

       counter++;
       if (counter == 3)
       {
          // second invocation redirect to original target (obj 1)
          org.omg.CORBA.Object CORBA_OBJ = ri.target();
          throw new ForwardRequest(CORBA_OBJ);
       }
       else if (counter == 7)
       {
          org.omg.CORBA.Object CORBA_OBJ = ri.target();
          throw new ForwardRequest(CORBA_OBJ);
       }
       else if (counter == 11)
       {
          throw new ForwardRequest(RTJ634Test.server2);
       }
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
