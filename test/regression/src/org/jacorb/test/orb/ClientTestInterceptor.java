package org.jacorb.test.orb;

import org.omg.CORBA.LocalObject;
import org.omg.PortableInterceptor.ClientRequestInfo;
import org.omg.PortableInterceptor.ClientRequestInterceptor;
import org.omg.PortableInterceptor.ForwardRequest;
import org.jacorb.test.bugs.bugrtj634.CInterceptor;


public class ClientTestInterceptor extends CInterceptor
{
   private int testCount = 0;

   /**
     * <code>send_request</code>
     *
     * @param ri a <code>ClientRequestInfo</code> value
     * @exception ForwardRequest if an error occurs
     */
    public void send_request( ClientRequestInfo ri )
        throws ForwardRequest
    {
        if (testCount++ == 0)
        {
            throw new RuntimeException();
        }
        else
        {
            throw new org.omg.CORBA.TRANSIENT();
        }
    }
}
