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

/**
 * Handle the Filtering of ProxyConsumer and SupplierAdmin Filters.
 *
 *
 * @author Alphonse Bendt
 * @version $Id$
 */

public class FilterIncomingTask extends FilterTaskBase {

    /** flag to determine wether we are filtering the ConsumerProxy or
     * the SupplierAdmin. The Initial value is false (Evaluate the ConsumerProxy).
     */
    private boolean filterAdmin_ = false;
    
    /**
     * The Flag <code>orSemantic_</code> determines if the
     * ConsumerProxy which Filters were evaluated in the previous run
     * had InterFilterGroupOperation.OR_OP enabled. In this Case there
     * is no need to evaluate the Filters of the corresponding ConsumerAdmin.
     */
    private boolean orSemantic_;

    public void reset() {
	super.reset();
	filterAdmin_ = false;
	arrayCurrentFilterStage_ = null;
	orSemantic_ = false;
    }

    public void doWork() {
	if (filterAdmin_) {
	    filterAdmin();
	} else {
	    filterProxy();
	}	   
    }

    /**
     * Evaluate all Filters attached to our ProxyConsumer.
     */
    private void filterProxy() {
	boolean _forward = filter();

	if (!_forward && arrayCurrentFilterStage_[0].hasOrSemantic()) {
	    if (logger_.isDebugEnabled()) {
		logger_.debug("filter failed, but "
			      + arrayCurrentFilterStage_ 
			      + " has Or Semantic Enabled");
	    }

	    // no filter attached to our ProxyConsumer
	    // matched. However the ProxyConsumer has
	    // InterFilterGroupOperator.OR_OP enabled. Therefor we
	    // have to continue processing because the Filters
	    // attached to the ConsumerProxy still may match.

	    matchingFilterStage_.addAll(arrayCurrentFilterStage_[0].getSubsequentFilterStages());
	} 

	filterAdmin_ = true;
	setStatus(RESCHEDULE);
    }

    /**
     * evaluate all Filters attached to our SupplierAdmin.
     */
    private void filterAdmin() {
	boolean _forward = filter();

	setStatus(DONE);
    }

    /**
     * do all the filtering.
     */
    private boolean filter() {
	boolean _forward = false;

	if (orSemantic_) {
	    // was set in the previous run
	    _forward = true;
	} else {
	    // eval attached filters
	    // as an Event passes only 1 ProxyConsumer and 1
	    // SupplierAdmin we assume constant array size here

	    _forward = FilterTaskUtils.filterEvent(arrayCurrentFilterStage_[0], event_);
	}

	if (_forward) {
	    matchingFilterStage_.addAll(arrayCurrentFilterStage_[0].getSubsequentFilterStages());
	}

	// check if this destination has OR enabled
	// if this is the case the filtering in the next run can be skipped
	if (arrayCurrentFilterStage_[0].hasOrSemantic()) {
	    orSemantic_ = true;
	}

	return _forward;
    }
}
