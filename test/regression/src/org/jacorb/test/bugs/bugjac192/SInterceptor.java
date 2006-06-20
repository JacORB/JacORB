package org.jacorb.test.bugs.bugjac192;

import org.jacorb.orb.CDRInputStream;
import org.omg.CORBA.Any;
import org.omg.CORBA.BAD_PARAM;
import org.omg.CORBA.INTERNAL;
import org.omg.IOP.ServiceContext;
import org.omg.PortableInterceptor.ForwardRequest;
import org.omg.PortableInterceptor.ServerRequestInfo;
import org.omg.PortableInterceptor.ServerRequestInterceptor;
import org.omg.PortableInterceptor.InvalidSlot;

/**
 * <code>SInterceptor</code> tries to examine the service context sent from
 * the client interceptor. It then stores an Any in a slot within PICurrent
 * for the server to analyse and return the ServiceContext propagation result.
 *
 * @author Nick Cross
 * @version $Id$
 */
public class SInterceptor
    extends org.omg.CORBA.LocalObject
    implements ServerRequestInterceptor
{
    /**
     * <code>receive_request</code> tests whether a service context has been sent.
     *
     * @param ri a <code>ServerRequestInfo</code> value
     * @exception ForwardRequest if an error occurs
     */
    public void receive_request( ServerRequestInfo ri )
        throws ForwardRequest
    {
        ServiceContext sc = null;

        try
        {
            sc = ri.get_request_service_context (BugJac192Test.svcID);
        }
        catch (Exception e)
        {
            throw new RuntimeException();
        }
        addResult (ri, sc);
    }


    /**
     * <code>receive_request_service_contexts</code> tests whether a service
     * context has been sent.
     *
     * @param ri a <code>ServerRequestInfo</code> value
     * @exception ForwardRequest if an error occurs
     */
    public void receive_request_service_contexts( ServerRequestInfo ri )
        throws ForwardRequest
    {
        ServiceContext sc = null;

        try
        {
            sc = ri.get_request_service_context (BugJac192Test.svcID);
        }
        catch (BAD_PARAM e)
        {
            throw new RuntimeException();
        }
        addResult (ri, sc);
    }


    /**
     * <code>addResult</code> stores the result of the service context propagation into
     * the .
     *
     * @param ri a <code>ServerRequestInfo</code> value
     * @param sc a <code>ServiceContext</code> value
     */
    private void addResult(ServerRequestInfo ri, ServiceContext sc)
    {
        boolean result = false;

        if (BugJac192Test.serverOrb == null)
        {
            BugJac192Test.serverOrb = org.omg.CORBA.ORB.init (new String[]{}, null);
        }

        if (sc != null)
        {
            byte []data = sc.context_data;

            // This part is proprietary code to unmarshal the service context data
            CDRInputStream is = new CDRInputStream(BugJac192Test.serverOrb, data);
            result = is.read_boolean();
            is.close();
            // End
        }

        Any nameAny = BugJac192Test.serverOrb.create_any();
        nameAny.insert_boolean(result);

        try
        {
            ri.set_slot(SInitializer.slotID, nameAny);
        }
        catch (InvalidSlot e)
        {
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
