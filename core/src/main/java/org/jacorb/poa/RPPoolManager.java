package org.jacorb.poa;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2014 Gerald Brose / The JacORB Team.
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
import java.util.LinkedList;
import org.jacorb.config.Configuration;
import org.jacorb.config.ConfigurationException;
import org.jacorb.poa.except.POAInternalError;
import org.slf4j.Logger;

/**
 * This class provides and manages a pool of ready started threads for
 * request processing.
 *
 * @author Gerald Brose
 * @author Reimo Tiedemann
 * @see org.jacorb.poa.RequestProcessor
 */

public abstract class RPPoolManager
{
    private RPPoolManagerListener pmListener;

    // the current for (un)registering the invocation contexts
    private final Current current;
    /**
     * <code>pool</code> is the set of currently available (inactive) request processors
     */
    private final LinkedList<RequestProcessor> pool;
    /**
     * <code>activeProcessors</code> is the set of currently active processors
     */
    private final HashSet<RequestProcessor> activeProcessors;
    /**
     * <code>numberOfProcessors</code> represents the current number of used <b>AND</b> unused request
     * processors in the pools (active/inactive)
     */
    private int numberOfProcessors;
    private int numberOfWaiters;
    /**
     * <code>max_pool_size</code> is the maximum size of the pool. This is effectively its
     * burst size
     */
    private final int max_pool_size;
    /**
     * <code>min_pool_size</code> is the minimum number of request processors. This is the
     * permanent number of processors held in the pool.
     */
    private final int min_pool_size;
    // a flag for delay the pool initialization
    private boolean inUse = false;

    private final Configuration configuration;
    private final Logger logger;

    /**
     * Used to add a timeout to the time it will wait for a requestprocessor.
     */
    private final int poolThreadTimeout;

    protected RPPoolManager(Current _current, int min, int max, int pt,
                            Logger _logger, Configuration _configuration)
    {
        current = _current;
        max_pool_size = max;
        min_pool_size = min;
        poolThreadTimeout = pt;
        logger = _logger;
        configuration = _configuration;

        numberOfProcessors = 0;
        numberOfWaiters = 0;
        pool = new LinkedList<RequestProcessor>();
        activeProcessors = new HashSet<RequestProcessor>();
    }

    private void init()
    {
        if (inUse)
        {
            return;
        }

        for (int i = 0; i < min_pool_size; i++)
        {
            addProcessor();
        }

        inUse = true;
    }

    private void addProcessor()
    {
        final RequestProcessor rp = new RequestProcessor(this);

        try
        {
            rp.configure(this.configuration);
        }
        catch (ConfigurationException ex)
        {
            throw new RuntimeException (ex.toString());
        }
        current._addContext(rp, rp);
        rp.setDaemon(true);
        pool.addFirst(rp);
        ++numberOfProcessors;
        rp.start();
    }

    protected synchronized void addRPPoolManagerListener(RPPoolManagerListener listener)
    {
        pmListener = EventMulticaster.add(pmListener, listener);
    }

    /**
     * invoked by clients to indicate that they won't use this poolManager anymore.
     */
    abstract void destroy();

    /**
     * shutdown this poolManager. clients should invoke {@link #destroy()} instead.
     */
    protected synchronized void destroy(boolean really)
    {
        if (!inUse)
        {
            return;
        }

        // wait until all active processors complete
        while (!activeProcessors.isEmpty())
        {
            try
            {
                wait();
            }
            catch (InterruptedException ex)
            {
                // ignore
            }
        }

        RequestProcessor[] rps = pool.toArray(new RequestProcessor[pool.size()]);

        for (int i=0; i<rps.length; i++)
        {
            if (rps[i].isActive())
            {
                throw new POAInternalError("error: request processor is active (RequestProcessorPM.destroy)");
            }

            pool.remove(rps[i]);
            --numberOfProcessors;
            current._removeContext(rps[i]);
            rps[i].end();
        }

        inUse = false;
    }

    /**
     * returns the number of unused processors contained in the pool
     */

    protected int getPoolCount()
    {
        return pool.size();
    }

    /**
     * returns the size of the processor pool (used and unused processors)
     */

    protected synchronized int getPoolSize()
    {
        return numberOfProcessors;
    }

    /**
     * returns a processor from pool, the first call causes
     * the initialization of the processor pool,
     * if no processor available the number of processors
     * will increased until the max_pool_size is reached,
     * this method blocks if no processor available and the
     * max_pool_size is reached until a processor will released
     */

    protected synchronized RequestProcessor getProcessor()
    {
        init();

        if (pool.isEmpty() && (numberOfProcessors < max_pool_size || max_pool_size < 1))
        {
            addProcessor();
        }

        int timeout = poolThreadTimeout;
        while (pool.isEmpty())
        {
            warnPoolIsEmpty();

            long start = System.currentTimeMillis();
            try
            {
                numberOfWaiters++;
                wait(timeout);
            }
            catch (InterruptedException e)
            {
            }
            finally {
                numberOfWaiters--;
            }
            if (timeout > 0)
            {
                // Timeout configured, woken up and the timeout has expired but nothing to run
                // it with.
                if (((System.currentTimeMillis() - start) >= timeout) && pool.isEmpty ())
                {
                    // A timeout has been configured, we have finished waiting still no processors.
                    // Throw an exception
                    throw new org.omg.CORBA.TIMEOUT ("No request processor available to handle request");
                }
                // If we have woken up before the timeout and the pool is still empty - have another
                // go and go back to sleep.
                else if ((System.currentTimeMillis() - start) < timeout && pool.isEmpty())
                {
                    // Need to reset timeout so we finish waiting.
                    timeout -= (System.currentTimeMillis() - start);
                }
            }

            // BZ946: There are some corner cases with request processor sizing.
            //
            // If min==max then the corner cases do not occur.
            //
            // In the default situation of e.g. min=5, max=20 then its
            // possible that when we reach the maximum number of processors,
            // use 1, and then go to release it, it will not get placed back
            // in the pool (as 19 > 5). In fact all 15 of the 'burst'
            // processors will be ended.
            //
            // If we are oscillating between min and max size then its possible that this thread
            // may be looping on isEmpty. A RequestProcessor finishes and frees itself by calling
            // releaseProcessor. As per above this this processor may just be released and not added
            // to the pool. Therefore the pool is still isEmpty. However if another thread now tries
            // to get a processor it will find the pool isEmpty and there is room to create a processor
            // Therefore the pool size increases and this thread keeps looping. While it might eventually
            // break out of the loop adding the below test breaks this pathological condition.
            if (pool.isEmpty() && (numberOfProcessors < max_pool_size || max_pool_size < 1))
            {
                addProcessor();
            }
        }

        RequestProcessor requestProcessor = pool.removeFirst();
        activeProcessors.add (requestProcessor);

        // notify a pool manager listener
        if (pmListener != null)
        {
            pmListener.processorRemovedFromPool(requestProcessor, pool.size(), numberOfProcessors);
        }

        return requestProcessor;
    }

    protected void warnPoolIsEmpty()
    {
        if (logger.isWarnEnabled())
        {
            logger.warn("Thread pool exhausted, consider increasing "
                      + "jacorb.poa.thread_pool_max (currently: "
                      + max_pool_size + ")");
        }
    }


    /**
     * gives a processor back into the pool if the number of
     * available processors is smaller than min_pool_size,
     * otherwise the processor will terminate
     */

    protected synchronized void releaseProcessor(RequestProcessor rp)
    {
        activeProcessors.remove (rp);

        if (pool.size() < min_pool_size || pool.size() < numberOfWaiters)
        {
            pool.addFirst(rp);
        }
        else
        {
            numberOfProcessors--;
            current._removeContext(rp);
            rp.end();
        }
        // notify a pool manager listener
        if (pmListener != null)
        {
            pmListener.processorAddedToPool(rp, pool.size(), numberOfProcessors);
        }

        // notify whoever is waiting for the release of active processors
        notifyAll();
    }

    protected synchronized void removeRPPoolManagerListener(RPPoolManagerListener listener)
    {
        pmListener = EventMulticaster.remove(pmListener, listener);
    }
}
