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

import java.util.LinkedList;
import java.util.HashSet;
import java.util.List;
import java.util.Vector;
import java.util.Iterator;
import org.apache.log.Logger;
import org.apache.log.Hierarchy;

/**
 * Abstract Base Class for Simple Pooling Mechanism. Subclasses must
 * at least implement the method newInstance. To use a Object call
 * lendObject. After use the Object must be returned with
 * returnObject(Object). An Object must not be used after it has been
 * returned to its pool!
 *
 *
 * @author Alphonse Bendt
 * @version $Id$
 */

abstract public class ObjectPoolBase implements Runnable
{
    public final static boolean DEBUG = true;

    public final static long SLEEP = 100L;
    public final static int THRESHOLD_DEFAULT = 30;
    public final static int SIZE_INCREASE_DEFAULT = 30;
    public final static int INITIAL_SIZE_DEFAULT = 100;
    public final static int MAXSIZE_DEFAULT = 1000;

    static List sPoolsToLookAfter = new LinkedList();
    static Thread sCleanerThread;

    static void registerPool( ObjectPoolBase pool )
    {
        sPoolsToLookAfter.add( pool );
    }

    static void deregisterPool( ObjectPoolBase pool )
    {
        sPoolsToLookAfter.remove( pool );
    }

    static class ListCleaner extends Thread
    {
        boolean active_ = true;

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
                        ( ( Runnable ) sPoolsToLookAfter.get( x ) ).run();
                        Thread.yield();
                    }
                }
                catch ( Throwable t )
                {}
            }
        }
    }

    static {
        ListCleaner _cleaner = new ListCleaner();
        sCleanerThread = new Thread( _cleaner );
        sCleanerThread.setName( "Notification ObjectPoolAdmin" );
        sCleanerThread.setPriority( Thread.MIN_PRIORITY + 1 );
        sCleanerThread.setDaemon( true );
        sCleanerThread.start();
    }

    LinkedList pool_;
    HashSet active_ = new HashSet();

    HashSet createdHere_ = new HashSet();

    Logger logger_ = Hierarchy.getDefaultHierarchy().getLoggerFor(getClass().getName());
    int minThreshold_;
    int maxSize_;
    int sizeIncrease_;
    int initialSize_;

    protected ObjectPoolBase()
    {
        this( THRESHOLD_DEFAULT,
              SIZE_INCREASE_DEFAULT,
              INITIAL_SIZE_DEFAULT,
              MAXSIZE_DEFAULT );
    }

    protected ObjectPoolBase( int threshold,
                              int sizeincrease,
                              int initialsize,
                              int maxsize )
    {
        pool_ = new LinkedList();
        minThreshold_ = threshold;
        sizeIncrease_ = sizeincrease;
        initialSize_ = initialsize;
        maxSize_ = maxsize;
        registerPool( this );
    }

    public void run()
    {
        if ( pool_.size() < minThreshold_ )
        {
            for ( int x = 0; x < sizeIncrease_; ++x )
            {
		Object _i = newInstance();
		pool_.add( _i );

		if (DEBUG) {
		    createdHere_.add( _i );
		}

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
	    createdHere_.add( _i );
        }
    }

    /**
     * Release this Pool.
     */
    public void release()
    {
        deregisterPool( this );
    }

    /**
     * lend an object from the pool.
     */
    public Object lendObject()
    {
        Object _ret = null;

        if ( !pool_.isEmpty() ) {
	    synchronized( pool_ ) {
		if ( !pool_.isEmpty() ) 
		    {
			_ret = pool_.removeFirst();
		    }
	    }
	}

	if ( _ret == null )
        {
            _ret = newInstance();

	    if (DEBUG) {
		createdHere_.add(_ret);
	    }
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
	if (DEBUG && !createdHere_.contains(o)) {
	    logger_.fatalError("Object " + o + " was not created here");
	}

	if ( active_.remove( o ) )
	    {
		passivateObject( o );
		
		if ( pool_.size() < maxSize_ )
		    {
			synchronized ( pool_ )
			    {
				pool_.add( o );
				pool_.notifyAll();
			    }
		    }
		else
		    {
			if (DEBUG) {
			    createdHere_.remove(o);
			}

			destroyObject( o );
		    }
	    }
	else
	    {
		// ignore
		logger_.warn("Object " + o + " was not in pool. multiple release?" );
		//		throw new RuntimeException();
	    }
    }
    
    /**
     * This method is called by the Pool to create a new
     * Instance. Subclasses must override appropiately .
     */
    abstract public Object newInstance();
    
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

} // ObjectPoolBase

