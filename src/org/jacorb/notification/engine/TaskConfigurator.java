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

import org.jacorb.notification.EventChannelImpl;
import org.jacorb.notification.NotificationEvent;
import org.apache.log4j.Logger;
import java.util.List;
import org.jacorb.notification.framework.DistributorNode;
import org.jacorb.notification.framework.EventDispatcher;
import java.util.Iterator;
import org.jacorb.notification.util.ObjectPoolBase;
import org.jacorb.notification.framework.Poolable;

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
    Logger timeLogger_ = Logger.getLogger("TIME.Taskconfigurator");

    DistributorNode[] ARRAY_TEMPLATE = new DistributorNode[0];
    Engine engine_;

    public TaskConfigurator(Engine engine) {
	engine_ = engine;
    }
    
    public void init() {
	filterTaskPool_.init();
	deliverTaskPool_.init();
    }
    
    ObjectPoolBase filterTaskPool_ = new ObjectPoolBase() {
	    public Object newInstance() {
		return new FilterTask();
	    }
	    public void passivateObject(Object o) {
		((Poolable)o).reset();
	    }
	    public void activateObject(Object o) {
		((Poolable)o).setObjectPool(this);
		((TaskBase)o).setTaskCoordinator(engine_);
	    }
	};

    ObjectPoolBase deliverTaskPool_ =
	new ObjectPoolBase() {
	    public Object newInstance() {
		return new DeliverTask();
	    }
	    public void passivateObject(Object o) {		
		((Poolable)o).reset();
		((DeliverTask)o).fresh_ = false;
	    }
	    public void activateObject(Object o) {
		((Poolable)o).setObjectPool(this);
		((TaskBase)o).setTaskCoordinator(engine_);
		((DeliverTask)o).released_ = false;
	    }
	};

    FilterTask initTask(NotificationEvent event) {
	long _start = System.currentTimeMillis();
	long _stop = 0;
	try {
	    FilterTask task = (FilterTask)filterTaskPool_.lendObject();
	    task.setNotificationEvent(event);
	    
	    DistributorNode[] _d = new DistributorNode[] {event.getDistributorNode()};
	    
	    task.configureDestinations(_d);
	    task.setStatus(Task.NEW);

	    _stop = System.currentTimeMillis();

	    return task;
	} catch (Throwable t) {
	    t.printStackTrace();
	} finally {
	    timeLogger_.info("initTask(): " + (_stop - _start));
	}
	return null;
    }

    DeliverTask[] initTask(FilterTask task) {
	List _allDests = task.getNewDestinations();
	DeliverTask _deliverTasks[] = new DeliverTask[_allDests.size()];

	EventDispatcher[] _disp = new EventDispatcher[_allDests.size()];
	int x=0;
	NotificationEvent _event = task.removeNotificationEvent();
	for (Iterator i=_allDests.iterator();i.hasNext();) {
	    _deliverTasks[x] = (DeliverTask)deliverTaskPool_.lendObject();
	    _deliverTasks[x].configureDestination(((DistributorNode)i.next()).getEventDispatcher());
	    _deliverTasks[x].setStatus(Task.DELIVERING);
	    _deliverTasks[x].setNotificationEvent(_event);
	    ++x;
	}
	_event.release();
	logger_.debug("return: " + _deliverTasks.length + " new tasks");
	return _deliverTasks;
    }

    void updateTask(Task task) throws InterruptedException {
	if (task.getDone()) {
	    switch(task.getStatus()) {
	    case Task.NEW:
		// fallthrough
	    case Task.FILTERING:
		FilterTask _filterTask = (FilterTask)task;
		List _allDests = _filterTask.getNewDestinations();
		DistributorNode[] _dest = (DistributorNode[])_allDests.toArray(ARRAY_TEMPLATE);
		_filterTask.incCount();
		if (_dest.length == 0) {
		    // Drop task		    
		    _filterTask.removeNotificationEvent().release();
		    _filterTask.release();
		} else if (_filterTask.getCount() < 4) {
		    _filterTask.configureDestinations(_dest);
		    engine_.queueFilterTask(_filterTask);
		} else {
		    Poolable _toBeReleased = (Poolable)task;
		    DeliverTask[] _deliverTasks = initTask(_filterTask);
		    for (int x=0; x<_deliverTasks.length; ++x) {
			if (_deliverTasks[x].released_) {
			    throw new RuntimeException();
			}
		    }
		    engine_.queueDeliverTask(_deliverTasks);
		    _toBeReleased.release();
		}
		break;
	    case Task.DELIVERING:
		// fallthrough
	    case Task.DELIVERED:
		((DeliverTask)task).removeNotificationEvent().release();
		logger_.debug("release task");
		((Poolable)task).release();
		logger_.debug("done");
		task = null;
		// fallthrough
	    default:
		break;
	    }
	}
    }

}// TaskConfigurator
