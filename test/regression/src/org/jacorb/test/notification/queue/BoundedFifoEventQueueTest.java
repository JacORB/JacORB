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
import org.jacorb.notification.queue.BoundedFifoEventQueue;
import org.jacorb.notification.queue.EventQueueOverflowStrategy;

/**
 * @author Alphonse Bendt
 */

public class BoundedFifoEventQueueTest extends TestCase
{
    private void addEventsToEventQueue(EventQueueOverflowStrategy strategy, List events)
    {
        BoundedFifoEventQueue queue = new BoundedFifoEventQueue(4, strategy);

        Iterator i = events.iterator();

        while (i.hasNext())
        {
            queue.put((Message) i.next());
        }
    }

    public void testFIFOOverflow() throws Exception
    {
        DelegatingOverflowStrategy strategy = new DelegatingOverflowStrategy(EventQueueOverflowStrategy.FIFO);
        
        List _events = new ArrayList();

        Message mockMessage1 = newMessage();

        Message mockMessage2 = newMessage();

        _events.add(mockMessage1);

        _events.add(mockMessage2);

        _events.add(newMessage());

        _events.add(newMessage());

        _events.add(newMessage());

        _events.add(newMessage());

        addEventsToEventQueue(strategy, _events);

        assertEquals(2, strategy.getRemovedElements().size());

        assertTrue(strategy.getRemovedElements().contains(mockMessage1));

        assertTrue(strategy.getRemovedElements().contains(mockMessage2));
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

    public void testGetAllClearsQueue() throws Exception
    {
        BoundedFifoEventQueue queue = new BoundedFifoEventQueue(10, EventQueueOverflowStrategy.LIFO);
        
        assertEquals(0, queue.getAllMessages(false).length );
        
        Message m = newMessage();
        queue.put(m);
        
        Message[] mesgs = queue.getAllMessages(false);
        
        assertEquals(1, mesgs.length);
        assertEquals(m, mesgs[0]);
        
        assertEquals(0, queue.getAllMessages(false).length);
    }
    
    private Message newMessage()
    {
        MockControl controlMessage = MockControl.createControl(Message.class);
        Message mockMessage = (Message) controlMessage.getMock();
        controlMessage.replay();
        return mockMessage;
    }

    public static Test suite()
    {
        return new TestSuite(BoundedFifoEventQueueTest.class);
    }
}

