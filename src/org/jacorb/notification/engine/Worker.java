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
