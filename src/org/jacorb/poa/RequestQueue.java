package org.jacorb.poa;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2003  Gerald Brose.
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

import java.util.*;

/**
 * This class will manage a queue of ServerRequest objects.
 *
 * @author Reimo Tiedemann, FU Berlin
 * @version 1.03, 06/09/99, RT
 */
public class RequestQueue
{
    private RequestQueueListener queueListener;
    private RequestController controller;
    private LogTrace logTrace;
    private Vector queue = new Vector(POAConstants.QUEUE_CAPACITY_INI, POAConstants.QUEUE_CAPACITY_INC);

    private RequestQueue()
    {
    }

    protected RequestQueue(RequestController _controller, LogTrace _logTrace)
    {
        controller = _controller;
        logTrace = _logTrace;
    }

    /**
     * Adds a request to this queue.  The properties
     * <code>jacorb.poa.queue_{min,max,wait}</code> specify what happens
     * when the queue is full, i.e. when it already contains
     * <code>queue_max</code> requests.  If <code>queue_wait</code> is
     * <i>off</i>, then this method does not add the request and throws a 
     * <code>ResourceLimitReachedException</code>.  If <code>queue_wait</code>
     * is <i>on</i>, then this method blocks until no more than
     * <code>queue_min</code> requests are in the queue; it then adds the
     * request, and returns.
     */
    protected synchronized void add(ServerRequest request)
        throws ResourceLimitReachedException
    {
        if (queue.size() >= Environment.queueMax())
        {
            if (Environment.queueWait())
            {
                while (queue.size() > Environment.queueMin())
                {
                    try
                    {
                        this.wait();
                    }
                    catch (InterruptedException ex)
                    {
                        // ignore
                    }   
                }
            }
            else
            {
                throw new ResourceLimitReachedException();
            }
        }
        queue.addElement(request);

        if (queue.size() == 1) {
            controller.continueToWork();
        }
        if (logTrace.test(3))
            logTrace.printLog(request, "is queued (queue size: " + queue.size() + ")");
        // notify a queue listener
        if (queueListener != null) queueListener.requestAddedToQueue(request, queue.size());
    }
    protected synchronized void addRequestQueueListener(RequestQueueListener listener) {
        queueListener = EventMulticaster.add(queueListener, listener);
    }
    protected synchronized StringPair[] deliverContent() {
        StringPair[] result = new StringPair[queue.size()];
        Enumeration en = queue.elements();
        ServerRequest sr;
        for (int i=0; i<result.length; i++) {
            sr = (ServerRequest) en.nextElement();
            result[i] = new StringPair(sr.requestId()+"", new String( sr.objectId() ) );
        }
        return result;
    }
    protected synchronized ServerRequest getElementAndRemove(int rid) {
        if (!queue.isEmpty()) {
            Enumeration en = queue.elements();
            ServerRequest result;
            while (en.hasMoreElements()) {
                result = (ServerRequest) en.nextElement();
                if (result.requestId() == rid) {
                    queue.removeElement(result);
                    this.notifyAll();
                    // notify a queue listener
                    if (queueListener != null) queueListener.requestRemovedFromQueue(result, queue.size());
                    return result;
                }
            }
        }
        return null;
    }
    protected synchronized ServerRequest getFirst() {
        if (!queue.isEmpty()) {
            return (ServerRequest) queue.firstElement();
        }
        return null;
    }
    protected boolean isEmpty() {
        return queue.isEmpty();
    }
    protected synchronized ServerRequest removeFirst() {
        if (!queue.isEmpty()) {
            ServerRequest result = (ServerRequest) queue.elementAt(0);
            queue.removeElementAt(0);
            this.notifyAll();
            // notify a queue listener
            if (queueListener != null) queueListener.requestRemovedFromQueue(result, queue.size());
            return result;
        }
        return null;
    }
    protected synchronized ServerRequest removeLast() {
        if (!queue.isEmpty()) {
            ServerRequest result = (ServerRequest) queue.lastElement();
            queue.removeElementAt(queue.size()-1);
            this.notifyAll();
            // notify a queue listener
            if (queueListener != null) queueListener.requestRemovedFromQueue(result, queue.size());
            return result;
        }
        return null;
    }
    protected synchronized void removeRequestQueueListener(RequestQueueListener listener) {
        queueListener = EventMulticaster.remove(queueListener, listener);
    }
    protected int size() {
        return queue.size();
    }
}
