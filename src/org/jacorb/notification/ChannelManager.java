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
public class ChannelManager implements Disposable {

    private static final Object[] INTEGER_ARRAY_TEMPLATE = new Integer[ 0 ];

    private Map allChannels_ = new HashMap();
    private Object allChannelsLock_ = allChannels_;
    private List listEventChannelEventListener_ = new ArrayList();

    public int[] get_all_channels()
    {
        Integer[] _keys;

        synchronized(allChannelsLock_) {
            _keys = ( Integer[] ) allChannels_.keySet().toArray( INTEGER_ARRAY_TEMPLATE );
        }

        int[] _ret = new int[ _keys.length ];

        for ( int x = _keys.length - 1; x >= 0; --x )
        {
            _ret[ x ] = _keys[ x ].intValue();
        }

        return _ret;
    }

    public AbstractEventChannel get_event_channel_servant( int id )
        throws ChannelNotFound
    {
        Integer _key = new Integer(id);

        synchronized(allChannelsLock_) {
            if (allChannels_.containsKey(_key)) {
                return ( AbstractEventChannel ) allChannels_.get( _key );
            } else {
                throw new ChannelNotFound("The Channel " + id + " does not exist");
            }
        }
    }


    public void addToChannels(int key, final AbstractEventChannel channel) {
        final Integer _key = new Integer(key);

        synchronized(allChannelsLock_) {
            allChannels_.put( _key, channel );
        }

        channel.setDisposeHook(new Runnable() {
                public void run() {
                    synchronized(allChannelsLock_) {
                        allChannels_.remove( _key );
                    }

                    fireEventChannelDestroyed(channel);
                }
            });

        eventChannelServantCreated(channel);
    }

    private void fireEventChannelDestroyed(AbstractEventChannel channel) {
        if (!listEventChannelEventListener_.isEmpty())
            {
                EventChannelEvent _event =
                    new EventChannelEvent( channel );

                Iterator i = listEventChannelEventListener_.iterator();

                while ( i.hasNext() )
                    {
                        ( ( EventChannelEventListener ) i.next() ).actionEventChannelDestroyed( _event );
                    }
            }
    }

    private void eventChannelServantCreated( AbstractEventChannel servant )
    {
        EventChannelEvent _event = new EventChannelEvent( servant );

        Iterator _i = listEventChannelEventListener_.iterator();

        while ( _i.hasNext() )
        {
            ( ( EventChannelEventListener ) _i.next() ).actionEventChannelCreated( _event );
        }
    }

    public void addEventChannelEventListener( EventChannelEventListener listener )
    {
        listEventChannelEventListener_.add( listener );
    }


    public void removeEventChannelEventListener( EventChannelEventListener listener )
    {
        listEventChannelEventListener_.remove( listener );
    }


    public Iterator getChannelIterator() {
        synchronized(allChannelsLock_) {
            return allChannels_.entrySet().iterator();
        }
    }

    public void dispose() {
        synchronized(allChannelsLock_) {
            Iterator _i = allChannels_.entrySet().iterator();

            while ( _i.hasNext() )
                {
                    AbstractEventChannel _ec =
                        ( AbstractEventChannel ) ( ( Map.Entry ) _i.next() ).getValue();

                    _i.remove();
                    _ec.dispose();
                }
        }

        listEventChannelEventListener_.clear();
    }
}
