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
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.jacorb.notification.interfaces.Message;

import edu.emory.mathcs.backport.java.util.PriorityQueue;

/**
 * Note that most of the methods are not thread-safe. this causes no problem as 
 * the methods are not intended to be directly called by clients. instead the superclass
 * implements the interface EventQueue and invokes the methods thereby synchronizing access.
 * 
 * @author Alphonse Bendt
 * @version $Id$
 */

public class BoundedDeadlineEventQueue extends AbstractBoundedEventQueue
{
    private final PriorityQueue heap_;

    private long counter_ = 0;

    ////////////////////////////////////////

    public BoundedDeadlineEventQueue( int maxSize,
                                      EventQueueOverflowStrategy overflowStrategy )
    {
        super(maxSize, overflowStrategy, new Object());
        
        heap_ = new PriorityQueue(maxSize, QueueUtil.ASCENDING_TIMEOUT_COMPARATOR );
    }

    ////////////////////////////////////////

    public String getOrderPolicyName()
    {
        return "DeadlineOrder";
    }
    
    protected Message getNextElement()
    {
        return getEarliestTimeout();
    }

    protected Message getOldestElement()
    {
        return removeFirstElement(QueueUtil.ASCENDING_AGE_COMPARATOR );
    }
    
    private Message removeFirstElement(Comparator comp)
    {
        List _entries = copyAllEntries();

        Collections.sort( _entries, comp );

        HeapEntry _entry = ( HeapEntry ) _entries.get( 0 );

        heap_.remove(_entry);
        
        return _entry.event_;
    }


    protected Message getYoungestElement()
    {
        return removeFirstElement( QueueUtil.DESCENDING_AGE_COMPARATOR );
    }


    protected Message getEarliestTimeout()
    {
        return ( ( HeapEntry ) heap_.remove() ).event_;
    }


    protected Message getLeastPriority()
    {
        return removeFirstElement(QueueUtil.ASCENDING_PRIORITY_COMPARATOR );
    }


    protected Message[] getElements( int max )
    {
        List _events = new ArrayList();
        Object _element;

        while ( ( _events.size() < max ) && ( _element = heap_.remove() ) != null )
        {
            _events.add( ( ( HeapEntry ) _element ).event_ );
        }

        return ( Message[] )
               _events.toArray( QueueUtil.MESSAGE_ARRAY_TEMPLATE );
    }


    protected void addElement( Message event )
    {
        heap_.add( new HeapEntry( event, counter_++ ) );
    }


    private List copyAllEntries()
    {
        List _events = new ArrayList(heap_.size());

        _events.addAll(heap_);

        return _events;
    }
    
    
    private List removeAllEntries()
    {
        List _entries = copyAllEntries();
        
        heap_.clear();
        
        return _entries;
    }


    protected Message[] getAllElements()
    {
        List _all = removeAllEntries();

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
