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
import java.util.List;
import java.util.Map;

import org.apache.avalon.framework.logger.Logger;
import org.jacorb.notification.util.LogUtil;
import org.omg.CosNotifyFilter.Filter;
import org.omg.CosNotifyFilter.FilterAdminOperations;
import org.omg.CosNotifyFilter.FilterNotFound;

import EDU.oswego.cs.dl.util.concurrent.SynchronizedInt;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */

public class FilterManager implements FilterAdminOperations
{
    public static final FilterManager EMPTY_FILTER_MANAGER = new FilterManager(Collections.EMPTY_MAP);

    private static final Integer[] INTEGER_ARRAY_TEMPLATE = new Integer[0];

    ////////////////////////////////////////

    private final Map filters_; 

    private final Object filtersLock_ = new Object();

    private boolean filtersModified_;

    private final List filterList_ = new ArrayList();

    private final List filtersReadOnlyView_ = Collections.unmodifiableList(filterList_);

    private final SynchronizedInt filterIdPool_ = new SynchronizedInt(0);

    private final Logger logger_;
    
    ////////////////////////////////////////

    protected FilterManager(Map filters)
    {
        filters_ = filters;

        filtersModified_ = true;
        
        logger_ = LogUtil.getLogger(getClass().getName());
    }

    public FilterManager()
    {
        this(new HashMap());
    }

    ////////////////////////////////////////

    private Integer getFilterId()
    {
        return new Integer(filterIdPool_.increment());
    }

    public int add_filter(Filter filter)
    {
        Integer _key = getFilterId();

        if (logger_.isWarnEnabled())
        {
            try
            {
                if (!((org.omg.CORBA.portable.ObjectImpl) filter)._is_local())
                {
                    logger_.warn("filter is not local!");
                }
            } catch (Exception e)
            {
            }
        }

        synchronized (filtersLock_)
        {
            filters_.put(_key, filter);

            filtersModified_ = true;
        }

        return _key.intValue();
    }

    public void remove_filter(int filterId) throws FilterNotFound
    {
        Integer _key = new Integer(filterId);

        synchronized (filtersLock_)
        {
            if (filters_.containsKey(_key))
            {
                filters_.remove(_key);
                filtersModified_ = true;
            }
            else
            {
                throwFilterNotFound(_key);
            }
        }
    }

    public Filter get_filter(int filterId) throws FilterNotFound
    {
        Integer _key = new Integer(filterId);

        final Filter _filter;

        synchronized (filtersLock_)
        {
            _filter = (Filter) filters_.get(_key);
        }

        if (_filter == null)
        {
            throwFilterNotFound(_key);
        }

        return _filter;
    }

    private void throwFilterNotFound(Integer filterId) throws FilterNotFound
    {
        throw new FilterNotFound("Filter with ID=" + filterId + " does not exist");
    }

    public int[] get_all_filters()
    {
        final Integer[] _keys;

        synchronized (filtersLock_)
        {
            _keys = (Integer[]) filters_.keySet().toArray(INTEGER_ARRAY_TEMPLATE);
        }

        final int[] _intKeys = new int[_keys.length];

        for (int x = 0; x < _keys.length; ++x)
        {
            _intKeys[x] = _keys[x].intValue();
        }

        return _intKeys;
    }

    public void remove_all_filters()
    {
        synchronized (filtersLock_)
        {
            filters_.clear();
            filtersModified_ = true;
        }
    }

    public List getFilters()
    {
        synchronized (filtersLock_)
        {
            if (filtersModified_)
            {
                filterList_.clear();

                filterList_.addAll(filters_.values());

                filtersModified_ = false;
            }
        }
        return filtersReadOnlyView_;
    }
}
