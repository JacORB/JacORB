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

import org.apache.log.Hierarchy;
import org.apache.log.Logger;
import org.jacorb.notification.interfaces.Message;
import org.jacorb.notification.interfaces.AbstractPoolable;

/**
 * TaskBase.java
 *
 * @author Alphonse Bendt
 * @version $Id$
 */

public abstract class AbstractTask extends AbstractPoolable implements Task
{
    protected Logger logger_ =
        Hierarchy.getDefaultHierarchy().getLoggerFor( getClass().getName() );

    private TaskFinishHandler coordinator_;
    private TaskErrorHandler errorHandler_;
    protected Message event_;
    protected int status_;

    AbstractTask()
    {
        status_ = NEW;
    }

    /**
     * Set Status of this Task.
     */
    protected void setStatus( int status )
    {
        status_ = status;
    }

    /**
     * Get the current Status of this Task.
     */
    public int getStatus()
    {
        return status_;
    }

    /**
     *
     */
    public void setTaskFinishHandler( TaskFinishHandler coord )
    {
        coordinator_ = coord;
    }

    public void setTaskErrorHandler( TaskErrorHandler handler )
    {
        errorHandler_ = handler;
    }

    /**
     * set the Message for this Task to use.
     */
    public void setMessage( Message event )
    {
        if ( event_ != null )
        {
            throw new RuntimeException( "remove old first" );
        }

        event_ = event;
    }

    public Message removeMessage()
    {
        Message _event = event_;
        event_ = null;
        return _event;
    }


    public Message copyMessage()
    {
        return ( Message ) event_.clone();
    }

    /**
     * Override this Method in Subclasses to do the "real work".
     */
    public abstract void doWork() throws Exception;

    /**
     * template method.
     * <ol><li>Call doWork()
     * <li>Call TaskFinishHandler in case of success
     * <li>Call TaskErrorHandler in case a exception occurs while
     * executing doWork
     * </ol>
     */
    public void run()
    {
        try
        {

            if ( event_ == null || !isEventDisposed() )
            {
                doWork();
            }

            if ( isEventDisposed() )
            {
                logger_.debug( "event has been marked disposable" );
                setStatus( DISPOSABLE );
            }

            coordinator_.handleTaskFinished( this );
        }
        catch ( Throwable t )
        {
            errorHandler_.handleTaskError( this, t );
        }

    }

    public void reset()
    {
        coordinator_ = null;
        event_ = null;
        status_ = NEW;
    }

    private boolean isEventDisposed()
    {
        return event_ != null && event_.isInvalid();
    }

    protected void checkInterrupt() throws InterruptedException
    {
        if ( Thread.currentThread().isInterrupted() || event_.isInvalid() )
        {
            logger_.debug( "Worker Thread has been interrupted" );
            throw new InterruptedException();
        }
    }
}
