package org.jacorb.notification.engine;

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

import org.jacorb.notification.NotificationEvent;
import java.util.List;
import java.util.Vector;
import org.apache.log4j.Logger;
import EDU.oswego.cs.dl.util.concurrent.PooledExecutor;
import EDU.oswego.cs.dl.util.concurrent.DirectExecutor;
import org.jacorb.notification.framework.EventDispatcher;
import EDU.oswego.cs.dl.util.concurrent.LinkedQueue;
import java.util.Map;
import java.util.Hashtable;
import EDU.oswego.cs.dl.util.concurrent.ThreadFactory;
import EDU.oswego.cs.dl.util.concurrent.Executor;

/**
 * Engine.java
 *
 *
 * Created: Thu Nov 14 22:07:32 2002
 *
 * @author <a href="mailto:bendt@inf.fu-berlin.de">Alphonse Bendt</a>
 * @version $Id$
 */

public class Engine implements TaskCoordinator {

    boolean active_;
    Executor filterPool_;
    Executor deliverPool_;
    TaskConfigurator taskConfigurator_;

    Logger logger_;
    Logger timeLogger_ = Logger.getLogger("TIME.Engine");

    ThreadFactory filterThreadFactory_ = new ThreadFactory() {
	    int counter_ = 0;
	    synchronized public Thread newThread(Runnable task) {
		Thread _t = new Thread(task);
		_t.setDaemon(true);
		_t.setName("FilterThread#" + (counter_++));

		return _t;
	    }
	};

    ThreadFactory deliverThreadFactory_ = new ThreadFactory() {
	    int counter = 0;
	    synchronized public Thread newThread(Runnable task) {
		Thread _t = new Thread(task);
		_t.setDaemon(true);
		_t.setName("DeliverThread#"+ (counter++));

		return _t;
	    }
	};


    public Engine() {
	logger_ = Logger.getLogger("Engine");

	boolean filterThreaded_ = true;
	boolean deliverThreaded_ = true;

	PooledExecutor _executor;
	if (filterThreaded_) {
	    _executor = new PooledExecutor(new LinkedQueue());
	    filterPool_ = _executor;
	    _executor.setThreadFactory(filterThreadFactory_);
	    _executor.setKeepAliveTime(-1); // live forever
	    _executor.createThreads(2); // preallocate x threads
	} else {
	    filterPool_ = new DirectExecutor();
	}

	if (deliverThreaded_) {
	    _executor = new PooledExecutor(new LinkedQueue());
	    deliverPool_ = _executor;
	    _executor.setThreadFactory(deliverThreadFactory_);
	    _executor.setKeepAliveTime(-1); // live forever
	    _executor.createThreads(4); // preallocate x treads
	} else {
	    deliverPool_ = new DirectExecutor();
	}
	
	taskConfigurator_ = new TaskConfigurator(this);
	taskConfigurator_.init();

	active_ = true;
    }    

    public void shutdown() {
	active_ = false;

	logger_.info("shutdown()");
	if (filterPool_ instanceof PooledExecutor) {
	    ((PooledExecutor)filterPool_).shutdownNow();
	    ((PooledExecutor)filterPool_).interruptAll();
	}
	if (deliverPool_ instanceof PooledExecutor) {
	    ((PooledExecutor)deliverPool_).shutdownNow();
	    ((PooledExecutor)deliverPool_).interruptAll();
	}

	logger_.info("shutdown - complete");
    }

    public void dispatchEvent(NotificationEvent event) {
	long _time = System.currentTimeMillis();

	FilterTask _task = taskConfigurator_.initTask(event);
	
	try {
	    queueFilterTask(_task);
	} catch (InterruptedException ie) {
	    logger_.fatal(ie);
	}

	timeLogger_.info("dispatchEvent(): " + (System.currentTimeMillis() - _time));
    }

    public void queueFilterTask(FilterTask task) throws InterruptedException {
	long _start = System.currentTimeMillis();
	filterPool_.execute(task);
	timeLogger_.info("queueFilterTask: " + (System.currentTimeMillis() - _start));
    }

    public void queueDeliverTask(DeliverTask task) throws InterruptedException {	
	task.queue();
	deliverPool_.execute(task);
    }


    private void queueTask2(Task task) {
	if (!active_)
	    return;

	long _start = System.currentTimeMillis();

	try {
	    if (task instanceof DeliverTask) {
		deliverPool_.execute(task);
	    } else if (task instanceof FilterTask) {
		filterPool_.execute(task);
	    }
	} catch (InterruptedException ie) {}
	long _stop = System.currentTimeMillis();

	timeLogger_.info("queueTask: " + (_stop - _start));
    }
    
    public void queueDeliverTask(DeliverTask[] tasks) throws InterruptedException {
	for (int x=0; x<tasks.length; ++x) {
	    queueDeliverTask(tasks[x]);
	}
    }

    public void workDone(Task task) {
	try {
	    taskConfigurator_.updateTask(task);
	} catch (InterruptedException ie) {
	    ie.printStackTrace();
	}
    }

    public void handleError(Task task, Throwable t) {
	t.printStackTrace();

	System.out.println("reschedule task");

	if (task instanceof DeliverTask) {
	    DeliverTask _deliverTask = (DeliverTask)task;
	    _deliverTask.destination_.markError();
	}
    }
}// Engine

