package org.jacorb.orb;

import java.util.Enumeration;

import org.jacorb.orb.portableInterceptor.*;
import org.jacorb.util.Debug;
import org.omg.CORBA.portable.RemarshalException;
import org.omg.IOP.ServiceContext;
import org.omg.PortableInterceptor.*;

/**
 * An instance of this class handles all interactions between one particular
 * client request and any interceptors registered for it.
 * 
 * @author Andre Spiegel
 * @version $Id$
 */
public class ClientInterceptorHandler
{
    private ClientRequestInfoImpl info = null;
    
    /**
     * Constructs an interceptor handler for the given parameters.
     * If no interceptors are registered on the client side,
     * the resulting object will be a dummy object that does nothing when
     * invoked.
     */
    public ClientInterceptorHandler 
                      ( org.jacorb.orb.ORB orb,
                        org.jacorb.orb.connection.RequestOutputStream ros,
                        org.omg.CORBA.Object self,
                        org.jacorb.orb.Delegate delegate,
                        org.jacorb.orb.ParsedIOR piorOriginal,
                        org.jacorb.orb.connection.ClientConnection connection )
    {
        if ( orb.hasClientRequestInterceptors() )
        {
            info = new ClientRequestInfoImpl ( orb, ros, self, delegate,
                                               piorOriginal, connection );
        }
    }
    
    public void handle_send_request() throws RemarshalException
    {
        if ( info != null )
        {
            invokeInterceptors ( info, ClientInterceptorIterator.SEND_REQUEST );
            
            // Add any new service contexts to the message
            Enumeration ctx = info.getRequestServiceContexts();
            
            while ( ctx.hasMoreElements() )
            {
                info.request_os.addServiceContext 
                                       ( ( ServiceContext ) ctx.nextElement() );
            }           
        }
    }   

    public void handle_receive_other ( short reply_status )
        throws RemarshalException
    {
        if ( info != null )
        {
            info.reply_status = reply_status;
            invokeInterceptors ( info, ClientInterceptorIterator.RECEIVE_OTHER );
        }   
    }

    public void handle_receive_exception ( org.omg.CORBA.SystemException ex )
        throws RemarshalException
    {
        if ( info != null )
        {
            SystemExceptionHelper.insert ( info.received_exception, ex );
        
            try
            {
                info.received_exception_id =
                    SystemExceptionHelper.type ( ex ).id();
            }
            catch ( org.omg.CORBA.TypeCodePackage.BadKind bk )
            {
                Debug.output ( Debug.INTERCEPTOR | Debug.INFORMATION, bk );
            }
            
            info.reply_status = SYSTEM_EXCEPTION.value;
            
            invokeInterceptors ( info,
                                 ClientInterceptorIterator.RECEIVE_EXCEPTION );
        }
        
        
        
    }
    
    public void invokeInterceptors( ClientRequestInfoImpl info, short op )
      throws RemarshalException
    {
        ClientInterceptorIterator intercept_iter =
            info.orb.getInterceptorManager().getClientIterator();

        try
        {
            intercept_iter.iterate( info, op );
        }
        catch ( org.omg.PortableInterceptor.ForwardRequest fwd )
        {
            info.delegate.rebind( info.orb.object_to_string( fwd.forward ) );
            throw new RemarshalException();
        }
        catch ( org.omg.CORBA.UserException ue )
        {
            Debug.output( Debug.INTERCEPTOR | Debug.IMPORTANT, ue );
        }
    }

    
}
