package org.jacorb.notification.util;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2004 Gerald Brose
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
 *
 */

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.logger.Logger;
import org.jacorb.notification.interfaces.Disposable;

import edu.emory.mathcs.backport.java.util.concurrent.atomic.AtomicBoolean;

/**
 * Abstract Base Class for Simple Pooling Mechanism. Subclasses must at least implement the method
 * newInstance. To use a Object call lendObject. After use the Object must be returned with
 * returnObject(Object). An Object must not be used after it has been returned to its pool!
 *
 * This class needs a two phase initialization: configure MUST be invoked before an instance can be used.
 *
 * @author Alphonse Bendt
 * @version $Id$
 */

public abstract class AbstractObjectPool implements Runnable, Configurable
{
    public static final boolean DEBUG = false;

    /**
     * time the cleaner thread sleeps between two cleanups
     */
    public static final long SLEEP = 5000L;

    public static final int LOWER_WATERMARK_DEFAULT = 5;

    public static final int SIZE_INCREASE_DEFAULT = 3;

    public static final int INITIAL_SIZE_DEFAULT = 10;

    public static final int MAXIMUM_WATERMARK_DEFAULT = 20;

    public static final int MAXIMUM_SIZE_DEFAULT = 0;

    /**
     * non synchronized as all accessing methods are synchronized.
     */
    private static final List sPoolsToLookAfter = new ArrayList();

    private static AbstractObjectPool[] asArray;

    private static boolean modified = true;

    private final static AbstractObjectPool[] ARRAY_TEMPLATE = new AbstractObjectPool[0];

    private static Thread sCleanerThread;

    private static final Logger sLogger_ = LogUtil.getLogger(AbstractObjectPool.class.getName());

    private static ListCleaner sListCleaner;

    private static boolean sUseListCleaner = true;

    private static AbstractObjectPool[] getAllPools()
    {
        synchronized (sPoolsToLookAfter)
        {
            if (modified)
            {
                asArray = (AbstractObjectPool[]) sPoolsToLookAfter.toArray(ARRAY_TEMPLATE);
                modified = false;
            }
        }
        return asArray;
    }

    private static void registerPool(AbstractObjectPool pool)
    {
        synchronized (sPoolsToLookAfter)
        {
            sPoolsToLookAfter.add(pool);
            modified = true;
            startListCleaner();
        }
    }

    private static void deregisterPool(AbstractObjectPool pool)
    {
        synchronized (sPoolsToLookAfter)
        {
            sPoolsToLookAfter.remove(pool);
            modified = true;
            if (sPoolsToLookAfter.isEmpty())
            {
                // this cleans up the asArray_ array for the GC.
                getAllPools();

                stopListCleaner();
            }
        }
    }

    private static class ListCleaner extends Thread
    {
        private AtomicBoolean active_ = new AtomicBoolean(true);

        public void setInactive()
        {
            active_.set(false);

            interrupt();
        }

        private void ensureIsActive() throws InterruptedException
        {
            if (!active_.get())
            {
                throw new InterruptedException();
            }
        }

        public void run()
        {
            try
            {
                while (active_.get())
                {
                    try
                    {
                        runLoop();
                    } catch (InterruptedException e)
                    {
                        sLogger_.info("PoolCleaner was interrupted");
                    } catch (Exception e)
                    {
                        sLogger_.error("Error cleaning Pool", e);
                    }
                }
            } finally
            {
                synchronized (AbstractObjectPool.class)
                {
                    sCleanerThread = null;
                }
            }
        }

        private void runLoop() throws InterruptedException
        {
            while (true)
            {
                try
                {
                    sleep(SLEEP);
                } catch (InterruptedException ie)
                {
                    // ignore here.
                    // ensureIsActive is called below to see if this Thread should
                    // still be active.
                }

                ensureIsActive();

                Runnable[] poolsToCheck = getAllPools();

                for (int x = 0; x < poolsToCheck.length; ++x)
                {
                    try
                    {
                        poolsToCheck[x].run();
                    } catch (Exception t)
                    {
                        // should not happen
                        sLogger_.error("Error cleaning up Pool", t);
                    }
                }
            }
        }
    }

    private static ListCleaner getListCleaner()
    {
        synchronized (AbstractObjectPool.class)
        {
            if (sListCleaner == null)
            {
                sListCleaner = new ListCleaner();
            }
            return sListCleaner;
        }
    }

    private static void stopListCleaner()
    {
        synchronized (AbstractObjectPool.class)
        {
            if (sCleanerThread != null)
            {
                sListCleaner.setInactive();
            }
        }
    }

    private static void startListCleaner()
    {
        synchronized (AbstractObjectPool.class)
        {
            if (sCleanerThread == null && sUseListCleaner )
            {
                sCleanerThread = new Thread(getListCleaner());

                sCleanerThread.setName("ObjectPoolCleaner");
                sCleanerThread.setPriority(Thread.MIN_PRIORITY + 1);
                sCleanerThread.setDaemon(true);
                sCleanerThread.start();
            }
        }
    }

    private final String name_;

    private final LinkedList pool_;

    private boolean isInitialized_;

    /**
     * Set that contains all objects that were created by this pool and are in use. Problems occured
     * as access to this member used to be non-synchronized see
     * news://news.gmane.org:119/200406041629.48096.Farrell_John_W@cat.com
     */
    private final Set active_ = Collections.synchronizedSet(new WeakHashSet());

    /**
     * lower watermark. if pool size is below that value, create sizeIncrease_ new elements.
     */
    private int lowerWatermark_;

    /**
     * how many instances should the pool maximal keep. instances that are returned to a pool which
     * size is greater than maxWatermark_ are discarded and left for the Garbage Collector.
     */
    private int maxWatermark_;

    /**
     * how many instances should be created if pool size falls below lowerWatermark_.
     */
    private int sizeIncrease_;

    /**
     * how many instances should be created at startup of the pool.
     */
    private int initialSize_;

    private int maximumSize_;

    protected final Logger logger_ = LogUtil.getLogger(getClass().getName());

    protected Configuration config_;

    public void configure(Configuration conf)
    {
        config_ = conf;

        init();
    }

    protected AbstractObjectPool(String name)
    {
        this(name, LOWER_WATERMARK_DEFAULT, SIZE_INCREASE_DEFAULT, INITIAL_SIZE_DEFAULT,
                MAXIMUM_WATERMARK_DEFAULT, MAXIMUM_SIZE_DEFAULT);
    }

    protected AbstractObjectPool(String name, int lowerWatermark, int sizeincrease,
            int initialsize, int maxWatermark, int maximumSize)
    {
        if (maximumSize > 0 && initialsize > maximumSize)
        {
            throw new IllegalArgumentException("InitialSize: " + initialsize
                    + " may not be larger than MaximumSize: " + maximumSize);
        }

        name_ = name;
        pool_ = new LinkedList();
        lowerWatermark_ = lowerWatermark;
        sizeIncrease_ = sizeincrease;
        initialSize_ = initialsize;
        maxWatermark_ = maxWatermark;
        maximumSize_ = maximumSize;
    }

    public void run()
    {
        final int maxToBeCreated;

        synchronized (pool_)
        {
            if (pool_.size() > lowerWatermark_)
            {
                return;
            }

            maxToBeCreated = getNumberOfCreationsAllowed();
        }

        final int sizeIncrease = Math.min(sizeIncrease_, maxToBeCreated);

        if (sizeIncrease > 0)
        {
            List os = new ArrayList(sizeIncrease);

            for (int x = 0; x < sizeIncrease; ++x)
            {
                Object _i = createInstance();

                os.add(_i);
            }

            synchronized (pool_)
            {
                pool_.addAll(os);
            }
        }
    }

    /**
     * check the number of instances that are allowed to be created.
     *
     * <b>preCondition:</b> lock pool_ must be held.
     */
    private int getNumberOfCreationsAllowed()
    {
        final int maxToBeCreated;

        if (maximumSize_ > 0)
        {
            maxToBeCreated = maximumSize_ - active_.size() - pool_.size();
        }
        else
        {
            maxToBeCreated = Integer.MAX_VALUE;
        }

        return maxToBeCreated;
    }

    private Object createInstance()
    {
        if (logger_.isDebugEnabled())
        {
            logger_.debug("created newInstance " + getInfo());
        }
        return newInstance();
    }

    /**
     * Initialize this Pool. An initial Number of Objects is created. Cleanup Thread is started.
     */
    private void init()
    {
        registerPool(this);

        synchronized (pool_)
        {
            if (isInitialized_)
            {
                throw new IllegalStateException("Already Initialized");
            }

            for (int x = 0; x < initialSize_; ++x)
            {
                Object _i = createInstance();

                pool_.add(_i);
            }

            isInitialized_ = true;
        }
    }

    /**
     * Release this Pool.
     */
    public void dispose()
    {
        deregisterPool(this);
        disposeCollection(pool_);
        pool_.clear();
        disposeCollection(active_);
        active_.clear();
    }

    private void disposeCollection(Collection collection)
    {
        final Iterator i = collection.iterator();

        while (i.hasNext())
        {
            final Object o = i.next();

            try
            {
                Disposable disposable = (Disposable) o;

                try
                {
                    ((AbstractPoolable) o).setObjectPool(null);
                } catch (ClassCastException e)
                {
                    // ignored
                }

                disposable.dispose();
            } catch (ClassCastException e)
            {
                // ignored
            }
        }
    }

    /**
     * lend an object from the pool.
     */
    public Object lendObject()
    {
        checkIsInitialized();

        Object _result = null;

        synchronized (pool_)
        {
            if (!pool_.isEmpty())
            {
                _result = pool_.removeFirst();
            }

            if (_result == null)
            {
                while (!isCreationAllowed())
                {
                    poolIsEmpty();
                }
            }
        }

        if (_result == null)
        {
            _result = createInstance();
        }

        try
        {
            ((Configurable) _result).configure(this.config_);
        } catch (ClassCastException cce)
        {
            // no worries, just don't configure
        } catch (ConfigurationException ce)
        {
            throw new RuntimeException("Could not configure instance");
        }

        doActivateObject(_result);
        active_.add(_result);

        return _result;
    }

    /**
     *
     */
    private void checkIsInitialized()
    {
        synchronized (pool_)
        {
            if (!isInitialized_)
            {
                throw new IllegalStateException("Not initialized");
            }
        }
    }

    /**
     * check if it is allowed to create more instances.
     *
     * <b>preCondition:</b> lock pool_ must be held.
     */
    protected boolean isCreationAllowed()
    {
        return getNumberOfCreationsAllowed() > 0;
    }

    /**
     *
     */
    protected void poolIsEmpty()
    {
        throw new RuntimeException(getInfo() + ": No more Elements allowed. ");
    }

    /**
     * return an Object to the pool.
     */
    public void returnObject(Object o)
    {
        checkIsInitialized();

        if (active_.remove(o))
        {
            doPassivateObject(o);

            if (pool_.size() < maxWatermark_)
            {
                synchronized (pool_)
                {
                    pool_.add(o);
                    pool_.notifyAll();
                }
            }
            else
            {
                doDestroyObject(o);
            }
        }
        else
        {
            throw new IllegalArgumentException("Object " + o + " was not created by this pool");
        }
    }

    public String toString()
    {
        return getInfo();
    }

    private String getInfo()
    {
        return "[" + name_ + "] Active=" + active_.size() + " Pooled=" + pool_.size() + " MaximumSize="
                + ((maximumSize_ > 0) ? Integer.toString(maximumSize_) : "unlimited");
    }

    /**
     * This method is called by the Pool to create a new Instance. Subclasses must override
     * appropiately .
     */
    public abstract Object newInstance();

    /**
     * Is called after Object is returned to pool. No Op.
     */
    public void doPassivateObject(Object o)
    {
        // No Op
    }

    /**
     * Is called before Object is returned to Client (lendObject). No Op
     */
    public void doActivateObject(Object o)
    {
        // No Op
    }

    /**
     * Is called if Pool is full and returned Object is discarded. No Op.
     */
    public void doDestroyObject(Object o)
    {
        // No Op
    }
}