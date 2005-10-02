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
 * Note that the methods do not need to be thread-safe. this causes no problem as the methods are not
 * intended to be directly called by clients. instead the superclass implements the interface
 * EventQueue and invokes the methods thereby synchronizing access.
 * 
 * @author Alphonse Bendt
 * @version $Id$
 */

public class BoundedPriorityEventQueue extends AbstractBoundedEventQueue
{
    private final PriorityQueue heap_;

    private long counter_ = 0;

    private final int maxCapacity_;

    // //////////////////////////////////////

    public BoundedPriorityEventQueue(int maxSize, EventQueueOverflowStrategy overflowStrategy)
    {
        super(maxSize, overflowStrategy, new Object());

        maxCapacity_ = maxSize;

        heap_ = new PriorityQueue(maxCapacity_, QueueUtil.DESCENDING_PRIORITY_COMPARATOR);
    }

    // //////////////////////////////////////

    public String getOrderPolicyName()
    {
        return "PriorityOrder";
    }

    protected Message getNextElement()
    {
        return ((HeapEntry) heap_.remove()).event_;
    }

    protected Message getEarliestTimeout()
    {
        return removeFirstElement(QueueUtil.ASCENDING_TIMEOUT_COMPARATOR);
    }

    protected Message getOldestElement()
    {
        return removeFirstElement(QueueUtil.ASCENDING_AGE_COMPARATOR);
    }

    protected Message getYoungestElement()
    {
        return removeFirstElement(QueueUtil.DESCENDING_AGE_COMPARATOR);
    }

    protected Message getLeastPriority()
    {
        return removeFirstElement(QueueUtil.ASCENDING_PRIORITY_COMPARATOR);
    }

    protected Message[] getElements(int max)
    {
        List _events = new ArrayList();

        while ((heap_.peek()) != null && (_events.size() <= max))
        {
            _events.add(((HeapEntry) heap_.remove()).event_);
        }

        return (Message[]) _events.toArray(QueueUtil.MESSAGE_ARRAY_TEMPLATE);
    }

    protected void addElement(Message event)
    {
        heap_.add(new HeapEntry(event, counter_++));
    }

    private List removeAllEntries()
    {
        List _entries = copyAllEntries();
        
        heap_.clear();

        return _entries;
    }

    protected Message[] getAllElements()
    {
        List _entries = removeAllEntries();

        Message[] _messages = new Message[_entries.size()];

        Iterator i = _entries.iterator();

        int x = 0;
        while (i.hasNext())
        {
            HeapEntry e = (HeapEntry) i.next();
            _messages[x++] = e.event_;
        }

        return _messages;
    }

    private Message removeFirstElement(Comparator comp)
    {
        List _entries = copyAllEntries();

        Collections.sort(_entries, comp);

        HeapEntry _entry = (HeapEntry) _entries.get(0);

        heap_.remove(_entry);

        return _entry.event_;
    }

    private List copyAllEntries()
    {
        List _entries = new ArrayList(heap_.size());

        _entries.addAll(heap_);

        return _entries;
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
