package org.jacorb.test.notification.queue;

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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.easymock.MockControl;
import org.jacorb.notification.interfaces.Message;
import org.jacorb.notification.queue.AbstractBoundedEventQueue;
import org.jacorb.notification.queue.BoundedPriorityEventQueue;
import org.jacorb.notification.queue.MessageQueue;
import org.jacorb.notification.queue.EventQueueOverflowStrategy;

/**
 * @author Alphonse Bendt
 */

public class BoundedPriorityEventQueueTest extends TestCase
{
    public BoundedPriorityEventQueueTest(String name)
    {
        super(name);
    }

    public static Test suite() throws Exception
    {
        return new TestSuite(BoundedPriorityEventQueueTest.class);
    }

    public void testPriorityOrder_ascendingInsert() throws Exception
    {
        BoundedPriorityEventQueue _queue = new BoundedPriorityEventQueue(20,
                EventQueueOverflowStrategy.FIFO);

        for (int x = 0; x < 10; ++x)
        {
            Message mockMessage = newMessage(x);
            _queue.put(mockMessage);
        }

        for (int x = 9; x >= 0; --x)
        {
            Message _event = _queue.getMessage(false);
            assertEquals(x, _event.getPriority());
        }
    }

    public void testPriorityOrder_descendingInsert() throws Exception
    {
        MessageQueue _queue = new BoundedPriorityEventQueue(20, EventQueueOverflowStrategy.FIFO);
        
        for (int x = 0; x < 10; ++x)
        {
            int prio = 10 - x;

            Message mockMessage = newMessage(prio);
            _queue.put(mockMessage);
        }

        for (int x = 0; x < 10; ++x)
        {
            Message _event = _queue.getMessage(false);
            assertEquals(10 - x, _event.getPriority());
        }

    }

    public void testFIFOOverflow() throws Exception
    {
        DelegatingOverflowStrategy strategy = new DelegatingOverflowStrategy(EventQueueOverflowStrategy.FIFO);

        List _events = new ArrayList();

        Message e1 = newMessage();
        Message e2 = newMessage();
        Message e3 = newMessage();
        Message e4 = newMessage();
        Message e5 = newMessage();

        _events.add(e1);

        _events.add(e2);

        _events.add(e3);

        _events.add(e4);

        _events.add(e5);

        addEventsToEventQueue(strategy, _events);

        assertEquals(1, strategy.getRemovedElements().size());

        assertTrue(strategy.getRemovedElements().contains(e1));
    }

    public void testLIFOOverflow() throws Exception
    {
        DelegatingOverflowStrategy strategy = new DelegatingOverflowStrategy(EventQueueOverflowStrategy.LIFO);
        
        List _events = new ArrayList();

        _events.add(newMessage());

        _events.add(newMessage());

        _events.add(newMessage());

        Message e1 = newMessage();

        Message e2 = newMessage();

        _events.add(e1);

        _events.add(e2);

        _events.add(newMessage());

        addEventsToEventQueue(strategy, _events);

        assertEquals(2, strategy.getRemovedElements().size());

        assertTrue(strategy.getRemovedElements().contains(e1));

        assertTrue(strategy.getRemovedElements().contains(e2));
    }

    private void addEventsToEventQueue(EventQueueOverflowStrategy strategy, List events)
    {
        AbstractBoundedEventQueue queue = new BoundedPriorityEventQueue(4, strategy);

        Iterator i = events.iterator();

        while (i.hasNext())
        {
            queue.put((Message) i.next());
        }
    }

    private Message newMessage()
    {
        return newMessage(0);
    }
    
    private Message newMessage(int priority)
    {
        MockControl controlMessage = MockControl.createControl(Message.class);
        Message mockMessage = (Message) controlMessage.getMock();
        mockMessage.getPriority();
        controlMessage.setDefaultReturnValue(priority);
        controlMessage.replay();
        
        return mockMessage;
    }
}