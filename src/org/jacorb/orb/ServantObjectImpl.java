package org.jacorb.orb;

import org.jacorb.orb.portableInterceptor.InterceptorManager;
import org.jacorb.orb.portableInterceptor.ServerInterceptorIterator;
import org.jacorb.orb.portableInterceptor.ServerRequestInfoImpl;
import org.omg.CORBA.UserException;
import org.omg.CORBA.portable.ServantObjectExt;
import org.omg.PortableInterceptor.SUCCESSFUL;
import org.omg.PortableInterceptor.SYSTEM_EXCEPTION;
import org.omg.PortableInterceptor.USER_EXCEPTION;

public class ServantObjectImpl extends ServantObjectExt
{
    /**
     * The Server request info associated with this servant
     */
    private ServerRequestInfoImpl sinfo = null;

    /**
     * The client interceptors associated with this servant
     */
    private DefaultClientInterceptorHandler interceptors = null;

    /**
     * The interceptor manager associated with this call
     */
    private InterceptorManager manager = null;

    /**
     * The server interceptor iterator
     */
    private ServerInterceptorIterator interceptorIterator = null;

    /**
     * Prevent call to sendException if an exception occurs during handling
     * of the reply in normalCompletion
     */
    private boolean normalCompletionCalled = false;

    /**
     * Reference to the orb for this servant
     */
    private org.jacorb.orb.ORB orb = null;

    /**
     * This method is called if a call to the local server is successful.
     * The call to SEND_EXCEPTION can result in further exceptions being
     * thrown, including a ForwardRequest
     */
    public void normalCompletion()
    {
        if (orb.hasServerRequestInterceptors())
        {
           manager = orb.getInterceptorManager();
           interceptorIterator = manager.getServerIterator();

           try
           {
               /**
                * Setting this flag here to prevent a duplicate call to SEND_EXCEPTION
                * if an error occurs during the call to SEND_REPLY.
                *
                * If an exception occurs during SEND_REPLY any necessary SEND_EXCEPTION
                * points will already have been called
                */
               normalCompletionCalled = true;

               interceptorIterator.iterate (sinfo,
                                            ServerInterceptorIterator.SEND_REPLY);

               sinfo.setReplyStatus (SUCCESSFUL.value);
            }
            catch (RuntimeException ex)
            {
                sinfo.setReplyStatus (SYSTEM_EXCEPTION.value);
                sinfo.updateException (ex);
                throw ex;
            }
            catch (UserException ue)
            {
               sinfo.setReplyStatus (USER_EXCEPTION.value);
               sinfo.updateException (ue);
               throw new RuntimeException (ue);
            }
         }
    }

    /**
     * This method is called if the exception thrown is a RuntimeException
     * @param re the RuntimeException
     */
    public void exceptionalCompletion (RuntimeException re)
    {
        exceptionalCompletion ( (Throwable) re);
    }

    /**
     * This method is called if the exception thrown is an Error
     * @param err the RuntimeException
     */
    public void exceptionalCompletion (Error err)
    {
        exceptionalCompletion ( (Throwable) err);
    }

    /**
     * Generic method to handle whatever is thrown
     */
    public void exceptionalCompletion (Throwable t)
    {
        /**
         * If we haven't come from SEND_REPLY and there are interceptors
         * then we need to call SEND_EXCEPTION
         */
        if (!normalCompletionCalled && orb.hasServerRequestInterceptors())
        {
           manager = orb.getInterceptorManager();
           interceptorIterator = manager.getServerIterator();

           sinfo.updateException (t);

           try
           {
              interceptorIterator.iterate (sinfo,
                                           ServerInterceptorIterator.SEND_EXCEPTION);
           }
           catch (UserException ue)
           {
                sinfo.setReplyStatus (USER_EXCEPTION.value);
                sinfo.updateException (ue);
                throw new RuntimeException (ue);
           }
        }
    }

    /**
     * Set a reference to the ORB to be used in subsequent processing
     * @param orb the orb reference
     */
    public void setORB (org.jacorb.orb.ORB orb)
    {
        this.orb = orb;
    }

    /**
     * Set the server request info associated with this Servant
     * @param sinfo the ServerRequestInfoImpl
     */
    public void setServerRequestInfo (ServerRequestInfoImpl sinfo)
    {
        this.sinfo = sinfo;
    }

    /**
     * Set the local client interceptor handler that was instantiated
     * during servant_preinvoke, we need this to handle any necessary
     * interception points if an exception occurs
     * @param interceptors the ClientInterceptorHandler
     */
    public void setClientInterceptorHandler (DefaultClientInterceptorHandler interceptors)
    {
        this.interceptors = interceptors;
    }

    /**
     * Accessor method to get the ServerRequestInfoImpl.
     *
     * @return the server request info
     */
    public ServerRequestInfoImpl getServerRequestInfo()
    {
        return sinfo;
    }

    /**
     * Accessor  method to get the ClientInterceptorHandler - required
     * in servant_postinvoke
     * @return the DefaultClientInterceptorHandler
     */
    public DefaultClientInterceptorHandler getClientInterceptorHandler()
    {
        return interceptors;
    }
}
