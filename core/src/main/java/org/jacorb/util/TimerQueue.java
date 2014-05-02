package org.jacorb.util;

/*
 * JacORB - a free Java ORB
 *
 * Copyright (C) 2012 Gerald Brose / The JacORB Team.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Library General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any
 * later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Library General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Library General Public License
 * along with this library; if not, write to the Free Software Foundation, Inc.,
 * 675 Mass Ave, Cambridge, MA 02139, USA.
 */



/**
 * Defines a single thread with a queue enabling notification of timer
 * expiration. The priniciple is to have a time ordered list of notification
 * objects. The thread waits until the next closest expiration time in the
 * queue. The thread is interrupted whenever a new entry is queued. Whenever
 * awaken, either by interruption or expiration, the thread notifies all expired
 * waiters, removing them from the queue. The timer queue entries consist of an
 * absolute expiry time, and an action object typically the action will be to
 * wake another thread, but it could do something more specialized. The
 * specialized action should not be blocking or it may adversely affect the
 * performance of the timer queue.
 *
 * This is a passthru to the SelectorManager instance and should probably
 * go away
 *
 * @author Phil Mesnier <mesnier_p@ociweb.com>
 */
public class TimerQueue
{
    SelectorManager impl;

    public TimerQueue (SelectorManager sm)
    {
        impl = sm;
    }

    public void halt ()
    {
        impl.halt();
    }


    public void add (TimerQueueAction a)
    {
        impl.add (a);
    }


    public void remove (TimerQueueAction a)
    {
        impl.remove (a);
    }

    public int depth ()
    {
        return impl.poolSize(SelectorRequest.Type.TIMER);
    }

}
