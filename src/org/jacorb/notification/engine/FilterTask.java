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
package org.jacorb.notification.engine;

import org.jacorb.notification.NotificationEvent;
import java.util.Vector;
import java.util.List;
import org.omg.CosNotification.StructuredEvent;
import java.util.Iterator;
import org.omg.CosNotifyFilter.Filter;
import org.omg.CosNotifyFilter.UnsupportedFilterableData;
import org.apache.log4j.Logger;
import org.omg.CORBA.Any;
import org.jacorb.notification.TransmitEventCapable;

/*
 *        JacORB - a free Java ORB
 */

/**
 * FilterTask.java
 *
 *
 * Created: Thu Nov 14 20:34:23 2002
 *
 * @author <a href="mailto:bendt@inf.fu-berlin.de">Alphonse Bendt</a>
 * @version $Id$
 */

public class FilterTask implements Task {
    Destination[] destination_;
    NotificationEvent event_;
    Logger logger_ = Logger.getLogger("TASK.Filter");

    List newDestinations_;

    private int status_;

    private int runs_ = 0;

    boolean forward_;

    FilterTask() {
    }

    public void configureDestinations(Destination[] dest) {
	logger_.debug("configureDestinations() with " + dest.length + " Destinations");
	destination_ = dest;
    }

    public void configureEvent(NotificationEvent event) {
	event_ = event;
    }

    public int getStatus() {
	return status_;
    }

    void setStatus(int status) {
	status_ = status;
    }

    public boolean isForward() {
	return forward_;
    }

    public List getNewDestinations() {
	logger_.debug("getNewDestinations()");
	logger_.debug("has " + newDestinations_.size() + " destinations");

	return newDestinations_;
    }

    public synchronized void run() {
	runs_++;

	logger_.info("run nr.: " + runs_);

	switch(getStatus()) {
	case NEW:
	    forward_ = filter();
	    setStatus(PROXY_CONSUMER_FILTERED);
	    logger_.info("NEW => PROXY_CONSUMER_FILTER");
	    break;
	case PROXY_CONSUMER_FILTERED:
	    forward_ = filter();
	    setStatus(SUPPLIER_ADMIN_FILTERED);
	    logger_.info("PROXY_CONSUMER_FILTER => SUPPLIER_ADMIN_FILTER");
	    break;
	case SUPPLIER_ADMIN_FILTERED:
	    forward_ = filter();
	    setStatus(CONSUMER_ADMIN_FILTERED);
	    logger_.info("SUPPLIER_ADMIN_FILTER => CONSUMER_ADMIN_FILTER");
	    break;
	case CONSUMER_ADMIN_FILTERED:
	    forward_ = filter();
	    setStatus(PROXY_SUPPLIER_FILTERED);
	    logger_.info("CONSUMER_ADMIN_FILTER => PROXY_SUPPLIER_FILTER");
	    break;
	case PROXY_SUPPLIER_FILTERED:
	    transmit_event();
	    setStatus(DELIVERED);
	    logger_.info("PROXY_SUPPLIER_FILTER => DELIVERED");
	    break;
	default:
	    throw new RuntimeException();
	}
    }

    private void transmit_event() {
	for (int x=0; x<destination_.length; ++x) {
	    logger_.debug("transmit to: " + destination_[x].getEventSink());

	    TransmitEventCapable _sink = destination_[x].getEventSink();
	    _sink.transmit_event(event_);
	}
    }

    private boolean filter() {
	logger_.info("filter");

	boolean _forward = false;

	newDestinations_ = new Vector();
	
	switch(event_.getType()) {
	case NotificationEvent.TYPE_ANY:
	    logger_.debug("event is any");

	    logger_.debug("i have " + destination_.length + " destinations");

	    Any _anyEvent = event_.toAny();
	    for (int x=0; x<destination_.length; ++x) {
		logger_.debug("test destination: " + destination_[x]);
		if (filterEvent(destination_[x].getFilters(), _anyEvent)) {
		    newDestinations_.addAll(destination_[x].getSubsequentDestinations());
		    logger_.debug("added destinations: " + destination_[x].getSubsequentDestinations());
		    _forward = true;
		}
	    }
	    break;
	case NotificationEvent.TYPE_STRUCTURED:
	    logger_.debug("event is structured");

	    StructuredEvent _structEvent = event_.toStructuredEvent();
	    for (int x=0; x<destination_.length; ++x) {
		if (filterEvent(destination_[x].getFilters(), _structEvent)) {
		    newDestinations_.addAll(destination_[x].getSubsequentDestinations());
		    logger_.debug("added destination: " + destination_[x]);
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
		Filter _filter = (Filter)_allFilters.next();
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
	logger_.info("filterEvent(" + filterList + ", any)");

	if (filterList.isEmpty()) {
	    return true;
	}

	Iterator _allFilters = filterList.iterator();
        while (_allFilters.hasNext()) {
            try {
                Filter _filter = (Filter)_allFilters.next();
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
	_b.append("i have been run " + runs_ + " times");

	return _b.toString();
    }
    
}// FilterTask
