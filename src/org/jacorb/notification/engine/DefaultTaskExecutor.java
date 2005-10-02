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
import org.jacorb.notification.util.DisposableManager;

import edu.emory.mathcs.backport.java.util.concurrent.Executor;
import edu.emory.mathcs.backport.java.util.concurrent.ExecutorService;
import edu.emory.mathcs.backport.java.util.concurrent.Executors;
import edu.emory.mathcs.backport.java.util.concurrent.ThreadFactory;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */

public class DefaultTaskExecutor implements TaskExecutor
{
    private static final class DefaultThreadFactory implements ThreadFactory
    {
        private int counter_ = 0;

        private final String name;

        private DefaultThreadFactory(String name)
        {
            super();
            this.name = name;
        }

        public synchronized Thread newThread(Runnable task)
        {
            Thread _thread = new Thread(task);

            _thread.setDaemon(true);
            _thread.setName(name + "#" + (counter_++));

            return _thread;
        }
    }

    private static final DefaultTaskExecutor DIRECT_EXECUTOR = new DefaultTaskExecutor("Direct", 0);

    private final Executor executor_;

    private final DisposableManager disposeHooks_ = new DisposableManager();
    
    ////////////////////////////////////////

    public static TaskExecutor getDefaultExecutor()
    {
        return DIRECT_EXECUTOR;
    }
    
    ////////////////////////////////////////

    public DefaultTaskExecutor(final String name, int numberOfThreads, boolean mayDie)
    {
        if (numberOfThreads < 0)
        {
            throw new IllegalArgumentException();
        }
        else if (numberOfThreads == 0)
        {
            executor_ = new Executor()
            {
                public void execute(Runnable command)
                {
                    command.run();
                }
            };
        }
        else
        {
            ThreadFactory _threadFactory = new DefaultThreadFactory(name);

            final ExecutorService _executor;
            
            if (mayDie)
            {
                _executor = Executors.newCachedThreadPool(_threadFactory);
            }
            else
            {
                _executor = Executors.newFixedThreadPool(numberOfThreads, _threadFactory);
            }
            
            executor_ = _executor;
        }
    }


    public DefaultTaskExecutor(String string, int numberOfThreads)
    {
        this(string, numberOfThreads, false);
    }

    /**
     * @deprecated impossible to determine if tasks are queued.
     */
    public boolean isTaskQueued()
    {
        return true;
    }

    public void dispose()
    {
        if (executor_ instanceof ExecutorService)
        {
            ((ExecutorService) executor_).shutdown();
            ((ExecutorService) executor_).shutdownNow();
        }
        
        disposeHooks_.dispose();
    }

    public void registerDisposable(Disposable d)
    {
        disposeHooks_.addDisposable(d);
    }
    
    public void execute(Runnable runnable)
    {
        executor_.execute(runnable);
    }
}

