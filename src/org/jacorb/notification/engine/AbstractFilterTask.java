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

import java.util.List;
import java.util.Vector;

import org.jacorb.notification.interfaces.FilterStage;

/**
 * Abstract Base Class for FilterTask.
 *
 * @author Alphonse Bendt
 * @version $Id$
 */

abstract class AbstractFilterTask extends AbstractTask
{

    /**
     * Template for internal use.
     */
    protected static final FilterStage[] FILTERSTAGE_ARRAY_TEMPLATE = new FilterStage[ 0 ];

    /**
     * FilterStages to process.
     */
    protected FilterStage[] arrayCurrentFilterStage_;

    /**
     * child FilterStages for which evaluation was successful. these
     * Stages are to be eval'd by the next Task.
     */
    protected List listOfFilterStageToBeProcessed_ = new Vector();

    /**
     * set the FilterStages for the next run.
     */
    public void setCurrentFilterStage( FilterStage[] currentFilterStage )
    {
        arrayCurrentFilterStage_ = currentFilterStage;
    }

    /**
     * get the matching FilterStages of the previous run.
     */
    public FilterStage[] getFilterStageToBeProcessed()
    {
        return ( FilterStage[] ) listOfFilterStageToBeProcessed_.toArray( FILTERSTAGE_ARRAY_TEMPLATE );
    }

    /**
     * clear the result of the previous run.
     */
    public void clearFilterStageToBeProcessed()
    {
        listOfFilterStageToBeProcessed_.clear();
    }

    public void reset()
    {
        super.reset();

        clearFilterStageToBeProcessed();
    }

}
