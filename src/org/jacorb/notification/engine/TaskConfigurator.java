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
