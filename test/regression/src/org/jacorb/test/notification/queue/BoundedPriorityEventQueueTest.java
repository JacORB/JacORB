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

import java.util.Iterator;
import java.util.List;

import org.jacorb.notification.interfaces.Message;
import org.jacorb.notification.queue.AbstractBoundedEventQueue;
import org.jacorb.notification.queue.BoundedPriorityEventQueue;
import org.jacorb.notification.queue.EventQueue;
import org.jacorb.notification.queue.EventQueueOverflowStrategy;
import org.jacorb.test.notification.MockMessage;
import org.jacorb.test.notification.NotificationTestCase;
import org.jacorb.test.notification.NotificationTestCaseSetup;

import EDU.oswego.cs.dl.util.concurrent.SynchronizedInt;
import junit.framework.Test;
import java.util.ArrayList;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */

public class BoundedPriorityEventQueueTest extends NotificationTestCase
{
    public BoundedPriorityEventQueueTest(String name, NotificationTestCaseSetup setup)
    {
        super(name, setup);
    }


    public static Test suite() throws Exception
    {
        return NotificationTestCase.suite(BoundedPriorityEventQueueTest.class);
    }


    public void testPriorityOrder_ascendingInsert() throws Exception {
        BoundedPriorityEventQueue _queue =
            new BoundedPriorityEventQueue(20, EventQueueOverflowStrategy.FIFO);

        for (int x=0; x<10; ++x) {
            Message _event =
                new MockMessage( "Prio:" + x).getHandle();

            _event.setPriority(x);
            _queue.put(_event);
        }

        for (int x=9; x>=0; x--) {
            Message _event = _queue.getEvent(false);
            assertEquals(x, _event.getPriority());
        }
    }


    public void testPriorityOrder_descendingInsert() throws Exception {
        EventQueue _queue =
            new BoundedPriorityEventQueue(20, EventQueueOverflowStrategy.FIFO);

        for (int x=0; x<10; ++x) {
            int prio = 10 - x;

            MockMessage _mockMessage = new MockMessage( "Prio: " + prio);

            _mockMessage.configure(getConfiguration());

            Message _event = _mockMessage.getHandle();

            _event.setPriority(prio);
            _queue.put(_event);
        }

        for (int x=0; x<10; ++x) {
            Message _event = _queue.getEvent(false);
            assertEquals(10 - x, _event.getPriority());
        }
    }



    public void testFIFOOverflow() throws Exception {

        final SynchronizedInt called = new SynchronizedInt(0);

        final List removedEvents = new ArrayList();

        EventQueueOverflowStrategy strategy = new
            EventQueueOverflowStrategy() {
                public Message removeElementFromQueue(AbstractBoundedEventQueue queue) {
                    called.increment();

                    Message e =
                        EventQueueOverflowStrategy.FIFO.removeElementFromQueue(queue);

                    removedEvents.add(e);

                    return e;
                }
            };

        List _events = new ArrayList();

        Message e1 = new MockMessage().getHandle();
        Message e2 = new MockMessage().getHandle();
        Message e3 = new MockMessage().getHandle();
        Message e4 = new MockMessage().getHandle();
        Message e5 = new MockMessage().getHandle();

        _events.add(e1);

        _events.add(e2);

        _events.add(e3);

        _events.add(e4);

        _events.add(e5);

        addEventsToEventQueue(strategy, _events);

        assertEquals(1, called.get());

        assertEquals(1, removedEvents.size());

        assertTrue(removedEvents.contains(e1));
    }

    public void testLIFOOverflow() throws Exception {

        final SynchronizedInt called = new SynchronizedInt(0);

        final List removedEvents = new ArrayList();

        EventQueueOverflowStrategy strategy = new
            EventQueueOverflowStrategy() {
                public Message removeElementFromQueue(AbstractBoundedEventQueue queue) {
                    called.increment();

                    Message e =
                        EventQueueOverflowStrategy.LIFO.removeElementFromQueue(queue);

                    removedEvents.add(e);

                    return e;
                }
            };

        List _events = new ArrayList();

        _events.add(new MockMessage( "#1").getHandle());

        _events.add(new MockMessage( "#2").getHandle());

        _events.add(new MockMessage( "#3").getHandle());

        Message e1 = new MockMessage( "#4").getHandle();

        Message e2 = new MockMessage("#5").getHandle();

        _events.add(e1);

        _events.add(e2);

        _events.add(new MockMessage( "#6").getHandle());

        addEventsToEventQueue(strategy, _events);

        assertEquals(2, called.get());

        assertEquals(2, removedEvents.size());

        assertTrue(removedEvents.contains(e1));

        assertTrue(removedEvents.contains(e2));
    }


    private void addEventsToEventQueue(EventQueueOverflowStrategy strategy,
                                       List events) {

        AbstractBoundedEventQueue queue = new BoundedPriorityEventQueue(4, strategy);

        Iterator i = events.iterator();

        while (i.hasNext()) {
            queue.put((Message)i.next());
        }
    }
}
