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

import org.jacorb.notification.interfaces.MessageConsumer;
import org.jacorb.notification.engine.TaskExecutor;


/**
 * @author Alphonse Bendt
 * @version $Id$
 */

public abstract class AbstractDeliverTask extends AbstractTask
{
    private MessageConsumer messageConsumer_;

    ////////////////////

    protected AbstractDeliverTask(TaskProcessor tp) {
        super(tp);
    }

    ////////////////////

    public static void scheduleTasks(AbstractDeliverTask[] tasks) throws InterruptedException
    {
        for ( int x = 0; x < tasks.length; ++x )
        {
            tasks[x].schedule(false);
        }
    }

    ////////////////////////////////////////

    public void reset()
    {
        super.reset();

        messageConsumer_ = null;
    }


    protected MessageConsumer getMessageConsumer()
    {
        return messageConsumer_;
    }


    public void setMessageConsumer( MessageConsumer messageConsumer )
    {
        messageConsumer_ = messageConsumer;
    }


    public void handleTaskError(AbstractTask task, Throwable error)
    {
        logger_.error("handleTaskError " + task, error);

        throw new RuntimeException();
    }


    /**
     * override default schedule to use the TaskExecutor provided
     * by the current MessageConsumer.
     */
    protected void schedule(boolean directRunAllowed) throws InterruptedException {
        schedule(getTaskExecutor(), directRunAllowed);
    }


    public void schedule() throws InterruptedException {
        schedule(!getTaskExecutor().isTaskQueued());
    }


    /**
     * override to use the TaskExecutor provided by the current MessageConsumer
     */
    protected TaskExecutor getTaskExecutor() {
        return getMessageConsumer().getExecutor();
    }
}
