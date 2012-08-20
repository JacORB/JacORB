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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.jacorb.config.Configurable;
import org.jacorb.config.Configuration;
import org.jacorb.config.ConfigurationException;
import org.jacorb.orb.dsi.ServerRequest;
import org.jacorb.poa.except.ResourceLimitReachedException;
import org.jacorb.poa.util.StringPair;
import org.omg.CORBA.BAD_INV_ORDER;
import org.slf4j.Logger;

/**
 * This class manages a queue of ServerRequest objects.
 *
 * @author Reimo Tiedemann, FU Berlin
 */
public class RequestQueue
    implements Configurable
{
    private RequestQueueListener queueListener;

    /** the configuration object for this queue */
    private org.jacorb.config.Configuration configuration = null;
    private Logger logger;
    private int queueMin;
    private int queueMax;
    private boolean queueWait;

    private boolean configured = false;

    private final LinkedList queue = new LinkedList();

    public synchronized void configure(Configuration myConfiguration)
        throws ConfigurationException
    {
        if (configured)
        {
            return;
        }

        this.configuration = (org.jacorb.config.Configuration)myConfiguration;
        logger = configuration.getLogger("jacorb.poa.queue");
        queueMax = configuration.getAttributeAsInteger("jacorb.poa.queue_max", 100);
        queueMin = configuration.getAttributeAsInteger("jacorb.poa.queue_min", 10);
        queueWait = configuration.getAttributeAsBoolean("jacorb.poa.queue_wait",false);
        List queueListeners = configuration.getAttributeList("jacorb.poa.queue_listeners");

        for (Iterator i = queueListeners.iterator(); i.hasNext();)
        {
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

        configured = true;
    }

    /**
       * Adds a request to this queue. The properties
       * <code>jacorb.poa.queue_{min,max,wait}</code> specify what happens when
       * the queue is full, i.e. when it already contains <code>queue_max</code>
       * requests. If <code>queue_wait</code> is <i>off</i>, then this method
       * does not add the request and throws a
       * <code>ResourceLimitReachedException</code>. If <code>queue_wait</code>
       * is <i>on</i>, then this method blocks until no more than
       * <code>queue_min</code> requests are in the queue; it then adds the
       * request, and returns.
       *
       * @param request the request
       *
       * @throws ResourceLimitReachedException the resource limit reached
       *         exception
       */

    protected synchronized void add(ServerRequest request)
        throws ResourceLimitReachedException
    {
        checkIsConfigured();

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

        if (logger.isDebugEnabled())
        {
            logger.debug("rid: " + request.requestId() +
                         " opname: " + request.operation() +
                         " is queued (queue size: " + queue.size() + ")");
        }

        // notify a queue listener
        if (queueListener != null)
        {
            queueListener.requestAddedToQueue(request, queue.size());
        }
    }

    /**
       * Adds the request queue listener.
       *
       * @param listener the listener
       */
    protected synchronized void addRequestQueueListener(RequestQueueListener listener)
    {
        checkIsConfigured();

        queueListener = EventMulticaster.add(queueListener, listener);
    }

    /**
       * Deliver content.
       *
       * @return the string pair[]
       */
    protected synchronized StringPair[] deliverContent()
    {
        checkIsConfigured();

        StringPair[] result = new StringPair[queue.size()];
        Iterator en = queue.iterator();
        ServerRequest sr;
        for (int i=0; i<result.length; i++)
        {
            sr = (ServerRequest) en.next();
            result[i] = new StringPair(Integer.toString(sr.requestId()), new String( sr.objectId() ) );
        }
        return result;
    }

    /**
     * Only used by the POA GUI.
     *
     * @param rid the rid
     * @return the element and remove
     */
    synchronized ServerRequest getElementAndRemove(int rid)
    {
        checkIsConfigured();

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
                    {
                        queueListener.requestRemovedFromQueue(result, queue.size());
                    }
                    return result;
                }
            }
        }
        return null;
    }

    /**
     * Used by the RequestController - gets the first element
     *
     * @return the first
     */
    protected synchronized ServerRequest getFirst()
    {
        checkIsConfigured();

        if (!queue.isEmpty())
        {
            return (ServerRequest) queue.getFirst();
        }
        return null;
    }

    /**
     * Used by the RequestController - checks if is empty.
     *
     * @return true, if is empty
     */
    protected boolean isEmpty()
    {
        checkIsConfigured();

        return queue.isEmpty();
    }

    /**
     * Used by the RequestController - removes the first.
     *
     * @return the server request
     */
    protected synchronized ServerRequest removeFirst()
    {
        checkIsConfigured();

        if (!queue.isEmpty())
        {
            ServerRequest result = (ServerRequest) queue.removeFirst();
            this.notifyAll();
            // notify a queue listener

            if (queueListener != null)
            {
                queueListener.requestRemovedFromQueue(result, queue.size());
            }
            return result;
        }
        return null;
    }

    /**
     * Used by the RequestController - removes the last.
     *
     * @return the server request
     */
    protected synchronized ServerRequest removeLast()
    {
        checkIsConfigured();

        if (!queue.isEmpty())
        {
            ServerRequest result = (ServerRequest) queue.removeLast();
            this.notifyAll();
            // notify a queue listener
            if (queueListener != null)
            {
                queueListener.requestRemovedFromQueue(result, queue.size());
            }
            return result;
        }
        return null;
    }

    /**
     * Removes the request queue listener.
     *
     * @param listener the listener
     */
    protected synchronized void removeRequestQueueListener(RequestQueueListener listener)
    {
        checkIsConfigured();
        queueListener = EventMulticaster.remove(queueListener, listener);
    }

    /**
     * Size.
     *
     * @return the int
     */
    protected int size()
    {
        return queue.size();
    }

    /**
     * Check is configured.
     */
    private void checkIsConfigured()
    {
        if (!configured)
        {
            throw new BAD_INV_ORDER("RequestQueue is not configured yet.");
        }
    }
}
