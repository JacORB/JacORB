package org.jacorb.notification;

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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.logger.Logger;
import org.jacorb.notification.interfaces.Disposable;
import org.omg.CORBA.ORB;
import org.omg.CosNotification.EventType;
import org.omg.CosNotifyComm.InvalidEventType;
import org.omg.CosNotifyComm.NotifySubscribe;
import org.omg.CosNotifyComm.NotifySubscribePOA;
import org.omg.CosNotifyFilter.CallbackNotFound;
import org.omg.CosNotifyFilter.Filter;
import org.omg.CosNotifyFilter.FilterAdminOperations;
import org.omg.CosNotifyFilter.FilterNotFound;

import EDU.oswego.cs.dl.util.concurrent.SynchronizedInt;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */

public class FilterManager
    implements FilterAdminOperations,
               SubscriptionChangeListener,
               Configurable
{
    public static final FilterManager EMPTY_FILTER_MANAGER =
        new FilterManager( Collections.EMPTY_MAP );

    private static final Integer[] INTEGER_ARRAY_TEMPLATE = new Integer[0];

    ////////////////////////////////////////

    private Map filters_;

    private ORB orb_;

    private Object filtersLock_;

    private boolean filtersModified_;

    private List filtersReadOnlyView_;

    private SynchronizedInt filterIdPool_ = new SynchronizedInt(0);

    private Map filterId2callbackId_ = new Hashtable();

    private Logger logger_ = null;

    private org.jacorb.config.Configuration config_ = null;

    ////////////////////////////////////////

    protected FilterManager( Map filters )
    {
        filters_ = filters;

        filtersLock_ = filters;

        filtersModified_ = true;
    }


    public FilterManager(ORB orb)
    {
        this( new HashMap() );

        setORB(orb);
    }


    public void configure (Configuration conf)
    {
        config_ = ((org.jacorb.config.Configuration)conf);
        logger_ = config_.getNamedLogger(getClass().getName());
    }

    ////////////////////////////////////////

    private Integer getFilterId()
    {
        return new Integer(filterIdPool_.increment());
    }


    public int add_filter( Filter filter )
    {
        Integer _key = getFilterId();

        if (logger_.isWarnEnabled()) {
            try {
                if (!((org.omg.CORBA.portable.ObjectImpl)filter)._is_local()) {
                    logger_.warn("filter is not local!");
                }
            } catch (Exception e) {}
        }

        synchronized(filtersLock_) {
            filters_.put(_key, filter);

            filtersModified_ = true;
        }

        return _key.intValue();
    }


    public void remove_filter( int filterId ) throws FilterNotFound
    {
        Integer _key = new Integer(filterId);

        synchronized(filtersLock_) {
            if (filters_.containsKey(_key)) {
                filters_.remove(_key);
                filtersModified_ = true;
            } else {
                throw new FilterNotFound("Filter with ID=" + _key + " does not exist");
            }
        }
    }


    public Filter get_filter( int filterId ) throws FilterNotFound
    {
        Integer _key = new Integer(filterId);

        Filter _filter;

        synchronized (filtersLock_) {
            _filter = (Filter)filters_.get(_key);
        }

        if (_filter == null) {
            throw new FilterNotFound("Filter with ID=" + _key + " does not exist");
        } else {
            return _filter;
        }
    }


    public int[] get_all_filters()
    {
        Integer[] _keys;

        synchronized(filtersLock_) {
            _keys = (Integer[])filters_.keySet().toArray(INTEGER_ARRAY_TEMPLATE);
        }

        int[] _intKeys = new int[ _keys.length ];

        for (int x=0; x<_keys.length; ++x) {
            _intKeys[x] = _keys[x].intValue();
        }

        return _intKeys;
    }


    public void remove_all_filters()
    {
        synchronized(filtersLock_) {
            filters_.clear();
            filtersModified_ = true;
        }
    }


    public List getFilters()
    {
        synchronized(filtersLock_) {
            if (filtersModified_) {
                List _filterReadOnlyView = new ArrayList();

                _filterReadOnlyView.addAll(filters_.values());

                filtersReadOnlyView_ = Collections.unmodifiableList(_filterReadOnlyView);

                filtersModified_ = false;
            }
        }
        return filtersReadOnlyView_;
    }


    public void subscriptionChangedForFilter(int filterId,
                                             EventType[] eventType1,
                                             EventType[] eventType2) {

    }


    private void attachFilterListener(int filterId, Filter filter) {
        FilterCallback filterCallback =
            new FilterCallback(this,
                               getORB(),
                               filterId,
                               filter);

        filterCallback.configure (config_);
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


    public ORB getORB() {
        return orb_;
    }


    public void setORB(ORB orb) {
        orb_ = orb;
    }
}


interface SubscriptionChangeListener {
    void subscriptionChangedForFilter(int filterId,
                                      EventType[] eventTypeArray,
                                      EventType[] eventTypeArray1);

}


class FilterCallback
    extends NotifySubscribePOA
    implements Disposable,
               Configurable
{

    int callbackId_;

    int filterId;

    Filter filter_;

    NotifySubscribe notifySubscribe_;

    SubscriptionChangeListener subscriptionChangeListener_;
    private Logger logger_ = null;
    private org.jacorb.config.Configuration config_ = null;


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

    public void configure (Configuration conf)
    {
        config_ = ((org.jacorb.config.Configuration)conf);
        logger_ = config_.getNamedLogger(getClass().getName());
    }

    ////////////////////////////////////////

    private void attach() {
        callbackId_ = filter_.attach_callback(notifySubscribe_);
    }


    private void detach() {
        try {
            filter_.detach_callback(callbackId_);
        } catch (CallbackNotFound e) {
//             logger_.error("error during detach", e);
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
