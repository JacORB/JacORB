package org.jacorb.poa;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2004 Gerald Brose.
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

import java.util.*;

import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.configuration.*;

import org.jacorb.orb.dsi.ServerRequest;
import org.jacorb.poa.except.ResourceLimitReachedException;
import org.jacorb.poa.util.StringPair;

/**
 * This class manages a queue of ServerRequest objects.
 *
 * @author Reimo Tiedemann, FU Berlin
 * @version $Id$
 */
public class RequestQueue
    implements Configurable
{
    private RequestQueueListener queueListener;
    private RequestController controller;

    /** the configuration object for this queue */
    private org.jacorb.config.Configuration configuration = null;
    private Logger logger;
    private int queueMin;
    private int queueMax;
    private boolean queueWait;
    private List queueListeners;
    
    private boolean configured = false;

    private List queue =
        new ArrayList (POAConstants.QUEUE_CAPACITY_INI);
                    // POAConstants.QUEUE_CAPACITY_INC);

    protected RequestQueue(RequestController controller)
    {
        this.controller = controller;
    }

    public synchronized void configure(Configuration myConfiguration)
        throws ConfigurationException
    {
        if (configured) return;
        
        this.configuration = (org.jacorb.config.Configuration)myConfiguration;
        logger = configuration.getNamedLogger("jacorb.poa.queue");
        queueMax = configuration.getAttributeAsInteger("jacorb.poa.queue_max", 100);
        queueMin = configuration.getAttributeAsInteger("jacorb.poa.queue_min", 10);
        queueWait = configuration.getAttributeAsBoolean("jacorb.poa.queue_wait",false);
        queueListeners = configuration.getAttributeList("jacorb.poa.queue_listeners");
        configured = true;
        
        for (Iterator i = queueListeners.iterator(); i.hasNext();) {
            String className = (String)i.next();
            try
            {
                RequestQueueListener rql = (RequestQueueListener)
                    org.jacorb.util.ObjectUtil.classForName(className).newInstance();
                addRequestQueueListener(rql);
            }
            catch (Exception ex)
            {
                throw new ConfigurationException ("could not instantiate queue listener", 
                                                  ex);
            }
        }
        
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
        if (!configured)
            throw new Error("RQ.add(): not configured!");

        if (queue.size() >= queueMax )
        {
            if (logger.isWarnEnabled())
            {
                logger.warn("Request queue is full, consider increasing "
                          + "jacorb.poa.queue_max (currently: "
                          + queueMax + ")");   
            }

            if ( queueWait )
            {
                while (queue.size() > queueMin )
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
        queue.add(request);

        if (queue.size() == 1)
        {
            controller.continueToWork();
        }

        if (logger.isDebugEnabled())
        {
            logger.debug("rid: " + request.requestId() +
                         " opname: " + request.operation() +
                         " is queued (queue size: " + queue.size() + ")");
        }

        // notify a queue listener
        if (queueListener != null)
            queueListener.requestAddedToQueue(request, queue.size());
    }

    protected synchronized void addRequestQueueListener(RequestQueueListener listener)
    {
        if (!configured)
            throw new Error("RQ.add(): not configured!");
        queueListener = EventMulticaster.add(queueListener, listener);
    }

    protected synchronized StringPair[] deliverContent()
    {
         if (!configured)
            throw new Error("RQ.add(): not configured!");
       StringPair[] result = new StringPair[queue.size()];
        Iterator en = queue.iterator();
        ServerRequest sr;
        for (int i=0; i<result.length; i++)
        {
            sr = (ServerRequest) en.next();
            result[i] = new StringPair(sr.requestId()+"", new String( sr.objectId() ) );
        }
        return result;
    }

    protected synchronized ServerRequest getElementAndRemove(int rid)
    {
        if (!configured)
            throw new Error("RQ.add(): not configured!");
        if (!queue.isEmpty())
        {
            Iterator en = queue.iterator();
            ServerRequest result;
            while (en.hasNext())
            {
                result = (ServerRequest) en.next();
                if (result.requestId() == rid)
                {
                    en.remove();
                    this.notifyAll();
                    // notify a queue listener
                    if (queueListener != null)
                        queueListener.requestRemovedFromQueue(result, queue.size());
                    return result;
                }
            }
        }
        return null;
    }

    protected synchronized ServerRequest getFirst()
    {
         if (!configured)
            throw new Error("RQ.add(): not configured!");
       if (!queue.isEmpty())
        {
            return (ServerRequest) queue.get(0);
        }
        return null;
    }

    protected boolean isEmpty()
    {
        if (!configured)
            throw new Error("RQ.add(): not configured!");
        return queue.isEmpty();
    }

    protected synchronized ServerRequest removeFirst()
    {
        if (!configured)
            throw new Error("RQ.add(): not configured!");
        if (!queue.isEmpty())
        {
            ServerRequest result = (ServerRequest) queue.get(0);
            queue.remove(0);
            this.notifyAll();
            // notify a queue listener

            if (queueListener != null)
                queueListener.requestRemovedFromQueue(result, queue.size());
            return result;
        }
        return null;
    }

    protected synchronized ServerRequest removeLast()
    {
         if (!configured)
            throw new Error("RQ.add(): not configured!");
       if (!queue.isEmpty())
        {
            int index = queue.size() - 1;
            ServerRequest result = (ServerRequest) queue.get(index);
            queue.remove(index);
            this.notifyAll();
            // notify a queue listener
            if (queueListener != null)
                queueListener.requestRemovedFromQueue(result, queue.size());
            return result;
        }
        return null;
    }

    protected synchronized void removeRequestQueueListener(RequestQueueListener listener)
    {
        if (!configured)
            throw new Error("RQ.add(): not configured!");
        queueListener = EventMulticaster.remove(queueListener, listener);
    }

    protected int size()
    {
        return queue.size();
    }
}
