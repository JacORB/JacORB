package org.jacorb.notification.engine;

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
import java.util.HashMap;
import java.util.Map;

import org.jacorb.notification.interfaces.FilterStage;
import org.jacorb.notification.interfaces.Message;
import org.omg.CORBA.AnyHolder;
import org.omg.CosNotifyFilter.UnsupportedFilterableData;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */

public class FilterProxySupplierTask extends AbstractFilterTask
{
    static class AlternateMessageMap
    {
        private final Map alternateMessages_;

        //////////////////////////////

        public AlternateMessageMap()
        {
            this(new HashMap());
        }

        AlternateMessageMap(Map m)
        {
            alternateMessages_ = m;
        }

        //////////////////////////////

        public Message getAlternateMessage(FilterStage s)
        {
            if (alternateMessages_.containsKey(s))
            {
                return (Message) alternateMessages_.get(s);
            }
            return null;
        }

        public void putAlternateMessage(FilterStage s, Message e)
        {
            alternateMessages_.put(s, e);
        }

        public void clear()
        {
            alternateMessages_.clear();
        }
    }

    public static final AlternateMessageMap EMPTY_MAP = new AlternateMessageMap(
            Collections.EMPTY_MAP)
    {
        public void clear()
        {
        }
    };

    ////////////////////////////////////////

    final AlternateMessageMap changedMessages_ = new AlternateMessageMap();

    private static int sCount = 0;

    private int id_ = ++sCount;

    ////////////////////////////////////////

    public FilterProxySupplierTask(TaskExecutor te, TaskProcessor tp, TaskFactory tc)
    {
        super(te, tp, tc);
    }

    ////////////////////////////////////////

    public String toString()
    {
        return "[FilterProxySupplierTask#" + id_ + "]";
    }

    public void reset()
    {
        super.reset();

        arrayCurrentFilterStage_ = EMPTY_FILTERSTAGE;
        changedMessages_.clear();
    }

    public void doFilter() throws InterruptedException
    {
        filter();

        AbstractDeliverTask.scheduleTasks(getTaskFactory().newPushToConsumerTask(this));
    }

    private Message updatePriority(int indexOfCurrentEvent, Message m)
    {
        AnyHolder _priorityFilterResult = new AnyHolder();

        Message _currentMessage = m;

        try
        {
            boolean priorityMatch = m.match(arrayCurrentFilterStage_[indexOfCurrentEvent]
                    .getPriorityFilter(), _priorityFilterResult);

            if (priorityMatch)
            {
                _currentMessage = (Message) getMessage().clone();

                _currentMessage.setPriority(_priorityFilterResult.value.extract_long());
            }
        } catch (UnsupportedFilterableData e)
        {
            //             logger_.error("error evaluating PriorityFilter", e);
        }

        return _currentMessage;
    }

    private Message updateTimeout(int indexOfCurrentFilterStage, Message event)
    {
        AnyHolder _lifetimeFilterResult = new AnyHolder();
        Message _currentEvent = event;

        try
        {
            boolean lifetimeMatch = _currentEvent.match(
                    arrayCurrentFilterStage_[indexOfCurrentFilterStage].getLifetimeFilter(),
                    _lifetimeFilterResult);

            if (lifetimeMatch && (_currentEvent == getMessage()))
            {
                // LifeTime Mapping Filter matched and current Message
                // was not copied yet. This depends on the fact that
                // updatePriority was run before.

                _currentEvent = (Message) getMessage().clone();

                _currentEvent.setTimeout(_lifetimeFilterResult.value.extract_long());
            }

        } catch (UnsupportedFilterableData e)
        {
            //             logger_.error("error evaluating PriorityFilter", e);
        }

        return _currentEvent;
    }

    private void filter()
    {
        for (int x = 0; x < arrayCurrentFilterStage_.length; ++x)
        {
            boolean _forward = false;

            if (!arrayCurrentFilterStage_[x].isDisposed())
            {
                Message _currentMessage = getMessage();

                if (arrayCurrentFilterStage_[x].hasPriorityFilter())
                {
                    _currentMessage = updatePriority(x, _currentMessage);
                }

                if (arrayCurrentFilterStage_[x].hasLifetimeFilter())
                {
                    _currentMessage = updateTimeout(x, _currentMessage);
                }

                if (_currentMessage != getMessage())
                {
                    // MappingFilter attached to a particular
                    // FilterStage did change (Timeout or Priority)
                    // the current Message.
                    // store changed Message in Map for later use.
                    changedMessages_.putAlternateMessage(arrayCurrentFilterStage_[x],
                            _currentMessage);
                }

                _forward = _currentMessage.match(arrayCurrentFilterStage_[x]);
            }

            if (_forward)
            {
                // the subsequent destination filters need to be eval'd
                addFilterStage(arrayCurrentFilterStage_[x].getSubsequentFilterStages());
            }
        }
    }
}