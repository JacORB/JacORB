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

import org.omg.CORBA.AnyHolder;
import org.omg.CosNotifyFilter.UnsupportedFilterableData;

/**
 * FilterProxyConsumerTask.java
 *
 *
 * @author Alphonse Bendt
 * @version $Id$
 */

public class FilterProxyConsumerTask extends AbstractFilterTask
{

    static int nr = 0;
    int myNr;

    FilterProxyConsumerTask()
    {
        super();
        myNr = nr++;
    }

    public String toString()
    {
        return "FilterProxyConsumerTask#" + myNr;
    }

    private boolean orSemantic_ = false;

    public void reset()
    {
        super.reset();
        orSemantic_ = false;
    }

    /**
     * access the Filter hint for next Stage
     */
    public boolean getSkip()
    {
        return orSemantic_;
    }

    void updatePriority()
    {
        try
        {
            AnyHolder newPriority = new AnyHolder();

            boolean priorityMatch =
                message_.match( arrayCurrentFilterStage_[ 0 ].getPriorityFilter(),
                              newPriority );

            if ( priorityMatch )
            {
                message_.setPriority( newPriority.value.extract_long() );
            }
        }
        catch ( UnsupportedFilterableData e )
        {
            logger_.error( "Error evaluating PriorityFilter", e );
        }
    }

    void updateLifetime()
    {
        try
        {
            AnyHolder newLifetime = new AnyHolder();

            boolean lifetimeMatch =
                message_.match( arrayCurrentFilterStage_[ 0 ].getLifetimeFilter(),
                              newLifetime );

            if ( lifetimeMatch )
            {
                message_.setTimeout( newLifetime.value.extract_long() );
            }
        }
        catch ( UnsupportedFilterableData e )
        {
            logger_.error( "Error evaluating LifetimeFilter", e );
        }
    }



    public void doWork()
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

        if ( !_filterMatch && arrayCurrentFilterStage_[ 0 ].hasOrSemantic() )
        {

            if ( logger_.isDebugEnabled() )
            {
                logger_.debug( "filter failed, but "
                               + arrayCurrentFilterStage_[ 0 ]
                               + " has InterFilterGroupOperator OR_OP Enabled" );
            }

            // no filter attached to our ProxyConsumer
            // matched. However the ProxyConsumer has
            // InterFilterGroupOperator.OR_OP enabled. Therefor we
            // have to continue processing because the Filters
            // attached to the SupplierAdmin still may match.

            listOfFilterStageToBeProcessed_
            .addAll( arrayCurrentFilterStage_[ 0 ].getSubsequentFilterStages() );
        }

        if ( listOfFilterStageToBeProcessed_.isEmpty() )
        {
            setStatus( DISPOSABLE );
        }
        else
        {
            setStatus( DONE );
        }
    }

    private boolean filter()
    {
        boolean _forward = false;

        // eval attached filters
        // as an Event passes only 1 ProxyConsumer we can assume
        // constant array size here

        _forward = message_.match( arrayCurrentFilterStage_[ 0 ] );

        if ( _forward )
        {
            listOfFilterStageToBeProcessed_
            .addAll( arrayCurrentFilterStage_[ 0 ].getSubsequentFilterStages() );
        }

        // check if this destination has OR enabled
        // if this is the case the filtering in the next run can be skipped
        if ( arrayCurrentFilterStage_[ 0 ].hasOrSemantic() )
        {
            orSemantic_ = true;
        }

        return _forward;
    }
}
