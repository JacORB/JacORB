package org.jacorb.test.notification.engine;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2012 Gerald Brose / The JacORB Team.
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

/**
 * @author Alphonse Bendt
 */
public class PushOperationTest extends TestCase
{
    static class MockPushOperation extends MessagePushOperation
    {
        int pushInvoked = 0;

        public MockPushOperation(Message message)
        {
            super(message);
        }

        public void invokePush()
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
        MockControl controlMessage = MockControl.createControl(Message.class);
        Message mockMessage = (Message) controlMessage.getMock();

        MockControl controlMessage2 = MockControl.createControl(Message.class);
        Message mockMessage2 = (Message) controlMessage2.getMock();

        mockMessage.clone();
        controlMessage.setReturnValue(mockMessage2);

        mockMessage2.dispose();
        controlMessage2.setVoidCallable();
        
        controlMessage2.replay();
        controlMessage.replay();

        MockPushOperation operation = new MockPushOperation(mockMessage);
        operation.dispose();

        controlMessage2.verify();
        controlMessage.verify();
    }

    public static TestSuite suite()
    {
        TestSuite suite = new TestSuite(PushOperationTest.class);

        return suite;
    }
}