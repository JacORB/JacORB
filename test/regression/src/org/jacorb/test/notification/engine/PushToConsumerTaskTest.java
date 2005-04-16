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
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.easymock.MockControl;
import org.jacorb.notification.engine.PushToConsumerTask;
import org.jacorb.notification.engine.TaskProcessor;
import org.jacorb.notification.interfaces.Message;
import org.jacorb.notification.interfaces.MessageConsumer;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */
public class PushToConsumerTaskTest extends TestCase
{
    private PushToConsumerTask objectUnderTest_;

    private MockControl controlMessage_;

    private Message mockMessage_;

    private MockControl controlMessageConsumer_;

    private MessageConsumer mockMessageConsumer_;

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();

        MockControl controlTaskProcessor = MockControl.createControl(TaskProcessor.class);
        TaskProcessor mockTaskProcessor = (TaskProcessor) controlTaskProcessor.getMock();

        controlMessage_ = MockControl.createControl(Message.class);
        mockMessage_ = (Message) controlMessage_.getMock();

        controlMessageConsumer_ = MockControl.createControl(MessageConsumer.class);
        mockMessageConsumer_ = (MessageConsumer) controlMessageConsumer_.getMock();

        objectUnderTest_ = new PushToConsumerTask(mockTaskProcessor);
    }

    /**
     * Constructor for PushToConsumerTaskTest.
     * 
     * @param name
     */
    public PushToConsumerTaskTest(String name)
    {
        super(name);
    }

    public void testDoWork() throws Exception
    {
        mockMessageConsumer_.deliverMessage(mockMessage_);
        controlMessageConsumer_.replay();
        
        mockMessage_.isInvalid();
        controlMessage_.setReturnValue(false);
        mockMessage_.dispose();
        controlMessage_.replay();
        
        objectUnderTest_.setMessage(mockMessage_);
        objectUnderTest_.setMessageConsumer(mockMessageConsumer_);

        objectUnderTest_.run();
        
        controlMessage_.verify();
    }
    
    public static Test suite() throws Exception
    {
        return new TestSuite(PushToConsumerTaskTest.class);
    }
}
