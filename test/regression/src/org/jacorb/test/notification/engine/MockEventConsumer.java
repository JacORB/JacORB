package org.jacorb.test.notification.engine;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jacorb.notification.interfaces.Message;
import org.jacorb.notification.interfaces.MessageConsumer;
import org.jacorb.notification.queue.BoundedPriorityEventQueue;
import org.jacorb.notification.queue.EventQueue;
import org.jacorb.notification.queue.EventQueueOverflowStrategy;
import org.jacorb.notification.engine.TaskExecutor;

import EDU.oswego.cs.dl.util.concurrent.SynchronizedInt;
import junit.framework.Assert;

public class MockEventConsumer implements MessageConsumer
{
    EventQueue eventQueue =
        new BoundedPriorityEventQueue(10,
                                      EventQueueOverflowStrategy.LEAST_PRIORITY);

    List eventsReceived = new ArrayList();

    boolean deliverPossible = true;

    boolean enabled = true;

    int disposeCalled = 0;

    int expectedDisposeCalls = -1;

    List expectedEvents = new ArrayList();

    public void setErrorThreshold(int t)
    {
        errorThreshold_ = t;
    }

    int errorThreshold_ = 3;

    SynchronizedInt errorCounter = new SynchronizedInt(0);

    public void addToExcepectedEvents(Object event)
    {
        expectedEvents.add(event);
    }


    public void check()
    {
        if (expectedEvents.size() > 0)
        {
            checkExpectedEvents();
        }

        if (expectedDisposeCalls != -1)
        {
            Assert.assertEquals(expectedDisposeCalls, disposeCalled);
        }
    }


    private void checkExpectedEvents()
    {
        Iterator i = expectedEvents.iterator();

        while (i.hasNext())
        {
            Object o = i.next();

            Assert.assertTrue(expectedEvents + " does not contain " + o,
                              eventsReceived.contains(o));
        }
    }


    public void enableDelivery()
    {
        enabled = true;
    }


    public void disableDelivery()
    {
        enabled = false;
    }


    public void deliverMessage(final Message event)
    {
        if (enabled)
        {
            if (deliverPossible)
            {
                eventsReceived.add(event.toAny());

                event.dispose();
            }
            else
            {

            }
        }
        else
        {
            eventQueue.put((Message)event.clone());
        }
    }


    public void dispose()
    {
        disposeCalled++;
    }


    public boolean isDisposed()
    {
        return disposeCalled > 0;
    }


    public void deliverPendingData()
    {
        try
        {
            Message[] events = eventQueue.getAllEvents(true);
            for (int x = 0; x < events.length; ++x)
            {
                deliverMessage(events[x]);
            }
        }
        catch (Exception e)
        {
        }
    }


    public boolean hasPendingData()
    {
        return (!eventQueue.isEmpty());
    }


    public void resetErrorCounter()
    {
        errorCounter.set(0);
    }


    public int getErrorCounter()
    {
        return errorCounter.get();
    }


    public int incErrorCounter()
    {
        return errorCounter.increment();
    }


    public int getErrorThreshold()
    {
        return errorThreshold_;
    }


    public TaskExecutor getExecutor()
    {
        return TaskExecutor.getDefaultExecutor();
    }
}
