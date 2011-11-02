/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 2011 Gerald Brose.
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
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
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
 * @version $Id$
 */
public class SelectorManager extends Thread
{

    final private RequestorPool connectRequestsPool;
    final private RequestorPool writeRequestsPool;
    final private RequestorPool readRequestsPool;

    final private RequestsBuffer timeOrderedRequests;
    final private RequestsBuffer canceledRequests;
    final private RequestsBuffer newRequests;
    final private RequestsBuffer reActivateBuffer;

    final private Selector selector;
    private boolean running;
    private Logger logger;

    final private ExecutorService executor;
    private int threadPoolMin = 2;
    private int threadPoolMax = 10;
    private int threadPoolKeepAliveTime = 60; // seconds
    private int executorPendingQueueSize = 5;

    private boolean loggerDebugEnabled = false;
    private final ReentrantLock bufferLock = new ReentrantLock();
    private final ReentrantLock runLock = new ReentrantLock();

    /**
     * Constructs a new Selector Manager. Typically called by the ORB
     */
    public SelectorManager ()
    {

        running = true;

        try
        {
            connectRequestsPool = new RequestorPool ();
            writeRequestsPool = new RequestorPool ();
            readRequestsPool = new RequestorPool ();

            timeOrderedRequests = new RequestsBuffer ();
            canceledRequests = new RequestsBuffer ();
            newRequests = new RequestsBuffer ();
            reActivateBuffer = new RequestsBuffer ();

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

                synchronized (bufferLock)
                {
                    while (canceledRequests.size() > 0)
                    {
                        removeRequest (canceledRequests.pop());
                    }

                    while (newRequests.size() > 0)
                    {
                        insertRequest (newRequests.pop());
                    }

                    while (reActivateBuffer.size() > 0)
                    {
                        reactivateRequest (reActivateBuffer.pop());
                    }
                }

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

                synchronized (runLock)
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

                for (Iterator<SelectionKey> iter =
                         selector.selectedKeys().iterator(); iter.hasNext();)
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


    /**
     * Called in Selector thread
     */
    private void dispatch (SelectionKey key)
    {
        if (loggerDebugEnabled)
        {
            logger.debug ("dispatched called for: "
                          + key.toString());
        }


        try
        {
            // loop four times to account for each of the
            //  ecah of the possible IO operations
            //  connect, accept, read, write
            SelectorRequest.Type jobType = SelectorRequest.Type.CONNECT;
            while (jobType != SelectorRequest.Type.TIMER)
            {
                int op = 0;
                // delegate work to worker thread
                SelectorRequest.Type nextType = SelectorRequest.Type.TIMER;
                switch (jobType)
                {
                case CONNECT:
                    if (key.isConnectable())
                    {
                        op = SelectionKey.OP_CONNECT;
                    }
                    nextType = SelectorRequest.Type.ACCEPT;
                    break;
                case ACCEPT:
                    if (key.isAcceptable())
                    {
                        op = SelectionKey.OP_ACCEPT;
                    }
                    nextType = SelectorRequest.Type.READ;
                    break;
                case READ:
                    if (key.isReadable())
                    {
                        op = SelectionKey.OP_READ;
                    }
                    nextType = SelectorRequest.Type.WRITE;
                    break;
                case WRITE:
                    if (key.isWritable())
                    {
                        op = SelectionKey.OP_WRITE;
                    }
                    nextType = SelectorRequest.Type.TIMER;
                    break;
                default:
                    return;
                }

                if (op != 0)
                {
                    if (loggerDebugEnabled)
                    {
                        logger.debug ("Key " + key.toString() +
                                      " ready for action: " +
                                      jobType.toString());
                    }

                    // disable op bit for SelectionKey
                    int currentOps = key.interestOps ();
                    int newOps = currentOps ^ op; // exclusive or
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
                jobType = nextType;
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

        cancelKeyFromPool (connectRequestsPool, key);
        cancelKeyFromPool (writeRequestsPool, key);
        cancelKeyFromPool (readRequestsPool, key);
    }

    /**
     * Called in Selector thread
     */
    private void cancelKeyFromPool (RequestorPool pool, SelectionKey key)
    {

        RequestsBuffer requestsBuffer = null;
        synchronized (pool)
        {
            requestsBuffer = pool.remove (key);
        }

        if (requestsBuffer != null)
        {
            cleanupBuffer (requestsBuffer);
        }
    }

    /**
     * Called in Selector thread
     */
    private void cleanupAll ()
    {

        cleanupPool (connectRequestsPool);
        cleanupPool (writeRequestsPool);
        cleanupPool (readRequestsPool);

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
    private void cleanupPool (RequestorPool pool)
    {

        synchronized (pool)
        {
            for (Enumeration<RequestsBuffer> e = pool.elements (); e.hasMoreElements();)
            {
                cleanupBuffer (e.nextElement());
            }
        }
    }

    /**
     * Called in Selector thread
     */
    private void cleanupBuffer (RequestsBuffer requestsBuffer)
    {
        synchronized (bufferLock)
        {
            while (requestsBuffer.size() > 0)
            {
                SelectorRequest request = requestsBuffer.pop();

                if (loggerDebugEnabled)
                {
                    logger.debug ("Cleaning up request. Request type: " +
                                  request.type.toString() +
                                  ", Request status: " +
                                  request.status.toString());
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
        switch (type)
        {
        case CONNECT:
            return connectRequestsPool.size();
        case ACCEPT:
            return 0; // acceptor pool is not yet implemented
        case WRITE:
            return writeRequestsPool.size();
        case READ:
            return readRequestsPool.size();
        case TIMER:
            return timeOrderedRequests.size();
        }
        return 0;
    }


    /**
     * Remove an existing request before it has expired
     * @param request is the event to be removed from the pool
     */

    public void remove (SelectorRequest request)
    {
        if (request == null || request.type == null)
            return;
        synchronized (bufferLock)
        {
            if (newRequests.remove(request))
                return;
            // no need to wake up the selector, since the request was
            // canceled before it was pulled from the new requests
            // list. That means that the previous call to add() had
            // (or will) awaken the selector and just find the new
            // requests list potentially empty.

            canceledRequests.push (request);
        }
        if (loggerDebugEnabled)
        {
            logger.debug ("Remove request. Request type: "
                          + request.type.toString());
        }

        selector.wakeup ();
    }


    /**
     * Adds a new request entity to the requestor pool.
     * @param request is the event to be added to the pool
     * @returns true if the request was successfully added
     */
    public boolean add (SelectorRequest request)
    {

        boolean immediateCallback = false;
        if (request == null)
        {
            return false;
        }
        if (request.type == null)
        {
            request.setStatus (SelectorRequest.Status.FAILED);
            immediateCallback = true;
        }
        if (request.nanoDeadline <= System.nanoTime())
        {
            request.setStatus (SelectorRequest.Status.EXPIRED);
            immediateCallback = true;
        }
        if (!running)
        {
            request.setStatus (SelectorRequest.Status.SHUTDOWN);
            immediateCallback = true;
        }

        switch (request.type)
        {
            case CONNECT:
                if (request.channel == null)
                {
                    request.setStatus (SelectorRequest.Status.FAILED);
                    immediateCallback = true;
                }
                break;
            case WRITE:
            case READ:
                if (request.channel == null)
                {
                    request.setStatus (SelectorRequest.Status.FAILED);
                    immediateCallback = true;
                }
                else if (!request.channel.isConnected())
                {
                    request.setStatus (SelectorRequest.Status.CLOSED);
                    immediateCallback = true;
                }
                break;
        }

        if (immediateCallback)
        {
            if (request.callback != null)
            {
                if (loggerDebugEnabled)
                {
                    logger.debug ("Immediate Requestor callback in " +
                                  "client thread. Request type: " +
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
                    // disregrad any client exceptions
                }

                if (loggerDebugEnabled)
                {
                    logger.debug ("Callback concluded");
                }
            }
            return false;
        }

        if (loggerDebugEnabled)
        {
            logger.debug ("Adding new request. Request type: "
                          + request.type.toString());
        }

        request.setStatus (SelectorRequest.Status.PENDING);
        synchronized (bufferLock)
        {
            newRequests.push (request);
        }

        selector.wakeup ();
        return true;
    }

    //----------------------------------------------------------------------

    /**
     * Called in Selector thread
     */
    private void reactivateRequest (SelectorRequest request)
    {
        if (request.type != SelectorRequest.Type.TIMER
                && !request.channel.isConnected ())
        {
            removeClosedRequests (request.key);
        }
        else
        {

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

        if (key == null)
        {
            return;
        }

        if (loggerDebugEnabled)
        {
            logger.debug ("Removing request matching key " + key.toString());
        }

        // cancel key
        key.cancel ();

        // traverse pools and cleanup requests mapped to this key
        removeClosedRequests (key, connectRequestsPool);
        removeClosedRequests (key, writeRequestsPool);
        removeClosedRequests (key, readRequestsPool);
    }

    /**
     * Called in Selector thread
     */
    private void removeClosedRequests (SelectionKey key, RequestorPool pool)
    {

        RequestsBuffer requestBuffer;
        synchronized (pool)
        {
            requestBuffer = pool.remove (key);
        }
        removeClosedRequests (requestBuffer);
    }

    /**
     * Called in Selector thread
     */
    private void removeClosedRequests (RequestsBuffer source)
    {
        if (source == null)
            return;

        RequestsBuffer local = new RequestsBuffer();
        synchronized (bufferLock)
        {
            while (source.size() > 0)
            {
                local.push(source.pop());
                // move requests to a local container out from the
                // source container
            }
        }

        SelectorRequest request;
        while (local.size() > 0)
        {
            request = local.pop();
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
    private void removeRequest (SelectorRequest request)
    {

        if (loggerDebugEnabled)
        {
            logger.debug ("Removing request type: " + request.type.toString());
        }

        switch (request.type)
        {
        case CONNECT:
            removeFromActivePool (connectRequestsPool, request);
            break;
        case WRITE:
            removeFromActivePool (writeRequestsPool, request);
            break;
        case READ:
            removeFromActivePool (readRequestsPool, request);
            break;
        case TIMER:
            synchronized (bufferLock)
            {
                boolean result = timeOrderedRequests.remove(request);
                if (loggerDebugEnabled)
                {
                    logger.debug ("Result of removing timer: " + result);
                }
            }
            break;
        }
    }

    private void removeFromActivePool (RequestorPool pool,
                                       SelectorRequest request)
    {
        request.key = request.channel.keyFor (selector);
        if (request.key == null)
        {
            return;
            // no key means that the request never actually was
            // inserted into an active pool.
        }

        RequestsBuffer requests = null;
        synchronized (pool)
        {
           requests = pool.get (request.key);
            if (requests == null)
            {
                return;
                // again, no request buffer means nothing to remove
            }
        }

        synchronized (bufferLock)
        {
            requests.remove(request);
            timeOrderedRequests.remove(request);
        }

        request.setStatus (SelectorRequest.Status.CLOSED);

        // call back request callable in worker thread
        SendJob sendJob = new SendJob (request);
        FutureTask<Object> task = new FutureTask<Object> (sendJob);
        executor.execute (task);
    }


    /**
     * Called in Selector thread
     */
    private void insertRequest (SelectorRequest request)
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

        switch (request.type)
        {
        case CONNECT:
            insertIntoActivePool (connectRequestsPool, request);
            break;
        case WRITE:
            insertIntoActivePool (writeRequestsPool, request);
            break;
        case READ:
            insertIntoActivePool (readRequestsPool, request);
            break;
        case TIMER:
            insertIntoTimedBuffer (request);
            break;
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
    private void insertIntoActivePool (RequestorPool pool,
                                       SelectorRequest request)
    {
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

        RequestsBuffer requests = null;
        synchronized (pool)
        {

            // Acquire pool lock and check if a list exists for this
            // key. If not create one

            requests = pool.get (request.key);
            if (requests == null)
            {
                requests = new RequestsBuffer ();
                pool.put (request.key, requests);
            }
        }

        boolean opUpdateFailed = false;
        int newOps = 0;
        try
        {
            synchronized (bufferLock)
            {
                if (requests.isEmpty ())
                {
                    // ops registration will be repeated if this is
                    // the first time the channel is being seen
                    int currentOps = request.key.interestOps ();
                    newOps = currentOps | request.op;
                    request.key.interestOps (newOps);
                }
                requests.push (request);
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
        synchronized (bufferLock)
        {
            boolean inserted = false;
            int indx = timeOrderedRequests.size ();
            for (ListIterator<SelectorRequest> iter =
                     timeOrderedRequests.listIterator(indx);
                 iter.hasPrevious();)
            {
                SelectorRequest request = iter.previous();
                // iterate till a request with earlier deadline is seen
                if (request.nanoDeadline < newRequest.nanoDeadline)
                {
                    break;
                }
                indx--;
            }
            timeOrderedRequests.add (indx, newRequest);
        }
    }

    /**
     * Called in Selector thread
     * Returns shortest expiration time in millisecond resolution
     */
    private long cleanupExpiredRequests ()
    {

        if (loggerDebugEnabled)
        {
            logger.debug ("Enter SelectorManager.cleanupExpiredRequests()");
        }

        long sleepTime = 0;//Long.MAX_VALUE;
        synchronized (bufferLock)
        {
            while (timeOrderedRequests.size() > 0)
            {
                SelectorRequest request = timeOrderedRequests.peek();

                if (loggerDebugEnabled)
                {
                    logger.debug ("Checking expiry. Request type: " +
                                  request.type.toString() +
                                  ", request status: " +
                                  request.status.toString());
                }

                // if not pending, some action is being taken, don't interfere
                if (request.status != SelectorRequest.Status.PENDING)
                {
                    timeOrderedRequests.pop ();
                    continue;
                }

                long currentNanoTime = System.nanoTime();
                // if still pending and time expired, no action is
                // being taken, just expire the sucker leave the op
                // registration alone. We don't know if another
                // request is pending. Upon firing, selector will
                // check if any unpired requests are pending.
                if (request.nanoDeadline <= currentNanoTime)
                {

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
                            cancelKeyFromPool (connectRequestsPool, request.key);
                        }
                    }

                    // Regardless of who set expired status (worker or
                    //  us) its our job to issue the request for a
                    //  request callback.  call back request callable
                    //  in worker thread
                    SendJob sendJob = new SendJob (request);
                    FutureTask<Object> task = new FutureTask<Object> (sendJob);
                    executor.execute (task);
                    timeOrderedRequests.pop ();
                    continue;
                }

                // the first non-pending, still to expire action gives
                // us the next sleep time.
                sleepTime = (request.nanoDeadline - currentNanoTime) / 1000000;
                if (sleepTime <= 0)
                    sleepTime = 1;
                // cannot return sleepTime 0 as thats an infinity
                break;
            }
        }

        return sleepTime;
    }

    /**
     * Called in Worker thread
     */
    private void callbackRequestor (SelectionKey key, RequestorPool pool)
    {

        RequestsBuffer requestsBuffer = pool.get(key);
        SelectorRequest request = null;

        synchronized (bufferLock)
        {
            for (Iterator<SelectorRequest> iter = requestsBuffer.iterator(); iter.hasNext();)
            {
                request = iter.next();

                // if current request is already marked expired,
                // remove from list & go on
                if (request.status == SelectorRequest.Status.EXPIRED)
                {
                    request = null;
                    iter.remove ();
                    continue;
                }

                // if current request isn't expired but its status
                //  hasn't been updated update status.  expiration
                //  callback will be done by selector loop
                if (request.status == SelectorRequest.Status.PENDING && request.nanoDeadline <= System.nanoTime())
                {
                    request.setStatus (SelectorRequest.Status.EXPIRED);
                    iter.remove ();
                    continue;
                }
                break;
            }
        }

        if (request != null)
        {
            request.setStatus (SelectorRequest.Status.READY);

            boolean reActivate = false;
            if (request.callback != null)
            {

                if (loggerDebugEnabled)
                {
                    logger.debug ("Requestor callback in worker thread. Request type: " + request.type.toString());
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
                    logger.debug ("Callback concluded. Reactivation request: " + (reActivate ? "TRUE" : "FALSE"));
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

            if (!reActivate)
            {
                // request complete; remove it from buffer and
                // activate key only if buffer has more elements
                synchronized (bufferLock)
                {
                    request.setStatus (SelectorRequest.Status.FINISHED);
                    // required to indicate request callback has finished
                    requestsBuffer.remove (0);
                    // cleanup expired requests
                    request = null;
                    for (Iterator<SelectorRequest> iter = requestsBuffer.iterator(); iter.hasNext();)
                    {
                        request = iter.next();

                        // if current request is already marked
                        // expired, remove from list & go on
                        if (request.status == SelectorRequest.Status.EXPIRED)
                        {
                            iter.remove();
                            request = null;
                            continue;
                        }

                        // if current request isn't expired but its
                        //  status hasn't been updated update status.
                        //  expiration callback will be done by
                        //  selector loop
                        if (request.nanoDeadline <= System.nanoTime())
                        {
                            request.setStatus (SelectorRequest.Status.EXPIRED);
                            iter.remove();
                            request = null;
                            continue;
                        }
                        break;
                    }
                }
            }

            // if any requests are pending re-active key
            if (request != null)
            {
                synchronized (bufferLock)
                {
                    reActivateBuffer.push (request);
        if (loggerDebugEnabled)
        {
            logger.debug ("Adding reactivate request. Request type: "
                          + request.type.toString());
        }

                    selector.wakeup ();
                }
            }
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
        if (request != null)
        {
            if (request.callback != null)
            {

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
        }
        else
        {
            // else we have more work
            switch (type)
            {
                case CONNECT:
                    callbackRequestor (key, connectRequestsPool);
                    break;
                case WRITE:
                    callbackRequestor (key, writeRequestsPool);
                    break;
                case READ:
                    callbackRequestor (key, readRequestsPool);
                    break;
            }
        }
    }

    private class RequestsBuffer
    {
        public LinkedList<SelectorRequest> requests = new LinkedList<SelectorRequest> ();

        public Iterator<SelectorRequest> iterator ()
        {
            return requests.iterator ();
        }

        public ListIterator<SelectorRequest> listIterator (int index)
        {
            return requests.listIterator (index);
        }

        public SelectorRequest peek ()
        {
            try
            {
                return requests.peek();
            }
            catch (NoSuchElementException ex)
            {
                return null;
            }
        }

        public SelectorRequest pop ()
        {
            try
            {
                return requests.pop();
            }
            catch (NoSuchElementException ex)
            {
                return null;
            }
        }

        public void push (SelectorRequest request)
        {
            requests.push (request);
        }

        public SelectorRequest get (int index)
        {
            return requests.get(index);
        }

        public void clear ()
        {
            requests.clear ();
        }

        public void add (int index, SelectorRequest request)
        {
            requests.add (index, request);
        }

        public SelectorRequest remove (int index)
        {
            return requests.remove (index);
        }

        public boolean remove (SelectorRequest request)
        {
            return requests.remove (request);
        }

        public int size ()
        {
            return requests.size ();
        }

        public boolean isEmpty ()
        {
            return requests.isEmpty ();
        }
    }

    private class RequestorPool
    {
        public Hashtable<SelectionKey, RequestsBuffer> pool =
            new Hashtable<SelectionKey, RequestsBuffer> ();

        public RequestsBuffer get (SelectionKey key)
        {
            return pool.get (key);
        }

        public RequestsBuffer put (SelectionKey key, RequestsBuffer requestBuffer)
        {
            return pool.put (key, requestBuffer);
        }

        public RequestsBuffer remove (SelectionKey key)
        {
            return pool.remove (key);
        }

        public Enumeration<RequestsBuffer> elements()
        {
            return pool.elements ();
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