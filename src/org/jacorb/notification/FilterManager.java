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


import org.omg.CosNotifyFilter.FilterNotFound;
import org.omg.CosNotifyFilter.Filter;
import org.omg.CosNotifyFilter.FilterAdminOperations;
import java.util.List;
import java.util.Vector;
import java.util.Iterator;
import java.util.Collections;

/**
 * FilterManager.java
 *
 *
 * Created: Thu Jan 09 16:15:08 2003
 *
 * @author <a href="mailto:bendt@inf.fu-berlin.de">Alphonse Bendt</a>
 * @version $Id$
 */

public class FilterManager implements FilterAdminOperations {
    protected List filters_;
    protected List filtersReadOnlyView_;
    protected int filterIdPool_ = 0;
    

    public static FilterManager EMPTY = new FilterManager(Collections.EMPTY_LIST);
    
    protected FilterManager(List list) {
	filters_ = list;
	filtersReadOnlyView_ = Collections.unmodifiableList(filters_);
    }

    FilterManager() {
	this(new Vector());
    }
    
    protected int getFilterId() {
	return ++filterIdPool_;
    }

    // Implementation of org.omg.CosNotifyFilter.FilterAdminOperations

    public int add_filter(Filter filter) {
	int _key = getFilterId();

	KeyedListEntry _entry = new KeyedListEntry(_key, filter);
	filters_.add(_entry);

	return _key;
    }

    public void remove_filter(int filterId) throws FilterNotFound {
	Iterator _i = filters_.iterator();
	while (_i.hasNext()) {
	    KeyedListEntry _entry = (KeyedListEntry)_i.next();
	    if (_entry.key_ == filterId) {
		_i.remove();
		return;
	    }
	}
	throw new FilterNotFound();
    }

    public Filter get_filter(int filterId) throws FilterNotFound {
	Iterator _i = filters_.iterator();
	while (_i.hasNext()) {
	    KeyedListEntry _entry = (KeyedListEntry)_i.next();
	    if (_entry.key_ == filterId) {
		return (Filter)_entry.getValue();
	    }
	}
	throw new FilterNotFound();
    }

    public int[] get_all_filters() {
	int[] _allKeys = new int[filters_.size()];
	
	Iterator _i = filters_.iterator();
	int x=0;
	while (_i.hasNext()) {
	    KeyedListEntry _entry = (KeyedListEntry)_i.next();
	    _allKeys[x++] = _entry.key_;
	}

	return _allKeys;
    }

    public void remove_all_filters() {
	filters_.clear();
    }

    public List getFilters() {
	return filtersReadOnlyView_;
    }
}// FilterManager
