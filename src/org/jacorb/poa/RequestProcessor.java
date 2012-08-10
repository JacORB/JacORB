package org.jacorb.poa;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2012 Gerald Brose / The JacORB Team.
 *
 *   This library is free software; you can redistribute it and/or
 *   modify it under the terms of the GNU Library General Public
 *   License as published by the Free Software Foundation; either
 *   version 2 of the License, or (at your option) any later version.
 *
 *   This library is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *   Library General Public License for more details.
 *
 *   You should have received a copy of the GNU Library General Public
 *   License along with this library; if not, write to the Free
 *   Software Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.jacorb.config.Configurable;
import org.jacorb.config.Configuration;
import org.jacorb.config.ConfigurationException;
import org.jacorb.orb.ORB;
import org.jacorb.orb.SystemExceptionHelper;
import org.jacorb.orb.dsi.ServerRequest;
import org.jacorb.orb.giop.ReplyOutputStream;
import org.jacorb.orb.portableInterceptor.InterceptorManager;
import org.jacorb.orb.portableInterceptor.ServerInterceptorIterator;
import org.jacorb.orb.portableInterceptor.ServerRequestInfoImpl;
import org.jacorb.poa.except.POAInternalError;
import org.jacorb.util.Time;
import org.omg.CORBA.CompletionStatus;
import org.omg.CORBA.UNKNOWN;
import org.omg.CORBA.portable.InvokeHandler;
import org.omg.GIOP.ReplyStatusType_1_2;
import org.omg.IOP.ServiceContext;
import org.omg.PortableInterceptor.LOCATION_FORWARD;
import org.omg.PortableInterceptor.SUCCESSFUL;
import org.omg.PortableInterceptor.SYSTEM_EXCEPTION;
import org.omg.PortableInterceptor.USER_EXCEPTION;
import org.omg.PortableServer.DynamicImplementation;
import org.omg.PortableServer.Servant;
import org.omg.PortableServer.ServantActivator;
import org.omg.PortableServer.ServantLocator;
import org.omg.PortableServer.ServantManager;
import org.omg.PortableServer.ServantLocatorPackage.CookieHolder;
import org.slf4j.Logger;

/**
 * This thread performs the request processing, the actual method invocation and
 * it returns the ServerRequest object to the ORB.
 *
 * @author Reimo Tiedemann, FU Berlin
 */

public class RequestProcessor
    extends Thread
    implements InvocationContext, Configurable
{
    private boolean start;
    private boolean terminate;
    private final RPPoolManager poolManager;

    private RequestController controller;
    private ServerRequest request;
    private Servant servant;
    private ServantManager servantManager;
    private CookieHolder cookieHolder;

    /**
     * Whether to check for expiry of any ReplyEndTimePolicy.  Normally,
     * it is sufficient to check this on the client side, but the additional
     * check on the server side can save the server and the network some work.
     * It requires that the clocks of the client and server machine are
     * synchronized, though.
     */
    private boolean checkReplyEndTime = false;

    /**
     * By default the servant code will be run in the currently executing class
     * loader. This allows the servant to be run its thread context class loader
     */
    private boolean useServantClassLoader = false;

    /** this processor's logger instance, obtained from the request controller */
    private Logger logger;

    /**
     * ID for RequestProcessor
     */
    private static int count = 0;

    private final static Set<String> specialOperations;

    static
    {
        specialOperations = new HashSet<String>(10);
        specialOperations.add("_is_a");
        specialOperations.add("_interface");
        specialOperations.add("_non_existent");
        specialOperations.add("_get_policy");
        specialOperations.add("_set_policy_overrides");
        specialOperations.add("_get_component");
    }

    RequestProcessor (RPPoolManager _poolManager)
    {
        super ("RequestProcessor-" + (++count));
        poolManager = _poolManager;
    }

    public void configure (Configuration configuration)
        throws ConfigurationException
    {
        checkReplyEndTime = configuration.getAttributeAsBoolean
        (
          "jacorb.poa.check_reply_end_time", false
        );
        useServantClassLoader = configuration.getAttributeAsBoolean
        (
            "jacorb.poa.useServantClassLoader", false
        );
    }

    /**
     * starts the request processor
     */

    synchronized void begin()
    {
        start = true;
        notify();
    }

    /**
     * terminates the request processor
     */

    synchronized void end()
    {
        terminate = true;
        notify();
        interrupt();
    }

    /**
     * returns the oid associated current servant invocation
     */

    public byte[] getObjectId()
    {
        if (!start)
            throw new POAInternalError("error: RequestProcessor not started (getObjectId)");
        return request.objectId();
    }

    /**
     * returns the orb that has received the request
     */

    public org.omg.CORBA.ORB getORB()
    {
        if (!start)
            throw new POAInternalError("error: RequestProcessor not started (getORB)");
        return controller.getORB();
    }

    /**
     * returns the poa that has dispatched the request
     */

    public POA getPOA()
    {
        if (!start)
            throw new POAInternalError("error: RequestProcessor not started (getPOA)");
        return controller.getPOA();
    }

    /**
     * returns the actual servant
     */

    public Servant getServant()
    {
        if (!start)
            throw new POAInternalError("error: RequestProcessor not started (getServant)");
        return servant;
    }

    /**
     * initializes the request processor
     */

    void init(RequestController requestController,
              ServerRequest serverRequest,
              Servant srvnt,
              ServantManager manager)
    {
        this.controller = requestController;
        this.request = serverRequest;
        this.servant = srvnt;
        this.servantManager = manager;
        cookieHolder = null;
        logger = requestController.getLogger();
    }

    private void clear()
    {
        controller = null;
        request = null;
        servant = null;
        servantManager = null;
        cookieHolder = null;
    }


    /**
     * causes the aom to perform the incarnate call on a servant activator
     */

    private void invokeIncarnate()
    {
        if (logger.isDebugEnabled())
        {
            logger.debug("rid: " + request.requestId() +
                         " opname: " + request.operation() +
                         " invoke incarnate on servant activator");
        }
        try
        {

            servant = controller.getAOM().incarnate( request.objectIdAsByteArrayKey(),
                                                     (ServantActivator) servantManager,
                                                     controller.getPOA());
            if (servant == null)
            {
                if (logger.isWarnEnabled())
                {
                    logger.warn("rid: " + request.requestId() +
                                " opname: " + request.operation() +
                                " incarnate: returns null");
                }

                request.setSystemException(new org.omg.CORBA.OBJ_ADAPTER());
            }
        }
        catch (org.omg.CORBA.SystemException e)
        {
            if (logger.isWarnEnabled())
            {
                logger.warn("rid: "+request.requestId() +
                            " opname: " + request.operation() +
                            " incarnate: system exception was thrown.",
                            e);
            }
            request.setSystemException(e);
        }
        catch (org.omg.PortableServer.ForwardRequest e)
        {
            if (logger.isWarnEnabled())
            {
                logger.warn("rid: " + request.requestId() +
                            " opname: " + request.operation() +
                            " incarnate: forward exception was thrown.",
                            e);
            }
            request.setLocationForward(e);

        }
        catch (Throwable e)
        {
            /* not spec. */
            if (logger.isErrorEnabled())
            {
                logger.error("rid: " + request.requestId() +
                             " opname: " + request.operation() +
                             " incarnate: throwable was thrown.",
                             e);
            }
            request.setSystemException(new org.omg.CORBA.OBJ_ADAPTER(e.toString()));
        }
    }


    /**
     * invokes the operation on servant,
     */

    private void invokeOperation()
    {
        String operation = request.operation();
        boolean specialOperation = false;
        ClassLoader prevClassLoader = null;
        Thread currentThread = null;

        if (useServantClassLoader)
        {
            // Set the TCCL to the servant's classloader, but save the current TCCL first.
            currentThread = Thread.currentThread();
            prevClassLoader = currentThread.getContextClassLoader();
            currentThread.setContextClassLoader(servant.getClass().getClassLoader());
        }

        try
        {
            if (servant instanceof org.omg.CORBA.portable.InvokeHandler)
            {
                if (logger.isDebugEnabled())
                {
                    logger.debug("rid: " + request.requestId() +
                                 " opname: " + operation +
                                 " invokeOperation on servant (stream based)");
                }

                if (specialOperations.contains(operation))
                {
                    if ("_get_policy".equals (operation))
                    {
                        // Check the number of args. If zero, then we assuming it's a "_get_policy"
                        // operation that was generated for an attribute named 'policy', instead
                        // of the 'get_policy' method on CORBA::Object
                        if (request.arguments() != null && request.arguments().count() >= 1)
                        {
                            specialOperation = true;
                        }
                    }
                    else
                    {
                        specialOperation = true;
                    }
                }

                if (specialOperation)
                {
                    ((org.jacorb.orb.ServantDelegate)servant._get_delegate())._invoke(servant,
                                                                                      operation,
                                                                                      request.getInputStream(),
                                                                                      request);
                }
                else
                {
                    ((InvokeHandler) servant)._invoke(operation,
                                                      request.getInputStream(),
                                                      request);
                }

            }
            else if (servant instanceof org.omg.PortableServer.DynamicImplementation)
            {
                if (logger.isDebugEnabled())
                {
                    logger.debug("rid: " + request.requestId() +
                                 " opname: " + operation +
                                 " invoke operation on servant (dsi based)");
                }
                if( specialOperations.contains(operation) &&
                    !(servant instanceof org.jacorb.orb.Forwarder) )
                {
                    ((org.jacorb.orb.ServantDelegate)servant._get_delegate())
                        ._invoke(servant,
                                 operation,
                                 request.getInputStream(),
                                 request);
                }
                else
                {
                    ((DynamicImplementation) servant).invoke(request);
                }
            }
            else
            {
                if (logger.isWarnEnabled())
                {
                    logger.warn("rid: " + request.requestId() +
                                " opname: " + operation +
                                " unknown servant type (neither stream nor dsi based)");
                }
            }

        }
        catch (org.omg.CORBA.SystemException e)
        {
            if (logger.isInfoEnabled())
            {
                logger.info("rid: " + request.requestId() +
                            " opname: " + operation +
                            " invocation: system exception was thrown.",
                            e);
            }
            request.setSystemException(e);
        }
        catch (OutOfMemoryError e)
        {
            /* not spec. */
            if (logger.isErrorEnabled())
            {
                logger.error("rid: " + request.requestId() +
                             " opname: " + operation +
                             " invocation: Caught OutOfMemory invoking operation.",
                             e);
            }
            request.setSystemException (new org.omg.CORBA.NO_MEMORY(e.toString()));
        }
        catch (Throwable e)
        {
            /* not spec. */
            if (logger.isErrorEnabled())
            {
                logger.error("rid: " + request.requestId() +
                             " opname: " + operation +
                             " invocation: throwable was thrown.",
                             e);
            }
            request.setSystemException (new org.omg.CORBA.UNKNOWN(e.toString()));
        }
        finally
        {
	    if (useServantClassLoader)
            {
                // Restore the original TCCL.
                currentThread.setContextClassLoader(prevClassLoader);
            }
        }
    }


    /**
     * performs the postinvoke call on a servant locator
     */

    private void invokePostInvoke()
    {
        try
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("rid: " + request.requestId() +
                             " opname: " + request.operation() +
                             " invoke postinvoke on servant locator");
            }

            ((ServantLocator) servantManager).postinvoke(request.objectId(),
                                                         controller.getPOA(),
                                                         request.operation(),
                                                         cookieHolder.value,
                                                         servant);
        }
        catch (org.omg.CORBA.SystemException e)
        {
            if (logger.isInfoEnabled())
            {
                logger.info("rid: " + request.requestId() +
                            " opname: " + request.operation() +
                            " postinvoke: system exception was thrown.",
                            e);
            }
            request.setSystemException(e);

        }
        catch (Throwable e)
        {
            /* not spec. */
            if (logger.isWarnEnabled())
            {
                logger.warn("rid: " + request.requestId() +
                            " opname: " + request.operation() +
                            " postinvoke: throwable was thrown.",
                            e);
            }
            request.setSystemException(new org.omg.CORBA.OBJ_ADAPTER(e.toString()));
            /* which system exception I should raise? */
        }
    }


    /**
     * performs the preinvoke call on a servant locator
     */

    private void invokePreInvoke()
    {
        if (logger.isDebugEnabled())
        {
            logger.debug("rid: " + request.requestId() +
                         " opname: " + request.operation() +
                         " invoke preinvoke on servant locator");
        }
        try
        {
            cookieHolder = new CookieHolder();
            servant = ((ServantLocator) servantManager).preinvoke(request.objectId(),
                                                                  controller.getPOA(),
                                                                  request.operation(),
                                                                  cookieHolder);
            if (servant == null)
            {
                if (logger.isWarnEnabled())
                {
                    logger.warn("rid: " + request.requestId() +
                                " opname: " + request.operation() +
                                " preinvoke: returns null");
                }
                request.setSystemException(new org.omg.CORBA.OBJ_ADAPTER());
            }
            controller.getORB().set_delegate( servant );        // set the orb
        }
        catch (org.omg.CORBA.SystemException e)
        {
            if (logger.isInfoEnabled())
            {
                logger.info("rid: " + request.requestId() +
                            " opname: " + request.operation() +
                            " preinvoke: system exception was thrown.",
                            e);
            }
            request.setSystemException(e);

        }
        catch (org.omg.PortableServer.ForwardRequest e)
        {
            if (logger.isInfoEnabled())
            {
                logger.info("rid: " + request.requestId() +
                            " opname: " + request.operation() +
                            " preinvoke: forward exception was thrown.",
                            e);
            }
            request.setLocationForward(e);
        }
        catch (Throwable e)
        {
            /* not spec. */
            if (logger.isWarnEnabled())
            {
                logger.warn("rid: " + request.requestId() +
                            " opname: " + request.operation() +
                            " preinvoke: throwable was thrown.",
                            e);
            }
            request.setSystemException(new org.omg.CORBA.OBJ_ADAPTER(e.toString()));
            /* which system exception I should raise? */
        }
    }

    boolean isActive()
    {
        return start;
    }


    /**
     * the main request processing routine
     */

    private void process()
    {
        final ORB orb = controller.getORB();
        ServerRequestInfoImpl info = null;

        // Notify parties interested in using information about a Transport
        orb.notifyTransportListeners(request.getConnection());

        if (orb.hasServerRequestInterceptors())
        {
            //RequestInfo attributes
            info = new ServerRequestInfoImpl(orb,
                                             request,
                                             servant);

            InterceptorManager manager = orb.getInterceptorManager();
            info.setCurrent (manager.getEmptyCurrent());

            if(! invokeInterceptors( info,
                                     ServerInterceptorIterator.
                                     RECEIVE_REQUEST_SERVICE_CONTEXTS))
            {
                //an interceptor bailed out, so don't continue request
                //processing and return here. The service contexts for
                //the result have to be set, of course.
                ReplyOutputStream out = request.getReplyOutputStream();
                Collection<ServiceContext> ctx = info.getReplyServiceContexts ();

                for (ServiceContext s: ctx)
                {
                    out.addServiceContext(s);
                }

                return;
            }

            manager.setTSCurrent(info.current());
        }

        // TODO: The exception replies below should also trigger interceptors.
        // Requires some re-arranging of the entire method.
        if (Time.hasPassed (request.getRequestEndTime()))
        {
            request.setSystemException
                (new org.omg.CORBA.TIMEOUT ("Request End Time exceeded",
                                            0, CompletionStatus.COMPLETED_NO));
            return;
        }
        if (checkReplyEndTime && Time.hasPassed (request.getReplyEndTime()))
        {
            request.setSystemException
                (new org.omg.CORBA.TIMEOUT ("Reply End Time exceeded",
                                            0, CompletionStatus.COMPLETED_NO));
            return;
        }

        Time.waitFor (request.getRequestStartTime());

        if (servantManager != null)
        {
            if (servantManager instanceof org.omg.PortableServer.ServantActivator)
            {
                invokeIncarnate();
            }
            else
            {
                invokePreInvoke();
            }
            servant._this_object (orb);
        }

        if (servant != null)
        {
            if (info != null)
            {
                info.setServant(servant);

                if (servant instanceof org.omg.CORBA.portable.InvokeHandler)
                {
                    if(! invokeInterceptors(info,
                                            ServerInterceptorIterator.RECEIVE_REQUEST ))
                    {
                        //an interceptor bailed out, so don't continue
                        //request processing and return here. The
                        //service contexts for the result have to be
                        //set, of course.

                        if( cookieHolder != null )
                        {
                            invokePostInvoke();
                        }

                        ReplyOutputStream out = request.getReplyOutputStream();
                        Collection<ServiceContext> ctx = info.getReplyServiceContexts ();

                        for (ServiceContext s: ctx)
                        {
                            out.addServiceContext(s);
                        }

                        return;
                    }
                }
                else if (servant instanceof org.omg.PortableServer.DynamicImplementation)
                {
                    request.setServerRequestInfo(info);
                }
            }

            invokeOperation();
        }

        // preinvoke and postinvoke are always called in pairs
        // but what happens if the servant is null

        if (cookieHolder != null)
        {
            invokePostInvoke();
        }

        if (checkReplyEndTime && Time.hasPassed (request.getReplyEndTime()))
        {
            request.setSystemException
                (new org.omg.CORBA.TIMEOUT ("Reply End Time exceeded after invocation",
                                            0, CompletionStatus.COMPLETED_YES));
        }

        if (info != null)
        {
            InterceptorManager manager =
                orb.getInterceptorManager();
            info.setCurrent (manager.getCurrent());

            short op = 0;
            switch(request.status().value())
            {
                case ReplyStatusType_1_2._NO_EXCEPTION :
                    op = ServerInterceptorIterator.SEND_REPLY;
                    info.setReplyStatus (SUCCESSFUL.value);
                    break;

                case ReplyStatusType_1_2._USER_EXCEPTION :
                    info.setReplyStatus (USER_EXCEPTION.value);
                    SystemExceptionHelper.insert(info.sending_exception,
                                                 new org.omg.CORBA.UNKNOWN("Stream-based UserExceptions are not available!"));
                    op = ServerInterceptorIterator.SEND_EXCEPTION;
                    break;

                case ReplyStatusType_1_2._SYSTEM_EXCEPTION :
                    info.setReplyStatus (SYSTEM_EXCEPTION.value);
                    SystemExceptionHelper.insert(info.sending_exception,
                                                 request.getSystemException());
                    op = ServerInterceptorIterator.SEND_EXCEPTION;
                    break;

                case ReplyStatusType_1_2._LOCATION_FORWARD :
                    info.setReplyStatus (LOCATION_FORWARD.value);
                    op = ServerInterceptorIterator.SEND_OTHER;
                    break;
            }

            invokeInterceptors(info, op);

            ReplyOutputStream out = request.get_out();
            Collection<ServiceContext> ctx = info.getReplyServiceContexts ();

            for (ServiceContext s: ctx)
            {
                out.addServiceContext(s);
            }

            manager.removeTSCurrent();
        }
    }

    private boolean invokeInterceptors( ServerRequestInfoImpl info,
                                        short op )
    {

        ServerInterceptorIterator intercept_iter =
            controller.getORB().getInterceptorManager().getServerIterator();

        try
        {
            intercept_iter.iterate(info, op);
        }
        catch(org.omg.CORBA.UserException ue)
        {
            if (ue instanceof org.omg.PortableInterceptor.ForwardRequest)
            {
                org.omg.PortableInterceptor.ForwardRequest fwd =
                    (org.omg.PortableInterceptor.ForwardRequest) ue;

                request.setLocationForward( new org.omg.PortableServer.
                    ForwardRequest(fwd.forward) );
            }
            return false;

        }
        catch (org.omg.CORBA.SystemException _sys_ex)
        {
            request.setSystemException(_sys_ex);
            return false;
        }
        catch(Exception e)
        {
            logger.error("unexpected exception during interceptor invocation", e);

            UNKNOWN exception = new UNKNOWN( e.getMessage() );
            request.setSystemException( exception );
            return false;
        }

        return true;
    }


    /**
     * the main loop for request processing
     */

    public void run()
    {
        while (true)
        {
            synchronized (this)
            {
                while (!terminate && !start)
                {
                    try
                    {
                        wait(); /* waits for the next task */
                    }
                    catch (InterruptedException e)
                    {
                        // ignored
                    }
                }

                if (terminate)
                {
                    return;
                }
            }

            if (logger.isDebugEnabled())
            {
                logger.debug("rid: " + request.requestId() +
                             " opname: " + request.operation() +
                             " starts with request processing");
            }

            if (request.syncScope() == org.omg.Messaging.SYNC_WITH_SERVER.value)
            {
                controller.returnResult (request);
                process();
            }
            else
            {
                process();
                controller.returnResult (request);
            }

            // return the request to the request controller
            if (logger.isDebugEnabled())
            {
                logger.debug("rid: " + request.requestId() +
                             " opname: " + request.operation() +
                             " ends with request processing");
            }

            controller.finish  (request);

            start = false;
            clear();

            // give back the processor into the pool
            poolManager.releaseProcessor(this);
        }
    }
}
