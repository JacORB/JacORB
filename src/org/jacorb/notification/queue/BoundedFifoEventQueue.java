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

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.jacorb.notification.interfaces.Message;

/**
 * Note that most of the methods are not thread-safe. this causes no problem as 
 * the methods are not intended to be directly called by clients. instead the superclass
 * implements the interface EventQueue and invokes the methods thereby synchronizing access.
*
 * @author Alphonse Bendt
 * @version $Id$
 */

public class BoundedFifoEventQueue extends AbstractBoundedEventQueue
{
    private final LinkedList linkedList_;
    
    public BoundedFifoEventQueue( int maxSize, EventQueueOverflowStrategy overflowStrategy)
    {
        this(maxSize, overflowStrategy, new Object());
    }
    
    public BoundedFifoEventQueue( int maxSize, EventQueueOverflowStrategy overflowStrategy, Object lock)
    {
        super( maxSize, overflowStrategy, lock);
        
        linkedList_ = new LinkedList();
    }
 
    public boolean isEmpty()
    {
        return linkedList_.isEmpty();
    }

    public int getSize()
    {
        return linkedList_.size();
    }

    protected Message getEarliestTimeout()
    {
        List _sorted = ( List ) linkedList_.clone();

        Collections.sort( _sorted, QueueUtil.ASCENDING_TIMEOUT_COMPARATOR );

        Message _event = ( Message ) _sorted.get( 0 );

        linkedList_.remove( _event );

        return _event;
    }

    protected Message getLeastPriority()
    {
        List _sorted = ( List ) linkedList_.clone();

        Collections.sort( _sorted, QueueUtil.ASCENDING_PRIORITY_COMPARATOR );

        Message _event = ( Message ) _sorted.get( 0 );

        linkedList_.remove( _event );

        return _event;
    }

    protected Message getNextElement()
    {
        return getOldestElement();
    }

    protected Message getOldestElement()
    {
        return ( Message ) linkedList_.removeFirst();
    }

    protected Message getYoungestElement()
    {
        return ( Message ) linkedList_.removeLast();
    }

    protected Message[] getAllElements()
    {
        return ( Message[] ) linkedList_.toArray( QueueUtil.MESSAGE_ARRAY_TEMPLATE );
    }

    protected void addElement( Message e )
    {
        linkedList_.add( e );
    }

    protected Iterator getListIterator()
    {
        return linkedList_.iterator();
    }

    protected void removeElement( Message e )
    {
        linkedList_.remove( e );
    }

    protected Message[] getElements( int max )
    {
        int _retSize = ( max > linkedList_.size() ) ? linkedList_.size() : max;

        Message[] _ret = new Message[ _retSize ];

        for ( int x = 0; x < _retSize; ++x )
        {
            _ret[ x ] = ( Message ) linkedList_.removeFirst();
        }

        return _ret;
    }
}
