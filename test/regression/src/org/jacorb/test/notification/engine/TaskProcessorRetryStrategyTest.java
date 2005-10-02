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

import edu.emory.mathcs.backport.java.util.concurrent.ScheduledFuture;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */
public class TaskProcessorRetryStrategyTest extends AbstractRetryStrategyTest
{
    private MockControl controlTaskProcessor_;

    private TaskProcessor mockTaskProcessor_;

    private MockControl controlScheduledResult_;

    private ScheduledFuture mockScheduledResult_;

    protected void setUpTest()
    {
        controlScheduledResult_ = MockControl.createControl(ScheduledFuture.class);
        mockScheduledResult_ = (ScheduledFuture) controlScheduledResult_.getMock();
        controlTaskProcessor_ = MockControl.createControl(TaskProcessor.class);
        mockTaskProcessor_ = (TaskProcessor) controlTaskProcessor_.getMock();
    }

    protected AbstractRetryStrategy newRetryStrategy()
    {
        return new TaskProcessorRetryStrategy(mockConsumer_, mockPushOperation_,
                mockTaskProcessor_, 10);
    }

    public TaskProcessorRetryStrategyTest(String name)
    {
        super(name);
    }

    public void testSuccessfulRetryDisposes() throws Exception
    {
        mockConsumer_.isRetryAllowed();
        controlConsumer_.setReturnValue(true);

        mockPushOperation_.invokePush();
        mockPushOperation_.dispose();

        replayAll();

        ((TaskProcessorRetryStrategy) objectUnderTest_).doPush();

        verifyAll();
    }

    public void testNotSuccessfulRetryDisposes() throws Exception
    {
        mockConsumer_.isRetryAllowed();
        controlConsumer_.setReturnValue(true);

        mockConsumer_.incErrorCounter();
        controlConsumer_.setDefaultReturnValue(0);

        mockConsumer_.isRetryAllowed();
        controlConsumer_.setReturnValue(false);

        mockConsumer_.destroy();

        mockPushOperation_.invokePush();
        controlPushOperation_.setDefaultThrowable(new TRANSIENT());
        mockPushOperation_.dispose();

        replayAll();

        ((TaskProcessorRetryStrategy) objectUnderTest_).doPush();

        verifyAll();
    }

    public void testFailedRetryRequeues() throws Exception
    {
        mockConsumer_.incErrorCounter();
        controlConsumer_.setDefaultReturnValue(0);

        mockConsumer_.isRetryAllowed();
        controlConsumer_.setDefaultReturnValue(true);

        mockPushOperation_.invokePush();
        controlPushOperation_.setThrowable(new TRANSIENT());

        mockTaskProcessor_.executeTaskAfterDelay(0, null);
        controlTaskProcessor_.setMatcher(MockControl.ALWAYS_MATCHER);
        controlTaskProcessor_.setReturnValue(mockScheduledResult_);

        replayAll();

        ((TaskProcessorRetryStrategy) objectUnderTest_).doPush();

        verifyAll();
    }

    private void replayAll()
    {
        controlConsumer_.replay();
        controlPushOperation_.replay();
        controlScheduledResult_.replay();
        controlTaskProcessor_.replay();
    }

    private void verifyAll()
    {
        controlConsumer_.verify();
        controlPushOperation_.verify();
        controlScheduledResult_.verify();
        controlTaskProcessor_.verify();
    }

    public static Test suite()
    {
        return new TestSuite(TaskProcessorRetryStrategyTest.class);
    }
}
