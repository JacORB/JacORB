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

import java.util.Collections;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.jacorb.notification.engine.AbstractFilterTask;
import org.jacorb.notification.engine.FilterConsumerAdminTask;
import org.jacorb.notification.interfaces.FilterStage;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */
public class FilterConsumerAdminTaskTest extends AbstractFilterTaskTestCase
{
    private FilterConsumerAdminTask objectUnderTest_;

    /**
     * Constructor for FilterConsumerAdminTaskTest.
     * 
     * @param name
     */
    public FilterConsumerAdminTaskTest(String name)
    {
        super(name);
    }

    protected AbstractFilterTask newObjectUnderTest()
    {
        objectUnderTest_ = new FilterConsumerAdminTask(mockTaskFactory_, mockTaskExecutor_);
        
        return objectUnderTest_;
    }

    public void testNoConsumerAdminConnected() throws Exception
    {
        mockMessage_.isInvalid();
        controlMessage_.setReturnValue(false);
        mockMessage_.dispose();

        controlMessage_.replay();

        objectUnderTest_.setMessage(mockMessage_);

        objectUnderTest_.run();

        controlMessage_.verify();
    }

    public void testFilter() throws Exception
    {
        mockMessage_.isInvalid();
        controlMessage_.setDefaultReturnValue(false);

        mockMessage_.match(mockFilterStage_);
        controlMessage_.setReturnValue(true);
        
        mockMessage_.dispose();

        controlMessage_.replay();

        objectUnderTest_.setMessage(mockMessage_);

        mockFilterStage_.isDisposed();
        controlFilterStage_.setReturnValue(false);
        
        mockFilterStage_.hasInterFilterGroupOperatorOR();
        controlFilterStage_.setReturnValue(false);
        
        mockFilterStage_.getSubsequentFilterStages();
        controlFilterStage_.setReturnValue(Collections.singletonList(mockNextFilterStage_));
        
        controlFilterStage_.replay();

        mockTaskFactory_.newFilterProxySupplierTask(objectUnderTest_);
        controlTaskFactory_.setReturnValue(mockSchedulable_);
        
        controlTaskFactory_.replay();
        
        mockSchedulable_.schedule();
        controlSchedulable_.replay();
        
        objectUnderTest_.setCurrentFilterStage(new FilterStage[] { mockFilterStage_ });

        objectUnderTest_.run();

        controlFilterStage_.verify();
        controlMessage_.verify();
        controlTaskFactory_.verify();
        controlSchedulable_.verify();    
    }

    public static Test suite()
    {
        return new TestSuite(FilterConsumerAdminTaskTest.class);
    }
}