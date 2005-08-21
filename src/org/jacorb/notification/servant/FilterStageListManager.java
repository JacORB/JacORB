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

import org.jacorb.notification.interfaces.FilterStage;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */

abstract public class FilterStageListManager {

    public interface List {
        void add(FilterStage d);
    }

    ////////////////////////////////////////

    private final Object lock_ = new Object();

    private java.util.List checkedList_ = Collections.EMPTY_LIST;

    private boolean sourceModified_;

    private java.util.List readOnlyView_ = Collections.EMPTY_LIST;

    ////////////////////////////////////////

    public void actionSourceModified() {
        synchronized(lock_) {
            sourceModified_ = true;
        }
    }


    public java.util.List getList() {
        synchronized(lock_) {
            if (sourceModified_) {
                final java.util.List _newList = new ArrayList();

                List _listProxy = new List() {
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
            sortCheckedList(checkedList_);
            
            return readOnlyView_;
        }
    }

    protected void sortCheckedList(java.util.List list)
    {
        // No OP
    }

    abstract protected void fetchListData(List listProxy);
}
