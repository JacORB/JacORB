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

import org.apache.avalon.framework.logger.Logger;
import org.jacorb.notification.interfaces.IProxyPushSupplier;
import org.jacorb.notification.util.LogUtil;
import org.omg.CORBA.OBJECT_NOT_EXIST;
import org.omg.CosEventComm.Disconnected;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */
public abstract class AbstractRetryStrategy implements RetryStrategy
{
    protected final Logger logger_ = LogUtil.getLogger(getClass().getName());    
    protected final PushOperation pushOperation_;

    protected final IProxyPushSupplier pushSupplier_;

    private boolean active_ = true;
    
    ////////////////////////////////////////

    public AbstractRetryStrategy(IProxyPushSupplier pushSupplier, PushOperation operation)
    {
        pushSupplier_ = pushSupplier;
        pushOperation_ = operation;
    }

    ////////////////////////////////////////

    public void dispose()
    {
        pushOperation_.dispose();
    }

    protected boolean isRetryAllowed()
    {
        return active_ && pushSupplier_.isRetryAllowed();
    }

    protected void remoteExceptionOccured(Throwable error) throws RetryException
    { 
        logger_.debug("Error during retry", error);
        
        if (isFatalException(error))
        {
            pushSupplier_.destroy();
            active_ = false;

            throw new RetryException("fatal exception while retrying push");
        }

        pushSupplier_.incErrorCounter();

        if (!isRetryAllowed())
        {
            pushSupplier_.destroy();
            active_ = false;

            throw new RetryException("no more retries. giving up.");
        }

        waitUntilNextTry();
    }

    public static boolean isFatalException(Throwable error)
    {
        if (error instanceof OBJECT_NOT_EXIST)
        {
            return true;
        }
        else if (error instanceof Disconnected)
        {
            return true;
        }
        return false;
    }

    protected abstract long getTimeToWait();

    public final void retry() throws RetryException
    {
        if (isRetryAllowed())
        {
            waitUntilNextTry();

            retryInternal();
        } else
        {
            dispose();
        }
    }

    protected abstract void retryInternal() throws RetryException;

    private void waitUntilNextTry()
    {
        long timeToWait = getTimeToWait();

        try
        {
            if (timeToWait > 0)
            {
                Thread.sleep(timeToWait);
            }
        } catch (InterruptedException ignored)
        {
            // ignored
        }
    }
}