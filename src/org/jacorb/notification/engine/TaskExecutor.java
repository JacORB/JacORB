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

import org.jacorb.notification.interfaces.Disposable;

import EDU.oswego.cs.dl.util.concurrent.DirectExecutor;
import EDU.oswego.cs.dl.util.concurrent.Executor;
import EDU.oswego.cs.dl.util.concurrent.LinkedQueue;
import EDU.oswego.cs.dl.util.concurrent.PooledExecutor;
import EDU.oswego.cs.dl.util.concurrent.ThreadFactory;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */

public class TaskExecutor implements Executor, Disposable
{
    private static final TaskExecutor DIRECT_EXECUTOR = new TaskExecutor("Direct", 0);

    //     private Logger logger_ = Debug.getNamedLogger( getClass().getName() );

    private Executor executor_;

    private LinkedQueue channel_;

    ////////////////////////////////////////

    public static TaskExecutor getDefaultExecutor() {
        return DIRECT_EXECUTOR;
    }

    ////////////////////////////////////////

    public TaskExecutor( final String name, int numberOfThreads )
    {
        if ( numberOfThreads < 0 )
            {
                throw new IllegalArgumentException();
            }
        else if ( numberOfThreads == 0 )
            {
                executor_ = new DirectExecutor();

                //                 if (logger_.isInfoEnabled() ) {
                //                     logger_.info( "Created ThreadPool " + name + ": DirectExecutor");
                //                 }
            }
        else
            {
                ThreadFactory _threadFactory =
                    new ThreadFactory()
                    {
                        private int counter_ = 0;

                        public synchronized Thread newThread( Runnable task )
                        {
                            Thread _thread = new Thread( task );

                            _thread.setDaemon( true );
                            _thread.setName( name + "#" + ( counter_++ ) );

                            return _thread;
                        }
                    };

                channel_ = new LinkedQueue();

                PooledExecutor _executor = new PooledExecutor( channel_ );

                _executor.setThreadFactory( _threadFactory );
                _executor.setKeepAliveTime( -1 );
                _executor.createThreads( numberOfThreads );

                executor_ = _executor;

                //                 if (logger_.isInfoEnabled()) {
                //                     logger_.info( "Created ThreadPool " + name + ": Threads=" + numberOfThreads );
                //                 }
            }
    }

    ////////////////////////////////////////

    public boolean isTaskQueued()
    {
        if ( channel_ != null )
            {
                return !channel_.isEmpty();
            }

        return false;
    }

    public void dispose()
    {
        if ( executor_ instanceof PooledExecutor )
            {
                ( ( PooledExecutor ) executor_ ).shutdownNow();
                ( ( PooledExecutor ) executor_ ).interruptAll();
            }
    }

    public void execute( Runnable r ) throws InterruptedException
    {
        executor_.execute( r );
    }
}

