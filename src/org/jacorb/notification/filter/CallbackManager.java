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

package org.jacorb.notification.filter;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.avalon.framework.logger.Logger;
import org.jacorb.notification.EventTypeSet;
import org.jacorb.notification.interfaces.Disposable;
import org.jacorb.notification.util.LogUtil;
import org.omg.CosNotification.EventType;
import org.omg.CosNotifyComm.InvalidEventType;
import org.omg.CosNotifyComm.NotifySubscribe;

import EDU.oswego.cs.dl.util.concurrent.SynchronizedInt;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */
public class CallbackManager extends EventTypeSet implements Disposable
{
    private final SynchronizedInt callbackIdPool_ = new SynchronizedInt(0);

    private final Map callbacks_ = new HashMap();

    private final Logger logger_ = LogUtil.getLogger(getClass().getName());

    public int attach_callback(NotifySubscribe subscriber)
    {
        final int id = callbackIdPool_.increment();

        Integer key = new Integer(id);

        callbacks_.put(key, subscriber);

        if (logger_.isInfoEnabled())
        {
            logger_.info("attach_callback: ID=" + id + " Subscriber=" + subscriber);
        }

        return id;
    }

    public void detach_callback(int id)
    {
        Object removed = callbacks_.remove(new Integer(id));

        if (logger_.isInfoEnabled())
        {
            boolean success = removed != null;
            logger_.info("detach_callback: ID=" + id + " Success=" + success);
        }
    }

    public int[] get_callbacks()
    {
        Integer[] keys = (Integer[]) callbacks_.keySet().toArray(new Integer[0]);

        int[] ids = new int[keys.length];

        for (int i = 0; i < keys.length; i++)
        {
            ids[i] = keys[i].intValue();
        }

        return ids;
    }

    protected void actionSetChanged(EventType[] added, EventType[] removed)
    {
        Iterator i = callbacks_.keySet().iterator();

        while (i.hasNext())
        {
            NotifySubscribe notifySubscribe = (NotifySubscribe) callbacks_.get(i.next());

            try
            {
                notifySubscribe.subscription_change(added, removed);
            } catch (InvalidEventType e)
            {
                logger_.error("error during subscription_change", e);
            }
        }
    }
    
    public void replaceWith(EventType[] replacement) throws InterruptedException
    {
        changeSet(replacement, getAllTypes());
    }
    
    public void dispose()
    {
        callbacks_.clear();
    }
}