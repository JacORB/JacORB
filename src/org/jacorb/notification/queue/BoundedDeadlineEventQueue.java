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

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.jacorb.notification.interfaces.Message;
import org.jacorb.util.Debug;

import EDU.oswego.cs.dl.util.concurrent.Heap;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */

public class BoundedDeadlineEventQueue extends AbstractBoundedEventQueue
{

    private Heap heap_;
    private long counter_ = 0;

    public BoundedDeadlineEventQueue( int maxSize,
                                      EventQueueOverflowStrategy overflowStrategy )
    {
        super( maxSize, overflowStrategy );
        heap_ = new Heap( maxSize, QueueUtil.ASCENDING_TIMEOUT_COMPARATOR );
    }

    BoundedDeadlineEventQueue( int maxSize )
    {
        super( maxSize );
        heap_ = new Heap( maxSize, QueueUtil.ASCENDING_TIMEOUT_COMPARATOR );
    }

    protected Message getNextElement()
    {
        return getEarliestTimeout();
    }

    protected Message getOldestElement()
    {
        List _all = getAllElementsInternal();

        Collections.sort( _all, QueueUtil.ASCENDING_AGE_COMPARATOR );

        HeapEntry _oldest = ( HeapEntry ) _all.remove( 0 );

        Heap _newHeap = new Heap( _all.size(), QueueUtil.ASCENDING_TIMEOUT_COMPARATOR );

        Iterator i = _all.iterator();

        while ( i.hasNext() )
        {
            HeapEntry e = ( HeapEntry ) i.next();
            _newHeap.insert( e );
        }

        return _oldest.event_;
    }

    protected Message getYoungestElement()
    {
        List _all = getAllElementsInternal();

        Collections.sort( _all, QueueUtil.DESCENDING_AGE_COMPARATOR );

        HeapEntry _youngest = ( HeapEntry ) _all.remove( 0 );

        Heap _newHeap = new Heap( _all.size(), QueueUtil.ASCENDING_TIMEOUT_COMPARATOR );

        Iterator i = _all.iterator();

        while ( i.hasNext() )
        {
            HeapEntry e = ( HeapEntry ) i.next();
            _newHeap.insert( e );
        }

        heap_ = _newHeap;

        return _youngest.event_;
    }

    protected Message getEarliestTimeout()
    {
        return ( ( HeapEntry ) heap_.extract() ).event_;
    }

    protected Message getLeastPriority()
    {
        List _all = getAllElementsInternal();

        Collections.sort( _all, QueueUtil.ASCENDING_PRIORITY_COMPARATOR );

        HeapEntry _leastPriority = ( HeapEntry ) _all.remove( 0 );

        Heap _newHeap = new Heap( _all.size(), QueueUtil.ASCENDING_TIMEOUT_COMPARATOR );

        Iterator i = _all.iterator();

        while ( i.hasNext() )
        {
            HeapEntry e = ( HeapEntry ) i.next();
            _newHeap.insert( e );
        }

        heap_ = _newHeap;

        return _leastPriority.event_;
    }

    protected Message[] getElements( int max )
    {
        List _events = new Vector();
        Object _element;

        while ( ( _element = heap_.extract() ) != null && ( _events.size() <= max ) )
        {
            _events.add( ( ( HeapEntry ) _element ).event_ );
        }

        return ( Message[] )
               _events.toArray( QueueUtil.NOTIFICATION_EVENT_ARRAY_TEMPLATE );
    }

    protected void addElement( Message event )
    {
        heap_.insert( new HeapEntry( event, counter_++ ) );
    }

    private List getAllElementsInternal()
    {

        Vector _events = new Vector();
        Object _element;

        while ( ( _element = heap_.extract() ) != null )
        {
            _events.add( _element );
        }

        return _events;

    }

    protected Message[] getAllElements()
    {
        List _all = getAllElementsInternal();

        Message[] _ret = new Message[ _all.size() ];

        Iterator i = _all.iterator();

        int x = 0;

        while ( i.hasNext() )
        {
            HeapEntry e = ( HeapEntry ) i.next();
            _ret[ x++ ] = e.event_;
        }

        return _ret;
    }

    public boolean isEmpty()
    {
        return ( getSize() == 0 );
    }

    public int getSize()
    {
        return heap_.size();
    }
}
