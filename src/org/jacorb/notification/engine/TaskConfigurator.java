package org.jacorb.notification.engine;

import org.jacorb.notification.EventChannelImpl;
import org.jacorb.notification.NotificationEvent;
import org.apache.log4j.Logger;
import java.util.List;



/*
 *        JacORB - a free Java ORB
 */

/**
 * TaskConfigurator.java
 *
 *
 * Created: Thu Nov 14 21:08:24 2002
 *
 * @author <a href="mailto:bendt@inf.fu-berlin.de">Alphonse Bendt</a>
 * @version $Id$
 */

public class TaskConfigurator {
    Logger logger_ = Logger.getLogger("ENGINE.TaskConfigurator");

    Destination[] ARRAY_TEMPLATE = new Destination[0];
    TaskCoordinator coordinator_;

    public TaskConfigurator(TaskCoordinator coordinator) {
	coordinator_ = coordinator;
    }
    
    FilterTask initTask(NotificationEvent event) {
	logger_.info("initTask()");

	FilterTask task = new FilterTask();
	
	task.configureEvent(event);
	Destination[] _d = new Destination[] {event.hops_[0]};

	logger_.debug("configure task with " + _d.length + " Destinations");

	task.configureDestinations(_d);
	task.setStatus(Task.NEW);

	return task;
    }

    DeliverTask initTask(FilterTask task) {
	DeliverTask _task = new DeliverTask();

	return _task;
    }

    Task updateTask(Task task) {
	logger_.info("updateTask()");

	switch(task.getStatus()) {
	case Task.PROXY_CONSUMER_FILTERED:
	case Task.SUPPLIER_ADMIN_FILTERED:
	case Task.CONSUMER_ADMIN_FILTERED: 
	case Task.PROXY_SUPPLIER_FILTERED: 
	    FilterTask _filterTask = (FilterTask)task;
	    List _allDests = _filterTask.getNewDestinations();
	    Destination[] _dest = (Destination[])_allDests.toArray(ARRAY_TEMPLATE);

	    logger_.debug("task has now " + _dest.length + " Destinations");
	    if (_dest.length == 0) {
		return null;
	    }
	    _filterTask.configureDestinations(_dest);	    
	    break;
	case Task.DELIVERED:
	    return null;
	}
	return task;
    }

}// TaskConfigurator
