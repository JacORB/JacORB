package org.jacorb.notification;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2012 Gerald Brose / The JacORB Team.
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
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.jacorb.notification.util.LogUtil;
import org.omg.CosNotification.EventType;
import org.omg.CosNotifyComm.InvalidEventType;
import org.omg.CosNotifyComm.NotifySubscribeOperations;

/**
 * Utility class that manages subscriptions of NotifySubscribers and broadcasting of subscription_change
 * requests.
 * 
 * @author Alphonse Bendt
 * @version $Id$
 */

public class SubscriptionManager extends EventTypeSet implements NotifySubscribeOperations
{
    public static final SubscriptionManager NULL_MANAGER = new SubscriptionManager(Collections.EMPTY_LIST);
    
    private final Logger logger_ = LogUtil.getLogger(getClass().getName());

    private final List listeners_;

    public SubscriptionManager()
    {
        this(new ArrayList());
    }

    private SubscriptionManager(List list)
    {
        listeners_ = list;
    }

    ////////////////////////////////////////

    public void addListener(NotifySubscribeOperations listener)
    {
        synchronized (listeners_)
        {
            listeners_.add(listener);
        }
    }

    public void removeListener(NotifySubscribeOperations listener)
    {
        synchronized (listeners_)
        {
            listeners_.remove(listener);
        }
    }

    public void actionSetChanged(EventType[] added, EventType[] removed)
    {
        synchronized (listeners_)
        {
            // use a iterator on a copy of the original list here.
            // otherwise the iterator would fail if the list would be
            // modified concurrently which may happen during
            // subscription_change.
            Iterator _i = new ArrayList(listeners_).iterator();

            while (_i.hasNext())
            {
                NotifySubscribeOperations _listener = (NotifySubscribeOperations) _i.next();

                try
                {
                    _listener.subscription_change(added, removed);
                } catch (Exception e)
                {
                    logger_.warn("subscription_change failed for " + _listener, e);
                }
            }
        }
    }

    public void subscription_change(EventType[] added, EventType[] removed) throws InvalidEventType
    {
        changeSet(added, removed);
    }

    public EventType[] obtain_subscription_types()
    {
        return getAllTypes();
    }
}