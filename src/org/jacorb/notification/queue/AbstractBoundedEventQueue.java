package org.jacorb.notification.queue;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2003 Gerald Brose
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

abstract public class AbstractBoundedEventQueue implements EventQueue
{
    private Object lock_ = new Object();

    private int capacity_;

    private EventQueueOverflowStrategy overflowStrategy_;

    protected AbstractBoundedEventQueue( int capacity,
                                         EventQueueOverflowStrategy overflowStrategy )
    {
        capacity_ = capacity;
        overflowStrategy_ = overflowStrategy;
    }

    protected AbstractBoundedEventQueue( int capacity )
    {
        capacity_ = capacity;
    }

    void setOverflowStrategy( EventQueueOverflowStrategy overflowStrategy )
    {
        overflowStrategy_ = overflowStrategy;
    }

    abstract protected Message getEarliestTimeout();

    abstract protected Message getLeastPriority();

    abstract protected Message getNextElement();

    abstract protected Message getOldestElement();

    abstract protected Message getYoungestElement();

    abstract protected Message[] getElements( int max );

    abstract protected void addElement( Message event );

    abstract protected Message[] getAllElements();

    private void fireEventDiscarded( Message event )
    {}

    public Message[] getAllEvents( boolean wait ) throws InterruptedException
    {
        if ( wait )
        {
            return getAllBlocking();
        }
        else
        {
            return getAllElements();
        }
    }

    private Message[] getAllBlocking() throws InterruptedException
    {
        synchronized ( lock_ )
        {
            while ( isEmpty() )
            {
                lock_.wait();
            }

            return getAllElements();
        }
    }

    public Message getEvent( boolean wait ) throws InterruptedException
    {
        if ( wait )
        {
            return getEventBlocking();
        }
        else
        {
            if ( isEmpty() )
            {
                return null;
            }
            else
            {
                return getNextElement();
            }
        }
    }

    public Message[] getEvents( int max, boolean wait ) throws InterruptedException
    {
        if ( wait )
        {
            return getEventsBlocking( max );
        }
        else
        {
            return getElements( max );
        }
    }

    private Message[] getEventsBlocking( int max ) throws InterruptedException
    {
        synchronized ( lock_ )
        {
            while ( isEmpty() )
            {
                lock_.wait();
            }

            return getElements( max );
        }
    }

    private Message getEventBlocking() throws InterruptedException
    {
        synchronized ( lock_ )
        {
            while ( isEmpty() )
            {
                lock_.wait();
            }

            return getOldestElement();
        }
    }

    public void put( Message event )
    {
        synchronized ( lock_ )
        {
            while ( getSize() >= capacity_ )
            {
                Message _e = overflowStrategy_.removeElementFromQueue( this );
                fireEventDiscarded( _e );
            }

            addElement( event );

            lock_.notifyAll();
        }
    }
}
