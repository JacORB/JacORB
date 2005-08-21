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

/**
 * @author Alphonse Bendt
 * @version $Id$
 */

public class WaitRetryStrategy extends AbstractRetryStrategy
{
    public static final long WAIT_TIME_DEFAULT = 1000;

    public static final long WAIT_INCREMENT_DEFAULT = 3000;

    private long currentTimeToWait_;

    private long waitTimeIncrement_;

    // //////////////////////////////////////

    public WaitRetryStrategy(IProxyPushSupplier pushSupplier, PushOperation pushOperation)
    {
        this(pushSupplier, pushOperation, WAIT_TIME_DEFAULT, WAIT_INCREMENT_DEFAULT);
    }

    public WaitRetryStrategy(IProxyPushSupplier pushSupplier, PushOperation pushOperation,
            long startingWaitTime, long waitTimeIncrement)
    {
        super(pushSupplier, pushOperation);

        currentTimeToWait_ = startingWaitTime;

        waitTimeIncrement_ = waitTimeIncrement;
    }

    // //////////////////////////////////////

    protected long getTimeToWait()
    {
        long _timeToWait = currentTimeToWait_;

        currentTimeToWait_ += waitTimeIncrement_;

        return _timeToWait;
    }

    protected void retryInternal() throws RetryException
    {
        try
        {
            while (isRetryAllowed())
            {
                try
                {
                    pushOperation_.invokePush();

                    return;
                } catch (Exception error)
                {
                    remoteExceptionOccured(error);
                }
            }
            
            throw new RetryException("no more retries possible");
        } finally
        {
            dispose();
        }
    }
}
