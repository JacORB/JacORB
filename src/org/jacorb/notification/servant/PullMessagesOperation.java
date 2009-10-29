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

import org.jacorb.notification.conf.Default;
import org.omg.CosEventComm.Disconnected;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class PullMessagesOperation
{
    /**
     * Total number of pull-Operations
     */
    private int pullCounter_;

    /**
     * Total time spent within pull-Operations
     */
    private long timeSpentInPull_;

    /**
     * Total number of successful pull-Operations
     */
    private int successfulPullCounter_;
    
    private final MessageSupplierDelegate delegate_;

    private final Semaphore pullSync_ = new Semaphore(Default.DEFAULT_CONCURRENT_PULL_OPERATIONS_ALLOWED);

    public PullMessagesOperation(MessageSupplierDelegate delegate)
    {
        delegate_ = delegate;
    }
    
    public void runPull() throws Disconnected
    {
        if (!delegate_.getConnected())
        {
            throw new Disconnected();
        }

        if (delegate_.isSuspended())
        {
            return;
        }

        runPullInternal();
    }

    private void runPullInternal() throws Disconnected
    {
        try
        {
            boolean _acquired = pullSync_.tryAcquire(1000, TimeUnit.MILLISECONDS);

            if (_acquired)
            {
                final MessageSupplierDelegate.PullResult _data;
                final long _now = System.currentTimeMillis();

                try
                {
                    _data = delegate_.pullMessages();
                } finally
                {
                    pullSync_.release();
                    timeSpentInPull_ += (System.currentTimeMillis() - _now);
                }

                ++pullCounter_;
                
                if (_data.success_)
                {
                    ++successfulPullCounter_;
                    delegate_.queueMessages(_data);
                }
            }
        } catch (InterruptedException e)
        {
            // ignored
            // TODO log
        }
    }

    public int getPullCounter()
    {
        return pullCounter_;
    }

    public int getSuccessfulPullCounter()
    {
        return successfulPullCounter_;
    }

    public long getTimeSpentInPull()
    {
        return timeSpentInPull_;
    }
}
