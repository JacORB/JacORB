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

import java.util.Collections;
import java.util.Hashtable;
import java.util.Map;

import org.jacorb.notification.NotificationEvent;
import org.jacorb.notification.NotificationEventFactory;
import org.jacorb.notification.interfaces.FilterStage;
import org.omg.CORBA.AnyHolder;
import org.omg.CosNotifyFilter.UnsupportedFilterableData;

/**
 * FilterProxySupplierTask.java
 *
 *
 * @author Alphonse Bendt
 * @version $Id$
 */

public class FilterProxySupplierTask extends FilterTaskBase 
{

    public static class AlternateNotificationEventMap {

	private Map changedNotificationEvents_;

	public AlternateNotificationEventMap() {
	    this(new Hashtable());
	}

	AlternateNotificationEventMap(Map m) {
	    changedNotificationEvents_ = m;
	}

	public NotificationEvent getAlternateNotificationEvent(FilterStage s) {
	    if (changedNotificationEvents_.containsKey(s)) {
		return (NotificationEvent)changedNotificationEvents_.get(s);
	    } 
	    return null;
	}
	
	public void addAlternateNotificationEvent(FilterStage s, NotificationEvent e) {
	    changedNotificationEvents_.put(s, e);
	}
	
	public void clear() {
	    changedNotificationEvents_.clear();
	}

    }

    public static final AlternateNotificationEventMap EMPTY_MAP = 
	new AlternateNotificationEventMap(Collections.EMPTY_MAP) {
	    public void clear() {
	    }
	};

    NotificationEventFactory notificationEventFactory_;

    AlternateNotificationEventMap changedNotificationEvents_ = new AlternateNotificationEventMap();

    private boolean skip_;


    /**
     * Initialize this FilterOutgoingTask with the Configuration of
     * another FilterTask.
     */
    public void setFilterStage(FilterTaskBase other) {
	arrayCurrentFilterStage_ = other.getFilterStageToBeProcessed();
    }

    public void reset() {
	super.reset();
	arrayCurrentFilterStage_ = null;
	changedNotificationEvents_.clear();
    }

    public void doWork() {
	
	filterProxy();

    }

    private void filterProxy() {
	filter();

	setStatus(DONE);
    }

    void updatePriority(int indexOfCurrentEvent) {
	AnyHolder _priorityFilterResult = new AnyHolder();

	try {
	    boolean priorityMatch =
		event_.match(arrayCurrentFilterStage_[indexOfCurrentEvent].getPriorityFilter(), 
			     _priorityFilterResult);

	    if (priorityMatch) {
		NotificationEvent _modifiedEvent =
		    notificationEventFactory_.newEvent(event_);

		_modifiedEvent.setPriority(_priorityFilterResult.value.extract_long());

		changedNotificationEvents_.
		    addAlternateNotificationEvent(arrayCurrentFilterStage_[indexOfCurrentEvent], 
						  _modifiedEvent);

	    }
	} catch (UnsupportedFilterableData e) {
	    logger_.error("error evaluating PriorityFilter", e);
	}
    }

    void updateTimeout(int indexOfCurrentEvent) {
	AnyHolder _lifetimeFilterResult = new AnyHolder();

	try {
	    boolean lifetimeMatch =
		event_.match(arrayCurrentFilterStage_[indexOfCurrentEvent].getLifetimeFilter(), 
			     _lifetimeFilterResult);

	    if (lifetimeMatch) {
		NotificationEvent _modifiedEvent = 
		    notificationEventFactory_.newEvent(event_);

		_modifiedEvent.setTimeout(_lifetimeFilterResult.value.extract_long());

		changedNotificationEvents_.
		    addAlternateNotificationEvent(arrayCurrentFilterStage_[indexOfCurrentEvent], 
						  _modifiedEvent);
	    }

	} catch (UnsupportedFilterableData e) {
	    logger_.error("error evaluating PriorityFilter", e);
	}
    }

    private void filter() {
	for (int x = 0; x < arrayCurrentFilterStage_.length; ++x) {

	    boolean _forward = false;

	    if (!arrayCurrentFilterStage_[x].isDisposed()) {

		if (arrayCurrentFilterStage_[x].hasPriorityFilter()) {
		    updatePriority(x);
		}

		if (arrayCurrentFilterStage_[x].hasLifetimeFilter()) {
		    updateTimeout(x);
		}

		_forward = 
		    event_.match(arrayCurrentFilterStage_[x]);

	    } 

	    if (_forward) {

		    // the subsequent destination filters need to be eval'd

		listOfFilterStageToBeProcessed_.
		    addAll(arrayCurrentFilterStage_[x].getSubsequentFilterStages());

	    }
	}
    }
}
