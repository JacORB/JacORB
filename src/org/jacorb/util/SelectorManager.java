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
import java.util.concurrent.locks.ReentrantLock;

import org.jacorb.config.*;
import org.slf4j.Logger;

public class SelectorManager extends Thread {

  final private RequestorPool connectRequestsPool;
  final private RequestorPool writeRequestsPool;
  final private RequestorPool readRequestsPool;

  final private RequestsBuffer timeOrderedRequests;
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
  private final ReentrantLock lock = new ReentrantLock();

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
    catch (IOException ex) {

      // System.out.println ("Got an exception in SelectorManager contstructor: " + ex.getMessage());
      throw new RuntimeException (ex);
    }
  }

  public void configure(Configuration configuration)
    throws ConfigurationException {

    if (configuration == null) {
      throw new ConfigurationException ("SelectorManager.configure was passed a null Configuration object");
    }

    org.jacorb.config.Configuration jacorbConfiguration = (org.jacorb.config.Configuration) configuration;
    logger = jacorbConfiguration.getLogger("jacorb.util");

    loggerDebugEnabled = logger.isDebugEnabled();
  }

  public void run () {

    try {
      while (running) {

        synchronized (newRequests) {
          for (Iterator<SelectorRequest> newRequestsIter = newRequests.iterator (); newRequestsIter.hasNext();) {
            SelectorRequest request = newRequestsIter.next ();
            request.setStatus (SelectorRequest.Status.PENDING);
            insertIntoActivePool (request);
          }
          newRequests.clear ();
        }

        synchronized (reActivateBuffer) {
          for (Iterator<SelectorRequest> reActivateIter = reActivateBuffer.iterator (); reActivateIter.hasNext();) {
            SelectorRequest request = reActivateIter.next ();
            reActivateRequest (request);
          }
          reActivateBuffer.clear ();
        }

        // cleanup expired requests & compute next sleep unit
        long sleepTime = cleanupExpiredRequests ();

        if (loggerDebugEnabled) {
          logger.debug (Thread.currentThread().getName() + "> Selector will wait for "
                        + (sleepTime == Long.MAX_VALUE ? 0 : sleepTime) + " millis.");
        }

        try {
          selector.select (sleepTime);
        }
        catch (IllegalArgumentException ex) {
          logger.error ("Select timeout ("+ sleepTime + ") argument flawed: " + ex.toString());
          // shouldn't be any problem to continue;
        }

        // // this will be verbose. Uncomment cautiously
        // if (loggerDebugEnabled) {
        //   logger.debug (Thread.currentThread().getName() + "> select() finished. Gonna check if selector loop can be broken.");
        // }

        synchronized (lock) {

          if (!running) {
            if (loggerDebugEnabled) {
              logger.debug (Thread.currentThread().getName()
                            + "> Breaking out of Selector loop; 'running' flag was disabled.");
            }
            break;
          }
        }

        for (Iterator iter = selector.selectedKeys().iterator(); iter.hasNext();) {
          SelectionKey key = (SelectionKey) iter.next();
          iter.remove();

          if (!key.isValid()) {
            continue;
          }

          // loop four times to account for each of the ecah of the possible IO operations
          //  connect, accept, read, write
          boolean checkConnect, checkAccept, checkRead, checkWrite;
          checkConnect = checkAccept = checkRead = checkWrite = true;
          for (int count = 0; count < 4; count++) {
            int op = 0;
            SelectorRequest.Type jobType = SelectorRequest.Type.CONNECT;
            // delegate work to worker thread
            if (checkConnect && key.isConnectable()) {
              checkConnect = false;
              op = SelectionKey.OP_CONNECT;
              jobType = SelectorRequest.Type.CONNECT;
            }
            else if (checkAccept && key.isAcceptable()) {
              checkAccept = false;
              op = SelectionKey.OP_ACCEPT;
              jobType = SelectorRequest.Type.ACCEPT;
            } else if (checkRead && key.isReadable()) {
              checkRead = false;
              op = SelectionKey.OP_READ;
              jobType = SelectorRequest.Type.READ;
            } else if (checkWrite && key.isWritable()) {
              checkWrite = false;
              op = SelectionKey.OP_WRITE;
              jobType = SelectorRequest.Type.WRITE;
            }
            else {
              break;
            }

            if (op != 0) {

              if (loggerDebugEnabled) {
                logger.debug (Thread.currentThread().getName() + " Key " + key.toString() + " ready for action: " + jobType.toString());
              }

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
      logger.error ("Exception in Selector loop. Bailing out: " + ex.toString());
      ex.printStackTrace ();
    }
    running = false;

    try {
      // clean up all pending requests

      if (loggerDebugEnabled) {
        logger.debug (Thread.currentThread().getName()
                      + "> SelectorManager loop is broken. Cleaning up pending requests");
      }
      cleanup ();

      if (loggerDebugEnabled) {
        logger.debug (Thread.currentThread().getName() + "> Issuing shutdown command to Threadpool executor.");
      }
      executor.shutdown ();
    }
    catch (Exception ex) {
      logger.error ("Exception in Selector manager cleanup. Bailing out: " + ex.toString());
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

        if (loggerDebugEnabled) {
          logger.debug (Thread.currentThread().getName() + "> Cleaning up request. Request type: "
                        + request.type.toString() + ", Request status: " + request.status.toString());
        }
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

    synchronized (lock) {
      if (!running) {
        return;
      }
      else {

        if (loggerDebugEnabled) {
          logger.debug (Thread.currentThread().getName() + "> Halting Selector Manager.");
        }

        running = false;
        selector.wakeup ();
      }
    }

    if (loggerDebugEnabled) {
      logger.debug (Thread.currentThread().getName() + "> Waiting for Threadpool executor to wind down.");
    }
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

  public boolean add (SelectorRequest request) {

    boolean immediateCallback = false;
    if (request == null) {
      return false;
    }
    if (request.type == null) {
      request.setStatus (SelectorRequest.Status.FAILED);
      immediateCallback = true;
    }
    if (request.nanoDeadline <= System.nanoTime()) {
      request.setStatus (SelectorRequest.Status.EXPIRED);
      immediateCallback = true;
    }
    if (!running) {
      request.setStatus (SelectorRequest.Status.SHUTDOWN);
      immediateCallback = true;
    }

    switch (request.type) {
    case CONNECT:
    case WRITE:
    case READ:
      if (request.channel == null) {
        request.setStatus (SelectorRequest.Status.FAILED);
        immediateCallback = true;
      }
      break;
    }

    if (immediateCallback) {
      if (request.callback != null) {
        if (loggerDebugEnabled) {
          logger.debug (Thread.currentThread().getName()
                        + "> Immediate Requestor callback in client thread. Request type: " + request.type.toString() +
                        ", Request status: " + request.status.toString());
        }

        try {
          request.callback.call (request);
        }
        catch (Exception ex) {
          // disregrad any client exceptions
        }

        if (loggerDebugEnabled) {
          logger.debug (Thread.currentThread().getName() + "> Callback concluded");
        }
      }
      return false;
    }

    if (loggerDebugEnabled) {
      logger.debug (Thread.currentThread().getName()
                    + "> Adding request to new requests buffer. Request type: " + request.type.toString());
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

    if (request.type != SelectorRequest.Type.CONNECT && request.type != SelectorRequest.Type.TIMER
        && !request.channel.isConnected ()) {
      removeClosedRequests (request.key);
    }
    else {

      if (loggerDebugEnabled) {
        logger.debug (Thread.currentThread().getName()
                      + "> Reactivating request. Request type: " + request.type.toString());
      }

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

    if (key == null) {
      return;
    }

    if (loggerDebugEnabled) {
      logger.debug (Thread.currentThread().getName() + "> Removing request matching key " + key.toString());
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
  private void removeClosedRequests (SelectionKey key, RequestorPool pool) {

    RequestsBuffer requestBuffer;
    synchronized (pool) {
      requestBuffer = pool.remove (key);
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

    if (loggerDebugEnabled) {
      logger.debug (Thread.currentThread().getName()
                    + "> Inserting request into active pool. Request type: " + request.type.toString());
    }

    if (request.type != SelectorRequest.Type.CONNECT && request.type != SelectorRequest.Type.TIMER
        && !request.channel.isConnected ()) {
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
      case TIMER:
        insertIntoTimedBuffer (request);
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

    request.key = request.channel.keyFor (selector);
    if (request.key == null) {
      try {
        request.key = request.channel.register (selector, request.op);
      }
      catch (ClosedChannelException e) {
        logger.error ("Got exception trying to register closed channel.");
        request.setStatus (SelectorRequest.Status.CLOSED);

        // call back request callable in worker thread
        SendJob sendJob = new SendJob (request);
        FutureTask task = new FutureTask (sendJob);
        executor.execute (task);

        return;
      }
    }

    RequestsBuffer requests = null;
    synchronized (pool) {

      // Acquire pool lock and check if a list exists for this key. If not create one
      requests = pool.get (request.key);
      if (requests == null) {
        requests = new RequestsBuffer ();
        pool.put (request.key, requests);
      }
    }

    boolean opUpdateFailed = false;
    int newOps = 0;
    try {
      synchronized (requests) {
        if (requests.isEmpty ()) {
          // ops registration will be repeated if this is the first time the channel is being seen
          int currentOps = request.key.interestOps ();
          newOps = currentOps | request.op;
          request.key.interestOps (newOps);
        }
        requests.add (requests.size(), request);
      }

      insertIntoTimedBuffer (request);
    }
    catch (CancelledKeyException ex) {
      logger.error ("Failed to update selector interest ops as the key is already cancelled.");
      opUpdateFailed = true;
    }
    catch (IllegalArgumentException ex) {
      logger.error ("Failed to update selector interest ops as the key. Illegal ops: " + newOps);
      opUpdateFailed = true;
    }

    if (opUpdateFailed) {
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
  private void insertIntoTimedBuffer (SelectorRequest newRequest) {

    RequestsBuffer requests = timeOrderedRequests;

    if (loggerDebugEnabled) {
      logger.debug (Thread.currentThread().getName()
                    + "> Inserting request into timed requests buffer. Request type: " + newRequest.type.toString());
    }

    synchronized (requests) {
      boolean inserted = false;
      int indx = requests.size ();
      for (ListIterator<SelectorRequest> iter = requests.listIterator(requests.size()); iter.hasPrevious();) {
        SelectorRequest request = iter.previous();
        // iterate till a request with earlier deadline is seen
        if (request.nanoDeadline < newRequest.nanoDeadline) {
          break;
        }
        indx--;
      }
      requests.add (indx, newRequest);
    }
  }

  /**
   * Called in Selector thread
   * Returns shortest expiration time in millisecond resolution
   */
  private long cleanupExpiredRequests () {

    if (loggerDebugEnabled) {
      logger.debug (Thread.currentThread().getName()
                    + "Enter SelectorManager.cleanupExpiredRequests()");
    }

    long sleepTime = 0;//Long.MAX_VALUE;
    synchronized (timeOrderedRequests) {
      for (Iterator<SelectorRequest> iter = timeOrderedRequests.iterator (); iter.hasNext();) {
        SelectorRequest request = iter.next();

        if (loggerDebugEnabled) {
          logger.debug (Thread.currentThread().getName()
                        + "Checking expiry on request. Request type: " + request.type.toString()
                        + ", request status: " + request.status.toString());
        }

        // if not pending, some action is being taken, don't interfere
        if (request.status != SelectorRequest.Status.PENDING) {
          iter.remove ();
          continue;
        }

        // if still pending and time expired, no action is being taken, just expire the sucker
        // leave the op registration alone. We don't know if another request is pending. Upon
        //  firing, selector will check if any unpired requests are pending.
        if (request.nanoDeadline <= System.nanoTime()) {

          if (loggerDebugEnabled) {
            logger.debug (Thread.currentThread().getName()
                          + "> Cleaning up expired request from timed requests queue:\n" +
                          "\trequest type: " + request.type.toString() +
                          ", request status: " + request.status.toString());
          }

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

        // the first non-pending, still to expire action gives us the next sleep time.
        sleepTime = (request.nanoDeadline - System.nanoTime())/ 1000000;
        sleepTime = (sleepTime < 0 ? 1 : sleepTime); // cannot return sleepTime 0 as thats an infinity
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
        if (request.status == SelectorRequest.Status.PENDING && request.nanoDeadline <= System.nanoTime()) {
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

        if (loggerDebugEnabled) {
          logger.debug (Thread.currentThread().getName()
                        + "> Requestor callback in worker thread. Request type: " + request.type.toString());
        }

        try {
          reActivate = request.callback.call (request);
        }
        catch (Exception ex) {
          // discard any uncaught requestor exceptions
        }

        if (loggerDebugEnabled) {
          logger.debug (Thread.currentThread().getName()
                        + "> Callback concluded. Reactivation request: " + (reActivate ? "TRUE" : "FALSE"));
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
            if (request.nanoDeadline <= System.nanoTime()) {
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

    if (loggerDebugEnabled) {
      logger.debug (Thread.currentThread().getName() + "> Enter SelectorManager.handleAction");
    }

    // if request object is available just call its callable object
    if (request != null) {
      if (request.callback != null) {

        if (loggerDebugEnabled) {
          logger.debug (Thread.currentThread().getName()
                        + "> Selector Worker thread " + Thread.currentThread().getId() + " calling request callback directly:\n" +
                        "\tRequest type: " + request.type.toString() + ", Request status: " + request.status.toString());
        }

        try {
          request.callback.call (request);
        }
        catch (Exception ex) {
          // ignore client exceptions
        }

        if (loggerDebugEnabled) {
          logger.debug (Thread.currentThread().getName() + "> Callback concluded");
        }
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

  private static class SelectorManagerThreadFactory implements ThreadFactory
  {
    private static final AtomicInteger poolNumber = new AtomicInteger(1);
    private final ThreadGroup group;
    private final AtomicInteger threadNumber = new AtomicInteger(1);
    private final String namePrefix;


    private SelectorManagerThreadFactory() {
      SecurityManager s = System.getSecurityManager();
      group = (s != null ? s.getThreadGroup() : Thread.currentThread().getThreadGroup());
      namePrefix = "SelectorManager worker-" + poolNumber.getAndIncrement() + "-thread-";
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