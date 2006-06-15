package org.jacorb.test.bugs.bugjac182;

import org.jacorb.orb.CDRInputStream;
import org.jacorb.orb.ORB;
import org.omg.CORBA.Any;
import org.omg.CORBA.INTERNAL;
import org.omg.IOP.ServiceContext;
import org.omg.PortableInterceptor.ForwardRequest;
import org.omg.PortableInterceptor.ServerRequestInfo;
import org.omg.PortableInterceptor.ServerRequestInterceptor;


/**
 * <code>SInterceptor</code> receives a ServiceContext from the CInterceptor.
 * It then stores the information within that ServiceContext in a slot within
 * PICurrent for the server to analyse.
 *
 * @author Nick Cross
 * @version $Id$
 */
public class SInterceptor
    extends org.omg.CORBA.LocalObject
    implements ServerRequestInterceptor
{
    private final ORB orb;

    public SInterceptor(ORB orb)
    {
        this.orb = orb;
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
        ServiceContext sc = ri.get_request_service_context (BugJac182Test.svcID);
        byte []data = sc.context_data;

        try
        {
            // This part is proprietary code to unmarshal the service context data
            CDRInputStream is = new CDRInputStream(orb, data);
            boolean result = is.read_boolean();
            is.close();
            // End

            Any nameAny = orb.create_any();
            nameAny.insert_boolean(result);

            ri.set_slot(SInitializer.slotID, nameAny);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new INTERNAL (e.toString());
        }
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
