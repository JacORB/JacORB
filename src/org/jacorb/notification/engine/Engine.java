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
package org.jacorb.notification.engine;

import org.jacorb.notification.NotificationEvent;
import java.util.List;
import java.util.Vector;
import org.apache.log4j.Logger;
import EDU.oswego.cs.dl.util.concurrent.PooledExecutor;



/*
 *        JacORB - a free Java ORB
 */

/**
 * Engine.java
 *
 *
 * Created: Thu Nov 14 22:07:32 2002
 *
 * @author <a href="mailto:bendt@inf.fu-berlin.de">Alphonse Bendt</a>
 * @version $Id$
 */

public class Engine {

    Logger logger_;
    boolean active_;

    public Engine() {
	logger_ = Logger.getLogger("Engine");

	taskCoordinator_ = new TaskCoordinator(this);
	taskConfigurator_ = new TaskConfigurator(taskCoordinator_);
	taskQueue_ = new Vector();

	active_ = true;

	taskCoordinator_.start();
    }
    
    TaskConfigurator taskConfigurator_;
    TaskCoordinator taskCoordinator_;

    private List taskQueue_;

    public void shutdown() {
	logger_.info("shutdown()");
	taskCoordinator_.shutdown();
	active_ = false;
	synchronized(taskQueue_) {
	    taskQueue_.notifyAll();
	}
	logger_.info("shutdown - complete");
    }

    public void enterEvent(NotificationEvent event) {
	logger_.info("enterEvent");

	Task _task = taskConfigurator_.initTask(event);
	synchronized(taskQueue_) {
	    taskQueue_.add(_task);
	    taskQueue_.notifyAll();
	    logger_.debug("added event to queue and notified thread");
	}
    }

    public Task checkoutTask() throws InterruptedException {
	logger_.info("checkoutTask()");

	Task _t;
	synchronized(taskQueue_) {
	    while(active_ && taskQueue_.isEmpty()) {
		try {
		    taskQueue_.wait();
		} catch (InterruptedException io) {}
	    }

	    if (!active_) {
		throw new InterruptedException();
	    }

	    _t = (Task)taskQueue_.get(0);
	    taskQueue_.remove(0);

	    logger_.debug("Task checked out from queue");
	    return _t;
	}
    }

    public void returnTask(Task task) {
	logger_.info("returnTask()");

	task = taskConfigurator_.updateTask(task);
	if (task != null) {
	    synchronized(taskQueue_) {
		taskQueue_.add(task);
		taskQueue_.notify();
	    }
	} else {
	    logger_.info("DROP Task");
	}
    }
}// Engine

