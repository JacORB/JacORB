package org.jacorb.notification;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2004 Gerald Brose.
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
 */

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.omg.CosNotification.EventType;

import EDU.oswego.cs.dl.util.concurrent.FIFOReadWriteLock;
import EDU.oswego.cs.dl.util.concurrent.ReadWriteLock;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */

public abstract class EventTypeSet
{
    private final static EventTypeWrapper[] EVENT_TYPE_WRAPPER_TEMPLATE = new EventTypeWrapper[0];

    private Set eventTypeSet_ = new TreeSet();

    private final ReadWriteLock readWriteLock_ = new FIFOReadWriteLock();

    private final Object arrayViewLock_ = new Object();

    private EventType[] arrayView_ = null;

    private boolean setModified_ = true;

    protected static final EventType[] EMPTY_EVENT_TYPE = new EventType[0];
    
    ////////////////////////////////////////

    public void changeSet(EventType[] added, EventType[] removed) throws InterruptedException
    {
        final List _addedList = new ArrayList();

        final List _removedList = new ArrayList();

        boolean _modified = false;

        try
        {
            readWriteLock_.writeLock().acquire();

            Set _modifiedSet = new TreeSet(eventTypeSet_);

            for (int x = 0; x < removed.length; ++x)
            {
                EventTypeWrapper event = new EventTypeWrapper(removed[x]);
                _modifiedSet.remove(event);
            }

            for (int x = 0; x < added.length; ++x)
            {
                EventTypeWrapper event = new EventTypeWrapper(added[x]);
                _modifiedSet.add(event);
            }

            Iterator _i = _modifiedSet.iterator();

            while (_i.hasNext())
            {
                Object _eventType = _i.next();

                if (!eventTypeSet_.contains(_eventType))
                {
                    _addedList.add(_eventType);
                    _modified = true;
                }
            }

            _i = eventTypeSet_.iterator();

            while (_i.hasNext())
            {
                Object _eventType = _i.next();

                if (!_modifiedSet.contains(_eventType))
                {
                    _removedList.add(_eventType);
                    _modified = true;
                }
            }

            if (_modified)
            {
                eventTypeSet_ = _modifiedSet;

                synchronized (arrayViewLock_)
                {
                    setModified_ = true;
                }
            }
        } finally
        {
            readWriteLock_.writeLock().release();
        }

        if (_modified)
        {
            fireSetChanged(_addedList, _removedList);
        }
    }

    private void fireSetChanged(List added, List removed)
    {
        EventType[] _addedArray = new EventType[added.size()];

        for (int x = 0; x < _addedArray.length; ++x)
        {
            _addedArray[x] = ((EventTypeWrapper) added.get(x)).getEventType();
        }

        EventType[] _removedArray = new EventType[removed.size()];

        for (int x = 0; x < _removedArray.length; ++x)
        {
            _removedArray[x] = ((EventTypeWrapper) removed.get(x)).getEventType();
        }

        actionSetChanged(_addedArray, _removedArray);
    }

    protected abstract void actionSetChanged(EventType[] added, EventType[] removed);

    protected EventType[] getAllTypes() throws InterruptedException
    {
        try
        {
            readWriteLock_.readLock().acquire();

            updateArrayView();

            return arrayView_;

        } finally
        {
            readWriteLock_.readLock().release();
        }
    }

    private void updateArrayView()
    {
        synchronized (arrayViewLock_)
        {
            if (setModified_)
            {
                EventTypeWrapper[] _allWrapped = (EventTypeWrapper[]) eventTypeSet_
                        .toArray(EVENT_TYPE_WRAPPER_TEMPLATE);

                EventType[] _all = new EventType[_allWrapped.length];

                for (int x = 0; x < _allWrapped.length; ++x)
                {
                    _all[x] = _allWrapped[x].getEventType();
                }

                setModified_ = false;

                arrayView_ = _all;
            }
        }
    }
}