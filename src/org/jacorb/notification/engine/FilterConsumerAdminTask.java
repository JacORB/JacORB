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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jacorb.notification.interfaces.FilterStage;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */

public class FilterConsumerAdminTask extends AbstractFilterTask
{
    private static int sCount = 0;

    private int id_ = ++sCount;

    /**
     * this List contains FilterStages (ProxySuppliers) which have a MessageConsumer associated.
     */
    private final List listOfFilterStageWithMessageConsumer_ = new ArrayList();

    ////////////////////////////////////////

    public FilterConsumerAdminTask(TaskExecutor te, TaskProcessor tp, TaskFactory tc)
    {
        super(te, tp, tc);
    }

    ////////////////////////////////////////

    public String toString()
    {
        return "[FilterConsumerAdminTask#" + id_ + "]";
    }

    /**
     * access the FilterStages that have a Event Consumer associated.
     */
    public FilterStage[] getFilterStagesWithMessageConsumer()
    {
        return (FilterStage[]) listOfFilterStageWithMessageConsumer_.toArray(EMPTY_FILTERSTAGE);
    }

    private void clearFilterStagesWithMessageConsumer()
    {
        listOfFilterStageWithMessageConsumer_.clear();
    }

    public void reset()
    {
        super.reset();

        clearFilterStagesWithMessageConsumer();
        arrayCurrentFilterStage_ = EMPTY_FILTERSTAGE;
    }

    public void doFilter() throws InterruptedException
    {
        filter();

        pushToConsumers();
    }

    private void pushToConsumers() throws InterruptedException
    {
        // if we are filtering Outgoing events its
        // possible that deliveries can be made as soon as
        // the ConsumerAdmin Filters are eval'd
        // (if InterFilterGroupOperator.OR_OP is set !)

        FilterStage[] _filterStagesWithMessageConsumer = getFilterStagesWithMessageConsumer();

        if (_filterStagesWithMessageConsumer.length > 0)
        {
            AbstractDeliverTask[] _listOfPushToConsumerTaskToBeScheduled = getTaskFactory()
                    .newPushToConsumerTask(_filterStagesWithMessageConsumer, copyMessage());

            AbstractDeliverTask.scheduleTasks(_listOfPushToConsumerTaskToBeScheduled);
        }

        Schedulable _filterTaskToBeScheduled = _filterTaskToBeScheduled = getTaskFactory()
                .newFilterProxySupplierTask(this);

        _filterTaskToBeScheduled.schedule();
    }

    private void filter() throws InterruptedException
    {
        for (int x = 0; x < arrayCurrentFilterStage_.length; ++x)
        {
            checkInterrupt();

            boolean _filterForCurrentFilterStageMatched = false;

            if (!arrayCurrentFilterStage_[x].isDisposed())
            {
                _filterForCurrentFilterStageMatched = getMessage().match(arrayCurrentFilterStage_[x]);
            }

            if (_filterForCurrentFilterStageMatched)
            {
                if (arrayCurrentFilterStage_[x].hasInterFilterGroupOperatorOR())
                {
                    // if the destinations
                    // InterFilterGroupOperator equals OR_OP
                    // we can add it to
                    // listOfFilterStageWithMessageConsumer_ as the
                    // corresponding filters dont need to be eval'd

                    listOfFilterStageWithMessageConsumer_.addAll(arrayCurrentFilterStage_[x]
                            .getSubsequentFilterStages());
                }
                else
                {
                    // the Filters of the ProxySupplier need to be
                    // eval'd

                    addFilterStage(arrayCurrentFilterStage_[x].getSubsequentFilterStages());
                }
            }
            else
            {
                // no filter matched at all. we have to check all subsequent
                // destinations if one of them has
                // InterFilterGroupOperator.OR_OP enabled. In this
                // Case add it to the list of ProxySupplier to be
                // eval'd by the next task.

                Iterator _i = arrayCurrentFilterStage_[x].getSubsequentFilterStages().iterator();

                while (_i.hasNext())
                {
                    FilterStage _n = (FilterStage) _i.next();

                    if (_n.hasInterFilterGroupOperatorOR())
                    {
                        addFilterStage(_n);
                    }
                }
            }
        }
    }
}