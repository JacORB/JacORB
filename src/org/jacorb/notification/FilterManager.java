package org.jacorb.notification;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2003 Gerald Brose
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.omg.CORBA.ORB;
import org.omg.CosNotification.EventType;
import org.omg.CosNotifyComm.InvalidEventType;
import org.omg.CosNotifyComm.NotifySubscribe;
import org.omg.CosNotifyComm.NotifySubscribePOA;
import org.omg.CosNotifyFilter.CallbackNotFound;
import org.omg.CosNotifyFilter.Filter;
import org.omg.CosNotifyFilter.FilterAdminOperations;
import org.omg.CosNotifyFilter.FilterNotFound;

import org.jacorb.notification.interfaces.Disposable;
import org.jacorb.util.Debug;

import org.apache.avalon.framework.logger.Logger;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */

public class FilterManager
    implements FilterAdminOperations,
               SubscriptionChangeListener
{
    protected Logger logger_ = Debug.getNamedLogger(getClass().getName());

    protected ChannelContext channelContext_;

    protected List filters_;

    protected List filtersReadOnlyView_;

    protected int filterIdPool_ = 0;

    protected Map filterId2callbackId_ = new Hashtable();

    ////////////////////////////////////////

    public static final FilterManager EMPTY_FILTER_MANAGER =
        new FilterManager( Collections.EMPTY_LIST );

    ////////////////////////////////////////

    protected FilterManager( List list )
    {
        filters_ = list;

        filtersReadOnlyView_ = Collections.unmodifiableList( filters_ );
    }


    public FilterManager(ChannelContext channelContext)
    {
        this( new ArrayList() );

        channelContext_ = channelContext;
    }

    ////////////////////////////////////////

    protected int getFilterId()
    {
        return ++filterIdPool_;
    }


    public int add_filter( Filter filter )
    {
        int _key = getFilterId();

        KeyedListEntry _entry = new KeyedListEntry( _key, filter );

        filters_.add( _entry );

        if (logger_.isWarnEnabled()) {
            try {
                if (!((org.omg.CORBA.portable.ObjectImpl)filter)._is_local()) {
                    logger_.warn("filter is not local!");
                }
            } catch (Exception e) {}
        }

        return _key;
    }


    public void remove_filter( int filterId ) throws FilterNotFound
    {
        Iterator _i = filters_.iterator();

        while ( _i.hasNext() )
        {
            KeyedListEntry _entry = ( KeyedListEntry ) _i.next();

            if ( _entry.getKey() == filterId )
            {
                _i.remove();
                return ;
            }
        }

        throw new FilterNotFound();
    }


    public Filter get_filter( int filterId ) throws FilterNotFound
    {
        Iterator _i = filters_.iterator();

        while ( _i.hasNext() )
        {
            KeyedListEntry _entry = ( KeyedListEntry ) _i.next();

            if ( _entry.getKey() == filterId )
            {
                return ( Filter ) _entry.getValue();
            }
        }

        throw new FilterNotFound();
    }


    public int[] get_all_filters()
    {
        int[] _allKeys = new int[ filters_.size() ];

        Iterator _i = filters_.iterator();
        int x = 0;

        while ( _i.hasNext() )
        {
            KeyedListEntry _entry = ( KeyedListEntry ) _i.next();
            _allKeys[ x++ ] = _entry.getKey();
        }

        return _allKeys;
    }


    public void remove_all_filters()
    {
        filters_.clear();
    }


    public List getFilters()
    {
        return filtersReadOnlyView_;
    }


    public void subscriptionChangedForFilter(int filterId,
                                             EventType[] eventType1,
                                             EventType[] eventType2) {

    }


    private void attachFilterListener(int filterId, Filter filter) {
        FilterCallback filterCallback =
            new FilterCallback(this,
                               channelContext_.getORB(),
                               filterId,
                               filter);

        filterId2callbackId_.put(new Integer(filterId),
                                 filterCallback);
    }


    private void detachFilterListener(int filterId) {
        Integer key = new Integer(filterId);

        if (filterId2callbackId_.containsKey(key)) {
            FilterCallback filterCallback =
                (FilterCallback)filterId2callbackId_.remove(key);

            filterCallback.dispose();
        }
    }
}


interface SubscriptionChangeListener {
    void subscriptionChangedForFilter(int filterId,
                                      EventType[] eventTypeArray,
                                      EventType[] eventTypeArray1);

}


class FilterCallback extends NotifySubscribePOA implements Disposable {

    Logger logger_ = Debug.getNamedLogger(getClass().getName());

    int callbackId_;

    int filterId;

    Filter filter_;

    NotifySubscribe notifySubscribe_;

    SubscriptionChangeListener subscriptionChangeListener_;

    ////////////////////////////////////////

    public FilterCallback(SubscriptionChangeListener subscriptionChangeListener,
                          ORB orb,
                          int filterId,
                          Filter filter) {
        subscriptionChangeListener_  = subscriptionChangeListener;
        filter_ = filter;
        notifySubscribe_ = _this(orb);
        attach();
    }

    ////////////////////////////////////////

    private void attach() {
        callbackId_ = filter_.attach_callback(notifySubscribe_);
    }


    private void detach() {
        try {
            filter_.detach_callback(callbackId_);
        } catch (CallbackNotFound e) {
            logger_.error("error during detach", e);
        }
    }


    public void subscription_change(EventType[] eventTypeArray,
                                    EventType[] eventTypeArray1)
        throws InvalidEventType {

        subscriptionChangeListener_.subscriptionChangedForFilter(filterId,
                                                                 eventTypeArray,
                                                                 eventTypeArray1);
    }


    public void dispose() {
        detach();
    }
}
