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

import java.util.HashSet;
import org.jacorb.config.Configurable;
import org.jacorb.config.Configuration;
import org.jacorb.config.ConfigurationException;
import org.jacorb.orb.dsi.ServerRequest;
import org.jacorb.orb.util.CorbaLoc;
import org.jacorb.poa.except.CompletionRequestedException;
import org.jacorb.poa.except.ResourceLimitReachedException;
import org.jacorb.poa.except.ShutdownInProgressException;
import org.jacorb.poa.util.ByteArrayKey;
import org.jacorb.poa.util.POAUtil;
import org.omg.PortableServer.Servant;
import org.omg.PortableServer.ServantManager;
import org.slf4j.Logger;

/**
 * This class manages all request processing affairs. The main thread takes the
 * requests out from the queue and will see that the necessary steps are taken.
 *
 * @author Reimo Tiedemann
 * @version $Id$
 */

public final class RequestController
    extends Thread
    implements Configurable
{
    private final POA                poa;
    private final org.jacorb.orb.ORB orb;
    private final RequestQueue       requestQueue;
    private final AOM                aom;
    private final RPPoolManager	     poolManager;
    private int                      localRequests = 0;

    private static int count = 0;

    /** the configuration object for this controller */
    private org.jacorb.config.Configuration configuration = null;

    /** this controller's logger instance */
    private Logger                    logger;

    // stores all active requests
    private HashSet             activeRequestTable;
    // RequestProcessor -> oid
    // for synchronisation with the object deactiviation process
    private final HashSet             deactivationList = new HashSet();
    // oid's

    // other synchronisation stuff
    private boolean		   terminate;
    private boolean 		   waitForCompletionCalled;
    private boolean 		   waitForShutdownCalled;
    private final java.lang.Object queueLog       = new java.lang.Object();
    private int                    threadPriority = Thread.MAX_PRIORITY;

    RequestController( POA _poa,
            org.jacorb.orb.ORB _orb,
            AOM _aom,
            RPPoolManager _poolManager)
    {
        super("RequestController-" + (++count));
        poa = _poa;
        aom = _aom;
        orb = _orb;
        poolManager = _poolManager;

        requestQueue = new RequestQueue();
    }

    public void configure(Configuration myConfiguration)
        throws ConfigurationException
    {
        this.configuration =
            (org.jacorb.config.Configuration)myConfiguration;

        logger = configuration.getLogger("jacorb.poa.controller");

        int threadPoolMax =
            orb.getConfiguration().getAttributeAsInteger("jacorb.poa.thread_pool_max", 20);

        activeRequestTable = poa.isSingleThreadModel() ? new HashSet(1) : new HashSet(threadPoolMax);

        requestQueue.configure(myConfiguration);

        threadPriority =
            configuration.getAttributeAsInteger("jacorb.poa.thread_priority",
                                                Thread.MAX_PRIORITY);

        if( threadPriority < Thread.MIN_PRIORITY )
        {
            threadPriority = Thread.MIN_PRIORITY;
        }
        else if( threadPriority > Thread.MAX_PRIORITY )
        {
            threadPriority = Thread.MAX_PRIORITY;
        }

        setPriority(threadPriority);
        setDaemon(true);

        start();
    }


    void clearUpPool()
    {
        poolManager.destroy();
    }

    /**
     * rejects all queued requests with specified system exception
     */

    void clearUpQueue(org.omg.CORBA.SystemException exception)
    {
        ServerRequest request;
        while ((request = requestQueue.removeLast()) != null)
        {
            rejectRequest(request, exception);
        }
    }

    /**
     * indicates that the assumptions for blocking the
     * request controller thread have changed,
     * a waiting request controller thread will notified
     */

    void continueToWork()
    {
        synchronized (queueLog)
        {
            queueLog.notifyAll();
        }
    }

    synchronized void end()
    {
        terminate = true;
        continueToWork();
    }

    /**
     * frees an object from the deactivation in progress state,
     * a call indicates that the object deactivation process is complete
     */

    synchronized void freeObject(ByteArrayKey oid)
    {
        deactivationList.remove( oid );
    }

    AOM getAOM()
    {
        return aom;
    }


    Logger getLogger()
    {
        return logger;
    }


    org.jacorb.orb.ORB getORB()
    {
        return orb;
    }


    POA getPOA()
    {
        return poa;
    }


    RPPoolManager getPoolManager()
    {
        return poolManager;
    }


    RequestQueue getRequestQueue() {
        return requestQueue;
    }


    synchronized boolean isDeactivating (ByteArrayKey oid)
    {
        return deactivationList.contains( oid );
    }


    /**
     * requests will dispatched to request processors,
     * attention, if the processor pool is empty, this method returns only
     * if the getProcessor() method from RequestProcessorPool can satisfied
     */

    private void processRequest(ServerRequest request)
        throws ShutdownInProgressException, CompletionRequestedException
    {
        Servant servant = null;
        ServantManager servantManager = null;
        boolean invalid = false;
        final ByteArrayKey oid = request.objectIdAsByteArrayKey();

        synchronized (this)
        {
            if (waitForCompletionCalled)
            {
                /* state has changed to holding, discarding or inactive */

                if (logger.isInfoEnabled())
                {
                    logger.info("rid: " + request.requestId() +
                                " opname: " + request.operation() +
                                " cannot process request because waitForCompletion was called");
                }
                throw new CompletionRequestedException();
            }

            if (waitForShutdownCalled)
            {
                /* poa goes down */
                if (logger.isInfoEnabled())
                {
                    logger.info("rid: " + request.requestId() +
                                " opname: " + request.operation() +
                                " cannot process request because POA shutdown in progress");
                }
                throw new ShutdownInProgressException();
            }

            /* below this point it's save that the poa is active */

            if ((aom != null && aom.isDeactivating( oid )) ||
                deactivationList.contains( oid ))
            {
                if (!poa.isUseServantManager() && !poa.isUseDefaultServant())
                {
                    if (logger.isInfoEnabled())
                    {
                        logger.info("rid: " + request.requestId() +
                                    " opname: " + request.operation() +
                                    " objectKey: " + CorbaLoc.parseKey (request.objectKey ()) +
                                    " cannot process request, because object is already in the deactivation process");
                    }

                    throw new org.omg.CORBA.OBJECT_NOT_EXIST();
                }
                invalid = true;
            }

            /* below this point it's save  that the object is not in a
               deactivation process */

            if (!invalid && poa.isRetain())
            {
                servant = aom.getServant(oid);
            }

            if (servant == null)
            {
                if (poa.isUseDefaultServant())
                {
                    if ((servant = poa.defaultServant) == null)
                    {
                        if (logger.isWarnEnabled())
                        {
                            logger.warn("rid: " + request.requestId() +
                                        " opname: " + request.operation() +
                                        " cannot process request because default servant is not set");
                        }
                        throw new org.omg.CORBA.OBJ_ADAPTER();
                    }

                }
                else if (poa.isUseServantManager())
                {
                    if ((servantManager = poa.servantManager) == null)
                    {
                        if (logger.isWarnEnabled())
                        {
                            logger.warn("rid: " + request.requestId() +
                                        " opname: " + request.operation() +
                                        " cannot process request because servant manager is not set");
                        }
                        throw new org.omg.CORBA.OBJ_ADAPTER();
                    }
                    // USE_OBJECT_MAP_ONLY is in effect but object not exists
                }
                else
                {
                    if (logger.isWarnEnabled())
                    {
                       logger.warn("rid: " + request.requestId() +
                                   " opname: " + request.operation() +
                                   " connection: " + request.getConnection ().toString () +
                                   " objectKey: " + CorbaLoc.parseKey (request.objectKey ()) +
                                   " cannot process request, because object doesn't exist");
                    }
                    throw new org.omg.CORBA.OBJECT_NOT_EXIST();
                }
            }
            /* below  this point it's  save that the request  is valid
               (all preconditions can be met) */
            activeRequestTable.add(oid);
        }

        // get and initialize a processor for request processing
        if (logger.isDebugEnabled())
        {
            logger.debug("rid: " + request.requestId() +
                         " opname: " + request.operation() +
                         " trying to get a RequestProcessor");
        }

        RequestProcessor processor = poolManager.getProcessor();
        processor.init(this, request, servant, servantManager);
        processor.begin();
    }


    void queueRequest(ServerRequest request)
        throws ResourceLimitReachedException
    {
        requestQueue.add(request);

        if (requestQueue.size() == 1)
        {
            continueToWork();
        }
    }

    /**
     * this method calls the basic adapter and hands out the request if
     * something went wrong, the specified system exception will set
     */

    void rejectRequest(ServerRequest request,
                       org.omg.CORBA.SystemException exception)
    {
        if (exception != null)
            request.setSystemException(exception);

        orb.getBasicAdapter().return_result(request);

        if (logger.isWarnEnabled())
        {
            logger.warn("rid: " + request.requestId() +
                        " opname: " + request.operation() +
                        " request rejected with exception: " +
                        exception.getMessage());
        }
    }

    /**
     * resets a previous waitForCompletion call,
     * everybody who is waiting will notified
     */

    synchronized void resetPreviousCompletionCall()
    {
        logger.debug("reset a previous completion call");

        waitForCompletionCalled = false;
        notifyAll(); /* maybe somebody waits for completion */
    }

    /**
     * Sends the reply of the given request via the BasicAdapter.
     */
    void returnResult(ServerRequest request)
    {
        orb.getBasicAdapter().return_result(request);
    }

    /**
     * Called from RequestProcessor when the request has been handled.
     * The request is removed from the active request table.
     */
    synchronized void finish (ServerRequest request)
    {
        final ByteArrayKey oid = request.objectIdAsByteArrayKey();
        activeRequestTable.remove (oid);
        notifyAll();
    }

    /**
     * the main loop for dispatching requests to request processors
     */

    public void run()
    {
        org.omg.PortableServer.POAManagerPackage.State state;
        ServerRequest request;
        org.omg.CORBA.OBJ_ADAPTER closed_connection_exception =
            new org.omg.CORBA.OBJ_ADAPTER("connection closed: adapter inactive");

        org.omg.CORBA.TRANSIENT transient_exception = new org.omg.CORBA.TRANSIENT();
        while (!terminate)
        {
            state = poa.getState();
            if (POAUtil.isActive(state))
            {
                request = requestQueue.getFirst();

                /* Request available */
                if (request != null)
                {
                    if (request.remainingPOAName() != null)
                    {
                        orb.getBasicAdapter().deliverRequest(request, poa);
                        requestQueue.removeFirst();
                    }
                    else
                    {
                        try
                        {
                            processRequest(request);
                            requestQueue.removeFirst();
                        }
                        catch (CompletionRequestedException e)
                        {
                            /* if waitForCompletion was called the poa
                               state    was   changed    to   holding,
                               discarding or  inactive, the loop don't
                               block  in   waitForContinue,  the  loop
                               continues  and will detect  the changed
                               state in  the next turn  (for this turn
                               the request will not processed) */
                        }
                        catch (ShutdownInProgressException e)
                        {
                            /* waitForShutdown was called */
                            waitForQueue();
                        }
                        catch (org.omg.CORBA.OBJ_ADAPTER e)
                        {
                            requestQueue.removeFirst();
                            rejectRequest(request, e);
                        }
                        catch (org.omg.CORBA.OBJECT_NOT_EXIST e)
                        {
                            requestQueue.removeFirst();
                            rejectRequest(request, e);
                        }
                        catch (org.omg.CORBA.TIMEOUT e)
                        {
                            requestQueue.removeFirst();
                            rejectRequest(request, e);
                        }
                    }
                    continue;
                }
            }
            else
            {
                if (!waitForShutdownCalled && (POAUtil.isDiscarding(state) || POAUtil.isInactive(state)))
                {
                    request = requestQueue.removeLast();

                    /* Request available */
                    if (request != null)
                    {
                        if (POAUtil.isDiscarding(state))
                        {
                            rejectRequest(request, transient_exception);
                        }
                        else
                        {
                            rejectRequest(request, closed_connection_exception);
                        }
                        continue;
                    }
                }
            }
            /* if waitForShutdown was called the RequestController
               loop blocks for ALL TIME in waitForQueue (the poa
               behaves as if he is in holding state now) ATTENTION,
               it's a lazy synchronisation, a request could be
               rejected if waitForShutdown was called but couldn't be
               processed (it's save)
            */
            waitForQueue();
        }
    }

    /**
     * called from external thread for synchronizing with the
     * request controller thread,
     * a caller waits for completion of all active requests,
     * no new requests will started from now on
     */

    synchronized void waitForCompletion()
    {
        waitForCompletionCalled = true;

        while (waitForCompletionCalled && !activeRequestTable.isEmpty())
        {
            try
            {
                logger.debug("somebody waits for completion and there are active processors");
                wait();
            }
            catch (InterruptedException e)
            {
            }
        }
    }

    /**
     * called from external  thread for synchronizing with the request
     * controller thread, a caller  waits for completion of all active
     * requests on this object. No new requests on this object will be
     *  started from  now  on  because a  steady  stream of  incoming
     *  requests  could keep  the  object  from  being deactivated,  a
     *  servant may  invoke recursive  method calls  on the  object it
     *  incarnates  and deactivation  should  not necessarily  prevent
     * those invocations.
     */

    synchronized void waitForObjectCompletion( ByteArrayKey oid )
    {
        while (activeRequestTable.contains(oid))
        {
            try
            {
                wait();
            }
            catch (InterruptedException e)
            {
            }
        }
        if (logger.isDebugEnabled())
        {
            logger.debug( POAUtil.convert(oid.getBytes ()) +
                          "all active processors for this object have finished");

        }

        deactivationList.add( oid );
    }

    /**
     * blocks the request controller thread if the queue is empty,
     * the poa is in holding state or waitForShutdown was called,
     * if waitForShutdown was called the RequestController loop blocks for
     * ALL TIME in this method (the poa behaves as if he is in holding state now)
     */

    private void waitForQueue()
    {
        if (logger.isDebugEnabled())
        {
            logger.debug("waiting for queue");
        }

        synchronized (queueLog)
        {
            while ((requestQueue.isEmpty() ||
                    POAUtil.isHolding (poa.the_POAManager().get_state()) ||
                    waitForShutdownCalled) &&
                   !terminate)
            {
                try
                {
                    queueLog.wait();
                }
                catch (java.lang.InterruptedException e)
                {
                    // ignored
                }
            }
        }
    }

    /**
     * called from external thread for synchronizing with the
     * request controller thread,
     * a caller waits for completion of all active requests,
     * no new requests will started for ALL TIME
     */
    synchronized void waitForShutdown()
    {
        waitForShutdownCalled = true;

        while ((waitForShutdownCalled && ! activeRequestTable.isEmpty())
               || (localRequests > 0)
        )
        {
            try
            {
                if (logger.isDebugEnabled())
                {
                    logger.debug("somebody waits for shutdown and there are active processors");
                }
                wait();
            }
            catch (InterruptedException e)
            {
            }
        }
    }

    synchronized void addLocalRequest()
    {
        localRequests++;
    }

    synchronized void removeLocalRequest()
    {
        localRequests--;
        notifyAll();
    }
}
