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

import org.jacorb.notification.interfaces.Message;

import EDU.oswego.cs.dl.util.concurrent.ReadWriteLock;
import EDU.oswego.cs.dl.util.concurrent.WriterPreferenceReadWriteLock;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */
public class RWLockEventQueueDecorator implements MessageQueueAdapter
{
    /**
     * lock variable used to control access to the reference to the pending messages queue.
     */
    private final ReadWriteLock delegateLock_ = new WriterPreferenceReadWriteLock();

    /**
     * multithreaded access to this member is protected by pendingMessagesLock_
     */
    private MessageQueueAdapter delegate_;

    /**
     * 
     */
    public RWLockEventQueueDecorator(MessageQueueAdapter initialDelegate)
            throws InterruptedException
    {
        super();

        delegateLock_.writeLock().acquire();

        try
        {
            delegate_ = initialDelegate;
        } finally
        {
            delegateLock_.writeLock().release();
        }
    }

    public void replaceDelegate(MessageQueueAdapter newDelegate) throws InterruptedException
    {
        delegateLock_.writeLock().acquire();

        try
        {
            if (delegate_.hasPendingMessages())
            {
                Message[] _allMessages = delegate_.getAllMessages();
                for (int x = 0; x < _allMessages.length; ++x)
                {
                    newDelegate.enqeue(_allMessages[x]);
                }
            }
            delegate_ = newDelegate;
        } finally
        {
            delegateLock_.writeLock().release();
        }
    }

    public void enqeue(Message message) throws InterruptedException
    {
        delegateLock_.readLock().acquire();

        try
        {
            delegate_.enqeue(message);
        } finally
        {
            delegateLock_.readLock().release();
        }
    }

    public boolean hasPendingMessages() throws InterruptedException
    {
        delegateLock_.readLock().acquire();

        try
        {
            return delegate_.hasPendingMessages();
        } finally
        {
            delegateLock_.readLock().release();
        }
    }

    public int getPendingMessagesCount() throws InterruptedException
    {
        delegateLock_.readLock().acquire();

        try
        {
            return delegate_.getPendingMessagesCount();
        } finally
        {
            delegateLock_.readLock().release();
        }
    }

    public Message getMessageBlocking() throws InterruptedException
    {
        delegateLock_.readLock().acquire();

        try
        {
            return delegate_.getMessageBlocking();
        } finally
        {
            delegateLock_.readLock().release();
        }
    }

    public Message getMessageNoBlock() throws InterruptedException
    {
        delegateLock_.readLock().acquire();

        try
        {
            return delegate_.getMessageNoBlock();
        } finally
        {
            delegateLock_.readLock().release();
        }
    }

    public Message[] getAllMessages() throws InterruptedException
    {
        delegateLock_.readLock().acquire();

        try
        {
            return delegate_.getAllMessages();
        } finally
        {
            delegateLock_.readLock().release();
        }
    }

    public Message[] getUpToMessages(int max) throws InterruptedException
    {
        delegateLock_.readLock().acquire();

        try
        {
            return delegate_.getUpToMessages(max);
        } finally
        {
            delegateLock_.readLock().release();
        }
    }

    public Message[] getAtLeastMessages(int min) throws InterruptedException
    {
        delegateLock_.readLock().acquire();

        try
        {
            return delegate_.getAtLeastMessages(min);
        } finally
        {
            delegateLock_.readLock().release();
        }
    }

    public void clear()
    {
        try
        {
            delegateLock_.writeLock().acquire();
        } catch (InterruptedException e)
        {
            // ignore exception. force dispose.
        }

        try
        {
            delegate_.clear();
        } finally
        {
            delegateLock_.writeLock().release();
        }
    }
    
    public String toString()
    {
        return delegate_.toString();
    }
}