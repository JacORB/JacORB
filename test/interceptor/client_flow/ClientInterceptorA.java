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

public class ClientInterceptorA 
    extends org.jacorb.orb.LocalityConstrainedObject 
    implements ClientRequestInterceptor
{    
    public ClientInterceptorA()
    {
    }

    public String name() 
    {
        return "A";
    }

    public void destroy()
    {
    }

    public void send_request( ClientRequestInfo ri ) 
        throws ForwardRequest
    {
        System.out.println("ClientInterceptor A: send_request");
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
        System.out.println("ClientInterceptor A: receive_other");
        
        try
        {
            System.out.println( ri.forward_reference() );
        }
        catch( Exception e )
        {
            e.printStackTrace();
        }
    }
}// ClientInterceptor
