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

import EDU.oswego.cs.dl.util.concurrent.PooledExecutor;
import org.apache.log4j.Logger;

class TaskCoordinator extends Thread {

    PooledExecutor pool = new PooledExecutor(1);
    Engine engine_;
    Logger logger_;
    
    volatile boolean active_;

    void shutdown() {
	active_ = false;
	pool.shutdownNow();
    }

    TaskCoordinator(Engine engine) {
	logger_ = Logger.getLogger("TaskCoordinator");

	logger_.info("init TaskCoordinator");
	engine_ = engine;
	active_ = true;
    }

    public void run() {
	logger_.info("taskcoordinator starts");
	while(active_) {
	    try {
		Task _t = engine_.checkoutTask();

		logger_.info(_t);

		try {
		    logger_.info("schedule task for execution");
		    _t.run();
		    //		    pool.execute(_t);
// 		} catch (InterruptedException e) {
// 		    logger_.warn("Task execution interrupted");
// 		    logger_.warn(e);
		} finally {
		    workDone(_t);
		}
	    } catch (InterruptedException e2) {
		logger_.warn("Task checkout interrupted");
	    }
	}
	logger_.info("taskcoordinator shutting down");
    }

    public void workDone(Task task) {
	logger_.info("workDone()");

	engine_.returnTask(task);
    }
}
