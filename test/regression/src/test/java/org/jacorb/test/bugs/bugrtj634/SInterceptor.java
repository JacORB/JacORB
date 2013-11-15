package org.jacorb.test.bugs.bugrtj634;

import org.omg.PortableInterceptor.ForwardRequest;
import org.omg.PortableInterceptor.ServerRequestInfo;
import org.omg.PortableInterceptor.ServerRequestInterceptor;

/**
 * <code>SInterceptor</code> receives a ServiceContext from the SInterceptor.
 * It then stores the information within that ServiceContext in a slot within
 * PICurrent for the server to analyse.
 *
 * @author <a href="mailto:rnc@prismtechnologies.com">Nick Cross</a>
 * @version 1.0
 */
public class SInterceptor
    extends org.omg.CORBA.LocalObject
    implements ServerRequestInterceptor
{
   private int counter = 0;

   static org.omg.CORBA.Object OBJ_2 = null;

    /**
     * <code>name</code> (default impl).
     *
     * @return a <code>String</code> value
     */
    public String name()
    {
        return "SInterceptor";
    }

    /**
     * <code>destroy</code> (default impl).
     */
    public void destroy()
    {
    }

    /**
     * <code>receive_request_service_contexts</code> (default impl).
     *
     * @param ri a <code>ServerRequestInfo</code> value
     * @exception ForwardRequest if an error occurs
     */
    public void receive_request_service_contexts( ServerRequestInfo ri )
        throws ForwardRequest
    {
       counter++;
       if (counter == 1 || counter == 4  || counter == 7)
       {
          // first invocation redirect to obj_2
          throw new org.omg.PortableInterceptor.ForwardRequest(OBJ_2);
       }
    }


     /**
      * <code>receive_request</code>
      *
      * @param ri a <code>ServerRequestInfo</code> value
      * @exception ForwardRequest if an error occurs
      */
     public void receive_request( ServerRequestInfo ri )
         throws ForwardRequest
     {
     }

     /**
      * <code>send_reply</code> (default impl).
      *
      * @param ri a <code>ServerRequestInfo</code> value
      */
     public void send_reply( ServerRequestInfo ri )
     {

     }

     /**
      * <code>send_exception</code> (default impl).
      *
      * @param ri a <code>ServerRequestInfo</code> value
      * @exception ForwardRequest if an error occurs
      */
     public void send_exception( ServerRequestInfo ri )
         throws ForwardRequest
     {
        throw new ForwardRequest();
     }

     /**
      * <code>send_other</code> (default impl).
      *
      * @param ri a <code>ServerRequestInfo</code> value
      * @exception ForwardRequest if an error occurs
      */
     public void send_other( ServerRequestInfo ri )
         throws ForwardRequest
     {
     }
}
