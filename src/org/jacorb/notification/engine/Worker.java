/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2002 Gerald Brose
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

import org.apache.log4j.Logger;



/*
 *        JacORB - a free Java ORB
 */

/**
 * Worker.java
 *
 *
 * Created: Thu Nov 14 22:11:11 2002
 *
 * @author <a href="mailto:bendt@inf.fu-berlin.de">Alphonse Bendt</a>
 * @version $Id$
 */

public class Worker extends Thread {

    TaskCoordinator coordinator_;
    Task task_;
    boolean active_ = true;
    Logger logger_ = Logger.getLogger("Worker");

    Worker(TaskCoordinator coordinator) {
	coordinator_ = coordinator;
    }

    public void run() {
	logger_.info("Worker starts");

	while (active_) {
	    synchronized(this) {
		try {
		    try {
			wait();
		    } catch (InterruptedException ie) {}    
		    logger_.debug("worker woke up beginning to work");
		    task_.run();
		} finally {
		    coordinator_.workerDone(this);
		}
	    } 
	}
    }
    
    public Task getTask() {
	return task_;
    }

    public void setTask(Task task) {
	task_ = task;
    }

}// Worker
