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

abstract public class FilterStageListManager {

    public interface FilterStageList {
        void add(FilterStage filterStage);
    }

    ////////////////////////////////////////

    private final Object lock_ = new Object();

    private List checkedList_ = Collections.EMPTY_LIST;

    private boolean sourceModified_;

    private List readOnlyView_ = Collections.EMPTY_LIST;

    ////////////////////////////////////////

    public void actionSourceModified() {
        synchronized(lock_) {
            sourceModified_ = true;
        }
    }


    public List getList() {
        synchronized(lock_) {
            if (sourceModified_) {
                final List _newList = new ArrayList();

                FilterStageList _listProxy = new FilterStageList() {
                        public void add(FilterStage d) {
                            if (!d.isDestroyed()) {
                                _newList.add(d);
                            }
                        }
                    };

                fetchListData(_listProxy);

                checkedList_ = _newList;
                readOnlyView_ = Collections.unmodifiableList(checkedList_);
                sourceModified_ = false;
            }
            doSortCheckedList(checkedList_);
            
            return readOnlyView_;
        }
    }

    protected void doSortCheckedList(List list)
    {
        // No OP
    }

    abstract protected void fetchListData(FilterStageList listProxy);
}
