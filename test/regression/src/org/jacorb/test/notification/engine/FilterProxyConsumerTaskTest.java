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

package org.jacorb.test.notification.engine;

import java.util.Arrays;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.jacorb.notification.engine.AbstractFilterTask;
import org.jacorb.notification.engine.FilterProxyConsumerTask;
import org.jacorb.notification.interfaces.FilterStage;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */
public class FilterProxyConsumerTaskTest extends AbstractFilterTaskTestCase
{
    private FilterProxyConsumerTask objectUnderTest_;

    protected AbstractFilterTask newObjectUnderTest()
    {
        objectUnderTest_ = new FilterProxyConsumerTask(mockTaskExecutor_, mockTaskProcessor_,
                mockTaskFactory_);
        
        return objectUnderTest_;
    }

    public void testMatch() throws Exception
    {
        List list = Arrays.asList(new Object[] { mockNextFilterStage_ });

        mockFilterStage_.hasLifetimeFilter();
        controlFilterStage_.setReturnValue(false);
        mockFilterStage_.hasPriorityFilter();
        controlFilterStage_.setReturnValue(false);
        mockFilterStage_.hasInterFilterGroupOperatorOR();
        controlFilterStage_.setDefaultReturnValue(false);
        mockFilterStage_.getSubsequentFilterStages();
        controlFilterStage_.setReturnValue(list);
        
        mockFilterStage_.hasPriorityFilter();
        controlFilterStage_.setDefaultReturnValue(false);

        mockFilterStage_.hasLifetimeFilter();
        controlFilterStage_.setDefaultReturnValue(false);
        
        controlFilterStage_.replay();
        
        mockMessage_.isInvalid();
        controlMessage_.setDefaultReturnValue(false);
        
        mockMessage_.match(mockFilterStage_);
        controlMessage_.setReturnValue(true);

        mockMessage_.dispose();

        controlMessage_.replay();

        mockTaskFactory_.newFilterSupplierAdminTask(objectUnderTest_);
        controlTaskFactory_.setReturnValue(mockSchedulable_);

        controlTaskFactory_.replay();

        mockSchedulable_.schedule();

        controlSchedulable_.replay();

        objectUnderTest_.setMessage(mockMessage_);
        objectUnderTest_.setCurrentFilterStage(new FilterStage[] { mockFilterStage_ });
        
        objectUnderTest_.run();

        controlFilterStage_.verify();

        controlMessage_.verify();

        controlTaskFactory_.verify();

        controlSchedulable_.verify();
    }

    public void testNoMatch() throws Exception
    {
        mockFilterStage_.hasLifetimeFilter();
        controlFilterStage_.setReturnValue(false);
        mockFilterStage_.hasPriorityFilter();
        controlFilterStage_.setReturnValue(false);
        mockFilterStage_.hasInterFilterGroupOperatorOR();
        controlFilterStage_.setDefaultReturnValue(false);
        controlFilterStage_.replay();

        mockMessage_.isInvalid();
        controlMessage_.setDefaultReturnValue(false);
        
        mockMessage_.match(mockFilterStage_);
        controlMessage_.setReturnValue(false);

        mockMessage_.dispose();

        controlMessage_.replay();

        objectUnderTest_.setMessage(mockMessage_);
        objectUnderTest_.setCurrentFilterStage(new FilterStage[] { mockFilterStage_ });
        
        objectUnderTest_.run();

        controlFilterStage_.verify();

        controlMessage_.verify();
    }

    /**
     * Constructor for FilterProxyConsumerTaskTest.
     * 
     * @param name
     */
    public FilterProxyConsumerTaskTest(String name)
    {
        super(name);
    }

    public static Test suite()
    {
        return new TestSuite(FilterProxyConsumerTaskTest.class);
    }
}