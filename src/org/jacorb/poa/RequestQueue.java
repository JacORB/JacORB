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

import java.util.Vector;
import java.util.Enumeration;

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
    private RequestQueue() {
    }
    protected RequestQueue(RequestController _controller, LogTrace _logTrace) {
        controller = _controller;
        logTrace = _logTrace;
    }
    synchronized protected void add(ServerRequest request) throws ResourceLimitReachedException {
        if (queue.size() == Environment.queueMax()) {
            throw new ResourceLimitReachedException();
        }
        queue.addElement(request);
                                
        if (queue.size() == 1) {
            controller.continueToWork();
        }
        logTrace.printLog(3, request, "is queued (queue size: " + queue.size() + ")");
        // notify a queue listener
        if (queueListener != null) queueListener.requestAddedToQueue(request, queue.size());
    }
    protected synchronized void addRequestQueueListener(RequestQueueListener listener) {
        queueListener = EventMulticaster.add(queueListener, listener);
    }
    synchronized protected StringPair[] deliverContent() {
        StringPair[] result = new StringPair[queue.size()];
        Enumeration en = queue.elements();
        ServerRequest sr;
        for (int i=0; i<result.length; i++) {
            sr = (ServerRequest) en.nextElement();
            result[i] = new StringPair(sr.requestId()+"", POAUtil.objectId_to_string(sr.objectId())); 
        }
        return result;
    }
    synchronized protected ServerRequest getElementAndRemove(int rid) {
        if (!queue.isEmpty()) {
            Enumeration en = queue.elements();
            ServerRequest result;
            while (en.hasMoreElements()) {
                result = (ServerRequest) en.nextElement();
                if (result.requestId() == rid) {
                    queue.removeElement(result);
                    // notify a queue listener                                      
                    if (queueListener != null) queueListener.requestRemovedFromQueue(result, queue.size());
                    return result;
                }
            }
        }
        return null;
    }
    synchronized protected ServerRequest getFirst() {
        if (!queue.isEmpty()) {
            return (ServerRequest) queue.firstElement();
        }
        return null;
    }
    protected boolean isEmpty() {
        return queue.isEmpty();
    }
    synchronized protected ServerRequest removeFirst() {
        if (!queue.isEmpty()) {                 
            ServerRequest result = (ServerRequest) queue.elementAt(0);
            queue.removeElementAt(0);
            // notify a queue listener
            if (queueListener != null) queueListener.requestRemovedFromQueue(result, queue.size());
            return result;
        }
        return null;
    }
    synchronized protected ServerRequest removeLast() {
        if (!queue.isEmpty()) {
            ServerRequest result = (ServerRequest) queue.lastElement();
            queue.removeElementAt(queue.size()-1);
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

 






