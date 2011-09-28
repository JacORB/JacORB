package org.jacorb.util;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 2011 Gerald Brose
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

import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Iterator;
import org.jacorb.config.*;
import org.slf4j.Logger;


/**
 * Defines a single thread with a queue enabling notification of timer
 * expiration. The priniciple is to have a time ordered list of notification
 * objects. The thread waits until the next closest expiration time in the
 * queue. The thread is interrupted whenever a new entry is queued. Whenever
 * awaken, either by interruption or expiration, the thread notifies all
 * expired waiters, removing them from the queue. The timer queue entries
 * consist of an absolute expiry time, and an action object typically the
 * action will be to wake another thread, but it could do something more
 * specialized. The specialized action should not be blocking or it may
 * adversely affect the performance of the timer queue.
 *
 * @author Phil Mesnier <mesnier_p@ociweb.com>
 * @version $Id$
 */
public class TimerQueue extends Thread
{
    private SortedMap<Long, TimerQueueAction> pending;
    private boolean running;
    protected Logger logger;

    public TimerQueue ()
    {
        pending = new TreeMap<Long, TimerQueueAction>();
        running = true;
    }

    public void configure(Configuration configuration)
    throws ConfigurationException
    {
        org.jacorb.config.Configuration jacorbConfiguration =
            (org.jacorb.config.Configuration) configuration;

        logger = jacorbConfiguration.getLogger("jacorb.util");
    }

    public void halt ()
    {
        synchronized (pending)
        {
            running = false;
            if (logger.isInfoEnabled())
                logger.info ("Timer Queue halted");
            pending.notifyAll();
        }
    }

    public void add (TimerQueueAction a)
    {
        if (a == null)
            return;
        synchronized (pending)
        {
            if (logger.isDebugEnabled())
                logger.debug ("Timer Queue adding action");
            pending.put (a.trigger, a);
            pending.notifyAll();
        }
    }

    public void remove (TimerQueueAction a)
    {
        if (a == null)
            return;
        synchronized (pending)
        {
            if (logger.isDebugEnabled())
                logger.debug ("Timer Queue removing action");
            if (pending.remove (a.trigger) == a)
                pending.notifyAll();
            // what if remove returns non-null but not a?
        }
    }

    public int depth ()
    {
        synchronized (pending)
        {
            return pending.size();
        }
    }

    private long waitTime ()
    {
        // no synch needed, only called while already synchronized
        if ( pending.size() == 0)
            return 0;
        else
        {
            // get smallest wait time
            long next = pending.firstKey();
            return next - System.currentTimeMillis();
        }
    }

    private void triggerExpired ()
    {
        if (!running || pending.size() == 0)
            return;
        // no synch needed, only called while already synchronized
        Long onemilli = new Long(System.currentTimeMillis() + 1);

        SortedMap<Long, TimerQueueAction> exp = pending.headMap(onemilli);
        for (Iterator<TimerQueueAction> iter = exp.values().iterator();
                iter.hasNext(); )
        {
            TimerQueueAction a = iter.next();
            pending.remove (a.trigger);
            a.expire();
        }
    }

    public void run ()
    {
        if (logger.isInfoEnabled())
            logger.info ("Timer Queue starting");

        while (running)
        {
            synchronized (pending)
            {
                long delay = waitTime();
                try
                {
                    if (delay == 0)
                    {
                        pending.wait();
                    }
                    else
                    {
                        pending.wait(delay);
                    }
                }
                catch (InterruptedException ex)
                {
                    // no worries
                    if (logger.isDebugEnabled())
                        logger.debug("TimerQueue interrupted");
                }
                triggerExpired();
            }
        }
    }

}
