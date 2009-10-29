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

package org.jacorb.notification.servant;

import org.jacorb.notification.engine.TaskProcessor;
import org.jacorb.notification.interfaces.MessageSupplier;

import java.util.concurrent.ScheduledFuture;

public class PullMessagesUtility
{
    private ScheduledFuture taskId_;

    private long interval_;

    private final TaskProcessor taskProcessor_;

    private final Runnable timerJob_;

    public PullMessagesUtility(final TaskProcessor taskProcessor, final MessageSupplier messageSupplier)
    {
        taskProcessor_ = taskProcessor;
        timerJob_ = new Runnable()
        {
            public void run()
            {
                taskProcessor.scheduleTimedPullTask(messageSupplier);
            }
        };
    }

    public synchronized void startTask(long interval)
    {
        if (interval <= 0)
        {
            throw new IllegalArgumentException("Interval " + interval + " must be > 0");
        }

        if (taskId_ == null)
        {
            taskId_ = taskProcessor_.executeTaskPeriodically(interval, timerJob_, true);
            interval_ = interval;
        }
    }

    public synchronized void stopTask()
    {
        if (taskId_ != null)
        {
            taskId_.cancel(true);
            taskId_ = null;
        }
    }

    public synchronized void restartTask(long interval)
    {
        if (taskId_ == null)
        {
            throw new IllegalStateException("Not started");
        }

        if (interval_ != interval)
        {
            stopTask();

            startTask(interval);
        }
    }
}
