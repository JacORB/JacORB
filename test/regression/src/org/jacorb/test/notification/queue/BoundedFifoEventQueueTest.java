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
import java.util.Vector;

import org.jacorb.notification.interfaces.Message;
import org.jacorb.notification.queue.AbstractBoundedEventQueue;
import org.jacorb.notification.queue.BoundedFifoEventQueue;
import org.jacorb.notification.queue.EventQueueOverflowStrategy;
import org.jacorb.test.notification.MockMessage;

import EDU.oswego.cs.dl.util.concurrent.SynchronizedInt;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */

public class BoundedFifoEventQueueTest extends TestCase
{
    public BoundedFifoEventQueueTest (String name)
    {
        super(name);
    }

    private void addEventsToEventQueue(EventQueueOverflowStrategy strategy,
                                       List events) {

        BoundedFifoEventQueue queue = new BoundedFifoEventQueue(4, strategy);

        Iterator i = events.iterator();

        while (i.hasNext()) {
            queue.put((Message)i.next());
        }
    }


    public void testFIFOOverflow() throws Exception {

        final SynchronizedInt called = new SynchronizedInt(0);

        final Vector removedEvents = new Vector();

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

        List _events = new Vector();

        Message e1 = new MockMessage().getHandle();
        Message e2 = new MockMessage().getHandle();

        _events.add(e1);

        _events.add(e2);

        _events.add(new MockMessage().getHandle());

        _events.add(new MockMessage().getHandle());

        _events.add(new MockMessage().getHandle());

        _events.add(new MockMessage().getHandle());

        addEventsToEventQueue(strategy, _events);

        assertEquals(2, called.get());

        assertEquals(2, removedEvents.size());

        assertTrue(removedEvents.contains(e1));

        assertTrue(removedEvents.contains(e2));
    }

    public void testLIFOOverflow() throws Exception {

        final SynchronizedInt called = new SynchronizedInt(0);

        final Vector removedEvents = new Vector();

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

        List _events = new Vector();

        _events.add(new MockMessage("#1").getHandle());

        _events.add(new MockMessage("#2").getHandle());

        _events.add(new MockMessage("#3").getHandle());

        Message e1 = new MockMessage("#4").getHandle();

        Message e2 = new MockMessage("#5").getHandle();

        _events.add(e1);

        _events.add(e2);

        _events.add(new MockMessage("#6").getHandle());

        addEventsToEventQueue(strategy, _events);

        assertEquals(2, called.get());

        assertEquals(2, removedEvents.size());

        assertTrue(removedEvents.contains(e1));

        assertTrue(removedEvents.contains(e2));

    }


    public static Test suite()
    {
        TestSuite suite = new TestSuite(BoundedFifoEventQueueTest.class);

        return suite;
    }
}

