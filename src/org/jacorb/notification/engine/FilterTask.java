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
import java.util.Vector;
import java.util.List;
import org.omg.CosNotification.StructuredEvent;
import java.util.Iterator;
import org.omg.CosNotifyFilter.Filter;
import org.omg.CosNotifyFilter.UnsupportedFilterableData;
import org.apache.log4j.Logger;
import org.omg.CORBA.Any;
import org.jacorb.notification.framework.EventDispatcher;
import org.jacorb.notification.framework.DistributorNode;
import org.jacorb.util.Assertion;
import org.jacorb.notification.KeyedListEntry;
/**
 * FilterTask.java
 *
 *
 * Created: Thu Nov 14 20:34:23 2002
 *
 * @author <a href="mailto:bendt@inf.fu-berlin.de">Alphonse Bendt</a>
 * @version $Id$
 */

public class FilterTask extends TaskBase {

    DistributorNode[] destination_;
    Logger logger_ = Logger.getLogger("TASK.Filter");

    Logger timeLogger_ = Logger.getLogger("TIME.Filter");

    boolean done_;
    List newDestinations_;
    int runs_ = 0;
    boolean forward_;
    
    public void reset() {
	super.reset();

	done_ = false;
	newDestinations_ = null;
	runs_ = 0;
	forward_ = false;
    }

    public void configureDestinations(DistributorNode[] dest) {
	//	logger_.debug("configureDestinations() with " + dest.length + " Destinations");
	destination_ = dest;
    }

    public int getStatus() {
	return status_;
    }

    public int getCount() {
	return runs_;
    }

    public void incCount() {
	runs_++;
    }

    void setStatus(int status) {
	status_ = status;
    }

    public boolean getDone() {
	return done_;
    }

    public boolean isForward() {
	return forward_;
    }

    public List getNewDestinations() {
	return newDestinations_;
    }

    public synchronized void doWork() {
	long _time = System.currentTimeMillis();

	setStatus(FILTERING);
	
	forward_ = filter();

	done_ = true;

	timeLogger_.info("filter(): " + (System.currentTimeMillis() - _time));

    }

    private boolean filter() {
	boolean _forward = false;

	// pool
	newDestinations_ = new Vector();
	
	switch(event_.getType()) {
	case NotificationEvent.TYPE_ANY:
	    long _start = System.currentTimeMillis();
	    Any _anyEvent = event_.toAny();
	    timeLogger_.info("event_.toAny(): " + (System.currentTimeMillis() - _start));

	    for (int x=0; x<destination_.length; ++x) {
		_start = System.currentTimeMillis();
		boolean _filterResult = filterEvent(destination_[x].getFilters(), _anyEvent);
		timeLogger_.info("filterEvent(Dest " + x + "): " + (System.currentTimeMillis() - _start));

		if (_filterResult) {
		    _start = System.currentTimeMillis();
		    newDestinations_.addAll(destination_[x].getSubsequentDestinations());
		    timeLogger_.info("newDest.addAll(Dest " + x + "): " + (System.currentTimeMillis() - _start));

		    _forward = true;
		}
	    }
	    break;
	case NotificationEvent.TYPE_STRUCTURED:
	    StructuredEvent _structEvent = event_.toStructuredEvent();
	    for (int x=0; x<destination_.length; ++x) {
		if (filterEvent(destination_[x].getFilters(), _structEvent)) {
		    newDestinations_.addAll(destination_[x].getSubsequentDestinations());
		    _forward = true;
		}
	    }
	    break;
	}
	return _forward;
    }

    private boolean filterEvent(List filterList, StructuredEvent event) {
	if (filterList.isEmpty()) {
	    return true;
	}

	Iterator _allFilters = filterList.iterator();
	while(_allFilters.hasNext()) {
	    try {
		Filter _filter = (Filter)((KeyedListEntry)_allFilters.next()).getValue();
		if (_filter.match_structured(event)) {
		    return true;
		}
	    } catch (UnsupportedFilterableData ufd) {
		logger_.error("Error in Filter: " + ufd);
	    }
	}
	return false;
    }

    private boolean filterEvent(List filterList, Any any) {
	logger_.debug("filterEvent(" + filterList + ", any)");

	if (filterList.isEmpty()) {
	    logger_.debug("list is empty return");
	    return true;
	}

	Iterator _allFilters = filterList.iterator();
        while (_allFilters.hasNext()) {
            try {
                Filter _filter = (Filter)((KeyedListEntry)_allFilters.next()).getValue();
                if (_filter.match(any)) {
                    return true;
                }
            } catch (UnsupportedFilterableData ufd) {
		logger_.error("Error in Filter: " + ufd);
            }
        }
	return false;
    }

    public String toString() {
	StringBuffer _b = new StringBuffer();

	_b.append("FilterTask run: " + runs_ + " times");

	return _b.toString();
    }    
}// FilterTask
