package org.jacorb.notification.queue;

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

import org.jacorb.notification.interfaces.Message;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */

abstract public class AbstractBoundedEventQueue implements MessageQueue
{
    private final Object lock_;

    private final int capacity_;

    private final EventQueueOverflowStrategy overflowStrategy_;

    protected AbstractBoundedEventQueue(int capacity, EventQueueOverflowStrategy overflowStrategy,
            Object lock)
    {
        lock_ = lock;
        capacity_ = capacity;
        overflowStrategy_ = overflowStrategy;
    }

    abstract protected Message getEarliestTimeout();

    abstract protected Message getLeastPriority();

    abstract protected Message getNextElement();

    abstract protected Message getOldestElement();

    abstract protected Message getYoungestElement();

    abstract protected Message[] getElements(int max);

    abstract protected void addElement(Message event);

    abstract protected Message[] getAllElements();

    public Message[] getAllMessages(boolean wait) throws InterruptedException
    {
        synchronized (lock_)
        {
            if (wait)
            {
                return getAllBlocking();
            }

            return getAllElements();
        }
    }

    /**
     * @pre current thread own monitor lock_
     */
    private Message[] getAllBlocking() throws InterruptedException
    {
        while (isEmpty())
        {
            lock_.wait();
        }

        return getAllElements();
    }

    public Message getMessage(boolean wait) throws InterruptedException
    {
        synchronized (lock_)
        {
            if (wait)
            {
                return getEventBlocking();
            }

            if (isEmpty())
            {
                return null;
            }

            return getNextElement();
        }
    }

    public Message[] getMessages(int max, boolean wait) throws InterruptedException
    {
        synchronized (lock_)
        {
            if (wait)
            {
                return getEventsBlocking(max);
            }

            return getElements(max);
        }
    }

    /**
     * @pre current thread owns monitor lock_
     */
    private Message[] getEventsBlocking(int max) throws InterruptedException
    {
        while (isEmpty())
        {
            lock_.wait();
        }

        return getElements(max);
    }

    /**
     * @pre current thread owns monitor lock_
     */
    private Message getEventBlocking() throws InterruptedException
    {
        while (isEmpty())
        {
            lock_.wait();
        }

        return getOldestElement();
    }

    public void put(Message event)
    {
        synchronized (lock_)
        {
            while (getSize() >= capacity_)
            {
                overflowStrategy_.removeElementFromQueue(this);
            }

            addElement(event);

            lock_.notifyAll();
        }
    }
}