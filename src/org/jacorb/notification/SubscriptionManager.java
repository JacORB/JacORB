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

import org.omg.CORBA.UNKNOWN;
import org.omg.CosNotification.EventType;
import org.omg.CosNotifyComm.InvalidEventType;
import org.omg.CosNotifyComm.NotifySubscribeOperations;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.avalon.framework.logger.Logger;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */

public class SubscriptionManager
    extends EventTypeSet
    implements NotifySubscribeOperations {

    private List listeners_ = new ArrayList();

    ////////////////////////////////////////

    public void addListener(NotifySubscribeOperations listener) {
        synchronized(listeners_) {
            listeners_.add(listener);
        }
    }


    public void removeListener(NotifySubscribeOperations listener) {
        synchronized(listeners_) {
            listeners_.remove(listener);
        }
    }


    public void actionSetChanged(EventType[] added, EventType[] removed) {
        synchronized(listeners_) {
            // use a iterator on a copy of the original list here.
            // otherwise the iterator would fail if the list would be
            // modified concurrently which may happen during
            // subscription_change.
            Iterator _i = new ArrayList(listeners_).iterator();

            while (_i.hasNext()) {
                NotifySubscribeOperations _listener = (NotifySubscribeOperations)_i.next();

                try {
                    _listener.subscription_change(added, removed);
                } catch (Throwable t) {
                    logger_.error("unable to subscription_change", t);
                }
            }
        }
    }


    public void subscription_change(EventType[] added, EventType[] removed) throws InvalidEventType {
        try {
            changeSet(added, removed);
        } catch (InterruptedException e) {
            logger_.fatalError("interrupted", e);
        }
    }


    public EventType[] obtain_subscription_types() {
        try {
            return getAllTypes();
        } catch (InterruptedException e) {
            logger_.fatalError("interrupted", e);

            return null;
        }
    }
}
