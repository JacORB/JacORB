package org.jacorb.notification.engine;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2004 Gerald Brose
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

import org.omg.CORBA.AnyHolder;
import org.omg.CosNotifyFilter.UnsupportedFilterableData;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */

public class FilterProxyConsumerTask extends AbstractFilterTask
{
    private static int sCount = 0;
    private int id_ = ++sCount;

    private boolean orSemantic_ = false;

    ////////////////////

    public FilterProxyConsumerTask(TaskExecutor executor, TaskProcessor processor, TaskFactory factory) {
        super(executor, processor, factory);
    }

    ////////////////////

    public String toString()
    {
        return "[FilterProxyConsumerTask#" + id_ + "]";
    }


    public void reset()
    {
        super.reset();

        orSemantic_ = false;
    }


    /**
     * access the Filter hint for next Stage. if the current
     * FilterStage has InterFilterGroupOperator.OR_OP enabled and a
     * filter matched the
     * evaluation of the SupplierAdmin Filters can be skipped.
     */
    public boolean getSkip()
    {
        return orSemantic_;
    }


    /**
     * match the attached Priority MappingFilter.
     * the current Message is matched to the MappingFilter attached to
     * the current FilterStage. In case of successful match
     * operation the priority of the Messages is updated accordingly.
     */
    private void updatePriority()
    {
        try
        {
            AnyHolder newPriority = new AnyHolder();

            boolean priorityMatch =
                getMessage().match( arrayCurrentFilterStage_[ 0 ].getPriorityFilter(),
                                newPriority );

            if ( priorityMatch )
            {
                getMessage().setPriority( newPriority.value.extract_long() );
            }
        }
        catch ( UnsupportedFilterableData e )
        {
            logger_.error( "Error evaluating PriorityFilter", e );
        }
    }


    /**
     * match the attached Lifetime MappingFilter.
     * the current Message is matched to the MappingFilter attached to
     * the current FilterStage. In case of successful match
     * operation the lifetime of the Messages is updated accordingly.
     */
    private void updateLifetime()
    {
        try
        {
            AnyHolder newLifetime = new AnyHolder();

            boolean lifetimeMatch =
                getMessage().match( arrayCurrentFilterStage_[ 0 ].getLifetimeFilter(),
                                newLifetime );

            if ( lifetimeMatch )
            {
                getMessage().setTimeout( newLifetime.value.extract_long() );
            }
        }
        catch ( UnsupportedFilterableData e )
        {
            logger_.error( "Error evaluating LifetimeFilter", e );
        }
    }


    public void doFilter() throws InterruptedException
    {
        if ( arrayCurrentFilterStage_[ 0 ].hasPriorityFilter() )
        {
            updatePriority();
        }

        if ( arrayCurrentFilterStage_[ 0 ].hasLifetimeFilter() )
        {
            updateLifetime();
        }

        boolean _filterMatch = filter();

        if ( !_filterMatch && arrayCurrentFilterStage_[ 0 ].hasInterFilterGroupOperatorOR() )
        {
            if ( logger_.isDebugEnabled() )
            {
                logger_.debug( "filter failed, but the ProxyConsumer"
                               + arrayCurrentFilterStage_[ 0 ]
                               + " has InterFilterGroupOperator OR_OP Enabled" );
            }

            // no filter attached to the current ProxyConsumer
            // matched. However the ProxyConsumer has
            // InterFilterGroupOperator.OR_OP enabled. Therefor we
            // have to continue processing because the Filters
            // attached to the SupplierAdmin still may match.

            addFilterStage( arrayCurrentFilterStage_[ 0 ].getSubsequentFilterStages() );
        }

        if ( !isFilterStageListEmpty() )
        {
            getTaskFactory().newFilterSupplierAdminTask( this ).schedule();
        } 
    }

    private boolean filter()
    {
        boolean _forward = false;

        // eval attached filters
        // as an Event passes only 1 ProxyConsumer we can assume
        // constant array size here

        _forward = getMessage().match( arrayCurrentFilterStage_[ 0 ] );

        if ( _forward )
        {
            addFilterStage( arrayCurrentFilterStage_[ 0 ].getSubsequentFilterStages() );
        }

        // check if this destination has OR enabled
        // if this is the case the filtering in the next run can be skipped
        if ( arrayCurrentFilterStage_[ 0 ].hasInterFilterGroupOperatorOR() )
        {
            orSemantic_ = true;
        }

        return _forward;
    }

    public void schedule() throws InterruptedException {
        // directRunAllowed is false here
        // cause the calling thread is usually created by the ORB.
        // exceptions are PullSuppliers.
        schedule(false);
    }
}
