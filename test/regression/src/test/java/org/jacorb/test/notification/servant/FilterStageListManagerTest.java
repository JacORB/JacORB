/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2017 Gerald Brose / The JacORB Team.
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
 package org.jacorb.test.notification.servant;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.jacorb.notification.interfaces.FilterStage;
import org.jacorb.notification.interfaces.MessageConsumer;
import org.jacorb.notification.servant.FilterStageListManager;
import org.junit.Test;
import org.omg.CosNotifyFilter.MappingFilter;

public class FilterStageListManagerTest {

    @Test
    public void gettingListShouldNotBreakFilterStageListManagerIfSortingIsDone()
            throws InterruptedException {
        FilterStageListManagerMock m = new FilterStageListManagerMock();
        m.actionSourceModified();

        Iterator it = m.getList().iterator();
        it.next();

        m.getList();

        it.next();
    }

    private class FilterStageListManagerMock extends FilterStageListManager {
        private final int arrSize = 100;

        @Override
        protected void fetchListData(FilterStageList listProxy) {
            // This simulates the addition of 0 - 100 new filters.
            Random r = new Random();
            int max = new Float(r.nextFloat() * arrSize).intValue();
            for (int i = 0; i < max; i++) {
                listProxy.add(new FilterStageMock(i));
            }
        }

        @Override
        protected void doSortCheckedList(List list) {
            // Sorting is done in ConsumerAdminImpl class also
            List<FilterStage> stageList = list;
            Collections.sort(stageList, new Comparator<FilterStage>() {
                @Override
                public int compare(FilterStage o1, FilterStage o2) {
                    return o1.toString().compareTo(o1.toString());
                }
            });
        }
    }

    private class FilterStageMock implements FilterStage {

        private final int index;

        @Override
        public String toString() {
            return "" + index;
        }

        public FilterStageMock(int index) {
            this.index = index;
        }

        @Override
        public List getSubsequentFilterStages() {
            return null;
        }

        @Override
        public boolean isDestroyed() {
            return false;
        }

        @Override
        public List getFilters() {
            List filters = new ArrayList();
            filters.add(index);
            return filters;
        }

        @Override
        public boolean hasMessageConsumer() {
            return false;
        }

        @Override
        public boolean hasInterFilterGroupOperatorOR() {
            return false;
        }

        @Override
        public MessageConsumer getMessageConsumer() {
            return null;
        }

        @Override
        public boolean hasLifetimeFilter() {
            return false;
        }

        @Override
        public boolean hasPriorityFilter() {
            return false;
        }

        @Override
        public MappingFilter getLifetimeFilter() {
            return null;
        }

        @Override
        public MappingFilter getPriorityFilter() {
            return null;
        }

    }
}
