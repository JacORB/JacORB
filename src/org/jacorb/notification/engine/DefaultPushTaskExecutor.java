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

package org.jacorb.notification.engine;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jacorb.notification.interfaces.Disposable;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class DefaultPushTaskExecutor implements PushTaskExecutor, Disposable
{
    private static int noOfExecutors_ = 0;
    private final int executorNr_ = noOfExecutors_++;
    
    final LinkedBlockingQueue scheduledPushTasks_ = new LinkedBlockingQueue();

    final AtomicBoolean isActive_ = new AtomicBoolean(true);

    final List workers_ = new ArrayList();

    private class Worker extends Thread
    {
        public Worker(String name)
        {
           super(name); 
        }
        
        public void run()
        {
            while (isActive_.get())
            {
                try
                {
                    PushTaskExecutor.PushTask pushTask = 
                        (PushTaskExecutor.PushTask) scheduledPushTasks_.take();

                    if (isActive_.get())
                    {
                        pushTask.doPush();
                    }
                } catch (InterruptedException e)
                {
                    // ignore
                }
            }
        }
    }

    public DefaultPushTaskExecutor(int numberOfWorkers)
    {
        if (numberOfWorkers < 1)
        {
            throw new IllegalArgumentException("At least 1 Worker");
        }

        createWorkers(numberOfWorkers);

        startWorkers();
    }

    public void executePush(PushTaskExecutor.PushTask pushTask)
    {
        if (isActive_.get())
        {
            try
            {
                scheduledPushTasks_.put(pushTask);
            } catch (InterruptedException e)
            {
                // TODO
            }
        }
    }

    public void dispose()
    {
        isActive_.set(false);

        try
        {
            while (!scheduledPushTasks_.isEmpty())
            {
                PushTaskExecutor.PushTask pushTask = (PushTask) scheduledPushTasks_.take();
                pushTask.cancel();
            }
            
        } catch (InterruptedException e)
        {
            // ignore
        }
        
        disposeWorkers();
    }

    private void createWorkers(int numberOfWorkers)
    {
        for (int x = 0; x < numberOfWorkers; ++x)
        {
            Worker worker = new Worker("PushTaskExecutor#" + executorNr_ + "-" + x);
            workers_.add(worker);
        }
    }

    private void startWorkers()
    {
        Iterator i = workers_.iterator();

        while (i.hasNext())
        {
            ((Thread) i.next()).start();
        }
    }

    private void disposeWorkers()
    {
        Iterator i = workers_.iterator();

        while (i.hasNext())
        {
            Thread thread = (Thread) i.next();

            thread.interrupt();
        }

        workers_.clear();
    }
}
