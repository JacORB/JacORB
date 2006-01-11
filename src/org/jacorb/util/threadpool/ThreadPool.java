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

/**
 * ThreadPool.java
 *
 *
 * Created: Fri Jun  9 15:09:01 2000
 *
 * @author Nicolas Noffke
 * $Id$
 */
public class ThreadPool
{
    private final int max_threads;
    private final int max_idle_threads;

    private int total_threads = 0;
    private int idle_threads = 0;

    private final LinkedList job_queue;
    private final ConsumerFactory factory;

    public ThreadPool( ConsumerFactory factory )
    {
        this( new LinkedList (),
              factory,
              10,
              10 );
    }

    public ThreadPool( ConsumerFactory factory,
                       int max_threads,
                       int max_idle_threads)
    {
        this
        (
            new LinkedList (),
            factory,
            max_threads,
            max_idle_threads
        );
    }

    private ThreadPool( LinkedList job_queue,
                        ConsumerFactory factory,
                        int max_threads,
                        int max_idle_threads)
    {
        this.job_queue = job_queue;
        this.factory = factory;
        this.max_threads = max_threads;
        this.max_idle_threads = max_idle_threads;
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
        while( job_queue.isEmpty() )
        {
            /*
             * This tells the newly idle thread to exit, because
             * there are already too much idle threads.
             */
            if (idle_threads > max_idle_threads)
            {
                total_threads--;
                idle_threads--;
                return null;
            }

            try
            {
                wait();
            }
            catch( InterruptedException e )
            {
            }
        }

        idle_threads--;

        return job_queue.removeFirst();
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
    }

    private void createNewThread()
    {
        Thread t = 
            new Thread( new ConsumerTie( this, factory.create() ));
        t.setDaemon( true );
        t.start();

        total_threads++;
    }
} // ThreadPool
