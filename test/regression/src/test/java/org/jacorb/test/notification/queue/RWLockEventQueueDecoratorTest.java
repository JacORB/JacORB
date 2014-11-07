/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2014 Gerald Brose / The JacORB Team.
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
import org.jacorb.notification.queue.MessageQueueAdapter;
import org.jacorb.notification.queue.RWLockEventQueueDecorator;
import org.junit.Before;
import org.junit.Test;

public class RWLockEventQueueDecoratorTest
{
    private RWLockEventQueueDecorator objectUnderTest_;

    private MockControl controlInitialQueue_;

    private MessageQueueAdapter mockInitialQueue_;

    private MockControl controlReplacementQueue_;

    private MessageQueueAdapter mockReplacementQueue_;

    @Before
    public void setUp() throws Exception
    {
        controlInitialQueue_ = MockControl.createControl(MessageQueueAdapter.class);
        mockInitialQueue_ = (MessageQueueAdapter) controlInitialQueue_.getMock();
        objectUnderTest_ = new RWLockEventQueueDecorator(mockInitialQueue_);

        controlReplacementQueue_ = MockControl.createControl(MessageQueueAdapter.class);
        mockReplacementQueue_ = (MessageQueueAdapter) controlReplacementQueue_.getMock();
    }

    @Test
    public void testReplaceEmpty() throws Exception
    {
        mockInitialQueue_.hasPendingMessages();
        controlInitialQueue_.setReturnValue(false);
        controlInitialQueue_.replay();
        controlReplacementQueue_.replay();

        objectUnderTest_.replaceDelegate(mockReplacementQueue_);
    }

    @Test
    public void testReplaceNonEmpty() throws Exception
    {
        final MockControl controlMessage = MockControl.createControl(Message.class);
        final Message mockMessage = (Message) controlMessage.getMock();
        final Message[] mesgs = new Message[] { mockMessage };

        mockInitialQueue_.hasPendingMessages();
        controlInitialQueue_.setReturnValue(true);

        mockInitialQueue_.getAllMessages();
        controlInitialQueue_.setReturnValue(mesgs);

        controlInitialQueue_.replay();

        mockReplacementQueue_.enqeue(mockMessage);

        controlReplacementQueue_.replay();

        objectUnderTest_.replaceDelegate(mockReplacementQueue_);
    }
}
