package org.jacorb.proxy;

import org.omg.PortableInterceptor.*;
import org.omg.CosNaming.*;
import org.omg.CORBA.Any;
import org.jacorb.util.Environment;
import org.omg.IOP.ServiceContext;
import org.omg.IOP.Codec;

/**
 * This interceptor will silently redirect requests of a
 * client to another target by throwing a ForwardRequest
 * exception.
 *
 * @author Nicolas Noffke, Sebastian Müller
 */

public class ProxyServerForwardInterceptor
    extends org.omg.CORBA.LocalObject
    implements ServerRequestInterceptor
{
    private org.jacorb.orb.ORB orb = null;
    private Codec codec = null;
    private org.jacorb.proxy.Proxy proxy = null;
    public static int slot_id = -1;

    public void destroy ()
    {
    }
    
    public ProxyServerForwardInterceptor (ORBInitInfo info, Codec codec, int slot_id)
    {
        this.codec = codec;
        this.slot_id = slot_id;
        orb = ((org.jacorb.orb.portableInterceptor.ORBInitInfoImpl) info).getORB ();

        String url = Environment.getProperty ("jacorb.ProxyServerURL");
        org.omg.CORBA.Object obj = orb.string_to_object (url);
        proxy = org.jacorb.proxy.ProxyHelper.narrow (obj);
    }

    public String name ()
    {
        return "JacORB.ProxyServerForwardInterceptor";
    }

    /**
     * Throws a ForwardRequest, if target is not applethost
     */
    public void receive_request (ServerRequestInfo ri)
        throws ForwardRequest
    {
    }

    public void send_reply (ServerRequestInfo ri)
    {
    }

    public void send_exception (ServerRequestInfo ri)
        throws ForwardRequest
    {
    }

    public void send_other (ServerRequestInfo ri)
        throws ForwardRequest
    {
    }

    public void receive_request_service_contexts (ServerRequestInfo ri)
        throws ForwardRequest
    {
        ServiceContext context = ri.get_request_service_context (1245790978);
        String ior_str = new String (context.context_data);
        Any any = orb.create_any ();

        any.insert_string (ior_str);
        try
        {
            ri.set_slot (slot_id, any);
        }
        catch (org.omg.PortableInterceptor.InvalidSlot e)
        {
            e.printStackTrace ();
        }
    }
}
