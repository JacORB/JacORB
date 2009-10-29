/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2006 Gerald Brose
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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.jacorb.notification.interfaces.Message;

import java.util.PriorityQueue;

public abstract class AbstractBoundedEventHeap extends AbstractBoundedEventQueue
{
    private final PriorityQueue heap_;

    protected AbstractBoundedEventHeap(int capacity, EventQueueOverflowStrategy overflowStrategy, Object lock, Comparator comparator)
    {
        super(capacity, overflowStrategy, lock);

        heap_ = new PriorityQueue(capacity, comparator);
    }

    private final List copyAllEntries()
    {
        final List _events = new ArrayList(heap_.size());

        _events.addAll(heap_);

        return _events;
    }

    private final List removeAllEntries()
    {
        final List _entries = copyAllEntries();

        heap_.clear();

        return _entries;
    }

    protected final Message removeFirstElement(Comparator comp)
    {
        final List _entries = copyAllEntries();
        Collections.sort(_entries, comp);

        final Message _mesg = (Message) _entries.remove(0);

        heap_.clear();
        heap_.addAll(_entries);

        return _mesg;
    }

    protected final Message[] getAllElements()
    {
        final List _entries = removeAllEntries();

        return (Message[]) _entries.toArray(new Message[_entries.size()]);
    }

    public final boolean isEmpty()
    {
        return getSize() == 0;
    }

    public final int getSize()
    {
        return heap_.size();
    }

    protected final void addElement( Message event )
    {
        heap_.add(event);
    }

    protected final Message[] getElements(int max)
    {
        final List _result = new ArrayList();

        while ((heap_.peek()) != null && (_result.size() < max))
        {
            _result.add(heap_.remove());
        }

        return (Message[]) _result.toArray(QueueUtil.MESSAGE_ARRAY_TEMPLATE);
    }

    protected final Message getNextHeapElement()
    {
        return ( Message ) heap_.remove();
    }

    protected final Message getNextElement()
    {
        return getNextHeapElement();
    }

    public final String toString()
    {
        return heap_.toString();
    }
}
