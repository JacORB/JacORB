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

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.jacorb.notification.engine.PushOperation;
import org.jacorb.notification.interfaces.Message;
import org.jacorb.test.notification.MockMessage;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */
public class PushOperationTest extends TestCase {

    class MockPushOperation extends PushOperation {
        int pushInvoked = 0;

        public MockPushOperation(Message m) {
            super(m);
        }

        public void invokePush() {
            ++pushInvoked;
        }
    }

    public PushOperationTest(String name){
        super(name);
    }

    public void testCreateDispose() {
        MockMessage mockMessage = new MockMessage();
        mockMessage.setMaxRef(2);
        mockMessage.setExpectedRef(2);
        Message message = mockMessage.getHandle();
        MockPushOperation operation = new MockPushOperation(message);
        message.dispose();
        operation.dispose();

        mockMessage.validateRefCounter();
    }

    public static TestSuite suite(){
        TestSuite suite = new TestSuite(PushOperationTest.class);

        return suite;
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }
}
