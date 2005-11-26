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
        /*
         * This tells the newly idle thread to exit,
         * because there are already too much idle
         * threads.
         */
        if (idle_threads >= max_idle_threads)
        {
            total_threads--;
            return null;
        }

        idle_threads++;

        while( job_queue.isEmpty() )
        {
            try
            {
                wait();
            }
            catch( InterruptedException e )
            {
            }
        }

        return job_queue.removeFirst();
    }

    public synchronized void putJob( Object job )
    {
        job_queue.add(job);
        notifyAll();

        // yes it is possible to have a negative value, look further
        if(idle_threads <= 0)
        {
            if(total_threads < max_threads)
            {
                createNewThread();

                // reserve the new thread for this job, not idle anymore
                // can become negative, will be immediately reincremented in getJob()
                idle_threads--;
            }
        } 
        else
        {
            // reserve a thread for this job, not idle anymore
            // can become negative, will be immediately reincremented in getJob()
            idle_threads--;
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
