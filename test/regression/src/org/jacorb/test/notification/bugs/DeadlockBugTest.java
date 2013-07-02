/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2012 Gerald Brose / The JacORB Team.
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

package org.jacorb.test.notification.bugs;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.jacorb.notification.AnyMessage;
import org.jacorb.notification.queue.BoundedFifoEventQueue;
import org.jacorb.notification.queue.DefaultMessageQueueAdapter;
import org.jacorb.notification.queue.EventQueueOverflowStrategy;
import org.jacorb.notification.queue.MessageQueue;
import org.jacorb.notification.queue.RWLockEventQueueDecorator;

/**
 * @author Alphonse Bendt
 */
public class DeadlockBugTest extends TestCase
{
    private RWLockEventQueueDecorator objectUnderTest_;

    private Object lock_;

    protected void setUp() throws Exception
    {
        lock_ = new Object();

        MessageQueue queue = new BoundedFifoEventQueue(4, EventQueueOverflowStrategy.FIFO, lock_);

        objectUnderTest_ = new RWLockEventQueueDecorator(new DefaultMessageQueueAdapter(queue));
    }

    /**
     * Test that should provoke bug ID 544. getter thread
     * needs to invoke getMessageBlocking() on an empty queue first. then second putter thread tries to
     * enqueue a message into the queue which then should be returned to the first thread.
     * wrong synchronization led to a deadlock.
     */
    public void testDeadlock() throws Exception
    {
        final AnyMessage mesg = new AnyMessage();

        final AtomicBoolean received = new AtomicBoolean(false);
        final AtomicBoolean delivered = new AtomicBoolean(false);
        final CountDownLatch threadsDone = new CountDownLatch(1);
        final CountDownLatch getPutOrder = new CountDownLatch(1);
        final AtomicReference getException = new AtomicReference(null);
        final AtomicReference putException = new AtomicReference(null);

        final CyclicBarrier barrier = new CyclicBarrier(2, new Runnable()
        {
            public void run()
            {
                threadsDone.countDown();
            }
        });

        Runnable getCommand = new Runnable()
        {
            public void run()
            {
                try
                {
                    synchronized (lock_)
                    {
                        getPutOrder.countDown();
                        objectUnderTest_.getMessageBlocking();
                    }

                    received.set(true);
                } catch (Exception e)
                {
                    getException.set(e);
                }
                finally
                {
                    try
                    {
                        barrier.await();
                    } catch (InterruptedException e)
                    {
                        // ignored
                    } catch (BrokenBarrierException e)
                    {
                        // should not happen
                        e.printStackTrace();
                    }
                }
            }
        };

        Runnable putCommand = new Runnable()
        {
            public void run()
            {
                try
                {
                    getPutOrder.await();

                    objectUnderTest_.enqeue(mesg.getHandle());

                    delivered.set(true);
                } catch (Exception e)
                {
                    putException.set(e);
                }
                finally
                {
                    try
                    {
                        barrier.await();
                    } catch (InterruptedException e)
                    {
                        // ignored
                    } catch (BrokenBarrierException e)
                    {
                        // should not happen
                        e.printStackTrace();
                    }
                }
            }
        };

        Thread getter = new Thread(getCommand);
        getter.setDaemon(true);

        Thread putter = new Thread(putCommand);
        putter.setDaemon(true);

        getter.start();

        putter.start();

        threadsDone.await(1000, TimeUnit.MILLISECONDS);

        assertTrue(delivered.get());
        assertTrue(received.get());

        assertNull(putException.get());
        assertNull(getException.get());

        getter.interrupt();
        putter.interrupt();
    }

    public static Test suite()
    {
        return new TestSuite(DeadlockBugTest.class);
    }
}