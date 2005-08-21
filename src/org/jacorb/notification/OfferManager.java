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
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.avalon.framework.logger.Logger;
import org.jacorb.notification.util.LogUtil;
import org.omg.CosNotification.EventType;
import org.omg.CosNotifyComm.InvalidEventType;
import org.omg.CosNotifyComm.NotifyPublishOperations;

/**
 * Utility class that manages subscriptions of NotifyPublishers and broadcasting of offer_change requests.
 * 
 * @author Alphonse Bendt
 * @version $Id$
 */

public class OfferManager extends EventTypeSet implements NotifyPublishOperations
{
    public static final OfferManager NULL_MANAGER = new OfferManager(Collections.EMPTY_LIST);

    private final List listeners_;

    private final Logger logger_ = LogUtil.getLogger(getClass().getName());

    public OfferManager()
    {
        this(new ArrayList());
    }

    private OfferManager(List list) 
    {
        listeners_ = list;   
    }

    ////////////////////////////////////////

    public void addListener(NotifyPublishOperations listener)
    {
        synchronized (listeners_)
        {
            listeners_.add(listener);
        }
    }

    public void removeListener(NotifyPublishOperations listener)
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
            // modified concurrently which happens during offer_change.
            Iterator _i = new ArrayList(listeners_).iterator();

            while (_i.hasNext())
            {
                NotifyPublishOperations _listener = (NotifyPublishOperations) _i.next();

                try
                {
                    _listener.offer_change(added, removed);
                } catch (Exception e)
                {
                    logger_.error("offer_change failed for " + _listener, e);
                }
            }
        }
    }

    public void offer_change(EventType[] added, EventType[] removed) throws InvalidEventType
    {
        try
        {
            changeSet(added, removed);
        } catch (InterruptedException e)
        {
            // ignore
        }
    }

    public EventType[] obtain_offered_types()
    {
        try
        {
            return getAllTypes();
        } catch (InterruptedException e)
        {
            return EMPTY_EVENT_TYPE;
        }
    }
}