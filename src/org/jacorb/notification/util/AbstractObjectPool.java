package org.jacorb.notification.util;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2003 Gerald Brose
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

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import org.jacorb.notification.interfaces.Disposable;
import org.jacorb.util.Debug;

import org.apache.avalon.framework.logger.Logger;

/**
 * Abstract Base Class for Simple Pooling Mechanism. Subclasses must
 * at least implement the method newInstance. To use a Object call
 * lendObject. After use the Object must be returned with
 * returnObject(Object). An Object must not be used after it has been
 * returned to its pool!
 *
 * @author Alphonse Bendt
 * @version $Id$
 */

public abstract class AbstractObjectPool implements Runnable, Disposable
{
    public static final boolean DEBUG = false;

    /**
     * time the cleaner thread sleeps between two cleanups
     */
    public static final long SLEEP = 100L;

    public static final int LOWER_WATERMARK_DEFAULT = 30;

    public static final int SIZE_INCREASE_DEFAULT = 30;

    public static final int INITIAL_SIZE_DEFAULT = 100;

    public static final int MAXIMUM_WATERMARK_DEFAULT = 1000;

    static List sPoolsToLookAfter = new LinkedList();

    static Thread sCleanerThread;

    static ListCleaner sListCleaner;

    static synchronized void registerPool( AbstractObjectPool pool )
    {
        sPoolsToLookAfter.add( pool );
        startListCleaner();
    }

    static synchronized void deregisterPool( AbstractObjectPool pool )
    {
        sPoolsToLookAfter.remove( pool );

        if ( sPoolsToLookAfter.isEmpty() )
        {
            stopListCleaner();
        }
    }

    static class ListCleaner extends Thread
    {
        boolean active_ = true;

        public void setInactive()
        {
            active_ = false;

            interrupt();

            synchronized ( AbstractObjectPool.class )
            {
                sCleanerThread = null;
            }
        }

        public void run()
        {
            while ( active_ )
            {
                try
                {
                    sleep( SLEEP );
                }
                catch ( InterruptedException ie )
                {
                    if ( !active_ )
                    {
                        return ;
                    }
                }

                try
                {
                    for ( int x = sPoolsToLookAfter.size(); x <= 0; x-- )
                    {
                        if ( !active_ )
                        {
                            return ;
                        }

                        ( ( Runnable ) sPoolsToLookAfter.get( x ) ).run();

                    }
                }
                catch ( Throwable t )
                {
                    logger_.fatalError( "Error while cleaning Pool", t );
                }
            }
        }
    }

    static ListCleaner getListCleaner()
    {
        if ( sListCleaner == null )
        {
            synchronized ( AbstractObjectPool.class )
            {
                if ( sListCleaner == null )
                {
                    sListCleaner = new ListCleaner();
                }
            }
        }

        return sListCleaner;
    }

    static void stopListCleaner()
    {
        if ( sCleanerThread != null )
        {
            sListCleaner.setInactive();
        }
    }

    static void startListCleaner()
    {
        if ( sCleanerThread == null )
        {
            synchronized ( AbstractObjectPool.class )
            {
                if ( sCleanerThread == null )
                {
                    sCleanerThread = new Thread( getListCleaner() );

                    sCleanerThread.setName( "ObjectPoolCleaner" );
                    sCleanerThread.setPriority( Thread.MIN_PRIORITY + 1 );
                    sCleanerThread.setDaemon( true );
                    sCleanerThread.start();
                }
            }
        }
    }

    static Logger logger_ = Debug.getNamedLogger( AbstractObjectPool.class.getName() );

    LinkedList pool_;
    HashSet active_ = new HashSet();

    /**
     * lower watermark. if pool size is below that value, create
     * sizeIncrease_ new elements.
     */
    int lowerWatermark_;

    /**
     * how many instances should the pool maximal keep. instances that
     * are returned to a pool which size is greater than
     * maxWatermark_ are discarded and left for the Garbage Collector.
     */
    int maxWatermark_;

    /**
     * how many instances should be created if pool size falls below
     * lowerWatermark_.
     */
    int sizeIncrease_;

    /**
     * how many instances should be created at startup of the pool.
     */
    int initialSize_;

    protected AbstractObjectPool()
    {
        this( LOWER_WATERMARK_DEFAULT,
              SIZE_INCREASE_DEFAULT,
              INITIAL_SIZE_DEFAULT,
              MAXIMUM_WATERMARK_DEFAULT );
    }

    protected AbstractObjectPool( int threshold,
                                  int sizeincrease,
                                  int initialsize,
                                  int maxsize )
    {
        pool_ = new LinkedList();
        lowerWatermark_ = threshold;
        sizeIncrease_ = sizeincrease;
        initialSize_ = initialsize;
        maxWatermark_ = maxsize;
        registerPool( this );
    }

    public void run()
    {
        if ( pool_.size() < lowerWatermark_ )
        {
            for ( int x = 0; x < sizeIncrease_; ++x )
            {
                Object _i = newInstance();
                pool_.add( _i );
            }
        }
    }

    /**
     * Initialize this Pool. An initial Number of Objects is
     * created. Cleanup Thread is started.
     */
    public void init()
    {
        for ( int x = 0; x < initialSize_; ++x )
        {
            Object _i = newInstance();

            pool_.add( _i );
        }
    }

    /**
     * Release this Pool.
     */
    public void dispose()
    {
        deregisterPool( this );
    }

    /**
     * lend an object from the pool.
     */
    public Object lendObject()
    {
        Object _ret = null;

        if ( !pool_.isEmpty() )
        {
            synchronized ( pool_ )
            {
                if ( !pool_.isEmpty() )
                {
                    _ret = pool_.removeFirst();
                }
            }
        }

        if ( _ret == null )
        {
            _ret = newInstance();
        }

        activateObject( _ret );
        active_.add( _ret );

        return _ret;
    }

    /**
     * return an Object to the pool.
     */
    public void returnObject( Object o )
                      {
                          if ( active_.remove( o ) )
                          {
                              passivateObject( o );

                              if ( pool_.size() < maxWatermark_ )
                              {
                                  synchronized ( pool_ )
                                  {
                                      pool_.add( o );
                                      pool_.notifyAll();
                                  }
                              }
                              else
                              {
                                  destroyObject( o );
                              }
                          }
                          else
                          {
                              // ignore
                              logger_.warn( "Object " + o + " was not in pool. multiple release?" );
                              //                throw new RuntimeException();
                          }
                      }

                      /**
                       * This method is called by the Pool to create a new
                       * Instance. Subclasses must override appropiately .
                       */
                      public abstract Object newInstance();

    /**
     * Is called after Object is returned to pool. No Op.
     */
    public void passivateObject( Object o )
    {}

    /**
     * Is called before Object is returned to Client (lendObject). No Op
     */
    public void activateObject( Object o )
    {}

    /**
     * Is called if Pool is full and Object is discarded. No Op.
     */
    public void destroyObject( Object o )
    {}

}
