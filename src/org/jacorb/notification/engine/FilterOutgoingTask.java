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

import org.jacorb.notification.interfaces.FilterStage;
import java.util.Vector;
import java.util.Iterator;
import java.util.List;

/**
 * Handle the Filtering of ConsumerAdmin and ProxySupplier Filters.
 *
 * @author Alphonse Bendt
 * @version $Id$
 */

public class FilterOutgoingTask extends FilterTaskBase {

    /**
     * this List contains FilterStages which hava a EventConsumer associated.
     */
    protected List listOfFilterStageWithEventConsumer_ = new Vector();

    /** flag to determine wether we are filtering the SupplierProxy or
     * the ConsumerAdmin. The Initial value is true (Evaluate the ConsumerAdmin).
     */
    private boolean filterAdmin_ = true;

    /**
     * Initialize this FilterOutgoingTask with the Configuration of
     * another FilterTask.
     */
    public void setFilterStage(FilterTaskBase other) {
	arrayCurrentFilterStage_ = other.getMatchingFilterStage();
    }

    /**
     * access the FilterStages that have a Event Consumer associated.
     */
    public FilterStage[] getFilterStagesWithEventConsumer() {
	return (FilterStage[]) 
	    listOfFilterStageWithEventConsumer_.toArray(FILTERSTAGE_ARRAY_TEMPLATE);
    }

    public void clearFilterStagesWithEventConsumer() {
	listOfFilterStageWithEventConsumer_.clear();
    }

    public void reset() {
	super.reset();
	clearFilterStagesWithEventConsumer();
	arrayCurrentFilterStage_ = null;
	filterAdmin_ = true;
    }

    public void doWork() {
	if (filterAdmin_) {
	    filterAdmin();
	} else {
	    filterProxy();
	}
    }

    private void filterAdmin() {
	filter();
	filterAdmin_ = false;
	setStatus(RESCHEDULE);
    }

    private void filterProxy() {
	filter();
	setStatus(DONE);
    }

    private void filter() {
	for (int x = 0; x < arrayCurrentFilterStage_.length; ++x) {

	    if (Thread.currentThread().isInterrupted()) {
		logger_.debug("interrupted !!!");
		return;
	    }

	    boolean _forward = false;

	    if (!arrayCurrentFilterStage_[x].isDisposed()) {

		_forward = 
		    FilterTaskUtils.filterEvent(arrayCurrentFilterStage_[x], event_);

	    } else {
		if (logger_.isDebugEnabled()) {
		    logger_.debug("skip Destination: "
				  + arrayCurrentFilterStage_[x]
				  + " cause its disposed");
		}
	    }

	    if (_forward) {

		List _subsequentDests = 
		    arrayCurrentFilterStage_[x].getSubsequentFilterStages();

		// check all subsequent destinations
		if (filterAdmin_ && arrayCurrentFilterStage_[x].hasOrSemantic()) {

		    // if the subsequent destinations
		    // InterFilterGroupOperator equals OR_OP
		    // we can add it to
		    // listOfFilterStageWithEventConsumer_ as the 
		    // corresponding filters dont need to be eval'd

		    listOfFilterStageWithEventConsumer_.
			addAll(arrayCurrentFilterStage_[x].getSubsequentFilterStages());

		} else {

		    // the subsequent destination filters need to be eval'd

		    matchingFilterStage_.
			addAll(arrayCurrentFilterStage_[x].getSubsequentFilterStages());

		}
		
	    } else if (filterAdmin_) {

		// no filter matched. we have to check all subsequent
		// destinations if one of them has
		// InterFilterGroupOperator.OR_OP enabled. In this
		// Case add it to the result set.

		Iterator _i = 
		    arrayCurrentFilterStage_[x].getSubsequentFilterStages().iterator();
		while (_i.hasNext()) {
		    FilterStage _n = (FilterStage) _i.next();
		    if (_n.hasOrSemantic()) {
			if (logger_.isDebugEnabled()) {
			    logger_.debug("add " + _n);
			}
			matchingFilterStage_.add(_n);
		    }
		}
	    }
	}
    }
} 
