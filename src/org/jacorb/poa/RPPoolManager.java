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

import org.jacorb.poa.except.*;

import org.jacorb.poa.except.POAInternalError;
import java.util.*;

/**
 * This class provides and manages a pool of ready started threads for
 * request processing.
 *
 * @author Gerald Brose, Reimo Tiedemann, FU Berlin
 * @version 1.04, 10/26/99, RT
 * @see org.jacorb.poa.RequestProcessor
 */

public class RPPoolManager
{
    private RPPoolManagerListener pmListener;

    // the current for (un)registering the invocation contexts
    private Current current;
    /**
     * <code>pool</code> represents the total number of request processors.
     */
    private List pool;
    /**
     * <code>unused_size</code> represents the current number of unused request processors
     * in the pool.
     */
    private int unused_size;
    /**
     * <code>max_pool_size</code> is the maximum size of the pool.
     */
    private int max_pool_size;
    /**
     * <code>min_pool_size</code> is the minimum number of request processors.
     */
    private int min_pool_size;
    // a flag for delay the pool initialization
    private boolean inUse = false;

    private RPPoolManager() {
    }

    protected RPPoolManager(Current _current, int min, int max)
    {
        current = _current;
        max_pool_size = max;
        min_pool_size = min;
    }

    private void addProcessor()
    {
        RequestProcessor rp = new RequestProcessor(this);
        current._addContext(rp, rp);
        rp.setDaemon(true);
        pool.add(rp);
        unused_size++;
        rp.start();
    }

    protected synchronized void addRPPoolManagerListener(RPPoolManagerListener listener)
    {
        pmListener = EventMulticaster.add(pmListener, listener);
    }

    protected synchronized void destroy()
    {
        if (pool == null || inUse == false) return;
        RequestProcessor[] rps = new RequestProcessor[pool.size()];
        pool.toArray(rps);
        for (int i=0; i<rps.length; i++)
        {
            if (rps[i].isActive())
            {
                throw new POAInternalError("error: request processor is active (RequestProcessorPM.destroy)");

            }
            else
            {
                pool.remove(rps[i]);
                unused_size--;
                current._removeContext(rps[i]);
                rps[i].end();
            }
        }
        inUse = false;
    }

    /**
     * returns the number of unused processors contained in the pool
     */

    protected int getPoolCount()
    {
        return (pool == null) ? 0 : pool.size();
    }

    /**
     * returns the size of the processor pool (used and unused processors)
     */

    protected synchronized int getPoolSize()
    {
        return unused_size;
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
        if (!inUse)
        {
            init();
            inUse = true;
        }

        if (pool.size() == 0 && unused_size < max_pool_size)
        {
            addProcessor();
        }

        while (pool.size() == 0)
        {
            try
            {
                wait();
            }
            catch (InterruptedException e)
            {
            }
        }
        RequestProcessor rp = (RequestProcessor) pool.remove( pool.size() - 1 );

        // notify a pool manager listener
        if (pmListener != null)
            pmListener.processorRemovedFromPool(rp, pool.size(), unused_size);
        return rp;
    }

    private void init()
    {
        pool = new ArrayList(max_pool_size);
        for (int i = 0; i < min_pool_size; i++)
        {
            addProcessor();
        }
    }

    /**
     * gives a processor back into the pool if the number of
     * available processors is smaller than min_pool_size,
     * otherwise the processor will terminated
     */

    protected synchronized void releaseProcessor(RequestProcessor rp)
    {
        if (pool.size() < min_pool_size)
        {
            pool.add(rp);
            notifyAll();
        }
        else
        {
            unused_size--;
            current._removeContext(rp);
            rp.end();
        }
        // notify a pool manager listener
        if (pmListener != null)
            pmListener.processorAddedToPool(rp, pool.size(), unused_size);
    }

    protected synchronized void removeRPPoolManagerListener(RPPoolManagerListener listener)
    {
        pmListener = EventMulticaster.remove(pmListener, listener);
    }
}
