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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jacorb.notification.interfaces.Disposable;
import org.jacorb.notification.interfaces.EventChannelEvent;
import org.jacorb.notification.interfaces.EventChannelEventListener;
import org.omg.CosNotifyChannelAdmin.ChannelNotFound;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */
public class ChannelManager implements Disposable
{
    private static final Object[] INTEGER_ARRAY_TEMPLATE = new Integer[0];

    private final Map channels_ = new HashMap();

    private final Object channelsLock_ = channels_;

    private boolean isChannelsModified_ = true;

    private int[] cachedKeys_;

    private final List eventListeners_ = new ArrayList();

    //////////////////////////////

    public int[] get_all_channels()
    {
        synchronized (channelsLock_)
        {
            if (isChannelsModified_)
            {
                Integer[] _keys = (Integer[]) channels_.keySet().toArray(INTEGER_ARRAY_TEMPLATE);

                cachedKeys_ = new int[_keys.length];

                for (int x = 0; x < _keys.length; ++x)
                {
                    cachedKeys_[x] = _keys[x].intValue();
                }

                isChannelsModified_ = false;
            }
        }
        return cachedKeys_;
    }

    public AbstractEventChannel get_channel_servant(int id) throws ChannelNotFound
    {
        Integer _key = new Integer(id);

        synchronized (channelsLock_)
        {
            if (channels_.containsKey(_key))
            {
                return (AbstractEventChannel) channels_.get(_key);
            }

            throw new ChannelNotFound("The Channel " + id + " does not exist");
        }
    }

    public void add_channel(int key, final AbstractEventChannel channel)
    {
        final Integer _key = new Integer(key);

        synchronized (channelsLock_)
        {
            channels_.put(_key, channel);
            isChannelsModified_ = true;
        }

        channel.addDisposeHook(new Disposable()
        {
            public void dispose()
            {
                synchronized (channelsLock_)
                {
                    channels_.remove(_key);
                    isChannelsModified_ = true;
                }

                fireChannelRemoved(channel);
            }
        });

        fireChannelAdded(channel);
    }

    private void fireChannelRemoved(AbstractEventChannel channel)
    {
        EventChannelEvent _event = new EventChannelEvent(channel);

        synchronized (eventListeners_)
        {
            Iterator i = eventListeners_.iterator();

            while (i.hasNext())
            {
                ((EventChannelEventListener) i.next()).actionEventChannelDestroyed(_event);
            }
        }
    }

    private void fireChannelAdded(AbstractEventChannel servant)
    {
        EventChannelEvent _event = new EventChannelEvent(servant);

        synchronized (eventListeners_)
        {
            Iterator i = eventListeners_.iterator();

            while (i.hasNext())
            {
                ((EventChannelEventListener) i.next()).actionEventChannelCreated(_event);
            }
        }
    }

    public void addEventChannelEventListener(EventChannelEventListener listener)
    {
        synchronized (eventListeners_)
        {
            eventListeners_.add(listener);
        }
    }

    public void removeEventChannelEventListener(EventChannelEventListener listener)
    {
        synchronized (eventListeners_)
        {
            eventListeners_.remove(listener);
        }
    }

    public Iterator getChannelIterator()
    {
        synchronized (channelsLock_)
        {
            return channels_.entrySet().iterator();
        }
    }

    public void dispose()
    {
        synchronized (channelsLock_)
        {
            Iterator i = channels_.entrySet().iterator();

            while (i.hasNext())
            {
                AbstractEventChannel _channel = (AbstractEventChannel) ((Map.Entry) i.next())
                        .getValue();

                i.remove();
                _channel.dispose();
            }
        }

        eventListeners_.clear();
    }
}