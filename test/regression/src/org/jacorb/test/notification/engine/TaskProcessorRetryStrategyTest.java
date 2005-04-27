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

import junit.framework.Test;
import junit.framework.TestSuite;

import org.easymock.MockControl;
import org.jacorb.notification.engine.AbstractRetryStrategy;
import org.jacorb.notification.engine.TaskProcessor;
import org.jacorb.notification.engine.TaskProcessorRetryStrategy;
import org.omg.CORBA.TRANSIENT;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */
public class TaskProcessorRetryStrategyTest extends AbstractRetryStrategyTest
{
    private MockControl controlTaskProcessor_;
    private TaskProcessor mockTaskProcessor_;

    protected void setUpTest()
    {
        controlTaskProcessor_ = MockControl.createControl(TaskProcessor.class);
        mockTaskProcessor_ = (TaskProcessor) controlTaskProcessor_.getMock();
    }

    protected AbstractRetryStrategy newRetryStrategy()
    {
        return new TaskProcessorRetryStrategy(mockConsumer_, mockPushOperation_, mockTaskProcessor_, 10);
    }
    
    /**
     * Constructor for TaskProcessorRetryStrategyTest.
     * 
     * @param name
     */
    public TaskProcessorRetryStrategyTest(String name)
    {
        super(name);
    }
    
    public void testSuccessfulRetryDisposes() throws Exception
    {        
        controlConsumer_.replay();
        
        mockPushOperation_.invokePush();
        mockPushOperation_.dispose();
        
        controlPushOperation_.replay();
        
        controlTaskProcessor_.replay();
        
        ((TaskProcessorRetryStrategy)objectUnderTest_).doPush();
        
        controlConsumer_.verify();
        controlPushOperation_.verify();
        controlTaskProcessor_.verify();
    }
    
    public void testNotSuccessfulRetryDisposes() throws Exception
    {        
        mockConsumer_.incErrorCounter();
        controlConsumer_.setDefaultReturnValue(0);
        
        mockConsumer_.isRetryAllowed();
        controlConsumer_.setDefaultReturnValue(false);
        
        mockConsumer_.destroy();
        controlConsumer_.replay();
        
        mockPushOperation_.invokePush();
        controlPushOperation_.setDefaultThrowable(new TRANSIENT());
        mockPushOperation_.dispose();
        
        controlPushOperation_.replay();
                
        controlTaskProcessor_.replay();
        
        ((TaskProcessorRetryStrategy)objectUnderTest_).doPush();
        
        controlConsumer_.verify();
        controlPushOperation_.verify();
        controlTaskProcessor_.verify();
    }

    
    public void testFailedRetryRequeues() throws Exception
    {
        mockConsumer_.incErrorCounter();
        controlConsumer_.setDefaultReturnValue(0);
        
        mockConsumer_.isRetryAllowed();
        controlConsumer_.setReturnValue(true, 2);
        controlConsumer_.replay();
        
        mockPushOperation_.invokePush();
        controlPushOperation_.setThrowable(new TRANSIENT());
           
        controlPushOperation_.replay();
                
        mockTaskProcessor_.executeTaskAfterDelay(0, null);
        controlTaskProcessor_.setMatcher(MockControl.ALWAYS_MATCHER);
        controlTaskProcessor_.setReturnValue(new Object());
        
        controlTaskProcessor_.replay();
        
        ((TaskProcessorRetryStrategy)objectUnderTest_).doPush();
        
        controlConsumer_.verify();
        controlPushOperation_.verify();
        controlTaskProcessor_.verify();
    }
   
    public static Test suite()
    {
        return new TestSuite(TaskProcessorRetryStrategyTest.class);
    }
}
