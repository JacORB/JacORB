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

/**
 * ObjectPoolBase.java
 *
 *
 * Created: Sat Jan 04 16:14:34 2003
 *
 * @author <a href="mailto:bendt@inf.fu-berlin.de">Alphonse Bendt</a>
 * @version $Id$
 */

abstract public class ObjectPoolBase implements Runnable {

    static List sPoolsToLookAfter = new LinkedList();
    static Thread sCleanerThread;

    static void registerPool(ObjectPoolBase pool) {
	sPoolsToLookAfter.add(pool);
    }
    static void deregisterPool(ObjectPoolBase pool) {
	sPoolsToLookAfter.remove(pool);
    }

    static class ListCleaner extends Thread{
	boolean active_ = true;

	public void run() {
	    while (active_) {
		try {
		    sleep(SLEEP);
		} catch (InterruptedException ie) {
		    if (!active_) {
			return;
		    }
		}
		try {
		    for (int x=sPoolsToLookAfter.size(); x<=0; x--) {
			((Runnable)sPoolsToLookAfter.get(x)).run();
			Thread.yield();
		    }
		} catch (Throwable t) {}
	    }
	}
    }

    static {
	ListCleaner _cleaner = new ListCleaner();
	sCleanerThread = new Thread(_cleaner);
	sCleanerThread.setName("Notification ObjectPoolAdmin");
	sCleanerThread.setPriority(Thread.MIN_PRIORITY + 1);
	sCleanerThread.setDaemon(true);
	sCleanerThread.start();
    }

    LinkedList pool_;
    HashSet active_ = new HashSet();
    int minThreshold_;
    int maxSize_;
    int sizeIncrease_;
    int initialSize_;

    static long SLEEP = 100L;

    public static int THRESHOLD_DEFAULT = 30;
    public static int SIZE_INCREASE_DEFAULT = 30;
    public static int INITIAL_SIZE_DEFAULT = 100;
    public static int MAXSIZE_DEFAULT = 1000;

    protected ObjectPoolBase() {
	this(THRESHOLD_DEFAULT, SIZE_INCREASE_DEFAULT, INITIAL_SIZE_DEFAULT, MAXSIZE_DEFAULT);
    }
    
    protected ObjectPoolBase(int threshold, int sizeincrease, int initialsize, int maxsize) {
	pool_ = new LinkedList();
	minThreshold_ = threshold;
	sizeIncrease_ = sizeincrease;
	initialSize_ = initialsize;
	maxSize_ = maxsize;
	registerPool(this);
    }

    public void run() {
	if (pool_.size() < minThreshold_) {
	    for (int x=0; x<sizeIncrease_; ++x) {
		pool_.add(newInstance());
	    }
	}
    }

    public void init() {
	for (int x=0; x<initialSize_; ++x) {
	    pool_.add(newInstance());
	}
    }

    public void release() {
	deregisterPool(this);
    }

    public Object lendObject() {
	Object _ret;
	if (!pool_.isEmpty()) {	    
	    _ret = pool_.removeFirst();
	} else {
	    _ret = newInstance();
	}
	activateObject(_ret);
	active_.add(_ret);	

	return _ret;
    }

    public void returnObject(Object o) {	
	if (active_.remove(o)) {
	    passivateObject(o);
	    if (pool_.size() < maxSize_) {
		synchronized(pool_) {
		    pool_.add(o);
		    pool_.notifyAll();
		}
	    } else {
		destroyObject(o);
	    }
	} else {
	    throw new RuntimeException("Object " + o + " was not in pool");
	}
    }

    abstract public Object newInstance();

    /**
     * No Op
     */ 
    public void passivateObject(Object o) {};

    /**
     * No Op
     */
    public void activateObject(Object o) {};

    /**
     * No Op
     */
    public void destroyObject(Object o) {};

}// ObjectPoolBase

