package org.jacorb.notification.servant;

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
import java.util.List;

import org.jacorb.notification.interfaces.FilterStage;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */

public abstract class FilterStageListManager
{
    public interface FilterStageList
    {
        void add(FilterStage filterStage);
    }

    // //////////////////////////////////////

    private final Object lock_ = new Object();

    private List checkedList_ = Collections.EMPTY_LIST;

    private boolean sourceModified_;

    private List readOnlyView_ = Collections.EMPTY_LIST;

    // //////////////////////////////////////

    public void actionSourceModified()
    {
        synchronized (lock_)
        {
            sourceModified_ = true;
        }
    }

    public List getList()
    {
        synchronized (lock_)
        {
            refreshNoLocking();

            // as readOnlyView_ delegates to checkedList_ sorting
            // will also affect the order of readOnlyView_
            doSortCheckedList(checkedList_);

            return readOnlyView_;
        }
    }

    public void refresh()
    {
        synchronized (lock_)
        {
            refreshNoLocking();
        }
    }

    private void refreshNoLocking()
    {
        if (sourceModified_)
        {
            final List _newList = new ArrayList();

            FilterStageList _listProxy = new FilterStageList()
            {
                public void add(FilterStage filterStage)
                {
                    if (!filterStage.isDestroyed())
                    {
                        _newList.add(filterStage);
                    }
                }
            };

            fetchListData(_listProxy);

            checkedList_ = _newList;
            readOnlyView_ = Collections.unmodifiableList(checkedList_);
            sourceModified_ = false;
        }
    }

    protected void doSortCheckedList(List list)
    {
        // No OP
    }

    protected abstract void fetchListData(FilterStageList listProxy);
}
