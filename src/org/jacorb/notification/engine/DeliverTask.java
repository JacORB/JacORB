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

import org.jacorb.notification.framework.EventDispatcher;
import org.jacorb.notification.NotificationEvent;
import org.apache.log4j.Logger;

/**
 * DeliverTask.java
 *
 *
 * Created: Thu Nov 14 23:24:10 2002
 *
 * @author <a href="mailto:bendt@inf.fu-berlin.de">Alphonse Bendt</a>
 * @version $Id$
 */

public class DeliverTask extends TaskBase {

    Logger logger_ = Logger.getLogger("TASK.Deliver");
    Logger timeLogger_ = Logger.getLogger("TIME.Deliver");
    EventDispatcher destination_;
    boolean done_;
    boolean released_;
    Thread releasedBy_;
    Thread createdBy_;
    long releaseTime_;
    int runs_ = 0;
    boolean fresh_ = true;
    Exception[] es = new Exception[2]; 

    static int number = 0;
    int myNumber = number++;

    public void reset() {
	destination_ = null;
	done_ = false;
	super.reset();
    }

    void configureDestination(EventDispatcher dest) {
	destination_ = dest;
    }

    public int getStatus() {
	return status_;
    }

    void setStatus(int status) {
	status_ = status;
    }

    public boolean getDone() {
	return done_;
    }

    public void doWork() {
	long _time = System.currentTimeMillis();

	logger_.debug("dispatch to " + destination_);
	destination_.dispatchEvent(event_);
	done_ = true;
	runs_++;

	timeLogger_.info("deliver(): " + (System.currentTimeMillis() - _time));
    }

    public void release() {
	if (released_) {
	    throw new RuntimeException();
	}
	released_ = true;

	super.release();
    }
    
    public String toString() {
	return (released_?" released" : "") + "DeliverTask:" +myNumber+ "/" + runs_ + " with Destination " + destination_;
    }

    public void queue() {
	if (released_) {
	    System.out.println("fresh: " + fresh_);
	    System.out.println(this);
	    
	    System.out.println("Was released multiple times:");
	    System.out.println("1: " + releasedBy_);
	    System.out.println("2: " + Thread.currentThread());
	    System.out.println("was created by: " + createdBy_);
	    System.out.println("rel1: " + releaseTime_);
	    System.out.println("rel2: " + System.currentTimeMillis());

	    System.out.println("the first time here: ");
	    es[0].printStackTrace();	    
	    es[1].printStackTrace();	    

	    throw new RuntimeException();
	}
    }
}// DeliverTask
