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
import java.util.Iterator;
import java.util.List;

import org.jacorb.notification.interfaces.Message;

import EDU.oswego.cs.dl.util.concurrent.Heap;

/**
 * Note that most of the methods are not thread-safe. this causes no problem as 
 * the methods are not intended to be directly called by clients. instead the superclass
 * implements the interface EventQueue and invokes the methods thereby synchronizing access.

 * @author Alphonse Bendt
 * @version $Id$
 */

public class BoundedPriorityEventQueue extends AbstractBoundedEventQueue
{
    private Heap heap_;

    private long counter_ = 0;
    
    private final int maxCapacity_;

    ////////////////////////////////////////

    public BoundedPriorityEventQueue(int maxSize,
                                     EventQueueOverflowStrategy overflowStrategy)
    {
        super(maxSize, overflowStrategy, new Object());

        maxCapacity_ = maxSize;
        
        heap_ = newHeap();
    }

    ////////////////////////////////////////

    private Heap newHeap()
    {
        return new Heap2(maxCapacity_, QueueUtil.DESCENDING_PRIORITY_COMPARATOR);
    }

    protected Message getNextElement()
    {
        return ((HeapEntry)heap_.extract()).event_;
    }


    protected Message getEarliestTimeout()
    {
        List _all = getAllElementsInternal();

        Collections.sort(_all, QueueUtil.ASCENDING_TIMEOUT_COMPARATOR);

        HeapEntry _earliest = (HeapEntry)_all.remove(0);

        Heap _newHeap = newHeap();
        
        Iterator i = _all.iterator();

        while (i.hasNext())
        {
            HeapEntry e = (HeapEntry)i.next();
            _newHeap.insert(e);
        }

        heap_ = _newHeap;

        return _earliest.event_;
    }


    protected Message getOldestElement()
    {
        List _all = getAllElementsInternal();

        Collections.sort(_all, QueueUtil.ASCENDING_AGE_COMPARATOR);

        HeapEntry _oldest = (HeapEntry)_all.remove(0);

        Heap _newHeap = newHeap();
        
        Iterator i = _all.iterator();

        while (i.hasNext())
        {
            HeapEntry e = (HeapEntry)i.next();
            _newHeap.insert(e);
        }

        return _oldest.event_;
    }


    protected Message getYoungestElement()
    {
        List _all = getAllElementsInternal();

        Collections.sort(_all, QueueUtil.DESCENDING_AGE_COMPARATOR);

        HeapEntry _youngest = (HeapEntry)_all.remove(0);

        Heap _newHeap = newHeap();
        
        Iterator i = _all.iterator();

        while (i.hasNext())
        {
            HeapEntry e = (HeapEntry)i.next();
            _newHeap.insert(e);
        }

        heap_ = _newHeap;

        return _youngest.event_;
    }


    protected Message getLeastPriority()
    {
        List _all = getAllElementsInternal();

        Collections.sort(_all, QueueUtil.ASCENDING_PRIORITY_COMPARATOR);

        HeapEntry _leastPriority = (HeapEntry)_all.remove(0);

        Heap _newHeap = newHeap();
        
        Iterator i = _all.iterator();

        while (i.hasNext())
        {
            HeapEntry e = (HeapEntry)i.next();
            _newHeap.insert(e);
        }

        heap_ = _newHeap;

        return _leastPriority.event_;
    }


    protected Message[] getElements(int max)
    {
        List _events = new ArrayList();
        Object _element;

        while ((_element = heap_.extract()) != null && (_events.size() <= max))
        {
            _events.add(((HeapEntry)_element).event_);
        }

        return (Message[])
               _events.toArray(QueueUtil.MESSAGE_ARRAY_TEMPLATE);
    }


    protected void addElement(Message event)
    {
        heap_.insert(new HeapEntry(event, counter_++));
    }


    private List getAllElementsInternal()
    {
        List _events = new ArrayList();
        Object _element;

        while ((_element = heap_.extract()) != null)
        {
            _events.add(_element);
        }

        heap_.clear();
        
        return _events;
    }


    protected Message[] getAllElements()
    {
        List _all = getAllElementsInternal();

        Message[] _ret = new Message[_all.size()];

        Iterator i = _all.iterator();

        int x = 0;
        while (i.hasNext())
        {
            HeapEntry e = (HeapEntry)i.next();
            _ret[x++] = e.event_;
        }

        return _ret;
    }


    public boolean isEmpty()
    {
        return (getSize() == 0);
    }


    public int getSize()
    {
        return heap_.size();
    }
    
    public String toString()
    {
        return heap_.toString();
    }
}
