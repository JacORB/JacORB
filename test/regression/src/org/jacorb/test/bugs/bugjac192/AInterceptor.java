package org.jacorb.test.bugs.bugjac192;

import org.jacorb.orb.portableInterceptor.ClientRequestInfoImpl;
import org.omg.CORBA.INTERNAL;
import org.omg.CORBA.ORB;
import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.CORBA.Policy;
import org.omg.CORBA.SetOverrideType;
import org.omg.IOP.TAG_INTERNET_IOP;
import org.omg.IOP.TaggedProfile;
import org.omg.PortableInterceptor.ClientRequestInfo;
import org.omg.PortableInterceptor.ClientRequestInterceptor;
import org.omg.PortableInterceptor.ForwardRequest;
import org.omg.RTCORBA.Protocol;
import org.omg.RTCORBA.RTORB;
import org.omg.RTCORBA.RTORBHelper;


/**
 * <code>AInterceptor</code> is a client request interceptor to help test
 * service context propagation after a ForwardRequest. It throws the
 * ForwardRequest
 *
 * @author Nick Cross
 * @version $Id$
 */
public class AInterceptor
    extends org.omg.CORBA.LocalObject
    implements ClientRequestInterceptor
{
    private static boolean once;
    /**
     * <code>send_request</code>
     *
     * @param ri a <code>ClientRequestInfo</code> value
     * @exception ForwardRequest if an error occurs
     */
    public void send_request( ClientRequestInfo ri )
        throws ForwardRequest
    {
        TaggedProfile effProfile = ri.effective_profile();

        if (!once &&
            effProfile.tag == TAG_INTERNET_IOP.value &&
            ri.forward_reference() == null)
        {
            try
            {
                ORB orb =  ((ClientRequestInfoImpl)ri).orb;
                RTORB rtorb = RTORBHelper.narrow(orb.resolve_initial_references("RTORB"));

                Protocol wiop = new Protocol();

                // This is WIOP protocol value from WIOPFactories.
                wiop.protocol_type = 7;
                Policy[] wiopP = new Policy[]
                    { rtorb.create_client_protocol_policy(new Protocol[] { wiop }) };

                ri.effective_target()._set_policy_override(wiopP, SetOverrideType.SET_OVERRIDE);

                throw new ForwardRequest (ri.effective_target());
            }
            catch (InvalidName e)
            {
                once = true;
                throw new INTERNAL ("Caught invalid name " + e);
            }
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
