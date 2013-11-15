package org.jacorb.test.bugs.bug832;

import org.jacorb.orb.ORB;
import org.omg.BiDirPolicy.BIDIRECTIONAL_POLICY_TYPE;
import org.omg.PortableInterceptor.ForwardRequest;
import org.omg.PortableInterceptor.ServerRequestInfo;
import org.omg.PortableInterceptor.ServerRequestInterceptor;


/**
 * <code>SInterceptor</code> receives a ServiceContext from the CInterceptor.
 * It then stores the information within that ServiceContext in a slot within
 * PICurrent for the server to analyse.
 *
 * @author Nick Cross
 */
public class SInterceptor
    extends org.omg.CORBA.LocalObject
    implements ServerRequestInterceptor
{
    public SInterceptor(ORB orb)
    {
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
       ri.get_server_policy (BIDIRECTIONAL_POLICY_TYPE.value);
    }


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
