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

package org.jacorb.notification.queue;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jacorb.notification.interfaces.Message;
import org.jacorb.notification.queue.MessageQueue.DiscardListener;

import edu.emory.mathcs.backport.java.util.concurrent.locks.ReadWriteLock;
import edu.emory.mathcs.backport.java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */
public class RWLockEventQueueDecorator implements MessageQueueAdapter
{
    private final List listeners_ = new ArrayList();

    /**
     * lock variable used to control access to the reference to the pending messages queue.
     */
    private final ReadWriteLock delegateLock_ = new ReentrantReadWriteLock();

    /**
     * multithreaded access to this member is protected by delegateLock__
     */
    private MessageQueueAdapter delegate_;

    /**
     * 
     */
    public RWLockEventQueueDecorator(MessageQueueAdapter initialDelegate)
    {
        super();

        delegateLock_.writeLock().lock();

        try
        {
            delegate_ = initialDelegate;
        } finally
        {
            delegateLock_.writeLock().unlock();
        }
    }

    public void replaceDelegate(MessageQueueAdapter newDelegate) throws InterruptedException
    {
        delegateLock_.writeLock().lock();

        try
        {
            for (final Iterator i = listeners_.iterator(); i.hasNext();)
            {
                delegate_.removeDiscardListener((DiscardListener) i.next());
            }
            
            if (delegate_.hasPendingMessages())
            {
                final Message[] _allMessages = delegate_.getAllMessages();
                for (int x = 0; x < _allMessages.length; ++x)
                {
                    newDelegate.enqeue(_allMessages[x]);
                }
            }
            delegate_ = newDelegate;
            
            for (Iterator i = listeners_.iterator(); i.hasNext();)
            {
                delegate_.addDiscardListener((DiscardListener) i.next());
            }
        } finally
        {
            delegateLock_.writeLock().unlock();
        }
    }

    public void enqeue(Message message) throws InterruptedException
    {
        delegateLock_.readLock().lock();

        try
        {
            delegate_.enqeue(message);
        } finally
        {
            delegateLock_.readLock().unlock();
        }
    }

    public boolean hasPendingMessages() throws InterruptedException
    {
        delegateLock_.readLock().lock();

        try
        {
            return delegate_.hasPendingMessages();
        } finally
        {
            delegateLock_.readLock().unlock();
        }
    }

    public int getPendingMessagesCount() throws InterruptedException
    {
        delegateLock_.readLock().lock();

        try
        {
            return delegate_.getPendingMessagesCount();
        } finally
        {
            delegateLock_.readLock().unlock();
        }
    }

    public Message getMessageBlocking() throws InterruptedException
    {
        delegateLock_.readLock().lock();

        try
        {
            return delegate_.getMessageBlocking();
        } finally
        {
            delegateLock_.readLock().unlock();
        }
    }

    public Message getMessageNoBlock() throws InterruptedException
    {
        delegateLock_.readLock().lock();

        try
        {
            return delegate_.getMessageNoBlock();
        } finally
        {
            delegateLock_.readLock().unlock();
        }
    }

    public Message[] getAllMessages() throws InterruptedException
    {
        delegateLock_.readLock().lock();

        try
        {
            return delegate_.getAllMessages();
        } finally
        {
            delegateLock_.readLock().unlock();
        }
    }

    public Message[] getUpToMessages(int max) throws InterruptedException
    {
        delegateLock_.readLock().lock();

        try
        {
            return delegate_.getUpToMessages(max);
        } finally
        {
            delegateLock_.readLock().unlock();
        }
    }

    public Message[] getAtLeastMessages(int min) throws InterruptedException
    {
        delegateLock_.readLock().lock();

        try
        {
            return delegate_.getAtLeastMessages(min);
        } finally
        {
            delegateLock_.readLock().unlock();
        }
    }

    public void clear()
    {
        delegateLock_.readLock().lock();

        try
        {
            delegate_.clear();
        } finally
        {
            delegateLock_.readLock().unlock();
        }
    }

    public String toString()
    {
        lockReadLock();

        try
        {
            return delegate_.toString();
        } finally
        {
            delegateLock_.readLock().unlock();
        }
    }

    private void lockReadLock()
    {
        delegateLock_.readLock().lock();
    }

    public String getDiscardPolicyName()
    {
        lockReadLock();

        try
        {
            return delegate_.getDiscardPolicyName();
        } finally
        {
            delegateLock_.readLock().unlock();
        }
    }

    public String getOrderPolicyName()
    {
        lockReadLock();

        try
        {
            return delegate_.getOrderPolicyName();
        } finally
        {
            delegateLock_.readLock().unlock();
        }
    }

    public void addDiscardListener(DiscardListener listener)
    {
        lockReadLock();

        try
        {
            listeners_.add(listener);
            delegate_.addDiscardListener(listener);
        } finally
        {
            delegateLock_.readLock().unlock();
        }
    }

    public void removeDiscardListener(DiscardListener listener)
    {
        lockReadLock();

        try
        {
            listeners_.remove(listener);
            delegate_.removeDiscardListener(listener);
        } finally
        {
            delegateLock_.readLock().unlock();
        }
    }
}