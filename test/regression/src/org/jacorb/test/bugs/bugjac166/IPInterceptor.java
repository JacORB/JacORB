package org.jacorb.test.bugs.bugjac166;

import java.net.Socket;
import org.jacorb.orb.iiop.ServerIIOPConnection;
import org.jacorb.orb.dsi.ServerRequest;
import org.jacorb.orb.portableInterceptor.ServerRequestInfoImpl;
import org.omg.CORBA.Any;
import org.omg.CORBA.INTERNAL;
import org.omg.CORBA.ORB;
import org.omg.PortableInterceptor.ForwardRequest;
import org.omg.PortableInterceptor.ServerRequestInfo;
import org.omg.PortableInterceptor.ServerRequestInterceptor;


/**
 * <code>IPInterceptor</code> illustrates and tests extracting information from
 * a ServerRequestInterceptor and storing it in PICurrent for the Servant to use.
 *
 * @author Nick Cross
 * @version $Id$
 */
public class IPInterceptor
    extends org.omg.CORBA.LocalObject
    implements ServerRequestInterceptor
{
    private final ORB orb;

    public IPInterceptor(ORB orb)
    {
        this.orb = orb;
    }

    /**
     * <code>receive_request</code> uses proprietary non-public API to extract an
     * IP address.
     *
     * @param ri a <code>ServerRequestInfo</code> value
     * @exception ForwardRequest if an error occurs
     */
    public void receive_request( ServerRequestInfo ri )
        throws ForwardRequest
    {
        String ipAddr = null;

        // This is proprietary non-public API and specific to JacORB only.
        if (ri instanceof ServerRequestInfoImpl)
        {
            ServerRequest request = ((ServerRequestInfoImpl)ri).request;
            // Retrieve the transport from the ServerRequest/GIOPConnection
            ServerIIOPConnection transport =
                ((ServerIIOPConnection)request.getConnection().getTransport());
            // Get the socket from the IIOP layer
            Socket socket = transport.getSocket();

            ipAddr = socket.getInetAddress().getHostAddress();
        }
        // End

        try
        {
            Any nameAny = orb.create_any();
            nameAny.insert_string(ipAddr);

            ri.set_slot(IPInitializer.slotID, nameAny);
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
        return "IPInterceptor";
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
