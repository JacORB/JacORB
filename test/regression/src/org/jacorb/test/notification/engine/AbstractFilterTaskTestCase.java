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

import org.jacorb.config.Configuration;

import junit.framework.TestCase;

import org.easymock.MockControl;
import org.jacorb.notification.engine.AbstractFilterTask;
import org.jacorb.notification.engine.Schedulable;
import org.jacorb.notification.engine.TaskExecutor;
import org.jacorb.notification.engine.TaskFactory;
import org.jacorb.notification.engine.TaskProcessor;
import org.jacorb.notification.interfaces.FilterStage;
import org.jacorb.notification.interfaces.Message;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */
public abstract class AbstractFilterTaskTestCase extends TestCase
{
    protected MockControl controlMessage_;
    protected Message mockMessage_;
    protected FilterStage mockFilterStage_;
    protected MockControl controlFilterStage_;
    protected MockControl controlTaskFactory_;
    protected TaskFactory mockTaskFactory_;
    protected MockControl controlSchedulable_;
    protected Schedulable mockSchedulable_;
    protected FilterStage mockNextFilterStage_;
    protected MockControl controlTaskExecutor_;
    protected TaskExecutor mockTaskExecutor_;
    protected TaskProcessor mockTaskProcessor_;
    protected MockControl controlTaskProcessor_;
    
    private AbstractFilterTask objectUnderTest_;
    
    public AbstractFilterTaskTestCase(String name)
    {
        super(name);
    }
    
    protected final void setUp() throws Exception
    {
        super.setUp();
    
        controlTaskExecutor_ = MockControl.createControl(TaskExecutor.class);
        mockTaskExecutor_ = (TaskExecutor) controlTaskExecutor_.getMock();
        controlTaskProcessor_ = MockControl.createControl(TaskProcessor.class);
        mockTaskProcessor_ = (TaskProcessor) controlTaskProcessor_.getMock();
        controlTaskFactory_ = MockControl.createStrictControl(TaskFactory.class);
        mockTaskFactory_ = (TaskFactory) controlTaskFactory_.getMock();
        controlMessage_ = MockControl.createControl(Message.class);
        mockMessage_ = (Message) controlMessage_.getMock();
    
        controlFilterStage_ = MockControl.createControl(FilterStage.class);
        mockFilterStage_ = (FilterStage) controlFilterStage_.getMock();
    
        MockControl controlNextFilterStage = MockControl.createControl(FilterStage.class);
        mockNextFilterStage_ = (FilterStage) controlNextFilterStage.getMock();
        controlSchedulable_ = MockControl.createControl(Schedulable.class);
        mockSchedulable_ = (Schedulable) controlSchedulable_.getMock();
        
        objectUnderTest_ = newObjectUnderTest();
        
        Configuration config = Configuration.getConfiguration(null, null, false);
        
        objectUnderTest_.configure(config);
    }
    
    public final void testCreate() throws Exception
    {
        objectUnderTest_.doWork();
    }
    
    public final void testFilterInvalidMessage() throws Exception
    {
        mockMessage_.isInvalid();
        controlMessage_.setReturnValue(true);
        mockMessage_.dispose();
        
        controlMessage_.replay();
        
        controlFilterStage_.replay();
        
        objectUnderTest_.setCurrentFilterStage(new FilterStage[] {mockFilterStage_});
        
        objectUnderTest_.setMessage(mockMessage_);
        
        objectUnderTest_.run();
        
        controlMessage_.verify();
        controlFilterStage_.verify();
    }
    
    protected abstract AbstractFilterTask newObjectUnderTest();
}
