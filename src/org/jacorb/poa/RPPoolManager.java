package org.jacorb.poa;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2002  Gerald Brose.
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
  
import java.util.Vector;

/**
 * This class provides and manages a pool of ready started threads for
 * request processing.
 *
 * @author Gerald Brose, Reimo Tiedemann, FU Berlin
 * @version 1.04, 10/26/99, RT
 * @see		jacorb.poa.RequestProcessor
 */

public class RPPoolManager 
{
    private RPPoolManagerListener pmListener;

    // the current for (un)registering the invocation contexts
    private Current current;
    // pool management stuff
    private Vector pool;
    private int pool_size;
    private int max_pool_size;
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
        throws IllegalAccessException, InstantiationException 
    {
        RequestProcessor rp = new RequestProcessor(this);
        current._addContext(rp, rp);
        rp.setDaemon(true);
        pool.addElement(rp);
        pool_size++;
        rp.start();
    }

    protected synchronized void addRPPoolManagerListener(RPPoolManagerListener listener) 
    {
        pmListener = EventMulticaster.add(pmListener, listener);
    }

    synchronized protected void destroy() 
    {
        if (pool == null || inUse == false) return;
        RequestProcessor[] rps = new RequestProcessor[pool.size()];
        pool.copyInto(rps);
        for (int i=0; i<rps.length; i++) 
        {
            if (rps[i].isActive()) 
            {
                throw new POAInternalError("error: request processor is active (RequestProcessorPM.destroy)");
				
            }
            else 
            {
                pool.removeElement(rps[i]);
                pool_size--;
				current._removeContext(rps[i]);
                rps[i].stop();
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

    protected int getPoolSize() 
    {
        return pool_size;
    }

    /**
     * returns a processor from pool, the first call causes
     * the initialization of the processor pool,
     * if no processor available the number of processors
     * will increased until the max_pool_size is reached,
     * this method blocks if no processor available and the 
     * max_pool_size is reached until a processor will released
     */

    synchronized protected RequestProcessor getProcessor() 
    {
        if (!inUse) 
        {
            init();
            inUse = true;
        }

        if (pool.size() == 0 && pool_size < max_pool_size) 
        {
            try 
            {
                addProcessor();
            } 
            catch (Exception e) 
            {
                e.printStackTrace();
            }
        }

        while (pool.size() == 0) 
        {
            try 
            {
                wait();
            } 
            catch (Exception e) 
            {
                e.printStackTrace();
            }
        }
        RequestProcessor rp = (RequestProcessor) pool.lastElement();
        pool.removeElement(rp);

        // notify a pool manager listener		
        if (pmListener != null) 
            pmListener.processorRemovedFromPool(rp, pool.size(), pool_size);
        return rp;
    }

    private void init() 
    {
        pool = new Vector(max_pool_size);
        for (int i = 0; i < min_pool_size; i++) 
        {
            try 
            {
                addProcessor();
            } 
            catch (Exception e) 
            {
                e.printStackTrace();
            }
        }
    }

    /**
     * gives a processor back into the pool if the number of
     * available processors is smaller than min_pool_size,
     * otherwise the processor will terminated
     */

    synchronized protected void releaseProcessor(RequestProcessor rp) 
    {
        if (pool.size() < min_pool_size) 
        {
            pool.addElement(rp);
            notifyAll();
        }
        else 
        {
            pool_size--;
            current._removeContext(rp);
            rp.end();		
        }
        // notify a pool manager listener		
        if (pmListener != null) 
            pmListener.processorAddedToPool(rp, pool.size(), pool_size);		
    }

    protected synchronized void removeRPPoolManagerListener(RPPoolManagerListener listener) 
    {
        pmListener = EventMulticaster.remove(pmListener, listener);
    }

    /**
     * resets the values for min_pool_size and max_pool_size
     */

    synchronized protected void setPoolSize(int min, int max) 
    {
        min_pool_size = min;
        max_pool_size = max;
        while (pool_size < min_pool_size) 
        {
            try 
            {
                addProcessor();
            } 
            catch (Exception e) 
            {
                e.printStackTrace();
            }
        }
    }
}







