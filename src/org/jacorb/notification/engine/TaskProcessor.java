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

import java.util.Date;

import org.apache.log.Hierarchy;
import org.apache.log.Logger;
import org.jacorb.notification.NotificationEvent;
import org.jacorb.notification.Properties;
import org.jacorb.notification.interfaces.Disposable;
import org.jacorb.notification.interfaces.TimerEventConsumer;
import org.jacorb.notification.interfaces.TimerEventSupplier;
import org.jacorb.notification.util.ThreadPool;
import org.jacorb.util.Environment;
import org.omg.CORBA.Any;
import org.omg.CosNotification.StructuredEvent;

import EDU.oswego.cs.dl.util.concurrent.ClockDaemon;

/**
 *
 * @author Alphonse Bendt
 * @version $Id$
 */

public class TaskProcessor implements Disposable {

    final static int DEFAULT_FILTER_POOL_WORKERS = 2;
    final static int DEFAULT_DELIVER_POOL_WORKERS = 4;

    private Logger logger_ = 
	Hierarchy.getDefaultHierarchy().getLoggerFor(getClass().getName());

    class TimeoutTask 
	implements Runnable, 
		   NotificationEvent.NotificationEventStateListener {
	
	Object timerRegistration_;
	NotificationEvent event_;

	TimeoutTask(NotificationEvent event) {
	    event_ = event;
	    
	    event_.setNotificationEventStateListener(this);

	    timerRegistration_ = executeTaskAfterDelay(event.getTimeout(), this);
	}

	public void actionLifetimeChanged(long timeout) {
	    cancelTask(timerRegistration_);
	    timerRegistration_ = executeTaskAfterDelay(event_.getTimeout(), this);
	}

	public void run() {
	    event_.setDisposable();

	    event_.setNotificationEventStateListener(null);
	}
    }

    class DeferedStopTask implements Runnable {
	Object timerRegistration_;
	NotificationEvent event_;
	
	DeferedStopTask(NotificationEvent event) {
	    event_ = event;

	    timerRegistration_ = executeTaskAt(event.getStopTime(), this);
	}
	
	public void run() {
	    event_.setDisposable();
	}
    }

    class DeferedStartTask implements Runnable {
	
	Object timerRegistration_;
	NotificationEvent event_;
	
	DeferedStartTask(NotificationEvent event) {
	    event_ = event;
	    timerRegistration_ = executeTaskAt(event.getStartTime(), this);
	}

	public void run() {
	    processEventInternal(event_);
	}
    }

    private TaskErrorHandler nullErrorHandler_ = new TaskErrorHandler() {
	    public void handleTaskError(Task task, Throwable error) {
		logger_.error("Error in Task: " + task, error);
	    }
	};

    private TaskFinishHandler nullFinishHandler_ = new TaskFinishHandler() {
	    public void handleTaskFinished(Task task) {
		logger_.debug("Task " + task + " finished");
	    }
	};

    private ThreadPool filterPool_;
    private ThreadPool deliverPool_;

    private ClockDaemon clockDaemon_;
    private TaskConfigurator taskConfigurator_;

    
    ////////////////////////////////////////

    /**
     * Start ClockDaemon
     * Set up DeliverThreadPool
     * Set up FilterThreadPool
     * Set up TaskConfigurator
     */ 
    public TaskProcessor() {
	logger_.info("create TaskProcessor");

	clockDaemon_ = new ClockDaemon();
	
	filterPool_ = 
	    new ThreadPool("FilterThread", 
			   getNumberFromProperty(Properties.FILTER_POOL_WORKERS, 
						 DEFAULT_FILTER_POOL_WORKERS));

	deliverPool_ = 
	    new ThreadPool("DeliverThread", 
			   getNumberFromProperty(Properties.DELIVER_POOL_WORKERS,
						 DEFAULT_DELIVER_POOL_WORKERS));
	
	taskConfigurator_ = new TaskConfigurator(this);
	taskConfigurator_.init();
    }

    ////////////////////////////////////////

    private int getNumberFromProperty(String propertyName, int defaultValue) {
	if ( Environment.getProperty(propertyName) == null) {
	    return defaultValue;
	}

	try {
	    return Integer.parseInt(Environment.getProperty(propertyName));
	} catch (NumberFormatException e) {
	    return defaultValue;
	}
    }

    boolean isFilterTaskQueued() {
	return (filterPool_.isTaskQueued());
    }

    boolean isDeliverTaskQueued() {
	return (deliverPool_.isTaskQueued());
    }

    /**
     * shutdown this TaskProcessor. The Threadpools will be shutdown, the
     * running Threads interrupted and all
     * allocated ressources will be freed. As the active Threads will
     * be interrupted pending Events will be discarded.
     */
    public void dispose() {
	logger_.info("dispose");

	clockDaemon_.shutDown();
	filterPool_.dispose();
	deliverPool_.dispose();
	taskConfigurator_.dispose();

	logger_.debug("dispose - complete");
    }

    /**
     * begin to process a NotificationEvent
     */
    public void processEvent(NotificationEvent event) {
	if (event.hasTimeout()) {
	    new TimeoutTask(event);
	}

	if (event.hasStopTime()) {

	    if (event.getStopTime().getTime() <= System.currentTimeMillis()) {

		fireEventDiscarded(event);

		return;
	    } else {
		new DeferedStopTask(event);
	    }
	}

	if (event.hasStartTime()) {
	    new DeferedStartTask(event);
	} else {
	    processEventInternal(event);
	}
    }


    /**
     * begin to process a NotificationEvent
     */
    public void processEventInternal(NotificationEvent event) {
	logger_.debug("processEvent");

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

	logger_.debug("scheduleFilterTask");

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

	logger_.debug("schedulePushToConsumerTask");

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

	PullFromSupplierTask _task = new PullFromSupplierTask();

	_task.setTaskFinishHandler(nullFinishHandler_);
	_task.setTaskErrorHandler(nullErrorHandler_);
	_task.setTarget(dest);

	deliverPool_.execute(_task);
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

    public Object executeTaskPeriodically(long intervall, 
					  Runnable task, 
					  boolean startImmediately) {
	
	return getClockDaemon().executePeriodically(intervall, 
						    task, 
						    startImmediately);
    }

    public void cancelTask(Object id) {
	ClockDaemon.cancel(id);
    }

    private Object executeTaskAfterDelay(long delay, Runnable task) {
	return clockDaemon_.executeAfterDelay(delay, task);
    }

    private Object executeTaskAt(Date startTime, Runnable task) {
	return clockDaemon_.executeAt(startTime, task);
    }

    void fireEventDiscarded(NotificationEvent event) {
	switch(event.getType()) {
	case NotificationEvent.TYPE_ANY:
	    fireEventDiscarded(event.toAny());
	    break;
	case NotificationEvent.TYPE_STRUCTURED:
	    fireEventDiscarded(event.toStructuredEvent());
	    break;
	default:
	    throw new RuntimeException();
	}
    }

    void fireEventDiscarded(Any a) {
    }

    void fireEventDiscarded(StructuredEvent e) {
    }

}

