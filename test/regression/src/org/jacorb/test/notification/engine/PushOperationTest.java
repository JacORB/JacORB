package org.jacorb.test.notification.engine;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2003  Gerald Brose.
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
 */

import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.easymock.MockControl;
import org.jacorb.notification.engine.MessagePushOperation;
import org.jacorb.notification.interfaces.Message;
import org.jacorb.notification.interfaces.MessageConsumer;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */
public class PushOperationTest extends TestCase
{
    static class MockPushOperation extends MessagePushOperation
    {
        int pushInvoked = 0;

        public MockPushOperation(MessageConsumer messageConsumer, Message message)
        {
            super(messageConsumer, message);
        }

        public void invokePushInternal()
        {
            ++pushInvoked;
        }
    }

    public PushOperationTest(String name)
    {
        super(name);
    }

    public void testCreateDispose()
    {
        MockControl controlMessageConsumer = MockControl.createControl(MessageConsumer.class);
        MessageConsumer mockMessageConsumer = (MessageConsumer) controlMessageConsumer.getMock();

        MockControl controlMessage = MockControl.createControl(Message.class);
        Message mockMessage = (Message) controlMessage.getMock();

        MockControl controlMessage2 = MockControl.createControl(Message.class);
        Message mockMessage2 = (Message) controlMessage.getMock();

        mockMessage.clone();
        controlMessage.setReturnValue(mockMessage2);

        mockMessage2.dispose();

        controlMessage2.replay();
        controlMessage.replay();

        MockPushOperation operation = new MockPushOperation(mockMessageConsumer, mockMessage);
        operation.dispose();

        controlMessage2.verify();
        controlMessage.verify();
    }

    public static TestSuite suite()
    {
        TestSuite suite = new TestSuite(PushOperationTest.class);

        return suite;
    }

    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(suite());
    }
}