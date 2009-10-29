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

package org.jacorb.test.notification.servant;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.easymock.AbstractMatcher;
import org.easymock.MockControl;
import org.jacorb.notification.engine.TaskProcessor;
import org.jacorb.notification.interfaces.MessageSupplier;
import org.jacorb.notification.servant.PullMessagesUtility;

import java.util.concurrent.ScheduledFuture;

public class PullMessagesUtilityTest extends TestCase
{
    private PullMessagesUtility objectUnderTest_;

    private MockControl controlTaskProcessor_;

    private TaskProcessor mockTaskProcessor_;

    private MockControl controlMessageSupplier_;

    private MessageSupplier mockMessageSupplier_;

    private ScheduledFuture mockScheduledFuture_;

    private MockControl controlScheduledFuture_;

    protected void setUp() throws Exception
    {
        super.setUp();

        controlScheduledFuture_ = MockControl.createControl(ScheduledFuture.class);
        mockScheduledFuture_ = (ScheduledFuture) controlScheduledFuture_.getMock();
        controlTaskProcessor_ = MockControl.createControl(TaskProcessor.class);
        mockTaskProcessor_ = (TaskProcessor) controlTaskProcessor_.getMock();
        controlMessageSupplier_ = MockControl.createControl(MessageSupplier.class);
        mockMessageSupplier_ = (MessageSupplier) controlMessageSupplier_.getMock();
        objectUnderTest_ = new PullMessagesUtility(mockTaskProcessor_, mockMessageSupplier_);
    }

    private void replayAll()
    {
        controlScheduledFuture_.replay();
        controlTaskProcessor_.replay();
        controlMessageSupplier_.replay();
    }

    private void verifyAll()
    {
        controlScheduledFuture_.verify();
        controlTaskProcessor_.verify();
        controlMessageSupplier_.verify();
    }

    public void testIllegalArgument()
    {
        replayAll();
        try
        {
            objectUnderTest_.startTask(0);
        } catch (IllegalArgumentException e)
        {
            // expected
        }

        try
        {
            objectUnderTest_.startTask(-1);
        } catch (IllegalArgumentException e)
        {
            // expected
        }
        verifyAll();
    }

    public void testStartTaskRegistersTask()
    {
        mockTaskProcessor_.executeTaskPeriodically(1000, null, true);
        controlTaskProcessor_.setMatcher(new AbstractMatcher()
        {
            public boolean matches(Object[] expected, Object[] actual)
            {
                return expected[0].equals(actual[0]);
            }
        });
        controlTaskProcessor_.setReturnValue(mockScheduledFuture_);
        replayAll();
        objectUnderTest_.startTask(1000);
        verifyAll();
    }

    public void testMultipleStartsAreIgnored()
    {
        mockTaskProcessor_.executeTaskPeriodically(0, null, false);
        controlTaskProcessor_.setMatcher(MockControl.ALWAYS_MATCHER);
        controlTaskProcessor_.setReturnValue(mockScheduledFuture_);
        replayAll();

        objectUnderTest_.startTask(2000);
        objectUnderTest_.startTask(2000);

        verifyAll();
    }

    public void testStopNotStartedTask()
    {
        replayAll();
        objectUnderTest_.stopTask();
        verifyAll();
    }

    public void testStopTask()
    {
        mockTaskProcessor_.executeTaskPeriodically(0, null, false);
        controlTaskProcessor_.setMatcher(MockControl.ALWAYS_MATCHER);
        controlTaskProcessor_.setReturnValue(mockScheduledFuture_);
        controlScheduledFuture_.expectAndReturn(mockScheduledFuture_.cancel(true), true);
        replayAll();

        objectUnderTest_.startTask(2000);
        objectUnderTest_.stopTask();

        verifyAll();
    }

    public void testRestartNotStarted()
    {
        replayAll();
        try
        {
            objectUnderTest_.restartTask(1000);
        } catch (IllegalStateException e)
        {
            // expected
        }
        verifyAll();
    }

    public void testRestartStarted()
    {
        mockTaskProcessor_.executeTaskPeriodically(1000, null, true);
        controlTaskProcessor_.setMatcher(new AbstractMatcher()
        {
            public boolean matches(Object[] expected, Object[] actual)
            {
                return expected[0].equals(actual[0]);
            }
        });
        controlTaskProcessor_.setReturnValue(mockScheduledFuture_);
        mockScheduledFuture_.cancel(true);
        controlScheduledFuture_.setReturnValue(true);
        
        mockTaskProcessor_.executeTaskPeriodically(2000, null, true);
        controlTaskProcessor_.setReturnValue(mockScheduledFuture_);

        replayAll();
        objectUnderTest_.startTask(1000);
        objectUnderTest_.restartTask(2000);
        verifyAll();
    }

    public static Test suite() throws Exception
    {
        return new TestSuite(PullMessagesUtilityTest.class);
    }
}
