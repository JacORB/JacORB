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

package org.jacorb.test.notification.queue;

import org.easymock.MockControl;
import org.jacorb.notification.interfaces.Message;
import org.jacorb.notification.queue.BoundedDeadlineEventQueue;
import org.jacorb.notification.queue.EventQueueOverflowStrategy;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */
public class BoundedDeadlineEventQueueTest extends TestCase
{
    private BoundedDeadlineEventQueue objectUnderTest_;

    private MockControl controlMessage1_;

    private Message mockMessage1_;

    private MockControl controlMessage2_;

    private Message mockMessage2_;

    private MockControl controlMessage3_;

    private Message mockMessage3_;

    public BoundedDeadlineEventQueueTest(String name)
    {
        super(name);
    }

    protected void setUp()
    {
        objectUnderTest_ = new BoundedDeadlineEventQueue(2,
                EventQueueOverflowStrategy.EARLIEST_TIMEOUT);

        controlMessage1_ = MockControl.createControl(Message.class);
        controlMessage2_ = MockControl.createControl(Message.class);
        controlMessage3_ = MockControl.createControl(Message.class);

        mockMessage1_ = (Message) controlMessage1_.getMock();
        mockMessage2_ = (Message) controlMessage2_.getMock();
        mockMessage3_ = (Message) controlMessage3_.getMock();
    }

    public void testGetSingleNonBlocking() throws Exception
    {
        mockMessage1_.hasTimeout();
        controlMessage1_.setDefaultReturnValue(false);

        replayAll();

        assertNull(objectUnderTest_.getMessage(false));

        objectUnderTest_.put(mockMessage1_);

        assertEquals(mockMessage1_, objectUnderTest_.getMessage(false));

        verifyAll();
    }

    public void testGetAllNonBlocking() throws Exception
    {
        mockMessage1_.hasTimeout();
        controlMessage1_.setDefaultReturnValue(false);

        replayAll();

        assertEquals(0, objectUnderTest_.getAllMessages(false).length);

        objectUnderTest_.put(mockMessage1_);
        objectUnderTest_.put(mockMessage1_);

        assertEquals(2, objectUnderTest_.getAllMessages(false).length);
        verifyAll();
    }

    public void testGetAllClearsQueue() throws Exception
    {
        mockMessage1_.hasTimeout();
        controlMessage1_.setDefaultReturnValue(false);
        replayAll();

        objectUnderTest_.put(mockMessage1_);

        assertEquals(mockMessage1_, objectUnderTest_.getAllMessages(false)[0]);

        assertEquals(0, objectUnderTest_.getAllMessages(false).length);
        verifyAll();
    }

    public void testInsert() throws Exception
    {
        mockMessage1_.hasTimeout();
        controlMessage1_.setReturnValue(true);
        mockMessage1_.getTimeout();
        controlMessage1_.setReturnValue(100);

        mockMessage2_.hasTimeout();
        controlMessage2_.setReturnValue(true);
        mockMessage2_.getTimeout();
        controlMessage2_.setReturnValue(10);

        replayAll();

        objectUnderTest_.put(mockMessage1_);
        objectUnderTest_.put(mockMessage2_);

        assertEquals(mockMessage2_, objectUnderTest_.getMessage(false));

        verifyAll();
    }

    public void testOverflow() throws Exception
    {
        mockMessage1_.hasTimeout();
        controlMessage1_.setDefaultReturnValue(true);
        mockMessage1_.getTimeout();
        controlMessage1_.setDefaultReturnValue(100);

        mockMessage2_.hasTimeout();
        controlMessage2_.setDefaultReturnValue(true);
        mockMessage2_.getTimeout();
        controlMessage2_.setDefaultReturnValue(10);

        mockMessage3_.hasTimeout();
        controlMessage3_.setDefaultReturnValue(true);
        mockMessage3_.getTimeout();
        controlMessage3_.setDefaultReturnValue(1);

        replayAll();
        
        objectUnderTest_.put(mockMessage1_);
        objectUnderTest_.put(mockMessage2_);
        objectUnderTest_.put(mockMessage3_);

        assertEquals(mockMessage3_, objectUnderTest_.getMessage(false));
        assertEquals(mockMessage1_, objectUnderTest_.getMessage(false));

        assertTrue(objectUnderTest_.isEmpty());
        assertNull(objectUnderTest_.getMessage(false));

        verifyAll();
    }

    /**
     * test to provoke a bug i have found by poking around in the sources. size of array returned by
     * getEvents was size +1. also entries in queue could get lost.
     */
    public void testGetEvents() throws Exception
    {
        mockMessage1_.hasTimeout();
        controlMessage1_.setDefaultReturnValue(false);

        mockMessage2_.hasTimeout();
        controlMessage2_.setDefaultReturnValue(false);

        replayAll();
        
        objectUnderTest_.put(mockMessage1_);
        objectUnderTest_.put(mockMessage2_);

        assertEquals(1, objectUnderTest_.getMessages(1, false).length);
        assertEquals(1, objectUnderTest_.getMessages(1, false).length);
        
        verifyAll();
    }

    private void replayAll()
    {
        controlMessage1_.replay();
        controlMessage2_.replay();
        controlMessage3_.replay();
    }

    private void verifyAll()
    {
        controlMessage1_.verify();
        controlMessage2_.verify();
        controlMessage3_.verify();
    }

    public static Test suite()
    {
        return new TestSuite(BoundedDeadlineEventQueueTest.class);
    }
}