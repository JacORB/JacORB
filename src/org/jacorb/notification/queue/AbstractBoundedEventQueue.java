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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jacorb.notification.interfaces.Message;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */

public abstract class AbstractBoundedEventQueue implements MessageQueue
{
    private final Object lock_;
    private final int capacity_;
    private final List listeners_ = new ArrayList();
    private final EventQueueOverflowStrategy overflowStrategy_;

    protected AbstractBoundedEventQueue(int capacity, EventQueueOverflowStrategy overflowStrategy,
            Object lock)
    {
        lock_ = lock;
        capacity_ = capacity;
        overflowStrategy_ = overflowStrategy;
    }

    public final String getDiscardPolicyName()
    {
        return overflowStrategy_.getDiscardPolicyName();
    }
    
    protected abstract Message getEarliestTimeout();

    protected abstract Message getLeastPriority();

    protected abstract Message getNextElement();

    protected abstract Message getOldestElement();

    protected abstract Message getYoungestElement();

    protected abstract Message[] getElements(int max);

    protected abstract void addElement(Message message);

    protected abstract Message[] getAllElements();

    public abstract String getOrderPolicyName();
    
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

        return getNextElement();
    }

    public void put(Message event)
    {
        synchronized (lock_)
        {
            while (getSize() >= capacity_)
            {
                overflowStrategy_.removeElementFromQueue(this);
                
                fireMessageDiscarded();
            }

            addElement(event);

            lock_.notifyAll();
        }
    }
    
    private void fireMessageDiscarded()
    {
        final Iterator i = listeners_.iterator();
        
        while(i.hasNext())
        {
            ((DiscardListener)i.next()).messageDiscarded(capacity_);
        }
    }
    
    public void addDiscardListener(DiscardListener listener)
    {
        listeners_.add(listener);
    }
    
    public void removeDiscardListener(DiscardListener listener)
    {
        listeners_.remove(listener);
    }
}