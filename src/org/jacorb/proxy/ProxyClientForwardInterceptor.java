package org.jacorb.proxy;

import org.omg.PortableInterceptor.*;
import org.omg.CosNaming.*;
import org.omg.CORBA.Any;
import org.jacorb.util.Environment;
import org.omg.IOP.*;
import org.omg.IOP.ServiceContext;
import org.jacorb.orb.*;
/**
 * This interceptor will silently redirect requests of a
 * client to another target by throwing a ForwardRequest
 * exception.
 *
 * @author Nicolas Noffke, Sebastian Müller
 */

public class ProxyClientForwardInterceptor
    extends org.omg.CORBA.LocalObject
    implements ClientRequestInterceptor
{

    private boolean in_loop = false;
    private org.jacorb.proxy.Proxy proxy = null;
    private org.jacorb.orb.ORB orb = null;

    private int slot_id;
    private Codec codec = null;

    public ProxyClientForwardInterceptor(ORBInitInfo info, int slot_id, Codec codec )
    {
    	this.slot_id = slot_id;
        this.codec = codec;

    	try{
     	//narrow proxy
     	orb = ( (org.jacorb.orb.portableInterceptor.ORBInitInfoImpl)info ).getORB();

        String url = Environment.getProperty( "jacorb.ProxyServerURL" );
     	org.omg.CORBA.Object obj = orb.string_to_object( url );
     	proxy = org.jacorb.proxy.ProxyHelper.narrow( obj );

     	}
     	catch (Exception e)
        {
            e.printStackTrace();

        }
    }

    public String name()
    {
        return "Jacorb.ProxyClientForwardInterceptor";
    }

    /**
     * Throws a ForwardRequest, if target is not applethost
     */
    public void send_request(ClientRequestInfo ri)
        throws ForwardRequest
    {

		if (! proxy._is_equivalent(ri.target() ))
		{
			

			throw new ForwardRequest("ProxyForward", proxy);
		}
		else
		{
			String original_target = ri.effective_target().toString();

			ServiceContext ctx = new ServiceContext(1245790978, original_target.getBytes());
			ri.add_request_service_context(ctx, false);

		}
    }

    public void send_poll(ClientRequestInfo ri){
    }

    public void receive_reply(ClientRequestInfo ri){
    }

    public void receive_exception(ClientRequestInfo ri)
        throws ForwardRequest{
    }

    public void receive_other(ClientRequestInfo ri)
        throws ForwardRequest{
    }
    
    public void destroy()
    {
    }

    private boolean localAddress(String hostname)
    {
    	if ( orb.isApplet() ){
    		return ( orb.getApplet().getCodeBase().getHost() == hostname );
	}
	//TODO: intranet /internetcheck via netmask settings
	return true;
    }




} // ClientForwardInterceptor
