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

import org.jacorb.notification.util.AbstractPoolable;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */

public abstract class AbstractTask extends AbstractPoolable implements Runnable, Schedulable
{
    private TaskExecutor taskExecutor_;

    protected TaskExecutor getTaskExecutor()
    {
        return taskExecutor_;
    }

    protected void setTaskExecutor(TaskExecutor taskExecutor)
    {
        taskExecutor_ = taskExecutor;
    }

    /**
     * Override this Method in Subclasses to do the "real work".
     */
    public abstract void doWork() throws Exception;

    protected boolean isRunnable()
    {
        return true;
    }

    /**
     * run method invoked by TaskExecutor.
     */
    public void run()
    {
        try
        {
            if (isRunnable())
            {
                doWork();
            }
        } catch (Exception e)
        {
            handleTaskError(this, e);
        } finally
        {
            dispose();
        }
    }

    /**
     * error handler method that will be invoked if an exception occurs during doWork.
     * 
     * @param task the task that caused the error.
     * @param error the exception that was thrown.
     */
    abstract void handleTaskError(AbstractTask task, Exception error);

    protected void checkInterrupt() throws InterruptedException
    {
        if (Thread.currentThread().isInterrupted())
        {
            throw new InterruptedException();
        }
    }

    /**
     * schedule this Task for execution.
     * 
     * @param directRunAllowed
     *            true, if the task may be run in the calling thread. false, if the TaskExecutor
     *            should be used.
     */
    protected void schedule(boolean directRunAllowed) 
    {
        schedule(taskExecutor_, directRunAllowed);
    }

    /**
     * schedule this Task for execution.
     * 
     * @param executor
     *            TaskExecutor that should execute this Task
     * 
     * @param directRunAllowed
     *            true, if the task may be run in the calling thread. false, if the TaskExecutor
     *            should be used.
     */
    protected void schedule(TaskExecutor executor, boolean directRunAllowed)
    {
        if (directRunAllowed)
        {
            run();
        }
        else
        {
            executor.execute(this);
        }
    }
}