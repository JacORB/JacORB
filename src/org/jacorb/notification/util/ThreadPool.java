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

import EDU.oswego.cs.dl.util.concurrent.DirectExecutor;
import EDU.oswego.cs.dl.util.concurrent.Executor;
import EDU.oswego.cs.dl.util.concurrent.LinkedQueue;
import EDU.oswego.cs.dl.util.concurrent.PooledExecutor;
import EDU.oswego.cs.dl.util.concurrent.ThreadFactory;
import org.apache.log.Hierarchy;
import org.apache.log.Logger;
import org.jacorb.notification.interfaces.Disposable;

/**
 * ThreadPool.java
 *
 *
 * @author Alphonse Bendt
 * @version $Id$
 */

public class ThreadPool implements Executor, Disposable {

    Logger logger_ = 
	Hierarchy.getDefaultHierarchy().getLoggerFor(getClass().getName());

    private Executor executor_;
    private LinkedQueue channel_;
    
    public ThreadPool(final String name, int numberOfThreads) {
	if (numberOfThreads < 0) {
	    throw new IllegalArgumentException();
	} else if (numberOfThreads == 0) {	    
	    executor_ = new DirectExecutor();

	    logger_.info("Created direct Executing ThreadPool: " + name);
	} else {
	    ThreadFactory _threadFactory = new ThreadFactory() {
		    private int counter_ = 0;
		    
		    public synchronized Thread newThread(Runnable task) {
			Thread _thread = new Thread(task);
			
			_thread.setDaemon(true);
			_thread.setName(name + "#" + (counter_++));
			
			return _thread;
		    }
		};
	    
	    channel_ = new LinkedQueue();
	    
	    PooledExecutor _executor = new PooledExecutor(channel_);

	    _executor.setThreadFactory(_threadFactory);
	    _executor.setKeepAliveTime(-1);
	    _executor.createThreads(numberOfThreads);

	    executor_ = _executor;

	    logger_.info("Created ThreadPool " + name + " with Size " + numberOfThreads);
	}
    }
    
    public boolean isTaskQueued() {
	if (channel_ != null) {
	    return !channel_.isEmpty();
	}
	return false;
    }
    
    public void dispose() {
	if (executor_ instanceof PooledExecutor) {
	    ((PooledExecutor)executor_).shutdownNow();
	    ((PooledExecutor)executor_).interruptAll();
	}
    }
    
    public void execute(Runnable r) throws InterruptedException {
	executor_.execute(r);
    }
}
