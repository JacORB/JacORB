package org.jacorb.test.notification.engine;

import java.util.Iterator;
import java.util.Vector;

import org.jacorb.notification.interfaces.EventConsumer;
import org.jacorb.notification.interfaces.Message;
import org.jacorb.notification.queue.BoundedPriorityEventQueue;
import org.jacorb.notification.queue.EventQueue;
import org.jacorb.notification.queue.EventQueueOverflowStrategy;

import EDU.oswego.cs.dl.util.concurrent.SynchronizedInt;
import junit.framework.Assert;

import org.omg.CORBA.TRANSIENT;
import org.apache.avalon.framework.logger.Logger;
import org.jacorb.util.Debug;

class MockEventConsumer implements EventConsumer {

    Logger logger_ = Debug.getNamedLogger(getClass().getName());

    EventQueue eventQueue =
        new BoundedPriorityEventQueue(10,
                                      EventQueueOverflowStrategy.LEAST_PRIORITY);

    Vector eventsReceived = new Vector();
    boolean deliverPossible = true;
    boolean enabled = true;
    int disposeCalled = 0;
    int expectedDisposeCalls = -1;
    Vector expectedEvents = new Vector();

    SynchronizedInt errorCounter = new SynchronizedInt(0);

    public void addToExcepectedEvents(Object event) {
        expectedEvents.add(event);
    }

    public void check() {
        if (expectedEvents.size() > 0) {
            checkExceptedEvents();
        }

        if (expectedDisposeCalls != -1) {
            Assert.assertEquals(expectedDisposeCalls, disposeCalled);
        }
    }

    private void checkExceptedEvents() {
        Iterator i = expectedEvents.iterator();
        while(i.hasNext()) {
            Object o = i.next();
            Assert.assertTrue(expectedEvents + " does not contain " + o,
                              eventsReceived.contains(o));
        }
    }

    public void enableDelivery() {
        enabled = true;
    }

    public void disableDelivery() {
        enabled = false;
    }

    public void deliverEvent(Message event) {
        logger_.info("deliverEvent " + event);

        if (enabled) {
            if (deliverPossible) {
                eventsReceived.add(event.toAny());
            } else {
                throw new TRANSIENT();
            }
        } else {
            eventQueue.put(event);
        }
    }

    public void dispose() {
        disposeCalled++;
    }

    public void deliverPendingEvents() {
        logger_.debug("deliverPendingEvents");

        try {
            Message[] events = eventQueue.getAllEvents(true);
            for (int x=0; x<events.length; ++x) {
                eventsReceived.add(events[x].toAny());
            }
        } catch (Exception e) {

        }
    }

    public boolean hasPendingEvents() {
        return (!eventQueue.isEmpty());
    }

    public void resetErrorCounter() {
        errorCounter.set(0);
    }

    public int getErrorCounter() {
        return errorCounter.get();
    }

    public int incErrorCounter() {
        return errorCounter.increment();
    }
}
