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

package org.jacorb.util;

import java.io.IOException;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.spi.SelectorProvider;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NavigableSet;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.jacorb.config.Configuration;
import org.jacorb.config.ConfigurationException;
import org.slf4j.Logger;

/**
 * Defines the entity which waits for I/O and time events to occur and
 * dispatches notifications to the interested request callbacks. This is
 * used primarily to manage asynchronous I/O using the NIO package but
 * could be used to help orchestrate other sorts of asynch event handling
 * in the future.
 *
 * @auther Ciju John <johnc@ociweb.com>
 */
public class SelectorManager extends Thread
{
    final private HashMap<SelectorRequest.Type, RequestorPool> pools;

    final private ConcurrentSkipListSet<SelectorRequest> timeOrderedRequests;
    final private ConcurrentLinkedQueue<SelectorRequest> canceledRequests;
    final private ConcurrentLinkedQueue<SelectorRequest> newRequests;
    final private ConcurrentLinkedQueue<SelectorRequest> reActivateBuffer;

    final private Selector selector;
    private boolean running;
    private Logger logger;

    final private ExecutorService executor;
    private int threadPoolMin = 2;
    private int threadPoolMax = 10;
    private int threadPoolKeepAliveTime = 60; // seconds
    private int executorPendingQueueSize = 5;

    private boolean loggerDebugEnabled = false;
    private final Object runLock = new Object();

    class TimeOrderedComparitor implements Comparator<SelectorRequest>
    {
        @Override
        public int compare(SelectorRequest arg0, SelectorRequest arg1)
        {
            long x = (arg0.nanoDeadline - arg1.nanoDeadline);
            return x == 0 ? 0 : x > 0 ? 1 : -1;
        }
    }

    /**
     * Constructs a new Selector Manager. Typically called by the ORB
     */
    public SelectorManager ()
    {

        running = true;

        try
        {
            pools = new HashMap<SelectorRequest.Type, RequestorPool>(4);
            pools.put(SelectorRequest.Type.CONNECT, new RequestorPool());
            pools.put(SelectorRequest.Type.ACCEPT, new RequestorPool());
            pools.put(SelectorRequest.Type.READ, new RequestorPool());
            pools.put(SelectorRequest.Type.WRITE, new RequestorPool());

            timeOrderedRequests = new ConcurrentSkipListSet<SelectorRequest> (new TimeOrderedComparitor());
            canceledRequests = new ConcurrentLinkedQueue<SelectorRequest> ();
            newRequests = new ConcurrentLinkedQueue<SelectorRequest> ();
            reActivateBuffer = new ConcurrentLinkedQueue<SelectorRequest> ();

            selector = SelectorProvider.provider().openSelector ();
            executor = new ThreadPoolExecutor (threadPoolMin,
                                               threadPoolMax,
                                               threadPoolKeepAliveTime,
                                               TimeUnit.SECONDS,
                                               new ArrayBlockingQueue<Runnable>(executorPendingQueueSize),
                                               new SelectorManagerThreadFactory(),
                                               new ThreadPoolExecutor.CallerRunsPolicy());
        }
        catch (IOException ex)
        {
            throw new RuntimeException (ex);
        }
    }

    /**
     * Gives the SelectorManager an oportunity to configure
     * itself. Follows the pattern common to JacORB objects.
     */
    public void configure(Configuration configuration)
    throws ConfigurationException
    {

        if (configuration == null)
        {
            throw new ConfigurationException ("SelectorManager.configure was " +
                                              "passed a null Configuration object");
        }

        logger = configuration.getLogger("jacorb.util");
        loggerDebugEnabled = logger.isDebugEnabled();
    }

    /**
     * The thread function
     */
    public void run ()
    {

        try
        {
            while (running)
            {
                removeCanceled ();
                insertNew ();
                reactivate ();

                // cleanup expired requests & compute next sleep unit
                long sleepTime = cleanupExpiredRequests ();

                if (loggerDebugEnabled)
                {
                    logger.debug ("Selector will wait for " +
                                (sleepTime == Long.MAX_VALUE ? 0 : sleepTime) +
                                  " millis.");
                }

                try
                {
                    selector.select (sleepTime);
                }
                catch (IllegalArgumentException ex)
                {
                    logger.error ("Select timeout (" + sleepTime
                                  + ") argument flawed: " + ex.toString());
                    // shouldn't be any problem to continue;
                }

                synchronized(runLock)
                {
                    if (!running)
                    {
                        if (loggerDebugEnabled)
                        {
                            logger.debug ("Breaking out of Selector loop; " +
                                    "'running' flag was disabled.");
                        }
                        break;
                    }
                }

                Iterator<SelectionKey> iter = selector.selectedKeys().iterator();
                while (iter.hasNext())
                {
                    SelectionKey key = iter.next();
                    iter.remove();

                    if (key.isValid())
                    {
                        dispatch (key);
                    }
                }
            }
        }
        catch (Exception ex)
        {
            logger.error ("Exception in Selector loop. Bailing out: " +
                          ex.toString());
            ex.printStackTrace ();
        }
        running = false;

        try
        {
            // clean up all pending requests

            if (loggerDebugEnabled)
            {
                logger.debug ("SelectorManager loop is broken. " +
                              "Cleaning up pending requests");
            }
            cleanupAll ();

            if (loggerDebugEnabled)
            {
                logger.debug ("shutting down Threadpool executor.");
            }
            executor.shutdown ();
        }
        catch (Exception ex)
        {
            logger.error ("Selector manager cleanup: " +
                          ex.toString());
            ex.printStackTrace ();
        }
    }

    private void dispatch_i (int op, SelectorRequest.Type jobType, SelectionKey key)
    {
        if (loggerDebugEnabled)
        {
            logger.debug ("Key " + key + " ready for action: " + jobType);
        }

        // disable op bit for SelectionKey
        int newOps = key.interestOps () ^ op;

        try
        {
            key.interestOps (newOps);
        }
        catch (CancelledKeyException ex)
        {
            // let the worker deal with this
        }
        // delegate work to worker thread
        SendJob sendJob = new SendJob (key, jobType);
        FutureTask<Object> task = new FutureTask<Object> (sendJob);
        executor.execute (task);
    }

    /**
     * Called in Selector thread
     */
    private void dispatch (SelectionKey key)
    {
        if (loggerDebugEnabled)
        {
            logger.debug ("dispatch called for: " + key);
        }

        try
        {
            if (key.isConnectable())
            {
                dispatch_i(SelectionKey.OP_CONNECT,SelectorRequest.Type.CONNECT, key);
            }
            if (key.isAcceptable())
            {
                dispatch_i(SelectionKey.OP_ACCEPT, SelectorRequest.Type.ACCEPT, key);
            }
            if (key.isReadable())
            {
                dispatch_i(SelectionKey.OP_READ, SelectorRequest.Type.READ, key);
            }
            if (key.isWritable())
            {
                dispatch_i(SelectionKey.OP_WRITE, SelectorRequest.Type.WRITE, key);
            }
        }
        catch (CancelledKeyException ex)
        {
            // explicit key cancellations are only doen by the
            // selector thread, so the only way we got here is if
            // another thread closed the assocated channel. In that
            // case simple cleanup any requests associated with this
            // key and continue.

            if (loggerDebugEnabled)
            {
                logger.debug ("Cleaning up requests associated with key: " +
                              key.toString());
            }
            cancelKey (key);
        }
    }

    /**
     * Called in Selector thread
     */
    private void cancelKey (SelectionKey key)
    {
        Iterator<RequestorPool> p = pools.values().iterator();
        while (p.hasNext())
        {
            RequestorPool pool = p.next();
            ConcurrentLinkedQueue<SelectorRequest> buffer = pool.remove(key);

            if (buffer != null)
            {
                cleanupBuffer (buffer);
            }
        }
    }

    /**
     * Called in Selector thread
     */
    private void cleanupAll ()
    {
        Iterator<RequestorPool> p = pools.values().iterator();
        while (p.hasNext())
        {
            RequestorPool pool = p.next();
            Iterator<ConcurrentLinkedQueue<SelectorRequest>> e = pool.values();
            while (e.hasNext())
            {
                cleanupBuffer (e.next());
            }
	    }

        cleanupBuffer (reActivateBuffer);
        cleanupBuffer (canceledRequests);
        cleanupBuffer (newRequests);

        // the time ordered requests have already been cleaned up
        // during above cleanup
        timeOrderedRequests.clear ();
    }

    /**
     * Called in Selector thread
     */
    private void cleanupBuffer (ConcurrentLinkedQueue<SelectorRequest> buffer)
    {
        SelectorRequest request = null;
        while ((request = buffer.poll()) != null)
        {
            if (loggerDebugEnabled)
            {
                logger.debug ("Cleaning up request. Request type: " +
                              request.type + ", Request status: " +
                              request.status);
            }
            if (!running)
            {
                request.setStatus (SelectorRequest.Status.SHUTDOWN);
            }

            // delegate work to worker thread
            SendJob sendJob = new SendJob (request);
            FutureTask<Object> task = new FutureTask<Object> (sendJob);
            executor.execute (task);
        }
    }

    /**
     * signals the run loop to terminate
     */
    public synchronized void halt ()
    {
        synchronized (runLock)
        {
            if (!running)
            {
                return;
            }
            else
            {
                if (loggerDebugEnabled)
                {
                    logger.debug ("Halting Selector Manager.");
                }

                running = false;
                selector.wakeup ();
            }
        }

        if (loggerDebugEnabled)
        {
            logger.debug ("Waiting for Threadpool executor to wind down.");
        }
        // wait until all threadpool tasks have finished
        while (!executor.isTerminated())
        {
            try
            {
                executor.awaitTermination (Long.MAX_VALUE, TimeUnit.SECONDS);
            }
            catch (InterruptedException e)
            {
                // ignored
            }
        }
    }

    /**
     * get the current buffer depth for the given type of request
     * @param the request type of interest
     * @return the pool (or requestbuffer) size
     */

    public int poolSize (SelectorRequest.Type type)
    {
        if (type == SelectorRequest.Type.TIMER)
        {
            return timeOrderedRequests.size ();
        }
        RequestorPool p = pools.get (type);
        return p == null ? 0 : p.size();
    }

    /**
     * Remove an existing request before it has expired
     * @param request is the event to be removed from the pool
     */

    public void remove (SelectorRequest request)
    {
        if (request == null || request.type == null)
            return;
        if (newRequests.remove(request))
            return;
        // no need to wake up the selector, since the request was
        // canceled before it was pulled from the new requests
        // list. That means that the previous call to add() had
        // (or will) awaken the selector and just find the new
        // requests list potentially empty.

        canceledRequests.offer (request);

        if (loggerDebugEnabled)
        {
            logger.debug ("Remove request. Request type: "
                          + request.type.toString());
        }

        selector.wakeup ();
    }


    private boolean sendFailure (SelectorRequest request,
                                 SelectorRequest.Status reason)
    {
        request.setStatus (reason);
        if (request.callback == null)
        {
	    return false;
	}
	if (loggerDebugEnabled)
        {
	    logger.debug ("Immediate Requestor callback in client thread. " +
			  "Request type: " + request.type.toString() +
			  ", Request status: " + request.status.toString());
	}

	try
        {
	    request.callback.call (request);
	}
	catch (Exception ex)
        {
	    // disregard any client exceptions
	}

	if (loggerDebugEnabled)
        {
	    logger.debug ("Callback concluded");
	}
        return false;
    }

    /**
     * Adds a new request entity to the requestor pool.
     * @param request is the event to be added to the pool
     * @returns true if the request was successfully added
     */
    public boolean add (SelectorRequest request)
    {
        if (request == null)
        {
            return false;
        }
        if (!running)
        {
            return sendFailure (request, SelectorRequest.Status.SHUTDOWN);
        }
        if (request.nanoDeadline <= System.nanoTime())
        {
            return sendFailure (request, SelectorRequest.Status.EXPIRED);
        }
        if ((request.type == null) ||
            (request.type != SelectorRequest.Type.TIMER && request.channel == null))
        {
            return sendFailure (request, SelectorRequest.Status.FAILED);
        }
        if ((request.type == SelectorRequest.Type.READ || request.type == SelectorRequest.Type.WRITE) &&
            !request.channel.isConnected())
        {
            return sendFailure (request, SelectorRequest.Status.CLOSED);
        }

        if (loggerDebugEnabled)
        {
            logger.debug ("Adding new request. Request type: "
                          + request.type.toString());
        }

        request.setStatus (SelectorRequest.Status.PENDING);
        newRequests.offer (request);

        selector.wakeup ();
        return true;
    }

    //----------------------------------------------------------------------

    /**
     * Called in Selector thread
     */
    private void reactivate ()
    {
        SelectorRequest request = null;
        while ((request = reActivateBuffer.poll()) != null)
        {
            if (request.type != SelectorRequest.Type.TIMER && !request.channel.isConnected ())
            {
                removeClosedRequests (request.key);
                continue;
            }

            if (loggerDebugEnabled)
            {
                logger.debug ("Reactivating request. Request type: "
                        + request.type.toString());
            }

            try
            {
                int currentOps = request.key.interestOps ();
                int newOps = currentOps | request.op;
                request.key.interestOps (newOps);
            }
            catch (Exception ex)
            {
    		// channel interest ops weren't updated, so current
    		// ops are fine. We aren't leaving around extra ops
    		// internal data structures don't need cleanup as
    		// request wasn't inserted yet.
    		logger.error ("reactivate failed: " + ex.getMessage());
    		request.setStatus (SelectorRequest.Status.FAILED);

    		// call back request callable in worker thread
    		SendJob sendJob = new SendJob (request);
    		FutureTask<Object> task = new FutureTask<Object> (sendJob);
    		executor.execute (task);
    	    }
        }
    }

    /**
     * Called in Selector thread
     */
    private void removeClosedRequests (SelectionKey key)
    {
        if (key != null)
        {
            if (loggerDebugEnabled)
            {
                logger.debug ("Removing request matching key " + key.toString());
            }

            // cancel key
            key.cancel ();

            // traverse pools and cleanup requests mapped to this key
            Iterator<RequestorPool> p = pools.values().iterator();
            while (p.hasNext())
            {
                RequestorPool pool = p.next();
                ConcurrentLinkedQueue<SelectorRequest> requestBuffer;
                requestBuffer = pool.remove (key);
                removeClosedRequests (requestBuffer);
            }
        }
    }

    /**
     * Called in Selector thread
     */
    private void removeClosedRequests (ConcurrentLinkedQueue<SelectorRequest> source)
    {
        if (source == null)
            return;

        LinkedList<SelectorRequest> local = new LinkedList<SelectorRequest>(source);

        SelectorRequest request;
        while (local.size() > 0)
        {
            request = local.poll();
            request.setStatus (SelectorRequest.Status.CLOSED);

            // call back request callable in worker thread
            SendJob sendJob = new SendJob (request);
            FutureTask<Object> task = new FutureTask<Object> (sendJob);
            executor.execute (task);
        }
    }

    /**
     * Called in Selector thread
     */
    private void removeCanceled ()
    {
        SelectorRequest request = null;
        while ((request = canceledRequests.poll()) != null)
        {
            if (loggerDebugEnabled)
            {
                logger.debug ("Removing request type: " + request.type.toString());
            }

    	    if (request.type == SelectorRequest.Type.TIMER)
            {
    	        boolean result = timeOrderedRequests.remove(request);
    	        if (loggerDebugEnabled)
    	        {
    	            logger.debug ("Result of removing timer: " + result);
    	        }
    	    }
    	    else
            {
    	        removeFromActivePool (request);
    	    }
        }
    }

    private void removeFromActivePool (SelectorRequest request)
    {
        RequestorPool pool = pools.get(request.type);
        request.key = request.channel.keyFor (selector);
        if (request.key == null)
        {
            return;
            // no key means that the request never actually was
            // inserted into an active pool.
        }

        ConcurrentLinkedQueue<SelectorRequest> requests = null;
        requests = pool.get (request.key);
        if (requests == null)
        {
            return;
            // again, no request buffer means nothing to remove
        }

        requests.remove(request);
        timeOrderedRequests.remove(request);

        request.setStatus (SelectorRequest.Status.CLOSED);

        // call back request callable in worker thread
        SendJob sendJob = new SendJob (request);
        FutureTask<Object> task = new FutureTask<Object> (sendJob);
        executor.execute (task);
    }

    /**
     * Called in Selector thread
     */
    private void insertNew ()
    {
        SelectorRequest request = null;
        while ((request = newRequests.poll()) != null)
        {
            if (loggerDebugEnabled)
            {
                logger.debug ("Inserting request type: " + request.type.toString());
            }

    	    if (request.type != SelectorRequest.Type.CONNECT &&
    	        request.type != SelectorRequest.Type.TIMER &&
    	        !request.channel.isConnected ())
            {
    	        removeClosedRequests (request.key);
    	        return;
            }

    	    if (request.type == SelectorRequest.Type.TIMER)
    	    {
    	        insertIntoTimedBuffer (request);
    	    }
    	    else
            {
    	        insertIntoActivePool (request);
            }
        }
    }

    /**
     * Called in Selector thread
     * insertIntoActivePool does the following functions:
     * 1.0 If the channel is being seen for the first time by this
     *     selector a key will be created.
     * 1.1 If this channel is in a particular role for the first time,
     *     a new list buffer in the particualr pool will be created
     * 2.0 If the list buffer is empty, the key will be enabled in the
     *     selector for that action.
     * 2.1 If the list buffer isn't empty, the key will not be enabled
     *     in the selector for that action.  In that case it is either
     *     already enabled for that role or a worker is currently
     *     active in thet role.  We do not want two workers active in
     *     the same role on the same channel.
     */
    private void insertIntoActivePool (SelectorRequest request)
    {
        RequestorPool pool = pools.get(request.type);
        // if this is the first time selector is seeing this channel,
        //  acquire a key no synchronization required as only one
        //  thread should be inserting this channel at the moment
        request.key = request.channel.keyFor (selector);
        if (request.key == null)
        {
            try
            {
                request.key = request.channel.register (selector, request.op);
            }
            catch (ClosedChannelException e)
            {
                logger.error ("Insert failed: " + e.getMessage());
                request.setStatus (SelectorRequest.Status.CLOSED);

                // call back request callable in worker thread
                SendJob sendJob = new SendJob (request);
                FutureTask<Object> task = new FutureTask<Object> (sendJob);
                executor.execute (task);

                return;
            }
        }

        ConcurrentLinkedQueue<SelectorRequest>  requests = pool.get (request.key);
        if (requests == null)
        {
            requests = new ConcurrentLinkedQueue<SelectorRequest> ();
            pool.put (request.key, requests);
        }

        boolean opUpdateFailed = false;
        int newOps = 0;
        try
        {
            if (requests.isEmpty ())
            {
                // ops registration will be repeated if this is
                // the first time the channel is being seen
                int currentOps = request.key.interestOps ();
                newOps = currentOps | request.op;
                request.key.interestOps (newOps);
            }
            requests.offer (request);
            if (request.nanoDeadline != Long.MAX_VALUE)
            {
                insertIntoTimedBuffer (request);
            }
        }
        catch (CancelledKeyException ex)
        {
            logger.error ("Insert failed to update selector interest " +
                          ex.getMessage());
            opUpdateFailed = true;
        }
        catch (IllegalArgumentException ex)
        {
            logger.error ("Insert failed to update selector interest: " +
                          newOps + ": " + ex.getMessage());
            opUpdateFailed = true;
        }

        if (opUpdateFailed)
        {
            // channel interest ops weren't updated, so current ops
            // are fine. We aren't leaving around extra ops internal
            // data structures don't need cleanup as request wasn't
            // inserted yet.
            request.setStatus (SelectorRequest.Status.FAILED);

            // call back request callable in worker thread
            SendJob sendJob = new SendJob (request);
            FutureTask<Object> task = new FutureTask<Object> (sendJob);
            executor.execute (task);
        }
    }

    /**
     * Called in Selector thread
     */
    private void insertIntoTimedBuffer (SelectorRequest newRequest)
    {
        timeOrderedRequests.add (newRequest);
    }

    /**
     * Called in Selector thread
     * Returns shortest expiration time in millisecond resolution
     */
    private long cleanupExpiredRequests ()
    {
        long sleepTime = 0;
        SelectorRequest request = null;
        SelectorRequest atNow = new SelectorRequest(null,System.nanoTime());
        NavigableSet<SelectorRequest> expired = timeOrderedRequests.headSet(atNow, true);
        Iterator<SelectorRequest> i = expired.iterator();
        while (i.hasNext())
        {
            request = i.next();

            if (loggerDebugEnabled)
            {
                logger.debug ("Checking expiry. Request type: " + request.type +
                              ", request status: " +  request.status);
            }

            // if not pending, some action is being taken, don't interfere
            if (request.status != SelectorRequest.Status.PENDING)
            {
                timeOrderedRequests.remove (request);
                continue;
            }

            if (loggerDebugEnabled)
            {
                logger.debug ("Cleaning up expired request from timed" +
                              " requests queue:\n\trequest type: " +
                              request.type.toString() +
                              ", request status: " +
                              request.status.toString());
            }

            // a worker thread may already have caught the
            //  expired request. In that case don't refire the
            //  status update.
            if (request.status != SelectorRequest.Status.EXPIRED)
            {
                request.setStatus (SelectorRequest.Status.EXPIRED);

                // cleanup connection expiry requests
                // explicitly as this is probably the last
                // chance to clean it up
                if (request.type == SelectorRequest.Type.CONNECT &&
                    request.key != null)
                {
                    RequestorPool pool = pools.get(request.type);
                    ConcurrentLinkedQueue<SelectorRequest> buffer = pool.remove (request.key);

                    if (buffer != null)
                    {
                        cleanupBuffer (buffer);
                    }
                }
            }

            // Regardless of who set expired status (worker or
            //  us) its our job to issue the request for a
            //  request callback.  call back request callable
            //  in worker thread
            SendJob sendJob = new SendJob (request);
            FutureTask<Object> task = new FutureTask<Object> (sendJob);
            executor.execute (task);
            timeOrderedRequests.remove (request);
            continue;
        }

        if (!timeOrderedRequests.isEmpty())
        {
            request = timeOrderedRequests.first();
            // the first non-pending, still to expire action gives
            // us the next sleep time.
            sleepTime = (request.nanoDeadline - atNow.nanoDeadline) / 1000000;
        }
        if (sleepTime <= 0)
        {
            sleepTime = 1;
            // cannot return sleepTime 0 as thats an infinity
        }

        return sleepTime;
    }


    private SelectorRequest getNextRequest (boolean anyStatus, ConcurrentLinkedQueue<SelectorRequest> buffer)
    {
        SelectorRequest request = null;
        while ((request = buffer.poll()) != null)
        {
            if (request.status == SelectorRequest.Status.EXPIRED)
            {
                continue;
            }
            if ((anyStatus ||
                request.status == SelectorRequest.Status.PENDING) &&
                request.nanoDeadline <= System.nanoTime())
            {
                request.setStatus (SelectorRequest.Status.EXPIRED);
                continue;
            }
            break;
        }
        return request;
    }

    /**
     * Called in Worker thread
     */
    private void callbackRequestor (SelectionKey key, RequestorPool pool)
    {
        ConcurrentLinkedQueue<SelectorRequest> buffer = pool.get(key);
        SelectorRequest request = getNextRequest (false, buffer);
        if (request == null)
        {
            return;
        }
        request.setStatus (SelectorRequest.Status.READY);
        boolean reActivate = false;
        if (request.callback != null)
        {
            if (loggerDebugEnabled)
            {
                logger.debug ("Requestor callback in worker thread. " +
                              "Request type: " + request.type.toString());
            }

            try
            {
                reActivate = request.callback.call (request);
            }
            catch (Exception ex)
            {
                // discard any uncaught requestor exceptions
            }

            if (loggerDebugEnabled)
            {
                logger.debug ("Callback concluded. Reactivation request: "
                              + (reActivate ? "TRUE" : "FALSE"));
            }

            // if connected is closed, try to reactivate the
            //  request. this will let the selector loop to
            //  cleanup the channel from its structures.  Unwanted
            //  behavior of calling the requestor callback again.
            if (!request.channel.isConnected())
            {
                reActivate = true;
            }
        }

        if (reActivate)
        {
            request.setStatus (SelectorRequest.Status.ASSIGNED);
        }
        else
        {
            request.setStatus (SelectorRequest.Status.FINISHED);
            // required to indicate request callback has finished
            // Then if there is any followup requests, "reactivate" with the next pending request
            request = getNextRequest (true, buffer);
        }

        // if any requests are pending re-active key
        if (request != null)
        {
            reActivateBuffer.offer (request);
            if (loggerDebugEnabled)
            {
                    logger.debug ("Adding reactivate request. Request type: "
                                    + request.type.toString());
            }

            selector.wakeup ();
        }
    }


    /**
     * Called in Worker thread
     */
    private void handleAction (SelectionKey key,
                               SelectorRequest.Type type,
                               SelectorRequest request)
    {
        if (loggerDebugEnabled)
        {
            logger.debug ("Enter SelectorManager.handleAction");
        }

        // if request object is available just call its callable object
        if (request == null)
        {
            callbackRequestor (key, pools.get(type));
            return;
        }
        if (request.callback == null)
        {
            return;
        }
        if (loggerDebugEnabled)
        {
            logger.debug ("Selector Worker thread calling request " +
                          "callback directly:\n" +
                          "\tRequest type: " +
                          request.type.toString() +
                          ", Request status: " +
                          request.status.toString());
        }

        try
        {
            request.callback.call (request);
        }
        catch (Exception ex)
        {
            // ignore client exceptions
        }

        if (loggerDebugEnabled)
        {
            logger.debug ("Callback concluded");
        }
    }

    private class RequestorPool
    {
        public ConcurrentHashMap<SelectionKey, ConcurrentLinkedQueue<SelectorRequest>> pool =
            new ConcurrentHashMap<SelectionKey, ConcurrentLinkedQueue<SelectorRequest>> ();

        public ConcurrentLinkedQueue<SelectorRequest> get (SelectionKey key)
        {
            return pool.get (key);
        }

        public ConcurrentLinkedQueue<SelectorRequest> put (SelectionKey key, ConcurrentLinkedQueue<SelectorRequest> requestBuffer)
        {
            return pool.put (key, requestBuffer);
        }

        public ConcurrentLinkedQueue<SelectorRequest> remove (SelectionKey key)
        {
            return pool.remove (key);
        }

        public Iterator<ConcurrentLinkedQueue<SelectorRequest>> values()
        {
            return pool.values().iterator();
        }

        public int size ()
        {
            return pool.size();
        }
    }

    private static class SelectorManagerThreadFactory implements ThreadFactory
    {
        private static final AtomicInteger poolNumber = new AtomicInteger(1);
        private final ThreadGroup group;
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;


        private SelectorManagerThreadFactory()
        {
            SecurityManager s = System.getSecurityManager();
            group = (s != null ? s.getThreadGroup() : Thread.currentThread().getThreadGroup());
            namePrefix = "SelectorManager worker-" + poolNumber.getAndIncrement() + "-thread-";
        }


        public Thread newThread(Runnable runnable)
        {
            Thread t = new Thread(group, runnable, namePrefix + threadNumber.getAndIncrement(), 0);
            if (t.isDaemon())
            {
                t.setDaemon(false);
            }

            if (t.getPriority() != Thread.NORM_PRIORITY)
            {
                t.setPriority(Thread.NORM_PRIORITY);
            }

            return t;
        }
    }

    // a SendJob is a helper class used to send requests in background
    private class SendJob implements Callable<Object>
    {
        private SelectionKey key = null;
        private SelectorRequest.Type type = null;
        private SelectorRequest request = null;

        SendJob (SelectionKey key, SelectorRequest.Type type)
        {
            this.key = key;
            this.type = type;
        }

        SendJob (SelectorRequest request)
        {
            this.request = request;
        }

        public Object call()
        throws Exception
        {
            if (running)
            {
                handleAction (key, type, request);
            }
            return (null);
        }
    }
}