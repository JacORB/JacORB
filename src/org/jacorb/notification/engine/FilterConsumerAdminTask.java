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

import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.jacorb.notification.interfaces.FilterStage;

/**
 * FilterConsumerAdminTask.java
 *
 *
 * @author Alphonse Bendt
 * @version $Id$
 */

public class FilterConsumerAdminTask extends AbstractFilterTask
{
    private static final FilterStage[] NO_CURRENT_FILTER_STAGE =
        new FilterStage[ 0 ];

    /**
     * this List contains FilterStages (ProxySuppliers) which hava a EventConsumer associated.
     */
    protected List listOfFilterStageWithEventConsumer_ =
        new Vector();

    /**
     * Initialize this FilterOutgoingTask with the Configuration of
     * another FilterTask.
     */
    public void setFilterStage( AbstractFilterTask other )
    {
        arrayCurrentFilterStage_ = other.getFilterStageToBeProcessed();
    }

    /**
     * access the FilterStages that have a Event Consumer associated.
     */
    public FilterStage[] getFilterStagesWithEventConsumer()
    {
        return ( FilterStage[] )
               listOfFilterStageWithEventConsumer_.toArray( FILTERSTAGE_ARRAY_TEMPLATE );
    }

    public void clearFilterStagesWithEventConsumer()
    {
        listOfFilterStageWithEventConsumer_.clear();
    }

    public void reset()
    {
        super.reset();

        clearFilterStagesWithEventConsumer();
        arrayCurrentFilterStage_ = NO_CURRENT_FILTER_STAGE;
    }

    public void doWork() throws InterruptedException
    {
        filter();

        setStatus( DONE );
    }

    private void filter() throws InterruptedException
    {

        for ( int x = 0; x < arrayCurrentFilterStage_.length; ++x )
        {

            checkInterrupt();

            boolean _filterForCurrentFilterStageMatched = false;

            if ( !arrayCurrentFilterStage_[ x ].isDisposed() )
            {

                _filterForCurrentFilterStageMatched =
                    event_.match( arrayCurrentFilterStage_[ x ] );

            }

            if ( _filterForCurrentFilterStageMatched )
            {

                if ( arrayCurrentFilterStage_[ x ].hasOrSemantic() )
                {

                    // if the subsequent destinations
                    // InterFilterGroupOperator equals OR_OP
                    // we can add it to
                    // listOfFilterStageWithEventConsumer_ as the
                    // corresponding filters dont need to be eval'd

                    listOfFilterStageWithEventConsumer_.
                    addAll( arrayCurrentFilterStage_[ x ].getSubsequentFilterStages() );

                }
                else
                {

                    // the Filters of the ProxySupplier need to be
                    // eval'd

                    listOfFilterStageToBeProcessed_.
                    addAll( arrayCurrentFilterStage_[ x ].getSubsequentFilterStages() );

                }

            }
            else
            {

                // no filter matched at all. we have to check all subsequent
                // destinations if one of them has
                // InterFilterGroupOperator.OR_OP enabled. In this
                // Case add it to the list of ProxySupplier to be
                // eval'd by the next task.

                Iterator _i =
                    arrayCurrentFilterStage_[ x ].getSubsequentFilterStages().iterator();

                while ( _i.hasNext() )
                {
                    FilterStage _n = ( FilterStage ) _i.next();

                    if ( _n.hasOrSemantic() )
                    {

                        listOfFilterStageToBeProcessed_.add( _n );

                    }
                }
            }
        }
    }
}
