package org.jacorb.notification;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2003  Gerald Brose.
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
import org.omg.CosNotifyComm.InvalidEventType;

import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;

import java.util.Collections;
import EDU.oswego.cs.dl.util.concurrent.ReadWriteLock;
import EDU.oswego.cs.dl.util.concurrent.FIFOReadWriteLock;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */

abstract class EventTypeSet
    implements Configurable
{

    private static class EventTypeWrapper implements Comparable {

        private EventType wrappedEventType_;

        EventTypeWrapper(EventType wrappee) {
            wrappedEventType_ = wrappee;
        }


        public EventType getEventType() {
            return wrappedEventType_;
        }


        public String toString() {
            return "<EventType domain_name='" +
                wrappedEventType_.domain_name +
                "' type_name='" +
                wrappedEventType_.type_name +
                "'>";
        }


        public int hashCode() {
            return wrappedEventType_.hashCode();
        }


        public boolean equals(Object o) {
            try {
                EventTypeWrapper _other = (EventTypeWrapper)o;

                return _other.wrappedEventType_.domain_name.equals(wrappedEventType_.domain_name) &&
                    _other.wrappedEventType_.type_name.equals(wrappedEventType_.type_name);
            } catch (ClassCastException e) {
                return super.equals(o);
            }
        }


        public int compareTo(Object o) {
            try {
                EventTypeWrapper _other = (EventTypeWrapper)o;

                int _compare = wrappedEventType_.domain_name.compareTo(_other.wrappedEventType_.domain_name);

                if (_compare == 0) {
                    _compare = wrappedEventType_.type_name.compareTo(_other.wrappedEventType_.type_name);
                }

                return _compare;

            } catch (ClassCastException e) {
                throw new IllegalArgumentException();
            }
        }
    }

    ////////////////////////////////////////

    private final static EventTypeWrapper[] EVENT_TYPE_WRAPPER_TEMPLATE =
        new EventTypeWrapper[0];

    protected Logger logger_ = null;

    private Set eventTypeSet_ = new TreeSet();

    private ReadWriteLock readWriteLock_ = new FIFOReadWriteLock();

    private EventType[] arrayView_;

    private boolean setModified_;

    ////////////////////////////////////////
    public void configure (Configuration conf)
    {
        logger_ = ((org.jacorb.config.Configuration)conf).
            getNamedLogger( getClass().getName() );
    }

    protected void changeSet(EventType[] added,
                             EventType[] removed)
        throws InvalidEventType,
               InterruptedException
    {
        logger_.debug("changeSet");

        List _addedList = new ArrayList();

        List _removedList = new ArrayList();

        try {
            readWriteLock_.writeLock().acquire();

            Set _changedEventTypeSet = new TreeSet(eventTypeSet_);

            for (int x=0; x<added.length; ++x) {
                _changedEventTypeSet.add(new EventTypeWrapper(added[x]));
            }

            for (int x=0; x<removed.length; ++x) {
                _changedEventTypeSet.remove(new EventTypeWrapper(removed[x]));
            }


            Iterator _i = _changedEventTypeSet.iterator();

            while(_i.hasNext()) {
                Object _eventType = _i.next();

                if (!eventTypeSet_.contains(_eventType)) {
                    _addedList.add(_eventType);
                }
            }

            _i = eventTypeSet_.iterator();

            while(_i.hasNext()) {
                Object _eventType = _i.next();

                if (!_changedEventTypeSet.contains(_eventType)) {
                    _removedList.add(_eventType);
                }
            }

            if (logger_.isDebugEnabled()) {
                logger_.debug("added: " + _addedList);
                logger_.debug("removed: " + _removedList);
            }

            eventTypeSet_ = _changedEventTypeSet;

            setModified_ = true;
        } finally {
            readWriteLock_.writeLock().release();
        }

        if (!_addedList.isEmpty() || !_removedList.isEmpty()) {
            fireSetChanged(_addedList, _removedList);
        }
    }


    private void fireSetChanged(List added, List removed) {
        EventType[] _addedArray = new EventType[added.size()];

        for (int x=0; x<_addedArray.length; ++x) {
            _addedArray[x] = ((EventTypeWrapper)added.get(x)).getEventType();
        }

        EventType[] _removedArray = new EventType[removed.size()];

        for (int x=0; x<_removedArray.length; ++x) {
            _removedArray[x] = ((EventTypeWrapper)removed.get(x)).getEventType();
        }

        actionSetChanged(_addedArray, _removedArray);
    }


    abstract void actionSetChanged(EventType[] added, EventType[] removed);


    protected EventType[] getAllTypes() throws InterruptedException {
        try {
            readWriteLock_.readLock().acquire();


            synchronized(this) {
                if (setModified_ || arrayView_ == null) {

                    EventTypeWrapper[] _allWrapped =
                        (EventTypeWrapper[])eventTypeSet_.toArray(EVENT_TYPE_WRAPPER_TEMPLATE);

                    EventType[] _all = new EventType[_allWrapped.length];

                    for (int x=0; x<_allWrapped.length; ++x) {
                        _all[x] = _allWrapped[x].getEventType();
                    }

                    setModified_ = false;

                    arrayView_ = _all;
                }
            }
        } finally {
            readWriteLock_.readLock().release();
        }
        return arrayView_;
    }
}
