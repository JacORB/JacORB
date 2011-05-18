/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2008 Gerald Brose.
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

import java.util.Date;
import java.io.IOException;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Map;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Enumeration;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.CancelledKeyException;

import org.jacorb.config.*;
import org.slf4j.Logger;

public class SelectorManager extends Thread {

  // private class requestTracker {

  //   public SelectorRequest request;
  //   public int ops;
  //   public SelectionKey key = null;

  private class RequestsBuffer {
    public LinkedList<SelectorRequest> requests = new LinkedList<SelectorRequest> ();

    public Iterator<SelectorRequest> iterator () {
      return requests.iterator ();
    }

    public ListIterator<SelectorRequest> listIterator (int index) {
      return requests.listIterator (index);
    }

    public SelectorRequest get (int index) {
      return requests.get(index);
    }

    public void clear () {
      requests.clear ();
    }

    public void add (int index, SelectorRequest request) {
      requests.add (index, request);
    }

    public SelectorRequest remove (int index) {
      return requests.remove (index);
    }

    public int size () {
      return requests.size ();
    }

    public boolean isEmpty () {
      return requests.isEmpty ();
    }
  }

  private class RequestorPool {
    public Hashtable<SelectionKey, RequestsBuffer> pool = new Hashtable<SelectionKey, RequestsBuffer> ();

    public RequestsBuffer get (SelectionKey key) {
      return pool.get (key);
    }

    public RequestsBuffer put (SelectionKey key, RequestsBuffer requestBuffer) {
      return pool.put (key, requestBuffer);
    }

    public RequestsBuffer remove (SelectionKey key) {
      return pool.remove (key);
    }

    public Enumeration<RequestsBuffer> elements() {
      return pool.elements ();
    }
  }

  private RequestorPool connectRequestsPool;
  private RequestorPool writeRequestsPool;
  private RequestorPool readRequestsPool;

  private RequestsBuffer timeOrderedRequests;
  private RequestsBuffer newRequests;
  private RequestsBuffer reActivateBuffer;

  private Selector selector;
  private boolean running;
  private Logger logger;

  private ExecutorService executor = null;
  private int threadPoolMin = 1;
  private int threadPoolMax = 10;
  private int threadPoolKeepAliveTime = 60; // seconds
  private int executorPendingQueueSize = 60;

  public SelectorManager () {

    running = true;

    try {
      connectRequestsPool = new RequestorPool ();
      writeRequestsPool = new RequestorPool ();
      readRequestsPool = new RequestorPool ();

      timeOrderedRequests = new RequestsBuffer ();
      newRequests = new RequestsBuffer ();
      reActivateBuffer = new RequestsBuffer ();

      selector = SelectorProvider.provider().openSelector ();
      executor = new ThreadPoolExecutor (threadPoolMin, threadPoolMax, threadPoolKeepAliveTime
                                         , TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(executorPendingQueueSize),
                                         new SelectorManagerThreadFactory(), new ThreadPoolExecutor.CallerRunsPolicy());
    }
    catch (IOException e) {
      throw new RuntimeException (e);
    }
  }

  public void configure(Configuration configuration)
    throws ConfigurationException {

    org.jacorb.config.Configuration jacorbConfiguration = (org.jacorb.config.Configuration) configuration;
    logger = jacorbConfiguration.getLogger("jacorb.util");
  }

  public void run () {

    try {
      while (running) {

        synchronized (newRequests) {
          for (Iterator<SelectorRequest> newRequestsIter = newRequests.iterator (); newRequestsIter.hasNext();) {
            SelectorRequest action = newRequestsIter.next ();
            // if (action.deadline.getTime() <= System.currentTimeMillis()) {
            //   action.setStatus (SelectorRequest.Status.EXPIRED);
            //   continue;
            // }
            action.setStatus (SelectorRequest.Status.PENDING);
            insertIntoActivePool (action);
          }
          newRequests.clear ();
        }

        synchronized (reActivateBuffer) {
          for (Iterator<SelectorRequest> newRequestsIter = newRequests.iterator (); newRequestsIter.hasNext();) {
            SelectorRequest action = newRequestsIter.next ();
            // if (action.deadline.getTime() <= System.currentTimeMillis()) {
            //   action.setStatus (SelectorRequest.Status.EXPIRED);
            //   continue;
            // }
            // don't change status else a partially write request may get expired
            reActivateRequest (action);
          }
          reActivateBuffer.clear ();
        }

        // cleanup expired requests & compute next sleep unit
        long sleepTime = cleanupExpiredRequests ();

        try {
          selector.select (sleepTime);
        }
        catch (IllegalArgumentException ex) {
          logger.error ("Select timeout ("+ sleepTime + ") argument flawed: " + ex.getMessage());
          // shouldn't be any problem to continue;
        }

        if (!running) {
          break;
        }

        for (Iterator iter = selector.selectedKeys().iterator(); iter.hasNext();) {
          SelectionKey key = (SelectionKey) iter.next();
          iter.remove();

          if (!key.isValid()) {
            continue;
          }

          // loop four times to account for each of the ecah of the possible IO operations
          //  connect, accept, read, write
          for (int count = 0; count < 4; count++) {
            int op = 0;
            SelectorRequest.Type jobType = SelectorRequest.Type.CONNECT;
            // delegate work to worker thread
            if (key.isConnectable()) {
              op = SelectionKey.OP_CONNECT;
              jobType = SelectorRequest.Type.CONNECT;
            }
            else if (key.isAcceptable()) {
              op = SelectionKey.OP_ACCEPT;
              jobType = SelectorRequest.Type.ACCEPT;
            } else if (key.isReadable()) {
              op = SelectionKey.OP_READ;
              jobType = SelectorRequest.Type.READ;
            } else if (key.isWritable()) {
              op = SelectionKey.OP_WRITE;
              jobType = SelectorRequest.Type.WRITE;
            }
            else {
              break;
            }

            if (op != 0) {
              // disable op bit for SelectionKey
              int currentOps = key.interestOps ();
              int newOps = currentOps ^ op; // exclusive or
              try {
                key.interestOps (newOps);
              }
              catch (CancelledKeyException ex) {
                // let the worker deal with this
              }

              // delegate work to worker thread
              SendJob sendJob = new SendJob (key, jobType);
              FutureTask task = new FutureTask (sendJob);
              executor.execute (task);
            }
          }
        }
      }
    }
    catch (Exception ex) {
      logger.error ("Exception in Selector loop. Bailing out: " + ex.getMessage());
      running = false;
    }

    try {
      // clean up all pending requests
      cleanup ();
      executor.shutdown ();
    }
    catch (Exception ex) {
      logger.error ("Exception in Selector manager cleanup. Bailing out: " + ex.getMessage());
      running = false;
    }
  }

  /**
   * Called in Selector thread
   */
  private void cleanup () {

    cleanup (connectRequestsPool);
    cleanup (writeRequestsPool);
    cleanup (readRequestsPool);

    cleanup (reActivateBuffer);
    cleanup (newRequests);

    // the time ordered requests have already been cleaned up during above cleanup
    timeOrderedRequests.clear ();

  }

  /**
   * Called in Selector thread
   */
  private void cleanup (RequestorPool pool) {

    synchronized (pool) {
      for (Enumeration<RequestsBuffer> e = pool.elements (); e.hasMoreElements();) {
        cleanup (e.nextElement());
      }
    }
  }

  /**
   * Called in Selector thread
   */
  private void cleanup (RequestsBuffer requestsBuffer) {
    synchronized (requestsBuffer) {
      for (Iterator<SelectorRequest> iter = requestsBuffer.iterator(); iter.hasNext();) {
        SelectorRequest request = iter.next();
        request.setStatus (SelectorRequest.Status.SHUTDOWN);

        // delegate work to worker thread
        SendJob sendJob = new SendJob (request);
        FutureTask task = new FutureTask (sendJob);
        executor.execute (task);
      }
      requestsBuffer.clear ();
    }
  }

  public synchronized void halt () {

    if (running) {
      running = false;
      selector.wakeup ();

      // wait until all threadpool tasks have finished
      while (!executor.isTerminated()) {
        try {
          executor.awaitTermination (Long.MAX_VALUE, TimeUnit.SECONDS);
        }
        catch (InterruptedException e) {
          // ignored
        }
      }
    }
  }

  public boolean add (SelectorRequest request) {

    boolean immediateCallback = false;
    if (request == null) {
      return false;
    }
    if (request.type == null) {
      request.setStatus (SelectorRequest.Status.FAILED);
      immediateCallback = true;
    }
    if (request.deadline.getTime() <= System.currentTimeMillis()) {
      request.setStatus (SelectorRequest.Status.EXPIRED);
      immediateCallback = true;
    }
    if (!running) {
      request.setStatus (SelectorRequest.Status.SHUTDOWN);
      immediateCallback = true;
    }
    if (immediateCallback) {
      if (request.callback != null) {
        request.callback.call (request);
      }
      return false;
    }

    switch (request.type) {
    case CONNECT:
    case WRITE:
    case READ:
      if (request.channel == null) {
        return false;
      }
      break;
    }

    synchronized (newRequests) {
      newRequests.add (newRequests.size(), request);
    }

    selector.wakeup ();
    return true;
  }

  /**
   * Called in Selector thread
   */
  private void reActivateRequest (SelectorRequest request) {

    if (!request.channel.isConnected ()) {
      removeClosedRequests (request.key);
    }
    else {
      try {
        int currentOps = request.key.interestOps ();
        int newOps = currentOps | request.op;
        request.key.interestOps (newOps);
      }
      catch (Exception ex) {
        // channel interest ops weren't updated, so current ops are fine. We aren't leaving around extra ops
        // internal data structures don't need cleanup as request wasn't inserted yet.
        logger.error ("Failed to update selector interest ops for this channel.");
        request.setStatus (SelectorRequest.Status.FAILED);

        // call back request callable in worker thread
        SendJob sendJob = new SendJob (request);
        FutureTask task = new FutureTask (sendJob);
        executor.execute (task);
      }
    }
  }

  /**
   * Called in Selector thread
   */
  private void removeClosedRequests (SelectionKey key) {

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
  private void removeClosedRequests (SelectionKey key, RequestorPool pool) {

    RequestsBuffer requestBuffer;
    synchronized (connectRequestsPool) {
      requestBuffer = connectRequestsPool.remove (key);
    }
    removeClosedRequests (requestBuffer);
  }

  /**
   * Called in Selector thread
   */
  private void removeClosedRequests (RequestsBuffer requestBuffer) {

    if (requestBuffer != null) {
      RequestsBuffer myRequestBuffer = new RequestsBuffer();
      synchronized (requestBuffer) {

        // move requests to a local container out from the common container
        for (Iterator<SelectorRequest> iter = requestBuffer.iterator(); iter.hasNext();) {
          myRequestBuffer.add (0, iter.next ());
          iter.remove();
        }
      }

      SelectorRequest request;
      for (Iterator<SelectorRequest> iter = myRequestBuffer.iterator(); iter.hasNext();) {
        request = iter.next ();
        request.setStatus (SelectorRequest.Status.CLOSED);

        // call back request callable in worker thread
        SendJob sendJob = new SendJob (request);
        FutureTask task = new FutureTask (sendJob);
        executor.execute (task);
      }
      myRequestBuffer.clear ();
    }
  }

  /**
   * Called in Selector thread
   */
  private void insertIntoActivePool (SelectorRequest request) {

    if (!request.channel.isConnected ()) {
      removeClosedRequests (request.key);
    }
    else {
      switch (request.type) {
      case CONNECT:
        insertIntoActivePool (connectRequestsPool, request);
        break;
      case WRITE:
        insertIntoActivePool (writeRequestsPool, request);
        break;
      case READ:
        insertIntoActivePool (readRequestsPool, request);
        break;
      }
    }
  }

  // This method does three functions:
  // 1.0 If the channel is being seen for the first time by this selector a key will be created.
  // 1.1 If this chanenel is in a particular role for the first time, a new list buffer in the particualr pool will be created
  // 2.0 If the list buffer is empty, the key will be enabled in the selector for that action.
  // 2.1 If the list buffer isn't empty, the key will not be enabled in the selector for that action.
  //     In that case it is either already enabled for that role or a worker is currently active in thet role.
  //     We do not want two workers active in teh same role on the same channel.
  /**
   * Called in Selector thread
   */
  private void insertIntoActivePool (RequestorPool pool, SelectorRequest request) {

    // if this is the first time selector is seeing this channel, acquire a key
    //  no synchronization required as only one thread shoudl be inserting this channel at the moment

    try {
      if (request.key == null) {
        request.key = request.channel.register (selector, request.op);
      }
    }
    catch (ClosedChannelException e) {
      logger.error ("Got exception trying to register closed channel.");
      request.setStatus (SelectorRequest.Status.CLOSED);

      // call back request callable in worker thread
      SendJob sendJob = new SendJob (request);
      FutureTask task = new FutureTask (sendJob);
      executor.execute (task);
    }

    RequestsBuffer requests = null;
    synchronized (pool) {

      // Acquire pool lock and check if a list exists for this key. If not create one
      requests = pool.get (request.key);
      if (requests == null) {
        requests = new RequestsBuffer ();
        connectRequestsPool.put (request.key, requests);
      }
    }

    try {
      synchronized (requests) {
        if (requests.isEmpty ()) {
          // ops registration will be repeated if this is the first thme the channel is being seen
          int currentOps = request.key.interestOps ();
          int newOps = currentOps | request.op;
          request.key.interestOps (newOps);
        }
        requests.add (requests.size(), request);
      }

      insertByTime (timeOrderedRequests, request);
    }
    catch (Exception ex) {
      // channel interest ops weren't updated, so current ops are fine. We aren't leaving around extra ops
      // internal data structures don't need cleanup as request wasn't inserted yet.
      logger.error ("Failed to update selector interest ops for this channel.");
      request.setStatus (SelectorRequest.Status.FAILED);

      // call back request callable in worker thread
      SendJob sendJob = new SendJob (request);
      FutureTask task = new FutureTask (sendJob);
      executor.execute (task);
    }
  }

  /**
   * Called in Selector thread
   */
  private void insertByTime (RequestsBuffer actions, SelectorRequest newAction) {

    synchronized (actions) {
      boolean inserted = false;
      int indx = actions.size () - 1;
      for (ListIterator<SelectorRequest> iter = actions.listIterator(actions.size()); iter.hasPrevious();) {
        SelectorRequest action = iter.previous();
        if (action.deadline.compareTo (newAction.deadline) > 0) {
          break;
        }
        indx--;
      }
      actions.add (indx, newAction);
    }
  }

  /**
   * Called in Selector thread
   */
  private long cleanupExpiredRequests () {

    long sleepTime = Long.MAX_VALUE;
    synchronized (timeOrderedRequests) {
      for (Iterator<SelectorRequest> iter = timeOrderedRequests.iterator (); iter.hasNext();) {
        SelectorRequest request = iter.next();

        // if not pending, some action is being taken, don't interfere
        if (request.status != SelectorRequest.Status.PENDING) {
          iter.remove ();
          continue;
        }

        // if still pending and time expired, no action is being taken, just expire the sucker
        // leave the op registration alone. We don't know if another request is pending. Upon
        //  firing, selector will check if any unpired requests are pending.
        if (request.deadline.getTime() <= System.currentTimeMillis()) {

          // a worker thread may already have caught the expired request. In that case
          //  don't refire the status update.
          if (request.status != SelectorRequest.Status.EXPIRED) {
            request.setStatus (SelectorRequest.Status.EXPIRED);
          }

          // Regardless of who set expired status (worker or us) its our job to issue the
          //  request for a request callback.
          // call back request callable in worker thread
          SendJob sendJob = new SendJob (request);
          FutureTask task = new FutureTask (sendJob);
          executor.execute (task);
          iter.remove ();
          continue;
        }

        // the first non-pending, still to expire action gives us teh next sleep time.
        sleepTime = request.deadline.getTime() - System.currentTimeMillis ();
        break;
      }
    }

    return sleepTime;
  }

  /**
   * Called in Worker thread
   */
  private void callbackRequestor (SelectionKey key, RequestorPool pool) {

    RequestsBuffer requestsBuffer = pool.get(key);
    SelectorRequest request = null;

    synchronized (requestsBuffer) {
      for (Iterator<SelectorRequest> iter = requestsBuffer.iterator(); iter.hasNext();) {
        request = iter.next();

        // if current request is already marked expired, remove from list & go on
        if (request.status == SelectorRequest.Status.EXPIRED) {
          request = null;
          iter.remove ();
          continue;
        }

        // if current request isn't expired but its status hasn't been updated update status.
        //  expiration callback will be done by selector loop
        if (request.status == SelectorRequest.Status.PENDING && request.deadline.getTime() <= System.currentTimeMillis()) {
          request.setStatus (SelectorRequest.Status.EXPIRED);
          iter.remove ();
          continue;
        }
        break;
      }
    }

    if (request != null) {
      request.setStatus (SelectorRequest.Status.READY);

      boolean reActivate = false;
      if (request.callback != null) {
        try {
          reActivate = request.callback.call (request);
        }
        catch (Exception ex) {
          // discard any uncaught requestor exceptions
        }

        // if connected is closed, try to reactivate the request. this will let
        //  the selector loop to cleanup the channel from its structures.
        //  Unwanted behavior of calling the requestor callback again.
        if (!request.channel.isConnected()) {
          reActivate = true;
        }
      }

      if (reActivate) {
        request.setStatus (SelectorRequest.Status.ASSIGNED);
      }

      if (!reActivate) {
        // request complete; remove it from buffer and activate key only if buffer has more elements
        synchronized (requestsBuffer) {
          request.setStatus (SelectorRequest.Status.FINISHED); // required to indicate request callback has finished
          requestsBuffer.remove (0);
          // cleanup expired requests
          request = null;
          for (Iterator<SelectorRequest> iter = requestsBuffer.iterator(); iter.hasNext();) {
            request = iter.next();

            // if current request is already marked expired, remove from list & go on
            if (request.status == SelectorRequest.Status.EXPIRED) {
              iter.remove();
              request = null;
              continue;
            }

            // if current request isn't expired but its status hasn't been updated update status.
            //  expiration callback will be done by selector loop
            if (request.deadline.getTime() < System.currentTimeMillis()) {
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
      if (request != null) {
        synchronized (reActivateBuffer) {
          reActivateBuffer.add (0, request);
          selector.wakeup ();
        }
      }
    }
  }

  /**
   * Called in Worker thread
   */
  private void handleAction (SelectionKey key, SelectorRequest.Type type, SelectorRequest request) {

    // if request object is available just call its callable object
    if (request != null) {
      if (request.callback != null) {
        request.callback.call (request);
      }
    }
    else {
      // else we have more work
      switch (type) {
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

  private static class SelectorManagerThreadFactory implements ThreadFactory
  {
    private static final AtomicInteger poolNumber = new AtomicInteger(1);
    private final ThreadGroup group;
    private final AtomicInteger threadNumber = new AtomicInteger(1);
    private final String namePrefix;


    private SelectorManagerThreadFactory() {
      SecurityManager s = System.getSecurityManager();
      group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
      namePrefix = "org.jacorb.util.SelectorManager ThreadPoolExecutor pool-" + poolNumber.getAndIncrement() + "-thread-";
    }


    public Thread newThread(Runnable runnable) {
      Thread t = new Thread(group, runnable, namePrefix + threadNumber.getAndIncrement(), 0);
      if (t.isDaemon()) {
        t.setDaemon(false);
      }

      if (t.getPriority() != Thread.NORM_PRIORITY) {
        t.setPriority(Thread.NORM_PRIORITY);
      }

      return t;
    }
  }

  // a SendJob is a helper class used to send requests in background
  private class SendJob implements Callable
  {
    private SelectionKey key = null;
    private SelectorRequest.Type type = null;
    private SelectorRequest request = null;

    SendJob (SelectionKey key, SelectorRequest.Type type) {
      this.key = key;
      this.type = type;
    }

    SendJob (SelectorRequest request) {
      this.request = request;
    }

    public java.lang.Object call()
      throws Exception {

      if (running) {
          handleAction (key, type, request);
      }
      return (null);
    }
  }
}