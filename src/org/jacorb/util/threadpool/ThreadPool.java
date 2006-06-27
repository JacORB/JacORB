package org.jacorb.util.threadpool;

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

import java.util.*;
import org.jacorb.config.Configuration;
import org.apache.avalon.framework.logger.Logger;

/**
 * @author Nicolas Noffke
 * @version $Id$
 */
public class ThreadPool
{
    private final int max_threads;
    private final int max_idle_threads;

    private int total_threads = 0;
    private int idle_threads = 0;

    private final LinkedList job_queue;
    private final ConsumerFactory factory;

    private final String namePrefix;
    private int threadCount = 0;

    /**
     * <code>logger</code> is the logger for threadpool.
     */
    private final Logger logger;

    /**
     * <code>shutdown</code> denotes whether to shutdown the pool.
     */
    private boolean shutdown;

    public ThreadPool( Configuration configuration,
                       String threadNamePrefix,
                       ConsumerFactory factory,
                       int max_threads,
                       int max_idle_threads)
    {
        namePrefix = threadNamePrefix;
        this.job_queue = new LinkedList ();
        this.factory = factory;
        this.max_threads = max_threads;
        this.max_idle_threads = max_idle_threads;

        logger = configuration.getNamedLogger("jacorb.util.tpool");
    }

    protected synchronized Object getJob()
    {
        idle_threads++;

        /*
         * Check job queue is empty before having surplus idle threads exit,
         * otherwise (as was done previously) if a large number of jobs get
         * enqueued just after a large number of previously non-idle threads
         * complete their jobs, then all the newly created threads as well as
         * the newly non-idle threads will exit until max_idle_threads is
         * reached before serving up any jobs, and if there are more jobs than
         * max_idle_threads despite the fact that enough threads once existed to
         * handle the queued jobs, the excess jobs will be blocked until the
         * jobs taking up the max_idle_threads complete.
         *
         * Also, checking the idle_thread count every time the thread is
         * notified and the job queue is empty ensures that the surplus idle
         * threads get cleaned up more quickly, otherwise idle threads can only
         * be cleaned up after completing a job.
         */
        while( (! shutdown) && job_queue.isEmpty() )
        {
            /*
             * This tells the newly idle thread to exit, because
             * there are already too much idle threads.
             */
            if (idle_threads > max_idle_threads)
            {
                if (logger.isDebugEnabled())
                {
                    logger.debug("[" + idle_threads + "/" + total_threads +
                                 "] Telling thread to exit (too many idle)");
                }
                return getShutdownJob();
            }

            try
            {
                if (logger.isDebugEnabled())
                {
                    logger.debug("[" + idle_threads + "/" + total_threads +
                                 "] job queue empty");
                }
                wait();
            }
            catch( InterruptedException e )
            {
                // ignored
            }
        }
        //pool is to be shut down completely
        if (shutdown)
        {
            return getShutdownJob();
        }

        idle_threads--;

        if (logger.isDebugEnabled())
        {
            logger.debug("[" + idle_threads + "/" + total_threads +
                         "] removed idle thread (job scheduled)");
        }
        return job_queue.removeFirst();
    }

    /**
     * the returned null will cause the ConsumerTie to exit.
     * also decrement thread counters.
     */
    private Object getShutdownJob()
    {
        total_threads--;
        idle_threads--;
        return null;
    }

    public synchronized void putJob( Object job )
    {
        job_queue.add(job);
        notifyAll();

        /*
         * Create a new thread if there aren't enough idle threads
         * to handle all the jobs in the queue and we haven't reached
         * the max thread limit.  This ensures that there are always
         * enough idle threads to handle all the jobs in the queue
         * and no jobs get stuck blocked waiting for a thread to become
         * idle while we are still below the max thread limit.
         */
        if ((job_queue.size() > idle_threads) &&
            (total_threads < max_threads))
        {
            createNewThread();
        }
        else if (logger.isDebugEnabled())
        {
            logger.debug
            (
                "(Pool)[" + idle_threads + "/" + total_threads +
                "] no idle threads but maximum number of threads reached (" +
                max_threads + ")"
            );
        }
    }

    private void createNewThread()
    {
        if (logger.isDebugEnabled())
        {
            logger.debug("[" + idle_threads + "/" + total_threads +
                         "] creating new thread" );
        }

        Thread thread = new Thread( new ConsumerTie( this, factory.create() ));
        thread.setName(namePrefix + (threadCount++));
        thread.setDaemon( true );
        thread.start();

        total_threads++;
    }


    /**
     * <code>getLogger</code> returns the threadpools logger.
     *
     * @return a <code>Logger</code> value
     */
    Logger getLogger ()
    {
        return logger;
    }


    /**
     * <code>shutdown</code> will shutdown the pool.
     */
    public synchronized void shutdown()
    {
        if (logger.isDebugEnabled())
        {
            logger.debug("[" + idle_threads + "/" + total_threads +
                         "] shutting down pool" );
        }

        shutdown = true;
        notifyAll();
    }
} // ThreadPool
