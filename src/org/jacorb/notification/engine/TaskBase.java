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
import org.jacorb.notification.framework.Poolable;
import org.jacorb.notification.util.ObjectPoolBase;

/**
 * TaskBase.java
 *
 *
 * Created: Fri Jan 03 11:22:52 2003
 *
 * @author <a href="mailto:bendt@inf.fu-berlin.de">Alphonse Bendt</a>
 * @version $Id$
 */

public abstract class TaskBase implements Task, Poolable {

    protected ObjectPoolBase myPool_;
    private TaskCoordinator coordinator_;
    protected NotificationEvent event_;
    protected int status_;

    TaskBase() {
    }
    
    public void setTaskCoordinator(TaskCoordinator coord) {
	coordinator_ = coord;
    }

    public void setNotificationEvent(NotificationEvent event) {
	if (event_ != null) {
	    throw new RuntimeException("remove old first");
	}

	event_ = event;
	event_.addReference();
    }

    public NotificationEvent removeNotificationEvent() {
	NotificationEvent _event = event_;
	event_ = null;
	return _event;
    }

    public abstract void doWork();

    public synchronized void run() {
	try {
	    doWork();
	    coordinator_.workDone(this);
	} catch (Throwable t) {
	    coordinator_.handleError(this, t);
	}
    }

    // Interface Poolable
    
    public void setObjectPool(ObjectPoolBase pool) {
	myPool_ = pool;
    }

    public void release() {
	myPool_.returnObject(this);
    }
    
    public void reset() {
	//	myPool_ = null;
	coordinator_ = null;
	event_ = null;
	status_ = 0;
    }
}// TaskBase
