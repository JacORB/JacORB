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
public abstract class AbstractFilterTask extends AbstractMessageTask
{
    private final TaskFactory taskFactory_;

    /**
     * for debugging purpose.
     */
    private static final boolean STRICT_CHECKING = true;

    /**
     * empty default value for field arrayCurrentFilterStage_. its used instead of null
     */
    protected static final FilterStage[] EMPTY_FILTERSTAGE = new FilterStage[0];

    /**
     * FilterStages to process.
     */
    protected FilterStage[] arrayCurrentFilterStage_;

    /**
     * child FilterStages for which evaluation was successful. these Stages are to be eval'd by the
     * next Task. As each Task is processed by one Thread at a time unsynchronized ArrayList can be
     * used here.
     */
    private final List listOfFilterStageToBeProcessed_ = new ArrayList();

    ////////////////////

    protected AbstractFilterTask(TaskFactory taskFactory, TaskExecutor taskExecutor)
    {
        setTaskExecutor(taskExecutor);

        taskFactory_ = taskFactory;

        arrayCurrentFilterStage_ = EMPTY_FILTERSTAGE;
    }

    ////////////////////

    public final void doWork() throws InterruptedException
    {
        if (arrayCurrentFilterStage_.length > 0)
        {
            doFilter();
        }
    }

    protected abstract void doFilter() throws InterruptedException;

    protected TaskFactory getTaskFactory()
    {
        return taskFactory_;
    }

    protected boolean isFilterStageListEmpty()
    {
        return listOfFilterStageToBeProcessed_.isEmpty();
    }

    protected void addFilterStage(FilterStage s)
    {
        listOfFilterStageToBeProcessed_.add(s);
    }

    protected void addFilterStage(List s)
    {
        if (STRICT_CHECKING)
        {
            Iterator i = s.iterator();

            while (i.hasNext())
            {
                if (!(i.next() instanceof FilterStage))
                {
                    throw new IllegalArgumentException();
                }
            }
        }
        listOfFilterStageToBeProcessed_.addAll(s);
    }

    /**
     * set the FilterStages for the next run.
     */
    public void setCurrentFilterStage(FilterStage[] currentFilterStage)
    {
        arrayCurrentFilterStage_ = currentFilterStage;
    }

    /**
     * get the matching FilterStages of the previous run.
     */
    public FilterStage[] getFilterStageToBeProcessed()
    {
        return (FilterStage[]) listOfFilterStageToBeProcessed_.toArray(EMPTY_FILTERSTAGE);
    }

    /**
     * clear the result of the previous run.
     */
    public void clearFilterStageToBeProcessed()
    {
        listOfFilterStageToBeProcessed_.clear();
    }

    public synchronized void reset()
    {
        super.reset();

        clearFilterStageToBeProcessed();
        arrayCurrentFilterStage_ = EMPTY_FILTERSTAGE;        
    }

    public void handleTaskError(AbstractTask task, Throwable error)
    {
        logger_.fatalError("Error while Filtering in Task:" + task, error);
    }

    /**
     * Schedule this Task on its default Executor for execution.
     */
    public void schedule() throws InterruptedException
    {
        // as all FilterTasks share their Executor, queuing of this
        // Task can be avoided if there are no other Tasks to run.
        // in this case this Task will be run immediately.
        schedule(!getTaskExecutor().isTaskQueued());
    }
}