package org.jacorb.poa;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-98  Gerald Brose.
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

import org.jacorb.poa.util.*;
import org.jacorb.poa.except.*;

import org.jacorb.util.Environment;
import org.jacorb.orb.dsi.ServerRequest;
import org.jacorb.orb.BasicAdapter;

import org.omg.PortableServer.POAManagerPackage.State;
import org.omg.PortableServer.Servant;
import org.omg.PortableServer.ServantManager;
import org.omg.PortableServer.ServantActivator;

import java.util.Hashtable;
import java.util.Vector;
import java.util.Enumeration;

/**
 * This class manages all request processing affairs. The main thread takes the
 * requests out from the queue and will see that the necessary steps are taken.
 *
 * @author Reimo Tiedemann, FU Berlin
 * @version 1.11, 10/26/99, RT $Id$
 */
public class RequestController 
    extends Thread 
{       
    private POA 			poa;
    private org.jacorb.orb.ORB		orb;
    private RequestQueue 		requestQueue;
    private AOM 			aom;
    private RPPoolManager		poolManager;

    /*  a  singleton   for  all  POA's  in  one   virtual  machine  if
       SINGLE_THREAD_MODEL is in use */
    private static RPPoolManager 	singletonPoolManager;

    private LogTrace                    logTrace;	

    // stores all active requests	
    private Hashtable 			activeRequestTable;
    // RequestProcessor -> oid
    // for synchronisation with the object deactiviation process
    private Vector 			deactivationList = new Vector();
    // oid's

    // other synchronisation stuff
    private boolean			terminate;
    private boolean 			waitForCompletionCalled;
    private boolean 			waitForShutdownCalled;
    private java.lang.Object 		queueLog = new java.lang.Object();
    private String                      priorityProp = Environment.getProperty("jacorb.poa.thread_priority");
    private int                         threadPriority = Thread.MAX_PRIORITY;

    /**
     */

    private RequestController() 
    {
    }

    /**
     */

    RequestController( POA _poa, org.jacorb.orb.ORB _orb, 
                       AOM _aom, LogTrace _logTrace) 
    {
        poa = _poa;
        aom = _aom;
        orb = _orb;
        logTrace = _logTrace;
		
        requestQueue = new RequestQueue(this, logTrace);
        activeRequestTable = 
            poa.isSingleThreadModel() ? new Hashtable(1) : new Hashtable(Environment.threadPoolMax());
        getPoolManager();

        if( priorityProp != null )
            threadPriority = Integer.parseInt( priorityProp );

        if( threadPriority < Thread.MIN_PRIORITY )
            threadPriority = Thread.MIN_PRIORITY;
        else if( threadPriority > Thread.MIN_PRIORITY )
            threadPriority = Thread.MAX_PRIORITY;

        setPriority(threadPriority);	
        setDaemon(true);
        start();
    }

    void clearUpPool() 
    {
        getPoolManager().destroy();
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

    synchronized void freeObject( byte[] oid ) 
    {
        deactivationList.removeElement(POAUtil.oid_to_bak(oid));	
    }

    AOM getAOM() 
    {
        return aom;
    }


    LogTrace getLogTrace() 
    {
        return logTrace;
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
        if (poolManager == null) 
        {
            if (poa.isSingleThreadModel()) 
            {
                if (singletonPoolManager == null) 
                {
                    singletonPoolManager = new RPPoolManager(orb.getPOACurrent(), 1, 1);
                }
                poolManager = singletonPoolManager;
				
            } 
            else 
            {
                poolManager = 
                    new RPPoolManager(orb.getPOACurrent(), 
                                      Environment.threadPoolMin(),
                                      Environment.threadPoolMax());
            }
        }
        return poolManager;
    }


    RequestQueue getRequestQueue() {
        return requestQueue;
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
		
        synchronized (this) 
        {
			
            if (waitForCompletionCalled) 
            {
                /* state has changed to holding, discarding or inactive */
                if (logTrace.test(2))
                    logTrace.printLog(request, "cannot process request, because waitForCompletion was called");
                throw new CompletionRequestedException();
            }
			
            if (waitForShutdownCalled) 
            { 
                /* poa goes shutdown */
                if (logTrace.test(2))
                    logTrace.printLog(request, "cannot process request, because the poa goes shutdown");
                throw new ShutdownInProgressException();
            }
			
            /* below this point it's save that the poa is active */ 

            if (deactivationList.contains(POAUtil.oid_to_bak(request.objectId()))) 
            {
                if (!poa.isUseServantManager() && !poa.isUseDefaultServant()) 
                {
                	if (logTrace.test(0))
                        logTrace.printLog(request, "cannot process request, because object is already in the deactivation process");
                    throw new org.omg.CORBA.OBJECT_NOT_EXIST();
                }
                invalid = true;
            }
			
            /* below this point it's save  that the object is not in a
               deactivation process */
			
            if (!invalid && poa.isRetain()) 
            {
                servant = aom.getServant(request.objectId());
            }

            if (servant == null) 
            {
                if (poa.isUseDefaultServant()) 
                {
                    if ((servant = poa.defaultServant) == null) 
                    {
                    	if (logTrace.test(0))
                            logTrace.printLog(request, "cannot process request, because default servant is not set");
                        throw new org.omg.CORBA.OBJ_ADAPTER();
                    }

                } 
                else if (poa.isUseServantManager()) 
                {
                    if ((servantManager = poa.servantManager) == null) 
                    {
                    	if (logTrace.test(0))
                            logTrace.printLog(request, "cannot process request, because servant manager is not set");
                        throw new org.omg.CORBA.OBJ_ADAPTER();
                    }

				// USE_OBJECT_MAP_ONLY is in effect but object not exists
                } 
                else 
                {
                	if (logTrace.test(2))
                        logTrace.printLog(request, "cannot process request, because object doesn't exist");
                    throw new org.omg.CORBA.OBJECT_NOT_EXIST();
                }
            }

            /* below  this point it's  save that the request  is valid
               (all preconditions can be met) */
            activeRequestTable.put(request, POAUtil.oid_to_bak(request.objectId()));
        }

        // get and initialize a processor for request processing
        if (logTrace.test(3))
            logTrace.printLog(request, "trying to get a RequestProcessor");
        RequestProcessor processor = getPoolManager().getProcessor();
        processor.init(this, request, servant, servantManager);	
        processor.begin();	
    }


    void queueRequest(ServerRequest request) 
        throws ResourceLimitReachedException 
    {
        requestQueue.add(request);
    }

    /**
     * this method calls the basic adapter and hands out the request if
     * something went wrong, the specified system exception will set
     */

    void rejectRequest(ServerRequest request, org.omg.CORBA.SystemException exception) 
    {
        if (exception != null) 
            request.setSystemException(exception);

        orb.getBasicAdapter().return_result(request);
        if (logTrace.test(2))
            logTrace.printLog(request, " request rejected with exception: "+exception);
    }

    /**
     * resets a previous waitForCompletion call,
     * everybody who is waiting will notified
     */

    synchronized void resetPreviousCompletionCall() 
    {
    	if (logTrace.test(6))
            logTrace.printLog("reset a previous completion call");
        waitForCompletionCalled = false;
        notifyAll(); /* maybe somebody waits for completion */
    }

    /**
     * called from request processor if the request comes to an end,
     * this method calls the basic adapter and removes
     * the request from the active request table
     */

    void returnResult(ServerRequest request) 
    {
        orb.getBasicAdapter().return_result(request);
        synchronized (this) 
        {
            activeRequestTable.remove(request);
            notifyAll();
        }
    }

    /**
     * the main loop for dispatching requests to request processors
     */

    public void run() 
    {
        State state;
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
                    }
                    continue;
                }
            } 
            else
                if (!waitForShutdownCalled && (POAUtil.isDiscarding(state) || POAUtil.isInactive(state))) {
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
				/*  if waitForShutdown was  called the
				 RequestController loop blocks for ALL
				 TIME in waitForQueue (the poa behaves
				 as  if he  is in  holding  state now)
				 ATTENTION,      it's      a      lazy
				 synchronisation,  a request  could be
				 rejected   if   waitForShutdown   was
				 called  but   couldn't  be  processed
				 (it's save) */
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
            	if (logTrace.test(6))
                    logTrace.printLog("somebody waits for completion and there are active processors");
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
     *  started from  now  on  because a  steady  stream of  incomming
     *  requests  could keep  the  object  from  being deactivated,  a
     *  servant may  invoke recursive  method calls  on the  object it
     *  incarnates  and deactivation  should  not necessarily  prevent
     * those invocations.  
     */

    synchronized void waitForObjectCompletion( byte[] oid ) 
    {
        ByteArrayKey oidbak = POAUtil.oid_to_bak(oid);			
        while (activeRequestTable.contains(oidbak)) 
        {
            try 
            {
                wait();
            }
            catch (InterruptedException e) 
            {
            }
        }		
        if (logTrace.test(6))
            logTrace.printLog(oid, "all active processors for this object have finished");		
        deactivationList.addElement(oidbak);
    }

    /**
     * blocks the request controller thread if the queue is empty,
     * the poa is in holding state or waitForShutdown was called,
     * if waitForShutdown was called the RequestController loop blocks for
     * ALL TIME in this method (the poa behaves as if he is in holding state now)
     */

    private void waitForQueue() 
    {
        synchronized (queueLog) 
        {
            if ((requestQueue.isEmpty() || poa.isHolding() || waitForShutdownCalled) && 
                !terminate) 
            {
                try 
                {
                	if (logTrace.test(6))
                        logTrace.printLog("the RequestController goes to sleep");
                    queueLog.wait();
                } 
                catch (java.lang.InterruptedException e) {
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
		
        while (waitForShutdownCalled && !activeRequestTable.isEmpty()) 
        {
            try 
            {
            	if (logTrace.test(6))
                    logTrace.printLog("somebody waits for shutdown and there are active processors");
                wait();
            } 
            catch (InterruptedException e) 
            {
            }
        }
    }
}







