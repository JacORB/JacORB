/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2012 Gerald Brose / The JacORB Team.
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

import org.jacorb.notification.engine.AbstractFilterTask;
import org.jacorb.notification.engine.FilterProxySupplierTask;
import org.jacorb.notification.interfaces.FilterStage;
import org.junit.Test;

/**
 * @author Alphonse Bendt
 */
public class FilterProxySupplierTaskTest extends AbstractFilterTaskTestCase
{
    private FilterProxySupplierTask objectUnderTest_;
    
    protected AbstractFilterTask newObjectUnderTest()
    {
        objectUnderTest_ = new FilterProxySupplierTask(mockTaskFactory_, mockTaskExecutor_);
        
        return objectUnderTest_;
    }
    
    
    @Test
    public void testFilter() throws Exception
    {
        mockMessage_.isInvalid();
        controlMessage_.setDefaultReturnValue(false);
        
        mockMessage_.match(mockFilterStage_);
        controlMessage_.setReturnValue(false);
        
        mockMessage_.dispose();
        controlMessage_.replay();
        objectUnderTest_.setMessage(mockMessage_);
        
        mockFilterStage_.isDestroyed();
        controlFilterStage_.setDefaultReturnValue(false);
        
        mockFilterStage_.hasPriorityFilter();
        controlFilterStage_.setDefaultReturnValue(false);
        
        mockFilterStage_.hasLifetimeFilter();
        controlFilterStage_.setDefaultReturnValue(false);
       
        controlFilterStage_.replay();
        
        mockTaskFactory_.enqueueMessage(objectUnderTest_);
        controlTaskFactory_.replay();
        
        /////////////////////
        
        objectUnderTest_.setCurrentFilterStage(new FilterStage[] {mockFilterStage_});
        
        objectUnderTest_.run();
        
        /////////////////////
        
        controlMessage_.verify();
        
        controlFilterStage_.verify();
        controlTaskFactory_.verify();
    }
}
