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

import EDU.oswego.cs.dl.util.concurrent.ClockDaemon;
import EDU.oswego.cs.dl.util.concurrent.DirectExecutor;
import EDU.oswego.cs.dl.util.concurrent.Executor;
import EDU.oswego.cs.dl.util.concurrent.LinkedQueue;
import EDU.oswego.cs.dl.util.concurrent.PooledExecutor;
import EDU.oswego.cs.dl.util.concurrent.ThreadFactory;
import org.apache.log.Hierarchy;
import org.apache.log.Logger;
import org.jacorb.notification.NotificationEvent;
import org.jacorb.notification.interfaces.TimerEventConsumer;
import org.jacorb.notification.interfaces.TimerEventSupplier;

/**
 *
 *
 * Created: Thu Nov 14 22:07:32 2002
 *
 * @author <a href="mailto:bendt@inf.fu-berlin.de">Alphonse Bendt</a>
 * @version $Id$
 */

public class TaskProcessor {

    private Logger logger_ = 
	Hierarchy.getDefaultHierarchy().getLoggerFor(getClass().getName());


    private TaskErrorHandler nullErrorHandler_ = new TaskErrorHandler() {
	    public void handleTaskError(Task task, Throwable error) {
		logger_.debug("Error in Task: " + task);
		error.printStackTrace();
	    }
	};

    private TaskFinishHandler nullFinishHandler_ = new TaskFinishHandler() {
	    public void handleTaskFinished(Task task) {
		logger_.debug("Task " + task + " finished");
	    }
	};

    private boolean active_;

    private Executor filterPool_;
    private LinkedQueue filterTaskQueue_;

    private Executor deliverPool_;
    private LinkedQueue deliverTaskQueue_;

    private ClockDaemon clockDaemon_;
    private TaskConfigurator taskConfigurator_;

    /**
     * ThreadFactory for the FilterThreadPool. 
     * The ThreadFactory sets Daemon status for the created Threads
     * and assigns a humanreadable name.
     */
    protected ThreadFactory filterThreadFactory_ = new ThreadFactory() {
	    private int counter_ = 0;
	    public synchronized Thread newThread(Runnable task) {
		Thread _t = new Thread(task);
		_t.setDaemon(true);
		_t.setName("FilterThread#" + (counter_++));
		
		return _t;
	    }
	};

    /**
     * ThreadFactory for the DeliverThreadPool. 
     * The ThreadFactory sets Daemon status for the created Threads
     * and assigns a humanreadable name.
     */
    protected ThreadFactory deliverThreadFactory_ = new ThreadFactory() {
	    private int counter_ = 0;
	    public synchronized Thread newThread(Runnable task) {
		Thread _t = new Thread(task);
		_t.setDaemon(true);
		_t.setName("DeliverThread#" + (counter_++));
		
		return _t;
	    }
	};
    
    ////////////////////////////////////////

    /**
     * Start ClockDaemon
     * Set up DeliverThreadPool
     * Set up FilterThreadPool
     * Set up TaskConfigurator
     */ 
    public TaskProcessor() {
	// TODO this should be a user configurable

	boolean _filterThreaded = true;
	boolean _deliverThreaded = false;

	clockDaemon_ = new ClockDaemon();

	PooledExecutor _executor;
	if (_filterThreaded) {
	    filterTaskQueue_ = new LinkedQueue();
	    _executor = new PooledExecutor(filterTaskQueue_);
	    filterPool_ = _executor;
	    _executor.setThreadFactory(filterThreadFactory_);
	    _executor.setKeepAliveTime(-1); // live forever
	    _executor.createThreads(2); // preallocate x threads
	} else {
	    filterPool_ = new DirectExecutor();
	}

	if (_deliverThreaded) {
	    int _initialPoolSize = 4;

	    deliverTaskQueue_ = new LinkedQueue();
	    _executor = new PooledExecutor(deliverTaskQueue_);
	    deliverPool_ = _executor;
	    _executor.setThreadFactory(deliverThreadFactory_);
	    _executor.setKeepAliveTime(-1); // live forever
	    _executor.setMinimumPoolSize(_initialPoolSize);
	    _executor.createThreads(_initialPoolSize); // preallocate x treads
	} else {
	    deliverPool_ = new DirectExecutor();
	}
	
	taskConfigurator_ = new TaskConfigurator(this);
	taskConfigurator_.init();

	active_ = true;
    }    

    ////////////////////////////////////////

    boolean isFilterTaskQueued() {
	return (!filterTaskQueue_.isEmpty());
    }

    boolean isDeliverTaskQueued() {
	return (!deliverTaskQueue_.isEmpty());
    }

    /**
     * shutdown this TaskProcessor. The Threadpools will be shutdown, the
     * running Threads interrupted and all
     * allocated ressources will be freed. As the active Threads will
     * be interrupted pending Events will be discarded.
     */
    private void shutdown() {
	active_ = false;

	if (filterPool_ instanceof PooledExecutor) {
	    ((PooledExecutor) filterPool_).shutdownNow();
	    ((PooledExecutor) filterPool_).interruptAll();
	}

	if (deliverPool_ instanceof PooledExecutor) {
	    ((PooledExecutor) deliverPool_).shutdownNow();
	    ((PooledExecutor) deliverPool_).interruptAll();
	}
	
	clockDaemon_.shutDown();
	logger_.info("shutdown - complete");
    }

    /**
     * begin to process a NotificationEvent
     */
    public void processEvent(NotificationEvent event) {
	FilterTaskBase _task = taskConfigurator_.newFilterIncomingTask(event);
	
	try {
	    scheduleFilterTask(_task);
	} catch (InterruptedException ie) {
	    logger_.error("Interrupt while scheduling FilterTask", ie);
	}
    }

    /**
     * Schedule a FilterTask for execution.
     */
    void scheduleFilterTask(FilterTaskBase task) 
	throws InterruptedException {

	filterPool_.execute(task);
    }

    /**
     * Schedule a FilterTask for execution. Bypass Queuing if
     * possible. If no FilterTasks are queued this 
     * Thread can be used to perform the FilterTask. Otherwise queue
     * FilterTask for execution
     */
    void scheduleOrExecuteFilterTask(FilterTaskBase task) 
	throws InterruptedException {

	if (isFilterTaskQueued()) {
	    scheduleFilterTask(task);
	} else {
	    task.run();
	}
    }

    /**
     * Schedule or Execute PushToConsumerTask for execution. Bypass
     * Scheduling if possible.
     */
    void scheduleOrExecutePushToConsumerTask(PushToConsumerTask task) 
	throws InterruptedException {

	if (isDeliverTaskQueued()) {
	    schedulePushToConsumerTask(task);
	} else {
	    task.run();
	}
    }

    /**
     * Schedule a PushToConsumerTask for execution.
     */
    void schedulePushToConsumerTask(PushToConsumerTask task) 
	throws InterruptedException {

	deliverPool_.execute(task);
    }

    /**
     * Schedule an array of PushToConsumerTask for execution.
     */
    void schedulePushToConsumerTask(PushToConsumerTask[] tasks) 
	throws InterruptedException {

	for (int x = 0; x < tasks.length; ++x) {
	    schedulePushToConsumerTask(tasks[x]);
	}
    }

    /**
     * Schedule ProxyPullConsumer for pull-Operation.
     * If a Supplier connects to a ProxyPullConsumer the
     * ProxyPullConsumer needs to regularely poll the Supplier.
     * This method queues a Task to run runPullEvent on the specified
     * TimerEventSupplier 
     */
    public void scheduleTimedPullTask(TimerEventSupplier dest) 
	throws InterruptedException {

	PullFromSupplierTask _t = new PullFromSupplierTask();

	_t.setTaskFinishHandler(nullFinishHandler_);
	_t.setTaskErrorHandler(nullErrorHandler_);
	_t.setTarget(dest);

	deliverPool_.execute(_t);
    }

    /**
     * Schedule ProxyPushSupplier for push-Operation.
     * A SequenceProxyPushSuppliers need to push Events regularely to its
     * connected Consumer. This method allows to queue a Task to call
     * deliverPendingEvents on the specified TimerEventConsumer
     */
    public void scheduleTimedPushTask(TimerEventConsumer d) 
	throws InterruptedException {

	TimerDeliverTask _task = new TimerDeliverTask();

	_task.setTimedDeliverTarget(d);
	_task.setTaskFinishHandler(nullFinishHandler_);
	_task.setTaskErrorHandler(nullErrorHandler_);

	deliverPool_.execute(_task);
    }

    /**
     * access the Clock Daemon instance.
     */
    private ClockDaemon getClockDaemon() {
	return clockDaemon_;
    }

    public Object registerPeriodicTask(long intervall, 
				       Runnable task, 
				       boolean startImmediately) {

	return getClockDaemon().executePeriodically(intervall, 
						    task, 
						    startImmediately);
    }

    public void unregisterTask(Object id) {
	getClockDaemon().cancel(id);
    }
}
