package org.jacorb.test.bugs.bugjac192b;

import org.jacorb.orb.portableInterceptor.ClientRequestInfoImpl;
import org.omg.CORBA.INTERNAL;
import org.omg.CORBA.ORB;
import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.IOP.TAG_INTERNET_IOP;
import org.omg.IOP.TaggedProfile;
import org.omg.PortableInterceptor.ClientRequestInfo;
import org.omg.PortableInterceptor.ClientRequestInterceptor;
import org.omg.PortableInterceptor.ForwardRequest;
import org.omg.RTCORBA.RTORBHelper;


/**
 * <code>AInterceptor</code> is a client request interceptor to test
 * that interceptors are still called on local objects after a
 * system exception has been thrown.
 *
 * @author Nick Cross
 * @version $Id$
 */
public class AInterceptor
    extends org.omg.CORBA.LocalObject
    implements ClientRequestInterceptor
{
    /**
     * <code>once</code> is used a toggle to determine whether to throw
     * an exception.
     */
    private static boolean once;


    /**
     * <code>send_request</code> will throw an exception upon the first call
     * and return normally upon the second.
     *
     * @param ri a <code>ClientRequestInfo</code> value
     * @exception ForwardRequest if an error occurs
     */
    public void send_request( ClientRequestInfo ri )
        throws ForwardRequest
    {
        TaggedProfile effProfile = ri.effective_profile();

        if ( ! once &&
            effProfile.tag == TAG_INTERNET_IOP.value &&
            ri.forward_reference() == null)
        {
            try
            {
                ORB orb =  ((ClientRequestInfoImpl)ri).orb;

                // This will throw an exception
                RTORBHelper.narrow
                    (orb.resolve_initial_references("A_DUMMY_RESULE"));
            }
            catch (InvalidName e)
            {
                once = true;
                throw new INTERNAL ("Caught invalid name " + e);
            }
        }
        else if (once)
        {
            BugJac192bTest.interceptorCalled = true;
        }
    }


    /**
     * <code>name</code> (default impl).
     *
     * @return a <code>String</code> value
     */
    public String name()
    {
        return "AInterceptor";
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
