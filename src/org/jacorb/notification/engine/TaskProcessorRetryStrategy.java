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

import org.jacorb.notification.interfaces.MessageConsumer;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */
public class TaskProcessorRetryStrategy extends RetryStrategy
{
    Runnable retryPushOperation_ = new Runnable()
    {
        public void run()
        {
            try
            {
                pushOperation_.invokePush();
                taskProcessor_.scheduleTimedPushTask(messageConsumer_);
            }
            catch (Throwable error)
            {
                try
                {
                    remoteExceptionOccured(error);
                    retry();
                }
                catch (RetryException e)
                {
                    dispose();
                }
            }
        }
    };

    Runnable enableMessageConsumer_ = new Runnable()
    {
        public void run()
        {
            try
            {
                messageConsumer_.enableDelivery();
                TaskExecutor _executor = messageConsumer_.getExecutor();
                _executor.execute(retryPushOperation_);
            }
            catch (InterruptedException e)
            {
            }
        }
    };

    TaskProcessor taskProcessor_;

    /**
     * specify how long a ProxySupplier should be disabled in case
     * delivering messages to its Consumer fails.
     */
    private long backoutInterval_;

    public TaskProcessorRetryStrategy(MessageConsumer mc,
                                      PushOperation op,
                                      TaskProcessor tp)
    {
        super(mc, op);
        taskProcessor_ = tp;
        backoutInterval_ = tp.getBackoutInterval();
    }


    public long getTimeToWait() {
        return 0;
    }


    public void retry() throws RetryException
    {
        messageConsumer_.disableDelivery();

        taskProcessor_.executeTaskAfterDelay(backoutInterval_,
                                             enableMessageConsumer_);
    }
}
