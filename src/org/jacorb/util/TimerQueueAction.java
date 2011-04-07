package org.jacorb.util;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 2002-2004 Gerald Brose
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

import java.util.Calendar;


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
 * @version $ $
 */
public class TimerQueueAction
{
    private Object notifyTarget = null;
    public Calendar trigger = null;

    public TimerQueueAction ()
    {
    }

    public TimerQueueAction ( long relative )
    {
        set_relative (relative);
    }

    public TimerQueueAction ( long relative, Object target  )
    {
        set_relative (relative);
        notifyTarget = target;
    }

    public TimerQueueAction ( Calendar absolute ) 
    {
        trigger = absolute;
    }

    public TimerQueueAction ( Calendar absolute, Object target )
    {
        trigger = absolute;
        notifyTarget = target;
    }

    public void set_relative (long relative)
    {
        trigger = Calendar.getInstance();
        long now = trigger.getTimeInMillis();
        trigger.setTimeInMillis (now + relative);
    }

    public void set_absolute (Calendar absolute)
    {
        trigger = absolute;
    }

    public void expire ()
    {
        // subclass this to do something besides notify a waiter
        if ( notifyTarget != null ) {
            synchronized (notifyTarget) {
                notifyTarget.notifyAll();
            }
        }
    }

    public long expiryTime ()
    {
        return trigger.getTimeInMillis();
    }

} // end of class Action
