package test.interceptor.client_flow;

import org.omg.PortableInterceptor.*;

/**
 * ClientInterceptor.java
 *
 *
 * Created: Fri Oct 26 11:04:19 2001
 *
 * @author Nicolas Noffke
 * @version $Id$
 */

public class ClientInterceptorB 
    extends org.jacorb.orb.LocalityConstrainedObject 
    implements ClientRequestInterceptor
{    
    private org.omg.CORBA.Object forward = null;
    private boolean triggered = false;

    public ClientInterceptorB( org.omg.CORBA.Object forward )
    {
        this.forward = forward;
    }

    public String name() 
    {
        return "B";
    }

    public void destroy()
    {
    }

    public void send_request( ClientRequestInfo ri ) 
        throws ForwardRequest
    {
        System.out.println("ClientInterceptor B: send_request");
        
        if( ! triggered )
        {
            triggered = true;

            System.out.println("ClientInterceptor B: throwing ForwardRequest");
         
            throw new ForwardRequest( forward,
                                      false );   
        }
    }

    public void send_poll(ClientRequestInfo ri)
    {
    }

    public void receive_reply(ClientRequestInfo ri)
    {
    }

    public void receive_exception(ClientRequestInfo ri) 
        throws ForwardRequest
    {
    }

    public void receive_other(ClientRequestInfo ri) 
        throws ForwardRequest
    {
        System.out.println("ClientInterceptor B: receive_other");
    }
}// ClientInterceptor
