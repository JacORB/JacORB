/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2004 Gerald Brose
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

import junit.extensions.RepeatedTest;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.jacorb.notification.AnyMessage;
import org.jacorb.notification.queue.BasicMessageQueueAdapter;
import org.jacorb.notification.queue.BoundedFifoEventQueue;
import org.jacorb.notification.queue.MessageQueue;
import org.jacorb.notification.queue.EventQueueOverflowStrategy;
import org.jacorb.notification.queue.RWLockEventQueueDecorator;

import EDU.oswego.cs.dl.util.concurrent.CyclicBarrier;
import EDU.oswego.cs.dl.util.concurrent.Latch;
import EDU.oswego.cs.dl.util.concurrent.SynchronizedBoolean;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */
public class DeadlockBugTest extends TestCase
{
    RWLockEventQueueDecorator objectUnderTest_;

    Object lock_;

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        lock_ = new Object();

        MessageQueue queue = new BoundedFifoEventQueue(4, EventQueueOverflowStrategy.FIFO, lock_);

        objectUnderTest_ = new RWLockEventQueueDecorator(new BasicMessageQueueAdapter(queue));
    }

    /**
     * Constructor for AbstractProxySupplierTest.
     * 
     * @param name
     */
    public DeadlockBugTest(String name)
    {
        super(name);
    }

    /**
     * Test that should provoke bug ID 544 setup is a bit difficult for this test. first thread
     * needs to invoke getMessageBlocking() on an empty queue first. then second thread tries to
     * enqueue a message into the queue which then should be returned to the first thread. this test
     * still is timing dependent and therefor contains Thread.sleep().
     */
    public void testDeadlock() throws Exception
    { 
        final AnyMessage mesg = new AnyMessage();

        final SynchronizedBoolean received = new SynchronizedBoolean(false);
        final SynchronizedBoolean delivered = new SynchronizedBoolean(false);
        final Latch threadsDone = new Latch();
        final Latch getPutOrder = new Latch();

        final CyclicBarrier barrier = new CyclicBarrier(2, new Runnable()
        {
            public void run()
            {
                threadsDone.release();
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
                        getPutOrder.release();
                        objectUnderTest_.getMessageBlocking();
                    }

                    received.set(true);

                    barrier.barrier();
                } catch (Exception e)
                {
                    fail();
                }
            }
        };

        Runnable putCommand = new Runnable()
        {
            public void run()
            {
                try
                {
                    getPutOrder.acquire();

                    objectUnderTest_.enqeue(mesg.getHandle());

                    delivered.set(true);

                    barrier.barrier();
                } catch (Exception e)
                {
                    fail();
                }
            }
        };

        Thread getter = new Thread(getCommand);
        getter.setDaemon(true);

        Thread putter = new Thread(putCommand);
        putter.setDaemon(true);

        getter.start();

        putter.start();

        threadsDone.attempt(1000);

        assertTrue(delivered.get());
        assertTrue(received.get());

        getter.interrupt();
        putter.interrupt();
        
        System.out.println("*");
    }

    /**
     * @return
     */
    public static Test suite()
    {
        return new TestSuite(DeadlockBugTest.class);
    }
}