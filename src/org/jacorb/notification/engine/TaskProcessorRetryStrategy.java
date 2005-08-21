package org.jacorb.notification.engine;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2004 Gerald Brose.
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
 */

import org.jacorb.notification.interfaces.IProxyPushSupplier;

import EDU.oswego.cs.dl.util.concurrent.SynchronizedBoolean;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */
public class TaskProcessorRetryStrategy extends AbstractRetryStrategy implements
        PushTaskExecutor.PushTask
{
    /**
     * retry the failed operation. schedule the retry for delivery.
     */
    public final Runnable retryPushOperation_ = new Runnable()
    {
        public void run()
        {
            if (pushSupplier_.isRetryAllowed())
            {
                pushSupplier_.schedulePush(TaskProcessorRetryStrategy.this);
            } 
            else
            {
                dispose();
            }
        }
    };

    private SynchronizedBoolean isCancelled_ = new SynchronizedBoolean(false);
    
    private final TaskProcessor taskProcessor_;

    private final long backoutInterval_;

    public TaskProcessorRetryStrategy(IProxyPushSupplier pushSupplier, PushOperation pushOperation,
            TaskProcessor taskProcessor, long backoutInterval)
    {
        super(pushSupplier, pushOperation);

        taskProcessor_ = taskProcessor;
        backoutInterval_ = backoutInterval;
    }

    protected long getTimeToWait()
    {
        return 0;
    }

    protected void retryInternal() throws RetryException
    {
        if (pushSupplier_.isRetryAllowed())
        {
            pushSupplier_.disableDelivery();

            taskProcessor_.executeTaskAfterDelay(backoutInterval_, retryPushOperation_);
        }
    }

    public void doPush()
    {
        if (!isCancelled_.get())
        {
            try
            {
                if (pushSupplier_.isRetryAllowed())
                {
                    pushOperation_.invokePush();
                    pushSupplier_.pushPendingData();
                }

                dispose();
            } catch (Exception error)
            {
                try
                {
                    remoteExceptionOccured(error);
                    retry();
                } catch (RetryException e)
                {
                    dispose();
                }
            }
        }
    }

    public void cancel()
    {
        isCancelled_.set(true);
        
        dispose();
    }
}
