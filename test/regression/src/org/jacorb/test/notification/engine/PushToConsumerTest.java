package org.jacorb.test.notification.engine;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2003 Gerald Brose
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

import junit.framework.Test;

import org.easymock.MockControl;
import org.jacorb.notification.engine.DefaultTaskExecutor;
import org.jacorb.notification.engine.DefaultTaskProcessor;
import org.jacorb.notification.engine.PushToConsumerTask;
import org.jacorb.notification.interfaces.Message;
import org.jacorb.notification.interfaces.MessageConsumer;
import org.jacorb.test.notification.NotificationTestCase;
import org.jacorb.test.notification.NotificationTestCaseSetup;

/**
 * @author Alphonse Bendt
 */

public class PushToConsumerTest extends NotificationTestCase
{
    private DefaultTaskProcessor taskProcessor_;

    public void setUpTest() throws Exception
    {
        taskProcessor_ = new DefaultTaskProcessor(getConfiguration());
    }

    public void tearDownTest() throws Exception
    {
        taskProcessor_.dispose();
    }

    public void testPush() throws Exception
    {
        PushToConsumerTask _pushToConsumerTask = new PushToConsumerTask(taskProcessor_);

        MockControl controlMessage = MockControl.createControl(Message.class);
        Message mockMessage = (Message) controlMessage.getMock();

        MockControl controlMessageConsumer = MockControl.createControl(MessageConsumer.class);
        MessageConsumer mockMessageConsumer = (MessageConsumer) controlMessageConsumer.getMock();
        
        mockMessage.isInvalid();
        controlMessage.setReturnValue(false);
        
        mockMessage.dispose();
        
        controlMessage.replay();
        
        mockMessageConsumer.getExecutor();
        controlMessageConsumer.setReturnValue(DefaultTaskExecutor.getDefaultExecutor(), 2);

        mockMessageConsumer.deliverMessage(mockMessage);
        
        controlMessageConsumer.replay();
        
        _pushToConsumerTask.setMessage(mockMessage);

        _pushToConsumerTask.setMessageConsumer(mockMessageConsumer);

        _pushToConsumerTask.schedule();

        controlMessageConsumer.verify();
    }

    public PushToConsumerTest(String name, NotificationTestCaseSetup setup)
    {
        super(name, setup);
    }

    public static Test suite() throws Exception
    {
        return NotificationTestCase.suite(PushToConsumerTest.class);
    }
}